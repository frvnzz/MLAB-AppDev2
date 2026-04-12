package com.example.purrsistence.data.local.dao

import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.data.local.relation.GoalWithSessions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDao : Dao {

    private val users = mutableListOf<User>()
    private val goals = mutableListOf<Goal>()
    private val trackingSessions = mutableListOf<TrackingSession>()

    private val goalsFlow = MutableStateFlow<List<Goal>>(emptyList())
    private var nextGoalId = 1
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

    override suspend fun getUserById(userId: Int): User? {
        return users.find { it.userId == userId }
    }

    override suspend fun insertGoal(goal: Goal) {
        val userExists = users.any { it.userId == goal.userId }
        if (!userExists) {
            throw IllegalArgumentException("User with id ${goal.userId} does not exist")
        }

        val storedGoal = goal.copy(goalId = nextGoalId++)
        goals.add(storedGoal)
        goalsFlow.value = goals.toList()
    }

    override fun getGoals(userId: Int): Flow<List<GoalWithSessions>> {
        return goalsFlow.map { goalList ->
            goalList
                .filter { it.userId == userId }
                .map { goal ->
                    GoalWithSessions(
                        goal = goal,
                        sessions = trackingSessions.filter { it.goalId == goal.goalId }
                    )
                }
        }
    }

    override fun observeTotalTime(goalId: Int): Flow<Long?> {
        return MutableStateFlow(
            trackingSessions
                .filter { it.goalId == goalId && it.endTime != null }
                .sumOf { (it.endTime ?: 0L) - it.startTime }
                .takeIf { it > 0L }
        )
    }

    override suspend fun deleteGoal(goalId: Int) {
        goals.removeAll { it.goalId == goalId }
        trackingSessions.removeAll { it.goalId == goalId }
        goalsFlow.value = goals.toList()
    }

    override fun getGoal(goalId: Int): Flow<Goal?> {
        return goalsFlow.map { goalList ->
            goalList.find { it.goalId == goalId }
        }
    }

    override suspend fun updateGoal(
        goalId: Int,
        title: String,
        type: String,
        hours: Int,
        deepFocus: Boolean
    ) {
        val index = goals.indexOfFirst { it.goalId == goalId }
        if (index == -1) return

        val old = goals[index]
        goals[index] = old.copy(
            title = title,
            type = type,
            targetDuration = hours,
            deepFocus = deepFocus
        )
        goalsFlow.value = goals.toList()
    }

    override suspend fun insertTrackingSession(session: TrackingSession): Long {
        val stored = session.copy(trackingId = nextTrackingId++)
        trackingSessions.add(stored)
        goalsFlow.value = goals.toList()
        return stored.trackingId.toLong()
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return trackingSessions
            .lastOrNull { it.goalId == goalId && it.endTime == null }
    }

    override suspend fun stopTrackingSession(trackingId: Int, endTime: Long) {
        val index = trackingSessions.indexOfFirst { it.trackingId == trackingId }
        if (index == -1) return

        val old = trackingSessions[index]
        trackingSessions[index] = old.copy(endTime = endTime)
        goalsFlow.value = goals.toList()
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return trackingSessions.find { it.trackingId == trackingId }
    }
}