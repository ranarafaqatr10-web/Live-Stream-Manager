package com.example.data.model

enum class StreamStatus {
    IDLE,
    CONNECTING,
    LIVE,
    RECONNECTING,
    STOPPING,
    ERROR
}

data class EncoderMetrics(
    val bitrateKbps: Int = 5850,
    val fps: Float = 60.0f,
    val resolution: String = "1080p60",
    val cpuUsagePercent: Float = 24.5f,
    val memoryUsageMb: Float = 412.0f,
    val networkOutMbps: Float = 6.2f,
    val droppedFrames: Int = 0,
    val audioBitrateKbps: Int = 160,
    val keyframeIntervalSec: Float = 2.0f,
    val rttPingMs: Int = 34,
    val videoCodec: String = "H.264 (NVENC/CBR)",
    val audioCodec: String = "AAC-LC (Stereo)"
)

data class LiveControlState(
    val status: StreamStatus = StreamStatus.IDLE,
    val streamId: String? = null,
    val streamTitle: String = "",
    val videoSourceUrl: String = "",
    val channelTitle: String = "",
    val channelAvatarUrl: String = "",
    val youtubeBroadcastUrl: String = "",
    val elapsedSeconds: Long = 0,
    val scheduledDurationMinutes: Int = 120,
    val isLooping: Boolean = true,
    val isMuted: Boolean = false,
    val metrics: EncoderMetrics = EncoderMetrics(),
    val reconnectAttempts: Int = 0,
    val errorMessage: String? = null,
    val connectionQuality: String = "EXCELLENT" // "EXCELLENT", "GOOD", "UNSTABLE", "POOR"
)

data class AdminSystemStats(
    val totalUsers: Int = 3,
    val activeLiveStreams: Int = 0,
    val completedStreams: Int = 3,
    val failedStreams: Int = 0,
    val serverCpuPercent: Float = 18.4f,
    val serverRamUsedGb: Float = 6.2f,
    val serverRamTotalGb: Float = 32.0f,
    val networkBandwidthMbps: Float = 12.8f,
    val encoderWorkerHealth: String = "HEALTHY (4/4 Workers Online)",
    val ffmpegVersion: String = "FFmpeg 7.0-static / RTMPS TLS 1.3"
)
