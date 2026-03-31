package com.example.purrsistence.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["userId"])]
)

data class Goal(
    @PrimaryKey(autoGenerate = true) val goalId: Int = 0,

    val userId: Int,
    val title: String,
    val type: String,
    val targetDuration: Int, // in minutes
    val deepFocus: Boolean,
    val inactive: Boolean,
    val createdAt: Long,
    val isCompleted: Boolean
)
