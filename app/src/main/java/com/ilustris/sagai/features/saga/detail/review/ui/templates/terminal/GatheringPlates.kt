package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** How long each plate waits before the next one lands. */
private const val DEAL_STAGGER_MS = 260L

/** How long the scattered plates stay spread out before being collected. */
private const val HOLD_BEFORE_GATHER_MS = 900L

private const val DEAL_ANIM_MS = 520
private const val GATHER_ANIM_MS = 900

/** Stack offset per plate once collected, so the pile still reads as many things, not one. */
private val STACK_STEP = 4.dp

/**
 * Deals a set of plates out across the area one at a time, then collects them into a single
 * stack — the gesture of gathering loose material into a folder.
 *
 * The two phases carry different meanings and so are deliberately not one interpolation: dealing
 * is *these are the pieces*, gathering is *and they are now one thing*. Each plate's scattered
 * position is a stable pseudo-random point derived from its index, so the spread looks strewn
 * rather than arranged, but never reshuffles across recomposition.
 *
 * Plates stay axis-aligned. Tilting them would read as paper being tossed on a desk, and a
 * terminal has no paper — a machine placing frames on a grid does not rotate them.
 *
 * The plates keep their painting order in the stack, so the first thing dealt ends up at the
 * bottom of the pile, which is how a physical stack would end up.
 */
@Composable
fun <T> GatheringPlates(
    items: List<T>,
    modifier: Modifier = Modifier,
    plateSize: Dp = 132.dp,
    areaHeight: Dp = 260.dp,
    canAnimate: Boolean = true,
    seed: Int = 0,
    plate: @Composable (item: T, index: Int) -> Unit,
) {
    if (items.isEmpty()) return

    // -1 = nothing dealt yet; items.size = everything dealt; then `gathered` flips.
    var dealtCount by remember(items.size, canAnimate) {
        mutableIntStateOf(if (canAnimate) 0 else items.size)
    }
    var gathered by remember(items.size, canAnimate) { mutableStateOf(!canAnimate) }

    LaunchedEffect(items.size, canAnimate) {
        if (!canAnimate) return@LaunchedEffect
        repeat(items.size) {
            delay(DEAL_STAGGER_MS)
            dealtCount++
        }
        delay(HOLD_BEFORE_GATHER_MS)
        gathered = true
    }

    val gatherProgress by animateFloatAsState(
        targetValue = if (gathered) 1f else 0f,
        animationSpec = tween(GATHER_ANIM_MS, easing = FastOutSlowInEasing),
        label = "gather",
    )

    BoxWithConstraints(
        modifier.fillMaxWidth().height(areaHeight),
        contentAlignment = Alignment.Center,
    ) {
        val spreadX = (maxWidth - plateSize).coerceAtLeast(0.dp)
        val spreadY = (areaHeight - plateSize).coerceAtLeast(0.dp)

        items.forEachIndexed { index, item ->
            val isDealt = index < dealtCount
            val dealProgress by animateFloatAsState(
                targetValue = if (isDealt) 1f else 0f,
                animationSpec = tween(DEAL_ANIM_MS, easing = LinearOutSlowInEasing),
                label = "deal_$index",
            )

            // Stable per-plate scatter: a fixed seed plus the index, so the layout is random-looking
            // but identical on every recomposition.
            val scatter =
                remember(index, seed, spreadX, spreadY) {
                    val random = Random(seed * 31 + index)
                    val angle = random.nextFloat() * 6.2831853f
                    val distance = 0.35f + random.nextFloat() * 0.65f
                    ScatterSpot(
                        x = cos(angle) * distance * (spreadX.value / 2f),
                        y = sin(angle) * distance * (spreadY.value / 2f),
                    )
                }

            // Once gathered every plate converges on the centre, keeping only a small per-index
            // step so the pile still reads as a stack of many rather than as one card.
            val stackStep = STACK_STEP.value * index
            val x = scatter.x * (1f - gatherProgress) + stackStep * gatherProgress
            val y = scatter.y * (1f - gatherProgress) + stackStep * gatherProgress

            Box(
                Modifier
                    .size(plateSize)
                    .graphicsLayer {
                        translationX = x * density
                        translationY = y * density
                        // Plates arrive slightly oversized and settle, so landing reads as a drop
                        // rather than as a fade-in.
                        val scale = 0.86f + 0.14f * dealProgress
                        scaleX = scale
                        scaleY = scale
                    }.alpha(dealProgress),
            ) {
                plate(item, index)
            }
        }
    }
}

private data class ScatterSpot(
    val x: Float,
    val y: Float,
)

/**
 * The frame the terminal draws around any rendered plate — a hard bright rule over a dark ground,
 * so a stacked pile still reads as separate frames rather than as one blurred mass of art.
 *
 * The shadow is spread in the frame's own colour rather than in black: a phosphor edge throws
 * light outward, it does not cast a shadow, so tinting the spread is what makes the border read as
 * lit instead of as a card lifted off the page.
 */
fun Modifier.plateFrame(accent: Color) =
    dropShadow(
        shape = RectangleShape,
        shadow =
            Shadow(
                radius = 12.dp,
                spread = 1.dp,
                color = accent,
                offset = DpOffset.Zero,
            ),
    ).background(Color.Black)
        .border(2.dp, accent)
        .padding(2.dp)
