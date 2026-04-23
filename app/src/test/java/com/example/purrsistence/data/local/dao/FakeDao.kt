package com.example.purrsistence.data.local.dao

import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.data.local.relation.GoalWithSessionsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// TODO: Split Dao !!

class FakeDao : Dao {

    private val userEntities = mutableListOf<UserEntity>()
    private val goalEntities = mutableListOf<GoalEntity>()
    private val trackingSessionEntities = mutableListOf<TrackingSessionEntity>()

    private val goalsFlow = MutableStateFlow<List<GoalEntity>>(emptyList())
    private var nextGoalId = 1
    private var nextTrackingId = 1

    private val balanceFlows = mutableMapOf<Int, MutableStateFlow<Int>>()

    //User
    override suspend fun insertUser(userEntity: UserEntity) {
        userEntities.add(userEntity)
        balanceFlows[userEntity.userId] = MutableStateFlow(userEntity.balance)
    }

    override suspend fun addCurrency(userId: Int, amount: Int) {
        val index = userEntities.indexOfFirst { it.userId == userId }
        if (index == -1) return

        val old = userEntities[index]
        val updated = old.copy(balance = old.balance + amount)
        userEntities[index] = updated

        balanceFlows.getOrPut(userId) { MutableStateFlow(0) }.value = updated.balance
    }

    override suspend fun getUserById(userId: Int): UserEntity? {
        return userEntities.find { it.userId == userId }
    }

    //Goals
    override suspend fun insertGoal(goalEntity: GoalEntity) {
        val userExists = userEntities.any { it.userId == goalEntity.userId }
        if (!userExists) {
            throw IllegalArgumentException("User with id ${goalEntity.userId} does not exist")
        }

        val storedGoal = goalEntity.copy(goalId = nextGoalId++)
        goalEntities.add(storedGoal)
        goalsFlow.value = goalEntities.toList()
    }

    override fun getGoals(userId: Int): Flow<List<GoalWithSessionsEntity>> {
        return goalsFlow.map { goalList ->
            goalList
                .filter { it.userId == userId }
                .map { goal ->
                    GoalWithSessionsEntity(
                        goalEntity = goal,
                        sessions = trackingSessionEntities.filter { it.goalId == goal.goalId }
                    )
                }
        }
    }

    override suspend fun deleteGoal(goalId: Int) {
        goalEntities.removeAll { it.goalId == goalId }
        trackingSessionEntities.removeAll { it.goalId == goalId }
        goalsFlow.value = goalEntities.toList()
    }

    override fun getGoal(goalId: Int): Flow<GoalEntity?> {
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
        val index = goalEntities.indexOfFirst { it.goalId == goalId }
        if (index == -1) return

        val old = goalEntities[index]
        goalEntities[index] = old.copy(
            title = title,
            type = type,
            targetDuration = hours,
            deepFocus = deepFocus
        )
        goalsFlow.value = goalEntities.toList()
    }

    override fun searchGoalsWithSessions(userId: Int, query: String): Flow<List<GoalWithSessionsEntity>> {
        return goalsFlow.map { goalList ->
            goalList
                .filter { goal ->
                    goal.userId == userId &&
                            goal.title.contains(query, ignoreCase = true)
                }
                .map { goal ->
                    GoalWithSessionsEntity(
                        goalEntity = goal,
                        sessions = trackingSessionEntities.filter { it.goalId == goal.goalId }
                    )
                }
        }
    }

    //Tracking Session
    override suspend fun insertTrackingSession(session: TrackingSessionEntity): Long {
        val stored = session.copy(trackingId = nextTrackingId++)
        trackingSessionEntities.add(stored)
        goalsFlow.value = goalEntities.toList()
        return stored.trackingId.toLong()
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSessionEntity? {
        return trackingSessionEntities
            .lastOrNull { it.goalId == goalId && it.endTime == null }
    }

    override suspend fun stopTrackingSession(trackingId: Int, endTime: Long) {
        val index = trackingSessionEntities.indexOfFirst { it.trackingId == trackingId }
        if (index == -1) return

        val old = trackingSessionEntities[index]
        trackingSessionEntities[index] = old.copy(endTime = endTime)
        goalsFlow.value = goalEntities.toList()
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSessionEntity? {
        return trackingSessionEntities.find { it.trackingId == trackingId }
    }

    override fun observeTotalTime(goalId: Int): Flow<Long?> {
        val total = trackingSessionEntities
            .filter { it.goalId == goalId && it.endTime != null }
            .sumOf { (it.endTime ?: 0L) - it.startTime }

        return flowOf(total.takeIf { it > 0L })
    }

    //added for statistics
    override fun getGoalsRaw(userId: Int): Flow<List<GoalEntity>> {
        return goalsFlow.map { goalList ->
            goalList.filter { it.userId == userId }
        }
    }

    //added for statistics
    override fun getCompletedSessionsForUser(userId: Int): Flow<List<TrackingSessionEntity>> {
        return goalsFlow.map {
            val userGoalIds = goalEntities
                .filter { it.userId == userId }
                .map { it.goalId }

            trackingSessionEntities.filter { session ->
                session.goalId in userGoalIds && session.endTime != null
            }
        }
    }
}