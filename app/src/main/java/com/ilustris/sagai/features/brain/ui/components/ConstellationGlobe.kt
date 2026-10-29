package com.ilustris.sagai.features.brain.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.themeBrushColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

private const val NODES = 34
private const val LINKS_PER_NODE = 2
private const val TILT = 0.35f
private const val SECONDS_PER_TURN = 26f
private const val MIN_FRAME_SECONDS = 1f / 30f
private const val POP_DELAY_MS = 140L

/** Nodes spread evenly over a unit sphere and the links between each node and its nearest neighbours. */
private object GlobeShape {
    val points: List<FloatArray> =
        List(NODES) { i ->
            val y = 1f - 2f * (i + 0.5f) / NODES
            val r = sqrt(1f - y * y)
            val theta = i * 2.399963f
            floatArrayOf(cos(theta) * r, y, sin(theta) * r)
        }

    val links: List<Pair<Int, Int>> =
        buildSet {
            for (a in 0 until NODES) {
                points.indices
                    .filter { it != a }
                    .sortedBy { b -> distance(points[a], points[b]) }
                    .take(LINKS_PER_NODE)
                    .forEach { b -> add(minOf(a, b) to maxOf(a, b)) }
            }
        }.toList()

    private fun distance(
        a: FloatArray,
        b: FloatArray,
    ) = hypot(hypot(a[0] - b[0], a[1] - b[1]), a[2] - b[2])
}

/** The theme gradient without its near-black backdrop tints, which would only dirty a small drawing. */
private fun globePalette(colors: List<Color>): List<Color> =
    colors.filter { it.luminance() > 0.03f }.let { lit ->
        when {
            lit.size >= 2 -> lit
            lit.size == 1 -> listOf(lit[0], lerp(lit[0], Color.White, 0.45f))
            else -> listOf(Color(0xFF6EA8FF), Color(0xFFFF8FD0))
        }
    }

private fun List<Color>.at(t: Float): Color {
    val scaled = t.coerceIn(0f, 1f) * (size - 1)
    val i = floor(scaled).toInt().coerceAtMost(size - 2)
    return lerp(this[i], this[i + 1], scaled - i)
}

@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/** The turning angle in radians; [speedBoost] (0 = none) lets a tap spin it up for a moment. */
@Composable
private fun rememberGlobeAngle(
    reduceMotion: Boolean,
    speedBoost: () -> Float = { 0f },
): Float {
    val active = rememberLifecycleAnimationsActive()
    var angle by remember { mutableFloatStateOf(0.6f) }
    LaunchedEffect(active, reduceMotion) {
        if (!active || reduceMotion) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last == 0L) {
                    last = now
                } else {
                    val dt = (now - last) / 1_000_000_000f
                    if (dt >= MIN_FRAME_SECONDS) {
                        val speed = 2f * PI.toFloat() / SECONDS_PER_TURN * (1f + 9f * speedBoost())
                        angle = (angle + dt * speed) % (2f * PI.toFloat())
                        last = now
                    }
                }
            }
        }
    }
    return angle
}

/**
 * The globe itself: nodes and links filled with the [palette] gradient left to right, over a faint dark
 * disc that keeps it readable on light art. [ring] (0..1) draws a pulse leaving the globe after a tap.
 */
