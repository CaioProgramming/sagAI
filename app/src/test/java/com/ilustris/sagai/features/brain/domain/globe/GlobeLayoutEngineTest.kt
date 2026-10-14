package com.ilustris.sagai.features.brain.domain.globe

import com.ilustris.sagai.features.brain.domain.model.BrainEdge
import com.ilustris.sagai.features.brain.domain.model.BrainEdgeType
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainMode
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class GlobeLayoutEngineTest {
    private fun graph(
        acts: Int,
        eventsPerAct: Int,
        characters: Int,
    ): BrainGraph {
        val nodes = mutableListOf(BrainNode("saga", BrainNodeType.SAGA, "Saga"))
        val edges = mutableListOf<BrainEdge>()
        repeat(acts) { a ->
            nodes.add(BrainNode("act$a", BrainNodeType.ACT, "Act $a"))
            edges.add(BrainEdge("s-a$a", "saga", "act$a", BrainEdgeType.CONTAINS))
            repeat(eventsPerAct) { e ->
                val id = "ev${a}_$e"
                nodes.add(BrainNode(id, BrainNodeType.EVENT, id))
                edges.add(BrainEdge("a-$id", "act$a", id, BrainEdgeType.CONTAINS))
            }
        }
        repeat(characters) { c ->
            nodes.add(BrainNode("char$c", BrainNodeType.CHARACTER, "Char $c"))
        }
        if (characters > 0 && acts > 0) {
            edges.add(BrainEdge("p", "char0", "ev0_0", BrainEdgeType.PARTICIPATED))
        }
        return BrainGraph(nodes, edges, BrainMode.STORY, "saga", sagaId = 1)
    }

    @Test
    fun `one position per node and nothing extra`() {
        val g = graph(acts = 3, eventsPerAct = 5, characters = 4)
        val layout = GlobeLayoutEngine.layout(g)
        assertEquals(g.nodes.size, layout.size)
        assertEquals(g.nodes.map { it.id }.toSet(), layout.keys)
    }

    @Test
    fun `center node is the core and every other node sits on the unit sphere`() {
        val layout = GlobeLayoutEngine.layout(graph(2, 4, 3))
        val core = layout.getValue("saga")
        assertTrue(core.isCore)
        assertEquals(0f, core.x, 0f)
        layout.values.filter { !it.isCore }.forEach {
            val length = sqrt(it.x * it.x + it.y * it.y + it.z * it.z)
            assertTrue("node ${it.nodeId} off sphere: $length", abs(length - 1f) < 1e-3f)
        }
    }

    @Test
    fun `layout is deterministic`() {
        val g = graph(3, 6, 5)
        assertEquals(GlobeLayoutEngine.layout(g), GlobeLayoutEngine.layout(g))
    }

    @Test
    fun `a saga with only a core and one node still lays out`() {
        val g = BrainGraph(listOf(BrainNode("saga", BrainNodeType.SAGA, "S"), BrainNode("a", BrainNodeType.ACT, "A")), emptyList(), BrainMode.STORY, "saga", 1)
        assertEquals(2, GlobeLayoutEngine.layout(g).size)
        assertEquals(1, GlobeLayoutEngine.layout(BrainGraph(listOf(BrainNode("saga", BrainNodeType.SAGA, "S")), emptyList(), BrainMode.STORY, "saga", 1)).size)
    }

    @Test
    fun `focus angles bring a node to the front`() {
        val out = FloatArray(3)
        val (yaw, pitch) = GlobeMath.focusAngles(0.6f, 0.3f, sqrt(1f - 0.36f - 0.09f))
        GlobeMath.rotate(0.6f, 0.3f, sqrt(1f - 0.36f - 0.09f), yaw, pitch, out)
        assertEquals(0f, out[0], 1e-4f)
        assertEquals(0f, out[1], 1e-4f)
        assertEquals(1f, out[2], 1e-4f)
    }
}
