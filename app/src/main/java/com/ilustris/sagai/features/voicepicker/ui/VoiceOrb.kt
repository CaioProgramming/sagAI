package com.ilustris.sagai.features.voicepicker.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A glass sphere colored by a voice's palette: a dark body lit from inside by drifting color
 * blobs, a bright fresnel rim and a specular glint on top. The orb is the picker's selector, so
 * only the [selected] one lives: it drifts and swells with [level]. The others freeze where they
 * are, drained of color and dimmed, which reads as "not this one" without any extra chrome.
 *
 * [level] and the clock are read in the draw phase, so the pulse redraws without recomposing.
 */
@Composable
fun VoiceOrb(
    palette: List<Color>,
    selected: Boolean,
    level: () -> Float,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val animationsActive = rememberLifecycleAnimationsActive()
    val clock = remember { mutableLongStateOf(0L) }
    // The clock stops while unselected, so a deselected orb holds its last pose instead of drifting.
    LaunchedEffect(animationsActive, selected) {
        if (!animationsActive || !selected) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameMillis { now ->
                if (last != 0L) clock.longValue += now - last
                last = now
            }
        }
    }
    val life by animateFloatAsState(if (selected) 1f else 0f, tween(450), label = "orbLife")
    val smoothed = remember { floatArrayOf(0f) }
    val colors = palette.ifEmpty { listOf(Color(0xFF8A7CFF)) }

    Canvas(
        modifier
            .size(size)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        val t = clock.longValue.toFloat()
        smoothed[0] += ((if (selected) level() else 0f) - smoothed[0]) * 0.18f
        val energy = smoothed[0]
        // The sphere leaves a margin inside the canvas for the voice ring, which would otherwise be clipped.
        val radius = this.size.minDimension / 2f * SPHERE_FRACTION
        val center = this.center
        val alpha = 0.55f + 0.45f * life
        val saturation = 0.1f + 0.9f * life

        fun tone(color: Color): Color {
            val gray = color.luminance().coerceIn(0.25f, 0.8f)
            return lerp(Color(gray, gray, gray), color, saturation)
        }

        val sphere = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, radius)) }
        clipPath(sphere) {
            // Body: near-black glass, slightly lifted toward the light.
            drawCircle(
                brush =
                    Brush.radialGradient(
                        0f to Color(0xFF15131F).copy(alpha = alpha),
                        1f to Color(0xFF050409).copy(alpha = alpha),
                        center = Offset(center.x - radius * 0.25f, center.y - radius * 0.3f),
                        radius = radius * 1.5f,
                    ),
                radius = radius,
                center = center,
            )
            // Inner light: additive blobs that orbit and swell with the voice.
            colors.take(BLOB_COUNT).forEachIndexed { i, raw ->
                val angle = t * 0.00045f + i * 2.1f
                val orbit = radius * (0.28f + energy * 0.12f)
                val x = center.x + cos(angle) * orbit
                val y = center.y + sin(angle * 1.17f) * orbit * 0.9f
                val r = radius * (0.62f + energy * 0.3f) + sin(t * 0.0011f + i * 2f) * radius * 0.04f * life
                val color = tone(raw)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            0f to color.copy(alpha = 0.9f * alpha),
                            0.6f to color.copy(alpha = 0.32f * alpha),
                            1f to Color.Transparent,
                            center = Offset(x, y),
                            radius = r,
                        ),
                    radius = r,
                    center = Offset(x, y),
                    blendMode = BlendMode.Plus,
                )
            }
            // Fresnel: glass is brightest at the silhouette.
            drawCircle(
                brush =
                    Brush.radialGradient(
                        0.72f to Color.Transparent,
                        0.93f to Color.White.copy(alpha = 0.16f * alpha),
                        1f to Color.White.copy(alpha = 0.5f * alpha),
                        center = center,
                        radius = radius,
                    ),
                radius = radius,
                center = center,
            )
        }

        // Specular glint: a short arc, built from stacked strokes (wide and faint to thin and
        // bright) so it reads as a soft reflection instead of a flat line.
        val glintRadius = radius * 0.86f
        rotate(-128f, center) {
            GLINT_PASSES.forEach { (width, strength) ->
                drawArc(
                    brush =
                        Brush.sweepGradient(
                            0f to Color.White.copy(alpha = strength * alpha),
                            0.14f to Color.Transparent,
                            center = center,
                        ),
                    startAngle = 0f,
                    sweepAngle = 50f,
                    useCenter = false,
                    topLeft = Offset(center.x - glintRadius, center.y - glintRadius),
                    size = Size(glintRadius * 2, glintRadius * 2),
                    style = Stroke(width = radius * width, cap = StrokeCap.Round),
                )
            }
        }
        drawCircle(
            brush =
                Brush.radialGradient(
                    0f to Color.White.copy(alpha = 0.28f * alpha),
                    1f to Color.Transparent,
                    center = Offset(center.x - radius * 0.4f, center.y - radius * 0.46f),
                    radius = radius * 0.3f,
                ),
            radius = radius * 0.3f,
            center = Offset(center.x - radius * 0.4f, center.y - radius * 0.46f),
        )

        // Voice ring: only while there is sound.
        if (energy > 0.01f) {
            val base = radius * 1.04f
            for (layer in 0 until 3) {
                val path = Path()
                for (i in 0..WAVE_POINTS) {
                    val a = (i.toFloat() / WAVE_POINTS) * 2f * PI.toFloat()
                    val noise =
                        sin(a * 3f + t * 0.004f + layer) * 0.5f +
                            sin(a * 5f - t * 0.006f + layer * 2f) * 0.3f +
                            sin(a * 9f + t * 0.009f) * 0.2f
                    val r = base + layer * radius * 0.03f + energy * radius * (0.07f + layer * 0.035f) * (0.55f + 0.45f * noise)
                    val point = Offset(center.x + cos(a) * r, center.y + sin(a) * r)
                    if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                path.close()
                val ring = colors[layer % colors.size]
                val strength = (energy * 2.2f).coerceAtMost(1f)
                drawPath(
                    path,
                    color = if (layer == 0) Color.White.copy(alpha = 0.8f * strength) else ring.copy(alpha = (if (layer == 1) 0.7f else 0.35f) * strength),
                    style = Stroke(width = if (layer == 0) 1.4f else 2f),
                )
            }
        }
    }
}

/** Stroke width (fraction of the radius) and opacity of each glint pass. */
private val GLINT_PASSES = listOf(0.18f to 0.06f, 0.11f to 0.1f, 0.06f to 0.2f, 0.03f to 0.55f, 0.012f to 0.9f)
private const val SPHERE_FRACTION = 0.8f
private const val BLOB_COUNT = 4
private const val WAVE_POINTS = 96
