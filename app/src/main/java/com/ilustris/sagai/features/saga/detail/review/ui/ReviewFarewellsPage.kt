package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.Farewell

/** "The Send-Off" — a short farewell from each of the saga's most important characters. */
class ReviewFarewellsPage(
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
        val speakers =
            remember(farewells) {
                farewells.mapNotNull { farewell ->
                    content.characters
                        .find { it.data.id == farewell.characterId }
                        ?.let { it.data to farewell.message }
                }
            }

        Column(
            modifier
                .fillMaxSize()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.review_farewells_title),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                textAlign = TextAlign.Center,
            )

            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(speakers) { (character, message) ->
                    PopIn(index = speakers.indexOfFirst { it.first.id == character.id }) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CharacterAvatar(
                                character,
                                genre = genre,
                                borderSize = 2.dp,
                                modifier = Modifier.size(56.dp),
                            )

                            Column {
                                Text(
                                    text = character.name,
                                    style =
                                        MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        ),
                                )
                                Text(
                                    text = message,
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
