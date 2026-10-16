package com.ilustris.sagai.features.brain.ui.components

import android.graphics.Paint
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.brain.domain.globe.GlobeLayoutEngine
import com.ilustris.sagai.features.brain.domain.globe.GlobeMath
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.glowColor
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val MESH_MERIDIANS = 18
private const val MESH_MERIDIAN_STEPS = 36
private const val MESH_PARALLEL_STEPS = 72
private const val MESH_PARALLEL_DEG = 15
private const val MESH_PARALLEL_MAX_DEG = 75
private const val FACED_ZOOM = 1.7f
private const val CORE_ZOOM = 1.1f
private const val FAINT_EDGE_LIMIT = 900
private const val MAX_LABELS = 14
private const val MAX_IDLE_PULSES = 6
private const val ARC_SAMPLES = 14
private const val FAINT_ARC_SAMPLES = 7

/** Static wireframe of the planet (meridians and parallels) as unit vectors and segment pairs. */
private class GlobeMesh {
    val vertices: FloatArray
    val segments: IntArray

    init {
        val parallels = (-MESH_PARALLEL_MAX_DEG..MESH_PARALLEL_MAX_DEG step MESH_PARALLEL_DEG).toList()
        val vertexCount =
            MESH_MERIDIANS * (MESH_MERIDIAN_STEPS + 1) + parallels.size * (MESH_PARALLEL_STEPS + 1)
        val segmentCount = MESH_MERIDIANS * MESH_MERIDIAN_STEPS + parallels.size * MESH_PARALLEL_STEPS
        vertices = FloatArray(vertexCount * 3)
        segments = IntArray(segmentCount * 2)
        var v = 0
        var s = 0
        val pi = PI.toFloat()
        for (m in 0 until MESH_MERIDIANS) {
            val theta = m / MESH_MERIDIANS.toFloat() * 2f * pi
            for (i in 0..MESH_MERIDIAN_STEPS) {
                val lat = -pi / 2f + i / MESH_MERIDIAN_STEPS.toFloat() * pi
                vertices[v * 3] = cos(lat) * cos(theta)
                vertices[v * 3 + 1] = sin(lat)
                vertices[v * 3 + 2] = cos(lat) * sin(theta)
                if (i > 0) {
                    segments[s * 2] = v - 1
                    segments[s * 2 + 1] = v
                    s++
                }
                v++
            }
        }
        for (deg in parallels) {
            val lat = deg * pi / 180f
            for (j in 0..MESH_PARALLEL_STEPS) {
                val theta = j / MESH_PARALLEL_STEPS.toFloat() * 2f * pi
                vertices[v * 3] = cos(lat) * cos(theta)
                vertices[v * 3 + 1] = sin(lat)
                vertices[v * 3 + 2] = cos(lat) * sin(theta)
                if (j > 0) {
                    segments[s * 2] = v - 1
                    segments[s * 2 + 1] = v
                    s++
                }
                v++
            }
        }
    }
}

