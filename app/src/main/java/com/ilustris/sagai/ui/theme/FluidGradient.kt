package com.ilustris.sagai.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikepenz.hypnoticcanvas.shaderBackground
import com.mikepenz.hypnoticcanvas.shaders.MeshGradient

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
    Box(
        Modifier
            .fillMaxSize()
            .shaderBackground(
                MeshGradient(
                    colors.toTypedArray(),
                    speed = .3f,
                ),
            ),
    )
}
