package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.rankByHour
import com.ilustris.sagai.features.playthrough.toPlaytimeBreakdown
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.terminal.TerminalLine
import com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter
import com.ilustris.sagai.ui.genre.terminal.terminalPromptLine

/**
 * `uptime` — the session's hard numbers, printed as a shell would report them.
 *
 * Deliberately not the big centred counter the other templates use. A terminal does not stage its
 * output: it reports a value on the left margin and moves on, and the figure carries more here for
 * being stated plainly than it did for being made a monument.
 *
 * The peak hour comes back with it. It was always computed — [rankByHour] feeds it to the model as
 * a prompt input — but nothing ever showed it to the player, which left the stage's prose talking
 * about a fact the screen never stated.
 */
class TerminalUptimePage(
    override val content: SagaContent,
    private val playstyle: ReviewText?,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val normal = MaterialTheme.colorScheme.onBackground

        val uptime = remember(content) { content.data.playTimeMs.toPlaytimeBreakdown().format() }
        val peakHour =
            remember(content) {
                content.rankByHour().maxByOrNull { it.value.size }?.key
            }

        val valueStyle =
            MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        val bodyStyle =
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = normal,
            )

        val lines =
            buildList {
                add(
                    terminalPromptLine(
                        host = content.terminalHost(),
                        command = playstyle?.title ?: "uptime",
                        accent = accent,
                    ),
                )
                add(TerminalLine("> uptime: $uptime", valueStyle))
                peakHour?.let {
                    add(TerminalLine("> peak_hour: %02dh".format(it), valueStyle))
                }
                playstyle?.subtitle?.let {
                    add(TerminalLine(it, bodyStyle, alpha = .75f))
                }
            }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            TerminalTypewriter(
                lines = lines,
                modifier = Modifier.padding(24.dp),
                canAnimate = canAnimate,
                caretColor = accent,
            )
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        TerminalBackground(modifier)
    }
}
