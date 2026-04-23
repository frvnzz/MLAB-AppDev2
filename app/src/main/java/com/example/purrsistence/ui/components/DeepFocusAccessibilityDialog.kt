package com.example.purrsistence.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeepFocusAccessibilityDialog(
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