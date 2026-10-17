package com.ilustris.sagai.features.live.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.features.live.presentation.EpilogueLiveViewModel

/** Shared-element key between the epilogue chat's input avatar and the live record button. */
fun epilogueLiveSpeakerKey(
    sagaId: Int,
    characterId: Int,
) = "epilogue_live_${sagaId}_${characterId}_speaker"

/** Live mode for one character after the saga ended: the same stage, one speaker, no carousel. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EpilogueLiveView(
    sagaId: Int,
    characterId: Int,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: EpilogueLiveViewModel = hiltViewModel(),
) {
    LaunchedEffect(sagaId, characterId) { viewModel.start(sagaId, characterId) }
    LiveStage(
        viewModel = viewModel,
        sharedKey = epilogueLiveSpeakerKey(sagaId, characterId),
        onBack = onBack,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        subtitle = stringResource(R.string.book_epilogue_title),
    )
}
