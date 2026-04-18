package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun ShopScreen(
    userViewModel: UserViewModel
) {
    val balance by userViewModel.userBalance.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        TopBar(
            title = "Cat Shop",
            actions = {
                CurrencyBadge(balance = balance)
            }
        )

        // Placeholder (replace with grid later)
        Text(
            text = "Buy cats with your rewards 🐱",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}