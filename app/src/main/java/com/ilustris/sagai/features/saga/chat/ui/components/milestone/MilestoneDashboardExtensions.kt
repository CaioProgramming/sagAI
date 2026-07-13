package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

fun ChapterContent.eventEmotionalBreakdown(): List<Pair<EmotionalTone, Int>> =
    events
        .mapNotNull { event ->
            event.data.emotionalTone ?: event.emotionalRanking().firstOrNull()?.first
        }.groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .map { it.toPair() }

fun ContinuitySummary.toMilestoneSections(resourceHelper: StringResourceHelper): Map<String, String> {
    if (isBlank()) return emptyMap()
    return buildMap {
        if (establishedFacts.isNotEmpty()) {
            put(
                resourceHelper.getString(R.string.milestone_label_established_facts),
                establishedFacts.joinToString("\n") { "• $it" },
            )
        }
        if (openThreads.isNotEmpty()) {
            put(
                resourceHelper.getString(R.string.milestone_label_open_threads),
                openThreads.joinToString("\n") { "• $it" },
            )
        }
        if (consequences.isNotEmpty()) {
            put(
                resourceHelper.getString(R.string.milestone_label_consequences),
                consequences.joinToString("\n") { "• $it" },
            )
        }
        if (characterStates.isNotEmpty()) {
            put(
                resourceHelper.getString(R.string.milestone_label_character_states),
                characterStates.joinToString("\n") { "• $it" },
            )
        }
        if (persistentSetups.isNotEmpty()) {
            put(
                resourceHelper.getString(R.string.milestone_label_persistent_setups),
                persistentSetups.joinToString("\n") { "• $it" },
            )
        }
    }
}

fun ContinuitySummary.factCount(): Int =
    establishedFacts.size +
        openThreads.size +
        consequences.size +
        characterStates.size +
        persistentSetups.size

fun ContinuitySummary.isBlank(): Boolean = factCount() == 0

fun ChapterContent.toMilestoneItems(
    resourceHelper: StringResourceHelper,
    sagaContent: SagaContent,
): List<MilestoneDashboardItem> =
    buildList {
        val chapter = data
        if (chapter.introduction.isNotBlank()) {
            add(
                MilestoneDashboardItem(
                    title = chapter.title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_introduction),
                    content = chapter.introduction,
                    iconRes = R.drawable.ic_note,
                    fullWidth = true,
                    kind = MilestoneCardKind.Narrative,
                ),
            )
        }
        if (chapter.content.isNotBlank()) {
            add(
                MilestoneDashboardItem(
                    title = chapter.title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_chapter_summary),
                    content = chapter.content,
                    iconRes = R.drawable.center_spark,
                    fullWidth = true,
                    kind = MilestoneCardKind.Narrative,
                ),
            )
        }
        val emotionBreakdown = eventEmotionalBreakdown()
        val hasEmotionalReview = chapter.emotionalReview?.isNotBlank() == true
        if (hasEmotionalReview || emotionBreakdown.isNotEmpty()) {
            add(
                MilestoneDashboardItem(
                    title = chapter.title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_emotional_review),
                    content = chapter.emotionalReview,
                    iconRes = R.drawable.ic_review,
                    fullWidth = emotionBreakdown.isEmpty(),
                    kind =
                        if (emotionBreakdown.isNotEmpty()) {
                            MilestoneCardKind.Emotional
                        } else {
                            MilestoneCardKind.Narrative
                        },
                    emotionBreakdown = emotionBreakdown,
                ),
            )
        }
        chapter.continuitySummary?.takeUnless { it.isBlank() }?.let { continuity ->
            add(continuity.toDashboardItem(resourceHelper, chapter.title))
        }
        val sagaId = sagaContent.data.id.toString()
        if (chapter.featuredCharacters.isNotEmpty()) {
            val characters =
                chapter.featuredCharacters.mapNotNull { id ->
                    sagaContent.findCharacter(id)?.data
                }
            if (characters.isNotEmpty()) {
                val characterAction =
                    if (characters.size == 1) {
                        MilestoneDetailAction.OpenCharacter(characters.first().id)
                    } else {
                        MilestoneDetailAction.OpenCharacters(sagaId)
                    }
                add(
                    MilestoneDashboardItem(
                        title = chapter.title,
                        subtitle = resourceHelper.getString(R.string.milestone_label_featured_characters),
                        value = characters.size.toString(),
                        iconRes = R.drawable.character_icon,
                        chipCharacters = characters,
                        kind = MilestoneCardKind.Stat,
                        detailAction = characterAction,
                    ),
                )
            }
        }
    }

fun Act.toMilestoneItems(
    resourceHelper: StringResourceHelper,
    sagaId: Int,
): List<MilestoneDashboardItem> =
    buildList {
        val chronicleAction = MilestoneDetailAction.OpenBookReader(sagaId, id)
        if (introduction.isNotBlank()) {
            add(
                MilestoneDashboardItem(
                    title = title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_introduction),
                    content = introduction,
                    iconRes = R.drawable.ic_note,
                    fullWidth = true,
                    kind = MilestoneCardKind.Narrative,
                    detailAction = chronicleAction,
                ),
            )
        }
        if (content.isNotBlank()) {
            add(
                MilestoneDashboardItem(
                    title = title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_act_summary),
                    content = content,
                    iconRes = R.drawable.center_spark,
                    fullWidth = true,
                    kind = MilestoneCardKind.Narrative,
                    detailAction = chronicleAction,
                ),
            )
        }
        emotionalReview?.takeIf { it.isNotBlank() }?.let { review ->
            add(
                MilestoneDashboardItem(
                    title = title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_emotional_review),
                    content = review,
                    iconRes = R.drawable.ic_review,
                    fullWidth = true,
                    kind = MilestoneCardKind.Narrative,
                ),
            )
        }
        continuitySummary?.takeUnless { it.isBlank() }?.let { continuity ->
            add(continuity.toDashboardItem(resourceHelper, title))
        }
    }

private fun ContinuitySummary.toDashboardItem(
    resourceHelper: StringResourceHelper,
    entityTitle: String,
): MilestoneDashboardItem {
    val sections = toMilestoneSections(resourceHelper)
    val preview =
        sections.values
            .firstOrNull()
            ?.lineSequence()
            ?.take(2)
            ?.joinToString("\n")
    return MilestoneDashboardItem(
        title = entityTitle,
        subtitle = resourceHelper.getString(R.string.milestone_label_continuity),
        value = factCount().toString(),
        content = preview,
        iconRes = R.drawable.ic_globe,
        displayContent = sections,
        kind = MilestoneCardKind.Continuity,
    )
}
