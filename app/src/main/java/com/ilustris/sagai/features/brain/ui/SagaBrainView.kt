package com.ilustris.sagai.features.brain.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.brain.domain.model.BrainMode
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.presentation.BrainViewModel
import com.ilustris.sagai.features.brain.ui.components.BrainGlobeCanvas
import com.ilustris.sagai.features.brain.ui.components.BrainGlobeCard
import com.ilustris.sagai.ui.animations.ConstellationCanvas
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

@OptIn(ExperimentalSharedTransitionApi::class)
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
            var selectedId by remember(graph) { mutableStateOf<String?>(null) }
            val selectedNode = selectedId?.let { graph?.nodeById(it) }

            BackHandler {
                if (selectedNode != null) selectedId = null else onBack()
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
            ) {
                ConstellationCanvas(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .alpha(ambientAlpha),
                    starCount = 90 + state.chapterKnowledgeClusters.sumOf { it.satelliteCount },
                    knowledgeColor = accent.copy(alpha = 0.85f),
                    chapterClusters = state.chapterKnowledgeClusters,
                )

                if (graph != null) {
                    BrainGlobeCanvas(
                        graph = graph,
                        selectedNodeId = selectedId,
                        onNodeSelected = { selectedId = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SagaTopBar(
                        title = title,
                        genre = genre,
                        onBackClick = onBack,
                        actionContent = { Box(Modifier.fillMaxWidth()) },
                        modifier = Modifier.fillMaxWidth(),
                        titleModifier =
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = "brain_${sagaId}_title"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                    )
                    if (graph != null) {
                        val unlinked =
                            remember(graph) {
                                val linked = HashSet<String>()
                                graph.edges.forEach {
                                    linked.add(it.fromId)
                                    linked.add(it.toId)
                                }
                                graph.nodes.count { it.id !in linked }
                            }
                        Text(
                            text = stringResource(R.string.saga_globe_stats, graph.nodes.size, graph.edges.size, unlinked),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        )
                    }
                }

                if (graph != null) {
                    AnimatedVisibility(
                        visible = selectedNode != null,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding(),
                    ) {
                        // Keep showing the last node while the card animates out.
                        val shown = remember { mutableStateOf<BrainNode?>(null) }
                        if (selectedNode != null) shown.value = selectedNode
                        shown.value?.let { node ->
                            BrainGlobeCard(
                                node = node,
                                connections =
                                    graph
                                        .neighborsOf(node.id)
                                        .sortedByDescending { it.importance },
                                onClose = { selectedId = null },
                                onConnectionClick = { selectedId = it },
                                onOpenCharacter =
                                    if (node.type == BrainNodeType.CHARACTER && graph.mode == BrainMode.STORY) {
                                        { onOpenCharacterBrain(node.characterId ?: node.entityId) }
                                    } else {
                                        null
                                    },
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }

                    if (selectedNode == null) {
                        Text(
                            text = stringResource(R.string.saga_globe_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(horizontal = 32.dp, vertical = 24.dp),
                        )
                    }
                }
            }
        }
    }
}
