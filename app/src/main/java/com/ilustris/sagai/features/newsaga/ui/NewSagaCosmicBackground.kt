package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import kotlin.random.Random

private val Nebula = Color(0xFF5434AA)
private val DeepBlue = Color(0xFF1C3E96)
private const val STAR_COUNT = 70
private const val TWINKLE_MS = 6000

private class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val layer: Int,
)

/**
 * One background for the whole creation flow, so no step breaks it: the screen's own background,
 * a violet and a blue nebula, twinkling stars, and a glow at the top in [accent] — the genre of the
 * story in focus — that eases between genres instead of snapping. Steps and loaders draw on top of
 * it and never paint a background of their own.
 */
@Composable
internal fun NewSagaCosmicBackground(
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val stars =
        remember {
            val random = Random(7)
            List(STAR_COUNT) {
                Star(
                    x = random.nextFloat(),
                    y = random.nextFloat(),
                    radius = 0.6f + random.nextFloat() * 1.2f,
                    layer = it % 2,
                )
            }
        }
    val animationsActive = rememberLifecycleAnimationsActive()
    val transition = rememberInfiniteTransition(label = "cosmic_twinkle")
    val twinkle by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(TWINKLE_MS, easing = LinearEasing), RepeatMode.Reverse),
        label = "cosmic_twinkle_a",
    )
    val glow by animateColorAsState(accent, tween(800), label = "cosmic_glow")
    val starColor = MaterialTheme.colorScheme.onBackground

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
            val a = if (animationsActive) twinkle else 0.6f
            stars.forEach { star ->
                val alpha = if (star.layer == 0) a else 1.3f - a
                drawCircle(
                    color = starColor.copy(alpha = alpha.coerceIn(0.2f, 0.95f) * 0.8f),
                    radius = star.radius * density,
                    center = Offset(star.x * size.width, star.y * size.height),
                )
            }
        }
        content()
    }
}
