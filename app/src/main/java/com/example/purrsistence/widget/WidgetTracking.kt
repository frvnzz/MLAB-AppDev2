package com.example.purrsistence.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.purrsistence.data.local.AppDatabase
import com.example.purrsistence.data.local.repository.GoalRepositoryImpl
import com.example.purrsistence.data.local.repository.TrackingRepositoryImpl
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession

class WidgetTracking : GlanceAppWidget(){
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val goalRepo = GoalRepositoryImpl(db.goalsDao())
        val trackingRepo = TrackingRepositoryImpl(db.trackingDao())
        val userId = 1 // Consistent with StartTrackingAction
        
        val runningSession = trackingRepo.getRunningSession(userId)
        val recentGoal = goalRepo.getMostRecentlyTrackedGoal(userId)

        provideContent {
            MyContent(runningSession, recentGoal)
        }
    }

    @Composable
    private fun MyContent(runningSession: TrackingSession?, recentGoal: Goal?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.White)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (runningSession != null) {
                    Text(
                        text = "Currently tracking!",
                        modifier = GlanceModifier.padding(bottom = 8.dp),
                        style = TextStyle(color = ColorProvider(Color.Black))
                    )
                    Button(
                        text = "Open App",
                        onClick = actionRunCallback<StartTrackingAction>()
                    )
                } else {
                    val title = recentGoal?.let { "Continue with: ${it.title}" } ?: "No recent goals"
                    Text(
                        text = title,
                        modifier = GlanceModifier.padding(bottom = 8.dp),
                    )

                    if (recentGoal != null) {
                        Row(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                text = "Start Tracking",
                                onClick = actionRunCallback<StartTrackingAction>()
                            )
                        }
                    }
                }
            }
        }
    }
}
