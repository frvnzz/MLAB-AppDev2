package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.ui.components.animation.SpriteAnimation

@Composable
fun CatImage(
    catId: String,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false
) {
    val cat = CatList.getCatById(catId)
    if (cat != null) {
        val finalModifier = modifier
            .size(100.dp)
            .graphicsLayer {
                if (isMirrored) {
                    scaleX = -1f
                }
            }

        if (cat.animationData != null) {
            SpriteAnimation(
                spriteSheetRes = cat.imageRes,
                data = cat.animationData,
                modifier = finalModifier,
                contentDescription = cat.name
            )
        } else {
            Image(
                painter = painterResource(cat.imageRes),
                contentDescription = cat.name,
                modifier = finalModifier
            )
        }
    }
}