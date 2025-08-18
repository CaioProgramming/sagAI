package com.ilustris.sagai.features.saga.chat.domain.mapper

import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.TimelineSummaryData

object SagaContentUIMapper {
    fun mapToActDisplayData(domainActs: List<ActContent>): List<ActDisplayData> =
        domainActs.map { actContentDomain ->
            ActDisplayData(
                content = actContentDomain,
                isComplete = actContentDomain.isComplete(),
                chapters =
                    actContentDomain.chapters.map { chapterContentDomain ->
                        ChapterDisplayData(
                            chapter = chapterContentDomain.data,
                            isComplete = chapterContentDomain.isComplete(),
                            timelineSummaries =
                                chapterContentDomain.events.map {
                                    TimelineSummaryData(
                                        id = it.timeline.id,
                                        title = it.timeline.title,
                                        content = it.timeline.content,
                                        messages = it.messages.sortedBy { m -> m.message.timestamp },
                                        isComplete = it.isComplete(),
                                    )
                                },
                        )
                    },
            )
        }
}
