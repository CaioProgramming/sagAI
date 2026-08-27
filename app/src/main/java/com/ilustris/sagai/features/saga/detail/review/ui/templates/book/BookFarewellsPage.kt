package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.data.model.Farewell
import com.ilustris.sagai.features.saga.detail.data.model.cleanMessage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.book.BookBackground

/** The "Afterword" — a signed farewell letter from each of the saga's most important characters. */
class BookFarewellsPage(
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
        val genre = content.data.genre
        val ink = LocalContentColor.current
        val speakers =
            remember(farewells) {
                farewells.mapNotNull { farewell ->
                    content.characters
                        .find { it.data.id == farewell.characterId }
                        ?.let { it.data to farewell.cleanMessage(it.data.name) }
                }
            }

        Column(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.review_farewells_title),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )

            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                speakers.forEachIndexed { index, (character, message) ->
                    val isLeft = index % 2 == 0
                    val sideAlignment = if (isLeft) Alignment.Start else Alignment.End
                    val sideTextAlign = if (isLeft) TextAlign.Start else TextAlign.End

                    val avatar: @Composable () -> Unit = {
                        CharacterAvatar(
                            character,
                            genre = genre,
                            borderColor = MaterialTheme.colorScheme.primary,
                            borderSize = 2.dp,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                    val textBlock: @Composable RowScope.() -> Unit = {
                        Column(
                            horizontalAlignment = sideAlignment,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "“$message”",
                                fontStyle = FontStyle.Italic,
                                color = ink,
                                textAlign = sideTextAlign,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "— ${character.name}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = sideTextAlign,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isLeft) {
                            avatar()
                            textBlock()
                        } else {
                            textBlock()
                            avatar()
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
