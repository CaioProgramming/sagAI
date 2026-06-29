package com.ilustris.sagai.features.brain.domain.index

import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.findTimelineChapter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import javax.inject.Inject

data class SpineEntry(
    val actTitle: String,
    val chapterTitle: String,
    val eventTitle: String,
    val actId: Int,
    val chapterId: Int,
    val eventId: Int,
)

data class CharacterIndexEntry(
    val id: Int,
    val name: String,
    val role: String,
    val keyEvents: List<String>,
    val relations: List<String>,
)

data class LoreIndexEntry(
    val title: String,
    val type: String,
    val linkedEventId: Int?,
)

data class FrontierEntry(
    val actTitle: String,
    val chapterTitle: String,
    val eventTitle: String,
)

data class StoryBrainIndex(
    val sagaId: Int,
    val narrativeSpine: List<SpineEntry>,
    val characterEntries: List<CharacterIndexEntry>,
    val loreEntries: List<LoreIndexEntry>,
    val connectionSummaries: List<String>,
    val currentFrontier: FrontierEntry?,
)

/**
 * Computed story index for future AI prompt enrichment.
 * Not wired to prompts in v1 — see [toPromptContext].
 */
class BrainIndexBuilder
    @Inject
    constructor() {
        fun build(
            sagaContent: SagaContent,
            graph: BrainGraph,
        ): StoryBrainIndex {
            val spine = buildSpine(sagaContent)
            val characters = buildCharacterEntries(sagaContent)
            val lore = buildLoreEntries(sagaContent)
            val connections = buildConnectionSummaries(graph)
            val frontier = buildFrontier(sagaContent)
            return StoryBrainIndex(
                sagaId = sagaContent.data.id,
                narrativeSpine = spine,
                characterEntries = characters,
                loreEntries = lore,
                connectionSummaries = connections,
                currentFrontier = frontier,
            )
        }
    }

fun StoryBrainIndex.toPromptContext(maxConnections: Int = 10): String =
    buildString {
        appendLine("=== NARRATIVE SPINE ===")
        narrativeSpine.takeLast(8).forEach { entry ->
            appendLine("${entry.actTitle} > ${entry.chapterTitle} > ${entry.eventTitle}")
        }
        currentFrontier?.let {
            appendLine("\n=== CURRENT FRONTIER ===")
            appendLine("${it.actTitle} > ${it.chapterTitle} > ${it.eventTitle}")
        }
        appendLine("\n=== KEY CONNECTIONS ===")
        connectionSummaries.take(maxConnections).forEach { appendLine("- $it") }
        appendLine("\n=== CHARACTERS ===")
        characterEntries.take(12).forEach { char ->
            appendLine("${char.name} (${char.role}): ${char.relations.take(2).joinToString("; ")}")
        }
    }

private fun buildSpine(sagaContent: SagaContent): List<SpineEntry> =
    sagaContent.acts.flatMap { act ->
        act.chapters.flatMap { chapter ->
            chapter.events.map { event ->
                SpineEntry(
                    actTitle = act.data.title,
                    chapterTitle = chapter.data.title,
                    eventTitle = event.data.title,
                    actId = act.data.id,
                    chapterId = chapter.data.id,
                    eventId = event.data.id,
                )
            }
        }
    }

private fun buildCharacterEntries(sagaContent: SagaContent): List<CharacterIndexEntry> =
    sagaContent.characters.map { character ->
        CharacterIndexEntry(
            id = character.data.id,
            name = character.data.fullName(),
            role = character.data.profile.occupation,
            keyEvents = character.events.takeLast(5).map { it.event.title },
            relations =
                character.relationships.map { relation ->
                    val other = relation.getCharacterExcluding(character.data)
                    "${other.name} (${relation.data.title})"
                },
        )
    }

private fun buildLoreEntries(sagaContent: SagaContent): List<LoreIndexEntry> =
    sagaContent.wikis.map { wiki ->
        LoreIndexEntry(
            title = wiki.title,
            type = wiki.type?.name.orEmpty(),
            linkedEventId = wiki.timelineId,
        )
    }

private fun buildConnectionSummaries(graph: BrainGraph): List<String> =
    graph.edges.mapNotNull { edge ->
        val from = graph.nodeById(edge.fromId)?.label ?: return@mapNotNull null
        val to = graph.nodeById(edge.toId)?.label ?: return@mapNotNull null
        val label = edge.label?.let { " ($it)" }.orEmpty()
        "$from — ${edge.type.name}$label → $to"
    }

private fun buildFrontier(sagaContent: SagaContent): FrontierEntry? {
    val current = sagaContent.getCurrentTimeLine() ?: return null
    val chapter = sagaContent.findTimelineChapter(current.data) ?: return null
    val act = sagaContent.findChapterAct(chapter.data) ?: return null
    return FrontierEntry(
        actTitle = act.data.title,
        chapterTitle = chapter.data.title,
        eventTitle = current.data.title,
    )
}
