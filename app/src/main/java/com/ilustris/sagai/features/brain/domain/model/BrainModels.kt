package com.ilustris.sagai.features.brain.domain.model

import androidx.compose.ui.graphics.Color

enum class BrainMode {
    STORY,
    CHARACTER,
}

enum class BrainNodeType {
    SAGA,
    ACT,
    CHAPTER,
    EVENT,
    CHARACTER,
    CHARACTER_EVENT,
    RELATION,
    WIKI,
}

enum class BrainEdgeType {
    CONTAINS,
    PARTICIPATED,
    RELATED_TO,
    LORE_LINK,
    DEBUT,
    FEATURED_IN,
    ARC_FROM,
}

data class BrainNode(
    val id: String,
    val type: BrainNodeType,
    val label: String,
    val subtitle: String = "",
    val entityId: Int = 0,
    val importance: Float = 0.5f,
    val glowColorArgb: Long = 0xFFFFFFFF,
    val connectedNodeIds: List<String> = emptyList(),
    val detailBody: String = "",
    val characterId: Int? = null,
)

data class BrainEdge(
    val id: String,
    val fromId: String,
    val toId: String,
    val type: BrainEdgeType,
    val label: String? = null,
)

data class BrainGraph(
    val nodes: List<BrainNode>,
    val edges: List<BrainEdge>,
    val mode: BrainMode,
    val centerNodeId: String,
    val sagaId: Int,
    val focusCharacterId: Int? = null,
) {
    fun nodeById(id: String): BrainNode? = nodes.find { it.id == id }

    fun neighborsOf(nodeId: String): List<BrainNode> {
        val neighborIds =
            edges
                .filter { it.fromId == nodeId || it.toId == nodeId }
                .map { if (it.fromId == nodeId) it.toId else it.fromId }
                .distinct()
        return neighborIds.mapNotNull { nodeById(it) }
    }

    fun orbitNodes(selectedId: String): List<BrainNode> {
        val selected = nodeById(selectedId) ?: return emptyList()
        val neighbors =
            neighborsOf(selectedId)
                .sortedWith(
                    compareByDescending<BrainNode> { it.type == BrainNodeType.CHARACTER }
                        .thenByDescending { it.type == BrainNodeType.RELATION }
                        .thenByDescending { it.type == BrainNodeType.CHARACTER_EVENT }
                        .thenByDescending { it.importance }
                        .thenBy { it.label },
                )
        return listOf(selected) + neighbors.filter { it.id != selectedId }
    }
}

data class BrainNodeLayout(
    val nodeId: String,
    val x: Float,
    val y: Float,
    val radius: Float,
)

data class BrainLayoutResult(
    val layouts: Map<String, BrainNodeLayout>,
    val boundsWidth: Float,
    val boundsHeight: Float,
)

fun BrainNode.glowColor(): Color = Color(glowColorArgb.toULong())

object BrainNodeIds {
    fun saga(id: Int) = "saga_$id"

    fun act(id: Int) = "act_$id"

    fun chapter(id: Int) = "chapter_$id"

    fun event(id: Int) = "event_$id"

    fun character(id: Int) = "char_$id"

    fun characterEvent(id: Int) = "char_event_$id"

    fun relation(id: Int) = "relation_$id"

    fun wiki(id: Int) = "wiki_$id"
}
