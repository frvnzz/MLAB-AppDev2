package com.example.purrsistence.data.local.repository


import com.example.purrsistence.data.local.dao.Dao
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.domain.time.TimeProvider

interface TrackingRepository {
    suspend fun startTracking(goalId: Int, userId: Int, pauseReminder: Boolean = false): TrackingSession
    suspend fun stopTracking(trackingId: Int)
    suspend fun getTrackingSessionById(trackingId: Int): TrackingSession?
    suspend fun getActiveTrackingSession(goalId: Int): TrackingSession?
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

    fun calculateCurrencyEarned(trackingDuration: Long): Int{
        val rewardedCoins = (trackingDuration / 1000 / 60).toInt()

        return rewardedCoins
    }

    override suspend fun stopTracking(trackingId: Int) {
        val endTime = timeProvider.now()
        dao.stopTrackingSession(trackingId, endTime)

        val finishedSession = dao.getTrackingSessionById(trackingId) ?: return
        val durationMillis = finishedSession.endTime?.minus(finishedSession.startTime) ?: return

        val rewardedCurrency = calculateCurrencyEarned(durationMillis)
        
        if(rewardedCurrency > 0){
            dao.addCurrency(finishedSession.userId, rewardedCurrency)
        }
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return dao.getTrackingSessionById(trackingId)
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return dao.getActiveTrackingSession(goalId)
    }
}