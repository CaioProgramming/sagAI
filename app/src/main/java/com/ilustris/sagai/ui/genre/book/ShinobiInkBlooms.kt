package com.ilustris.sagai.ui.genre.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.gradientFill
import kotlinx.coroutines.delay
import kotlin.random.Random

/** How many blooms are allowed on screen together — more than this reads as clutter, not ambience. */
private const val MAX_CONCURRENT_BLOOMS = 3

/** How often a new bloom is allowed to spawn, independent of scroll — an ambient loop, not a
 *  reaction to reader activity, so it keeps going even while they're holding still reading a page. */
private const val SPAWN_INTERVAL_MS = 2200L

/** How long the "ink spreading" reveal takes before a bloom sits fully formed. */
private const val SPREAD_DURATION_MS = 1400

/** How long a fully-formed bloom lingers before it starts dissolving back into the page. */
private const val HOLD_DURATION_MS = 2600

/** How long the fade-out ("dissolving into paper") takes. */
private const val DISSOLVE_DURATION_MS = 1800

private data class Bloom(
    val id: Long,
    val icon: Int,
    val xFraction: Float,
    val yFraction: Float,
    val sizeDp: Dp,
    val rotationDegrees: Float,
)

/**
 * Shinobi-only: an ambient, self-running loop of ink blooms scattered across the review's
 * background. Not tied to scroll (a first pass spawned off `LazyListState.firstVisibleItemIndex`
 * changes, but that meant a reader who stopped scrolling to actually read a page saw nothing
 * happen at all) — a plain interval timer instead, so the effect keeps breathing regardless of
 * reader activity. Each one is "inked in" by [com.ilustris.sagai.ui.theme.gradientFill] with an
 * expanding radial [Brush.radialGradient] — a faint, barely-there tint to start, then
 * [MaterialTheme.colorScheme.primary] spreads outward from the shape's center and darkens it,
 * like a drop of ink soaking into paper — before fading back out. Uses the two assets supplied
 * for this effect specifically ([R.drawable.ic_japan_kanji]/[R.drawable.noun_japan_cloud]), not
 * the sakura blossom/branch the real chat bubble decoration wears — those stay bubble-only.
 */
@Composable
fun ShinobiInkBlooms(modifier: Modifier = Modifier) {
    if (LocalSagaGenre.current != Genre.SHINOBI) return

    var blooms by remember { mutableStateOf(emptyList<Bloom>()) }
    var nextId by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(SPAWN_INTERVAL_MS)
            if (blooms.size < MAX_CONCURRENT_BLOOMS) {
                blooms =
                    blooms +
                        Bloom(
                            id = nextId++.toLong(),
                            icon = if (Random.nextBoolean()) R.drawable.ic_japan_kanji else R.drawable.noun_japan_cloud,
                            xFraction = 0.1f + Random.nextFloat() * 0.8f,
                            yFraction = 0.1f + Random.nextFloat() * 0.8f,
                            sizeDp = (64 + Random.nextInt(70)).dp,
                            rotationDegrees = Random.nextFloat() * 360f,
                        )
            }
        }
    }

    BoxWithConstraints(modifier) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        blooms.forEach { bloom ->
            key(bloom.id) {
                InkBloom(
                    icon = bloom.icon,
                    sizeDp = bloom.sizeDp,
                    rotationDegrees = bloom.rotationDegrees,
                    modifier =
                        Modifier.offset(
                            x = (containerWidth - bloom.sizeDp) * bloom.xFraction,
                            y = (containerHeight - bloom.sizeDp) * bloom.yFraction,
                        ),
                    onDissolved = { blooms = blooms.filter { it.id != bloom.id } },
                )
            }
        }
    }
}

@Composable
private fun InkBloom(
    icon: Int,
    sizeDp: Dp,
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
    onDissolved: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val faintTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val spread = remember { Animatable(0f) }
    val dissolveAlpha = remember { Animatable(1f) }
    val sizePx = with(LocalDensity.current) { sizeDp.toPx() }

    LaunchedEffect(Unit) {
        spread.animateTo(1f, animationSpec = tween(SPREAD_DURATION_MS, easing = FastOutSlowInEasing))
        delay(HOLD_DURATION_MS.toLong())
        dissolveAlpha.animateTo(0f, animationSpec = tween(DISSOLVE_DURATION_MS))
        onDissolved()
    }

    Image(
        painterResource(icon),
        contentDescription = null,
        colorFilter = ColorFilter.tint(faintTint),
        modifier =
            modifier
                .size(sizeDp)
                .graphicsLayer {
                    alpha = dissolveAlpha.value
                    rotationZ = rotationDegrees
                }.gradientFill(
                    Brush.radialGradient(
                        colors = listOf(accent, accent.copy(alpha = 0f)),
                        center = Offset(sizePx / 2f, sizePx / 2f),
                        radius = (sizePx * 0.8f * spread.value).coerceAtLeast(1f),
                    ),
                ),
    )
}
