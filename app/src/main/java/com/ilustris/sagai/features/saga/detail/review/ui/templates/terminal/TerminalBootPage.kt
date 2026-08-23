package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.terminal.TerminalLine
import com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter
import com.ilustris.sagai.ui.genre.terminal.blockDecode
import com.ilustris.sagai.ui.genre.terminal.terminalPromptLine
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.themeStylizedText
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
    private val saga: Saga,
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

        // The boot text runs first and the picture decodes only once it has finished. A machine
        // announces what it is loading before the thing appears; running both at once made the
        // reveal something that merely happened to coincide with the words.
        var booted by remember(hook) { mutableStateOf(!canAnimate) }
        val reveal = remember { Animatable(if (canAnimate) 0f else 1f) }

        LaunchedEffect(booted, image.url) {
            if (!booted) return@LaunchedEffect
            reveal.snapTo(0f)
            reveal.animateTo(1f, tween(2000, easing = LinearEasing))
        }

        Box(modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .blockDecode(
                        progress = reveal.value,
                        edgeColor = accent.copy(alpha = 0.3f),
                        seed = image.url.hashCode(),
                    ),
            ) {
                DepthLayout(image.url) {
                    themeStylizedText(
                        saga.title,
                        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(32.dp),
                    )
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 0.4f,
                            ),
                        ),
                )
            }

            Column(
                Modifier
                    .background(fadeGradientBottom())
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TerminalTypewriter(
                    lines =
                        buildList {
                            add(
                                terminalPromptLine(
                                    host = content.terminalHost(),
                                    command = hook?.title ?: "boot",
                                    accent = accent,
                                ),
                            )
                            hook?.subtitle?.let {
                                add(
                                    TerminalLine(
                                        text = it,
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                color = normal,
                                            ),
                                    ),
                                )
                            }
                        },
                    canAnimate = canAnimate,
                    caretColor = accent,
                    onFinished = { booted = true },
                )

                Spacer(Modifier.height(100.dp))
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        TerminalBackground(modifier)
    }
}
