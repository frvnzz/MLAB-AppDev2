package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.components.ShopItemCard
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.state.ShopItem
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun ShopScreen(
    userViewModel: UserViewModel
) {
    // Get current user data (balance, collected cats)
    val user by userViewModel.user.collectAsState()
    val balance = user?.balance ?: 0
    val collectedCats = user?.collectedCatsIds ?: emptyList()

    val shopItems = listOf(
        ShopItem("cat_1", "Orange Cat", 2),
        ShopItem("cat_2", "Black Cat", 75),
        ShopItem("cat_3", "White Cat", 100),
        ShopItem("cat_4", "Brown Cat", 120),
        ShopItem("cat_5", "Gray Cat", 150),
        ShopItem("cat_6", "Spotted Cat", 200),
        ShopItem("cat_7", "Siamese Cat", 300),
        ShopItem("cat_8", "Lucky Cat", 500)
    )

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
        // TODO: LazyColumn is a bit laggy on emulator during scrolling, maybe try to fix that?
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(shopItems) { item ->

                val isOwned = item.id in collectedCats

                ShopItemCard(
                    item = item,
                    balance = balance,
                    isOwned = isOwned,
                    onBuy = {
                        userViewModel.buyCat(item.id, item.price)
                    }
                )
            }
        }
    }
}