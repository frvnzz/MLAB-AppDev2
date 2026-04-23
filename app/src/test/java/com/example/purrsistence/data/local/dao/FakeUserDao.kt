package com.example.purrsistence.data.local.dao

import com.example.purrsistence.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserDao : UserDao {

    private val users = mutableListOf<User>()
    private val userFlow = mutableMapOf<Int, MutableStateFlow<User?>>()
    private val balanceFlows = mutableMapOf<Int, MutableStateFlow<Int>>()

    fun insertUser(user: User) {
        users.add(user)
        balanceFlows[user.userId] = MutableStateFlow(user.balance)
    }

    override fun getUser(userId: Int): Flow<User?> {
        return userFlow.getOrPut(userId) {
            MutableStateFlow(users.find { it.userId == userId })
        }
    }

    fun addCurrency(userId: Int, amount: Int) {
        val index = users.indexOfFirst { it.userId == userId }
        if (index == -1) return

        val old = users[index]
        val updated = old.copy(balance = old.balance + amount)
        users[index] = updated

        balanceFlows.getOrPut(userId) { MutableStateFlow(0) }.value = updated.balance
    }

    override suspend fun buyCat(
        userId: Int,
        price: Int,
        cats: List<String>
    ) {
        val index = users.indexOfFirst { it.userId == userId }
        if (index == -1) return

        val old = users[index]
        val updated = old.copy(
            balance = old.balance - price,
            collectedCatsIds = cats
        )

        users[index] = updated
        userFlow.getOrPut(userId) { MutableStateFlow(updated) }.value = updated

    }

}