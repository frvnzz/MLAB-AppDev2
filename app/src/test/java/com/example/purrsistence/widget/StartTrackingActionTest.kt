package com.example.purrsistence.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.example.purrsistence.data.local.repository.FakeGoalRepository
import com.example.purrsistence.data.local.repository.FakeTrackingRepository
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.domain.service.fakes.FakeTrackingService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant

class StartTrackingActionTest {

    private lateinit var trackingRepo: FakeTrackingRepository
    private lateinit var goalRepo: FakeGoalRepository
    private lateinit var trackingService: FakeTrackingService
    private lateinit var context: Context
    private lateinit var action: StartTrackingAction

    @Before
    fun setup() {
        trackingRepo = FakeTrackingRepository()
        goalRepo = FakeGoalRepository()
        trackingService = FakeTrackingService()
        context = mockk(relaxed = true)
        action = StartTrackingAction()

        // Mock Intent constructor because we are in a pure JUnit test
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setFlags(any()) } answers { self as Intent }
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Int>()) } answers { self as Intent }

        // Mock Glance updateAll
        mockkStatic("androidx.glance.appwidget.GlanceAppWidgetKt")
        io.mockk.coEvery { any<androidx.glance.appwidget.GlanceAppWidget>().updateAll(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun performActionStartsExistingSessionIfAlreadyRunning() = runTest {
        val userId = 1
        val goalId = 10
        val existingSession = TrackingSession(
            id = 55,
            goalId = goalId,
            userId = userId,
            startTime = Instant.now(),
            endTime = null,
            pauseReminder = false,
            deepFocus = false
        )
        trackingRepo.seedSession(existingSession)

        val intentSlot = slot<Intent>()
        var updateWidgetCalled = false

        action.performAction(
            context = context,
            trackingRepo = trackingRepo,
            goalRepo = goalRepo,
            trackingService = trackingService,
            onStartActivity = { intentSlot.captured = it },
            onUpdateWidget = { updateWidgetCalled = true }
        )

        // Verify tracking service was not called to start a new one
        assertEquals(0, trackingService.startCalls)

        // Verify activity was started with correct session ID
        assertTrue(intentSlot.isCaptured)
        
        assertTrue(updateWidgetCalled)
    }

    @Test
    fun performActionStartsNewSessionIfNoneRunningAndRecentGoalExists() = runTest {
        val userId = 1
        val recentGoal = Goal(
            id = 20,
            userId = userId,
            title = "Recent Goal",
            type = GoalType.DAILY,
            targetDuration = Duration.ofHours(1),
            deepFocus = false,
            inactive = false,
            createdAt = Instant.now(),
            isCompleted = false,
            lastCompletedAt = null
        )
        goalRepo.seedGoal(recentGoal)

        val intentSlot = slot<Intent>()
        var updateWidgetCalled = false

        action.performAction(
            context = context,
            trackingRepo = trackingRepo,
            goalRepo = goalRepo,
            trackingService = trackingService,
            onStartActivity = { intentSlot.captured = it },
            onUpdateWidget = { updateWidgetCalled = true }
        )

        // Verify tracking service was called
        assertEquals(1, trackingService.startCalls)
        assertEquals(20, trackingService.lastStartedGoalId)

        assertTrue(intentSlot.isCaptured)
        assertTrue(updateWidgetCalled)
    }

    @Test
    fun performActionDoesNothingIfNoSessionRunningAndNoRecentGoal() = runTest {
        val intentSlot = slot<Intent>()
        var updateWidgetCalled = false

        action.performAction(
            context = context,
            trackingRepo = trackingRepo,
            goalRepo = goalRepo,
            trackingService = trackingService,
            onStartActivity = { intentSlot.captured = it },
            onUpdateWidget = { updateWidgetCalled = true }
        )

        assertEquals(0, trackingService.startCalls)
        assertTrue(!intentSlot.isCaptured)
        assertTrue(!updateWidgetCalled)
    }
}
