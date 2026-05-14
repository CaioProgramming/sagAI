package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiGroup
import com.ilustris.sagai.features.wiki.ui.WikiGroupedList
import com.ilustris.sagai.ui.theme.components.SagaTopBar

@Composable
fun WikiContent(
    title: String,
    subtitle: String,
    genre: Genre,
    sagaId: Int,
    groups: List<WikiGroup>,
    onBackClick: () -> Unit,
    titleModifier: Modifier = Modifier,
    onHoldWiki: (Wiki) -> Unit = {},
) {
    val gridState = rememberLazyGridState()

    Box {
        WikiGroupedList(
            genre = genre,
            groups = groups,
            gridState = gridState,
            modifier = Modifier.padding(top = 80.dp),
            header = {
                Column {
                    Text(
                        title,
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            titleModifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )

                    Text(
                        subtitle,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }
            },
            onHoldWiki = onHoldWiki,
        )

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
