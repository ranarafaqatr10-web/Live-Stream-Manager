package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "youtube_channels")
data class YouTubeChannelEntity(
    @PrimaryKey val channelId: String,
    val userId: String,
    val channelTitle: String,
    val channelHandle: String,
    val avatarUrl: String,
    val subscriberCount: String,
    val isConnected: Boolean,
    val streamKeyMasked: String = "••••-••••-••••-9821",
    val ingestUrl: String = "rtmps://a.rtmps.youtube.com/live2",
    val connectedAt: Long = System.currentTimeMillis(),
    val lastTokenRefresh: Long = System.currentTimeMillis()
)
