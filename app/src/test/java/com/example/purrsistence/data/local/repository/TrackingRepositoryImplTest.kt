package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.FakeDao
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.domain.time.FakeTimeProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TrackingRepositoryImplTest {

    @Test
    fun startTracking_createsSessionWithStartTime_andStoresIt() = runBlocking {
        val fakeDao = FakeDao()
        val fakeTimeProvider = FakeTimeProvider(1000L)
        val repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        val session = repository.startTracking(
            goalId = 1,
            userId = 3,
            pauseReminder = false
        )

        assertEquals(1, session.trackingId)
        assertEquals(1, session.goalId)
        assertEquals(3, session.userId)
        assertEquals(1000L, session.startTime)
        assertNull(session.endTime)

        val activeSession = repository.getActiveTrackingSession(1)

        assertNotNull(activeSession)
        assertEquals(1, activeSession!!.trackingId)
        assertEquals(1000L, activeSession.startTime)
        assertNull(activeSession.endTime)
    }

    @Test
    fun stopTracking_setsEndTime_onStoredSession() = runBlocking {
        val fakeDao = FakeDao()
        val fakeTimeProvider = FakeTimeProvider(1000L)
        val repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        val started = repository.startTracking(
            goalId = 1,
            userId = 4,
            pauseReminder = false
        )

        fakeTimeProvider.currentTime = 5000L
        repository.stopTracking(started.trackingId)

        val stored = repository.getTrackingSessionById(started.trackingId)

        assertNotNull(stored)
        assertEquals(1000L, stored!!.startTime)
        assertEquals(5000L, stored.endTime)
    }

    @Test
    fun stopTracking_addsCurrency() = runBlocking {
        val fakeDao = FakeDao()
        val fakeTimeProvider= FakeTimeProvider(1000L)
        val repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        fakeDao.insertUser(
            User(
                userId = 10,
                username = "TestUser",
                balance = 10,
                friends= emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        val started = repository.startTracking(
            goalId = 2,
            userId = 10,
            pauseReminder = false
        )

        fakeTimeProvider.currentTime = 181000L
        repository.stopTracking(started.trackingId)

        val updateUser= fakeDao.getUserById(10)

        assertNotNull(updateUser)
        assertEquals("TestUser",updateUser!!.username)
        assertEquals(13, updateUser.balance)

    }

    @Test
    fun observeTotalTime_returnsFinishedTrackedDuration() = runBlocking {
        val fakeDao = FakeDao()
        val fakeTimeProvider = FakeTimeProvider(1000L)
        val repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        val started = repository.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
        )

        fakeTimeProvider.currentTime = 4000L
        repository.stopTracking(started.trackingId)

        val total = fakeDao.observeTotalTime(7)

        var value: Long? = null
        total.collect {
            value = it
        }

        assertEquals(3000L, value)
    }

    @Test
    fun stopTracking_appliesRewardMultipliersAndCap_correctly() = runBlocking {


        var fakeDao = FakeDao()
        var fakeTimeProvider = FakeTimeProvider(1000L)
        var repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        fakeDao.insertUser(
            User(
                userId = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        var started = repository.startTracking(
            goalId = 1,
            userId = 1,
            pauseReminder = false
        )

        // 10 minutes
        fakeTimeProvider.currentTime = 601000L
        repository.stopTracking(started.trackingId)

        var updatedUser = fakeDao.getUserById(1)
        assertNotNull(updatedUser)
        assertEquals(10, updatedUser!!.balance)


        fakeDao = FakeDao()
        fakeTimeProvider = FakeTimeProvider(1000L)
        repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        fakeDao.insertUser(
            User(
                userId = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        started = repository.startTracking(
            goalId = 1,
            userId = 1,
            pauseReminder = false
        )

        // 15 minutes
        fakeTimeProvider.currentTime = 901000L
        repository.stopTracking(started.trackingId)

        updatedUser = fakeDao.getUserById(1)
        assertNotNull(updatedUser)
        assertEquals(17, updatedUser!!.balance)


        fakeDao = FakeDao()
        fakeTimeProvider = FakeTimeProvider(1000L)
        repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        fakeDao.insertUser(
            User(
                userId = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        started = repository.startTracking(
            goalId = 1,
            userId = 1,
            pauseReminder = false
        )

        // 30 minutes
        fakeTimeProvider.currentTime = 1_801_000L
        repository.stopTracking(started.trackingId)

        updatedUser = fakeDao.getUserById(1)
        assertNotNull(updatedUser)
        assertEquals(38, updatedUser!!.balance)


        // ---- Case 4: 45 minutes -> 1.35x ----
        fakeDao = FakeDao()
        fakeTimeProvider = FakeTimeProvider(1000L)
        repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        fakeDao.insertUser(
            User(
                userId = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        started = repository.startTracking(
            goalId = 1,
            userId = 1,
            pauseReminder = false
        )

        // 45 minutes
        fakeTimeProvider.currentTime = 2_701_000L
        repository.stopTracking(started.trackingId)

        updatedUser = fakeDao.getUserById(1)
        assertNotNull(updatedUser)
        assertEquals(61, updatedUser!!.balance)


        // ---- Case 5: cap at 2.0x ----
        fakeDao = FakeDao()
        fakeTimeProvider = FakeTimeProvider(1000L)
        repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        fakeDao.insertUser(
            User(
                userId = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        started = repository.startTracking(
            goalId = 1,
            userId = 1,
            pauseReminder = false
        )

        // 150 minutes -> multiplier would exceed 2.0, so it must cap at 2.0
        fakeTimeProvider.currentTime = 9_001_000L
        repository.stopTracking(started.trackingId)

        updatedUser = fakeDao.getUserById(1)
        assertNotNull(updatedUser)
        assertEquals(300, updatedUser!!.balance)
    }


}