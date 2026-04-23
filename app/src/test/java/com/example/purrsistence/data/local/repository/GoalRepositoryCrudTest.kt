package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.FakeDao
import com.example.purrsistence.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalRepositoryCrudTest {

    @Test
    fun createGoal_insertsGoal_andGoalAppearsInList() = runBlocking {
        val dao = FakeDao()
        val repository = GoalRepository(dao)

        dao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        repository.createGoal(
            userId = 1,
            title = "Read papers",
            type = "Weekly",
            weeklyMinutes = 120,
            deepFocus = true,
            inactive = false,
            createdAt = 1000L,
            isCompleted = false
        )

        val goals = repository.getGoals(1).first()

        assertEquals(1, goals.size)
        assertEquals("Read papers", goals[0].goalEntity.title)
        assertEquals("Weekly", goals[0].goalEntity.type)
        assertEquals(120, goals[0].goalEntity.targetDuration)
        assertTrue(goals[0].goalEntity.deepFocus)
    }

    @Test
    fun getGoals_returnsOnlyGoalsOfRequestedUser() = runBlocking {
        val dao = FakeDao()
        val repository = GoalRepository(dao)

        dao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )
        dao.insertUser(
            UserEntity(
                userId = 2,
                username = "TestUser2",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        repository.createGoal(
            userId = 1,
            title = "Goal A",
            type = "Weekly",
            weeklyMinutes = 60,
            deepFocus = false,
            inactive = false,
            createdAt = 1000L,
            isCompleted = false
        )

        repository.createGoal(
            userId = 2,
            title = "Goal B",
            type = "Daily",
            weeklyMinutes = 30,
            deepFocus = true,
            inactive = false,
            createdAt = 2000L,
            isCompleted = false
        )

        val user1Goals = repository.getGoals(1).first()
        val user2Goals = repository.getGoals(2).first()

        assertEquals(1, user1Goals.size)
        assertEquals("Goal A", user1Goals[0].goalEntity.title)

        assertEquals(1, user2Goals.size)
        assertEquals("Goal B", user2Goals[0].goalEntity.title)
    }

    @Test
    fun getGoal_returnsCorrectGoal() = runBlocking {
        val dao = FakeDao()
        val repository = GoalRepository(dao)

        dao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        repository.createGoal(
            userId = 1,
            title = "Write thesis",
            type = "Monthly",
            weeklyMinutes = 300,
            deepFocus = true,
            inactive = false,
            createdAt = 3000L,
            isCompleted = false
        )

        val createdGoalId = repository.getGoals(1).first().first().goalEntity.goalId
        val goal = repository.getGoal(createdGoalId).first()

        assertNotNull(goal)
        assertEquals("Write thesis", goal!!.title)
        assertEquals("Monthly", goal.type)
        assertEquals(300, goal.targetDuration)
    }

    @Test
    fun updateGoal_updatesStoredGoal() = runBlocking {
        val dao = FakeDao()
        val repository = GoalRepository(dao)

        dao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        repository.createGoal(
            userId = 1,
            title = "Old Title",
            type = "Weekly",
            weeklyMinutes = 90,
            deepFocus = false,
            inactive = false,
            createdAt = 4000L,
            isCompleted = false
        )

        val goalId = repository.getGoals(1).first().first().goalEntity.goalId

        repository.updateGoal(
            goalId = goalId,
            title = "New Title",
            type = "Daily",
            hours = 45,
            deepFocus = true
        )

        val updatedGoal = repository.getGoal(goalId).first()

        assertNotNull(updatedGoal)
        assertEquals("New Title", updatedGoal!!.title)
        assertEquals("Daily", updatedGoal.type)
        assertEquals(45, updatedGoal.targetDuration)
        assertTrue(updatedGoal.deepFocus)
    }

    @Test
    fun deleteGoal_removesGoalFromList() = runBlocking {
        val dao = FakeDao()
        val repository = GoalRepository(dao)

        dao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        repository.createGoal(
            userId = 1,
            title = "Temporary Goal",
            type = "Weekly",
            weeklyMinutes = 50,
            deepFocus = false,
            inactive = false,
            createdAt = 5000L,
            isCompleted = false
        )

        val goalId = repository.getGoals(1).first().first().goalEntity.goalId

        repository.deleteGoal(goalId)

        val goalsAfterDelete = repository.getGoals(1).first()
        val goalAfterDelete = repository.getGoal(goalId).first()

        assertTrue(goalsAfterDelete.isEmpty())
        assertNull(goalAfterDelete)
    }

    @Test
    fun searchGoals_returnsOnlyMatchingGoalsForUser() = runBlocking {
        val fakeDao = FakeDao()
        val repository = GoalRepository(fakeDao)

        fakeDao.insertUser(
            UserEntity(
                userId = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        fakeDao.insertUser(
            UserEntity(
                userId = 2,
                username = "OtherUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        repository.createGoal(
            userId = 1,
            title = "Read Research Papers",
            type = "Weekly",
            weeklyMinutes = 120,
            deepFocus = true,
            inactive = false,
            createdAt = 1000L,
            isCompleted = false
        )

        repository.createGoal(
            userId = 1,
            title = "Workout",
            type = "Daily",
            weeklyMinutes = 60,
            deepFocus = false,
            inactive = false,
            createdAt = 2000L,
            isCompleted = false
        )

        repository.createGoal(
            userId = 2,
            title = "Read Book",
            type = "Weekly",
            weeklyMinutes = 90,
            deepFocus = false,
            inactive = false,
            createdAt = 3000L,
            isCompleted = false
        )

        val results = repository.searchGoals(userId = 1, query = "read").first()

        assertEquals(1, results.size)
        assertEquals("Read Research Papers", results[0].goalEntity.title)
        assertTrue(results.all { it.goalEntity.userId == 1 })
    }
}