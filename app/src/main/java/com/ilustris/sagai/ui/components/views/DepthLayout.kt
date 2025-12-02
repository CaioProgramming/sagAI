package com.ilustris.sagai.ui.components.views

import android.graphics.Bitmap
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
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Image(
            bitmap = originalImage.asImageBitmap(),
            contentDescription = null,
            modifier = backgroundImageModifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
        Image(
            bitmap = segmentedImage.asImageBitmap(),
            contentDescription = null,
            modifier = foregroundImageModifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}