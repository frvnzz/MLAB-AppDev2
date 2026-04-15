package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.FakeDao
import com.example.purrsistence.data.local.entity.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRepositoryTest {

    @Test
    fun getUserBalance_returnsBalanceFromDao() = runBlocking {
        val fakeDao = FakeDao()
        val repository = UserRepository(fakeDao)

        fakeDao.insertUser(
            User(
                userId = 1,
                username = "TestUser",
                balance = 25,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        val balance = repository.getUserBalance(1).first()

        assertEquals(25, balance)
    }

    @Test
    fun getUserBalance_reflectsUpdatedBalance() = runBlocking {
        val fakeDao = FakeDao()
        val repository = UserRepository(fakeDao)

        fakeDao.insertUser(
            User(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        fakeDao.addCurrency(userId = 1, amount = 7)

        val balance = repository.getUserBalance(1).first()

        assertEquals(17, balance)
    }
}