package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import kotlinx.coroutines.delay
import kotlin.random.Random

private val SCRIBBLE_ICONS =
    listOf(
        R.drawable.ic_lightning_bolt,
        R.drawable.ic_punk_stars,
        R.drawable.ic_punk_skull,
        R.drawable.ic_doodle,
        R.drawable.ic_doodle_heart,
        R.drawable.ic_star_doodle,
    )
private const val DRAW_DURATION_MS = 2000
private const val HOLD_DURATION_MS = 800L
private const val FADE_DURATION_MS = 5000
private const val FILL_FADE_DURATION_MS = 400
private const val STAGGER_MS = 1500L
private const val SLOT_COUNT = 2
private val ICON_SIZE = 56.dp
private val SCRIBBLE_STROKE_WIDTH = 3.dp

/**
 * Two icons — picked from the same pool [com.ilustris.sagai.features.saga.chat.ui.components.decoration.PunkRockOverlay]
 * scatters around Punk Rock chat bubbles — perpetually "hand-drawn" into place: the actual vector
 * path is traced progressively (via [android.graphics.PathMeasure], not a plain fade-in), held,
 * faded out, then a new random icon/position/rotation is picked and the cycle repeats. The two
 * slots are staggered [STAGGER_MS] apart so they never start drawing in lockstep. Ink color
 * defaults to the theme background — reads as a pale scribble against the poster rather than a
 * themed accent.
 */
@Composable
fun PunkScribbleOverlay(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
) {
    BoxWithConstraints(modifier) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        repeat(SLOT_COUNT) { slotIndex ->
            ScribbleSlot(
                slotIndex = slotIndex,
                color = color,
                startDelayMs = slotIndex * STAGGER_MS,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
            )
        }
    }
}

private data class ScribbleCycle(
    val iconRes: Int,
    val rotation: Float,
    val offsetXFraction: Float,
    val offsetYFraction: Float,
)

@Composable
private fun ScribbleSlot(
    slotIndex: Int,
    color: Color,
    startDelayMs: Long,
    containerWidth: Dp,
    containerHeight: Dp,
) {
    var cycle by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    val cycleState =
        remember(slotIndex, cycle) {
            val r = Random(slotIndex * 733 + cycle * 197)
            ScribbleCycle(
                iconRes = SCRIBBLE_ICONS[r.nextInt(SCRIBBLE_ICONS.size)],
                rotation = r.nextInt(-25, 25).toFloat(),
                offsetXFraction = 0.12f + r.nextFloat() * 0.66f,
                offsetYFraction = 0.1f + r.nextFloat() * 0.7f,
            )
        }

    LaunchedEffect(cycle) {
        if (cycle == 0) delay(startDelayMs)
        progress.snapTo(0f)
        alpha.snapTo(1f)
        progress.animateTo(1f, tween(DRAW_DURATION_MS, easing = LinearEasing))
        delay(HOLD_DURATION_MS)
        alpha.animateTo(0f, tween(FADE_DURATION_MS))
        cycle++
    }

    ScribbleIcon(
        iconRes = cycleState.iconRes,
        drawProgress = progress.value,
        alpha = alpha.value,
        color = color,
        modifier =
            Modifier
                .offset(x = containerWidth * cycleState.offsetXFraction, y = containerHeight * cycleState.offsetYFraction)
                .rotate(cycleState.rotation),
    )
}

/** Everything about an icon that's fixed regardless of animation progress — measured once per [ScribbleIcon.iconRes] instead of every frame. */
private data class ScribblePathData(
    val composePath: Path,
    val androidPath: android.graphics.Path,
    val contourCount: Int,
)

