package com.ilustris.sagai.features.timeline.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
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
import com.ilustris.sagai.ui.components.SectionLoading

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SagaEventsView(
    sagaId: String,
    onBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        viewModel.loadSaga(sagaId.toInt())
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
            label = "SagaEventsContent",
        ) { sagaContent ->
            if (sagaContent != null) {
                TimeLineContent(
                    saga = sagaContent,
                    title = stringResource(R.string.saga_detail_section_title_timeline),
                    subtitle =
                        stringResource(
                            R.string.saga_detail_section_subtitle_timeline,
                            sagaContent.eventsSize(),
                        ),
                    onBackClick = onBack,
                    titleModifier =
                        Modifier.then(
                            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                with(sharedTransitionScope) {
                                    Modifier.sharedBounds(
                                        rememberSharedContentState(key = "saga-title-${sagaContent.data.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    )
                                }
                            } else {
                                Modifier
                            },
                        ),
                )
            } else {
                SectionLoading()
            }
        }
    }
}
