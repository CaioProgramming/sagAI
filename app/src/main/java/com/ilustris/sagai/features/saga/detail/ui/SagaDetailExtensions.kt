package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.act.ui.ActsGalleryContent
import com.ilustris.sagai.features.chapter.ui.ChaptersGalleryContent
import com.ilustris.sagai.features.characters.ui.CharactersGalleryContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.TimelineDrawer
import com.ilustris.sagai.features.timeline.ui.TimeLineContent
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.shape

@Composable
fun DetailSectionView.RenderSection(
    saga: SagaContent,
    animationScopes: Pair<SharedTransitionScope, AnimatedContentScope>,
    onAction: (DetailAction) -> Unit = {},
) {
    when (this) {
        is DetailSectionView.InitialSection -> {
            SagaDetailInitialContent(
                saga = saga,
                section = this,
                onAction = onAction,
            )
        }

        is DetailSectionView.CharacterSection -> {
            CharactersGalleryContent(
                section = this,
                animationScopes = animationScopes,
                onOpenEvent = {
                    onAction(DetailAction.OpenSection(RequestSection.EVENTS))
                },
            )
        }

        is DetailSectionView.WikiSection -> {
            WikiContent(
                title,
                subtitle,
                insight,
                saga,
                groups = wikiGroup,
                onBackClick = {
                    onAction(DetailAction.OpenSection(RequestSection.START))
                },
                reviewWiki = {
                    onAction(DetailAction.ReviewWiki(it))
                },
            )
        }

        is DetailSectionView.EventsSection -> {
            TimeLineContent(
                saga,
                this.insight,
                this.title,
                this.subtitle,
                onBackClick = {
                    onAction(DetailAction.OpenSection(RequestSection.START))
                },
            )
        }

        is DetailSectionView.ChapterSection -> {
            ChaptersGalleryContent(
                section = this,
                onBackClick = {
                    onAction(DetailAction.OpenSection(RequestSection.START))
                },
            )
        }

        is DetailSectionView.ActSection -> {
            ActsGalleryContent(
                section = this,
                onBackClick = {
                    onAction(DetailAction.OpenSection(RequestSection.START))
                },
            )
        }
    }
}

@Composable
fun TimelineDrawer.renderDrawer(saga: SagaContent) {
    val genre = saga.data.genre
    LazyColumn(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            ) {
                Text(
                    title,
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = genre.headerFont(),
                            color = genre.resolveIconColor(),
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .background(
                                progressiveBrush(
                                    genre.resolveColor(),
                                    progress,
                                ),
                                genre.shape(),
                            ).padding(8.dp),
                )
            }
        }
        group.forEach {
            item {
                Text(
                    it.title,
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier.gradientFill(
                            progressiveBrush(
                                genre.resolveColor(),
                                it.progress,
                            ),
                        ),
                )
            }

            items(it.items) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val iconTint =
                        if (it.isComplete.not()) {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = .6f)
                        } else {
                            genre.resolveColor()
                        }

                    Icon(
                        painterResource(genre.icon),
                        null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp),
                    )

                    Text(
                        it.title,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                color = iconTint,
                            ),
                    )
                }
            }
        }
    }
}
