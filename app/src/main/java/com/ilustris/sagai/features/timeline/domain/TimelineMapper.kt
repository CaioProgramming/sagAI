package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.services.MascotEmotionService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.findChapter
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

class TimelineMapper(
    private val mascotEmotionService: MascotEmotionService,
    private val stringResourceHelper: StringResourceHelper,
    private val genreVisualConfigService: GenreVisualConfigService,
    private val remoteConfigService: RemoteConfigService,
) {
    suspend fun buildTimelines(sagaContent: SagaContent) =
        TimelineViewContent(
            saga = sagaContent,
            visualConfig = genreVisualConfigService.getVisualConfig(sagaContent.data.genre),
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
                                .mapNotNull {
                                    buildTimeline(
                                        sagaContent,
                                        it,
                                    )
                                },
                    )
                },
        )

    suspend fun buildTimeline(
        saga: SagaContent,
        timelineContent: TimelineContent,
    ): TimelineCardContent {
        val narrativeRules = remoteConfigService.getNarrativeRules()
        val genre = saga.data.genre
        val chapter = saga.findChapter(timelineContent.data.id)
        val chapterNumber = saga.chapterNumber(chapter?.data).toRoman()
        val topEmotion =
            timelineContent.emotionalRanking().firstOrNull()?.first ?: EmotionalTone.NEUTRAL
        val mascotEmotion =
            mascotEmotionService.getEmotionUrl(
                genre,
                topEmotion,
            )
        return TimelineCardContent(
            timelineContent,
            topEmotion to mascotEmotion,
            chapterNumber,
            timelineContent.isComplete(narrativeRules),
        )
    }
}

data class TimelineViewContent(
    val saga: SagaContent,
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
    val canShowData: Boolean,
)
