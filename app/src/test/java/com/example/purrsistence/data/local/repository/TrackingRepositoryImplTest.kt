package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.FakeTrackingDao
import com.example.purrsistence.domain.time.FakeTimeProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TrackingRepositoryImplTest {

    @Test
    fun startTracking_createsSessionWithStartTime_andStoresIt() = runBlocking {
        val fakeDao = FakeTrackingDao()
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
        val fakeDao = FakeTrackingDao()
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
    fun observeTotalTime_returnsFinishedTrackedDuration() = runBlocking {
        val fakeDao = FakeTrackingDao()
        val fakeTimeProvider = FakeTimeProvider(1000L)
        val repository = TrackingRepositoryImpl(fakeDao, fakeTimeProvider)

        val started = repository.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
        )

        fakeTimeProvider.currentTime = 4000L
        repository.stopTracking(started.trackingId)

        val total = fakeDao.observeTotalTime(7L)

        var value: Long? = null
        total.collect {
            value = it
        }

        assertEquals(3000L, value)
    }
}