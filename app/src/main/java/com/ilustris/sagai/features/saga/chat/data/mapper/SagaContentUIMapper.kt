package com.ilustris.sagai.features.saga.chat.data.mapper

import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData

object SagaContentUIMapper {
    /**
     * Maps domain acts to UI display data with pre-reversed lists.
     * This avoids calling .reversed() in the UI layer during LazyColumn rendering,
     * which would create new list copies on every recomposition.
     */
    fun mapToActDisplayData(domainActs: List<ActContent>): List<ActDisplayData> =
        domainActs.asReversed().map { actContentDomain ->
            ActDisplayData(
                content = actContentDomain,
                isComplete = actContentDomain.isComplete(),
                chapters =
                    actContentDomain.chapters.asReversed().map { chapterContentDomain ->
                        ChapterDisplayData(
                            chapter = chapterContentDomain,
                            isComplete = chapterContentDomain.isComplete(),
                            timelineSummaries =
                                chapterContentDomain.events.asReversed().map {
                                    it.copy(
                                        messages = it.messages.sortedByDescending { m -> m.message.timestamp },
                                    )
                                },
                        )
                    },
            )
        }
}
