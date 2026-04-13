package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.components.GoalBottomDrawer
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    onStartTracking: (Int, Int) -> Unit
) {
    val balance by userViewModel.userBalance.collectAsState()

    val goals by goalViewModel.goals(1).collectAsState(initial = emptyList())

    // Use ViewModel state so that user can switch between screens and selectedGoalId is remembered
    val selectedGoalId = goalViewModel.selectedGoalId

    // Auto-select first goal if none is selected
    LaunchedEffect(goals) {
        if (selectedGoalId == null && goals.isNotEmpty()) {
            goalViewModel.selectGoal(goals.first().goal.goalId)
        }
    }

    val selectedGoal = goals.find { it.goal.goalId == selectedGoalId }?.goal

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        GoalBottomDrawer(
            goals = goals,
            selectedGoalId = selectedGoalId,
            onGoalSelected = { goalViewModel.selectGoal(it) },
            onStartClick = {
                selectedGoal?.let {
                    onStartTracking(it.goalId, it.userId)
                }
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Welcome Home!",
                        style = MaterialTheme.typography.titleLarge
                    )
                    // Display user's currency balance
                    CurrencyBadge(balance = balance)
                }
                // Cat UI can go here later :)
            }
        }
    }
}