package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.LocalSagaGenre

private val TAPE_YELLOW = Color(0xFFE8B923)
private val TAPE_STRIPE = Color(0xFF141210)

/**
 * Horror-only: caution tape strips crossing the screen, like a scene that's already been taped
 * off. Unlike [ShinobiInkBlooms]/[CowboyBurnMarks] — background layers sitting behind the
 * content — this one is a real overlay: [SagaReview]'s [ContinuousScrollReviewContainer] draws it
 * *after* (on top of) `AutoScrollLazyColumn`, so it reads as physically taped across the scene
 * rather than part of the page underneath. Deliberately static in position (real tape doesn't
 * drift) — the only motion is the stenciled text marquee-scrolling along each strip's length.
 */
@Composable
fun HorrorPoliceTapeOverlay(modifier: Modifier = Modifier) {
    if (LocalSagaGenre.current != Genre.HORROR) return

    val tapeText = stringResource(R.string.review_horror_tape_text)
    val repeatedText = remember(tapeText) { "   $tapeText   ".repeat(12) }

    Box(modifier.fillMaxSize()) {
        PoliceTapeStrip(
            text = repeatedText,
            rotationDegrees = -9f,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 96.dp),
        )
        PoliceTapeStrip(
            text = repeatedText,
            rotationDegrees = 7f,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-72).dp),
        )
    }
}

@Composable
private fun PoliceTapeStrip(
    text: String,
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "policeTapeScroll")
    val scrollPx by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1600f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "tapeTextScroll",
    )
    val stripeWidthPx = with(LocalDensity.current) { 14.dp.toPx() }
    val stripeGapPx = with(LocalDensity.current) { 14.dp.toPx() }

    Box(
        modifier
            .fillMaxWidth(1.35f)
            .height(34.dp)
            .graphicsLayer { rotationZ = rotationDegrees }
            .background(TAPE_YELLOW)
            .drawStripes(stripeWidthPx, stripeGapPx),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = TAPE_STRIPE,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            maxLines = 1,
            softWrap = false,
            modifier =
                Modifier.offset {
                    IntOffset(scrollPx.toInt(), 0)
                },
        )
    }
}

/** Diagonal caution-tape stripes drawn behind whatever content sits on this modifier's node. */
private fun Modifier.drawStripes(
    stripeWidthPx: Float,
    stripeGapPx: Float,
) = this.drawWithContent {
    drawContent()
    clipRect {
        var x = -size.height
        val period = stripeWidthPx + stripeGapPx
        while (x < size.width + size.height) {
            val path =
                Path().apply {
                    moveTo(x, size.height)
                    lineTo(x + size.height, 0f)
                    lineTo(x + size.height + stripeWidthPx, 0f)
                    lineTo(x + stripeWidthPx, size.height)
                    close()
                }
            drawPath(path, color = TAPE_STRIPE)
            x += period
        }
    }
}
