package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp

private const val GLOW_STEPS = 6
private const val GLOW_STEP_DP = 4
private const val GLOW_ALPHA = 0.14f
private const val SHEEN_MS = 7000

private fun Outline.toPath(): Path =
    when (this) {
        is Outline.Rectangle -> Path().apply { addRect(rect) }
        is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
        is Outline.Generic -> path
    }

/**
 * The light of a glass surface, laid over whatever material the surface already is — dark glass,
 * leather, a terminal plate, paper. It draws no fill of its own, only:
 *  - a soft reflection hugging the bottom edge, blurred inward, with a faint wash rising from it;
 *  - a specular rim that is bright at the top-left corner and takes [tint] at the bottom-right.
 * [strength] (0..1) scales only the tinted light, so a disabled control keeps its rim but goes flat.
 * It is read while drawing, so animating it never recomposes. Everything is clipped to [shape].
 */
@Composable
fun Modifier.glassLight(
    shape: Shape,
    tint: Color,
    strength: () -> Float = { 1f },
    light: Color = MaterialTheme.colorScheme.onBackground,
): Modifier =
    drawWithContent {
        val s = strength().coerceIn(0f, 1f)
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = outline.toPath()
        clipPath(path) {
            // A faint wash rising from the bottom edge, under the content.
            drawOutline(
                outline,
                Brush.verticalGradient(0.6f to Color.Transparent, 1f to tint.copy(alpha = 0.3f * s)),
            )
        }
        drawContent()
        clipPath(path) {
            // The reflection itself: stacked strokes that widen while they overlap, so the light bleeds
            // inward and fades instead of ending on a hard line. Each stroke is centered on the edge
            // and the clip drops its outer half, so only the inner half remains.
            val glow = Brush.verticalGradient(0.5f to Color.Transparent, 1f to tint.copy(alpha = GLOW_ALPHA * s))
            for (step in 1..GLOW_STEPS) {
                drawOutline(outline, glow, style = Stroke(width = (step * GLOW_STEP_DP).dp.toPx()))
            }
            // The specular rim, one dp visible.
            drawOutline(
                outline,
                Brush.linearGradient(
                    colorStops =
                        arrayOf(
                            0f to light.copy(alpha = 0.55f),
                            0.22f to light.copy(alpha = 0.10f),
                            0.6f to light.copy(alpha = 0.04f),
                            1f to tint.copy(alpha = 0.6f * s).compositeOver(light.copy(alpha = 0.12f)),
                        ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }

/**
 * A band of light that slowly crosses the surface, so it reads as glossy. Drawn over the content,
 * clipped to [shape], blended to brighten, and read while drawing so it never recomposes.
 */
@Composable
fun Modifier.glassSheen(
    shape: Shape,
    alpha: Float,
    tint: Color = Color.White,
): Modifier {
    val progress =
        rememberInfiniteTransition(label = "glass_sheen").animateFloat(
            initialValue = -0.3f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(tween(SHEEN_MS, easing = LinearEasing), RepeatMode.Restart),
            label = "glass_sheen_progress",
        )
    return drawWithContent {
        drawContent()
        val shift = (progress.value - 0.5f) * size.width * 2f
        val outline = shape.createOutline(size, layoutDirection, this)
        clipPath(outline.toPath()) {
            drawRect(
                brush =
                    Brush.linearGradient(
                        colorStops =
                            arrayOf(
                                0.30f to Color.Transparent,
                                0.40f to Color.White.copy(alpha = 0.06f),
                                0.47f to Color.White.copy(alpha = 0.34f),
                                0.51f to tint,
                                0.57f to Color.White.copy(alpha = 0.10f),
                                0.66f to Color.Transparent,
                            ),
                        start = Offset(shift, 0f),
                        end = Offset(shift + size.width, size.height * 0.6f),
                    ),
                alpha = alpha,
                blendMode = BlendMode.Screen,
            )
        }
    }
}
