package com.example.purrsistence.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.navigation.TrackingEvent
import com.example.purrsistence.ui.viewmodel.TrackingViewModel


@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onNavigateBackHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is TrackingEvent.NavigateBackHome) {
                onNavigateBackHome()
            }
        }
    }

    if (state.rewardedCurrency != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "+${state.rewardedCurrency!!} coins",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tracked Time: ${
                    formatDuration(state.sessionDurationMillis ?: 0L)
                }",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Multiplier: x${"%.2f".format(state.multiplier ?: 1.0)}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = viewModel::returnHome) {
                Text("Return Home")
            }
        }
    } else {
        // TIMER CONTENT
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatDuration(state.elapsedMillis),
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Button(onClick = { /* TODO: Pause functionality here */ }) {
                    Text("Pause")
                }

                Button(
                    onClick = { viewModel.stopTracking() },
                    enabled = state.isTracking
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}