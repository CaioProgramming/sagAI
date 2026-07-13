package com.ilustris.sagai.core.ai.model

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.Timeline

data class LoreContent(
    val title: String = "",
    val content: String = "",
    val emotionalReview: String? = null,
    val narrativeGuide: String? = null,
    val emotionalTone: EmotionalTone? = null,
) {
    fun isComplete(): Boolean = title.isNotEmpty() && content.isNotEmpty()
}

data class GeneratedTimeline(
    val title: String = "",
    val content: String = "",
    val emotionalReview: String? = null,
    val narrativeGuide: String? = null,
    val emotionalTone: EmotionalTone? = null,
    val sceneSummary: SceneSummary? = null,
    val currentObjective: String? = null,
) {
    fun toLoreContent(): LoreContent =
        LoreContent(
            title = title,
            content = content,
            emotionalReview = emotionalReview,
            narrativeGuide = narrativeGuide,
            emotionalTone = emotionalTone,
        )

    fun mergeInto(existing: Timeline): Timeline =
        existing.copy(
            title = title,
            content = content,
            emotionalReview = emotionalReview,
            narrativeGuide = narrativeGuide,
            emotionalTone = emotionalTone ?: existing.emotionalTone,
            sceneSummary = sceneSummary ?: existing.sceneSummary,
            currentObjective = currentObjective ?: existing.currentObjective,
        )
}

data class GeneratedChapter(
    val title: String = "",
    val content: String = "",
    val introduction: String = "",
    val emotionalReview: String? = null,
    val narrativeGuide: String? = null,
    val emotionalTone: EmotionalTone? = null,
    val featuredCharacters: List<Int> = emptyList(),
    val artwork: String? = null,
) {
    fun toLoreContent(): LoreContent =
        LoreContent(
            title = title,
            content = content,
            emotionalReview = emotionalReview,
            narrativeGuide = narrativeGuide,
            emotionalTone = emotionalTone,
        )

    fun mergeInto(existing: Chapter): Chapter =
        existing.copy(
            title = title,
            content = content,
            introduction = introduction,
            emotionalReview = emotionalReview,
            narrativeGuide = narrativeGuide,
            featuredCharacters = featuredCharacters.ifEmpty { existing.featuredCharacters },
            artwork = artwork ?: existing.artwork,
        )
}

data class GeneratedAct(
    val title: String = "",
    val content: String = "",
    val introduction: String = "",
    val emotionalReview: String? = null,
    val narrativeGuide: String? = null,
    val emotionalTone: EmotionalTone? = null,
) {
    fun toLoreContent(): LoreContent =
        LoreContent(
            title = title,
            content = content,
            emotionalReview = emotionalReview,
            narrativeGuide = narrativeGuide,
            emotionalTone = emotionalTone,
        )

    fun mergeInto(existing: Act): Act =
        existing.copy(
            title = title,
            content = content,
            introduction = introduction,
            emotionalReview = emotionalReview,
            narrativeGuide = narrativeGuide,
        )
}

fun Timeline.toLoreContent(): LoreContent =
    LoreContent(
        title = title,
        content = content,
        emotionalReview = emotionalReview,
        narrativeGuide = narrativeGuide,
        emotionalTone = emotionalTone,
    )

fun Chapter.toLoreContent(): LoreContent =
    LoreContent(
        title = title,
        content = content,
        emotionalReview = emotionalReview,
        narrativeGuide = narrativeGuide,
    )

fun Act.toLoreContent(): LoreContent =
    LoreContent(
        title = title,
        content = content,
        emotionalReview = emotionalReview,
        narrativeGuide = narrativeGuide,
    )
