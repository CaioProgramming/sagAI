package com.ilustris.sagai.features.saga.detail.ui

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
import com.ilustris.sagai.features.wiki.presentation.WikiViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SagaWikiView(
    sagaId: String,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: WikiViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    val groups by viewModel.wikiGroups.collectAsStateWithLifecycle()

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
            label = "SagaWikiContent",
        ) { sagaContent ->
            if (sagaContent != null) {
                WikiContent(
                    title = stringResource(R.string.saga_detail_section_title_wiki),
                    subtitle =
                        stringResource(
                            R.string.saga_detail_section_subtitle_wiki,
                            groups.sumOf { it.wikis.size },
                        ),
                    genre = sagaContent.genre,
                    sagaId = sagaContent.id,
                    groups = groups,
                    onBackClick = onBack,
                    onHoldWiki = {
                        // viewModel.reviewWiki(it)
                    },
                    titleModifier =
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = "saga_${sagaContent.id}_title"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        },
                )
            }
        }
    }
}
