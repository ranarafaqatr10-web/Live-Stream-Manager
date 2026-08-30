package com.example.data.repository

import com.example.data.local.SystemLogDao
import com.example.data.local.SystemLogEntity
import kotlinx.coroutines.flow.Flow

class AdminRepository(private val systemLogDao: SystemLogDao) {
    val recentLogs: Flow<List<SystemLogEntity>> = systemLogDao.getRecentLogs()

    suspend fun logEvent(level: String, source: String, message: String, userId: String? = null) {
        systemLogDao.insertLog(
            SystemLogEntity(
                level = level,
                source = source,
                message = message,
                userId = userId
            )
        )
    }

    suspend fun clearLogs() = systemLogDao.clearLogs()
}
