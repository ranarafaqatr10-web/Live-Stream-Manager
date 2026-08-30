package com.example.data.repository

import com.example.data.local.YouTubeChannelDao
import com.example.data.local.YouTubeChannelEntity
import kotlinx.coroutines.flow.Flow

class YouTubeChannelRepository(private val channelDao: YouTubeChannelDao) {
    fun getChannelForUser(userId: String): Flow<YouTubeChannelEntity?> =
        channelDao.getChannelForUser(userId)

    suspend fun getChannelForUserSync(userId: String): YouTubeChannelEntity? =
        channelDao.getChannelForUserSync(userId)

    suspend fun connectChannel(channel: YouTubeChannelEntity) =
        channelDao.insertChannel(channel)

    suspend fun disconnectChannel(userId: String) =
        channelDao.updateConnectionStatus(userId, false)

    suspend fun reconnectChannel(userId: String) =
        channelDao.updateConnectionStatus(userId, true)

    suspend fun removeChannel(userId: String) =
        channelDao.deleteChannelForUser(userId)
}
