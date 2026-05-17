@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.chapter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel

@Composable
fun ChapterView(
    sagaId: String,
    onBack: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: ChapterViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    val chaptersInfo by viewModel.chaptersInfo.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val reasoningMessage by viewModel.reasoningMessage.collectAsStateWithLifecycle()

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        viewModel.loadSaga(sagaId)
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        AnimatedContent(
            saga,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(400))
            },
            label = "SagaChaptersContent",
        ) { sagaContent ->
            if (sagaContent != null) {
                ChaptersGalleryContent(
                    saga = sagaContent,
                    chapters = chaptersInfo,
                    isGenerating = isGenerating,
                    loadingMessage = reasoningMessage,
                    onGenerateIcon = {
                        viewModel.generateIcon(it.id)
                    },
                    onReviewChapter = {
                        viewModel.reviewChapter(it.id)
                    },
                )
            }
        }
    }
}
