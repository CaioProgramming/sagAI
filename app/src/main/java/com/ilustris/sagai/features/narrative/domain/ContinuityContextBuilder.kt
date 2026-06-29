package com.ilustris.sagai.features.narrative.domain

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.narrative.data.model.distantCanonSlice
import com.ilustris.sagai.features.narrative.data.model.limitDistantFacts
import com.ilustris.sagai.features.narrative.data.model.mergeAll

data class ChapterContinuityEntry(
    val chapterNumber: Int,
    val chapterTitle: String,
    val summary: ContinuitySummary,
)

data class ChatContinuityContext(
    val currentScene: com.ilustris.sagai.features.saga.chat.data.model.SceneSummary?,
    val currentChapterRollup: ContinuitySummary?,
    val recentChapterCanon: List<ChapterContinuityEntry>,
    val distantCanon: ContinuitySummary?,
    val actContinuity: ContinuitySummary?,
    val globalWorldState: String?,
) {
    fun toContextMap(): Map<String, Any?> =
        buildMap {
            currentScene?.let { put("currentScene", it.asMap()) }
            currentChapterRollup?.let { put("currentChapterRollup", it.asMap()) }
            if (recentChapterCanon.isNotEmpty()) {
                put(
                    "recentChapterCanon",
                    recentChapterCanon.map {
                        mapOf(
                            "chapterNumber" to it.chapterNumber,
                            "chapterTitle" to it.chapterTitle,
                            "summary" to it.summary.asMap(),
                        )
                    },
                )
            }
            distantCanon?.let { put("distantCanon", it.asMap()) }
            actContinuity?.let { put("actContinuity", it.asMap()) }
            globalWorldState?.takeIf { it.isNotBlank() }?.let { put("globalWorldState", it) }
        }
}

fun SagaContent.buildChatContinuityContext(rules: NarrativeRules): ChatContinuityContext {
    val currentAct = currentActInfo
    val currentChapter = currentAct?.currentChapterInfo
    val currentScene = getCurrentTimeLine()?.data?.sceneSummary
    val currentChapterRollup =
        currentChapter
            ?.rollupContinuity()
            ?.takeUnless { it.isBlank() }

    val actChapters = currentAct?.chapters.orEmpty()
    val currentChapterIndex =
        currentChapter?.let { chapter ->
            actChapters.indexOfFirst { it.data.id == chapter.data.id }
        } ?: -1

    val recentChapterCanon =
        if (currentChapterIndex > 0) {
            actChapters
                .take(currentChapterIndex)
                .takeLast(rules.continuityRecentChapters)
                .mapNotNull { chapter -> chapter.toContinuityEntry(this, rules) }
        } else {
            emptyList()
        }

    val distantSources = mutableListOf<ContinuitySummary>()

    actChapters
        .take(if (currentChapterIndex > 0) currentChapterIndex else actChapters.size)
        .dropLast(recentChapterCanon.size.coerceAtMost(actChapters.size))
        .forEach { chapter ->
            chapter.resolveContinuity(rules)?.distantCanonSlice()?.let { distantSources.add(it) }
        }

    acts
        .filter { currentAct == null || it.data.id != currentAct.data.id }
        .forEach { act -> collectActDistantCanon(act, distantSources) }

    currentAct?.data?.continuitySummary?.distantCanonSlice()?.let { distantSources.add(it) }

    val distantCanon =
        distantSources
            .mergeAll()
            .limitDistantFacts(rules.continuityDistantFactsLimit)
            .takeUnless { it.isBlank() }

    val actContinuity =
        currentAct
            ?.data
            ?.continuitySummary
            ?.takeUnless { it.isBlank() }

    return ChatContinuityContext(
        currentScene = currentScene,
        currentChapterRollup = currentChapterRollup,
        recentChapterCanon = recentChapterCanon,
        distantCanon = distantCanon,
        actContinuity = actContinuity,
        globalWorldState = data.worldState,
    )
}

private fun collectActDistantCanon(
    act: ActContent,
    distantSources: MutableList<ContinuitySummary>,
) {
    act.data.continuitySummary?.distantCanonSlice()?.let { distantSources.add(it) }
    act.chapters.forEach { chapter ->
        chapter.data.continuitySummary?.distantCanonSlice()?.let { distantSources.add(it) }
    }
}

private fun ChapterContent.toContinuityEntry(
    saga: SagaContent,
    rules: NarrativeRules,
): ChapterContinuityEntry? {
    val summary = resolveContinuity(rules) ?: return null
    return ChapterContinuityEntry(
        chapterNumber = saga.chapterNumber(data),
        chapterTitle = data.title.ifBlank { "Chapter ${saga.chapterNumber(data)}" },
        summary = summary,
    )
}

private fun ChapterContent.resolveContinuity(rules: NarrativeRules): ContinuitySummary? {
    val stored = data.continuitySummary?.takeUnless { it.isBlank() }
    if (stored != null) return stored
    if (!isComplete(rules)) return null
    return rollupContinuity().takeUnless { it.isBlank() }
}
