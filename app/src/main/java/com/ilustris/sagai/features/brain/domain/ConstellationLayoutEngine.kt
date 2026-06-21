package com.ilustris.sagai.features.brain.domain

import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainLayoutResult
import com.ilustris.sagai.features.brain.domain.model.BrainNodeLayout
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.BrainScene
import com.ilustris.sagai.features.brain.domain.model.satelliteScale
import com.ilustris.sagai.features.brain.domain.model.starScale
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ConstellationLayoutEngine
    @Inject
    constructor() {
        fun layout(
            graph: BrainGraph,
            spacing: Float = 72f,
        ): BrainLayoutResult {
            val centerNode = graph.nodeById(graph.centerNodeId) ?: graph.nodes.firstOrNull()
            if (centerNode == null) {
                return BrainLayoutResult(emptyMap(), spacing, spacing)
            }

            val layouts = mutableMapOf<String, BrainNodeLayout>()
            layouts[centerNode.id] =
                BrainNodeLayout(
                    nodeId = centerNode.id,
                    x = 0f,
                    y = 0f,
                    radius = radiusFor(centerNode.type, isCenter = true),
                )

            val ringGroups =
                graph.nodes
                    .filter { it.id != centerNode.id }
                    .groupBy { ringIndex(it.type) }
                    .toSortedMap()

            ringGroups.forEach { (ring, nodes) ->
                val ringRadius = spacing * (ring + 1)
                nodes.forEachIndexed { index, node ->
                    val angle = (index.toFloat() / nodes.size.coerceAtLeast(1)) * 2f * Math.PI.toFloat()
                    val jitter = (node.id.hashCode() % 20) / 40f
                    val x = (ringRadius + jitter * spacing * 0.3f) * cos(angle)
                    val y = (ringRadius + jitter * spacing * 0.3f) * sin(angle)
                    layouts[node.id] =
                        BrainNodeLayout(
                            nodeId = node.id,
                            x = x,
                            y = y,
                            radius = radiusFor(node.type, isCenter = false),
                        )
                }
            }

            resolveCollisions(layouts, iterations = 2)

            val xs = layouts.values.map { it.x }
            val ys = layouts.values.map { it.y }
            val width = (xs.maxOrNull() ?: 0f) - (xs.minOrNull() ?: 0f) + spacing * 2
            val height = (ys.maxOrNull() ?: 0f) - (ys.minOrNull() ?: 0f) + spacing * 2

            return BrainLayoutResult(
                layouts = layouts,
                boundsWidth = width.coerceAtLeast(spacing * 3),
                boundsHeight = height.coerceAtLeast(spacing * 3),
            )
        }

        fun layoutScene(
            graph: BrainGraph,
            scene: BrainScene,
            spacing: Float = 110f,
        ): BrainLayoutResult {
            val focusNode = graph.nodeById(scene.focusNodeId)
            if (focusNode == null) return layout(graph, spacing)

            val layouts = mutableMapOf<String, BrainNodeLayout>()
            layouts[focusNode.id] =
                BrainNodeLayout(
                    nodeId = focusNode.id,
                    x = 0f,
                    y = 0f,
                    radius = radiusFor(focusNode.type, isCenter = true),
                )

            val structuralRing =
                scene.structuralNodeIds
                    .filter { it != scene.focusNodeId && graph.nodeById(it) != null }
                    .sortedBy { it.hashCode() }
            structuralRing.forEachIndexed { index, nodeId ->
                val node = graph.nodeById(nodeId) ?: return@forEachIndexed
                val angle =
                    (index.toFloat() / structuralRing.size.coerceAtLeast(1)) * 2f * Math.PI.toFloat()
                val ringRadius = spacing * 1.35f
                val jitter = (nodeId.hashCode() % 16) / 48f
                layouts[nodeId] =
                    BrainNodeLayout(
                        nodeId = nodeId,
                        x = (ringRadius + jitter * spacing * 0.25f) * cos(angle),
                        y = (ringRadius + jitter * spacing * 0.25f) * sin(angle),
                        radius = radiusFor(node.type, isCenter = false),
                    )
            }

            val satellites =
                scene.satelliteNodeIds
                    .filter { graph.nodeById(it) != null }
                    .sortedBy { it.hashCode() }
            val galaxySpread = spacing * 3.8f
            satellites.forEachIndexed { index, nodeId ->
                val node = graph.nodeById(nodeId) ?: return@forEachIndexed
                val (x, y) = scatterGalaxyPosition(nodeId, index, galaxySpread)
                layouts[nodeId] =
                    BrainNodeLayout(
                        nodeId = nodeId,
                        x = x,
                        y = y,
                        radius = radiusForSatellite(node.type),
                    )
            }

            resolveCollisions(layouts, iterations = 2, minGap = 4f)

            val xs = layouts.values.map { it.x }
            val ys = layouts.values.map { it.y }
            val width = (xs.maxOrNull() ?: 0f) - (xs.minOrNull() ?: 0f) + spacing * 4f
            val height = (ys.maxOrNull() ?: 0f) - (ys.minOrNull() ?: 0f) + spacing * 4f

            return BrainLayoutResult(
                layouts = layouts,
                boundsWidth = width.coerceAtLeast(spacing * 4),
                boundsHeight = height.coerceAtLeast(spacing * 4),
            )
        }

        /** Pseudo-random scatter — stars feel lost in space, not orbiting the focus. */
        private fun scatterGalaxyPosition(
            nodeId: String,
            index: Int,
            spread: Float,
        ): Pair<Float, Float> {
            val h1 = nodeId.hashCode()
            val h2 = (nodeId.hashCode() * 31) xor (index + 7)
            val h3 = (h1 * 17) xor (h2 shl 5)
            val xNorm = ((h1 and 0x7FFF) / 32767f) * 2f - 1f
            val yNorm = (((h2 shr 8) and 0x7FFF) / 32767f) * 2f - 1f
            val x = xNorm * spread + ((h3 and 0xFF) - 128) * 0.65f
            val y = yNorm * spread * 1.12f + (((h3 shr 8) and 0xFF) - 128) * 0.55f
            return x to y
        }

        fun layoutMini(
            graph: BrainGraph,
            width: Float,
            height: Float,
        ): BrainLayoutResult {
            val full = layout(graph, spacing = 36f)
            val scaleX = if (full.boundsWidth > 0) (width * 0.85f) / full.boundsWidth else 1f
            val scaleY = if (full.boundsHeight > 0) (height * 0.85f) / full.boundsHeight else 1f
            val scale = minOf(scaleX, scaleY, 1f)
            val scaled =
                full.layouts.mapValues { (_, layout) ->
                    layout.copy(
                        x = layout.x * scale + width / 2f,
                        y = layout.y * scale + height / 2f,
                        radius = layout.radius * scale.coerceAtLeast(0.6f),
                    )
                }
            return BrainLayoutResult(scaled, width, height)
        }

        private fun ringIndex(type: BrainNodeType): Int =
            when (type) {
                BrainNodeType.CHARACTER -> 1
                BrainNodeType.ACT -> 2
                BrainNodeType.CHAPTER -> 3
                BrainNodeType.EVENT -> 4
                BrainNodeType.CHARACTER_EVENT -> 3
                BrainNodeType.RELATION -> 2
                BrainNodeType.WIKI -> 5
                BrainNodeType.SAGA -> 0
            }

        private fun radiusFor(
            type: BrainNodeType,
            isCenter: Boolean,
        ): Float {
            val unit = 2.6f
            return unit * type.starScale(isCenter)
        }

        private fun radiusForSatellite(type: BrainNodeType): Float {
            val unit = 2.2f
            return unit * type.satelliteScale()
        }

        private fun resolveCollisions(
            layouts: MutableMap<String, BrainNodeLayout>,
            iterations: Int,
            minGap: Float = 8f,
        ) {
            repeat(iterations) {
                val entries = layouts.values.toList()
                entries.forEachIndexed { i, a ->
                    entries.drop(i + 1).forEach { b ->
                        val dx = b.x - a.x
                        val dy = b.y - a.y
                        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(0.01f)
                        val minDist = a.radius + b.radius + minGap
                        if (dist < minDist) {
                            val push = (minDist - dist) / 2f
                            val nx = dx / dist
                            val ny = dy / dist
                            layouts[a.nodeId] = a.copy(x = a.x - nx * push, y = a.y - ny * push)
                            layouts[b.nodeId] = b.copy(x = b.x + nx * push, y = b.y + ny * push)
                        }
                    }
                }
            }
        }
    }
