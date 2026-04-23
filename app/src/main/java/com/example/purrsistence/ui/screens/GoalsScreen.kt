package com.example.purrsistence.ui.screens

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.purrsistence.ui.components.goalsScreen.GoalSearchBar
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.components.goalsScreen.DeleteGoalDialog
import com.example.purrsistence.ui.components.goalsScreen.GoalCard
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun GoalsScreen(
    userViewModel: UserViewModel,
    goalViewModel: GoalViewModel,
    onAddGoalClick: () -> Unit = {},
    onGoalClick: (Int) -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    // get all goals from the current user
    val goals by goalViewModel
        .searchedGoals(userViewModel.currentUserId)
        .collectAsState(initial = emptyList())
    // search goals (GoalSearchBar)
    val query = goalViewModel.searchQuery
    val isSearching = query.isNotBlank()

    // Edit / delete goal
    var isEditMode by remember { mutableStateOf(false) }
    var selectedGoals by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Reset search query and edit mode when screen is resumed
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            goalViewModel.onSearchQueryChange("")   // reset search
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

            // SEARCH BAR
            GoalSearchBar(
                query = goalViewModel.searchQuery,
                onQueryChange = goalViewModel::onSearchQueryChange
            )

            // CONTENT
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // if no goals are added or found, show a message
                if (goals.isEmpty()) {
                    item {
                        val message = if (isSearching) {
                            "No results for \"$query\""
                        } else {
                            "No goals yet - Add one! 🐱"
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(message)
                        }

                    }
                // List Goal Cards that are found
                } else {
                    items(
                        items = goals,
                        key = { it.goal.goalId }
                    ) { goalWithSessions ->

                        val goal = goalWithSessions.goal
                        val isSelected = selectedGoals.contains(goal.goalId)

                        GoalCard(
                            goalWithSessions = goalWithSessions,
                            isEditMode = isEditMode,
                            isSelected = isSelected,
                            onClick = {
                                onGoalClick(goal.goalId)
                            },
                            onCheckedChange = { checked ->
                                selectedGoals = if (checked) {
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
            DeleteGoalDialog(
                message = "Are you sure you want to delete ${selectedGoals.size} goals?",
                onConfirm = {
                    selectedGoals.forEach { goalViewModel.deleteGoal(it) }
                    selectedGoals = emptySet()
                    isEditMode = false
                    showDeleteDialog = false
                },
                onDismiss = {
                    showDeleteDialog = false
                }
            )
        }
    }
}