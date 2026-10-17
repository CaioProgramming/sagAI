package com.ilustris.sagai.ui.genre.choice

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private const val FLIP_MS = 560
private const val FLIP_MIDPOINT = 90f

/**
 * A card that turns over around its vertical axis. [back] shows until the halfway point of the
 * turn and [face] after it, so the swap happens edge-on where nothing can be seen popping.
 *
 * The face is rotated a further half turn to undo the mirroring the card's own rotation would put
 * on it. A face-up card also rises slightly, so which one is in hand reads even mid-turn.
 */
@Composable
fun FlipCard(
    faceUp: Boolean,
    modifier: Modifier = Modifier,
    back: @Composable () -> Unit,
    face: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (faceUp) 180f else 0f,
        animationSpec = tween(FLIP_MS, easing = FastOutSlowInEasing),
        label = "choice-card-flip",
    )
    val lift by animateFloatAsState(
        targetValue = if (faceUp) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "choice-card-lift",
    )

    Box(
        modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 14f * density
            scaleX = lift
            scaleY = lift
        },
    ) {
        if (rotation <= FLIP_MIDPOINT) {
            Box(Modifier.fillMaxSize()) { back() }
        } else {
            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) { face() }
        }
    }
}
