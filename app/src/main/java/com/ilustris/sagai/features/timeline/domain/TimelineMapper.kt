package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.services.MascotEmotionService
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.findChapter
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

class TimelineMapper(
    private val mascotEmotionService: MascotEmotionService,
    private val stringResourceHelper: StringResourceHelper,
    private val genreVisualConfigService: GenreVisualConfigService,
) {
    suspend fun buildTimelines(sagaContent: SagaContent) =
        TimelineViewContent(
            saga = sagaContent,
            visualConfig = genreVisualConfigService.getVisualConfig(sagaContent.data.genre),
            title = stringResourceHelper.getString(R.string.saga_detail_section_title_timeline),
            subtitle =
                stringResourceHelper.getString(
                    R.string.saga_detail_section_subtitle_timeline,
                    sagaContent.eventsSize(),
                ),
            groups =
                sagaContent.acts.map {
                    val chapters = it.chapters
                    TimelineGroup(
                        title =
                            it.data.title.ifEmpty {
                                stringResourceHelper.getString(
                                    R.string.act_title,
                                    sagaContent.actNumber(it.data).toRoman(),
                                )
                            },
                        events =
                            chapters
                                .flatMap { chapterContent -> chapterContent.events }
                                .mapIndexed { index, content ->
                                    val chapterNumber =
                                        if (index == 0) {
                                            sagaContent
                                                .chapterNumber(sagaContent.findChapter(content.data.chapterId)?.data)
                                                .toRoman()
                                        } else {
                                            null
                                        }

                                    val topEmotion =
                                        content.emotionalRanking().first().first
                                            ?: EmotionalTone.NEUTRAL

                                    TimelineCardContent(
                                        content,
                                        topEmotion to
                                            mascotEmotionService.getEmotionUrl(
                                                sagaContent.data.genre,
                                                topEmotion,
                                            ),
                                        chapterNumber,
                                    )
                                },
                    )
                },
        )
}

data class TimelineViewContent(
    val saga: SagaContent,
    val title: String,
    val subtitle: String,
    val groups: List<TimelineGroup>,
    val visualConfig: GenreVisualConfig?,
)

data class TimelineGroup(
    val title: String,
    val events: List<TimelineCardContent>,
)

data class TimelineCardContent(
    val timelineContent: TimelineContent,
    val mascotEmotion: Pair<EmotionalTone, String?>?,
    val chapterNumber: String?,
)
