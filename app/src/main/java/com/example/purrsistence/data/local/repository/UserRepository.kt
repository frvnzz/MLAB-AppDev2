package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val userDao: UserDao
) {

    fun getUser(userId: Int): Flow<User?> {
        return userDao.getUser(userId).map { entity ->
            entity?.toDomain()
        }
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    suspend fun addCurrency(userId: Int, amount: Int) {
        userDao.addCurrency(userId, amount)
    }
}