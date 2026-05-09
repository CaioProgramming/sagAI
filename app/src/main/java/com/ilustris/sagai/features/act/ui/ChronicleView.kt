@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.act.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.ui.components.BookShelf
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.navigation.BookReaderKey
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.headerFont

/**
 * Chronicle shelf screen — shows the book collection for a saga and triggers generation
 * when a book hasn't been written yet. Once a book is ready it emits a navigation event
 * consumed by [onOpenBook], which pushes [BookReaderView] onto the back stack.
 */
@Composable
fun ChronicleView(
    title: String,
    subtitle: String,
    saga: SagaContent,
    acts: List<ActContent>,
    initialActId: Int? = null,
    titleModifier: Modifier = Modifier,
    onClose: () -> Unit,
    onOpenBook: (BookReaderKey) -> Unit,
) {
    val viewModel: ChronicleViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val visualConfig by viewModel.visualConfig.collectAsStateWithLifecycle()
    val genre = saga.data.genre

    LaunchedEffect(initialActId) {
        viewModel.start(saga)
        if (initialActId != null) {
            viewModel.selectBookById(saga, initialActId)
        }
    }

    // Navigation event — emitted when generation completes or a book already exists
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { key ->
            onOpenBook(key)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .statusBarsPadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = { onClose() },
                        modifier = Modifier.clip(CircleShape),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_back_left),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                LargeHorizontalHeader(
                    title,
                    subtitle,
                    titleStyle =
                        MaterialTheme.typography.displaySmall.copy(
                            fontFamily = genre.headerFont(),
                        ),
                    subtitleStyle =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = genre.bodyFont(),
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    titleModifier = titleModifier,
                )
            }

            SharedTransitionLayout(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                AnimatedContent(
                    targetState = state,
                    label = "ShelfTransition",
                    transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                    modifier = Modifier.fillMaxSize(),
                ) { currentState ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        val starColor by animateColorAsState(genre.resolveColor())
                        StarryTextPlaceholder(
                            modifier = Modifier.fillMaxSize(),
                            starColor,
                        )
                        BookShelf(
                            saga = saga,
                            acts = acts,
                            selectedBook = null,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            onBookSelected = viewModel::selectBook,
                            isLoading = currentState is ChronicleState.Generating,
                            reasoning = (currentState as? ChronicleState.Generating)?.message,
                            generatingActTitle = (currentState as? ChronicleState.Generating)?.actTitle,
                            visualConfig = visualConfig,
                        )
                    }
                }
            }
        }
    }
}