private fun DrawScope.drawGlobe(
    angle: Float,
    palette: List<Color>,
    glow: Color,
    ring: Float = 0f,
) {
    val radius = size.minDimension * 0.4f
    val center = Offset(size.width / 2f, size.height / 2f)
    drawCircle(Color.Black.copy(alpha = 0.3f), radius * 1.18f, center)
    drawCircle(palette.at(0.5f).copy(alpha = 0.4f), radius * 1.18f, center, style = Stroke(width = 0.8.dp.toPx()))
    if (ring > 0f) {
        drawCircle(
            color = glow.copy(alpha = 0.6f * (1f - ring)),
            radius = radius * (1.2f + 1.5f * ring),
            center = center,
            style = Stroke(width = (1.6f * (1f - ring) + 0.4f).dp.toPx()),
        )
    }
    val ca = cos(angle)
    val sa = sin(angle)
    val ct = cos(TILT)
    val st = sin(TILT)
    // x, y on screen and depth (-1 back .. 1 front)
    val projected =
        GlobeShape.points.map { p ->
            val x = p[0] * ca + p[2] * sa
            val z = -p[0] * sa + p[2] * ca
            val y = p[1] * ct - z * st
            val depth = p[1] * st + z * ct
            Offset(center.x + x * radius, center.y + y * radius) to depth
        }
    GlobeShape.links.forEach { (a, b) ->
        val depth = (projected[a].second + projected[b].second) / 2f
        val midX = (projected[a].first.x + projected[b].first.x) / 2f / size.width
        drawLine(
            color = palette.at(midX).copy(alpha = 0.25f + 0.55f * (depth + 1f) / 2f),
            start = projected[a].first,
            end = projected[b].first,
            strokeWidth = 1.dp.toPx() * 0.9f,
        )
    }
    projected.forEachIndexed { i, (pos, depth) ->
        val front = (depth + 1f) / 2f
        val tint = palette.at(pos.x / size.width)
        val nodeRadius = (if (i % 7 == 0) 1.7.dp else 1.15.dp).toPx() * (0.8f + 0.3f * front)
        drawCircle(tint.copy(alpha = 0.3f * front), nodeRadius * 2.8f, pos)
        drawCircle(lerp(tint, Color.White, 0.35f).copy(alpha = 0.45f + 0.55f * front), nodeRadius, pos)
    }
    drawCircle(glow.copy(alpha = 0.95f), 2.2.dp.toPx(), center)
}

/**
 * Small, soft, slowly turning globe that opens the saga constellation. It is a hint, not a button
 * shape: no frame and no label, so it never competes with the main action under the title. The touch
 * target is larger than the drawing.
 *
 * It is filled with the theme gradient. A tap makes it pop: it swells and settles with a spring, a ring
 * leaves it and it spins up for a moment, then the constellation opens. With reduced motion the tap
 * opens straight away.
 */
@Composable
fun ConstellationGlobe(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glow: Color = Color.White,
    colors: List<Color> = themeBrushColors(),
) {
    val reduceMotion = rememberReduceMotion()
    val palette = remember(colors) { globePalette(colors) }
    val pop = remember { Animatable(0f) }
    val ring = remember { Animatable(0f) }
    val boost = remember { Animatable(0f) }
    val angle = rememberGlobeAngle(reduceMotion) { boost.value }
    val scope = rememberCoroutineScope()

    val description = stringResource(R.string.saga_brain_title)
    Box(
        modifier =
            modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClickLabel = description, role = Role.Button) {
                    if (reduceMotion) {
                        onClick()
                    } else {
                        scope.launch {
                            launch {
                                pop.snapTo(0f)
                                pop.animateTo(1f, tween(110, easing = FastOutSlowInEasing))
                                pop.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                            }
                            launch {
                                ring.snapTo(0f)
                                ring.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
                            }
                            launch {
                                boost.animateTo(1f, tween(120))
                                boost.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
                            }
                            delay(POP_DELAY_MS)
                            onClick()
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            Modifier
                .size(40.dp)
                .graphicsLayer {
                    val scale = 1f + 0.3f * pop.value
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            drawGlobe(angle, palette, glow, ring.value)
        }
    }
}

/** The same globe as a plain icon: it only turns, the surrounding control takes the touch. */
@Composable
fun ConstellationGlobeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    glow: Color = Color.White,
    colors: List<Color> = themeBrushColors(),
) {
    val palette = remember(colors) { globePalette(colors) }
    val angle = rememberGlobeAngle(rememberReduceMotion())
    Canvas(modifier.size(size)) {
        drawGlobe(angle, palette, glow)
    }
}
