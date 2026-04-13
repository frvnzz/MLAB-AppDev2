package com.example.purrsistence.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.purrsistence.focus.DeepFocusAccessibilityState
import kotlin.math.roundToInt

@Composable
fun AddGoalScreen(
    onSave: (
        String,
        String,
        Int,
        Boolean
    ) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Weekly") }
    var hours by remember { mutableStateOf("") }
    var deepFocus by remember { mutableStateOf(false) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Add Goal", style = MaterialTheme.typography.titleLarge)

        // title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Goal Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // type (Daily / Weekly / Monthly)
        Text("Goal Type")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Daily", "Weekly", "Monthly").forEach { option ->
                FilterChip(
                    selected = type == option,
                    onClick = { type = option },
                    label = { Text(option) }
                )
            }
        }

        // Duration (in hours, but saved in minutes)
        OutlinedTextField(
            value = hours,
            onValueChange = { hours = it },
            label = { Text("Target Duration (hours)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        // Deep Focus
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Deep Focus")
            Switch(
                checked = deepFocus,
                onCheckedChange = {
                    deepFocus = it
                    if (it && !DeepFocusAccessibilityState.isServiceEnabled(context)) {
                        showAccessibilityDialog = true
                    }
                }
            )
        }

        if (showAccessibilityDialog) {
            AlertDialog(
                onDismissRequest = { showAccessibilityDialog = false },
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
                    Button(
                        onClick = {
                            showAccessibilityDialog = false
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
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAccessibilityDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Save Button
        Button(
            onClick = {
                val normalized = hours.trim().replace(",", ".")
                val hoursFloat = normalized.toFloatOrNull() ?: 0f
                val hoursRounded = (hoursFloat * 10).roundToInt() / 10f
                val minutes = (hoursRounded * 60).roundToInt()

                onSave(
                    title,
                    type,
                    minutes,
                    deepFocus
                )
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goal")
        }
    }
}