package com.example.data.repository

import com.example.data.local.LiveStreamDao
import com.example.data.local.LiveStreamEntity
import kotlinx.coroutines.flow.Flow

class LiveStreamRepository(private val liveStreamDao: LiveStreamDao) {
    fun getStreamsForUser(userId: String): Flow<List<LiveStreamEntity>> =
        liveStreamDao.getStreamsForUser(userId)

    fun getActiveStreamForUser(userId: String): Flow<LiveStreamEntity?> =
        liveStreamDao.getActiveStreamForUser(userId)

    suspend fun getStreamById(id: String): LiveStreamEntity? =
        liveStreamDao.getStreamById(id)

    fun getStreamByIdFlow(id: String): Flow<LiveStreamEntity?> =
        liveStreamDao.getStreamByIdFlow(id)

    suspend fun createStream(stream: LiveStreamEntity) =
        liveStreamDao.insertStream(stream)

    suspend fun updateStream(stream: LiveStreamEntity) =
        liveStreamDao.updateStream(stream)

    suspend fun setStatus(id: String, status: String, startTime: Long? = null) =
        liveStreamDao.updateStreamStatus(id, status, startTime)

    suspend fun finishStream(id: String, status: String, endTime: Long, duration: Long) =
        liveStreamDao.finishStream(id, status, endTime, duration)

    suspend fun deleteStream(id: String, userId: String) =
        liveStreamDao.deleteStream(id, userId)

    // Admin
    val allStreams: Flow<List<LiveStreamEntity>> = liveStreamDao.getAllStreams()
    val activeStreamsCount: Flow<Int> = liveStreamDao.getActiveStreamsCount()
    val completedStreamsCount: Flow<Int> = liveStreamDao.getCompletedStreamsCount()
    val failedStreamsCount: Flow<Int> = liveStreamDao.getFailedStreamsCount()
}
