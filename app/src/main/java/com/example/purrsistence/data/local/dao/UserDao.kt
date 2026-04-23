package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.purrsistence.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM User WHERE userId = :userId LIMIT 1")
    fun getUser(userId: Int): Flow<User?>

    @Query("""
    UPDATE User 
    SET balance = balance - :price,
        collectedCatsIds = :cats
    WHERE userId = :userId
    """)
    suspend fun buyCat(userId: Int, price: Int, cats: List<String>)
}