package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.local.entity.User
import kotlinx.coroutines.flow.Flow


class UserRepository(
    private val userDao: UserDao
) {
    fun observeUser(userId: Int): Flow<User> {
        return userDao.observeUser(userId)
    }

    fun getUserBalance(userId: Int): Flow<Int> {
        return userDao.getUserBalance(userId)
    }

    suspend fun buyCat(userId: Int, catId: String, price: Int) {
        val user = userDao.getUser(userId)

        // cat already owned -> do nothing
        if (catId in user.collectedCatsIds) return
        // not enough money to buy the cat
        if (user.balance < price) return

        val updatedCats = user.collectedCatsIds + catId

        userDao.spendCurrency(userId, price)
        userDao.updateCollectedCats(userId, updatedCats)
    }
}