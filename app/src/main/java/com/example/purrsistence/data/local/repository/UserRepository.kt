package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull


class UserRepository(
    private val userDao: UserDao
) {
    fun getUser(userId: Int): Flow<User?> {
        return userDao.getUser(userId)
    }

    suspend fun buyCat(userId: Int, catId: String, price: Int) {
        val user = userDao.getUser(userId).firstOrNull() ?: return

        // cat already owned -> do nothing
        if (catId in user.collectedCatsIds) return
        // not enough money to buy the cat
        if (user.balance < price) return

        val updatedCats = user.collectedCatsIds + catId

        userDao.buyCat(userId, price, updatedCats)
    }
}