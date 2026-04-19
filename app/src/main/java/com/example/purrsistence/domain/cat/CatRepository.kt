package com.example.purrsistence.domain.cat

import com.example.purrsistence.R
import com.example.purrsistence.ui.state.ShopItem

object CatRepository {
    // List of all cats the user can buy in the Shop
    val cats = listOf(
        ShopItem("cat_1", "Orange Cat", 2, R.drawable.cat_orange),
        ShopItem("cat_2", "Black Cat", 13, R.drawable.cat_black),
        ShopItem("cat_3", "White Cat", 20, R.drawable.cat_white),
        ShopItem("cat_4", "Brown Cat", 100, R.drawable.cat_brown),
        ShopItem("cat_5", "Gray Cat", 150, R.drawable.cat_gray),
        ShopItem("cat_6", "Spotted Cat", 200, R.drawable.cat_spotted),
        ShopItem("cat_7", "Siamese Cat", 300, R.drawable.cat_siamese),
        ShopItem("cat_8", "Lucky Cat", 500, R.drawable.cat_lucky)
    )

    private val catMap = cats.associateBy { it.id }
    fun getCatById(id: String): ShopItem? = catMap[id]
}