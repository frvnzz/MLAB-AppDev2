package com.example.purrsistence.ui.tracking

import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.domain.focus.FocusBlocker
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelDeepFocusTest {

    @Test
    fun startTrack_withDeepFocus_startsBlocking() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeTrackingRepositoryForViewModel()
            val blocker = FakeFocusBlocker()
            val viewModel = TrackingViewModel(repository, FakeTimeProvider(1_000L), blocker)

            viewModel.startTrack(goalId = 9, userId = 1, deepFocus = true)
            runCurrent()

            assertEquals(1, blocker.startCalls)
            assertEquals(9, repository.lastStartedGoalId)
            assertEquals(1, repository.lastStartedUserId)

            viewModel.stopTracking()
            runCurrent()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun startTrack_withoutDeepFocus_doesNotStartBlocking() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeTrackingRepositoryForViewModel()
            val blocker = FakeFocusBlocker()
            val viewModel = TrackingViewModel(repository, FakeTimeProvider(1_000L), blocker)

            viewModel.startTrack(goalId = 9, userId = 1, deepFocus = false)
            runCurrent()

            assertEquals(0, blocker.startCalls)

            viewModel.stopTracking()
            runCurrent()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun stopTracking_afterDeepFocusSession_stopsBlocking() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeTrackingRepositoryForViewModel()
            val blocker = FakeFocusBlocker()
            val viewModel = TrackingViewModel(repository, FakeTimeProvider(1_000L), blocker)

            viewModel.startTrack(goalId = 9, userId = 1, deepFocus = true)
            runCurrent()
            viewModel.stopTracking()
            runCurrent()

            assertEquals(1, blocker.stopCalls)
            assertEquals(listOf(1), repository.stoppedTrackingIds)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeFocusBlocker : FocusBlocker {
    var startCalls = 0
    var stopCalls = 0

    override fun startBlocking() {
        startCalls++
    }

    override fun stopBlocking() {
        stopCalls++
    }
}

private class FakeTrackingRepositoryForViewModel : TrackingRepository {
    private var nextTrackingId = 1
    var lastStartedGoalId: Int? = null
    var lastStartedUserId: Int? = null
    val stoppedTrackingIds = mutableListOf<Int>()
    private val sessions = mutableListOf<TrackingSession>()

    override suspend fun startTracking(
        goalId: Int,
        userId: Int,
        pauseReminder: Boolean
    ): TrackingSession {
        val session = TrackingSession(
            trackingId = nextTrackingId++,
            goalId = goalId,
            userId = userId,
            pauseReminder = pauseReminder,
            startTime = 1_000L,
            endTime = null
        )
        lastStartedGoalId = goalId
        lastStartedUserId = userId
        sessions.add(session)
        return session
    }

    override suspend fun stopTracking(trackingId: Int) {
        stoppedTrackingIds.add(trackingId)
    }

    override suspend fun getTrackingSessionById(trackingId: Int): TrackingSession? {
        return sessions.find { it.trackingId == trackingId }
    }

    override suspend fun getActiveTrackingSession(goalId: Int): TrackingSession? {
        return sessions.lastOrNull { it.goalId == goalId && it.endTime == null }
    }
}
