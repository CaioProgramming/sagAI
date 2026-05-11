@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.act.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.act.ui.components.SagaStoryReaderContent

@Composable
fun SagaStoryReaderView(
    sagaId: String,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: SagaStoryReaderViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    val visualConfig by viewModel.visualConfig.collectAsStateWithLifecycle()

    LaunchedEffect(sagaId) {
        viewModel.loadSaga(sagaId.toInt())
    }

    CompositionLocalProvider(LocalGenreVisualConfig provides visualConfig) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = saga,
                label = "StoryReaderTransition",
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                modifier = Modifier.fillMaxSize(),
            ) { currentSaga ->
                if (currentSaga != null) {
                    currentSaga.data.genre
                    Column(
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .fillMaxSize(),
                    ) {
                        SagaStoryReaderContent(
                            saga = currentSaga,
                            onBack = onBack,
                        )
                    }
                }
            }
        }
    }
}