/** Immutable data derived from the graph, plus per-frame scratch arrays. */
private class GlobeScene(
    graph: BrainGraph,
) {
    val layout = GlobeLayoutEngine.layout(graph)
    val nodes = graph.nodes.filter { layout.containsKey(it.id) }
    val count = nodes.size
    val indexById = HashMap<String, Int>(count * 2).also { map -> nodes.forEachIndexed { i, n -> map[n.id] = i } }
    val mesh = GlobeMesh()

    val baseX = FloatArray(count)
    val baseY = FloatArray(count)
    val baseZ = FloatArray(count)
    val phase = FloatArray(count)
    val frequency = FloatArray(count)
    val isCore = BooleanArray(count)
    val colors = Array(count) { Color.White }
    val radiusDp = FloatArray(count)
    val degree = IntArray(count)

    val edgeA: IntArray
    val edgeB: IntArray
    val incidentEdges: Array<IntArray>

    // Per-frame scratch: rotated position (unit globe) and screen position.
    val rx = FloatArray(count)
    val ry = FloatArray(count)
    val rz = FloatArray(count)
    val sx = FloatArray(count)
    val sy = FloatArray(count)
    val meshSx = FloatArray(mesh.vertices.size / 3)
    val meshSy = FloatArray(mesh.vertices.size / 3)
    val meshZ = FloatArray(mesh.vertices.size / 3)

    init {
        val a = ArrayList<Int>()
        val b = ArrayList<Int>()
        graph.edges.forEach { edge ->
            val from = indexById[edge.fromId]
            val to = indexById[edge.toId]
            if (from != null && to != null && from != to) {
                a.add(from)
                b.add(to)
            }
        }
        edgeA = a.toIntArray()
        edgeB = b.toIntArray()
        val incident = Array(count) { ArrayList<Int>() }
        for (e in edgeA.indices) {
            incident[edgeA[e]].add(e)
            incident[edgeB[e]].add(e)
        }
        incidentEdges = Array(count) { incident[it].toIntArray() }

        nodes.forEachIndexed { i, node ->
            val place = layout.getValue(node.id)
            baseX[i] = place.x
            baseY[i] = place.y
            baseZ[i] = place.z
            phase[i] = place.phase
            frequency[i] = place.frequency
            isCore[i] = place.isCore
            colors[i] = node.glowColor()
            degree[i] = incidentEdges[i].size
            radiusDp[i] = typeRadiusDp(node.type) * (1f + min(degree[i], 18) * 0.045f)
        }
    }

    fun neighborOf(
        node: Int,
        edge: Int,
    ): Int = if (edgeA[edge] == node) edgeB[edge] else edgeA[edge]

    private fun typeRadiusDp(type: BrainNodeType): Float =
        when (type) {
            BrainNodeType.SAGA -> 7f
            BrainNodeType.ACT -> 4.2f
            BrainNodeType.CHAPTER -> 3f
            BrainNodeType.EVENT -> 2f
            BrainNodeType.CHARACTER -> 3.6f
            BrainNodeType.CHARACTER_EVENT -> 2.2f
            BrainNodeType.RELATION -> 2.4f
            BrainNodeType.WIKI -> 2.2f
        }
}

/** Mutable camera. Lives outside Compose state; the frame loop mutates it and invalidates the draw. */
private class GlobeCamera {
    var width = 0f
    var height = 0f
    var yaw = 0.4f
    var pitch = 0.25f
    var targetYaw: Float? = null
    var targetPitch = 0f
    var zoom = 1f
    var targetZoom = 1f
    var userZoom = 1f
    var centerX = 0f
    var centerY = 0f
    var time = 0f
    var hasSelection = false

    fun step(
        dt: Float,
        autoRotate: Boolean,
        snap: Boolean,
    ) {
        val k = if (snap) 1f else 1f - exp(-dt * 6f)
        val goalY = height * if (hasSelection) 0.36f else 0.5f
        if (centerX == 0f && centerY == 0f) {
            centerX = width / 2f
            centerY = goalY
        }
        centerX += (width / 2f - centerX) * k
        centerY += (goalY - centerY) * k
        zoom += (targetZoom - zoom) * k

        val ty = targetYaw
        if (ty != null) {
            val dy = GlobeMath.angleDelta(yaw, ty)
            yaw += dy * k
            pitch += (targetPitch - pitch) * k
            if (kotlin.math.abs(dy) < 0.002f && kotlin.math.abs(targetPitch - pitch) < 0.002f) targetYaw = null
        } else if (autoRotate && !hasSelection) {
            yaw += dt * 0.09f
        }
    }
}

private class Pulse(
    var edge: Int,
    var t: Float,
    var speed: Float,
)

/**
 * The saga as a planet. Every node of [graph] is a point on the globe surface (the center node is
 * the glowing core), edges are arcs over the surface. Selecting a node turns the globe to it,
 * zooms in and lights its connections.
 */
