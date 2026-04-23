package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.FakeDao
import com.example.purrsistence.data.local.dao.FakeUserDao
import com.example.purrsistence.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRepositoryTest {

    @Test
    fun getUserBalance_returnsBalanceFromDao() = runBlocking {
        val fakeDao = FakeUserDao()
        val repository = UserRepository(fakeDao)

        fakeDao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 25,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        val balance = repository.getUser(1).first()?.balance

        assertEquals(25, balance)
    }

    @Test
    fun getUserBalance_reflectsUpdatedBalance() = runBlocking {
        val fakeDao = FakeUserDao()
        val repository = UserRepository(fakeDao)

        fakeDao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        fakeDao.addCurrency(userId = 1, amount = 7)

        val balance = repository.getUser(1).first()?.balance

        assertEquals(17, balance)
    }

    @Test
    fun buyCatFromShop() = runBlocking {
        val fakeDao = FakeUserDao()
        val repository = UserRepository(fakeDao)

        fakeDao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 100,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        repository.buyCat(
            userId = 1,
            catId = "cat_1",
            price = 30
        )

        val updatedUser = fakeDao.getUser(1).first()

        assertEquals(70, updatedUser!!.balance)
        assert("cat_1" in updatedUser.collectedCatsIds)
    }

    @Test
    fun buyCat_CatIsOwned() = runBlocking {
        val fakeDao = FakeDao()
        val fakeUserDao = FakeUserDao()
        val repository = UserRepository(fakeUserDao)

        fakeDao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 100,
                friends = emptyList(),
                collectedCatsIds = listOf("cat_1")
            )
        )

        repository.buyCat(
            userId = 1,
            catId = "cat_1",
            price = 30
        )

        val updatedUser = fakeDao.getUserById(1)

        assertEquals(100, updatedUser!!.balance)
        assertEquals(listOf("cat_1"), updatedUser.collectedCatsIds)
    }

    @Test
    fun buyCat_NoMoney() = runBlocking {
        val fakeDao = FakeDao()
        val fakeUserDao = FakeUserDao()
        val repository = UserRepository(fakeUserDao)

        fakeDao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        repository.buyCat(
            userId = 1,
            catId = "cat_2",
            price = 30
        )

        val updatedUser = fakeDao.getUserById(1)

        assertEquals(10, updatedUser!!.balance)
        assert(updatedUser.collectedCatsIds.isEmpty())
    }
}