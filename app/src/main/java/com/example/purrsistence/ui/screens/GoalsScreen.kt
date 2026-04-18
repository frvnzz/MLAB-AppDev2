package com.example.purrsistence.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.repeatOnLifecycle
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun GoalsScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    onAddGoalClick: () -> Unit = {},
    onGoalClick: (Int) -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    // get all goals from the current user
    val goals by goalViewModel.goals(userViewModel.currentUserId)
        .collectAsState(initial = emptyList())

    var isEditMode by remember { mutableStateOf(false) }
    var selectedGoals by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            isEditMode = false
            selectedGoals = emptySet()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TOP BAR
            TopBar(
                title = "Your Goals",
                actions = if (goals.isNotEmpty()) {
                    {
                        Row {
                            if (isEditMode) {
                                // Delete Button
                                Button(
                                    onClick = {
                                        if (selectedGoals.isEmpty()) {
                                            // show snackbar when no goals are selected for deletion
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Select at least one goal to delete")
                                            }
                                        } else {
                                            showDeleteDialog = true
                                        }
                                    },
                                    shape = MaterialTheme.shapes.large,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Text("Delete (${selectedGoals.size})", style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            // Toggle Edit-Mode Button
                            Button(
                                onClick = {
                                    isEditMode = !isEditMode
                                    selectedGoals = emptySet()
                                },
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(
                                    if (isEditMode) "Cancel" else "Edit",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                } else null
            )

            // CONTENT
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (goals.isEmpty()) {
                    item {
                        Text("No goals yet - Add one! 🐱")
                    }
                } else {
                    items(
                        items = goals,
                        key = { it.goal.goalId }
                    ) { goalWithSessions ->

                        val goal = goalWithSessions.goal
                        val isSelected = selectedGoals.contains(goal.goalId)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    // click on a Card to edit the goal (when isEditMode)
                                    if (isEditMode) Modifier.clickable {
                                        onGoalClick(goal.goalId)
                                    } else Modifier
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(goal.title, style = MaterialTheme.typography.titleMedium)
                                    Text("Type: ${goal.type}")

                                    val minutes = goal.targetDuration
                                    val hoursFloat = minutes / 60f
                                    val displayHours =
                                        String.format(Locale.GERMANY, "%.1f", hoursFloat)
                                    val trackedMinutes =
                                        goalWithSessions.totalTrackedMillis / 1000 / 60
                                    val trackedHours = trackedMinutes / 60
                                    val trackedRemainderMinutes = trackedMinutes % 60

                                    Text("Duration: ${displayHours}h (${minutes} min)")
                                    Text("Tracked: ${trackedHours}h ${trackedRemainderMinutes}min")

                                    Text("Deep Focus: ${if (goal.deepFocus) "ON" else "OFF"}")
                                    Text("Inactive: ${if (goal.inactive) "YES" else "NO"}")
                                    Text("Created: ${goal.createdAt}")
                                    Text("Completed: ${if (goal.isCompleted) "YES" else "NO"}")
                                }

                                if (isEditMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            selectedGoals = if (it) {
                                                selectedGoals + goal.goalId
                                            } else {
                                                selectedGoals - goal.goalId
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Goal Button (FAB) - always floats bottom right
        FloatingActionButton(
            onClick = onAddGoalClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Goal")
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Goals") },
                text = {
                    Text("Are you sure you want to delete ${selectedGoals.size} goals?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedGoals.forEach { goalViewModel.deleteGoal(it) }
                            selectedGoals = emptySet()
                            isEditMode = false
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}