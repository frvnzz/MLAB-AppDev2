package com.example.purrsistence.data.local.dao

import com.example.purrsistence.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserDao : UserDao {

    private val userEntities = mutableListOf<UserEntity>()
    private val userEntityFlow = mutableMapOf<Int, MutableStateFlow<UserEntity?>>()
    private val balanceFlows = mutableMapOf<Int, MutableStateFlow<Int>>()

    fun insertUser(userEntity: UserEntity) {
        userEntities.add(userEntity)
        balanceFlows[userEntity.userId] = MutableStateFlow(userEntity.balance)
    }

    override fun getUser(userId: Int): Flow<UserEntity?> {
        return userEntityFlow.getOrPut(userId) {
            MutableStateFlow(userEntities.find { it.userId == userId })
        }
    }

    fun addCurrency(userId: Int, amount: Int) {
        val index = userEntities.indexOfFirst { it.userId == userId }
        if (index == -1) return

        val old = userEntities[index]
        val updated = old.copy(balance = old.balance + amount)
        userEntities[index] = updated

        balanceFlows.getOrPut(userId) { MutableStateFlow(0) }.value = updated.balance
    }

    override suspend fun buyCat(
        userId: Int,
        price: Int,
        cats: List<String>
    ) {
        val index = userEntities.indexOfFirst { it.userId == userId }
        if (index == -1) return

        val old = userEntities[index]
        val updated = old.copy(
            balance = old.balance - price,
            collectedCatsIds = cats
        )

        userEntities[index] = updated
        userEntityFlow.getOrPut(userId) { MutableStateFlow(updated) }.value = updated

    }

}