package com.ilustris.sagai.features.brain.domain

import com.ilustris.sagai.features.brain.domain.index.BrainIndexBuilder
import com.ilustris.sagai.features.brain.domain.index.StoryBrainIndex
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainLayoutResult
import com.ilustris.sagai.features.brain.domain.model.BrainScene
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BrainContent(
    val sagaContent: SagaContent,
    val graph: BrainGraph,
    val layout: BrainLayoutResult,
    val index: StoryBrainIndex,
    val presence: BrainPresenceIndex,
    val scene: BrainScene,
)

class BrainUseCase
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val graphMapper: BrainGraphMapper,
        private val layoutEngine: ConstellationLayoutEngine,
        private val indexBuilder: BrainIndexBuilder,
        private val presenceIndexBuilder: BrainPresenceIndexBuilder,
        private val storyNavigation: BrainStoryNavigation,
    ) {
        suspend fun loadStoryBrain(sagaId: Int): BrainContent? =
            withContext(Dispatchers.Default) {
                val sagaContent = sagaRepository.getSagaById(sagaId).first() ?: return@withContext null
                val graph = graphMapper.mapStoryBrain(sagaContent)
                val presence = presenceIndexBuilder.build(sagaContent)
                val scene = storyNavigation.resolveScene(graph.centerNodeId, graph, presence)
                val layout = layoutEngine.layoutScene(graph, scene)
                val index = indexBuilder.build(sagaContent, graph)
                BrainContent(sagaContent, graph, layout, index, presence, scene)
            }

        suspend fun loadCharacterBrain(
            sagaId: Int,
            characterId: Int,
        ): BrainContent? =
            withContext(Dispatchers.Default) {
                val sagaContent = sagaRepository.getSagaById(sagaId).first() ?: return@withContext null
                val graph = graphMapper.mapCharacterBrain(sagaContent, characterId)
                val presence = presenceIndexBuilder.build(sagaContent)
                val scene = storyNavigation.resolveCharacterScene(graph.centerNodeId, graph)
                val layout = layoutEngine.layoutScene(graph, scene)
                val index = indexBuilder.build(sagaContent, graph)
                BrainContent(sagaContent, graph, layout, index, presence, scene)
            }

        suspend fun loadMiniPreview(sagaId: Int): BrainContent? =
            withContext(Dispatchers.Default) {
                val sagaContent = sagaRepository.getSagaById(sagaId).first() ?: return@withContext null
                val graph = graphMapper.mapMiniPreview(sagaContent)
                val layout = layoutEngine.layout(graph, spacing = 60f)
                val index = indexBuilder.build(sagaContent, graph)
                val presence = presenceIndexBuilder.build(sagaContent)
                val scene = storyNavigation.resolveScene(graph.centerNodeId, graph, presence)
                BrainContent(sagaContent, graph, layout, index, presence, scene)
            }
    }
