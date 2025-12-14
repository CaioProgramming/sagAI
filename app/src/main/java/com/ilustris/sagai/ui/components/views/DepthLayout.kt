package com.ilustris.sagai.ui.components.views

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun DepthLayout(
    originalImage: Bitmap,
    segmentedImage: Bitmap,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    backgroundImageModifier: Modifier = imageModifier,
    foregroundImageModifier: Modifier = imageModifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        AnimatedContent(originalImage, transitionSpec = {
            fadeIn(tween(500, easing = EaseIn)) togetherWith
                fadeOut(
                    tween(
                        250,
                        easing = FastOutSlowInEasing,
                    ),
                )
        }) {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = backgroundImageModifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        content()
        AnimatedContent(segmentedImage, transitionSpec = {
            fadeIn(tween(500, easing = EaseIn)) togetherWith
                fadeOut(
                    tween(
                        250,
                        easing = FastOutSlowInEasing,
                    ),
                )
        }) {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = foregroundImageModifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
