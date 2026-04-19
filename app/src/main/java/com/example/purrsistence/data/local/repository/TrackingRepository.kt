package com.example.purrsistence.data.local.repository


import com.example.purrsistence.data.local.dao.Dao
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.domain.time.TimeProvider
import kotlin.math.round

// TODO: refactor logic to service layer (Ramon) :)

interface TrackingRepository {
    suspend fun startTracking(goalId: Int, userId: Int, pauseReminder: Boolean = false): TrackingSession
    suspend fun stopTracking(trackingId: Int)
    suspend fun getTrackingSessionById(trackingId: Int): TrackingSession?
    suspend fun getActiveTrackingSession(goalId: Int): TrackingSession?
    fun calculateReward(trackingDuration: Long): Pair<Int, Double>
}
class TrackingRepositoryImpl (
    private val dao: Dao,
    private val timeProvider: TimeProvider
) : TrackingRepository{

    override suspend fun startTracking(
        goalId: Int,
        userId: Int,
        pauseReminder: Boolean
    ): TrackingSession {
        val session = TrackingSession(
            goalId = goalId,
            userId = userId,
            pauseReminder = pauseReminder,
            startTime = timeProvider.now(),
            endTime = null
        )

        val id = dao.insertTrackingSession(session).toInt()
        return session.copy(trackingId = id)
    }

    // CURRENCY

    override fun calculateReward(trackingDuration: Long): Pair<Int, Double> {
        val trackedMinutes = (trackingDuration/ 1000 / 60).toInt()

        val mult = calculateRewardMultiplier(trackedMinutes)
        val coins = round(trackedMinutes * mult).toInt()

        return coins to mult
    }

    // TRACKING SESSION

    override suspend fun stopTracking(trackingId: Int) {
        val endTime = timeProvider.now()
        dao.stopTrackingSession(trackingId, endTime)

        val finishedSession = dao.getTrackingSessionById(trackingId) ?: return
        val durationMillis = finishedSession.endTime?.minus(finishedSession.startTime) ?: return

        val (coins, _) = calculateReward(durationMillis)

        if (coins > 0) {
            dao.addCurrency(finishedSession.userId, coins)
        }
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return dao.getTrackingSessionById(trackingId)
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return dao.getActiveTrackingSession(goalId)
    }

    private fun calculateRewardMultiplier(trackedMinutes: Int): Double {
        if (trackedMinutes < 15) return 1.0

        val additionalReward = (trackedMinutes - 15) / 15
        val multiplier = 1.15 + (additionalReward * 0.10)

        return multiplier.coerceAtMost(2.0)
    }
}