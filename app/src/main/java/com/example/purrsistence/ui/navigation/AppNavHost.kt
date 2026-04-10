package com.example.purrsistence.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.purrsistence.ui.DataViewModel
import com.example.purrsistence.ui.screens.AddGoalScreen
import com.example.purrsistence.ui.screens.GoalsScreen
import com.example.purrsistence.ui.screens.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: DataViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        // all screens go here :)
        // TODO: Maybe replace with single source of truth for routes (screen model) in the future

        // HOME
        composable("home") { HomeScreen(
            viewModel = viewModel
        ) }
        // GOALS
        composable("goals") {
            GoalsScreen(
                viewModel = viewModel,
                onAddGoalClick = {
                    navController.navigate("add_goal")
                }
            )
        }
        // -> add goal
        composable("add_goal") {
            AddGoalScreen(
                onSave = { title, type, minutes, deepFocus, inactive ->
                    viewModel.addGoal(
                        userId = 1,
                        title = title,
                        type = type,
                        weeklyMinutes = minutes,
                        deepFocus = deepFocus,
                        inactive = inactive,
                        createdAt = System.currentTimeMillis(),
                        isCompleted = false
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}