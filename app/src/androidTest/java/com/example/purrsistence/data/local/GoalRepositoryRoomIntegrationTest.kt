package com.example.purrsistence.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.types.GoalType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class GoalRepositoryRoomIntegrationTest : RoomIntegrationTestBase() {

    @Test
    fun insertGoal_andGetGoal_returnsMappedGoal() = runBlocking {
        seedUserEntity(userId = 1)

        goalRepository.insertGoal(
            Goal(
                id = 0,
                userId = 1,
                title = "Read Papers",
                type = GoalType.WEEKLY,
                targetDuration = Duration.ofMinutes(120),
                deepFocus = true,
                inactive = false,
                createdAt = Instant.ofEpochMilli(1_700_000_000_000L),
                isCompleted = false,
                lastCompletedAt = null
            )
        )

        val stored = goalRepository.getGoal(1).first()

        assertNotNull(stored)
        assertEquals(1, stored!!.id)
        assertEquals(1, stored.userId)
        assertEquals("Read Papers", stored.title)
        assertEquals(GoalType.WEEKLY, stored.type)
        assertEquals(Duration.ofMinutes(120), stored.targetDuration)
        assertTrue(stored.deepFocus)
        assertFalse(stored.inactive)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), stored.createdAt)
        assertFalse(stored.isCompleted)
    }

    @Test
    fun getActiveGoals_returnsOnlyActiveGoals() = runBlocking {
        seedUserEntity(userId = 1)

        goalRepository.insertGoal(
            Goal(
                id = 0,
                userId = 1,
                title = "Active Goal",
                type = GoalType.DAILY,
                targetDuration = Duration.ofMinutes(30),
                deepFocus = false,
                inactive = false,
                createdAt = Instant.ofEpochMilli(1_700_000_000_000L),
                isCompleted = false,
                lastCompletedAt = null
            )
        )

        goalRepository.insertGoal(
            Goal(
                id = 0,
                userId = 1,
                title = "Inactive Goal",
                type = GoalType.MONTHLY,
                targetDuration = Duration.ofMinutes(90),
                deepFocus = false,
                inactive = true,
                createdAt = Instant.ofEpochMilli(1_700_000_000_001L),
                isCompleted = false,
                lastCompletedAt = null
            )
        )

        val result = goalRepository.getActiveGoals(1).first()

        assertEquals(1, result.size)
        assertEquals("Active Goal", result.first().goal.title)
        assertFalse(result.first().goal.inactive)
    }

    @Test
    fun updateGoal_persistsInactiveAndCompletedFlags() = runBlocking {
        seedUserEntity(userId = 1)
        seedGoalEntity(
            goalId = 1,
            userId = 1,
            title = "Original Goal",
            type = "WEEKLY",
            targetDuration = 120,
            deepFocus = false,
            inactive = false,
            createdAt = 1_700_000_000_000L,
            isCompleted = false
        )

        goalRepository.updateGoal(
            Goal(
                id = 1,
                userId = 1,
                title = "Updated Goal",
                type = GoalType.MONTHLY,
                targetDuration = Duration.ofMinutes(180),
                deepFocus = true,
                inactive = true,
                createdAt = Instant.ofEpochMilli(1_700_000_000_000L),
                isCompleted = true,
                lastCompletedAt = null
            )
        )

        val stored = goalRepository.getGoal(1).first()

        assertNotNull(stored)
        assertEquals("Updated Goal", stored!!.title)
        assertEquals(GoalType.MONTHLY, stored.type)
        assertEquals(Duration.ofMinutes(180), stored.targetDuration)
        assertTrue(stored.deepFocus)
        assertTrue(stored.inactive)
        assertTrue(stored.isCompleted)
    }

    @Test
    fun getGoalWithSessions_returnsGoalWithAssociatedSessions() = runBlocking {
        seedUserEntity(userId = 1)
        seedGoalEntity(goalId = 1, userId = 1)
        seedTrackingSessionEntity(sessionId = 1, goalId = 1, userId = 1, duration = 1000)
        seedTrackingSessionEntity(sessionId = 2, goalId = 1, userId = 1, duration = 2000)

        val result = goalRepository.getGoalWithSessions(1).first()

        assertNotNull(result)
        assertEquals(1, result!!.goal.id)
        assertEquals(2, result.sessions.size)
    }
}