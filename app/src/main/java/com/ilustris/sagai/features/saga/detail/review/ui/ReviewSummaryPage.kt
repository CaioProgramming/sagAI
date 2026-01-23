package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont

class ReviewSummaryPage(
    override val content: SagaContent,
    override val pageType: ReviewPageType = ReviewPageType.SUMMARY,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val review = content.data.review ?: return

        val stageTypes =
            remember(review) {
                listOfNotNull(
                    if (review.expressiveness != null) ReviewPageType.EXPRESSIVENESS else null,
                    if (review.playstyle != null) ReviewPageType.PLAYSTYLE else null,
                    if (review.topCharacters != null) ReviewPageType.CHARACTERS else null,
                    if (review.actsInsight != null) ReviewPageType.JOURNEY else null,
                    if (review.conclusion != null) ReviewPageType.CONCLUSION else null,
                )
            }

        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Our Story, Retold",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Black,
                    ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
            )

            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                item(span = { GridItemSpan(2) }) {
                    HeroSummaryCard(
                        content = content,
                        modifier =
                            Modifier
                                .aspectRatio(0.55f)
                                .clickable {
                                    onAction(ReviewAction.Navigate(ReviewPageType.INTRO))
                                },
                    )
                }

                items(stageTypes) { stage ->
                    val (title, subtitle) =
                        when (stage) {
                            ReviewPageType.EXPRESSIVENESS -> {
                                "Sua Vibe" to (
                                    review.expressiveness?.content?.title
                                        ?: "Sua marca emocional"
                                )
                            }

                            ReviewPageType.PLAYSTYLE -> {
                                "Seu Estilo" to (
                                    review.playstyle?.content?.title
                                        ?: "Como você agiu"
                                )
                            }

                            ReviewPageType.CHARACTERS -> {
                                "O Elenco" to (
                                    review.topCharacters?.content?.title
                                        ?: "Seu Squad"
                                )
                            }

                            ReviewPageType.JOURNEY -> {
                                "A Jornada" to (
                                    review.actsInsight?.content?.title
                                        ?: "O caminho que fizemos"
                                )
                            }

                            ReviewPageType.CONCLUSION -> {
                                "O Legado" to (
                                    review.conclusion?.content?.title
                                        ?: "O que ficou"
                                )
                            }

                            else -> {
                                "" to ""
                            }
                        }

                    val colorCombo =
                        listOf(
                            Triple(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.onSurface,
                                MaterialTheme.colorScheme.onSurface,
                            ),
                            Triple(
                                genre.color,
                                genre.iconColor,
                                MaterialTheme.colorScheme.background,
                            ),
                            Triple(
                                MaterialTheme.colorScheme.onBackground,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background,
                            ),
                            Triple(
                                genre.colorPalette().last(),
                                genre.iconColor,
                                MaterialTheme.colorScheme.onBackground,
                            ),
                        ).random()

                    DynamicCard(
                        title = title,
                        subtitle = subtitle,
                        titleStyle =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = genre.headerFont(),
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                color = colorCombo.second,
                            ),
                        subtitleStyle =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = colorCombo.second,
                            ),
                        lineColor = colorCombo.third,
                        modifier =
                            Modifier
                                .aspectRatio(1.1f)
                                .clip(genre.bubble(isNarrator = true))
                                .background(
                                    colorCombo.first,
                                    genre.bubble(isNarrator = true),
                                )
                                .clickable {
                                    onAction(ReviewAction.Navigate(stage))
                                },
                    )
                }
            }

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        onAction(ReviewAction.Restart)
                    },
                    colors =
                        ButtonDefaults.elevatedButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = genre.color,
                        ),
                ) {
                    Text("Recomeçar")
                }
            }
        }
    }
}
