package com.example.purrsistence.data.local.dao

import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.data.local.relation.GoalWithSessions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTrackingDao : Dao {
    private val users = mutableListOf<User>()
    private val sessions = mutableListOf<TrackingSession>()
    private var nextTrackingId = 1

    override suspend fun insertUser(user: User) {
        users.add(user)
    }
    override suspend fun addCurrency(userId: Int, amount: Int) {
        val index = users.indexOfFirst { it.userId == userId }
        if (index == -1) return

        val old = users[index]
        users[index] = old.copy(balance = old.balance + amount)
    }

    override fun getUserBalance(userId: Int): Flow<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserById(userId: Int): User? {
        return users.find { it.userId == userId }
    }

    override suspend fun insertGoal(goal: Goal) {
        throw UnsupportedOperationException("Not needed for tracking repository test")
    }

    override fun getGoals(userId: Int): Flow<List<GoalWithSessions>> {
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

    override fun getGoal(goalId: Int): Flow<Goal?> {
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

    override suspend fun insertTrackingSession(session: TrackingSession): Long {
        val stored = session.copy(trackingId = nextTrackingId++)
        sessions.add(stored)
        return stored.trackingId.toLong()
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return sessions.lastOrNull { it.goalId == goalId && it.endTime == null }
    }

    override suspend fun stopTrackingSession(trackingId: Int, endTime: Long) {
        val index = sessions.indexOfFirst { it.trackingId == trackingId }
        if (index == -1) return

        val old = sessions[index]
        sessions[index] = old.copy(endTime = endTime)
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return sessions.find { it.trackingId == trackingId }
    }

    //added for statistics
    override fun getGoalsRaw(userId: Int): Flow<List<Goal>> {
        return flowOf(emptyList())
    }

    //added for statistics
    override fun getCompletedSessionsForUser(userId: Int): Flow<List<TrackingSession>> {
        return flowOf(
            sessions.filter { it.endTime != null }
        )
    }
}