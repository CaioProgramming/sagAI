package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.terminal.TerminalCommandLine
import com.ilustris.sagai.ui.genre.terminal.TerminalLine
import com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter
import com.ilustris.sagai.ui.genre.terminal.neonGlow
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.blink
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.toEasing
import com.ilustris.sagai.ui.theme.toShape

/**
 * `scan --emotion` — the same dominant-emotion reading [com.ilustris.sagai.features.saga.detail.review.ui.ReviewExpressivenessPage]
 * shows via a drawn vibe shape, rendered here as a terminal stat line instead.
 */
class TerminalEmotionScanPage(
    override val content: SagaContent,
    private val review: ReviewText?,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.EXPRESSIVENESS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val normal = MaterialTheme.colorScheme.onBackground
        val dominantTone =
            remember {
                content
                    .flatEvents()
                    .map { it.emotionalRanking() }
                    .firstOrNull()
                    ?.firstOrNull()
                    ?.first
            }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TerminalCommandLine(
                    host = content.terminalHost(),
                    command = "scan --emotion",
                    accent = accent,
                    style = MaterialTheme.typography.labelLarge,
                    showCaret = false,
                )

                // These three used to race each other onto the screen — three typewriters starting
                // at once reads as a glitch, not as a readout. Now they print in order, and the
                // signal shape below only arrives once the scan has finished reporting.
                val lines =
                    buildList {
                        add(
                            TerminalLine(
                                text = "> scan complete",
                                style = MaterialTheme.typography.labelMedium,
                                alpha = .5f,
                            ),
                        )
                        review?.title?.let {
                            add(
                                TerminalLine(
                                    text = it,
                                    style =
                                        MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                ),
                            )
                        }
                        review?.subtitle?.let {
                            add(
                                TerminalLine(
                                    text = it,
                                    style = MaterialTheme.typography.labelMedium,
                                    alpha = .5f,
                                ),
                            )
                        }
                    }

                var scanReported by remember(lines) { mutableStateOf(!canAnimate) }

                TerminalTypewriter(
                    lines = lines,
                    canAnimate = canAnimate,
                    caretColor = accent,
                    onFinished = { scanReported = true },
                )

                if (!scanReported) return@Column

                dominantTone?.let { tone ->

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.gradientFill(Brush.verticalGradient(tone.color.darkerPalette())),
                    ) {
                        val shape = tone.starShape().toShape()
                        val infiniteTransition = rememberInfiniteTransition()
                        val rotation by infiniteTransition.animateFloat(
                            0f,
                            360f,
                            animationSpec =
                                infiniteRepeatable(
                                    tween(3000, easing = tone.toEasing()),
                                    repeatMode = RepeatMode.Reverse,
                                ),
                        )

                        Box(
                            Modifier
                                .rotate(rotation)
                                .size(24.dp)
                                .border(1.dp, sagaBrush(), shape)
                                .padding(2.dp)
                                .background(tone.color, shape),
                        )

                        SimpleTypewriterText(
                            text = "dominant_signal:",
                            style =
                                MaterialTheme.typography.labelSmall
                                    .copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ).neonGlow(
                                        accent,
                                        blurRadius = 18f,
                                    ),
                        )

                        SimpleTypewriterText(
                            text = tone.getTitle().uppercase(),
                            style =
                                MaterialTheme.typography.bodyLarge,
                            modifier =
                                Modifier
                                    .gradientFill(
                                        Brush.verticalGradient(
                                            tone.color.darkerPalette(),
                                        ),
                                    ).blink(),
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
