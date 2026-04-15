package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.Dao
import kotlinx.coroutines.flow.Flow


class UserRepository(
    private val dao: Dao
) {
    // Currency
    fun getUserBalance(userId: Int): Flow<Int> {
        return dao.getUserBalance(userId)
    }
}