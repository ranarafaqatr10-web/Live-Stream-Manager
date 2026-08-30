package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val totalUsersCount: Flow<Int> = userDao.getUserCount()

    suspend fun getUserById(id: String): UserEntity? = userDao.getUserById(id)

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun createUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun setSuspension(id: String, suspended: Boolean) = userDao.setUserSuspension(id, suspended)
}
