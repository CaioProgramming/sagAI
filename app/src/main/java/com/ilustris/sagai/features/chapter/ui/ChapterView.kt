@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.saga.detail.ui.sharedTransitionActionItemModifier
import com.ilustris.sagai.features.saga.detail.ui.titleAndSubtitle
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChapterView(
    navHostController: NavHostController,
    sagaId: String?,
    viewModel: ChapterViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()

    LaunchedEffect(saga) {
        if (saga == null) {
            viewModel.loadSaga(sagaId)
        }
    }
}

@Composable
fun ChapterContent(
    saga: SagaContent,
    onBackClick: () -> Unit = {},
    viewModel: ChapterViewModel = hiltViewModel(),
    titleModifier: Modifier = Modifier,
    animationScopes: Pair<SharedTransitionScope, AnimatedContentScope>,
) {
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val genre = saga.data.genre
    val titleAndSubtitle = DetailAction.CHAPTERS.titleAndSubtitle(saga)
    val listState = rememberLazyListState()
    with(animationScopes.first) {
        Box {
            LazyColumn(state = listState) {
                item {
                    LargeHorizontalHeader(
                        titleAndSubtitle.first,
                        titleAndSubtitle.second,
                        titleStyle =
                            MaterialTheme.typography.displayMedium.copy(
                                fontFamily = genre.headerFont(),
                            ),
                        subtitleStyle =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = genre.bodyFont(),
                            ),
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        titleModifier = titleModifier,
                    )
                }
                items(saga.flatChapters().filter { it.isComplete() }) {
                    val chapterModifier =
                        this@with.sharedTransitionActionItemModifier(
                            DetailAction.CHAPTERS,
                            animationScopes.second,
                            it.data.id,
                            it.data.id,
                        )
                    ChapterContentView(
                        it,
                        saga,
                        imageSize = 500.dp,
                        modifier = chapterModifier.fillMaxWidth(),
                    )
                }

                item {
                    Spacer(Modifier.height(50.dp))
                }
            }
            AnimatedVisibility(
                listState.canScrollBackward,
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
                            .padding(top = 50.dp, start = 16.dp),
                )
            }
        }
    }

    if (isGenerating) {
        Dialog(
            onDismissRequest = { },
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            StarryTextPlaceholder(
                modifier = Modifier.fillMaxSize().gradientFill(saga.data.genre.gradient(true)),
            )
        }
    }
}
