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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private const val OPEN_MS = 560
private const val OPEN_DEGREES = 78f
private const val PAGE_RISE_DP = 6f

/**
 * The folder's own way of turning over: instead of flipping, the cover is hinged on its left edge
 * and swings open, and the page inside rises a little as it is uncovered.
 *
 * The cover stops short of edge-on (78°). Past 90° it would swing out to the left, over the card
 * beside it; stopped here it stays inside the card as a narrow leaf on the hinge side, which is
 * also what an opened folder looks like from above.
 */
@Composable
fun FolderOpenCard(
    faceUp: Boolean,
    tilt: Float,
    modifier: Modifier = Modifier,
    cover: @Composable () -> Unit,
    inside: @Composable () -> Unit,
) {
    val open by animateFloatAsState(
        targetValue = if (faceUp) 1f else 0f,
        animationSpec = tween(OPEN_MS, easing = FastOutSlowInEasing),
        label = "folder-open",
    )
    val lift by animateFloatAsState(
        targetValue = if (faceUp) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "folder-lift",
    )

    Box(
        modifier.graphicsLayer {
            rotationZ = tilt
            scaleX = lift
            scaleY = lift
        },
    ) {
        Box(Modifier.fillMaxSize().graphicsLayer { translationY = -open * PAGE_RISE_DP.dp.toPx() }) { inside() }
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                rotationY = -open * OPEN_DEGREES
                transformOrigin = TransformOrigin(0f, 0.5f)
                // Far enough that the swinging edge never gets near the camera.
                cameraDistance = 32f * density
            },
        ) { cover() }
    }
}
