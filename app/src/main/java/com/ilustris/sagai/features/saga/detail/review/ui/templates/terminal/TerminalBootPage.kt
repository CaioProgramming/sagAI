package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlin.time.Duration.Companion.milliseconds

/**
 * The opening "boot screen": the saga's cover art full-bleed with a scan-line reveal wipe
 * (same technique as [TerminalDecodePage]), and the intro hook typed on top — like a
 * terminal window powering on over a photo, instead of a separate cover page followed by
 * a plain black text page.
 */
class TerminalBootPage(
    override val content: SagaContent,
    private val image: ReviewImageSource,
    private val hook: ReviewText?,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.INTRO

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val normal = MaterialTheme.colorScheme.onBackground
        val reveal = remember { Animatable(if (canAnimate) 0f else 1f) }
        var titleTyped by remember { mutableStateOf(!canAnimate || hook?.title == null) }

        LaunchedEffect(image.url) {
            if (canAnimate) {
                reveal.snapTo(0f)
                reveal.animateTo(1f, tween(1800, easing = LinearEasing))
            }
        }

        Box(modifier.fillMaxSize()) {
            AsyncImage(
                model = image.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val revealY = size.height * reveal.value

                var y = 0f
                while (y < revealY) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                    )
                    y += 4f
                }

                drawRect(
                    color = Color.Black,
                    topLeft = Offset(0f, revealY),
                    size = Size(size.width, (size.height - revealY).coerceAtLeast(0f)),
                )

                if (reveal.value < 1f) {
                    drawRect(
                        color = accent.copy(alpha = 0.5f),
                        topLeft = Offset(0f, revealY - 1f),
                        size = Size(size.width, 2f),
                    )
                }

                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = size.height * 0.4f,
                            endY = size.height,
                        ),
                )
            }

            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "${content.terminalHost()}:~$ boot",
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.bodyLarge.neonGlow(accent),
                )

                hook?.title?.let {
                    SimpleTypewriterText(
                        text = "> $it",
                        style =
                            MaterialTheme.typography.titleLarge
                                .copy(
                                    fontWeight = FontWeight.Bold,
                                    color = accent,
                                ).neonGlow(accent),
                        isAnimated = canAnimate,
                        duration = 900.milliseconds,
                        onAnimationFinished = { titleTyped = true },
                    )
                }

                if (titleTyped) {
                    hook?.subtitle?.let {
                        SimpleTypewriterText(
                            text = it,
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = normal,
                                ),
                            isAnimated = canAnimate,
                            duration = 1200.milliseconds,
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        TerminalBackground(modifier)
    }
}
