package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "live_streams")
data class LiveStreamEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val channelId: String,
    val title: String,
    val description: String,
    val videoSourceUrl: String,
    val thumbnailUrl: String,
    val category: String,
    val privacy: String, // "Public", "Unlisted", "Private"
    val scheduledStartTime: Long,
    val scheduledDurationMinutes: Int,
    val isLooping: Boolean,
    val resolutionPreset: String = "1080p60",
    val targetBitrateKbps: Int = 4500,
    val status: String, // "IDLE", "SCHEDULED", "LIVE", "RECONNECTING", "COMPLETED", "FAILED", "STOPPED"
    val actualStartTime: Long? = null,
    val actualEndTime: Long? = null,
    val youtubeBroadcastUrl: String = "https://youtube.com/live/broadcast",
    val peakViewers: Int = 0,
    val totalDurationSeconds: Long = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
