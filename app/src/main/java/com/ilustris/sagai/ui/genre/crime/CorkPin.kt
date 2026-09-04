package com.ilustris.sagai.ui.genre.crime

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/** How long the card takes to turn over. Slow enough to read as a hand turning a photo. */
private const val FLIP_MS = 620

/** Past this the card is edge-on and the other face takes over. */
private const val FLIP_MIDPOINT = 90f

/**
 * Chrome for one item pinned to
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CorkboardStrip]: a slightly
 * rotated paper card with a pushpin dot in the genre's accent color, like a photo or index card
 * tacked to a detective's board. [seed] gives each pin a small, stable rotation — derived once
 * rather than re-randomized on every recomposition — so the board doesn't visibly jitter as it
 * drifts past.
 *
 * The content lambda receives the ink to write with, the way [CrimeBubbleFrame] hands its own
 * content a `contentColor`. Pins must not reach for `colorScheme.onBackground` themselves: the
 * paper is a fixed light stock in both themes, so an "on background" ink is white-on-cream at
 * night. See [CorkboardPalette].
 *
 * Pass [back] to make the card two-sided — tapping turns it over. That's where a pin puts writing
 * too long to sit under its photo, which is the same place a person puts it: on the back. The front
 * is kept measured (just transparent) while the back shows, so turning a card never changes its
 * size and never shifts the pins around it.
 *
 * The front clips its content on purpose. Photos on a board are trimmed to the card, and text
 * running past the paper's edge and over the neighbouring pins is the one failure mode that makes
 * a board unreadable — so the paper wins over whatever overflows it. The pushpin is deliberately
 * outside that clip, since a pin head overhangs the card it holds.
 */
@Composable
fun CorkPin(
    modifier: Modifier = Modifier,
    seed: Int = 0,
    palette: CorkboardPalette = rememberCorkboardPalette(),
    pinColor: Color = palette.pin,
    back: @Composable ((ink: Color) -> Unit)? = null,
    content: @Composable (ink: Color) -> Unit,
) {
    val rotation = remember(seed) { (Random(seed).nextFloat() - 0.5f) * 9f }
    val shape = RoundedCornerShape(3.dp)

    var showingBack by remember { mutableStateOf(false) }
    val flip by animateFloatAsState(
        targetValue = if (showingBack) 180f else 0f,
        animationSpec = tween(FLIP_MS),
        label = "cork-pin-flip",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier
            .rotate(rotation)
            .graphicsLayer {
                rotationY = flip
                cameraDistance = 14f * density
            },
    ) {
        Box(
            Modifier
                .shadow(6.dp, shape, clip = false)
                .background(palette.paper, shape)
                .border(1.dp, Color.Black.copy(alpha = 0.08f), shape)
                .clip(shape)
                .then(
                    if (back == null) {
                        Modifier
                    } else {
                        // No ripple: a spreading circle of highlight on paper reads as a button,
                        // and this is a photo being picked up and turned over.
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) { showingBack = !showingBack }
                    },
                ).padding(10.dp),
        ) {
            Box(Modifier.alpha(if (flip < FLIP_MIDPOINT) 1f else 0f)) {
                content(palette.ink)
            }

            if (back != null && flip >= FLIP_MIDPOINT) {
                Box(
                    Modifier
                        .matchParentSize()
                        // Un-mirrors the back, which would otherwise be rendered reversed by the
                        // half-turn the card itself is holding.
                        .graphicsLayer { rotationY = 180f },
                ) {
                    back(palette.ink)
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .size(12.dp)
                .shadow(2.dp, CircleShape)
                .background(pinColor, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
        )
    }
}
