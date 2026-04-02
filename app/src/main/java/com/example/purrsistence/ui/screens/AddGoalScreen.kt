package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddGoalScreen(
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    val title = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Add Goal", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Goal Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSave(title.value)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goal")
        }
    }
}