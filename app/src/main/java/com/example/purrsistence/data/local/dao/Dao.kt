package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.data.local.relation.GoalWithSessions
import kotlinx.coroutines.flow.Flow

@Dao

interface Dao {

    // User
    // TODO: handle creation of user
    @Insert
    suspend fun insertUser(user: User)

    // Goal
    @Insert
    suspend fun insertGoal(goal: Goal)

    @androidx.room.Transaction
    @Query("SELECT * FROM Goal WHERE userId = :userId")
    fun getGoals(userId: String): Flow<List<GoalWithSessions>>

    // Sessions
    @Insert
    suspend fun insertSession(session: TrackingSession)

    // Observe total time spent on a goal
    @Query(
        """
        SELECT SUM(endTime - startTime) 
        FROM TrackingSession 
        WHERE goalId = :goalId
    """
    )
    fun observeTotalTime(goalId: Long): Flow<Long?>

    @Query("DELETE FROM Goal WHERE goalId = :goalId")
    suspend fun deleteGoal(goalId: Long)

    @Query("SELECT * FROM Goal WHERE goalId = :goalId")
    fun getGoal(goalId: Long): Flow<Goal?>

    @Query(
        """
        UPDATE Goal 
        SET title = :title, targetDuration = :hours 
        WHERE goalId = :goalId
    """
    )
    suspend fun updateGoal(goalId: Long, title: String, hours: Int)
}