@Composable
fun BrainGlobeCanvas(
    graph: BrainGraph,
    selectedNodeId: String?,
    onNodeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    meshColor: Color = Color(0xFFC8DCD2),
    atmosphereColor: Color = Color(0xFF82D2B4),
) {
    val scene = remember(graph) { GlobeScene(graph) }
    val camera = remember { GlobeCamera() }
    val active = rememberLifecycleAnimationsActive()
    val context = LocalContext.current
    val reduceMotion =
        remember {
            runCatching {
                Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
            }.getOrDefault(false)
        }
    val currentOnSelected by rememberUpdatedState(onNodeSelected)
    var tick by remember { mutableLongStateOf(0L) }
    val selectedIndex = selectedNodeId?.let { scene.indexById[it] } ?: -1
    val pulses = remember(scene) { ArrayList<Pulse>() }
    val random = remember(scene) { Random(7) }
    val labelPaint =
        remember {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.White.toArgb()
                setShadowLayer(6f, 0f, 0f, Color.Black.toArgb())
            }
        }
    val scratch = remember { FloatArray(3) }

    LaunchedEffect(selectedIndex, scene) {
        camera.hasSelection = selectedIndex >= 0
        when {
            selectedIndex < 0 -> {
                camera.targetYaw = null
                camera.targetZoom = 1f
            }
            scene.isCore[selectedIndex] -> {
                camera.targetYaw = null
                camera.targetZoom = CORE_ZOOM
            }
            else -> {
                val (yaw, pitch) = GlobeMath.focusAngles(scene.baseX[selectedIndex], scene.baseY[selectedIndex], scene.baseZ[selectedIndex])
                camera.targetYaw = yaw
                camera.targetPitch = pitch
                camera.targetZoom = FACED_ZOOM
            }
        }
    }

    LaunchedEffect(active, scene, reduceMotion) {
        if (!active) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0.016f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                camera.time += dt
                camera.step(dt, autoRotate = !reduceMotion, snap = reduceMotion)
                tick = now
            }
        }
    }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged {
                    camera.width = it.width.toFloat()
                    camera.height = it.height.toFloat()
                }.pointerInput(scene) {
                    detectTapGestures { tap ->
                        val reach = 24.dp.toPx()
                        var best = -1
                        var bestDistance = Float.MAX_VALUE
                        for (i in 0 until scene.count) {
                            if (scene.rz[i] < -0.2f) continue
                            val d = hypot(scene.sx[i] - tap.x, scene.sy[i] - tap.y)
                            if (d < reach && d < bestDistance) {
                                bestDistance = d
                                best = i
                            }
                        }
                        currentOnSelected(if (best >= 0) scene.nodes[best].id else null)
                    }
                }.pointerInput(scene) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        camera.targetYaw = null
                        camera.yaw += pan.x * 0.005f
                        camera.pitch = (camera.pitch - pan.y * 0.005f).coerceIn(-1.4f, 1.4f)
                        camera.userZoom = (camera.userZoom * zoom).coerceIn(0.7f, 1.6f)
                    }
                },
    ) {
        if (tick < 0L) return@Canvas // reads tick so the canvas redraws every frame
        if (camera.width == 0f || scene.count == 0) return@Canvas
        drawGlobe(scene, camera, selectedIndex, pulses, random, labelPaint, scratch, meshColor, atmosphereColor, !reduceMotion)
    }
}

