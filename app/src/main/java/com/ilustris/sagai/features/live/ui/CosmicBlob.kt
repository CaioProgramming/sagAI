package com.ilustris.sagai.features.live.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.live.presentation.LiveFocus
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class BlobMode {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
}

/**
 * The live screen's centerpiece: soft additive glow blobs that breathe, swell with the voice and
 * swirl while thinking, with the portrait of whoever is in focus inside, a rotating gradient ring
 * while waiting, and a radial wave that deforms with the loudness of what's playing.
 *
 * Everything that moves every frame ([level], the clock) is read in the draw phase, so a pulse
 * redraws without recomposing.
 */
@Composable
fun CosmicBlob(
    mode: BlobMode,
    palette: List<Color>,
    focus: LiveFocus?,
    focusColor: Color,
    /** The saga's own art, shown when the narrator is in focus. */
    narratorImage: String?,
    level: () -> Float,
    showRing: Boolean,
    showWave: Boolean,
    modifier: Modifier = Modifier,
    portraitSize: Dp = 168.dp,
    reduceMotion: Boolean = false,
) {
    val animationsActive = rememberLifecycleAnimationsActive()
    val clock = remember { mutableLongStateOf(0L) }
    LaunchedEffect(animationsActive) {
        if (!animationsActive) return@LaunchedEffect
        while (true) withFrameMillis { clock.longValue = it }
    }
    val speed = if (reduceMotion) 0.2f else 1f

    val baseScale by animateFloatAsState(
        targetValue =
            when (mode) {
                BlobMode.IDLE -> 0.78f
                BlobMode.LISTENING -> 0.92f
                BlobMode.THINKING -> 0.64f
                BlobMode.SPEAKING -> 0.95f
            },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "blobScale",
    )
    val swirl by animateFloatAsState(
        targetValue = if (mode == BlobMode.THINKING) 3.2f else 1f,
        animationSpec = tween(900),
        label = "blobSwirl",
    )
    val colors = (0 until BLOB_COUNT).map { i -> palette.getOrElse(i % palette.size.coerceAtLeast(1)) { focusColor } }
    val animatedColors = colors.mapIndexed { i, c -> animateColorAsState(c, tween(900), label = "blobColor$i") }

    // Smoothed values live across frames without triggering recomposition.
    val smoothed = remember { floatArrayOf(0f, 0f) }

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .blur(28.dp),
        ) {
            val t = clock.longValue * speed
            smoothed[0] += (level() - smoothed[0]) * 0.18f
            val energy = smoothed[0]
            val base = size.minDimension * 0.30f * baseScale
            val center = this.center
            animatedColors.forEachIndexed { i, colorState ->
                val color = colorState.value
                val angle = t * 0.0005f * swirl + i * 1.9f
                val orbit = size.minDimension * (0.07f + energy * 0.06f)
                val x = center.x + cos(angle) * orbit * (1f + 0.3f * sin(t * 0.0003f + i))
                val y = center.y + sin(angle * 1.13f) * orbit
                val radius = base + energy * size.minDimension * 0.16f + sin(t * 0.0012f + i * 2f) * base * 0.08f
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            0f to color.copy(alpha = 0.95f),
                            0.55f to color.copy(alpha = 0.4f),
                            1f to Color.Transparent,
                            center = Offset(x, y),
                            radius = radius,
                        ),
                    radius = radius,
                    center = Offset(x, y),
                    blendMode = BlendMode.Plus,
                )
            }
        }

        // Rotating gradient ring: "working on it" around the portrait.
        val ringAlpha by animateFloatAsState(if (showRing) 1f else 0f, tween(500), label = "ringAlpha")
        Canvas(Modifier.size(portraitSize + 10.dp)) {
            if (ringAlpha <= 0f) return@Canvas
            val angle = (clock.longValue * speed * 0.25f) % 360f
            rotate(angle) {
                drawCircle(
                    brush = Brush.sweepGradient(palette.ifEmpty { listOf(focusColor) } + palette.take(1).ifEmpty { listOf(focusColor) }),
                    radius = size.minDimension / 2f - 2.dp.toPx(),
                    style = Stroke(width = 2.5.dp.toPx()),
                    alpha = ringAlpha,
                )
            }
        }

        // Radial audio wave: only while a voice is actually playing.
        Canvas(Modifier.size(portraitSize + 64.dp)) {
            val target = if (showWave) level() else 0f
            smoothed[1] += (target - smoothed[1]) * 0.15f
            val energy = smoothed[1]
            if (energy < 0.01f) return@Canvas
            val t = clock.longValue * speed
            val baseRadius = portraitSize.toPx() / 2f + 8.dp.toPx()
            for (layer in 0 until 3) {
                val path = Path()
                for (i in 0..WAVE_POINTS) {
                    val a = (i.toFloat() / WAVE_POINTS) * 2f * PI.toFloat()
                    val noise =
                        sin(a * 3f + t * 0.004f + layer) * 0.5f +
                            sin(a * 5f - t * 0.006f + layer * 2f) * 0.3f +
                            sin(a * 9f + t * 0.009f) * 0.2f
                    val r = baseRadius + layer * 3.dp.toPx() + energy * (14 + layer * 6).dp.toPx() * (0.55f + 0.45f * noise)
                    val point = Offset(center.x + cos(a) * r, center.y + sin(a) * r)
                    if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                path.close()
                val alpha = min(1f, energy * 2.2f)
                // A wider faint pass under each stroke stands in for glow.
                drawPath(path, focusColor.copy(alpha = 0.18f * alpha), style = Stroke(width = 6.dp.toPx()))
                drawPath(
                    path,
                    color =
                        if (layer ==
                            0
                        ) {
                            Color.White.copy(alpha = 0.85f * alpha)
                        } else {
                            focusColor.copy(alpha = (if (layer == 1) 0.7f else 0.35f) * alpha)
                        },
                    style = Stroke(width = if (layer == 0) 1.4.dp.toPx() else 2.dp.toPx()),
                )
            }
        }

        // The portrait of whoever is in focus, crossfading when the speaker changes.
        AnimatedContent(
            targetState = focus,
            transitionSpec = {
                (fadeIn(tween(420)) + scaleIn(tween(520), initialScale = 0.7f)) togetherWith
                    (fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.85f))
            },
            contentKey = { it?.character?.id ?: if (it == null) -1 else -2 },
            label = "livePortrait",
        ) { target ->
            if (target == null) {
                Box(Modifier.size(portraitSize))
            } else {
                Portrait(
                    image = target.character?.image?.takeIf { it.isNotBlank() } ?: narratorImage,
                    description = target.character?.name,
                    size = portraitSize,
                    modifier =
                        Modifier.graphicsLayer {
                            val pulse = if (mode == BlobMode.SPEAKING) smoothed[1] * 0.06f else 0f
                            scaleX = 1f + pulse
                            scaleY = 1f + pulse
                        },
                )
            }
        }
    }
}

/** Character art (or the saga's, for the narrator; the app mascot as last resort) with a soft round edge. */
@Composable
private fun Portrait(
    image: String?,
    description: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val softEdge =
        Modifier
            .size(size)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush =
                        Brush.radialGradient(
                            0.78f to Color.Black,
                            1f to Color.Transparent,
                            radius = this.size.minDimension / 2f,
                        ),
                    blendMode = BlendMode.DstIn,
                )
            }
    if (!image.isNullOrBlank()) {
        AsyncImage(
            model = image,
            contentDescription = description,
            contentScale = ContentScale.Crop,
            modifier = modifier.then(softEdge),
        )
    } else {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.then(softEdge),
        )
    }
}

private const val BLOB_COUNT = 4
private const val WAVE_POINTS = 96
