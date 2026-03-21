package com.ilustris.sagai.features.saga.chat.data.mapper

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.TimelineDisplayData

object SagaContentUIMapper {
    /**
     * Maps domain acts to UI display data with pre-reversed lists.
     * This avoids calling .reversed() in the UI layer during LazyColumn rendering,
     * which would create new list copies on every recomposition.
     */
    fun mapToActDisplayData(
        domainActs: List<ActContent>,
        rules: NarrativeRules,
    ): List<ActDisplayData> =
        domainActs.asReversed().map { actContentDomain ->
            ActDisplayData(
                content = actContentDomain,
                isComplete = actContentDomain.isComplete(rules),
                chapters =
                    actContentDomain.chapters.asReversed().map { chapterContentDomain ->
                        ChapterDisplayData(
                            chapter = chapterContentDomain,
                            isComplete = chapterContentDomain.isComplete(rules),
                            timelineSummaries =
                                chapterContentDomain.events.asReversed().map {
                                    TimelineDisplayData(
                                        isComplete = it.isComplete(rules),
                                        timeline =
                                            it.copy(
                                                messages = it.messages.sortedByDescending { it.message.timestamp },
                                            ),
                                    )
                                },
                        )
                    },
            )
        }
}