private fun DrawScope.drawGlobe(
    scene: GlobeScene,
    cam: GlobeCamera,
    selected: Int,
    pulses: ArrayList<Pulse>,
    random: Random,
    labelPaint: Paint,
    tmp: FloatArray,
    meshColor: Color,
    atmosphere: Color,
    animate: Boolean,
) {
    val dp = 1.dp.toPx()
    val baseScale = min(cam.width, cam.height) * 0.4f
    val radius = baseScale * cam.zoom * cam.userZoom
    val cx = cam.centerX
    val cy = cam.centerY
    val center = Offset(cx, cy)
    val time = cam.time

    // Planet body and atmosphere.
    drawCircle(
        brush =
            Brush.radialGradient(
                colorStops =
                    arrayOf(
                        0f to Color(0xFF2C3A34).copy(alpha = 0.6f),
                        0.5f to Color(0xFF101616).copy(alpha = 0.62f),
                        1f to Color(0xFF07090C).copy(alpha = 0.85f),
                    ),
                center = Offset(cx - radius * 0.38f, cy - radius * 0.42f),
                radius = radius * 1.5f,
            ),
        radius = radius,
        center = center,
    )
    drawCircle(
        brush =
            Brush.radialGradient(
                colorStops =
                    arrayOf(
                        0f to Color.Transparent,
                        0.77f to Color.Transparent,
                        0.81f to atmosphere.copy(alpha = 0.2f),
                        1f to Color.Transparent,
                    ),
                center = center,
                radius = radius * 1.22f,
            ),
        radius = radius * 1.22f,
        center = center,
    )

    fun project(
        x: Float,
        y: Float,
        z: Float,
        out: FloatArray,
    ) {
        val f = 1f + z * 0.16f
        out[0] = cx + x * radius * f
        out[1] = cy - y * radius * f
    }

    // Wireframe.
    val mesh = scene.mesh
    val frontPath = Path()
    val backPath = Path()
    val vCount = mesh.vertices.size / 3
    val screen = FloatArray(2)
    val meshSx = scene.meshSx
    val meshSy = scene.meshSy
    for (i in 0 until vCount) {
        GlobeMath.rotate(mesh.vertices[i * 3], mesh.vertices[i * 3 + 1], mesh.vertices[i * 3 + 2], cam.yaw, cam.pitch, tmp)
        project(tmp[0], tmp[1], tmp[2], screen)
        meshSx[i] = screen[0]
        meshSy[i] = screen[1]
        scene.meshZ[i] = tmp[2]
    }
    var s = 0
    while (s < mesh.segments.size) {
        val a = mesh.segments[s]
        val b = mesh.segments[s + 1]
        val path = if (scene.meshZ[a] + scene.meshZ[b] > 0f) frontPath else backPath
        path.moveTo(meshSx[a], meshSy[a])
        path.lineTo(meshSx[b], meshSy[b])
        s += 2
    }
    val meshStroke = Stroke(width = 0.7f * dp, cap = StrokeCap.Round)
    drawPath(backPath, meshColor.copy(alpha = 0.07f), style = meshStroke)
    drawPath(frontPath, meshColor.copy(alpha = 0.2f), style = meshStroke)

    // Rotate and project nodes.
    for (i in 0 until scene.count) {
        var x = scene.baseX[i]
        var y = scene.baseY[i]
        var z = scene.baseZ[i]
        if (animate && !scene.isCore[i]) {
            val drift = 1f + sin(time * scene.frequency[i] + scene.phase[i]) * 0.012f
            x *= drift
            y *= drift
            z *= drift
        }
        GlobeMath.rotate(x, y, z, cam.yaw, cam.pitch, tmp)
        scene.rx[i] = tmp[0]
        scene.ry[i] = tmp[1]
        scene.rz[i] = tmp[2]
        project(tmp[0], tmp[1], tmp[2], screen)
        scene.sx[i] = screen[0]
        scene.sy[i] = screen[1]
    }

    val a = FloatArray(3)
    val b = FloatArray(3)
    val p = FloatArray(3)
    fun load(
        i: Int,
        out: FloatArray,
    ) {
        out[0] = scene.rx[i]
        out[1] = scene.ry[i]
        out[2] = scene.rz[i]
    }

    fun arcPath(
        edge: Int,
        samples: Int,
        path: Path,
    ) {
        load(scene.edgeA[edge], a)
        load(scene.edgeB[edge], b)
        for (k in 0..samples) {
            GlobeMath.arcPoint(a, b, k / samples.toFloat(), p)
            project(p[0], p[1], p[2], screen)
            if (k == 0) path.moveTo(screen[0], screen[1]) else path.lineTo(screen[0], screen[1])
        }
    }

    // Faint threads for every connection (skipped on very large sagas).
    val edgeCount = scene.edgeA.size
    if (edgeCount in 1 until FAINT_EDGE_LIMIT) {
        val frontThreads = Path()
        val backThreads = Path()
        for (e in 0 until edgeCount) {
            val depth = scene.rz[scene.edgeA[e]] + scene.rz[scene.edgeB[e]]
            arcPath(e, FAINT_ARC_SAMPLES, if (depth > 0f) frontThreads else backThreads)
        }
        val alphaScale = if (selected >= 0) 0.03f else 0.08f
        val thread = Stroke(width = 0.6f * dp, cap = StrokeCap.Round)
        drawPath(backThreads, meshColor.copy(alpha = alphaScale * 0.4f), style = thread)
        drawPath(frontThreads, meshColor.copy(alpha = alphaScale), style = thread)
    }

    // Highlighted connections of the selected node.
    var neighborMask: BooleanArray? = null
    if (selected >= 0) {
        val mask = BooleanArray(scene.count)
        neighborMask = mask
        for (e in scene.incidentEdges[selected]) {
            val other = scene.neighborOf(selected, e)
            mask[other] = true
            val color = scene.colors[other]
            val path = Path()
            arcPath(e, ARC_SAMPLES, path)
            drawPath(path, color.copy(alpha = 0.2f), style = Stroke(width = 5f * dp, cap = StrokeCap.Round))
            drawPath(path, color.copy(alpha = 0.75f), style = Stroke(width = 1.3f * dp, cap = StrokeCap.Round))
        }
        var k = 0
        for (e in scene.incidentEdges[selected]) {
            val other = scene.neighborOf(selected, e)
            load(selected, a)
            load(other, b)
            val t = (time * 0.35f + k * 0.17f) % 1f
            GlobeMath.arcPoint(a, b, t, p)
            project(p[0], p[1], p[2], screen)
            drawCircle(scene.colors[other].copy(alpha = 0.95f), radius = 2.4f * dp, center = Offset(screen[0], screen[1]))
            k++
        }
        pulses.clear()
    } else if (animate && edgeCount > 0) {
        val wanted = min(MAX_IDLE_PULSES, (edgeCount + 11) / 12)
        while (pulses.size < wanted) pulses.add(Pulse(random.nextInt(edgeCount), 0f, 0.25f + random.nextFloat() * 0.35f))
        while (pulses.size > wanted) pulses.removeAt(pulses.size - 1)
        val dt = 0.016f
        for (pulse in pulses) {
            pulse.t += pulse.speed * dt
            if (pulse.t >= 1f) {
                pulse.edge = random.nextInt(edgeCount)
                pulse.t = 0f
                pulse.speed = 0.25f + random.nextFloat() * 0.35f
            }
            load(scene.edgeA[pulse.edge], a)
            load(scene.edgeB[pulse.edge], b)
            val color = scene.colors[scene.edgeA[pulse.edge]]
            for (trail in 0 until 6) {
                val tt = (pulse.t - trail * 0.018f).coerceAtLeast(0f)
                GlobeMath.arcPoint(a, b, tt, p)
                project(p[0], p[1], p[2], screen)
                drawCircle(
                    color.copy(alpha = 0.55f * (1f - trail / 6f)),
                    radius = 2.2f * dp * (1f - trail / 8f),
                    center = Offset(screen[0], screen[1]),
                )
            }
        }
    }

    // Nodes: far side first, then near side.
    val zoomScale = kotlin.math.sqrt(cam.zoom * cam.userZoom)
    for (pass in 0..1) {
        for (i in 0 until scene.count) {
            val z = scene.rz[i]
            if ((pass == 0) != (z <= 0f)) continue
            val depth = (z + 1f) / 2f
            val breath = if (animate) 1f + 0.16f * sin(time * scene.frequency[i] * 1.6f + scene.phase[i]) else 1f
            var r = scene.radiusDp[i] * dp * (0.55f + 0.45f * depth) * breath * zoomScale * 0.62f
            var alpha = if (scene.isCore[i]) 1f else if (z < -0.15f) 0.16f + 0.2f * depth else 0.38f + 0.62f * depth
            val isSelected = i == selected
            val isNeighbor = neighborMask?.get(i) == true
            if (selected >= 0) {
                if (isSelected) {
                    r *= 2.1f
                    alpha = 1f
                } else if (isNeighbor) {
                    r *= 1.55f
                    alpha = maxOf(alpha, 0.85f)
                } else {
                    alpha *= 0.22f
                }
            }
            if (scene.degree[i] == 0 && !scene.isCore[i] && !isSelected) alpha *= 0.7f
            val color = scene.colors[i]
            val at = Offset(scene.sx[i], scene.sy[i])

            if (scene.isCore[i]) {
                val coreRadius = r * 7.5f * (1f + 0.08f * sin(time * 1.1f))
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colorStops =
                                arrayOf(
                                    0f to Color(0xFFFFF1D6).copy(alpha = 0.95f),
                                    0.25f to color.copy(alpha = 0.35f),
                                    1f to color.copy(alpha = 0f),
                                ),
                            center = at,
                            radius = coreRadius,
                        ),
                    radius = coreRadius,
                    center = at,
                )
            }
            val glows = isSelected || isNeighbor || (selected < 0 && z > 0f && scene.nodes[i].type in GLOW_TYPES)
            if (glows) {
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.55f * alpha), color.copy(alpha = 0f)),
                            center = at,
                            radius = r * 4f,
                        ),
                    radius = r * 4f,
                    center = at,
                )
            }
            drawCircle(color.copy(alpha = color.alpha * alpha), radius = maxOf(0.7f * dp, r), center = at)
            if (isSelected) {
                val ring = r * 2.2f + 3f * dp * sin(time * 2.4f) + 3f * dp
                drawCircle(color.copy(alpha = 0.8f), radius = ring, center = at, style = Stroke(width = 1f * dp))
            }
        }
    }

    // Labels for the selected node and its nearest connections.
    if (selected >= 0) {
        labelPaint.textSize = 11.dp.toPx()
        val order = ArrayList<Int>()
        order.add(selected)
        scene.incidentEdges[selected]
            .map { scene.neighborOf(selected, it) }
            .distinct()
            .filter { scene.rz[it] > -0.1f }
            .sortedByDescending { scene.degree[it] }
            .take(MAX_LABELS)
            .forEach { order.add(it) }
        val placed = ArrayList<FloatArray>()
        drawIntoCanvas { canvas ->
            for (i in order) {
                val text = scene.nodes[i].label
                val width = labelPaint.measureText(text)
                val lx = scene.sx[i] + 10f * dp
                val ly = scene.sy[i] - 12f * dp
                if (lx < 8f * dp || lx + width > cam.width - 8f * dp || ly < 24f * dp || ly > cam.height - 20f * dp) continue
                if (placed.any { kotlin.math.abs(it[1] - ly) < 14f * dp && lx < it[0] + it[2] + 6f * dp && lx + width > it[0] - 6f * dp }) continue
                placed.add(floatArrayOf(lx, ly, width))
                labelPaint.color = (if (i == selected) Color.White else scene.colors[i].copy(alpha = 1f)).toArgb()
                canvas.nativeCanvas.drawText(text, lx, ly, labelPaint)
            }
        }
    }
}

private val GLOW_TYPES = setOf(BrainNodeType.CHARACTER, BrainNodeType.ACT)
