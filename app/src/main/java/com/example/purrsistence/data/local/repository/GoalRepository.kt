package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.GoalsDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.GoalWithSessions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(
    private val dao: GoalsDao
) {

    fun getGoals(userId: Int): Flow<List<GoalWithSessions>> {
        return dao.getGoals(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insertGoal(goal: Goal) {
        dao.insertGoal(goal.toEntity())
    }

    suspend fun deleteGoal(goalId: Int) {
        dao.deleteGoal(goalId)
    }

    fun getGoal(goalId: Int?): Flow<Goal?> {
        return if (goalId == null) {
            kotlinx.coroutines.flow.flowOf(null)
        } else {
            dao.getGoal(goalId).map { entity ->
                entity?.toDomain()
            }
        }
    }

    suspend fun updateGoal(goal: Goal) {
        dao.updateGoal(
            goalId = goal.id,
            title = goal.title,
            type = goal.type.name,
            hours = goal.targetDuration.toMinutes().toInt(),
            deepFocus = goal.deepFocus
        )
    }

    fun searchGoals(userId: Int, query: String): Flow<List<GoalWithSessions>> {
        return dao.searchGoalsWithSessions(userId, query).map { list ->
            list.map { it.toDomain() }
        }
    }
}

