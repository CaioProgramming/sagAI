package com.ilustris.sagai.ui.animations

import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.narrative.domain.rollupContinuity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** A chapter hub in the universe canvas with orbiting knowledge micro-stars. */
data class ChapterKnowledgeCluster(
    val chapterId: Int,
    val chapterTitle: String,
    val anchorX: Float,
    val anchorY: Float,
    val satelliteCount: Int,
    val knowledgeIntensity: Float,
    val satellites: List<KnowledgeSatellite>,
)

/** Normalized offset from chapter anchor (multiplied by spread in canvas). */
data class KnowledgeSatellite(
    val offsetX: Float,
    val offsetY: Float,
    val size: Float,
    val alpha: Float,
)

fun SagaContent.buildChapterKnowledgeClusters(
    maxSatellitesPerChapter: Int = 12,
): List<ChapterKnowledgeCluster> {
    val chapters = flatChapters().filter { it.knowledgeFactCount() > 0 }
    if (chapters.isEmpty()) return emptyList()

    val maxFacts = chapters.maxOf { it.knowledgeFactCount() }.coerceAtLeast(1)

    return chapters.mapIndexed { index, chapter ->
        val factCount = chapter.knowledgeFactCount()
        val satelliteCount = mapFactCountToSatellites(factCount, maxSatellitesPerChapter)
        val intensity = (factCount.toFloat() / maxFacts).coerceIn(0.25f, 1f)
        val (anchorX, anchorY) = chapterAnchorPosition(index, chapters.size)

        ChapterKnowledgeCluster(
            chapterId = chapter.data.id,
            chapterTitle = chapter.data.title,
            anchorX = anchorX,
            anchorY = anchorY,
            satelliteCount = satelliteCount,
            knowledgeIntensity = intensity,
            satellites = generateKnowledgeSatellites(chapter.data.id, satelliteCount),
        )
    }
}

fun SagaContent.totalKnowledgeFactCount(): Int =
    flatChapters().sumOf { it.knowledgeFactCount() }

private fun ChapterContent.knowledgeFactCount(): Int {
    val summary = data.continuitySummary?.takeUnless { it.isBlank() } ?: rollupContinuity()
    if (summary.isBlank()) return 0
    return summary.establishedFacts.size +
        summary.openThreads.size +
        summary.consequences.size +
        summary.characterStates.size +
        summary.persistentSetups.size
}

private fun ContinuitySummary.isBlank(): Boolean =
    establishedFacts.isEmpty() &&
        openThreads.isEmpty() &&
        consequences.isEmpty() &&
        characterStates.isEmpty() &&
        persistentSetups.isEmpty()

internal fun mapFactCountToSatellites(
    factCount: Int,
    maxSatellites: Int,
): Int =
    when {
        factCount <= 0 -> 0
        factCount <= 2 -> 1
        factCount <= 5 -> 2
        factCount <= 10 -> 4
        factCount <= 20 -> 6
        else -> maxSatellites.coerceAtMost(8 + factCount / 5)
    }.coerceIn(0, maxSatellites)

/** Golden-spiral placement keeps chapter hubs spread across the sky. */
internal fun chapterAnchorPosition(
    index: Int,
    total: Int,
): Pair<Float, Float> {
    if (total <= 1) return 0.5f to 0.38f

    val goldenAngle = 2.399963f
    val angle = index * goldenAngle
    val radius = 0.1f + (index.toFloat() / total.coerceAtLeast(1)) * 0.32f
    val x = 0.5f + radius * cos(angle)
    val y = 0.42f + radius * sin(angle) * 0.72f
    return x.coerceIn(0.1f, 0.9f) to y.coerceIn(0.14f, 0.86f)
}

internal fun generateKnowledgeSatellites(
    chapterId: Int,
    count: Int,
): List<KnowledgeSatellite> {
    if (count <= 0) return emptyList()
    val random = Random(chapterId * 31 + 7)

    return List(count) { satelliteIndex ->
        val angle = (satelliteIndex.toFloat() / count) * 2f * Math.PI.toFloat()
        val ring = 0.55f + random.nextFloat() * 0.85f
        val jitterX = (random.nextFloat() - 0.5f) * 0.35f
        val jitterY = (random.nextFloat() - 0.5f) * 0.35f
        KnowledgeSatellite(
            offsetX = cos(angle) * ring + jitterX,
            offsetY = sin(angle) * ring + jitterY,
            size = random.nextFloat() * 0.55f + 0.45f,
            alpha = random.nextFloat() * 0.35f + 0.45f,
        )
    }
}
