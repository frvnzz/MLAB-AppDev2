package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.domain.time.TimeProvider
import kotlin.math.round


class FakeTrackingRepository(
    private val timeProvider: TimeProvider
) : TrackingRepository {

    private val sessions = mutableListOf<TrackingSession>()
    private var nextId = 1

    override suspend fun startTracking(
        goalId: Int,
        userId: Int,
        pauseReminder: Boolean
    ): TrackingSession {
        val session = TrackingSession(
            trackingId = nextId++,
            goalId = goalId,
            userId = userId,
            pauseReminder = pauseReminder,
            startTime = timeProvider.now(),
            endTime = null
        )
        sessions.add(session)
        return session
    }

    override suspend fun stopTracking(trackingId: Int) {
        val index = sessions.indexOfFirst { it.trackingId == trackingId }
        if (index == -1) return

        val old = sessions[index]
        sessions[index] = old.copy(endTime = timeProvider.now())
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return sessions.find { it.trackingId == trackingId }
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return sessions.lastOrNull { it.goalId == goalId && it.endTime == null }
    }

    override fun calculateReward(trackingDuration: Long): Pair<Int, Double> {
        val trackedMinutes = (trackingDuration / 1000 / 60).toInt()

        val mult = calculateRewardMultiplier(trackedMinutes)
        val coins = round(trackedMinutes * mult).toInt()

        return coins to mult
    }

    private fun calculateRewardMultiplier(trackedMinutes: Int): Double {
        if (trackedMinutes < 15) return 1.0

        val additionalReward = (trackedMinutes - 15) / 15
        val multiplier = 1.15 + (additionalReward * 0.10)

        return multiplier.coerceAtMost(2.0)
    }
}