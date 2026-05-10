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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.act.ui.components.SagaStoryReaderContent
import com.ilustris.sagai.ui.components.SectionLoading
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.headerFont

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
                    val genre = currentSaga.data.genre
                    Column(
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .fillMaxSize(),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            IconButton(
                                onClick = onBack,
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
                            title = currentSaga.data.title,
                            subtitle = "The Full Story",
                            titleStyle =
                                MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = genre.headerFont(),
                                ),
                            subtitleStyle =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                        )

                        SagaStoryReaderContent(
                            saga = currentSaga,
                            onBack = onBack,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        SectionLoading()
                    }
                }
            }
        }
    }
}
