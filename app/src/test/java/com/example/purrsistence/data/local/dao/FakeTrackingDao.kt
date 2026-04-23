package com.example.purrsistence.data.local.dao

import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.data.local.relation.GoalWithSessionsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTrackingDao : Dao {
    private val userEntities = mutableListOf<UserEntity>()
    private val sessions = mutableListOf<TrackingSessionEntity>()
    private var nextTrackingId = 1

    override suspend fun insertUser(userEntity: UserEntity) {
        userEntities.add(userEntity)
    }
    override suspend fun addCurrency(userId: Int, amount: Int) {
        val index = userEntities.indexOfFirst { it.userId == userId }
        if (index == -1) return

        val old = userEntities[index]
        userEntities[index] = old.copy(balance = old.balance + amount)
    }

    override suspend fun getUserById(userId: Int): UserEntity? {
        return userEntities.find { it.userId == userId }
    }

    override suspend fun insertGoal(goalEntity: GoalEntity) {
        throw UnsupportedOperationException("Not needed for tracking repository test")
    }

    override fun getGoals(userId: Int): Flow<List<GoalWithSessionsEntity>> {
        return flowOf(emptyList())
    }

    override fun observeTotalTime(goalId: Int): Flow<Long?> {
        val total = sessions
            .filter { it.goalId == goalId && it.endTime != null }
            .sumOf { (it.endTime ?: 0L) - it.startTime }

        return flowOf(total.takeIf { it > 0L })
    }

    override suspend fun deleteGoal(goalId: Int) {
        throw UnsupportedOperationException("Not needed for tracking repository test")
    }

    override fun getGoal(goalId: Int): Flow<GoalEntity?> {
        return flowOf(null)
    }

    override suspend fun updateGoal(
        goalId: Int,
        title: String,
        type: String,
        hours: Int,
        deepFocus: Boolean
    ) {
        throw UnsupportedOperationException("Not needed for tracking repository test")
    }

    override fun searchGoalsWithSessions(userId: Int, query: String): Flow<List<GoalWithSessionsEntity>> {
        return flowOf(emptyList())
    }

    override suspend fun insertTrackingSession(session: TrackingSessionEntity): Long {
        val stored = session.copy(trackingId = nextTrackingId++)
        sessions.add(stored)
        return stored.trackingId.toLong()
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSessionEntity? {
        return sessions.lastOrNull { it.goalId == goalId && it.endTime == null }
    }

    override suspend fun stopTrackingSession(trackingId: Int, endTime: Long) {
        val index = sessions.indexOfFirst { it.trackingId == trackingId }
        if (index == -1) return

        val old = sessions[index]
        sessions[index] = old.copy(endTime = endTime)
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSessionEntity? {
        return sessions.find { it.trackingId == trackingId }
    }

    //added for statistics
    override fun getGoalsRaw(userId: Int): Flow<List<GoalEntity>> {
        return flowOf(emptyList())
    }

    //added for statistics
    override fun getCompletedSessionsForUser(userId: Int): Flow<List<TrackingSessionEntity>> {
        return flowOf(
            sessions.filter { it.endTime != null }
        )
    }
}