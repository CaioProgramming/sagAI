package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.terminal.TerminalLine
import com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter
import com.ilustris.sagai.ui.genre.terminal.terminalPromptLine

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
        val accent = MaterialTheme.colorScheme.primary
        val normal = MaterialTheme.colorScheme.onBackground

        // The command is the stage's own title rather than a hardcoded verb: it is already the
        // label for what this beat is, it is already translated, and inventing a second English
        // word for the same thing meant carrying a string that would need translating later.
        val lines =
            buildList {
                add(
                    terminalPromptLine(
                        host = content.terminalHost(),
                        command = text.title ?: command,
                        accent = accent,
                    ),
                )
                text.subtitle?.let {
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
