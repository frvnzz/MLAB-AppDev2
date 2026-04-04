package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.DataViewModel
import com.example.purrsistence.ui.components.GoalBottomDrawer

@Composable
fun HomeScreen(
    viewModel: DataViewModel
) {
    val goals by viewModel.goals("1").collectAsState(initial = emptyList())

    // Use ViewModel state so that user can switch between screens and selectedGoalId is remembered
    val selectedGoalId = viewModel.selectedGoalId

    // Auto-select first goal if none is selected
    LaunchedEffect(goals) {
        if (selectedGoalId == null && goals.isNotEmpty()) {
            viewModel.selectGoal(goals.first().goal.goalId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        GoalBottomDrawer(
            goals = goals,
            selectedGoalId = selectedGoalId,
            onGoalSelected = { viewModel.selectGoal(it) },
            onStartClick = { goalId ->
                // TODO: add timer logic to start tracking goal
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Welcome Home!", style = MaterialTheme.typography.titleLarge)

                // Cat UI can go here later :)
            }
        }
    }
}