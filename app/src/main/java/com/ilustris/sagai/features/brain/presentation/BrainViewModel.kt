package com.ilustris.sagai.features.brain.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.brain.domain.BrainContent
import com.ilustris.sagai.features.brain.domain.BrainPresenceIndex
import com.ilustris.sagai.features.brain.domain.BrainStoryNavigation
import com.ilustris.sagai.features.brain.domain.BrainUseCase
import com.ilustris.sagai.features.brain.domain.ConstellationLayoutEngine
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainLayoutResult
import com.ilustris.sagai.features.brain.domain.model.BrainMode
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.BrainScene
import com.ilustris.sagai.features.newsaga.data.model.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrainUiState(
    val isLoading: Boolean = true,
    val graph: BrainGraph? = null,
    val layout: BrainLayoutResult? = null,
    val selectedNodeId: String? = null,
    val scene: BrainScene? = null,
    val storyPath: List<BrainNode> = emptyList(),
    val orbitNodes: List<BrainNode> = emptyList(),
    val genre: Genre? = null,
)

@HiltViewModel
class BrainViewModel
    @Inject
    constructor(
        private val brainUseCase: BrainUseCase,
        private val layoutEngine: ConstellationLayoutEngine,
        private val storyNavigation: BrainStoryNavigation,
    ) : ViewModel() {
        private val _state = MutableStateFlow(BrainUiState())
        val state = _state.asStateFlow()

        private var presence: BrainPresenceIndex? = null

        fun loadStoryBrain(sagaId: Int) {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true)
                val content = brainUseCase.loadStoryBrain(sagaId)
                applyContent(content)
            }
        }

        fun loadCharacterBrain(
            sagaId: Int,
            characterId: Int,
        ) {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true)
                val content = brainUseCase.loadCharacterBrain(sagaId, characterId)
                applyContent(content)
            }
        }

        fun loadMiniPreview(sagaId: Int) {
            viewModelScope.launch {
                val content = brainUseCase.loadMiniPreview(sagaId)
                applyContent(content, selectCenter = false)
            }
        }

        fun focusNode(nodeId: String) {
            val graph = _state.value.graph ?: return
            if (graph.mode == BrainMode.CHARACTER) {
                graph.nodeById(nodeId) ?: return
                val scene = storyNavigation.resolveCharacterScene(nodeId, graph)
                applyCharacterScene(
                    graph = graph,
                    scene = scene,
                    selectedNodeId = nodeId,
                )
                return
            }

            val currentPresence = presence ?: return
            val scene = _state.value.scene ?: return
            val node = graph.nodeById(nodeId) ?: return

            if (nodeId in scene.satelliteNodeIds) {
                _state.value = _state.value.copy(selectedNodeId = nodeId)
                return
            }

            if (node.type in structuralTypes) {
                applyStoryScene(
                    graph = graph,
                    scene = storyNavigation.resolveScene(nodeId, graph, currentPresence),
                    selectedNodeId = nodeId,
                )
            }
        }

        fun selectStoryPathNode(nodeId: String) {
            val graph = _state.value.graph ?: return
            if (graph.mode == BrainMode.CHARACTER) {
                selectOrbitNode(nodeId)
                return
            }
            focusNode(nodeId)
        }

        fun selectOrbitNode(nodeId: String) {
            val graph = _state.value.graph ?: return
            if (graph.mode == BrainMode.CHARACTER) {
                focusNode(nodeId)
                return
            }
            if (_state.value.orbitNodes.any { it.id == nodeId }) {
                _state.value = _state.value.copy(selectedNodeId = nodeId)
            }
        }

        fun recenter() {
            val graph = _state.value.graph ?: return
            if (graph.mode == BrainMode.CHARACTER) {
                focusNode(graph.centerNodeId)
                return
            }
            val currentPresence = presence ?: return
            applyStoryScene(
                graph = graph,
                scene = storyNavigation.resolveScene(graph.centerNodeId, graph, currentPresence),
                selectedNodeId = graph.centerNodeId,
            )
        }

        fun selectedNode(): BrainNode? {
            val id = _state.value.selectedNodeId ?: return null
            return _state.value.graph?.nodeById(id)
        }

        fun visibleNodeIds(): Set<String> {
            val graph = _state.value.graph ?: return emptySet()
            return _state.value.scene
                ?.visibleNodeIds
                .orEmpty()
        }

        fun spineEdgeIds(): Set<String> =
            _state.value.scene
                ?.spineEdgeIds
                .orEmpty()

        fun satelliteNodeIds(): Set<String> =
            _state.value.scene
                ?.satelliteNodeIds
                .orEmpty()

        fun sceneFocusId(): String? = _state.value.scene?.focusNodeId

        fun isStoryMode(): Boolean = _state.value.graph?.mode == BrainMode.STORY

        private fun applyStoryScene(
            graph: BrainGraph,
            scene: BrainScene,
            selectedNodeId: String,
        ) {
            val layout = layoutEngine.layoutScene(graph, scene)
            _state.value =
                _state.value.copy(
                    scene = scene,
                    layout = layout,
                    selectedNodeId = selectedNodeId,
                    storyPath = storyPathNodes(graph, scene.storyPath),
                )
        }

        private fun applyCharacterScene(
            graph: BrainGraph,
            scene: BrainScene,
            selectedNodeId: String,
        ) {
            val layout = layoutEngine.layoutScene(graph, scene)
            _state.value =
                _state.value.copy(
                    scene = scene,
                    layout = layout,
                    selectedNodeId = selectedNodeId,
                    orbitNodes = graph.orbitNodes(selectedNodeId),
                )
        }

        private fun applyContent(
            content: BrainContent?,
            selectCenter: Boolean = true,
        ) {
            if (content == null) {
                presence = null
                _state.value = BrainUiState(isLoading = false)
                return
            }
            presence = content.presence
            val centerId = content.graph.centerNodeId
            val selectedId = if (selectCenter) centerId else _state.value.selectedNodeId ?: centerId

            if (content.graph.mode == BrainMode.CHARACTER) {
                _state.value =
                    BrainUiState(
                        isLoading = false,
                        graph = content.graph,
                        layout = content.layout,
                        selectedNodeId = selectedId,
                        orbitNodes = content.graph.orbitNodes(selectedId),
                        scene = content.scene,
                        genre = content.sagaContent.data.genre,
                    )
            } else {
                val scene = content.scene
                _state.value =
                    BrainUiState(
                        isLoading = false,
                        graph = content.graph,
                        layout = content.layout,
                        selectedNodeId = selectedId,
                        scene = scene,
                        storyPath = storyPathNodes(content.graph, scene.storyPath),
                        genre = content.sagaContent.data.genre,
                    )
            }
        }

        private fun storyPathNodes(
            graph: BrainGraph,
            path: List<String>,
        ): List<BrainNode> = path.mapNotNull { graph.nodeById(it) }

        companion object {
            private val structuralTypes =
                setOf(
                    BrainNodeType.SAGA,
                    BrainNodeType.ACT,
                    BrainNodeType.CHAPTER,
                    BrainNodeType.EVENT,
                )
        }
    }
