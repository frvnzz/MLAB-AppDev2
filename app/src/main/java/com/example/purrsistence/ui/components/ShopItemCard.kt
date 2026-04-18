package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.state.ShopItem

@Composable
fun ShopItemCard(
    item: ShopItem,
    balance: Int,
    isOwned: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)

            Text("Price: ${item.price}")

            Button(
                onClick = onBuy,
                enabled = balance >= item.price && !isOwned
            ) {
                Text(
                    when {
                        isOwned -> "Owned"
                        balance < item.price -> "No Funds"
                        else -> "Adopt"
                    }
                )
            }
        }
    }
}