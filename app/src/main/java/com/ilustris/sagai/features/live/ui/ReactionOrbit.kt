package com.ilustris.sagai.features.live.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.live.presentation.LiveReaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.math.cos
import kotlin.math.sin

private data class Orbiter(
    val reaction: LiveReaction,
    val angleDeg: Float,
    val born: Long,
    val lifeMs: Long,
)

/**
 * Reactions orbiting the portrait in focus, TikTok-live style but kept off the captions: beads
 * only use the upper arc and the sides of the ring, pop in, drift along it and fade. A reaction
 * with a thought carries it as a small bubble under its bead, clamped inside the screen.
 *
 * Positions are computed in the draw/layout phase from a frame clock, so orbiting never
 * recomposes the screen.
 */
@Composable
fun ReactionOrbit(
    reactions: Flow<LiveReaction>,
    center: Offset01,
    orbitRadius: Dp,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
) {
    val orbiters = remember { mutableStateListOf<Orbiter>() }
    val clock = remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) { while (true) withFrameMillis { clock.longValue = it } }

    LaunchedEffect(reactions) {
        var slot = 0
        var lastEmit = 0L
        reactions.collect { reaction ->
            // Stagger bursts so beads don't stack on arrival.
            val now = System.currentTimeMillis()
            val wait = (lastEmit + STAGGER_MS - now).coerceAtLeast(0)
            if (wait > 0) delay(wait)
            lastEmit = System.currentTimeMillis()
            if (orbiters.size >= MAX_ON_SCREEN) orbiters.removeAt(0)
            orbiters +=
                Orbiter(
                    reaction = reaction,
                    angleDeg = SLOTS[slot++ % SLOTS.size],
                    born = clock.longValue,
                    lifeMs = if (reaction.thought.isNullOrBlank()) LIFE_MS else LIFE_MS + THOUGHT_EXTRA_MS,
                )
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val radiusPx = with(density) { orbitRadius.toPx() }
        val cx = widthPx * center.x
        val cy = heightPx * center.y
        val bubbleWidth = 150.dp
        val bubbleWidthPx = with(density) { bubbleWidth.toPx() }
        val edgePx = with(density) { 12.dp.toPx() }

        orbiters.toList().forEach { orbiter ->
            Box(
                Modifier.graphicsLayer {
                    val age = clock.longValue - orbiter.born
                    val k = (age.toFloat() / orbiter.lifeMs).coerceIn(0f, 1f)
                    if (k >= 1f) {
                        alpha = 0f
                        return@graphicsLayer
                    }
                    // Drift upward along the arc: beads on the left go clockwise, on the right counter.
                    val direction = if (orbiter.angleDeg < -90f) 1f else -1f
                    val drift = direction * (if (reduceMotion) 4f else 16f) * (age / 1000f)
                    val a = Math.toRadians((orbiter.angleDeg + drift).toDouble())
                    val r = radiusPx + sin(age / 700.0).toFloat() * 4f
                    translationX = cx + (cos(a) * r).toFloat() - size.width / 2f
                    translationY = cy + (sin(a) * r * 0.94f).toFloat() - with(density) { 20.dp.toPx() }
                    val pop = if (k < 0.08f) k / 0.08f else 1f
                    val s = if (k < 0.08f) 0.4f + 0.75f * pop else 1f + sin(age / 400.0).toFloat() * 0.04f
                    scaleX = s
                    scaleY = s
                    alpha = if (k > 0.8f) (1f - k) / 0.2f else pop
                },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Bead(orbiter.reaction)
                    orbiter.reaction.thought?.takeIf { it.isNotBlank() }?.let { thought ->
                        // Keep the bubble inside the screen whichever side of the ring it's on.
                        val beadX = cx + (cos(Math.toRadians(orbiter.angleDeg.toDouble())) * radiusPx).toFloat()
                        val desiredLeft = beadX - bubbleWidthPx / 2f
                        val clampedLeft = desiredLeft.coerceIn(edgePx, widthPx - edgePx - bubbleWidthPx)
                        val shiftDp = with(density) { (clampedLeft - desiredLeft).toDp() }
                        ThoughtBubble(orbiter.reaction, bubbleWidth, Modifier.offset(x = shiftDp))
                    }
                }
            }
        }

        LaunchedEffect(orbiters.size) {
            // Drop finished beads so the list stays small.
            delay(500)
            val now = clock.longValue
            orbiters.removeAll { now - it.born > it.lifeMs }
        }
    }
}

@Composable
private fun Bead(reaction: LiveReaction) {
    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
        Text(reaction.emoji, fontSize = 24.sp)
        AsyncImage(
            model = reaction.character.image,
            contentDescription = reaction.character.name,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape),
        )
    }
}

@Composable
private fun ThoughtBubble(
    reaction: LiveReaction,
    width: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .padding(top = 2.dp)
            .width(width)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(
            reaction.character.name.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            reaction.thought.orEmpty(),
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
        )
    }
}

/** A point as fractions of the container (0..1 on each axis). */
data class Offset01(
    val x: Float,
    val y: Float,
)

// Degrees: 0 = right, -90 = top. Upper arc and sides only — the captions sit below the blob.
private val SLOTS = listOf(-150f, -30f, -175f, -5f, -115f, -65f)
private const val LIFE_MS = 4_600L
private const val THOUGHT_EXTRA_MS = 1_400L
private const val STAGGER_MS = 450L
private const val MAX_ON_SCREEN = 6
