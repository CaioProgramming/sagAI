package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.sin

private const val TWO_PI = 6.2831855f
private const val LEVEL_EASE_MS = 250
private const val DRIFT_MS = 14_000

/** Share of the screen height the glow reaches in silence / at the narration's loudest moment. */
private const val MIN_HEIGHT = 0.16f
private const val MAX_HEIGHT = 0.38f
private const val MIN_ALPHA = 0.30f
private const val MAX_ALPHA = 0.70f
private const val DRIFT_RANGE = 0.12f
private const val WIDTH_STRETCH = 2.2f

/**
 * A soft aurora rising from the bottom edge that swells with the narration: louder moments make it
 * taller and brighter, silences and pauses let it settle to a low ambient glow, and each blob
 * drifts sideways slowly so it never sits still. Everything animated here — the eased [level] and
 * the drift — is read only inside the draw lambda, so it costs draw work per frame but never
 * recomposes anything.
 */
@Composable
fun AudiobookAurora(
    level: Float,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val breath =
        animateFloatAsState(
            targetValue = level,
            animationSpec = tween(LEVEL_EASE_MS, easing = LinearEasing),
            label = "auroraLevel",
        )
    val drift =
        rememberInfiniteTransition(label = "auroraDrift").animateFloat(
            initialValue = 0f,
            targetValue = TWO_PI,
            animationSpec = infiniteRepeatable(tween(DRIFT_MS, easing = LinearEasing)),
            label = "auroraPhase",
        )

    Box(
        modifier =
            modifier.drawBehind {
                if (colors.isEmpty()) return@drawBehind
                val voice = breath.value
                val phase = drift.value
                val regionHeight = size.height * (MIN_HEIGHT + (MAX_HEIGHT - MIN_HEIGHT) * voice)
                val alpha = MIN_ALPHA + (MAX_ALPHA - MIN_ALPHA) * voice
                colors.forEachIndexed { index, color ->
                    val share = (index + 0.5f) / colors.size
                    val centerX = size.width * share + sin(phase + index * 2.1f) * size.width * DRIFT_RANGE
                    // Whole multiples of the phase so the loop restart is seamless.
                    val radius = regionHeight * (0.85f + 0.2f * sin(phase * 2f + index * 1.3f))
                    val center = Offset(centerX, size.height)
                    withTransform({ scale(scaleX = WIDTH_STRETCH, scaleY = 1f, pivot = center) }) {
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors = listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.4f), Color.Transparent),
                                    center = center,
                                    radius = radius,
                                ),
                            radius = radius,
                            center = center,
                        )
                    }
                }
            },
    )
}
