package com.ilustris.sagai.features.brain.domain

import com.ilustris.sagai.features.brain.domain.model.BrainEdgeType
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainNodeIds
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.BrainScene
import javax.inject.Inject

class BrainStoryNavigation
    @Inject
    constructor() {
        fun buildStoryPath(
            graph: BrainGraph,
            presence: BrainPresenceIndex,
        ): List<String> {
            val path = mutableListOf<String>()
            if (graph.nodeById(graph.centerNodeId) != null) {
                path.add(graph.centerNodeId)
            }
            presence.actIdsInOrder.forEach { actId ->
                val actNodeId = BrainNodeIds.act(actId)
                if (graph.nodeById(actNodeId) == null) return@forEach
                path.add(actNodeId)
                presence.chapterIdsByAct[actId].orEmpty().forEach { chapterId ->
                    val chapterNodeId = BrainNodeIds.chapter(chapterId)
                    if (graph.nodeById(chapterNodeId) == null) return@forEach
                    path.add(chapterNodeId)
                    presence.eventIdsByChapter[chapterId].orEmpty().forEach { eventId ->
                        val eventNodeId = BrainNodeIds.event(eventId)
                        if (graph.nodeById(eventNodeId) != null) {
                            path.add(eventNodeId)
                        }
                    }
                }
            }
            return path.distinct()
        }

        fun resolveScene(
            focusNodeId: String,
            graph: BrainGraph,
            presence: BrainPresenceIndex,
        ): BrainScene {
            val storyPath = buildStoryPath(graph, presence)
            val focus = graph.nodeById(focusNodeId) ?: return emptyScene(graph.centerNodeId, storyPath)

            return when (focus.type) {
                BrainNodeType.SAGA -> sagaScene(graph, presence, storyPath)
                BrainNodeType.ACT -> actScene(focusNodeId, graph, presence, storyPath)
                BrainNodeType.CHAPTER -> chapterScene(focusNodeId, graph, presence, storyPath)
                BrainNodeType.EVENT -> eventScene(focusNodeId, graph, presence, storyPath)
                BrainNodeType.CHARACTER -> characterLensScene(focusNodeId, graph, storyPath)
                BrainNodeType.RELATION -> relationBridgeScene(focusNodeId, graph, storyPath)
                BrainNodeType.WIKI -> wikiAmbientScene(focusNodeId, graph, presence, storyPath)
            }
        }

        fun resolveCharacterLens(
            characterNodeId: String,
            graph: BrainGraph,
            presence: BrainPresenceIndex,
        ): BrainScene =
            resolveCharacterScene(characterNodeId, graph).copy(
                storyPath = buildStoryPath(graph, presence),
            )

        fun resolveCharacterScene(
            focusNodeId: String,
            graph: BrainGraph,
        ): BrainScene {
            val focus = graph.nodeById(focusNodeId) ?: return emptyCharacterScene(graph.centerNodeId)
            return when (focus.type) {
                BrainNodeType.RELATION -> relationBridgeScene(focusNodeId, graph)
                BrainNodeType.CHARACTER -> characterCenterScene(focusNodeId, graph)
                BrainNodeType.EVENT, BrainNodeType.WIKI -> leafCharacterScene(focusNodeId, graph)
                else -> characterCenterScene(graph.centerNodeId, graph)
            }
        }

        private fun sagaScene(
            graph: BrainGraph,
            presence: BrainPresenceIndex,
            storyPath: List<String>,
        ): BrainScene {
            val sagaId = graph.centerNodeId
            val actIds =
                presence
                    .actWindow(maxActs = 3)
                    .map { BrainNodeIds.act(it) }
                    .filter { graph.nodeById(it) != null }
            val structural = (setOf(sagaId) + actIds).toSet()
            val spine =
                spineForNodes(
                    graph,
                    structural,
                    sagaId,
                )
            return BrainScene(
                focusNodeId = sagaId,
                structuralNodeIds = structural,
                satelliteNodeIds = emptySet(),
                spineEdgeIds = spine,
                storyPath = storyPath,
            )
        }

        private fun actScene(
            actNodeId: String,
            graph: BrainGraph,
            presence: BrainPresenceIndex,
            storyPath: List<String>,
        ): BrainScene {
            val act = graph.nodeById(actNodeId) ?: return emptyScene(graph.centerNodeId, storyPath)
            val chapterIds =
                presence.chapterIdsByAct[act.entityId]
                    .orEmpty()
                    .map { BrainNodeIds.chapter(it) }
                    .filter { graph.nodeById(it) != null }
            val structural = (setOf(actNodeId) + chapterIds).toSet()
            val presenceScope = presence.byAct[act.entityId]
            val satellites = satelliteIds(graph, presenceScope)
            val spine =
                spineForNodes(
                    graph,
                    structural,
                    actNodeId,
                ) + characterEventSpineEdges(graph, actNodeId, structural, satellites)
            return BrainScene(
                focusNodeId = actNodeId,
                structuralNodeIds = structural,
                satelliteNodeIds = satellites,
                spineEdgeIds = spine,
                storyPath = storyPath,
            )
        }

        private fun chapterScene(
            chapterNodeId: String,
            graph: BrainGraph,
            presence: BrainPresenceIndex,
            storyPath: List<String>,
        ): BrainScene {
            val chapter =
                graph.nodeById(chapterNodeId) ?: return emptyScene(graph.centerNodeId, storyPath)
            val eventIds =
                presence.eventIdsByChapter[chapter.entityId]
                    .orEmpty()
                    .map { BrainNodeIds.event(it) }
                    .filter { graph.nodeById(it) != null }
            val parentAct =
                graph.edges
                    .firstOrNull { it.type == BrainEdgeType.CONTAINS && it.toId == chapterNodeId }
                    ?.fromId
            val structural = (setOf(chapterNodeId) + eventIds + setOfNotNull(parentAct)).toSet()
            val satellites = satelliteIds(graph, presence.byChapter[chapter.entityId])
            val structuralScope = structural + ancestors(graph, chapterNodeId)
            val spine =
                spineForNodes(graph, structuralScope, chapterNodeId) +
                    characterEventSpineEdges(graph, chapterNodeId, structural, satellites)
            return BrainScene(
                focusNodeId = chapterNodeId,
                structuralNodeIds = structural,
                satelliteNodeIds = satellites,
                spineEdgeIds = spine,
                storyPath = storyPath,
            )
        }

        private fun eventScene(
            eventNodeId: String,
            graph: BrainGraph,
            presence: BrainPresenceIndex,
            storyPath: List<String>,
        ): BrainScene {
            val event = graph.nodeById(eventNodeId) ?: return emptyScene(graph.centerNodeId, storyPath)
            val parentChapter =
                graph.edges
                    .firstOrNull { it.type == BrainEdgeType.CONTAINS && it.toId == eventNodeId }
                    ?.fromId
            val structural =
                setOf(eventNodeId) + setOfNotNull(parentChapter) + ancestors(graph, parentChapter ?: "")
            val satellites = satelliteIds(graph, presence.byEvent[event.entityId])
            val structuralScope = structural + ancestors(graph, eventNodeId)
            val spine =
                spineForNodes(graph, structuralScope, eventNodeId) +
                    characterEventSpineEdges(graph, eventNodeId, structural, satellites)
            return BrainScene(
                focusNodeId = eventNodeId,
                structuralNodeIds = structural,
                satelliteNodeIds = satellites,
                spineEdgeIds = spine,
                storyPath = storyPath,
            )
        }

        private fun characterLensScene(
            characterNodeId: String,
            graph: BrainGraph,
            storyPath: List<String>,
        ): BrainScene =
            characterCenterScene(characterNodeId, graph).copy(
                storyPath = storyPath,
            )

        private fun characterCenterScene(
            focusId: String,
            graph: BrainGraph,
        ): BrainScene {
            val relationIds =
                graph
                    .neighborsOf(focusId)
                    .filter { it.type == BrainNodeType.RELATION }
                    .map { it.id }
                    .toSet()
            val linkedCharacterIds =
                relationIds
                    .flatMap { relId ->
                        graph
                            .neighborsOf(relId)
                            .filter { it.type == BrainNodeType.CHARACTER && it.id != focusId }
                            .map { it.id }
                    }.toSet()
            val eventIds =
                graph
                    .neighborsOf(focusId)
                    .filter { it.type == BrainNodeType.EVENT }
                    .map { it.id }
                    .toSet()
            val wikiIds =
                graph
                    .neighborsOf(focusId)
                    .filter { it.type == BrainNodeType.WIKI }
                    .map { it.id }
                    .toSet()

            val structural = setOf(focusId) + relationIds + eventIds
            val satellites = linkedCharacterIds + wikiIds

            val bridgeEdges =
                graph.edges
                    .filter { edge ->
                        edge.type == BrainEdgeType.RELATED_TO &&
                            (
                                (edge.fromId == focusId && edge.toId in relationIds) ||
                                    (edge.toId == focusId && edge.fromId in relationIds) ||
                                    (edge.fromId in relationIds && edge.toId in linkedCharacterIds) ||
                                    (edge.toId in relationIds && edge.fromId in linkedCharacterIds)
                            )
                    }.map { it.id }
                    .toSet()

            val charSpine =
                characterEventSpineEdges(
                    graph,
                    focusId,
                    structural,
                    satellites,
                )
            val lensEdges =
                charSpine +
                    graph.edges
                        .filter { edge ->
                            edge.fromId in structural + satellites + focusId &&
                                edge.toId in structural + satellites + focusId &&
                                edge.type != BrainEdgeType.CONTAINS &&
                                edge.id !in bridgeEdges &&
                                edge.id !in charSpine
                        }.sortedWith(
                            compareBy<com.ilustris.sagai.features.brain.domain.model.BrainEdge> {
                                graph.nodeById(it.fromId)?.type != BrainNodeType.EVENT
                            }.thenBy { graph.nodeById(it.fromId)?.entityId ?: 0 }
                                .thenBy { it.id },
                        ).map { it.id }
                        .toSet()

            return BrainScene(
                focusNodeId = focusId,
                structuralNodeIds = structural.ifEmpty { setOf(focusId) },
                satelliteNodeIds = satellites,
                spineEdgeIds = bridgeEdges + lensEdges,
                storyPath = listOf(focusId),
                isCharacterLens = true,
            )
        }

        private fun relationBridgeScene(
            relationNodeId: String,
            graph: BrainGraph,
            storyPath: List<String> = listOf(relationNodeId),
        ): BrainScene {
            val characterIds =
                graph
                    .neighborsOf(relationNodeId)
                    .filter { it.type == BrainNodeType.CHARACTER }
                    .map { it.id }
                    .toSet()
            val structural = setOf(relationNodeId) + characterIds
            val spine =
                graph.edges
                    .filter { edge ->
                        edge.type == BrainEdgeType.RELATED_TO &&
                            (
                                (edge.fromId == relationNodeId && edge.toId in characterIds) ||
                                    (edge.toId == relationNodeId && edge.fromId in characterIds)
                            )
                    }.map { it.id }
                    .toSet()
            return BrainScene(
                focusNodeId = relationNodeId,
                structuralNodeIds = structural,
                satelliteNodeIds = emptySet(),
                spineEdgeIds = spine,
                storyPath = storyPath,
                isCharacterLens = true,
            )
        }

        private fun leafCharacterScene(
            focusId: String,
            graph: BrainGraph,
        ): BrainScene {
            val centerId = graph.centerNodeId
            val neighborIds =
                graph
                    .neighborsOf(focusId)
                    .filter { it.type in setOf(BrainNodeType.CHARACTER, BrainNodeType.RELATION) }
                    .map { it.id }
                    .toSet()
            val structural = setOf(focusId, centerId) + neighborIds
            val spine =
                graph.edges
                    .filter { edge ->
                        edge.fromId in structural &&
                            edge.toId in structural &&
                            edge.type != BrainEdgeType.CONTAINS
                    }.map { it.id }
                    .toSet()
            return BrainScene(
                focusNodeId = focusId,
                structuralNodeIds = structural,
                satelliteNodeIds = emptySet(),
                spineEdgeIds = spine,
                storyPath = listOf(focusId),
                isCharacterLens = true,
            )
        }

        private fun emptyCharacterScene(focusId: String) =
            BrainScene(
                focusNodeId = focusId,
                structuralNodeIds = setOf(focusId),
                satelliteNodeIds = emptySet(),
                spineEdgeIds = emptySet(),
                storyPath = listOf(focusId),
                isCharacterLens = true,
            )

        private fun wikiAmbientScene(
            wikiNodeId: String,
            graph: BrainGraph,
            presence: BrainPresenceIndex,
            storyPath: List<String>,
        ): BrainScene {
            val anchor =
                storyPath.lastOrNull { id ->
                    graph.nodeById(id)?.type in
                        setOf(
                            BrainNodeType.ACT,
                            BrainNodeType.CHAPTER,
                            BrainNodeType.EVENT,
                            BrainNodeType.SAGA,
                        )
                } ?: graph.centerNodeId
            return resolveScene(anchor, graph, presence).copy(
                focusNodeId = anchor,
            )
        }

        private fun satelliteIds(
            graph: BrainGraph,
            scope: BrainScopePresence?,
        ): Set<String> {
            if (scope == null) return emptySet()
            val chars =
                scope.characterIds.mapNotNull { id -> graph.nodeById(BrainNodeIds.character(id))?.id }
            val wikis = scope.wikiIds.mapNotNull { id -> graph.nodeById(BrainNodeIds.wiki(id))?.id }
            return (chars + wikis).toSet()
        }

        private fun ancestors(
            graph: BrainGraph,
            nodeId: String,
        ): Set<String> {
            if (nodeId.isEmpty()) return emptySet()
            val chain = mutableSetOf<String>()
            var current: String? = nodeId
            while (current != null) {
                val parent =
                    graph.edges
                        .firstOrNull { it.type == BrainEdgeType.CONTAINS && it.toId == current }
                        ?.fromId
                if (parent == null) break
                chain.add(parent)
                current = parent
            }
            return chain
        }

        private fun spineForNodes(
            graph: BrainGraph,
            nodeScope: Set<String>,
            focusNodeId: String,
        ): Set<String> {
            val chain = mutableListOf<String>()
            var current: String? = focusNodeId
            while (current != null) {
                chain.add(0, current)
                current =
                    graph.edges
                        .firstOrNull { it.type == BrainEdgeType.CONTAINS && it.toId == current }
                        ?.fromId
            }
            val spineEdges = mutableSetOf<String>()
            for (i in 0 until chain.lastIndex) {
                val from = chain[i]
                val to = chain[i + 1]
                graph.edges
                    .firstOrNull { edge ->
                        edge.type == BrainEdgeType.CONTAINS &&
                            edge.fromId == from &&
                            edge.toId == to
                    }?.let { spineEdges.add(it.id) }
            }
            graph.edges
                .filter { edge ->
                    edge.type == BrainEdgeType.CONTAINS &&
                        edge.fromId == focusNodeId &&
                        edge.toId in nodeScope
                }.forEach { spineEdges.add(it.id) }
            return spineEdges
        }

        private fun characterEventSpineEdges(
            graph: BrainGraph,
            focusNodeId: String,
            structuralNodeIds: Set<String>,
            satelliteNodeIds: Set<String>,
        ): Set<String> {
            val visible = structuralNodeIds + satelliteNodeIds + focusNodeId
            val orderedEvents =
                structuralNodeIds
                    .mapNotNull { graph.nodeById(it) }
                    .filter { it.type == BrainNodeType.EVENT }
                    .sortedBy { it.entityId }

            val edges = linkedSetOf<String>()

            graph.edges
                .filter { edge ->
                    edge.type in CHARACTER_LINK_TYPES &&
                        edge.fromId in visible &&
                        edge.toId in visible &&
                        (edge.fromId == focusNodeId || edge.toId == focusNodeId)
                }.sortedWith(
                    compareByDescending<com.ilustris.sagai.features.brain.domain.model.BrainEdge> { edge ->
                        val otherId = if (edge.fromId == focusNodeId) edge.toId else edge.fromId
                        graph.nodeById(otherId)?.importance ?: 0f
                    },
                ).forEach { edges.add(it.id) }

            orderedEvents.forEach { eventNode ->
                graph.edges
                    .filter { edge ->
                        edge.type == BrainEdgeType.PARTICIPATED &&
                            edge.fromId in visible &&
                            edge.toId in visible &&
                            (edge.fromId == eventNode.id || edge.toId == eventNode.id) &&
                            (
                                (edge.fromId in satelliteNodeIds && edge.toId == eventNode.id) ||
                                    (edge.toId in satelliteNodeIds && edge.fromId == eventNode.id)
                            )
                    }.sortedByDescending { edge ->
                        val charId = if (edge.fromId in satelliteNodeIds) edge.fromId else edge.toId
                        graph.nodeById(charId)?.importance ?: 0f
                    }.forEach { edges.add(it.id) }
            }

            return edges
        }

        private fun emptyScene(
            focusId: String,
            storyPath: List<String>,
        ) = BrainScene(
            focusNodeId = focusId,
            structuralNodeIds = setOf(focusId),
            satelliteNodeIds = emptySet(),
            spineEdgeIds = emptySet(),
            storyPath = storyPath,
        )

        fun orderedStructuralIds(scene: BrainScene): List<String> {
            val focus = scene.focusNodeId
            val rest = scene.structuralNodeIds.filter { it != focus }.sortedBy { it.hashCode() }
            return listOf(focus) + rest
        }

        fun orderedSatelliteIds(scene: BrainScene): List<String> = scene.satelliteNodeIds.sortedBy { it.hashCode() }

        private val CHARACTER_LINK_TYPES =
            setOf(
                BrainEdgeType.PARTICIPATED,
                BrainEdgeType.DEBUT,
                BrainEdgeType.FEATURED_IN,
            )
    }
