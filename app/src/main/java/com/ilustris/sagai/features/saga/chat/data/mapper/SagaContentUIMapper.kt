package com.ilustris.sagai.features.saga.chat.data.mapper

import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData

object SagaContentUIMapper {
    fun mapToActDisplayData(domainActs: List<ActContent>): List<ActDisplayData> =
        domainActs.map { actContentDomain ->
            ActDisplayData(
                content = actContentDomain,
                isComplete = actContentDomain.isComplete(),
                chapters =
                    actContentDomain.chapters.map { chapterContentDomain ->
                        ChapterDisplayData(
                            chapter = chapterContentDomain,
                            isComplete = chapterContentDomain.isComplete(),
                            timelineSummaries =
                                chapterContentDomain.events.map {
                                    it.copy(
                                        messages = it.messages.sortedBy { m -> m.message.timestamp },
                                    )
                                },
                        )
                    },
            )
        }
}
