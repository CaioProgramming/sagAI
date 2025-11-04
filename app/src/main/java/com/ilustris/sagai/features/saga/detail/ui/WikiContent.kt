package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape

@Composable
fun WikiContent(
    saga: SagaContent,
    onBackClick: () -> Unit,
    titleModifier: Modifier = Modifier,
    reviewWiki: (List<Wiki>) -> Unit,
) {
    Box {
        val gridState = rememberLazyGridState()
        val titleAndSubtitle =
            DetailAction.WIKI.titleAndSubtitle(saga)
        val genre = remember { saga.data.genre }
        val chapters =
            remember {
                saga.flatChapters().filter {
                    it.events
                        .map { events -> events.updatedWikis }
                        .flatten()
                        .isNotEmpty()
                }
            }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier =
                Modifier
                    .padding(top = 80.dp)
                    .animateContentSize(),
        ) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    titleAndSubtitle.first,
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            fontFamily = genre.headerFont(),
                        ),
                    modifier =
                        titleModifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                )
            }

            chapters.forEach { chapter ->
                val wikis = chapter.fetchChapterWikis()
                item(span = { GridItemSpan(2) }) {
                    Text(
                        chapter.data.title.ifEmpty {
                            stringResource(
                                R.string.chapter_number_label,
                                saga.chapterNumber(chapter.data).toRoman(),
                            )
                        },
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = genre.headerFont(),
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }

                items(wikis) { wiki ->
                    WikiCard(
                        wiki,
                        saga.data.genre,
                        modifier =
                            Modifier
                                .animateItem()
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }

                if (chapter.isComplete()) {
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .clip(genre.shape())
                                    .clickable {
                                        reviewWiki(wikis)
                                    },
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_review),
                                null,
                                tint = genre.color,
                                modifier =
                                    Modifier
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .padding(2.dp),
                            )
                            Text(
                                "Revisar Items",
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = genre.bodyFont(),
                                        brush = genre.gradient(),
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier =
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                        .alpha(.6f),
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            gridState.canScrollBackward,
            enter = fadeIn(tween(400, delayMillis = 200)),
            exit = fadeOut(tween(200)),
        ) {
            SagaTopBar(
                titleAndSubtitle.first,
                titleAndSubtitle.second,
                saga.data.genre,
                onBackClick = { onBackClick() },
                actionContent = { Box(Modifier.size(24.dp)) },
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .safeContentPadding(),
            )
        }
    }
}
