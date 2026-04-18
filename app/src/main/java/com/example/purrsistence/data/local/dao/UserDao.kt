package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.purrsistence.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // observeUser gets all the data of a User with the given id
    @Query("SELECT * FROM User WHERE userId = :userId LIMIT 1")
    fun observeUser(userId: Int): Flow<User>

    @Query("SELECT balance FROM User WHERE userId = :userId")
    fun getUserBalance(userId: Int): Flow<Int>

    @Query("SELECT * FROM User WHERE userId = :userId LIMIT 1")
    suspend fun getUser(userId: Int): User

    @Query("SELECT * FROM User WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    // TODO: merge these two functions so that there is only one call to the database
    @Query("UPDATE User SET balance = balance - :amount WHERE userId = :userId")
    suspend fun spendCurrency(userId: Int, amount: Int)

    @Query("UPDATE User SET collectedCatsIds = :cats WHERE userId = :userId")
    suspend fun updateCollectedCats(userId: Int, cats: List<String>)
}