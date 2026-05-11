@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.saga.detail.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.TimelineDrawer
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel
import com.ilustris.sagai.features.wiki.ui.EmotionalSheet
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.shape

@Composable
fun SagaDetailView(
    sagaId: String,
    paddingValues: PaddingValues,
    onBack: () -> Unit = {},
    onChapters: () -> Unit = {},
    onCharacters: () -> Unit = {},
    onWiki: () -> Unit = {},
    onEvents: () -> Unit = {},
    onActs: () -> Unit = {},
    onStoryReader: () -> Unit = {},
    onOpenBookReader: (Int) -> Unit = {},
    onDeleted: () -> Unit = {},
    onCharacterDetails: (Int) -> Unit = {},
    onLoreDebug: () -> Unit = {},
    viewModel: SagaDetailViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    var sagaToDelete by remember { mutableStateOf<Saga?>(null) }
    val initialSection by viewModel.initialSection.collectAsStateWithLifecycle()

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    var showReview by remember { mutableStateOf(false) }
    var showEmotionalReview by remember { mutableStateOf(false) }
    val showPremiumSheet by viewModel.showPremiumSheet.collectAsStateWithLifecycle()
    val visualConfig by viewModel.visualConfig.collectAsStateWithLifecycle()
    val drawer by viewModel.detailDrawer.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {
        if (showDeleteConfirmation) {
            showDeleteConfirmation = false
        } else {
            onBack()
        }
    }

    LaunchedEffect(state) {
        if (state == State.Deleted) {
            onDeleted()
        }
    }

    CompositionLocalProvider(LocalGenreVisualConfig provides visualConfig) {
        var lastNavigationTime by remember { mutableStateOf(0L) }
        val onAction: (DetailAction) -> Unit =
            remember(onCharacterDetails, onLoreDebug, viewModel) {
                { action ->
                    when (action) {
                        DetailAction.OpenReview -> {
                            showReview = true
                        }

                        DetailAction.OpenEmotionalReview -> {
                            showEmotionalReview = true
                        }

                        is DetailAction.OpenChronicles -> {
                            if (action.actId != null) {
                                onOpenBookReader(action.actId)
                            } else {
                                onActs()
                            }
                        }

                        DetailAction.Delete -> {
                            sagaToDelete = saga?.data
                        }

                        is DetailAction.OpenCharacter -> {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastNavigationTime > 500L) {
                                lastNavigationTime = currentTime
                                onCharacterDetails(action.characterId)
                            }
                        }

                        DetailAction.OpenLoreDebug -> {
                            onLoreDebug()
                        }

                        is DetailAction.OpenSection -> {
                            when (action.section) {
                                RequestSection.EVENTS -> onEvents()
                                RequestSection.CHARACTERS -> onCharacters()
                                RequestSection.WIKI -> onWiki()
                                RequestSection.CHAPTERS -> onChapters()
                                RequestSection.ACTS -> onActs()
                                RequestSection.START -> viewModel.loadInitialSection()
                            }
                        }

                        DetailAction.OpenStoryReader -> {
                            onStoryReader()
                        }

                        else -> {
                            viewModel.handleAction(action)
                        }
                    }
                }
            }
        SagaDetailContentView(
            state = state,
            initialSection = initialSection,
            drawer = drawer,
            onAction = onAction,
            modifier = Modifier.fillMaxSize(),
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )

        AnimatedVisibility(isGenerating) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                StarryLoader(
                    isLoading = true,
                    loadingMessage = loadingMessage ?: emptyString(),
                    textStyle = MaterialTheme.typography.labelMedium,
                    brushColors = saga?.data?.genre?.colorPalette() ?: holographicGradient,
                )
            }
        }

        if (showPremiumSheet) {
            OnboardingDialog(
                type = OnboardingType.PREMIUM_GUIDE,
                force = true,
                onDismiss = { viewModel.togglePremiumSheet() },
            )
        }

        sagaToDelete?.let {
            val genre = it.genre
            AlertDialog(
                onDismissRequest = { sagaToDelete = null },
                title = { Text(text = stringResource(R.string.saga_detail_delete_confirmation_title)) },
                text = { Text(text = stringResource(R.string.saga_detail_delete_confirmation_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSaga(it)
                        },
                        shape = genre.shape(),
                        colors =
                            ButtonDefaults.buttonColors().copy(
                                containerColor = genre.resolveColor(),
                                contentColor = genre.iconColor,
                            ),
                    ) {
                        Text(stringResource(R.string.saga_detail_delete_saga_button))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { sagaToDelete = null },
                    ) {
                        Text(stringResource(R.string.saga_detail_cancel_button))
                    }
                },
            )
        }

        saga?.let { sagaContent ->
            if (showReview) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showReview = false
                    },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    dragHandle = null,
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    SagaReview(saga = sagaContent, onDismiss = {
                        showReview = false
                    })
                }
            }

            if (showEmotionalReview) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showEmotionalReview = false
                    },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    dragHandle = null,
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    EmotionalSheet(saga = sagaContent, onDismissRequest = {
                        showEmotionalReview = false
                    })
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchSagaDetails(sagaId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SagaDetailContentView(
    state: State,
    initialSection: DetailSectionView.InitialSection?,
    drawer: TimelineDrawer?,
    onAction: (DetailAction) -> Unit = {},
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val saga = ((state as? State.Success)?.data as? SagaContent)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    saga?.let { sagaContent ->
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
        ) {
            Box(modifier = modifier) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        val genre = sagaContent.data.genre
                        val brush = Brush.verticalGradient(genre.resolveColor().darkerPalette())
                        val shape = genre.shape()
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            ModalDrawerSheet(
                                modifier =
                                    Modifier
                                        .statusBarsPadding()
                                        .fillMaxWidth(.8f)
                                        .padding(16.dp)
                                        .dropShadow(shape, {
                                            this.brush = brush
                                            this.radius = 10f
                                            this.spread = 5f
                                        }),
                                drawerShape = shape,
                                drawerContainerColor = MaterialTheme.colorScheme.background,
                            ) {
                                drawer?.renderDrawer(saga)
                            }
                        }
                    },
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        AnimatedContent(
                            initialSection,
                            transitionSpec = {
                                fadeIn(tween(500)) togetherWith
                                    fadeOut(
                                        tween(
                                            400,
                                        ),
                                    )
                            },
                            modifier =
                                Modifier
                                    .fillMaxSize(),
                            label = "SagaDetailContentTransition",
                        ) { section ->
                            section?.let {
                                SagaDetailInitialContent(
                                    saga = sagaContent,
                                    section = it,
                                    onAction = onAction,
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
