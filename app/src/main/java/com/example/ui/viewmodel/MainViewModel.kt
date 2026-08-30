package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LiveStreamEntity
import com.example.data.local.SystemLogEntity
import com.example.data.local.UserEntity
import com.example.data.local.YouTubeChannelEntity
import com.example.data.model.AdminSystemStats
import com.example.data.model.EncoderMetrics
import com.example.data.model.LiveControlState
import com.example.data.model.StreamStatus
import com.example.data.repository.AdminRepository
import com.example.data.repository.LiveStreamRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.YouTubeChannelRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val userRepository = UserRepository(db.userDao())
    private val channelRepository = YouTubeChannelRepository(db.youtubeChannelDao())
    private val liveStreamRepository = LiveStreamRepository(db.liveStreamDao())
    private val adminRepository = AdminRepository(db.systemLogDao())

    // All Users
    val allUsers: StateFlow<List<UserEntity>> = userRepository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Authenticated User
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Connected YouTube Channel for current user
    val currentChannel: StateFlow<YouTubeChannelEntity?> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            channelRepository.getChannelForUser(user.id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current User's Streams (Multi-user isolated)
    val userStreams: StateFlow<List<LiveStreamEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            liveStreamRepository.getStreamsForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Streaming Engine State
    private val _liveControlState = MutableStateFlow(LiveControlState())
    val liveControlState: StateFlow<LiveControlState> = _liveControlState.asStateFlow()

    // Admin Telemetry & Metrics
    private val _adminStats = MutableStateFlow(AdminSystemStats())
    val adminStats: StateFlow<AdminSystemStats> = _adminStats.asStateFlow()

    val adminLogs: StateFlow<List<SystemLogEntity>> = adminRepository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val systemLogs: StateFlow<List<SystemLogEntity>> get() = adminLogs

    val allStreamsAcrossPlatform: StateFlow<List<LiveStreamEntity>> = liveStreamRepository.allStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allStreams: StateFlow<List<LiveStreamEntity>> get() = allStreamsAcrossPlatform

    // Event Messages (Snackbars / Toast alerts)
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()
    val toastMessage: SharedFlow<String> get() = toastEvent

    fun rebroadcastStream(stream: LiveStreamEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val channel = channelRepository.getChannelForUserSync(user.id)
            if (channel == null || !channel.isConnected) {
                _toastEvent.emit("Cannot rebroadcast: Channel not connected.")
                return@launch
            }
            startStreamingPipeline(stream, channel)
        }
    }

    // Background Jobs
    private var streamingTickerJob: Job? = null
    private var serverMetricsJob: Job? = null

    init {
        // Automatically sign in as first default user or initialize
        viewModelScope.launch {
            delay(300)
            val users = db.userDao().getUserById("usr_101")
            if (users != null) {
                _currentUser.value = users
            } else {
                AppDatabase.populateInitialData(db)
                _currentUser.value = db.userDao().getUserById("usr_101")
            }
            startServerMetricsTelemetry()
        }
    }

    // ==================== AUTHENTICATION & MULTI-USER ====================

    fun switchUser(userId: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                if (user.isSuspended) {
                    _toastEvent.emit("Account ${user.displayName} is suspended by Administrator.")
                    return@launch
                }
                // If switching user while a stream is live, ensure state machine handles properly
                _currentUser.value = user
                _toastEvent.emit("Switched to ${user.displayName} (${user.role})")
                adminRepository.logEvent("INFO", "Auth", "User switched active session to: ${user.email}", user.id)
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank()) {
                _toastEvent.emit("Please enter valid email and password")
                return@launch
            }
            val existing = userRepository.getUserByEmail(email.trim())
            if (existing != null) {
                if (existing.isSuspended) {
                    _toastEvent.emit("This account is currently suspended.")
                    return@launch
                }
                _currentUser.value = existing
                _toastEvent.emit("Welcome back, ${existing.displayName}!")
                adminRepository.logEvent("INFO", "Auth", "Successful user login: $email", existing.id)
                onSuccess()
            } else {
                // Auto create for demo or show error
                val newUser = UserEntity(
                    id = "usr_" + UUID.randomUUID().toString().take(6),
                    email = email.trim(),
                    displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    role = if (email.contains("admin", ignoreCase = true)) "ADMIN" else "USER",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
                )
                userRepository.createUser(newUser)
                _currentUser.value = newUser
                _toastEvent.emit("Account created & logged in!")
                adminRepository.logEvent("INFO", "Auth", "New user registered: $email", newUser.id)
                onSuccess()
            }
        }
    }

    fun signUp(name: String, email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || pass.length < 6) {
                _toastEvent.emit("Please provide a valid name, email and 6+ char password")
                return@launch
            }
            val newUser = UserEntity(
                id = "usr_" + UUID.randomUUID().toString().take(6),
                email = email.trim(),
                displayName = name.trim(),
                role = if (email.contains("admin", ignoreCase = true)) "ADMIN" else "USER",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
            )
            userRepository.createUser(newUser)
            _currentUser.value = newUser
            _toastEvent.emit("Welcome to YT Live Manager, ${newUser.displayName}!")
            adminRepository.logEvent("INFO", "Auth", "User registration completed: ${newUser.email}", newUser.id)
            onSuccess()
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                _toastEvent.emit("Please enter your registered email.")
                return@launch
            }
            _toastEvent.emit("Password reset instructions sent to $email (OAuth recovery)")
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                adminRepository.logEvent("INFO", "Auth", "User logged out: ${user.email}", user.id)
            }
            _currentUser.value = null
            onLoggedOut()
        }
    }

    // ==================== YOUTUBE OAUTH & CHANNEL CONNECTION ====================

    fun connectYouTubeChannel(channelName: String = "", handle: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _toastEvent.emit("Initiating Google OAuth 2.0 & YouTube Live API...")
            delay(1200)

            val name = if (channelName.isNotBlank()) channelName else "${user.displayName} Live TV"
            val chHandle = if (handle.isNotBlank()) handle else "@${user.displayName.replace(" ", "").lowercase()}_yt"
            val channelId = "UC_" + UUID.randomUUID().toString().take(12)

            val newChannel = YouTubeChannelEntity(
                channelId = channelId,
                userId = user.id,
                channelTitle = name,
                channelHandle = chHandle,
                avatarUrl = user.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150" },
                subscriberCount = "${Random.nextInt(10, 500)}.${Random.nextInt(1, 9)}K",
                isConnected = true,
                streamKeyMasked = "••••-••••-••••-${Random.nextInt(1000, 9999)}",
                ingestUrl = "rtmps://a.rtmps.youtube.com/live2",
                connectedAt = System.currentTimeMillis()
            )

            channelRepository.connectChannel(newChannel)
            adminRepository.logEvent("INFO", "YouTubeAPI", "Google OAuth grant for $name on behalf of user ${user.email}", user.id)
            _toastEvent.emit("YouTube Channel '$name' connected successfully!")
        }
    }

    fun disconnectYouTubeChannel() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            channelRepository.disconnectChannel(user.id)
            adminRepository.logEvent("WARN", "YouTubeAPI", "Channel disconnected by user ${user.email}", user.id)
            _toastEvent.emit("YouTube Channel disconnected.")
        }
    }

    fun reconnectYouTubeChannel() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _toastEvent.emit("Refreshing OAuth tokens with YouTube Live Streaming API...")
            delay(800)
            channelRepository.reconnectChannel(user.id)
            adminRepository.logEvent("INFO", "YouTubeAPI", "OAuth tokens refreshed for user ${user.email}", user.id)
            _toastEvent.emit("YouTube Channel connection refreshed and active!")
        }
    }

    // ==================== CREATE & SCHEDULE LIVE STREAM ====================

    fun createAndStartLiveStream(
        title: String,
        description: String,
        videoUrl: String,
        thumbnailUrl: String,
        category: String,
        privacy: String,
        durationMinutes: Int,
        isLooping: Boolean,
        resolutionPreset: String,
        startImmediately: Boolean,
        onSuccess: (streamId: String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            viewModelScope.launch { _toastEvent.emit("Please log in first") }
            return
        }

        if (title.isBlank()) {
            viewModelScope.launch { _toastEvent.emit("Stream title is required") }
            return
        }
        if (videoUrl.isBlank()) {
            viewModelScope.launch { _toastEvent.emit("Video source URL is required") }
            return
        }

        viewModelScope.launch {
            val channel = channelRepository.getChannelForUserSync(user.id)
            if (channel == null || !channel.isConnected) {
                _toastEvent.emit("Error: Please connect your YouTube Channel before streaming.")
                return@launch
            }

            val streamId = "stream_" + UUID.randomUUID().toString().take(8)
            val broadcastHash = UUID.randomUUID().toString().take(8)
            val ytBroadcastUrl = "https://youtube.com/live/$broadcastHash"

            val targetBitrate = when {
                resolutionPreset.contains("9,500") -> 9500
                resolutionPreset.contains("6,000") -> 6000
                resolutionPreset.contains("3,500") -> 3500
                else -> 4500
            }

            val newStream = LiveStreamEntity(
                id = streamId,
                userId = user.id,
                channelId = channel.channelId,
                title = title.trim(),
                description = description.trim(),
                videoSourceUrl = videoUrl.trim(),
                thumbnailUrl = thumbnailUrl.ifBlank { "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600" },
                category = category,
                privacy = privacy,
                scheduledStartTime = System.currentTimeMillis(),
                scheduledDurationMinutes = durationMinutes,
                isLooping = isLooping,
                resolutionPreset = resolutionPreset,
                targetBitrateKbps = targetBitrate,
                status = if (startImmediately) "LIVE" else "SCHEDULED",
                actualStartTime = if (startImmediately) System.currentTimeMillis() else null,
                youtubeBroadcastUrl = ytBroadcastUrl,
                peakViewers = if (startImmediately) Random.nextInt(120, 850) else 0,
                totalDurationSeconds = 0
            )

            liveStreamRepository.createStream(newStream)
            adminRepository.logEvent("INFO", "RTMPS", "Created YouTube Live broadcast: '$title' for channel '${channel.channelTitle}'", user.id)

            if (startImmediately) {
                startStreamingPipeline(newStream, channel)
            } else {
                _toastEvent.emit("Live Stream scheduled successfully!")
            }
            onSuccess(streamId)
        }
    }

    // ==================== LIVE STREAMING ENGINE & CONTROL PANEL ====================

    fun startStreamingPipeline(stream: LiveStreamEntity, channel: YouTubeChannelEntity) {
        streamingTickerJob?.cancel()

        _liveControlState.value = LiveControlState(
            status = StreamStatus.CONNECTING,
            streamId = stream.id,
            streamTitle = stream.title,
            videoSourceUrl = stream.videoSourceUrl,
            channelTitle = channel.channelTitle,
            channelAvatarUrl = channel.avatarUrl,
            youtubeBroadcastUrl = stream.youtubeBroadcastUrl,
            elapsedSeconds = 0,
            scheduledDurationMinutes = stream.scheduledDurationMinutes,
            isLooping = stream.isLooping,
            metrics = EncoderMetrics(
                bitrateKbps = stream.targetBitrateKbps,
                resolution = stream.resolutionPreset.substringBefore(" ")
            )
        )

        viewModelScope.launch {
            adminRepository.logEvent("INFO", "FFmpeg", "Initializing FFmpeg RTMPS pipeline for stream ${stream.id} to ${channel.ingestUrl}", stream.userId)
            delay(1500) // Initializing RTMPS handshake & hardware NVENC encoder

            liveStreamRepository.setStatus(stream.id, "LIVE", System.currentTimeMillis())

            _liveControlState.value = _liveControlState.value.copy(
                status = StreamStatus.LIVE,
                connectionQuality = "EXCELLENT"
            )

            adminRepository.logEvent("INFO", "RTMPS", "Broadcast active: Ingesting at ${stream.targetBitrateKbps} kbps. Keyframe=2.0s", stream.userId)
            _toastEvent.emit("🔴 YOU ARE LIVE! Broadcasting to YouTube channel '${channel.channelTitle}'")

            // Start live duration counter & telemetry ticker
            streamingTickerJob = viewModelScope.launch {
                var seconds = 0L
                while (isActive && _liveControlState.value.status == StreamStatus.LIVE) {
                    delay(1000)
                    seconds++

                    // Dynamic fluctuations in encoder metrics
                    val jitterBitrate = stream.targetBitrateKbps + Random.nextInt(-120, 150)
                    val jitterFps = 59.8f + Random.nextFloat() * 0.4f
                    val jitterCpu = 22.0f + Random.nextFloat() * 8.0f
                    val jitterRam = 380.0f + (seconds % 60) * 1.5f

                    _liveControlState.value = _liveControlState.value.copy(
                        elapsedSeconds = seconds,
                        metrics = _liveControlState.value.metrics.copy(
                            bitrateKbps = jitterBitrate,
                            fps = jitterFps,
                            cpuUsagePercent = jitterCpu,
                            memoryUsageMb = jitterRam,
                            networkOutMbps = (jitterBitrate / 1000.0f) * 1.05f
                        )
                    )
                }
            }
        }
    }

    fun stopLiveStream(streamId: String? = null) {
        val activeId = streamId ?: _liveControlState.value.streamId ?: return
        viewModelScope.launch {
            _liveControlState.value = _liveControlState.value.copy(status = StreamStatus.STOPPING)
            _toastEvent.emit("Stopping stream and closing RTMPS connection...")
            delay(1000)

            val elapsed = _liveControlState.value.elapsedSeconds
            val endTime = System.currentTimeMillis()
            liveStreamRepository.finishStream(activeId, "COMPLETED", endTime, elapsed)

            adminRepository.logEvent("INFO", "RTMPS", "Live broadcast $activeId stopped. Final duration: ${formatDuration(elapsed)}", _currentUser.value?.id)

            streamingTickerJob?.cancel()
            _liveControlState.value = LiveControlState(status = StreamStatus.IDLE)
            _toastEvent.emit("Live broadcast completed. Saved to Stream History.")
        }
    }

    fun triggerSimulatedReconnect() {
        if (_liveControlState.value.status != StreamStatus.LIVE) return
        viewModelScope.launch {
            _liveControlState.value = _liveControlState.value.copy(
                status = StreamStatus.RECONNECTING,
                connectionQuality = "UNSTABLE",
                reconnectAttempts = _liveControlState.value.reconnectAttempts + 1
            )
            adminRepository.logEvent("WARN", "RTMPS", "Network packet loss detected on RTMPS pipeline. Auto-reconnecting (Attempt #${_liveControlState.value.reconnectAttempts})...", _currentUser.value?.id)
            _toastEvent.emit("⚠️ Network glitch detected: Auto-reconnecting RTMPS encoder...")

            delay(2500)

            _liveControlState.value = _liveControlState.value.copy(
                status = StreamStatus.LIVE,
                connectionQuality = "EXCELLENT"
            )
            adminRepository.logEvent("INFO", "RTMPS", "RTMPS pipeline re-established successfully. Stream frame sync restored.", _currentUser.value?.id)
            _toastEvent.emit("✅ Reconnected! Stream resumed smoothly.")
        }
    }

    fun toggleAudioMute() {
        val current = _liveControlState.value.isMuted
        _liveControlState.value = _liveControlState.value.copy(isMuted = !current)
        viewModelScope.launch {
            _toastEvent.emit(if (!current) "Audio muted on live broadcast" else "Audio unmuted")
        }
    }

    // ==================== ADMIN ACTIONS ====================

    fun toggleUserSuspension(userId: String, currentSuspended: Boolean) {
        viewModelScope.launch {
            val target = userRepository.getUserById(userId) ?: return@launch
            val newStatus = !currentSuspended
            userRepository.setSuspension(userId, newStatus)
            adminRepository.logEvent(
                if (newStatus) "WARN" else "INFO",
                "Admin",
                "User '${target.displayName}' (${target.email}) was ${if (newStatus) "SUSPENDED" else "UNSUSPENDED"} by Admin.",
                userId
            )
            _toastEvent.emit("User ${target.displayName} ${if (newStatus) "suspended" else "restored"}")
        }
    }

    fun clearSystemLogs() {
        viewModelScope.launch {
            adminRepository.clearLogs()
            _toastEvent.emit("System logs cleared.")
        }
    }

    // ==================== SERVER TELEMETRY SIMULATOR ====================

    private fun startServerMetricsTelemetry() {
        serverMetricsJob?.cancel()
        serverMetricsJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                val isLive = _liveControlState.value.status == StreamStatus.LIVE
                val activeCount = if (isLive) 1 else 0

                val cpu = if (isLive) 28.0f + Random.nextFloat() * 10.0f else 12.0f + Random.nextFloat() * 4.0f
                val ram = if (isLive) 8.4f + Random.nextFloat() * 0.8f else 4.2f + Random.nextFloat() * 0.3f
                val net = if (isLive) 18.5f + Random.nextFloat() * 3.0f else 2.1f + Random.nextFloat() * 0.5f

                _adminStats.value = _adminStats.value.copy(
                    activeLiveStreams = activeCount,
                    serverCpuPercent = cpu,
                    serverRamUsedGb = ram,
                    networkBandwidthMbps = net
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        streamingTickerJob?.cancel()
        serverMetricsJob?.cancel()
    }

    companion object {
        fun formatDuration(totalSeconds: Long): String {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
