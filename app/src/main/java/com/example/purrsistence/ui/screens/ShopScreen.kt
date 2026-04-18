package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.R
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

    val shopItems = remember { listOf(
        ShopItem("cat_1", "Orange Cat", 2, R.drawable.cat_orange),
        ShopItem("cat_2", "Black Cat", 13, R.drawable.cat_black),
        ShopItem("cat_3", "White Cat", 20, R.drawable.cat_white),
        ShopItem("cat_4", "Brown Cat", 100, R.drawable.cat_brown),
        ShopItem("cat_5", "Gray Cat", 150, R.drawable.cat_gray),
        ShopItem("cat_6", "Spotted Cat", 200, R.drawable.cat_spotted),
        ShopItem("cat_7", "Siamese Cat", 300, R.drawable.cat_siamese),
        ShopItem("cat_8", "Lucky Cat", 500, R.drawable.cat_lucky)
    ) }

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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = shopItems,
                key = { it.id }
            ) { item ->

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