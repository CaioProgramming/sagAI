package com.ilustris.sagai.features.brain.domain.globe

import com.ilustris.sagai.features.brain.domain.model.BrainEdgeType
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Position of a node on the unit globe. The center node of the graph sits at the core (0, 0, 0);
 * every other node sits on the surface (|p| = 1).
 */
data class GlobeNodeLayout(
    val nodeId: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val phase: Float,
    val frequency: Float,
    val isCore: Boolean,
)

/**
 * Places every node of the [BrainGraph] on a sphere. There are no filler points: the number of
 * dots is exactly the number of nodes in the graph.
 *
 * Nodes are ordered by their position in the story hierarchy (acts, chapters, events) and then
 * laid along a Fibonacci spiral, so related nodes end up in the same latitude band. Characters,
 * wiki entries and relations are keyed by the nodes they connect to, so they settle near them.
 */
object GlobeLayoutEngine {
    private const val GOLDEN_ANGLE = 2.3999632f

    fun layout(graph: BrainGraph): Map<String, GlobeNodeLayout> {
        if (graph.nodes.isEmpty()) return emptyMap()
        val coreId = graph.nodeById(graph.centerNodeId)?.id ?: graph.nodes.first().id
        val keys = orderingKeys(graph, coreId)

        val surface =
            graph.nodes
                .filter { it.id != coreId }
                .sortedWith(
                    compareBy<BrainNode>({ keys[it.id] ?: 0f }, { it.type.ordinal }, { it.id }),
                )

        val result = LinkedHashMap<String, GlobeNodeLayout>(graph.nodes.size)
        result[coreId] = GlobeNodeLayout(coreId, 0f, 0f, 0f, phaseOf(coreId), 1f, isCore = true)

        val count = surface.size
        surface.forEachIndexed { index, node ->
            val y = if (count == 1) 0f else 1f - 2f * (index + 0.5f) / count
            val ring = sqrt(max(0f, 1f - y * y))
            val theta = index * GOLDEN_ANGLE
            result[node.id] =
                GlobeNodeLayout(
                    nodeId = node.id,
                    x = cos(theta) * ring,
                    y = y,
                    z = sin(theta) * ring,
                    phase = phaseOf(node.id),
                    frequency = 0.6f + unit(node.id, 7) * 0.9f,
                    isCore = false,
                )
        }
        return result
    }

    private fun orderingKeys(
        graph: BrainGraph,
        coreId: String,
    ): Map<String, Float> {
        val children = HashMap<String, MutableList<String>>()
        graph.edges.filter { it.type == BrainEdgeType.CONTAINS }.forEach {
            children.getOrPut(it.fromId) { mutableListOf() }.add(it.toId)
        }

        val keys = HashMap<String, Float>()
        var counter = 0f
        val stack = ArrayDeque<String>()
        stack.addLast(coreId)
        while (stack.isNotEmpty()) {
            val id = stack.removeLast()
            if (keys.containsKey(id)) continue
            keys[id] = counter++
            children[id].orEmpty().asReversed().forEach { if (!keys.containsKey(it)) stack.addLast(it) }
        }
        val span = max(counter, 1f)

        val neighbors = HashMap<String, MutableList<String>>()
        graph.edges.forEach {
            neighbors.getOrPut(it.fromId) { mutableListOf() }.add(it.toId)
            neighbors.getOrPut(it.toId) { mutableListOf() }.add(it.fromId)
        }

        // Nodes outside the containment tree follow the structural nodes they connect to.
        val anchored = HashMap<String, Float>()
        graph.nodes.forEach { node ->
            if (keys.containsKey(node.id)) return@forEach
            val known = neighbors[node.id].orEmpty().mapNotNull { keys[it] }
            anchored[node.id] =
                if (known.isEmpty()) {
                    unit(node.id, 3) * span
                } else {
                    known.average().toFloat() + (unit(node.id, 5) - 0.5f) * 2f
                }
        }
        keys.putAll(anchored)
        return keys
    }

    private fun phaseOf(id: String): Float = unit(id, 11) * 2f * PI.toFloat()

    /** Deterministic pseudo-random value in [0, 1) derived from the node id. */
    private fun unit(
        id: String,
        salt: Int,
    ): Float {
        var h = id.hashCode() * 31 + salt * 0x9E3779B1.toInt()
        h = h xor (h ushr 15)
        h *= 0x2C1B3C6D
        h = h xor (h ushr 12)
        h *= 0x297A2D39
        h = h xor (h ushr 15)
        return (h and 0xFFFFFF) / 16777216f
    }
}
