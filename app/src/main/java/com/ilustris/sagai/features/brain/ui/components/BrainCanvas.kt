package com.ilustris.sagai.features.brain.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.brain.domain.model.BrainEdge
import com.ilustris.sagai.features.brain.domain.model.BrainEdgeType
import com.ilustris.sagai.features.brain.domain.model.BrainGraph
import com.ilustris.sagai.features.brain.domain.model.BrainLayoutResult
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.connectionScale
import com.ilustris.sagai.features.brain.domain.model.satelliteScale
import com.ilustris.sagai.features.brain.domain.model.starScale
import com.ilustris.sagai.ui.animations.draw4PointCosmicStar
import com.ilustris.sagai.ui.theme.themeBrushColors
import kotlinx.coroutines.launch
import kotlin.math.sqrt

private const val MOBILE_BASE_ZOOM = 4.4f
private const val MIN_ZOOM = 2.25f
private const val MAX_ZOOM = 11.5f
private const val FOCUS_ANIM_MS = 1400
private const val EDGE_REVEAL_MS = 2800
private const val OVERVIEW_ZOOM_THRESHOLD = 0.92f
private val FOCUS_TARGET_STAR_SIZE = 32.dp
private val PINCH_MAX_DRIFT = 56.dp
private val CanvasLabelColor = Color.White.copy(alpha = 0.92f)

private fun focusAnchorUserOffset(
    focusId: String,
    layout: BrainLayoutResult,
    effectiveScale: Float,
    focusOffsetX: Float,
    focusOffsetY: Float,
): Pair<Float, Float> {
    val nodeLayout = layout.layouts[focusId] ?: return 0f to 0f
    val idealTotalX = -nodeLayout.x * effectiveScale
    val idealTotalY = -nodeLayout.y * effectiveScale
    return (idealTotalX - focusOffsetX) to (idealTotalY - focusOffsetY)
}

private fun clampUserOffsetToFocusAnchor(
    userOffsetX: Float,
    userOffsetY: Float,
    anchorUserX: Float,
    anchorUserY: Float,
    maxDriftPx: Float,
): Pair<Float, Float> {
    val driftX = (userOffsetX - anchorUserX).coerceIn(-maxDriftPx, maxDriftPx)
    val driftY = (userOffsetY - anchorUserY).coerceIn(-maxDriftPx, maxDriftPx)
    return (anchorUserX + driftX) to (anchorUserY + driftY)
}

private fun focusZoomForNode(
    node: BrainNode,
    nodeLayout: com.ilustris.sagai.features.brain.domain.model.BrainNodeLayout,
    centerNodeId: String,
    targetStarRadiusPx: Float,
): Float {
    val typeScale = node.type.starScale(node.id == centerNodeId)
    val layoutRadius = (nodeLayout.radius * typeScale).coerceAtLeast(4f)
    return (targetStarRadiusPx / layoutRadius).coerceIn(MOBILE_BASE_ZOOM, MAX_ZOOM)
}

@Composable
fun BrainCosmicBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
    }
}

