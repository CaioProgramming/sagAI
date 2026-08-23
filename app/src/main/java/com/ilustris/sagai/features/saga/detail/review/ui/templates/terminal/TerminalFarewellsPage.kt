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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.Farewell
import com.ilustris.sagai.features.saga.detail.data.model.cleanMessage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.terminal.TerminalLine
import com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter
import com.ilustris.sagai.ui.genre.terminal.neonGlow
import com.ilustris.sagai.ui.genre.terminal.terminalColor
import com.ilustris.sagai.ui.genre.terminal.terminalPromptLine
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlin.time.Duration.Companion.milliseconds

/** `tail ./farewells.log` — every important character's last message before disconnecting. */
class TerminalFarewellsPage(
    override val content: SagaContent,
    private val farewells: List<Farewell>,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.FAREWELLS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val speakers =
            remember(farewells) {
                farewells.mapNotNull { farewell ->
                    content.characters
                        .find { it.data.id == farewell.characterId }
                        ?.let { characterContent ->
                            Triple(
                                characterContent.data.name,
                                farewell.cleanMessage(characterContent.data.name),
                                characterContent.data.terminalColor(accent),
                            )
                        }
                }
            }
        var revealedCount by remember { mutableIntStateOf(if (canAnimate) 0 else speakers.size) }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TerminalTypewriter(
                    lines =
                        buildList {
                            add(
                                terminalPromptLine(
                                    host = content.terminalHost(),
                                    command = "tail ./farewells.log",
                                    accent = accent,
                                ),
                            )
                            speakers.forEach { (name, message, color) ->
                                add(
                                    TerminalLine(
                                        text = "> $name: $message",
                                        style =
                                            MaterialTheme.typography.bodyLarge
                                                .copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = color,
                                                ).neonGlow(color, blurRadius = 8f),
                                    ),
                                )
                            }
                        },
                    canAnimate = canAnimate,
                    caretColor = accent,
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        TerminalBackground(modifier)
    }
}
