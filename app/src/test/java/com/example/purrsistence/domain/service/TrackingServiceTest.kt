package com.example.purrsistence.domain.service

import com.example.purrsistence.data.local.repository.FakeTrackingRepository
import com.example.purrsistence.data.local.repository.FakeUserRepository
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.TrackingServiceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

class TrackingServiceTest {

    @Test
    fun startTracking_createsSessionWithCurrentTime() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val rewardService = RewardService()

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
        )

        assertEquals(7, session.goalId)
        assertEquals(1, session.userId)
        assertEquals(Instant.ofEpochMilli(1000L), session.startTime)
        assertNotNull(session.id)
    }

    @Test
    fun stopTracking_finishesSession_andAddsCurrency() = runBlocking {
        val trackingRepository = FakeTrackingRepository()
        val userRepository = FakeUserRepository()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(1000L))
        val rewardService = RewardService()

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 0,
                friends = emptyList(),
                collectedCatsIds = emptyList()
            )
        )

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepository,
            userRepository = userRepository,
            rewardService = rewardService,
            timeProvider = timeProvider
        )

        val session = service.startTracking(
            goalId = 7,
            userId = 1,
            pauseReminder = false
        )

        timeProvider.setNow(Instant.ofEpochMilli(901000L))

        val result = service.stopTracking(session.id)

        assertNotNull(result)
        assertEquals(17, result!!.rewardedCurrency)
        assertEquals(1.15, result.multiplier, 0.0001)
        assertEquals(900000L, result.sessionDurationMillis)

        val updatedUser = userRepository.getUser(1)
        assertEquals(17, updatedUser.first()?.balance)
    }
}