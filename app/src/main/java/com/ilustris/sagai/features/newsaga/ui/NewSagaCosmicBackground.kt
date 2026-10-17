package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder

private val Nebula = Color(0xFF5434AA)
private val DeepBlue = Color(0xFF1C3E96)

/**
 * One background for the whole creation flow, so no step breaks it: the screen's own background,
 * a violet and a blue nebula, the app's own [StarryTextPlaceholder] stars, and a glow at the top in
 * [accent] — the genre of the story in focus — that eases between genres instead of snapping.
 * Steps and loaders draw on top of it and never paint a background of their own.
 */
@Composable
internal fun NewSagaCosmicBackground(
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val glow by animateColorAsState(accent, tween(800), label = "cosmic_glow")

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    listOf(Nebula.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(size.width * 0.9f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(DeepBlue.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(0f, size.height),
                    radius = size.width,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(glow.copy(alpha = 0.32f), Color.Transparent),
                    center = Offset(size.width * 0.5f, -size.height * 0.05f),
                    radius = size.width * 0.8f,
                ),
            )
        }
        StarryTextPlaceholder(
            modifier = Modifier.fillMaxSize(),
            starColor = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}
