package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveStreamDao {
    @Query("SELECT * FROM live_streams WHERE userId = :userId ORDER BY createdAt DESC")
    fun getStreamsForUser(userId: String): Flow<List<LiveStreamEntity>>

    @Query("SELECT * FROM live_streams WHERE userId = :userId AND status = 'LIVE' LIMIT 1")
    fun getActiveStreamForUser(userId: String): Flow<LiveStreamEntity?>

    @Query("SELECT * FROM live_streams WHERE id = :id LIMIT 1")
    suspend fun getStreamById(id: String): LiveStreamEntity?

    @Query("SELECT * FROM live_streams WHERE id = :id LIMIT 1")
    fun getStreamByIdFlow(id: String): Flow<LiveStreamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: LiveStreamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreams(streams: List<LiveStreamEntity>)

    @Update
    suspend fun updateStream(stream: LiveStreamEntity)

    @Query("UPDATE live_streams SET status = :status, actualStartTime = :startTime WHERE id = :id")
    suspend fun updateStreamStatus(id: String, status: String, startTime: Long? = null)

    @Query("UPDATE live_streams SET status = :status, actualEndTime = :endTime, totalDurationSeconds = :duration WHERE id = :id")
    suspend fun finishStream(id: String, status: String, endTime: Long, duration: Long)

    @Query("DELETE FROM live_streams WHERE id = :id AND userId = :userId")
    suspend fun deleteStream(id: String, userId: String)

    // Admin queries across all users
    @Query("SELECT * FROM live_streams ORDER BY createdAt DESC")
    fun getAllStreams(): Flow<List<LiveStreamEntity>>

    @Query("SELECT COUNT(*) FROM live_streams WHERE status = 'LIVE'")
    fun getActiveStreamsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM live_streams WHERE status = 'COMPLETED'")
    fun getCompletedStreamsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM live_streams WHERE status = 'FAILED'")
    fun getFailedStreamsCount(): Flow<Int>
}