@Composable
fun BrainCanvas(
    graph: BrainGraph,
    layout: BrainLayoutResult,
    selectedNodeId: String?,
    visibleNodeIds: Set<String>,
    spineEdgeIds: Set<String> = emptySet(),
    satelliteNodeIds: Set<String> = emptySet(),
    recenterNonce: Int = 0,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    genrePrimary: Color,
    genreSecondary: Color,
    onNodeSelected: (String) -> Unit = {},
) {
    var pinchScale by remember { mutableFloatStateOf(1f) }
    var userOffsetX by remember { mutableFloatStateOf(0f) }
    var userOffsetY by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val focusScaleAnim = remember { Animatable(MOBILE_BASE_ZOOM) }
    val focusOffsetX = remember { Animatable(0f) }
    val focusOffsetY = remember { Animatable(0f) }

    val focusId = selectedNodeId ?: graph.centerNodeId
    val sceneSignature = remember(visibleNodeIds) { visibleNodeIds.hashCode() }
    var previousSceneSignature by remember { mutableIntStateOf(sceneSignature) }
    val effectiveScale = focusScaleAnim.value * pinchScale
    val totalOffsetX = focusOffsetX.value + userOffsetX
    val totalOffsetY = focusOffsetY.value + userOffsetY

    val focusNode = graph.nodeById(focusId)
    val canvasPrimary = remember(genrePrimary) { BrainStarGlow.themeAccentForCanvas(genrePrimary) }
    val density = LocalDensity.current
    val focusTargetStarRadiusPx = with(density) { FOCUS_TARGET_STAR_SIZE.toPx() }
    val focusGlowColor =
        focusNode?.let { node ->
            BrainStarGlow.color(
                node = node,
                isSatellite = node.id in satelliteNodeIds,
                isSelected = true,
                primary = genrePrimary,
                secondary = genreSecondary,
            )
        } ?: canvasPrimary

    val visibleEdges =
        remember(focusId, visibleNodeIds, graph.edges) {
            graph.edges.filter { edge ->
                edge.fromId in visibleNodeIds && edge.toId in visibleNodeIds
            }
        }
    val focusLinkedEdges =
        remember(focusId, visibleEdges) {
            visibleEdges.filter { edge ->
                edge.fromId == focusId || edge.toId == focusId
            }
        }
    val focusLinkedIds = remember(focusLinkedEdges) { focusLinkedEdges.map { it.id }.toSet() }
    val spineEdges =
        remember(spineEdgeIds, visibleEdges, focusLinkedIds) {
            visibleEdges.filter { it.id in spineEdgeIds && it.id !in focusLinkedIds }
        }
    val ambientEdges =
        remember(spineEdgeIds, visibleEdges, focusLinkedIds) {
            visibleEdges.filter { it.id !in spineEdgeIds && it.id !in focusLinkedIds }
        }

    val edgeReveal = remember { Animatable(0f) }
    LaunchedEffect(focusId, focusLinkedEdges.map { it.id }, spineEdges.map { it.id }) {
        edgeReveal.snapTo(0f)
        edgeReveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = EDGE_REVEAL_MS, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(recenterNonce) {
        if (recenterNonce == 0) return@LaunchedEffect
        val mergedScale = focusScaleAnim.value * pinchScale
        val mergedOffsetX = focusOffsetX.value + userOffsetX
        val mergedOffsetY = focusOffsetY.value + userOffsetY
        focusScaleAnim.snapTo(mergedScale)
        focusOffsetX.snapTo(mergedOffsetX)
        focusOffsetY.snapTo(mergedOffsetY)
        pinchScale = 1f
        userOffsetX = 0f
        userOffsetY = 0f
        edgeReveal.snapTo(0f)
        kotlinx.coroutines.coroutineScope {
            launch {
                focusScaleAnim.animateTo(
                    targetValue = MOBILE_BASE_ZOOM,
                    animationSpec =
                        tween(
                            durationMillis = FOCUS_ANIM_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
            launch {
                focusOffsetX.animateTo(
                    targetValue = 0f,
                    animationSpec =
                        tween(
                            durationMillis = FOCUS_ANIM_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
            launch {
                focusOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec =
                        tween(
                            durationMillis = FOCUS_ANIM_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
            launch {
                edgeReveal.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        tween(
                            durationMillis = EDGE_REVEAL_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
        }
    }

    LaunchedEffect(focusId, layout, sceneSignature) {
        val nodeLayout = layout.layouts[focusId]
        val mergedScale = focusScaleAnim.value * pinchScale
        val mergedOffsetX = focusOffsetX.value + userOffsetX
        val mergedOffsetY = focusOffsetY.value + userOffsetY
        val sceneChanged = sceneSignature != previousSceneSignature

        focusScaleAnim.snapTo(mergedScale)
        pinchScale = 1f
        userOffsetX = 0f
        userOffsetY = 0f

        if (sceneChanged) {
            focusOffsetX.snapTo(0f)
            focusOffsetY.snapTo(0f)
        } else {
            focusOffsetX.snapTo(mergedOffsetX)
            focusOffsetY.snapTo(mergedOffsetY)
        }
        previousSceneSignature = sceneSignature

        val targetZoom =
            when {
                focusId == graph.centerNodeId || nodeLayout == null -> MOBILE_BASE_ZOOM
                mergedScale <= MOBILE_BASE_ZOOM * OVERVIEW_ZOOM_THRESHOLD -> mergedScale

                else -> {
                    focusNode?.let { node ->
                        focusZoomForNode(
                            node = node,
                            nodeLayout = nodeLayout,
                            centerNodeId = graph.centerNodeId,
                            targetStarRadiusPx = focusTargetStarRadiusPx,
                        )
                    } ?: MOBILE_BASE_ZOOM
                }
            }
        val targetX =
            if (focusId == graph.centerNodeId || nodeLayout == null) {
                0f
            } else {
                -nodeLayout.x * targetZoom
            }
        val targetY =
            if (focusId == graph.centerNodeId || nodeLayout == null) {
                0f
            } else {
                -nodeLayout.y * targetZoom
            }

        edgeReveal.snapTo(0f)
        kotlinx.coroutines.coroutineScope {
            launch {
                focusScaleAnim.animateTo(
                    targetValue = targetZoom,
                    animationSpec =
                        tween(
                            durationMillis = FOCUS_ANIM_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
            launch {
                focusOffsetX.animateTo(
                    targetValue = targetX,
                    animationSpec =
                        tween(
                            durationMillis = FOCUS_ANIM_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
            launch {
                focusOffsetY.animateTo(
                    targetValue = targetY,
                    animationSpec =
                        tween(
                            durationMillis = FOCUS_ANIM_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
            launch {
                edgeReveal.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        tween(
                            durationMillis = EDGE_REVEAL_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
        }
    }

    val twinkleTransition = rememberInfiniteTransition(label = "brain_twinkle")
    val twinkle by twinkleTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "twinkle_alpha",
    )
    val nebulaBreathe by twinkleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(9000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "nebula_breathe",
    )

    val nebulaColors = themeBrushColors()
    val nebulae =
        remember(graph.centerNodeId, sceneSignature, nebulaColors) {
            generateBrainNebulae(
                seed = graph.centerNodeId.hashCode() xor sceneSignature,
                colors = nebulaColors,
            )
        }

    val starPresenceAlphas = rememberBrainStarPresenceAlphas(visibleNodeIds)

    BrainCosmicBackground(modifier = modifier) {
        val labelGapPx = with(density) { 10.dp.toPx() }
        val labelLineHeightPx =
            with(density) {
                MaterialTheme.typography.labelMedium.lineHeight
                    .toPx()
            }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 112.dp)
                    .onSizeChanged { canvasSize = it }
                    .then(
                        if (interactive) {
                            Modifier.pointerInput(
                                layout,
                                focusId,
                                focusScaleAnim.value,
                                focusOffsetX.value,
                                focusOffsetY.value,
                            ) {
                                val maxDriftPx = PINCH_MAX_DRIFT.toPx()
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val oldEffectiveScale = focusScaleAnim.value * pinchScale
                                    val newEffectiveScale =
                                        (oldEffectiveScale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                                    pinchScale =
                                        (newEffectiveScale / focusScaleAnim.value).coerceIn(
                                            MIN_ZOOM / focusScaleAnim.value.coerceAtLeast(0.01f),
                                            MAX_ZOOM / focusScaleAnim.value.coerceAtLeast(0.01f),
                                        )
                                    val effectiveAfterPinch = focusScaleAnim.value * pinchScale
                                    val scaledDrift =
                                        maxDriftPx *
                                            (effectiveAfterPinch / MOBILE_BASE_ZOOM)
                                                .coerceIn(0.55f, 1.6f)
                                    val (anchorUserX, anchorUserY) =
                                        focusAnchorUserOffset(
                                            focusId = focusId,
                                            layout = layout,
                                            effectiveScale = effectiveAfterPinch,
                                            focusOffsetX = focusOffsetX.value,
                                            focusOffsetY = focusOffsetY.value,
                                        )
                                    val startUserX = if (zoom != 1f) anchorUserX else userOffsetX
                                    val startUserY = if (zoom != 1f) anchorUserY else userOffsetY
                                    val (clampedX, clampedY) =
                                        clampUserOffsetToFocusAnchor(
                                            userOffsetX = startUserX + pan.x,
                                            userOffsetY = startUserY + pan.y,
                                            anchorUserX = anchorUserX,
                                            anchorUserY = anchorUserY,
                                            maxDriftPx = scaledDrift,
                                        )
                                    userOffsetX = clampedX
                                    userOffsetY = clampedY
                                }
                            }
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (interactive) {
                            Modifier.pointerInput(
                                graph,
                                layout,
                                effectiveScale,
                                focusOffsetX.value,
                                focusOffsetY.value,
                                userOffsetX,
                                userOffsetY,
                                canvasSize,
                                visibleNodeIds,
                                focusId,
                                satelliteNodeIds,
                            ) {
                                detectTapGestures(
                                    onTap = { tapOffset ->
                                        findNodeAt(
                                            tapOffset = tapOffset,
                                            graph = graph,
                                            layout = layout,
                                            scale = effectiveScale,
                                            offsetX = focusOffsetX.value + userOffsetX,
                                            offsetY = focusOffsetY.value + userOffsetY,
                                            canvasSize = canvasSize,
                                            visibleNodeIds = visibleNodeIds,
                                            focusId = focusId,
                                            satelliteNodeIds = satelliteNodeIds,
                                        )?.let { onNodeSelected(it.id) }
                                    },
                                )
                            }
                        } else {
                            Modifier
                        },
                    ),
        ) {
            Canvas(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = effectiveScale
                            scaleY = effectiveScale
                            translationX = totalOffsetX
                            translationY = totalOffsetY
                        },
            ) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val reveal = edgeReveal.value

                drawCosmicNebulae(
                    nebulae = nebulae,
                    centerX = centerX,
                    centerY = centerY,
                    breathePhase = nebulaBreathe,
                )

                ambientEdges.forEach { edge ->
                    val fromPresence = starPresenceAlphas[edge.fromId] ?: 1f
                    val toPresence = starPresenceAlphas[edge.toId] ?: 1f
                    val endpointPresence = minOf(fromPresence, toPresence)
                    if (endpointPresence < 0.02f) return@forEach
                    drawBrainEdge(
                        edge = edge,
                        graph = graph,
                        layout = layout,
                        centerX = centerX,
                        centerY = centerY,
                        focusId = focusId,
                        centerNodeId = graph.centerNodeId,
                        satelliteNodeIds = satelliteNodeIds,
                        twinkle = twinkle,
                        themePrimary = canvasPrimary,
                        revealProgress = reveal * endpointPresence,
                        endpointPresence = endpointPresence,
                        isSpine = false,
                        isFocusLink = false,
                        linkGlowColor = focusGlowColor,
                    )
                }

                spineEdges.forEachIndexed { index, edge ->
                    val fromPresence = starPresenceAlphas[edge.fromId] ?: 1f
                    val toPresence = starPresenceAlphas[edge.toId] ?: 1f
                    val endpointPresence = minOf(fromPresence, toPresence)
                    if (endpointPresence < 0.02f) return@forEachIndexed
                    val stagger =
                        ((reveal * spineEdges.size) - index * 0.35f)
                            .coerceIn(0f, 1f) * endpointPresence
                    drawBrainEdge(
                        edge = edge,
                        graph = graph,
                        layout = layout,
                        centerX = centerX,
                        centerY = centerY,
                        focusId = focusId,
                        centerNodeId = graph.centerNodeId,
                        satelliteNodeIds = satelliteNodeIds,
                        twinkle = twinkle,
                        themePrimary = canvasPrimary,
                        revealProgress = stagger,
                        endpointPresence = endpointPresence,
                        isSpine = true,
                        isFocusLink = false,
                        linkGlowColor = focusGlowColor,
                    )
                }

                focusLinkedEdges.forEachIndexed { index, edge ->
                    val fromPresence = starPresenceAlphas[edge.fromId] ?: 1f
                    val toPresence = starPresenceAlphas[edge.toId] ?: 1f
                    val endpointPresence = minOf(fromPresence, toPresence)
                    if (endpointPresence < 0.02f) return@forEachIndexed
                    val stagger =
                        ((reveal * focusLinkedEdges.size) - index * 0.28f)
                            .coerceIn(0f, 1f) * endpointPresence
                    drawBrainEdge(
                        edge = edge,
                        graph = graph,
                        layout = layout,
                        centerX = centerX,
                        centerY = centerY,
                        focusId = focusId,
                        centerNodeId = graph.centerNodeId,
                        satelliteNodeIds = satelliteNodeIds,
                        twinkle = twinkle,
                        themePrimary = canvasPrimary,
                        revealProgress = stagger,
                        endpointPresence = endpointPresence,
                        isSpine = edge.id in spineEdgeIds,
                        isFocusLink = true,
                        linkGlowColor = focusGlowColor,
                    )
                }

                graph.nodes.forEach { node ->
                    if (node.id !in visibleNodeIds) return@forEach
                    val nodeLayout = layout.layouts[node.id] ?: return@forEach
                    val isSelected = node.id == focusId
                    val isCenter = node.id == graph.centerNodeId
                    val isSatellite = node.id in satelliteNodeIds
                    val x = centerX + nodeLayout.x
                    val y = centerY + nodeLayout.y
                    val glowColor =
                        BrainStarGlow.color(
                            node = node,
                            isSatellite = isSatellite,
                            isSelected = isSelected,
                            primary = genrePrimary,
                            secondary = genreSecondary,
                        )

                    drawBrainStar(
                        node = node,
                        x = x,
                        y = y,
                        baseRadius = nodeLayout.radius,
                        isSelected = isSelected,
                        isCenter = isCenter,
                        isSatellite = isSatellite,
                        twinkle = twinkle,
                        glowColor = glowColor,
                        presenceAlpha = starPresenceAlphas[node.id] ?: 1f,
                        rotationDegrees = (node.id.hashCode() % 30 - 15).toFloat(),
                    )
                }
            }

            val selectedNode = graph.nodeById(focusId)
            val selectedLayout = layout.layouts[focusId]
            if (selectedNode != null && selectedLayout != null) {
                val isCenter = focusId == graph.centerNodeId
                val isSatellite = focusId in satelliteNodeIds
                val typeScale = selectedNode.type.starScale(isCenter)
                val starSizePx = selectedLayout.radius * typeScale * effectiveScale
                val glowPaddingPx =
                    if (selectedNode.type == BrainNodeType.CHARACTER) {
                        with(density) { 6.dp.toPx() }
                    } else {
                        with(density) { 3.dp.toPx() }
                    }
                val labelAlpha = (starPresenceAlphas[focusId] ?: 1f).coerceIn(0f, 1f)
                Text(
                    text = selectedNode.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = CanvasLabelColor.copy(alpha = CanvasLabelColor.alpha * labelAlpha),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                            .graphicsLayer {
                                translationX = (selectedLayout.x * effectiveScale) + totalOffsetX
                                translationY =
                                    (selectedLayout.y * effectiveScale) +
                                            totalOffsetY +
                                            starSizePx +
                                            glowPaddingPx +
                                            labelGapPx +
                                            (labelLineHeightPx * 0.5f)
                            },
                )
            }
        }
    }
}

@Composable
fun BrainMiniCanvas(
    graph: BrainGraph,
    layout: BrainLayoutResult,
    modifier: Modifier = Modifier,
    genrePrimary: Color = Color(0xFF90E0EF),
    genreSecondary: Color = Color(0xFFB0BEC5),
) {
    val twinkleTransition = rememberInfiniteTransition(label = "mini_twinkle")
    val twinkle by twinkleTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "mini_twinkle_alpha",
    )

    BrainCosmicBackground(modifier = modifier) {
        val canvasPrimary = BrainStarGlow.themeAccentForCanvas(genrePrimary)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val focusId = graph.centerNodeId
            graph.edges
                .filter { it.fromId == focusId || it.toId == focusId }
                .forEach { edge ->
                    drawBrainEdge(
                        edge = edge,
                        graph = graph,
                        layout = layout,
                        centerX = size.width / 2f,
                        centerY = size.height / 2f,
                        focusId = focusId,
                        centerNodeId = graph.centerNodeId,
                        satelliteNodeIds = emptySet(),
                        twinkle = 0.75f,
                        themePrimary = canvasPrimary,
                    )
                }
            graph.nodes.forEach { node ->
                val nodeLayout = layout.layouts[node.id] ?: return@forEach
                val isCenter = node.id == graph.centerNodeId
                val isLinked =
                    graph.edges.any { edge ->
                        (edge.fromId == focusId && edge.toId == node.id) ||
                            (edge.toId == focusId && edge.fromId == node.id)
                    }
                if (!isCenter && !isLinked) return@forEach
                val isSelected = isCenter
                drawBrainStar(
                    node = node,
                    x = nodeLayout.x,
                    y = nodeLayout.y,
                    baseRadius = nodeLayout.radius,
                    isSelected = isSelected,
                    isCenter = isCenter,
                    isSatellite = false,
                    twinkle = if (isSelected) twinkle else 1f,
                    glowColor =
                        BrainStarGlow.color(
                            node = node,
                            isSatellite = false,
                            isSelected = isSelected,
                            primary = genrePrimary,
                            secondary = genreSecondary,
                        ),
                    rotationDegrees = (node.id.hashCode() % 30 - 15).toFloat(),
                )
            }
        }
    }
}

private fun DrawScope.drawBrainEdge(
    edge: BrainEdge,
    graph: BrainGraph,
    layout: BrainLayoutResult,
    centerX: Float,
    centerY: Float,
    focusId: String,
    centerNodeId: String,
    satelliteNodeIds: Set<String>,
    twinkle: Float,
    themePrimary: Color,
    revealProgress: Float = 1f,
    endpointPresence: Float = 1f,
    isSpine: Boolean = false,
    isFocusLink: Boolean = false,
    linkGlowColor: Color = themePrimary,
) {
    val fromLayout = layout.layouts[edge.fromId] ?: return
    val toLayout = layout.layouts[edge.toId] ?: return

    val start = Offset(centerX + fromLayout.x, centerY + fromLayout.y)
    val end = Offset(centerX + toLayout.x, centerY + toLayout.y)

    val visibility = (revealProgress * endpointPresence).coerceIn(0f, 1f)
    if (visibility < 0.02f) return

    val alpha =
        when {
            isFocusLink -> (0.16f + twinkle * 0.22f) * visibility
            isSpine -> (0.14f + twinkle * 0.16f) * edge.type.connectionScale() * visibility
            else -> (0.08f + twinkle * 0.1f) * visibility
        }
    val stroke =
        when {
            isFocusLink -> {
                0.72f
            }

            isSpine -> {
                when (edge.type) {
                    BrainEdgeType.CONTAINS -> 0.82f
                    BrainEdgeType.DEBUT -> 0.9f
                    BrainEdgeType.RELATED_TO -> 0.78f * edge.type.connectionScale()
                    else -> 0.68f
                }
            }

            else -> {
                0.38f
            }
        }
    val color =
        when {
            isFocusLink -> linkGlowColor
            isSpine -> themePrimary
            else -> themePrimary
        }

    if (isFocusLink) {
        drawConstellationGlow(
            start = start,
            end = end,
            color = linkGlowColor,
            twinkle = twinkle,
            revealProgress = visibility,
            baseStrokeWidth = stroke,
        )
    }

    drawLine(
        color = color.copy(alpha = alpha),
        start = start,
        end = end,
        strokeWidth = stroke,
    )
}

private fun DrawScope.drawConstellationGlow(
    start: Offset,
    end: Offset,
    color: Color,
    twinkle: Float,
    revealProgress: Float,
    baseStrokeWidth: Float,
) {
    drawIntoCanvas { canvas ->
        val glowPaint =
            android.graphics.Paint().apply {
                isAntiAlias = true
                this.color = color.toArgb()
                alpha = ((0.08f + twinkle * 0.1f) * revealProgress * 255).toInt().coerceIn(0, 255)
                strokeWidth = baseStrokeWidth * 2.2f
                style = android.graphics.Paint.Style.STROKE
                strokeCap = android.graphics.Paint.Cap.ROUND
                maskFilter = BlurMaskFilter(baseStrokeWidth * 1.8f, BlurMaskFilter.Blur.NORMAL)
            }
        canvas.nativeCanvas.drawLine(start.x, start.y, end.x, end.y, glowPaint)
    }
    drawLine(
        color = color.copy(alpha = (0.06f + twinkle * 0.09f) * revealProgress),
        start = start,
        end = end,
        strokeWidth = baseStrokeWidth * 1.35f,
    )
}

/** Lines originate at star centers — stars are drawn on top. */
private fun DrawScope.drawBrainStar(
    node: BrainNode,
    x: Float,
    y: Float,
    baseRadius: Float,
    isSelected: Boolean,
    isCenter: Boolean,
    isSatellite: Boolean,
    twinkle: Float,
    glowColor: Color,
    presenceAlpha: Float = 1f,
    rotationDegrees: Float,
) {
    val fade = presenceAlpha.coerceIn(0f, 1f)
    val typeScale =
        if (isSatellite && !isSelected) {
            node.type.satelliteScale()
        } else {
            node.type.starScale(isCenter)
        }
    val presenceScale =
        when {
            isSelected -> 1f
            isSatellite -> 0.88f
            else -> 0.9f
        }
    val starSize = baseRadius * typeScale * presenceScale
    val coreColor = BrainStarGlow.starCoreColor(glowColor, isSelected, twinkle)
    val starColor = coreColor.copy(alpha = coreColor.alpha * fade)
    val haloAlpha = BrainStarGlow.haloAlpha(node, isSelected, twinkle) * fade
    val glowBlur = if (isSelected) BrainStarGlow.selectedGlowBlurFactor(node) else 1.8f
    val glowSpread = if (isSelected) BrainStarGlow.selectedGlowSpreadFactor(node) else 1.35f

    draw4PointCosmicStar(
        center = Offset(x, y),
        size = starSize,
        color = starColor,
        glowColor = glowColor.copy(alpha = glowColor.alpha.coerceIn(0f, 1f)),
        glowAlpha = haloAlpha,
        glowBlurFactor = glowBlur,
        glowSpreadFactor = glowSpread,
        rotationDegrees = rotationDegrees,
    )

    if (isSelected) {
        val accent = BrainStarGlow.selectedAccentColor(glowColor, twinkle)
        draw4PointCosmicStar(
            center = Offset(x, y),
            size = starSize * 0.28f,
            color = accent.copy(alpha = accent.alpha * fade),
            glowColor = glowColor.copy(alpha = 0.35f * twinkle * fade),
            glowAlpha = 1f,
            glowBlurFactor = glowBlur * 0.6f,
            glowSpreadFactor = glowSpread * 0.7f,
            rotationDegrees = rotationDegrees + 18f,
        )
    }
}

private fun findNodeAt(
    tapOffset: Offset,
    graph: BrainGraph,
    layout: BrainLayoutResult,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    canvasSize: IntSize,
    visibleNodeIds: Set<String>,
    focusId: String,
    satelliteNodeIds: Set<String>,
): BrainNode? {
    if (canvasSize == IntSize.Zero) return null
    val centerX = canvasSize.width / 2f
    val centerY = canvasSize.height / 2f
    var closest: BrainNode? = null
    var closestDist = Float.MAX_VALUE
    graph.nodes.forEach { node ->
        if (node.id !in visibleNodeIds) return@forEach
        val nodeLayout = layout.layouts[node.id] ?: return@forEach
        val isCenter = node.id == graph.centerNodeId
        val isSelected = node.id == focusId
        val isSatellite = node.id in satelliteNodeIds
        val typeScale =
            if (isSatellite && !isSelected) {
                node.type.satelliteScale()
            } else {
                node.type.starScale(isCenter)
            }
        val presenceScale = if (isSelected) 1f else 0.9f
        val starScreenRadius = nodeLayout.radius * typeScale * presenceScale * scale
        val screenX = centerX + nodeLayout.x * scale + offsetX
        val screenY = centerY + nodeLayout.y * scale + offsetY
        val hitRadius = starScreenRadius * 4.5f + 44f
        val dx = screenX - tapOffset.x
        val dy = screenY - tapOffset.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < closestDist && dist <= hitRadius) {
            closestDist = dist
            closest = node
        }
    }
    return closest
}
