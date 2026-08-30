package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        YouTubeChannelEntity::class,
        LiveStreamEntity::class,
        SystemLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun youtubeChannelDao(): YouTubeChannelDao
    abstract fun liveStreamDao(): LiveStreamDao
    abstract fun systemLogDao(): SystemLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yt_live_manager_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()

            // 1. Initial Users
            val user1 = UserEntity(
                id = "usr_101",
                email = "creator@studio.com",
                displayName = "Alex Morgan",
                role = "USER",
                isSuspended = false,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                createdAt = now - 86400000L * 30
            )
            val user2 = UserEntity(
                id = "usr_102",
                email = "gaming@zone.tv",
                displayName = "Vortex Gaming",
                role = "USER",
                isSuspended = false,
                avatarUrl = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
                createdAt = now - 86400000L * 15
            )
            val adminUser = UserEntity(
                id = "usr_admin",
                email = "admin@ytlivemanager.com",
                displayName = "System Administrator",
                role = "ADMIN",
                isSuspended = false,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                createdAt = now - 86400000L * 60
            )
            db.userDao().insertUsers(listOf(user1, user2, adminUser))

            // 2. Initial Connected YouTube Channels
            val channel1 = YouTubeChannelEntity(
                channelId = "UC_alex_morgan_live",
                userId = "usr_101",
                channelTitle = "Alex Morgan Official",
                channelHandle = "@alexmorgan_live",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                subscriberCount = "142.5K",
                isConnected = true,
                streamKeyMasked = "••••-••••-••••-7319",
                ingestUrl = "rtmps://a.rtmps.youtube.com/live2",
                connectedAt = now - 86400000L * 25
            )
            val channel2 = YouTubeChannelEntity(
                channelId = "UC_vortex_gaming",
                userId = "usr_102",
                channelTitle = "Vortex Gaming Network",
                channelHandle = "@vortex_gaming_hq",
                avatarUrl = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
                subscriberCount = "89.2K",
                isConnected = true,
                streamKeyMasked = "••••-••••-••••-4102",
                ingestUrl = "rtmps://a.rtmps.youtube.com/live2",
                connectedAt = now - 86400000L * 10
            )
            db.youtubeChannelDao().insertChannels(listOf(channel1, channel2))

            // 3. Initial Stream History for User 1
            val stream1 = LiveStreamEntity(
                id = "stream_101",
                userId = "usr_101",
                channelId = "UC_alex_morgan_live",
                title = "24/7 Deep Focus Music & Ambient Visuals",
                description = "Continuous live stream broadcast powered by YT Live Manager high-bitrate RTMPS engine.",
                videoSourceUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
                category = "Music",
                privacy = "Public",
                scheduledStartTime = now - 3600000L * 4,
                scheduledDurationMinutes = 240,
                isLooping = true,
                resolutionPreset = "1080p60",
                targetBitrateKbps = 6000,
                status = "COMPLETED",
                actualStartTime = now - 3600000L * 4,
                actualEndTime = now - 3600000L * 1,
                youtubeBroadcastUrl = "https://youtube.com/live/df93ks0a",
                peakViewers = 1840,
                totalDurationSeconds = 10800,
                createdAt = now - 3600000L * 5
            )

            val stream2 = LiveStreamEntity(
                id = "stream_102",
                userId = "usr_101",
                channelId = "UC_alex_morgan_live",
                title = "Nature Horizons 4K: Alpine Drone Flight",
                description = "Relaxing aerial journey over high alpine lakes and mountain ranges.",
                videoSourceUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600",
                category = "Travel & Events",
                privacy = "Public",
                scheduledStartTime = now - 86400000L * 2,
                scheduledDurationMinutes = 120,
                isLooping = true,
                resolutionPreset = "1080p60",
                targetBitrateKbps = 5500,
                status = "COMPLETED",
                actualStartTime = now - 86400000L * 2,
                actualEndTime = now - 86400000L * 2 + 7200000L,
                youtubeBroadcastUrl = "https://youtube.com/live/nx84hd2q",
                peakViewers = 920,
                totalDurationSeconds = 7200,
                createdAt = now - 86400000L * 2
            )

            // Stream for User 2 (Separate isolation check)
            val stream3 = LiveStreamEntity(
                id = "stream_201",
                userId = "usr_102",
                channelId = "UC_vortex_gaming",
                title = "Cyberpunk Night City Tour [60 FPS Ultra]",
                description = "Live game stream broadcast showcase.",
                videoSourceUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600",
                category = "Gaming",
                privacy = "Public",
                scheduledStartTime = now - 86400000L,
                scheduledDurationMinutes = 180,
                isLooping = true,
                resolutionPreset = "1080p60",
                targetBitrateKbps = 6000,
                status = "COMPLETED",
                actualStartTime = now - 86400000L,
                actualEndTime = now - 86400000L + 10800000L,
                youtubeBroadcastUrl = "https://youtube.com/live/cz84ma1p",
                peakViewers = 3450,
                totalDurationSeconds = 10800,
                createdAt = now - 86400000L
            )

            db.liveStreamDao().insertStreams(listOf(stream1, stream2, stream3))

            // 4. Initial System Logs for Admin
            val log1 = SystemLogEntity(
                timestamp = now - 1800000L,
                level = "INFO",
                source = "RTMPS",
                message = "RTMPS TLS 1.3 handshake succeeded with youtube-rtmps-edge-04.googlevideo.com",
                userId = "usr_101"
            )
            val log2 = SystemLogEntity(
                timestamp = now - 1200000L,
                level = "INFO",
                source = "FFmpeg",
                message = "Encoder worker pool #2 initialized (h264_nvenc, CBR 6000k, keyint=120, bframes=0)",
                userId = "usr_101"
            )
            val log3 = SystemLogEntity(
                timestamp = now - 600000L,
                level = "INFO",
                source = "YouTubeAPI",
                message = "OAuth token refreshed automatically for channel UC_alex_morgan_live via YouTube Live Streaming API v3",
                userId = "usr_101"
            )
            db.systemLogDao().insertLog(log1)
            db.systemLogDao().insertLog(log2)
            db.systemLogDao().insertLog(log3)
        }
    }
}
