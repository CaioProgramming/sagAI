package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val MAX_CONCURRENT_MARKS = 3

/** Ambient loop, independent of scroll — matches [ShinobiInkBlooms]'s pacing rationale. */
private const val SPAWN_INTERVAL_MS = 2400L

/** How long the char spreads out from the initial ember before it settles. */
private const val SPREAD_DURATION_MS = 1100

/** How long a fully-charred mark lingers before it starts fading. */
private const val HOLD_DURATION_MS = 3000

/** How long the fade-out takes. */
private const val DISSOLVE_DURATION_MS = 1400

/** Ember glow timing — rise, brief hold at peak, then a slow lingering fade (not a quick flash). */
private const val EMBER_RISE_MS = 300
private const val EMBER_HOLD_MS = 350
private const val EMBER_FADE_MS = 1300

/** Points around the blob's outline — more = rounder/less jagged, fewer = coarser. */
private const val BLOB_POINTS = 9

/** How much each outline point's radius can deviate from the mean — the "not a perfect circle" wobble. */
private const val BLOB_IRREGULARITY = 0.35f

private val CHAR_COLOR = Color(0xFF1C120A)
private val SINGE_COLOR = Color(0xFF6B4226)
private val EMBER_COLOR = Color(0xFFFF8A50)

private data class BurnMark(
    val id: Long,
    val xFraction: Float,
    val yFraction: Float,
    val sizeDp: Dp,
)

/**
 * Cowboy-only: small scorch marks that char into the page at random spots, like a cigarette burn
 * or an ember spark landing on paper — a restrained, localized effect, not
 * [com.ilustris.sagai.ui.animations.cowboyBurn]'s full-screen fireplace/heat-haze VFX (kept for
 * the real chat, not revived here). Same ambient self-running loop as [ShinobiInkBlooms]: not
 * tied to scroll, so it keeps breathing while the reader holds still.
 *
 * Each mark is an irregular blob (a smooth curve through [BLOB_POINTS] randomized-radius points,
 * not a perfect [androidx.compose.ui.graphics.drawscope.DrawScope.drawCircle]) so it reads as an
 * organic stain rather than a geometric ring, plus a warm ember glow underneath that actually
 * rises and lingers instead of flashing for a couple hundred milliseconds and vanishing.
 */
@Composable
fun CowboyBurnMarks(modifier: Modifier = Modifier) {
    if (LocalSagaGenre.current != Genre.COWBOY) return

    var marks by remember { mutableStateOf(emptyList<BurnMark>()) }
    var nextId by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(SPAWN_INTERVAL_MS)
            if (marks.size < MAX_CONCURRENT_MARKS) {
                marks =
                    marks +
                        BurnMark(
                            id = nextId++.toLong(),
                            xFraction = 0.1f + Random.nextFloat() * 0.8f,
                            yFraction = 0.1f + Random.nextFloat() * 0.8f,
                            sizeDp = (22 + Random.nextInt(26)).dp,
                        )
            }
        }
    }

    BoxWithConstraints(modifier) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        marks.forEach { mark ->
            key(mark.id) {
                BurnMarkView(
                    sizeDp = mark.sizeDp,
                    modifier =
                        Modifier.offset(
                            x = (containerWidth - mark.sizeDp) * mark.xFraction,
                            y = (containerHeight - mark.sizeDp) * mark.yFraction,
                        ),
                    onDissolved = { marks = marks.filter { it.id != mark.id } },
                )
            }
        }
    }
}

@Composable
private fun BurnMarkView(
    sizeDp: Dp,
    modifier: Modifier = Modifier,
    onDissolved: () -> Unit,
) {
    val spread = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }
    val dissolveAlpha = remember { Animatable(1f) }

    // Fixed per-mark "wobble" — the blob grows via spread, but its relative irregularity stays
    // put, otherwise the outline would visibly writhe every frame instead of just expanding.
    val radiusMultipliers = remember { List(BLOB_POINTS) { 1f - BLOB_IRREGULARITY / 2f + Random.nextFloat() * BLOB_IRREGULARITY } }
    val rotationOffsetDeg = remember { Random.nextFloat() * 360f }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { spread.animateTo(1f, animationSpec = tween(SPREAD_DURATION_MS, easing = FastOutSlowInEasing)) }
            launch {
                glow.animateTo(1f, animationSpec = tween(EMBER_RISE_MS, easing = LinearEasing))
                delay(EMBER_HOLD_MS.toLong())
                glow.animateTo(0f, animationSpec = tween(EMBER_FADE_MS, easing = FastOutSlowInEasing))
            }
        }
        delay(HOLD_DURATION_MS.toLong())
        dissolveAlpha.animateTo(0f, animationSpec = tween(DISSOLVE_DURATION_MS, easing = FastOutSlowInEasing))
        onDissolved()
    }

    Canvas(modifier = modifier.size(sizeDp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f
        val progress = spread.value
        val alpha = dissolveAlpha.value

        // Warm ember glow, bigger than the char itself so it bleeds out at the edges.
        if (glow.value > 0.01f) {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                EMBER_COLOR.copy(alpha = glow.value * 1f * alpha),
                                EMBER_COLOR.copy(alpha = glow.value * 0.35f * alpha),
                                Color.Transparent,
                            ),
                        center = center,
                        radius = maxRadius * 1.5f,
                    ),
                radius = maxRadius * 1.5f,
                center = center,
            )
        }

        val blobRadius = (maxRadius * 0.78f * progress).coerceAtLeast(0.5f)
        val blobPath = buildBlobPath(center, blobRadius, radiusMultipliers, rotationOffsetDeg)

        drawPath(blobPath, color = CHAR_COLOR.copy(alpha = 1f * alpha))
        drawPath(
            blobPath,
            color = SINGE_COLOR.copy(alpha = 0.85f * alpha),
            style = Stroke(width = maxRadius * 0.22f),
        )
    }
}

/** Smooth closed blob through [radiusMultipliers], curving through midpoints so corners round off instead of forming a jagged polygon. */
private fun buildBlobPath(
    center: Offset,
    baseRadius: Float,
    radiusMultipliers: List<Float>,
    rotationOffsetDeg: Float,
): Path {
    val n = radiusMultipliers.size
    val vertices =
        radiusMultipliers.mapIndexed { i, multiplier ->
            val angleDeg = rotationOffsetDeg + i * (360f / n)
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val r = baseRadius * multiplier
            Offset(
                center.x + (r * cos(angleRad)).toFloat(),
                center.y + (r * sin(angleRad)).toFloat(),
            )
        }

    fun mid(
        a: Offset,
        b: Offset,
    ) = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)

    val path = Path()
    val start = mid(vertices.last(), vertices.first())
    path.moveTo(start.x, start.y)
    for (i in vertices.indices) {
        val current = vertices[i]
        val next = vertices[(i + 1) % n]
        val midPoint = mid(current, next)
        path.quadraticTo(current.x, current.y, midPoint.x, midPoint.y)
    }
    path.close()
    return path
}
