package com.ilustris.sagai.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.ui.theme.components.meshGradient

/**
 * A "Multiverse" fluid gradient animation that transitions between multiple colors.
 * It uses heavy blur (150dp) to diffuse animated white waves that are dynamically colored
 * using a gradient fill modifier based on the current palette.
 *
 * @param colors The list of base colors to cycle through.
 * @param modifier The modifier to be applied to the container.
 */
@Composable
fun FluidGradient(
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val meshPoints =
        colors.map {
            listOf(
                Offset(0f, 0f) to it,
                Offset(.5f, .5f) to it,
                Offset(1f, 1f) to it,
            )
        }
    Box(
        Modifier
            .fillMaxSize()
            .meshGradient(
                points = meshPoints,
                resolutionX = 32,
            ).reactiveShimmer(true),
    )
}
