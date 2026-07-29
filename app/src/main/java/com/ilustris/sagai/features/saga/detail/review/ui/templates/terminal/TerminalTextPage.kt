package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlin.time.Duration.Companion.milliseconds

/**
 * A single terminal "command" — a prompt line followed by its typed output.
 * Covers hook and stage-content text alike, since both are just a title/subtitle pair.
 */
class TerminalTextPage(
    override val content: SagaContent,
    private val text: ReviewText,
    override val pageType: ReviewPageType,
    private val command: String,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = content.data.genre.compiledColorPalette().getOrElse(1) { MaterialTheme.colorScheme.primary }
        var titleTyped by remember { mutableStateOf(!canAnimate || text.title == null) }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "guest@sagai:~$ $command",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.bodyLarge,
                )

                text.title?.let {
                    SimpleTypewriterText(
                        text = "> $it",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                            ),
                        isAnimated = canAnimate,
                        duration = 900.milliseconds,
                        onAnimationFinished = { titleTyped = true },
                    )
                }

                if (titleTyped) {
                    text.subtitle?.let {
                        SimpleTypewriterText(
                            text = it,
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = accent.copy(alpha = 0.85f),
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
        TerminalBackground(content.data.genre, modifier)
    }
}
