package com.ilustris.sagai.features.brain.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.brain.domain.model.BrainMode
import com.ilustris.sagai.features.brain.presentation.BrainViewModel
import com.ilustris.sagai.features.brain.ui.components.BrainCanvas
import com.ilustris.sagai.features.brain.ui.components.BrainDetailSheet
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.components.SagaTopBar

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SagaBrainView(
    sagaId: String,
    onBack: () -> Unit,
    onOpenCharacterBrain: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: BrainViewModel = hiltViewModel(),
) {
    BrainScreenContent(
        sagaId = sagaId,
        characterId = null,
        title = stringResource(R.string.saga_brain_title),
        subtitle = stringResource(R.string.saga_brain_subtitle),
        onBack = onBack,
        onOpenCharacterBrain = onOpenCharacterBrain,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        viewModel = viewModel,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CharacterBrainView(
    sagaId: String,
    characterId: Int,
    onBack: () -> Unit,
    onOpenCharacterBrain: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: BrainViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val graph = state.graph
    val graphCenterLabel =
        graph?.nodeById(graph.centerNodeId)?.label
            ?: stringResource(R.string.saga_brain_character_title)
    BrainScreenContent(
        sagaId = sagaId,
        characterId = characterId,
        title = graphCenterLabel,
        subtitle = stringResource(R.string.saga_brain_character_subtitle),
        onBack = onBack,
        onOpenCharacterBrain = onOpenCharacterBrain,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        viewModel = viewModel,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BrainScreenContent(
    sagaId: String,
    characterId: Int?,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onOpenCharacterBrain: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: BrainViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val genre = state.genre
    val accent = MaterialTheme.colorScheme.primary

    val scaffoldState =
        rememberBottomSheetScaffoldState(
            bottomSheetState =
                rememberStandardBottomSheetState(
                    initialValue = SheetValue.PartiallyExpanded,
                    skipHiddenState = true,
                ),
        )

    BackHandler(onBack = onBack)

    LaunchedEffect(sagaId, characterId) {
        if (characterId != null) {
            viewModel.loadCharacterBrain(sagaId.toInt(), characterId)
        } else {
            viewModel.loadStoryBrain(sagaId.toInt())
        }
    }

    val ambientAlpha by animateFloatAsState(
        targetValue = if (state.isLoading) 1f else .2f,
        animationSpec = tween(durationMillis = 700),
        label = "brainAmbientAlpha",
    )

    with(sharedTransitionScope) {
        SagAITheme(genre = genre, darkTheme = true) {
            val graph = state.graph
            val layout = state.layout
            var recenterNonce by remember { mutableIntStateOf(0) }
            val isStoryMode = graph?.mode == BrainMode.STORY
            val pagerNodes =
                if (isStoryMode) state.storyPath else state.orbitNodes
            val selectedNode = viewModel.selectedNode()

            val navBarBottom =
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val collapsedPillHeight = 50.dp
            val isSheetExpanded by remember {
                derivedStateOf {
                    scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
                }
            }
            val sheetScope = rememberCoroutineScope()
            val sheetPeek by animateDpAsState(
                if (state.isLoading) {
                    0.dp
                } else {
                    collapsedPillHeight + navBarBottom + 12.dp
                },
                label = "brainSheetPeek",
            )

            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = sheetPeek,
                sheetDragHandle = null,
                containerColor = MaterialTheme.colorScheme.background,
                sheetContainerColor = Color.Transparent,
                sheetTonalElevation = 0.dp,
                sheetShadowElevation = 0.dp,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .statusBarsPadding(),
                sheetContent = {
                    val animatedPadding by animateDpAsState(
                        if (isSheetExpanded) 8.dp else 32.dp,
                    )
                    if (graph == null || layout == null) return@BottomSheetScaffold
                    BrainDetailSheet(
                        displayNode = selectedNode,
                        pagerNodes = pagerNodes,
                        selectedNodeId = state.selectedNodeId,
                        sceneFocusId = viewModel.sceneFocusId(),
                        sheetState = scaffoldState.bottomSheetState,
                        onPagerNodeSelected =
                            if (isStoryMode) {
                                viewModel::selectStoryPathNode
                            } else {
                                viewModel::selectOrbitNode
                            },
                        onRecenter = {
                            recenterNonce++
                            viewModel.recenter()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .padding(animatedPadding)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.shapes.large,
                                ).background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    MaterialTheme.shapes.large,
                                ),
                    )
                },
                topBar = {
                    SagaTopBar(
                        title = title,
                        genre = genre,
                        onBackClick = onBack,
                        actionContent = { Box(Modifier.fillMaxWidth()) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        titleModifier =
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = "brain_${sagaId}_title"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                    )
                },
            ) { innerPadding ->

                Box(
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                ) {
                    StarryTextPlaceholder(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .alpha(ambientAlpha),
                        starCount = 150,
                        twinkleDurationMillis = 5000,
                    )

                    AnimatedContent(graph != null && layout != null) {
                        if (it) {
                            if (graph != null && layout != null) {
                                BrainCanvas(
                                    graph = graph,
                                    layout = layout,
                                    selectedNodeId = state.selectedNodeId,
                                    visibleNodeIds = viewModel.visibleNodeIds(),
                                    spineEdgeIds = viewModel.spineEdgeIds(),
                                    satelliteNodeIds = viewModel.satelliteNodeIds(),
                                    recenterNonce = recenterNonce,
                                    modifier = Modifier.fillMaxSize(),
                                    genrePrimary = accent,
                                    genreSecondary = MaterialTheme.colorScheme.secondary,
                                    onNodeSelected = viewModel::focusNode,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
