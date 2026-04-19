package com.example.purrsistence.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.domain.cat.CatRepository
import com.example.purrsistence.focus.DeepFocusAccessibilityState
import androidx.compose.ui.res.painterResource
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.components.GoalBottomDrawer
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    onStartTracking: (Int, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    val user by userViewModel.user.collectAsState()
    val collectedCats = user?.collectedCatsIds ?: emptyList()
    val ownedCats = remember(collectedCats) {
        collectedCats.mapNotNull { CatRepository.getCatById(it) }
    }
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

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = ownedCats,
                            key = { it.id }
                        ) { cat ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {

                                    Image(
                                        painter = painterResource(cat.imageRes),
                                        contentDescription = cat.name,
                                        modifier = Modifier.size(90.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        cat.name,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
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

private fun handleStartTrackingClick(
    goal: Goal?,
    context: android.content.Context,
    onStartTracking: (Int, Int, Boolean) -> Unit,
    onNeedsAccessibilitySetup: () -> Unit
) {
    goal ?: return

    val needsAccessibilitySetup = goal.deepFocus &&
            !DeepFocusAccessibilityState.isServiceEnabled(context)

    if (needsAccessibilitySetup) {
        onNeedsAccessibilitySetup()
    } else {
        onStartTracking(goal.goalId, goal.userId, goal.deepFocus)
    }
}

private fun openAccessibilitySettings(context: android.content.Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(Settings.ACTION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

@Composable
private fun DeepFocusAccessibilityDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Deep Focus Blocking") },
        text = {
            Text(
                "To block other apps during Deep Focus, enable the accessibility service:\n\n" +
                        "1. Tap Open Settings\n" +
                        "2. Accessibility\n" +
                        "3. Use Purrsistence\n" +
                        "4. Turn it on"
            )
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}