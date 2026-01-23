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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont

class ReviewSummaryPage(
    override val content: SagaContent,
    private val onNavigate: (ReviewPageType) -> Unit,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.SUMMARY

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        content.data.review ?: return

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
                modifier = Modifier.padding(8.dp),
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
                                },
                    )
                }

                item {
                    DynamicCard(
                        title = "Vibe do Herói",
                        subtitle = "Sua marca emocional",
                        titleStyle =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = genre.headerFont(),
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            ),
                        subtitleStyle =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            ),
                        lineColor = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .aspectRatio(1.1f)
                                .clip(genre.bubble(isNarrator = true))
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    genre.bubble(isNarrator = true),
                                ),
                    )
                }

                item {
                    DynamicCard(
                        title = "Tempo de Jogo",
                        subtitle = "Sua dedicação",
                        titleStyle =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = genre.headerFont(),
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            ),
                        subtitleStyle =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            ),
                        lineColor = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .aspectRatio(1.1f)
                                .clip(genre.bubble(isNarrator = true))
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    genre.bubble(isNarrator = true),
                                ),
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
