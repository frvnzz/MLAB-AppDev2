package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.ui.components.DeepFocusAccessibilityDialog
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.components.homeScreen.GoalBottomDrawer
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.util.handleStartTrackingClick
import com.example.purrsistence.ui.util.openAccessibilitySettings
import com.example.purrsistence.ui.viewmodel.UserViewModel
import com.example.purrsistence.service.RoomService
import com.example.purrsistence.ui.components.homeScreen.RoomView

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    onStartTracking: (Int, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    // Get current user data (balance, collected cats)
    val user by userViewModel.user.collectAsState()
    val balance = user?.balance ?: 0
    val collectedCats = user?.collectedCatsIds ?: emptyList()
    val ownedCats = remember(collectedCats) {
        collectedCats.mapNotNull { CatList.getCatById(it) }
    }

    // get cats for the room at dedicated spots
    val roomService = remember { RoomService() }
    val spots = remember { roomService.getRoomSpots() }
    val placedCats = remember(collectedCats, spots) {
        roomService.assignCatsToSpots(collectedCats, spots)
    }

    val goals by goalViewModel.goals(1).collectAsState(initial = emptyList())

    // Use ViewModel state so that user can switch between screens and selectedGoalId is remembered
    val selectedGoalId = goalViewModel.selectedGoalId

    // Auto-select first goal if none is selected
    LaunchedEffect(goals) {
        if (selectedGoalId == null && goals.isNotEmpty()) {
            goalViewModel.selectGoal(goals.first().goal.id)
        }
    }

    val selectedGoal = goals.find { it.goal.id == selectedGoalId }?.goal

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        GoalBottomDrawer(
            goals = goals,
            selectedGoalId = selectedGoalId,
            onGoalSelected = { goalViewModel.selectGoal(it) },
            onStartClick = {
                handleStartTrackingClick(
                    goal = selectedGoal,
                    context = context,
                    onStartTracking = onStartTracking,
                    onNeedsAccessibilitySetup = { showAccessibilityDialog = true }
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TopBar(
                    title = "Your Cats",
                    actions = {
                        CurrencyBadge(balance = balance)
                    }
                )

                if (ownedCats.isEmpty()) {
                    Text("No cats yet - go adopt some 🐱")
                } else {

                    RoomView(
                        placedCats = placedCats,
                        spots = spots,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (showAccessibilityDialog) {
            DeepFocusAccessibilityDialog(
                onDismiss = { showAccessibilityDialog = false },
                onOpenSettings = {
                    showAccessibilityDialog = false
                    openAccessibilitySettings(context)
                }
            )
        }
    }
}