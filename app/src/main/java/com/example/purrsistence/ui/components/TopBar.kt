package com.example.purrsistence.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            .statusBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT SLOT - Navigation Icon
        Box(
            modifier = Modifier
                .size(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (navigationIcon != null) {
                navigationIcon()
            }
        }

        // CENTER SLOT - Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
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