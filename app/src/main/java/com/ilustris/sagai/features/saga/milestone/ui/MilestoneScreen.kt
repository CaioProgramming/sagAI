package com.ilustris.sagai.features.saga.milestone.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneUiState
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneViewModel
import com.ilustris.sagai.ui.genre.surface.GenreScreenEffects
import com.ilustris.sagai.ui.genre.surface.GenreStoryIntroduction
import com.ilustris.sagai.ui.genre.surface.GenreStoryLoading
import com.ilustris.sagai.ui.genre.surface.GenreStoryNotice
import com.ilustris.sagai.ui.genre.surface.GenreStorySurface
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.theme.SagAITheme

/**
 * The narrative chain between scenes: an event logged, a chapter closed, an act finished, a new one
 * opening. This screen fully owns that chain — it can't be backed out of, and it drives itself step
 * by step via [MilestoneViewModel].
 *
 * It has no idea what any genre looks like. Each state is described as a
 * [com.ilustris.sagai.ui.genre.surface.StoryBeat] (see `MilestoneStoryBeat.kt`) and handed to
 * [GenreStorySurface], which resolves the treatment from the theme. That is deliberate: the
 * previous design wrapped one fixed layout in per-genre chrome, and swapping only the backdrop left
 * every saga looking like the same screen with different wallpaper.
 *
 * [SagAITheme] is re-applied here from the ViewModel's own genre rather than trusted from above.
 * This screen is reachable straight from a push or deep link, without passing through anything that
 * established the saga's theme.
 */
@Composable
fun MilestoneScreen(
    sagaId: Int,
    onFinished: () -> Unit,
    onNavigate: (NavKey) -> Unit = {},
    viewModel: MilestoneViewModel = hiltViewModel(),
) {
    LaunchedEffect(sagaId) { viewModel.start(sagaId) }
    LaunchedEffect(Unit) { viewModel.finished.collect { onFinished() } }

    // A narrative chain is mandatory once started — no escaping back into chat mid-generation.
    BackHandler(enabled = true) { }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sagaData by viewModel.sagaData.collectAsStateWithLifecycle()
    val genre by viewModel.genre.collectAsStateWithLifecycle()
    val chapterCoverImage by viewModel.chapterCoverImage.collectAsStateWithLifecycle()
    val actChapterCovers by viewModel.actChapterCovers.collectAsStateWithLifecycle()
    val bookGenerationState by viewModel.bookGenerationState.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    SagAITheme(genre = genre) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            // Terminal's tube curvature/bloom and Cyberpunk's glitch, applied once around every
            // state this screen can show rather than per-state — a screen filter, not a beat one.
            GenreScreenEffects(genre = genre) {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "milestone_step",
                ) { state ->
                    when (state) {
                        is MilestoneUiState.Loading -> {
                            GenreStoryLoading(
                                title = sagaData?.title ?: emptyString(),
                                message =
                                    state.reasoning?.takeIf { it.isNotBlank() }
                                        ?: stringResource(
                                            if (state.isAutomaticStep) {
                                                R.string.milestone_adjusting_lore
                                            } else {
                                                R.string.milestone_loading_default
                                            },
                                        ),
                                genre = genre,
                            )
                        }

                        is MilestoneUiState.Error -> {
                            GenreStoryNotice(
                                title = stringResource(R.string.milestone_error_title),
                                message = state.message,
                                genre = genre,
                                action =
                                    if (state.canRetry) {
                                        StoryBeatAction(
                                            id = "retry",
                                            label = stringResource(R.string.try_again),
                                            onClick = viewModel::retryFailedStep,
                                        )
                                    } else {
                                        null
                                    },
                            )
                        }

                        is MilestoneUiState.ClosureStep -> {
                            GenreStorySurface(
                                beat =
                                    state.toStoryBeat(
                                        sagaId = sagaId,
                                        sagaTitle = sagaData?.title,
                                        coverImage = chapterCoverImage,
                                        actCoverImages = actChapterCovers,
                                        bookGenerationState = bookGenerationState,
                                        onContinue = viewModel::onContinue,
                                        onNavigate = onNavigate,
                                        onGenerateBook = viewModel::generateBook,
                                    ),
                                genre = genre,
                            )
                        }

                        // Always one centred composition, whatever the genre — see
                        // GenreStoryIntroduction's own doc for why this beat doesn't go through
                        // GenreStorySurface's per-style layouts like every other state here does.
                        is MilestoneUiState.IntroductionStep -> {
                            GenreStoryIntroduction(
                                beat =
                                    state.milestone.toStoryBeat(
                                        sagaTitle = sagaData?.title,
                                        onContinue = viewModel::onContinue,
                                    ),
                                genre = genre,
                            )
                        }
                    }
                }

                // Only ever true for a brand-new saga's first act, tutorials on — overlaps with
                // that act's introduction generating underneath instead of gating it.
                if (showOnboarding) {
                    OnboardingDialog(
                        type = OnboardingType.GAMEPLAY_GUIDE,
                        genre = sagaData?.genre,
                        saga = sagaData,
                        onDismiss = viewModel::dismissOnboarding,
                    )
                }
            }
        }
    }
}
