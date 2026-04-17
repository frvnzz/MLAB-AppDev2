package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.GoalStat

@Composable
fun GoalStatsList(goals: List<GoalStat>) {

    val max = goals.firstOrNull()?.totalMinutes ?: 1

    Column{
        Text("Tracked Time per Goal", style = MaterialTheme.typography.labelLarge)

        LazyColumn {
            items(goals) { goal ->
                val progress = goal.totalMinutes / max.toFloat()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(goal.goalName)
                        Text("${goal.totalMinutes} min")
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = ProgressIndicatorDefaults.linearColor,
                        trackColor = ProgressIndicatorDefaults.linearTrackColor,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                }
            }
        }
    }
}