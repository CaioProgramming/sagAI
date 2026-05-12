package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.services.MascotEmotionService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.home.data.model.SagaInfo
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.toMetaData
import com.ilustris.sagai.features.timeline.data.model.TimelineWithAct

class TimelineMapper(
    private val mascotEmotionService: MascotEmotionService,
    private val genreVisualConfigService: GenreVisualConfigService,
    private val remoteConfigService: RemoteConfigService,
) {
    suspend fun buildTimelines(
        sagaInfo: SagaInfo,
        timelineData: List<TimelineWithAct>,
    ) = TimelineViewContent(
        saga = sagaInfo,
        visualConfig = genreVisualConfigService.getVisualConfig(sagaInfo.genre),
        groups =
            timelineData
                .groupBy { it.actTitle }
                .map { (actTitle, events) ->
                    TimelineGroup(
                        title = actTitle,
                        events =
                            events.map {
                                buildTimeline(
                                    sagaInfo,
                                    it.timelineContent.toMetaData(),
                                )
                            },
                    )
                },
    )

    suspend fun buildTimeline(
        saga: SagaInfo,
        timelineContent: TimelineMetadata,
    ): TimelineCardContent {
        val narrativeRules = remoteConfigService.getNarrativeRules()
        val genre = saga.genre
        val topEmotion =
            timelineContent.data.emotionalTone
        val mascotPair =
            timelineContent.data.emotionalTone?.let {
                topEmotion to
                    mascotEmotionService.getEmotionUrl(
                        genre,
                        topEmotion,
                    )
            }

        return TimelineCardContent(
            timelineContent,
            mascotPair,
            null,
            timelineContent.isComplete(narrativeRules),
        )
    }
}

data class TimelineViewContent(
    val saga: SagaInfo,
    val groups: List<TimelineGroup>,
    val visualConfig: GenreVisualConfig?,
)

data class TimelineGroup(
    val title: String,
    val events: List<TimelineCardContent>,
)

data class TimelineCardContent(
    val timelineContent: TimelineMetadata,
    val mascotEmotion: Pair<EmotionalTone, String?>?,
    val chapterNumber: String?,
    val canShowData: Boolean,
)
