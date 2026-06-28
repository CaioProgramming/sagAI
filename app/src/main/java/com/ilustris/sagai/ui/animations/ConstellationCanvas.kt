package com.ilustris.sagai.ui.animations

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sqrt
import kotlin.random.Random

private data class ConstellationStar(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float,
)

@Composable
fun ConstellationCanvas(
    modifier: Modifier = Modifier,
    starColor: Color = Color.White.copy(alpha = 0.6f),
    lineColor: Color = Color.White.copy(alpha = 0.2f),
    knowledgeColor: Color = Color(0xFFB1A7F0),
    starCount: Int = 60,
    maxLineDistance: Float = 250f,
    chapterClusters: List<ChapterKnowledgeCluster> = emptyList(),
) {
    val stars = remember { mutableStateListOf<ConstellationStar>() }
    val totalKnowledgeStars = chapterClusters.sumOf { it.satelliteCount }
    val ambientStarCount = starCount + (totalKnowledgeStars / 3).coerceAtMost(40)

    val infiniteTransition = rememberInfiniteTransition(label = "constellation")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "phase",
    )

    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "twinkle",
    )

    val lineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(10000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "lineProgress",
    )

    val knowledgePulse by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(4200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "knowledgePulse",
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (stars.isEmpty()) {
                repeat(ambientStarCount) {
                    stars.add(
                        ConstellationStar(
                            x = Random.nextFloat() * size.width,
                            y = Random.nextFloat() * size.height,
                            size = Random.nextFloat() * 2f + 1f,
                            alpha = Random.nextFloat() * 0.6f + 0.2f,
                            speed = Random.nextFloat() * 0.03f + 0.005f,
                        ),
                    )
                }
            }

            stars.forEachIndexed { index, star ->
                val currentX = (star.x + phase * size.width * star.speed) % size.width
                val currentY = star.y

                val starAlpha = (star.alpha * twinkle).coerceIn(0f, 1f)

                drawCircle(
                    color = starColor.copy(alpha = starAlpha * 0.3f),
                    radius = star.size * 4f,
                    center = Offset(currentX, currentY),
                )

                drawCircle(
                    color = starColor.copy(alpha = starAlpha),
                    radius = star.size,
                    center = Offset(currentX, currentY),
                )

                if (lineProgress > 0.1f) {
                    for (j in index + 1 until stars.size) {
                        val otherStar = stars[j]
                        val otherX =
                            (otherStar.x + phase * size.width * otherStar.speed) % size.width
                        val otherY = otherStar.y

                        val distance =
                            sqrt(
                                (currentX - otherX) * (currentX - otherX) +
                                    (currentY - otherY) * (currentY - otherY),
                            )

                        if (distance < maxLineDistance) {
                            val lineAlpha = (1f - distance / maxLineDistance) * lineProgress * 0.3f
                            if (lineAlpha > 0.05f) {
                                drawLine(
                                    color = lineColor.copy(alpha = lineAlpha),
                                    start = Offset(currentX, currentY),
                                    end = Offset(otherX, otherY),
                                    strokeWidth = 1.5f,
                                )
                            }
                        }
                    }
                }
            }

            if (chapterClusters.isNotEmpty()) {
                drawChapterKnowledgeField(
                    clusters = chapterClusters,
                    knowledgeColor = knowledgeColor,
                    lineColor = lineColor,
                    twinkle = twinkle,
                    knowledgePulse = knowledgePulse,
                    lineProgress = lineProgress,
                )
            }
        }
    }
}

private fun DrawScope.drawChapterKnowledgeField(
    clusters: List<ChapterKnowledgeCluster>,
    knowledgeColor: Color,
    lineColor: Color,
    twinkle: Float,
    knowledgePulse: Float,
    lineProgress: Float,
) {
    val spread = size.minDimension * 0.075f

    clusters.forEach { cluster ->
        val anchor =
            Offset(
                cluster.anchorX * size.width,
                cluster.anchorY * size.height,
            )
        val hubAlpha = (0.55f + cluster.knowledgeIntensity * 0.4f) * twinkle * knowledgePulse
        val hubRadius = 2.8f + cluster.knowledgeIntensity * 1.8f

        drawCircle(
            color = knowledgeColor.copy(alpha = hubAlpha * 0.25f),
            radius = hubRadius * 5f,
            center = anchor,
        )

        draw4PointCosmicStar(
            center = anchor,
            size = hubRadius,
            color = Color.White.copy(alpha = hubAlpha.coerceIn(0f, 1f)),
            glowColor = knowledgeColor,
            glowAlpha = hubAlpha * 0.85f,
            glowBlurFactor = 1.6f,
            glowSpreadFactor = 1.25f,
            rotationDegrees = (cluster.chapterId % 24) * 7.5f,
        )

        val satellitePositions = mutableListOf<Offset>()
        cluster.satellites.forEach { satellite ->
            val position =
                anchor +
                    Offset(
                        satellite.offsetX * spread,
                        satellite.offsetY * spread,
                    )
            satellitePositions.add(position)

            val satAlpha =
                (satellite.alpha * twinkle * knowledgePulse * (0.7f + cluster.knowledgeIntensity * 0.3f))
                    .coerceIn(0f, 1f)
            val lineAlpha = satAlpha * lineProgress * 0.55f

            if (lineAlpha > 0.04f) {
                drawLine(
                    color = knowledgeColor.copy(alpha = lineAlpha),
                    start = anchor,
                    end = position,
                    strokeWidth = 1.1f,
                )
            }

            drawCircle(
                color = knowledgeColor.copy(alpha = satAlpha * 0.35f),
                radius = satellite.size * 3.2f,
                center = position,
            )
            drawCircle(
                color = Color.White.copy(alpha = satAlpha),
                radius = satellite.size,
                center = position,
            )
        }

        if (lineProgress > 0.2f && satellitePositions.size > 1) {
            satellitePositions.forEachIndexed { i, a ->
                satellitePositions.drop(i + 1).forEach { b ->
                    val distance = sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
                    if (distance < spread * 1.35f) {
                        val meshAlpha = (1f - distance / (spread * 1.35f)) * lineProgress * 0.18f
                        if (meshAlpha > 0.03f) {
                            drawLine(
                                color = lineColor.copy(alpha = meshAlpha),
                                start = a,
                                end = b,
                                strokeWidth = 0.8f,
                            )
                        }
                    }
                }
            }
        }
    }
}
