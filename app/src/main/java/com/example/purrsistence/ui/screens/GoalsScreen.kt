package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.DataViewModel

@Composable
fun GoalsScreen(
    viewModel: DataViewModel,
    onAddGoalClick: () -> Unit = {}
) {
    // collect goals from ViewModel by userId (= string)
    val goals by viewModel.goals("1").collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Your Goals", style = MaterialTheme.typography.titleLarge)
            }

            if (goals.isEmpty()) {
                item {
                    Text("No goals yet - Add one! 🐱")
                }
            } else {
                items(goals) { goalWithSessions ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = goalWithSessions.goal.title,
                            modifier = Modifier.padding(16.dp)
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
    }
}