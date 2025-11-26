package com.ilustris.sagai.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

val LocalBlurState = compositionLocalOf { { _: Boolean -> } }

@Composable
fun BlurProvider(content: @Composable () -> Unit) {
    var isBlurred by remember { mutableStateOf(false) }
    val blurRadius by animateDpAsState(
        targetValue = if (isBlurred) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 500),
        label = "blurAnimation",
    )

    CompositionLocalProvider(LocalBlurState provides { isBlurred = it }) {
        Box(
            modifier =
                Modifier.blur(
                    radius = blurRadius,
                ),
        ) {
            content()
        }
    }
}
