package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/** An image "decoding" line by line, like a file being pulled off a slow connection. */
class TerminalDecodePage(
    override val content: SagaContent,
    private val image: ReviewImageSource,
    private val command: String,
    override val pageType: ReviewPageType,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val reveal = remember { Animatable(if (canAnimate) 0f else 1f) }

        LaunchedEffect(image.url) {
            if (canAnimate) {
                reveal.snapTo(0f)
                reveal.animateTo(1f, tween(2200, easing = LinearEasing))
            }
        }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "${content.terminalHost()}:~$ $command ${image.caption}.img",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8f),
                ) {
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
                    }
                }

                val filledBars = (reveal.value * 20).toInt()
                Text(
                    text = "[${"#".repeat(filledBars)}${"_".repeat(20 - filledBars)}] ${(reveal.value * 100).toInt()}%",
                    fontFamily = FontFamily.Monospace,
                    color = accent.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        TerminalBackground(modifier)
    }
}
