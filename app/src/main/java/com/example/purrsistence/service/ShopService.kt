package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ShopService(
    private val userRepository: UserRepository
) {

    fun getUser(userId: Int): Flow<User?> {
        return userRepository.getUser(userId)
    }

    suspend fun buyCat(userId: Int, catId: String, price: Int) {
        val user = userRepository.getUser(userId).firstOrNull() ?: return

        if (catId in user.collectedCatsIds) return
        if (user.balance < price) return

        val updatedUser = user.copy(
            balance = user.balance - price,
            collectedCatsIds = user.collectedCatsIds + catId
        )

        userRepository.updateUser(updatedUser)
    }
}