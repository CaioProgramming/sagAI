package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiGroup
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape

@Composable
fun WikiContent(
    title: String,
    subtitle: String,
    insight: String?,
    saga: SagaContent,
    groups: List<WikiGroup>,
    onBackClick: () -> Unit,
    titleModifier: Modifier = Modifier,
    reviewWiki: (List<Wiki>) -> Unit = {},
    onHoldWiki: (Wiki) -> Unit = {},
) {
    Box {
        val gridState = rememberLazyGridState()
        val genre = saga.data.genre

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier =
                Modifier
                    .padding(top = 80.dp)
                    .animateContentSize(),
        ) {
            stickyHeader {
                Column {
                    Text(
                        title,
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = genre.headerFont(),
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            titleModifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    subtitle,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                )
            }

            item(span = { GridItemSpan(2) }) {
                insight?.let {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic,
                            ),
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .fillMaxWidth()
                                .alpha(.7f),
                    )
                }
            }

            groups.forEach {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        it.title,
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

                items(it.wikis) { wiki ->
                    WikiCard(
                        wiki = wiki,
                        genre = genre,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .animateItem()
                                .fillMaxWidth()
                                .pointerInput("wiki-long-press") {
                                    detectTapGestures(
                                        onLongPress = {
                                            onHoldWiki(wiki)
                                        },
                                    )
                                },
                    )
                }

                if (it.canBeReviewed) {
                    item(span = { GridItemSpan(2) }) {
                        Button(
                            onClick = {
                                reviewWiki(it.wikis)
                            },
                            modifier = Modifier.padding(16.dp),
                            shape = genre.shape(),
                            colors =
                                ButtonDefaults.elevatedButtonColors().copy(
                                    containerColor = genre.resolveColor(),
                                    contentColor = genre.resolveIconColor(),
                                ),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_review),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .padding(4.dp)
                                        .size(24.dp),
                            )

                            Text(stringResource(R.string.review_items))
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
                title,
                subtitle,
                genre,
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
