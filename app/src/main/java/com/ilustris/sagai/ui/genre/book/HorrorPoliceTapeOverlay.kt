package com.ilustris.sagai.ui.genre.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.themeVfx

private val TAPE_YELLOW = Color(0xFFE8B923)
private val TAPE_STRIPE = Color(0xFF141210)

/** Faded enough that a page can still be read through it — the tape marks the scene, it doesn't seal it. */
private const val TAPE_ALPHA = 0.3f

/**
 * Horror-only: caution tape strips crossing the screen, like a scene that's already been taped
 * off. Unlike [ShinobiInkBlooms]/[CowboyBurnMarks] — background layers sitting behind the
 * content — this one is a real overlay: [SagaReview]'s [ContinuousScrollReviewContainer] draws it
 * *after* (on top of) `AutoScrollLazyColumn`, so it reads as physically taped across the scene
 * rather than part of the page underneath.
 *
 * Each strip trembles under [themeVfx] rather than sitting dead still — Horror's own `psychosis()`
 * jitter, the same twitch every other Horror surface wears, applied here instead of a bespoke
 * shake. The stencilled label is a fixed stamp now, not a scrolling marquee: a scene that's already
 * been taped off doesn't have its own caption still crawling past.
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
    val stripeWidthPx = with(LocalDensity.current) { 14.dp.toPx() }
    val stripeGapPx = with(LocalDensity.current) { 14.dp.toPx() }

    // Rotation and tremble sit on the shared outer node so the tape and its stencil twitch as one
    // taped-down object — split across two layers they'd jitter out of sync with each other.
    Box(
        modifier
            .fillMaxWidth(1.35f)
            .height(34.dp)
            .graphicsLayer { rotationZ = rotationDegrees }
            .themeVfx(true),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .alpha(TAPE_ALPHA)
                .background(TAPE_YELLOW)
                .drawStripes(stripeWidthPx, stripeGapPx),
        )
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(start = 4.dp),
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
