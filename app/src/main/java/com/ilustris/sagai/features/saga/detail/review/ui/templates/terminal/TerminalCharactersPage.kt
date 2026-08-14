package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.review.ui.PopIn
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
                Text(
                    text = "${content.terminalHost()}:~$ ls ./characters --top ${topCharacters.size}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.bodyLarge.neonGlow(accent),
                )

                stage.content?.subtitle?.let {
                    Text(
                        text = "# $it",
                        fontFamily = FontFamily.Monospace,
                        color = normal.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                topCharacters.forEachIndexed { index, (character, messageCount) ->
                    val characterColor = character.terminalColor(accent)
                    PopIn(index = index) {
                        Text(
                            text =
                                "%02d  %-24s %s".format(
                                    index + 1,
                                    character.name.take(24),
                                    stringResource(R.string.messages_count_label, messageCount),
                                ),
                            fontFamily = FontFamily.Monospace,
                            color = characterColor,
                            style = MaterialTheme.typography.bodyMedium.neonGlow(characterColor, blurRadius = 8f),
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
