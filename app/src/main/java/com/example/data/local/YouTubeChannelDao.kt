package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface YouTubeChannelDao {
    @Query("SELECT * FROM youtube_channels WHERE userId = :userId LIMIT 1")
    fun getChannelForUser(userId: String): Flow<YouTubeChannelEntity?>

    @Query("SELECT * FROM youtube_channels WHERE userId = :userId LIMIT 1")
    suspend fun getChannelForUserSync(userId: String): YouTubeChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: YouTubeChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<YouTubeChannelEntity>)

    @Update
    suspend fun updateChannel(channel: YouTubeChannelEntity)

    @Query("UPDATE youtube_channels SET isConnected = :connected WHERE userId = :userId")
    suspend fun updateConnectionStatus(userId: String, connected: Boolean)

    @Query("DELETE FROM youtube_channels WHERE userId = :userId")
    suspend fun deleteChannelForUser(userId: String)
}
