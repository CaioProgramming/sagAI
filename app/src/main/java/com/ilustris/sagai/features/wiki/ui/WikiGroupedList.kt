package com.ilustris.sagai.features.wiki.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiGroup
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape

@Composable
fun WikiGroupedList(
    genre: Genre,
    groups: List<WikiGroup>,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    header: @Composable () -> Unit = {},
    reviewWiki: (List<Wiki>) -> Unit = {},
    onHoldWiki: (Wiki) -> Unit = {},
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier.animateContentSize(),
    ) {
        item(span = { GridItemSpan(2) }) {
            header()
        }

        groups.forEach { group ->
            item(span = { GridItemSpan(2) }) {
                Text(
                    group.title,
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

            items(group.wikis) { wiki ->
                WikiCard(
                    wiki = wiki,
                    genre = genre,
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .animateItem()
                            .fillMaxWidth()
                            .pointerInput("wiki-long-press-${wiki.id}") {
                                detectTapGestures(
                                    onLongPress = {
                                        onHoldWiki(wiki)
                                    },
                                )
                            },
                )
            }

            if (group.canBeReviewed) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = {
                                reviewWiki(group.wikis)
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
    }
}
