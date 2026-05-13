package com.example.purrsistence.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.purrsistence.MainActivity
import com.example.purrsistence.data.local.AppDatabase
import com.example.purrsistence.data.local.repository.GoalRepositoryImpl
import com.example.purrsistence.data.local.repository.TrackingRepositoryImpl
import com.example.purrsistence.data.local.repository.UserRepositoryImpl
import com.example.purrsistence.domain.time.SystemTimeProvider
import com.example.purrsistence.service.GoalService
import com.example.purrsistence.service.RewardService
import com.example.purrsistence.service.TrackingServiceImpl

class StartTrackingAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {

        val db = AppDatabase.getInstance(context)

        val trackingRepo = TrackingRepositoryImpl(db.trackingDao())
        val goalRepo = GoalRepositoryImpl(db.goalsDao())
        val goalService = GoalService(
            goalRepository = goalRepo,
            timeProvider = SystemTimeProvider())

        val service = TrackingServiceImpl(
            trackingRepository = trackingRepo,
            userRepository = UserRepositoryImpl(db.userDao()),
            goalRepository = goalRepo,
            goalService = goalService,
            rewardService = RewardService(),
            timeProvider = SystemTimeProvider()
        )

        val userId = 1

        val running = trackingRepo.getRunningSession(userId)

        val session = if(running != null) {
            running
        } else {
            val recentGoal =
                goalRepo.getMostRecentlyTrackedGoal(userId)
                    ?: return

            service.startTracking(
                goalId = recentGoal.id,
                userId = userId
            )
        }

        //open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra("tracking_session_id", session.id)
        }

        context.startActivity(intent)
        WidgetTracking().updateAll(context)
    }
}