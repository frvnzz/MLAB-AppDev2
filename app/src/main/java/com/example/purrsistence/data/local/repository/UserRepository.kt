package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.time.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

interface UserRepository {
    fun getUser(userId: Int): Flow<User?>
    suspend fun insertUser(user: User)
    suspend fun updateUserFromLocalAction(user: User)
    suspend fun addCurrency(userId: Int, amount: Int)
    suspend fun updateUserFromRemoteSync(user: User)
    suspend fun markUserSynced(userId: Int)
}

class UserRepositoryImpl (
    private val userDao: UserDao,
    private val timeProvider: TimeProvider
) : UserRepository {

    override fun getUser(userId: Int): Flow<User?> {
        return userDao.getUser(userId).map { entity ->
            entity?.toDomain()
        }
    }


    override suspend fun insertUser(user: User) {
        val nowMillis = timeProvider.now().toEpochMilli()
        val localUpdatedAt = user.toEntity().localUpdatedAt
        userDao.insertUser(
            user.toEntity().copy(
                localUpdatedAt = localUpdatedAt.takeIf { it!! > 0 } ?: nowMillis,
                lastSyncedAt = user.lastSyncedAt as Long?,
                hasPendingLocalChanges = user.hasPendingLocalChanges
            )
        )
    }

    override suspend fun updateUserFromLocalAction(user: User) {
        val nowMillis = timeProvider.now().toEpochMilli()

        userDao.updateUser(
            user.toEntity().copy(
                localUpdatedAt = nowMillis,
                hasPendingLocalChanges = true
            )
        )
    }

    override suspend fun updateUserFromRemoteSync(user: User) {
        val nowMillis = timeProvider.now().toEpochMilli()

        userDao.updateUser(
            user.toEntity().copy(
                lastSyncedAt = nowMillis,
                hasPendingLocalChanges = false
            )
        )
    }

    override suspend fun markUserSynced(userId: Int) {
        val user = getUser(userId).firstOrNull() ?: return
        val nowMillis = timeProvider.now().toEpochMilli()

        userDao.updateUser(
            user.toEntity().copy(
                lastSyncedAt = nowMillis,
                hasPendingLocalChanges = false
            )
        )
    }

    override suspend fun addCurrency(userId: Int, amount: Int) {
        val user = getUser(userId).firstOrNull() ?: return

        updateUserFromLocalAction(
            user.copy(
                balance = user.balance + amount
            )
        )
    }
}