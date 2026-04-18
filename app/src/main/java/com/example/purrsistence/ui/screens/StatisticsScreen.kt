package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.GoalStatsList
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.components.WeekSelector
import com.example.purrsistence.ui.components.WeeklyChart

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {

    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        CircularProgressIndicator()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TopBar(
            title = "Statistics"
        )

        WeekSelector(viewModel, state)

        Spacer(modifier = Modifier.height(16.dp))

        WeeklyChart(state.dailyStats)

        Spacer(modifier = Modifier.height(40.dp))

        GoalStatsList(state.goalStats)
    }
}