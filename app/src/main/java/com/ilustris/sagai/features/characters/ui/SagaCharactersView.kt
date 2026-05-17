package com.ilustris.sagai.features.characters.ui

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
import com.ilustris.sagai.features.characters.presentation.CharacterViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SagaCharactersView(
    sagaId: String,
    onBack: () -> Unit,
    onCharacterDetails: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: CharacterViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        viewModel.loadCharacters(sagaId.toInt())
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
            label = "SagaCharactersContent",
        ) { sagaContent ->
            if (sagaContent != null) {
                CharactersGalleryContent(
                    title = stringResource(R.string.saga_detail_section_title_characters),
                    subtitle =
                        stringResource(
                            R.string.saga_detail_section_subtitle_characters,
                            sagaContent.characters.size,
                        ),
                    saga = sagaContent,
                    characters = sagaContent.characters,
                    relationships = sagaContent.relationships,
                    onOpenCharacter = onCharacterDetails,
                    onBackClick = onBack,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    titleModifier =
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = "saga_${sagaContent.data.id}_title"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        },
                )
            }
        }
    }
}
