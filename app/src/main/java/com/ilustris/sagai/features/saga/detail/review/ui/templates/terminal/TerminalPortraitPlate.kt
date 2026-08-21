package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/** A single framed portrait tile — the terminal's basic unit for a character or image plate. */
@Composable
fun TerminalPortraitPlate(
    imageUrl: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().plateFrame(accentColor)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