@Composable
private fun ScribbleIcon(
    iconRes: Int,
    drawProgress: Float,
    alpha: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (alpha <= 0f) return

    val vector = ImageVector.vectorResource(iconRes)
    val pathData =
        remember(iconRes) {
            val nodes = mutableListOf<PathNode>()
            collectPathNodes(vector.root, nodes)
            val composePath = PathParser().addPathNodes(nodes).toPath()
            val androidPath = composePath.asAndroidPath()
            val measure = android.graphics.PathMeasure(androidPath, false)
            var contourCount = 0
            do {
                contourCount++
            } while (measure.nextContour())
            ScribblePathData(composePath, androidPath, contourCount)
        }
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { SCRIBBLE_STROKE_WIDTH.toPx() }
    val fullyDrawn = drawProgress >= 1f

    // Crossfades in on its own timer instead of snapping the instant drawProgress hits 1 — a
    // fillAlpha == drawProgress tie read as a rendering glitch (the fill just appearing) rather
    // than an animation, since drawProgress itself stops changing right at that point.
    val fillAlpha by animateFloatAsState(
        targetValue = if (fullyDrawn) 1f else 0f,
        animationSpec = tween(FILL_FADE_DURATION_MS),
        label = "scribbleFill",
    )

    Canvas(modifier.size(ICON_SIZE)) {
        val viewportScale = size.width / vector.viewportWidth
        scale(viewportScale, viewportScale, pivot = Offset.Zero) {
            if (fillAlpha > 0f) {
                drawPath(path = pathData.composePath, color = color, alpha = alpha * fillAlpha, style = Fill)
            }
            if (fullyDrawn) {
                // Keep the crisp outline even once filled, so the shape stays legible instead of
                // turning into a soft blob.
                drawPath(
                    path = pathData.composePath,
                    color = color,
                    alpha = alpha,
                    style = Stroke(width = strokeWidthPx / viewportScale, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            } else {
                val trimmed = buildTrimmedAndroidPath(pathData.androidPath, pathData.contourCount, drawProgress).asComposePath()
                drawPath(
                    path = trimmed,
                    color = color,
                    alpha = alpha,
                    style =
                        Stroke(
                            width = strokeWidthPx / viewportScale,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                )
            }
        }
    }
}

/** Flattens every leaf [VectorPath]'s node list under [group] — nested group transforms are ignored, fine for small single-layer decorative icons like these. */
private fun collectPathNodes(
    group: VectorGroup,
    out: MutableList<PathNode>,
) {
    for (node: VectorNode in group) {
        when (node) {
            is VectorPath -> out += node.pathData
            is VectorGroup -> collectPathNodes(node, out)
            else -> Unit
        }
    }
}

/**
 * Walks every contour of [source] in order, giving each an *equal* share of [progress] rather than
 * a share proportional to its arc length — a multi-path icon like the skull has one huge outline
 * contour and several tiny detail contours (eyes, nose, teeth); a length-proportional budget spent
 * ~95% of the animation on the outline alone and crammed the small details into the last sliver of
 * progress, so they visually snapped in as a separate, disconnected animation instead of reading as
 * part of the same continuous "pen tracing the shape" reveal. Equal shares make every contour take
 * a fair, visible amount of time regardless of how simple or complex it is.
 *
 * Each contour's segment is measured into its own throwaway [android.graphics.Path] and unioned
 * into [dest] with [android.graphics.Path.addPath] — calling [android.graphics.PathMeasure.getSegment]
 * straight into a shared accumulator across multiple calls does *not* reliably append on Android;
 * every earlier contour's segment kept getting clobbered by the next one, which is why multi-path
 * icons (the two stars, the skull's outline + eye/nose/teeth details, the bolt's six segments) only
 * ever showed their single most-recently-processed piece instead of accumulating into the whole icon.
 *
 * [contourCount] is passed in (measured once per icon in [ScribbleIcon], not recounted on every
 * animation frame) — that second full walk of the path was pure overhead once the actual segment
 * extraction below already has to walk every contour anyway.
 */
private fun buildTrimmedAndroidPath(
    source: android.graphics.Path,
    contourCount: Int,
    progress: Float,
): android.graphics.Path {
    val dest = android.graphics.Path()
    val clamped = progress.coerceIn(0f, 1f)
    if (clamped <= 0f || contourCount <= 0) return dest

    val perContourBudget = 1f / contourCount
    val measure = android.graphics.PathMeasure(source, false)
    var index = 0
    do {
        val contourLength = measure.length
        val contourStartProgress = index * perContourBudget
        val localProgress = ((clamped - contourStartProgress) / perContourBudget).coerceIn(0f, 1f)
        if (localProgress > 0f) {
            val segmentEnd = contourLength * localProgress
            if (segmentEnd > 0f) {
                val segment = android.graphics.Path()
                measure.getSegment(0f, segmentEnd, segment, true)
                dest.addPath(segment)
            }
        }
        index++
    } while (measure.nextContour())
    return dest
}
