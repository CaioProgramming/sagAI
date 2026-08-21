package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/** `ls ./characters --top 5` — the saga's cast rendered as a directory listing. */
class TerminalCharactersPage(
    override val content: SagaContent,
    private val stage: ReviewStage,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.CHARACTERS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val normal = MaterialTheme.colorScheme.onBackground
        val topCharacters =
            remember {
                content
                    .flatMessages()
                    .rankTopCharacters(content.getCharacters(true))
                    .take(5)
            }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TerminalTypewriter(
                    lines =
                        buildList {
                            add(
                                terminalPromptLine(
                                    host = content.terminalHost(),
                                    command =
                                        stage.content?.title
                                            ?: "ls ./characters --top ${topCharacters.size}",
                                    accent = accent,
                                ),
                            )
                            stage.content?.subtitle?.let {
                                add(
                                    TerminalLine(
                                        text = "# $it",
                                        style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                color = normal,
                                            ),
                                        alpha = .6f,
                                    ),
                                )
                            }
                        },
                    canAnimate = canAnimate,
                    caretColor = accent,
                )

                // The portraits are dealt out and then collected, the same gesture the archive
                // page makes with the chapter covers — the roster resolving into one dossier.
                topCharacters
                    .mapNotNull { (character, _) ->
                        character.image.takeIf { it.isNotBlank() }?.let { character to it }
                    }
                    // Dealt least-important first, so the lead lands last and ends up on top of
                    // the finished pile — the order the eye reads the stack in reverse.
                    .reversed()
                    .takeIf { it.isNotEmpty() }
                    ?.let { portraits ->
                        GatheringPlates(
                            items = portraits,
                            canAnimate = canAnimate,
                            seed = portraits.size + 7,
                            plateSize = 110.dp,
                            areaHeight = 210.dp,
                        ) { (character, image), _ ->
                            TerminalPortraitPlate(
                                imageUrl = image,
                                accentColor = character.terminalColor(accent),
                            )
                        }
                    }

                TerminalTypewriter(
                    lines =
                        topCharacters.mapIndexed { index, (character, messageCount) ->
                            val characterColor = character.terminalColor(accent)
                            TerminalLine(
                                text =
                                    "%02d  %-24s %s".format(
                                        index + 1,
                                        character.name.take(24),
                                        stringResource(R.string.messages_count_label, messageCount),
                                    ),
                                style =
                                    MaterialTheme.typography.bodyMedium
                                        .copy(
                                            fontFamily = FontFamily.Monospace,
                                            color = characterColor,
                                        ).neonGlow(characterColor, blurRadius = 8f),
                            )
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
