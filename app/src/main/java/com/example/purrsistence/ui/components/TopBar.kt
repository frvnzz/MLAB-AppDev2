package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        // RIGHT SLOT (fixed size to avoid different layouts)
        Box(
            modifier = Modifier
                .height(IntrinsicSize.Min),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (actions != null) {
                actions()
            } else {
                // Placeholder keeps layout identical when empty
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}