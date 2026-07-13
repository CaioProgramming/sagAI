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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BlurIntensity {
    None,
    Subtle,
    Medium,
    Strong,
    ;

    val radius: Dp
        get() =
            when (this) {
                None -> 0.dp
                Subtle -> 6.dp
                Medium -> 12.dp
                Strong -> 16.dp
            }
}

val LocalBlurState = compositionLocalOf { { _: BlurIntensity -> } }

internal val LocalBlurRadius = compositionLocalOf { 0.dp }

@Composable
fun BlurProvider(content: @Composable () -> Unit) {
    var blurIntensity by remember { mutableStateOf(BlurIntensity.None) }
    val blurRadius by animateDpAsState(
        targetValue = blurIntensity.radius,
        animationSpec = tween(durationMillis = 500),
        label = "blurAnimation",
    )

    CompositionLocalProvider(
        LocalBlurState provides { blurIntensity = it },
        LocalBlurRadius provides blurRadius,
    ) {
        content()
    }
}

/**
 * Applies the current [BlurIntensity] only to this subtree (e.g. nav content),
 * leaving siblings such as anchored panels sharp.
 */
@Composable
fun BlurTarget(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val blurRadius = LocalBlurRadius.current
    Box(
        modifier =
            Modifier
                .then(modifier)
                .blur(radius = blurRadius),
    ) {
        content()
    }
}
