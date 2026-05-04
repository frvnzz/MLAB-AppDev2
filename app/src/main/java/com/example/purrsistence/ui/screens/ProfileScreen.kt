package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    setTopBar: (TopBarState) -> Unit
) {
    // set TopBar content (header only)
    setTopBar(
        TopBarState(
            title = "Profile"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // PLACEHOLDER - Replace with actual Profile UI
        Text(
            text = "Your Profile is here :)",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}