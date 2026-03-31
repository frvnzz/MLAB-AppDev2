package com.example.purrsistence.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,

    val username: String,
    val balance: Int,
    val friends: List<String>,
    val collectedCatsIds: List<String>
)
