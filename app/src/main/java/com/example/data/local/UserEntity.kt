package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val role: String = "USER", // "USER" or "ADMIN"
    val isSuspended: Boolean = false,
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
