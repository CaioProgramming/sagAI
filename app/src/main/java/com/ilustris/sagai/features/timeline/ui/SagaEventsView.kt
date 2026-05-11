package com.ilustris.sagai.features.timeline.ui

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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SagaEventsView(
    sagaId: String,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val timelineView by viewModel.timelineView.collectAsStateWithLifecycle()
    val sagaInfo = timelineView?.saga

    BackHandler {
        onBack()
    }

    LaunchedEffect(sagaId) {
        viewModel.loadSaga(sagaId.toInt())
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        AnimatedContent(
            sagaInfo,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(400))
            },
            label = "SagaEventsContent",
        ) { info ->
            if (info != null) {
                TimeLineContent(
                    sagaId = sagaId.toInt(),
                    title = stringResource(R.string.saga_detail_section_title_timeline),
                    subtitle =
                        stringResource(
                            R.string.saga_detail_section_subtitle_timeline,
                            timelineView?.groups?.sumOf { it.events.size } ?: 0,
                        ),
                    onBackClick = onBack,
                    titleModifier =
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = "saga-title-${info.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        },
                )
            }
        }
    }
}
