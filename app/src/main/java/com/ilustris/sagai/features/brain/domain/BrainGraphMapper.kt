package com.ilustris.sagai.features.brain.domain

import android.content.Context
import com.ilustris.sagai.R
import com.ilustris.sagai.features.brain.domain.model.BrainEdge
import com.ilustris.sagai.features.brain.domain.model.BrainEdgeType
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainMode
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeIds
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BrainGraphMapper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val inProgressBody: String
            get() = context.getString(R.string.saga_brain_stage_in_progress_body)

        fun mapStoryBrain(sagaContent: SagaContent): BrainGraph {
            val saga = sagaContent.data
            val genre = saga.genre
            val nodes = mutableListOf<BrainNode>()
            val edges = mutableListOf<BrainEdge>()
            val connectionMap = mutableMapOf<String, MutableSet<String>>()

            fun connect(
                a: String,
                b: String,
                type: BrainEdgeType,
                label: String? = null,
            ) {
                if (a == b) return
                connectionMap.getOrPut(a) { mutableSetOf() }.add(b)
                connectionMap.getOrPut(b) { mutableSetOf() }.add(a)
                edges +=
                    BrainEdge(
                        id = "${a}_${b}_${type.name}",
                        fromId = a,
                        toId = b,
                        type = type,
                        label = label,
                    )
            }

            val sagaNodeId = BrainNodeIds.saga(saga.id)
            val currentEventId = sagaContent.getCurrentTimeLine()?.data?.id
            nodes +=
                BrainNode(
                    id = sagaNodeId,
                    type = BrainNodeType.SAGA,
                    label = saga.title.ifBlank { "Saga" },
                    subtitle = "",
                    entityId = saga.id,
                    importance = 1f,
                    glowColorArgb = BrainNodeGlow.saga(genre),
                    detailBody = saga.description,
                )

            val messageCounts =
                sagaContent
                    .flatEvents()
                    .flatMap { it.messages }
                    .groupingBy { it.message.characterId }
                    .eachCount()

            sagaContent.characters.forEach { character ->
                val charId = BrainNodeIds.character(character.data.id)
                val isMain = character.data.id == sagaContent.mainCharacter?.data?.id
                val msgCount = messageCounts[character.data.id] ?: 0
                val importance = (0.4f + (msgCount.coerceAtMost(50) / 50f) * 0.5f).coerceAtMost(0.95f)
                if (isMain) {
                    nodes.find { it.id == sagaNodeId }?.let { /* saga stays center */ }
                }
                nodes +=
                    BrainNode(
                        id = charId,
                        type = BrainNodeType.CHARACTER,
                        label = character.data.fullName(),
                        subtitle =
                            character.data.profile.occupation
                                .orEmpty(),
                        entityId = character.data.id,
                        importance = if (isMain) 0.95f else importance,
                        glowColorArgb = BrainNodeGlow.character(character.data.hexColor, isMain),
                        detailBody = character.data.backstory,
                        characterId = character.data.id,
                    )
                connect(sagaNodeId, charId, BrainEdgeType.CONTAINS)

                character.data.firstSceneId?.let { debutId ->
                    connect(charId, BrainNodeIds.event(debutId), BrainEdgeType.DEBUT, "debut")
                }
            }

            sagaContent.acts.forEachIndexed { actIndex, act ->
                val actNumber = actIndex + 1
                val actIncomplete =
                    act.data.title.isBlank() && act.data.content.isBlank()
                val actLabel =
                    if (actIncomplete) {
                        context.getString(R.string.saga_brain_act_in_progress, actNumber)
                    } else {
                        act.data.title.ifBlank {
                            context.getString(
                                R.string.saga_brain_act_in_progress,
                                actNumber,
                            )
                        }
                    }
                val actBody =
                    if (actIncomplete) {
                        inProgressBody
                    } else {
                        act.data.content
                    }
                val actId = BrainNodeIds.act(act.data.id)
                nodes +=
                    BrainNode(
                        id = actId,
                        type = BrainNodeType.ACT,
                        label = actLabel,
                        subtitle = "",
                        entityId = act.data.id,
                        importance = 0.7f,
                        glowColorArgb = BrainNodeGlow.actOrChapter(genre),
                        detailBody = actBody,
                    )
                connect(sagaNodeId, actId, BrainEdgeType.CONTAINS)

                act.chapters.forEachIndexed { chapterIndex, chapter ->
                    val chapterNumber = chapterIndex + 1
                    val chapterIncomplete =
                        chapter.data.title.isBlank() && chapter.data.content.isBlank()
                    val chapterLabel =
                        if (chapterIncomplete) {
                            context.getString(R.string.saga_brain_chapter_in_progress, chapterNumber)
                        } else {
                            chapter.data.title.ifBlank {
                                context.getString(
                                    R.string.saga_brain_chapter_in_progress,
                                    chapterNumber,
                                )
                            }
                        }
                    val chapterBody =
                        if (chapterIncomplete) {
                            inProgressBody
                        } else {
                            chapter.data.content
                        }
                    val chapterId = BrainNodeIds.chapter(chapter.data.id)
                    nodes +=
                        BrainNode(
                            id = chapterId,
                            type = BrainNodeType.CHAPTER,
                            label = chapterLabel,
                            subtitle = "",
                            entityId = chapter.data.id,
                            importance = 0.55f,
                            glowColorArgb = BrainNodeGlow.actOrChapter(genre),
                            detailBody = chapterBody,
                        )
                    connect(actId, chapterId, BrainEdgeType.CONTAINS)

                    chapter.data.featuredCharacters.forEach { charEntityId ->
                        connect(
                            chapterId,
                            BrainNodeIds.character(charEntityId),
                            BrainEdgeType.FEATURED_IN,
                        )
                    }

                    chapter.events.forEachIndexed { eventIndex, event ->
                        val eventNumber = eventIndex + 1
                        val eventId = BrainNodeIds.event(event.data.id)
                        val isCurrent = event.data.id == currentEventId
                        val eventIncomplete =
                            event.data.title.isBlank() && event.data.content.isBlank()
                        val eventBody =
                            if (eventIncomplete) {
                                inProgressBody
                            } else {
                                event.data.content
                            }
                        val eventLabel =
                            if (eventIncomplete) {
                                context.getString(R.string.saga_brain_event_in_progress, eventNumber)
                            } else {
                                event.data.title.ifBlank {
                                    context.getString(
                                        R.string.saga_brain_event_in_progress,
                                        eventNumber,
                                    )
                                }
                            }
                        nodes +=
                            BrainNode(
                                id = eventId,
                                type = BrainNodeType.EVENT,
                                label = eventLabel,
                                subtitle = "",
                                entityId = event.data.id,
                                importance = if (isCurrent) 0.9f else 0.45f,
                                glowColorArgb = BrainNodeGlow.event(if (isCurrent) 1f else 0.7f),
                                detailBody = eventBody,
                            )
                        connect(chapterId, eventId, BrainEdgeType.CONTAINS)

                        event.characterEventDetails.forEach { charEvent ->
                            connect(
                                BrainNodeIds.character(charEvent.character.id),
                                eventId,
                                BrainEdgeType.PARTICIPATED,
                                charEvent.event.title,
                            )
                        }
                    }
                }
            }

            sagaContent.wikis.forEach { wiki ->
                val wikiId = BrainNodeIds.wiki(wiki.id)
                nodes +=
                    BrainNode(
                        id = wikiId,
                        type = BrainNodeType.WIKI,
                        label = wiki.title,
                        subtitle = "",
                        entityId = wiki.id,
                        importance = 0.35f,
                        glowColorArgb = BrainNodeGlow.wiki(wiki.type),
                        detailBody = wiki.content,
                    )
                connect(sagaNodeId, wikiId, BrainEdgeType.LORE_LINK)
                wiki.timelineId?.let { timelineId ->
                    connect(wikiId, BrainNodeIds.event(timelineId), BrainEdgeType.LORE_LINK)
                }
                wiki.chapterId?.let { chapterId ->
                    connect(wikiId, BrainNodeIds.chapter(chapterId), BrainEdgeType.LORE_LINK)
                }
            }

            sagaContent.relationships.forEach { relation ->
                val charOne = BrainNodeIds.character(relation.data.characterOneId)
                val charTwo = BrainNodeIds.character(relation.data.characterTwoId)
                connect(charOne, charTwo, BrainEdgeType.RELATED_TO, relation.data.title)
            }

            val finalNodes =
                nodes.map { node ->
                    node.copy(connectedNodeIds = connectionMap[node.id]?.toList().orEmpty())
                }

            val fullGraph =
                BrainGraph(
                    nodes = finalNodes.distinctBy { it.id },
                    edges = edges.distinctBy { it.id },
                    mode = BrainMode.STORY,
                    centerNodeId = sagaNodeId,
                    sagaId = saga.id,
                )
            return simplifyStoryGraph(fullGraph)
        }

        fun mapCharacterBrain(
            sagaContent: SagaContent,
            characterId: Int,
        ): BrainGraph {
            val character =
                sagaContent.characters.find { it.data.id == characterId }
                    ?: return mapStoryBrain(sagaContent).copy(
                        mode = BrainMode.CHARACTER,
                        focusCharacterId = characterId,
                    )

            val genre = sagaContent.data.genre
            val nodes = mutableListOf<BrainNode>()
            val edges = mutableListOf<BrainEdge>()
            val connectionMap = mutableMapOf<String, MutableSet<String>>()

            fun connect(
                a: String,
                b: String,
                type: BrainEdgeType,
                label: String? = null,
            ) {
                if (a == b) return
                connectionMap.getOrPut(a) { mutableSetOf() }.add(b)
                connectionMap.getOrPut(b) { mutableSetOf() }.add(a)
                edges +=
                    BrainEdge(
                        id = "${a}_${b}_${type.name}",
                        fromId = a,
                        toId = b,
                        type = type,
                        label = label,
                    )
            }

            val centerId = BrainNodeIds.character(character.data.id)
            val isMain = character.data.id == sagaContent.mainCharacter?.data?.id
            nodes +=
                BrainNode(
                    id = centerId,
                    type = BrainNodeType.CHARACTER,
                    label = character.data.fullName(),
                    subtitle =
                        character.data.profile.occupation
                            .orEmpty(),
                    entityId = character.data.id,
                    importance = 1f,
                    glowColorArgb = BrainNodeGlow.character(character.data.hexColor, isMain),
                    detailBody = character.data.backstory,
                    characterId = character.data.id,
                )

            character.relationships.forEach { relation ->
                val other =
                    if (relation.characterOne.id == character.data.id) {
                        relation.characterTwo
                    } else {
                        relation.characterOne
                    }
                val otherId = BrainNodeIds.character(other.id)
                val relationId = BrainNodeIds.relation(relation.data.id)
                if (nodes.none { it.id == otherId }) {
                    nodes +=
                        BrainNode(
                            id = otherId,
                            type = BrainNodeType.CHARACTER,
                            label = other.fullName(),
                            subtitle = other.profile.occupation.orEmpty(),
                            entityId = other.id,
                            importance = 0.75f,
                            glowColorArgb = BrainNodeGlow.character(other.hexColor, false),
                            detailBody = other.backstory,
                            characterId = other.id,
                        )
                }
                if (nodes.none { it.id == relationId }) {
                    nodes +=
                        BrainNode(
                            id = relationId,
                            type = BrainNodeType.RELATION,
                            label = relation.data.title,
                            subtitle = other.fullName(),
                            entityId = relation.data.id,
                            importance = 0.85f,
                            glowColorArgb = BrainNodeGlow.relation(genre),
                            detailBody = relation.data.description,
                        )
                }
                connect(centerId, relationId, BrainEdgeType.RELATED_TO, relation.data.title)
                connect(relationId, otherId, BrainEdgeType.RELATED_TO, relation.data.title)
            }

            val timelineEvents = sagaContent.flatEvents().map { it.data }
            character.sortEventsByTimeline(timelineEvents).forEachIndexed { eventIndex, eventDetail ->
                val eventNumber = eventIndex + 1
                val timeline = eventDetail.timeline ?: return@forEachIndexed
                val charEvent = eventDetail.event
                val charEventId = BrainNodeIds.characterEvent(charEvent.id)
                val storyEventId = BrainNodeIds.event(timeline.id)
                val eventIncomplete =
                    charEvent.title.isBlank() &&
                        timeline.title.isBlank() &&
                        charEvent.summary.isBlank() &&
                        timeline.content.isBlank()
                val eventBody =
                    if (eventIncomplete) {
                        inProgressBody
                    } else {
                        charEvent.summary.ifBlank { timeline.content }
                    }
                val charEventLabel =
                    if (eventIncomplete) {
                        context.getString(R.string.saga_brain_event_in_progress, eventNumber)
                    } else {
                        charEvent.title.ifBlank { timeline.title }.ifBlank {
                            context.getString(R.string.saga_brain_event_in_progress, eventNumber)
                        }
                    }
                nodes +=
                    BrainNode(
                        id = charEventId,
                        type = BrainNodeType.CHARACTER_EVENT,
                        label = charEventLabel,
                        subtitle = "",
                        entityId = charEvent.id,
                        importance = 0.65f,
                        glowColorArgb = BrainNodeGlow.characterEvent(),
                        detailBody = if (eventIncomplete) inProgressBody else eventBody,
                    )
                if (nodes.none { it.id == storyEventId }) {
                    val storyLabel =
                        timeline.title.ifBlank {
                            context.getString(R.string.saga_brain_event_in_progress, eventNumber)
                        }
                    nodes +=
                        BrainNode(
                            id = storyEventId,
                            type = BrainNodeType.EVENT,
                            label = storyLabel,
                            subtitle = "",
                            entityId = timeline.id,
                            importance = 0.55f,
                            glowColorArgb = BrainNodeGlow.event(),
                            detailBody = timeline.content,
                        )
                }
                connect(centerId, charEventId, BrainEdgeType.PARTICIPATED, charEvent.title)
                connect(charEventId, storyEventId, BrainEdgeType.FEATURED_IN, timeline.title)
            }

            character.data.firstSceneId?.let { debutId ->
                val debutNodeId = BrainNodeIds.event(debutId)
                if (nodes.none { it.id == debutNodeId }) {
                    sagaContent.findTimeline(debutId)?.let { event ->
                        val eventIncomplete =
                            event.data.title.isBlank() && event.data.content.isBlank()
                        val eventLabel =
                            if (eventIncomplete) {
                                context.getString(R.string.saga_brain_event_in_progress, 1)
                            } else {
                                event.data.title.ifBlank { "Debut" }
                            }
                        nodes +=
                            BrainNode(
                                id = debutNodeId,
                                type = BrainNodeType.EVENT,
                                label = eventLabel,
                                subtitle = "",
                                entityId = event.data.id,
                                importance = 0.8f,
                                glowColorArgb = BrainNodeGlow.event(1f),
                                detailBody = if (eventIncomplete) inProgressBody else event.data.content,
                            )
                    }
                }
                connect(centerId, debutNodeId, BrainEdgeType.DEBUT, "debut")
            }

            sagaContent.wikis
                .filter { wiki ->
                    character.events.any { it.timeline?.id == wiki.timelineId } ||
                        wiki.content.contains(character.data.name, ignoreCase = true)
                }.forEach { wiki ->
                    val wikiId = BrainNodeIds.wiki(wiki.id)
                    nodes +=
                        BrainNode(
                            id = wikiId,
                            type = BrainNodeType.WIKI,
                            label = wiki.title,
                            subtitle = "",
                            entityId = wiki.id,
                            importance = 0.4f,
                            glowColorArgb = BrainNodeGlow.wiki(wiki.type),
                            detailBody = wiki.content,
                        )
                    connect(centerId, wikiId, BrainEdgeType.LORE_LINK)
                }

            val finalNodes =
                nodes.map { node ->
                    node.copy(connectedNodeIds = connectionMap[node.id]?.toList().orEmpty())
                }

            return BrainGraph(
                nodes = finalNodes.distinctBy { it.id },
                edges = edges.distinctBy { it.id },
                mode = BrainMode.CHARACTER,
                centerNodeId = centerId,
                sagaId = sagaContent.data.id,
                focusCharacterId = characterId,
            )
        }

        fun mapMiniPreview(sagaContent: SagaContent): BrainGraph {
            val simplified = mapStoryBrain(sagaContent)
            val sagaNode =
                simplified.nodeById(BrainNodeIds.saga(sagaContent.data.id)) ?: return simplified
            val topCharacters =
                simplified.nodes
                    .filter { it.type == BrainNodeType.CHARACTER }
                    .sortedByDescending { it.importance }
                    .take(5)
            val recentEvents =
                simplified.nodes
                    .filter { it.type == BrainNodeType.EVENT }
                    .sortedByDescending { it.importance }
                    .take(3)
            val keepIds =
                (listOf(sagaNode.id) + topCharacters.map { it.id } + recentEvents.map { it.id }).toSet()
            return rebuildSubgraph(simplified, keepIds)
        }

        private fun simplifyStoryGraph(graph: BrainGraph): BrainGraph {
            val centerId = graph.centerNodeId
            val topCharacters =
                graph.nodes
                    .filter { it.type == BrainNodeType.CHARACTER }
                    .sortedByDescending { it.importance }
                    .take(8)
            val acts = graph.nodes.filter { it.type == BrainNodeType.ACT }
            val chapters = graph.nodes.filter { it.type == BrainNodeType.CHAPTER }
            val events = graph.nodes.filter { it.type == BrainNodeType.EVENT }
            val keepIds =
                (
                    setOf(centerId) +
                        topCharacters.map { it.id } +
                        acts.map { it.id } +
                        chapters.map { it.id } +
                        events.map { it.id }
                ).toMutableSet()
            val linkedWikis =
                graph.nodes
                    .filter { it.type == BrainNodeType.WIKI }
                    .filter { wiki ->
                        graph.edges.any { edge ->
                            val other =
                                if (edge.fromId == wiki.id) {
                                    edge.toId
                                } else if (edge.toId == wiki.id) {
                                    edge.fromId
                                } else {
                                    null
                                }
                            other != null && keepIds.contains(other)
                        }
                    }
            keepIds.addAll(linkedWikis.map { it.id })
            return rebuildSubgraph(graph, keepIds)
        }

        private fun rebuildSubgraph(
            graph: BrainGraph,
            keepIds: Set<String>,
        ): BrainGraph {
            val nodes = graph.nodes.filter { it.id in keepIds }
            val edges =
                graph.edges.filter { edge ->
                    edge.fromId in keepIds && edge.toId in keepIds
                }
            val connectionMap = mutableMapOf<String, MutableList<String>>()
            edges.forEach { edge ->
                connectionMap.getOrPut(edge.fromId) { mutableListOf() }.add(edge.toId)
                connectionMap.getOrPut(edge.toId) { mutableListOf() }.add(edge.fromId)
            }
            val finalNodes =
                nodes.map { node ->
                    node.copy(connectedNodeIds = connectionMap[node.id]?.distinct().orEmpty())
                }
            return graph.copy(nodes = finalNodes, edges = edges)
        }
    }

private fun CharacterContent.fullName() = data.fullName()
