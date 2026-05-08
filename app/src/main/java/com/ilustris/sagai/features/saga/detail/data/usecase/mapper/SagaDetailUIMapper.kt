package com.ilustris.sagai.features.saga.detail.data.usecase.mapper

import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.timeline.domain.TimelineMapper
import com.ilustris.sagai.features.wiki.data.mapper.WikiMapper
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase

enum class RequestSection {
    CHARACTERS,
    EVENTS,

    WIKI,
    CHAPTERS,
    ACTS,
    START,
}

class SagaDetailUIMapper(
    private val stringResourceHelper: StringResourceHelper,
    private val remoteConfigService: RemoteConfigService,
    private val imageSegmentationHelper: ImageSegmentationHelper,
    private val sagaDetailUseCase: SagaDetailUseCase,
    private val timelineMapper: TimelineMapper,
    private val wikiMapper: WikiMapper,
    private val emotionalUseCase: EmotionalUseCase,
) {
    suspend fun buildDrawer(saga: SagaContent): TimelineDrawer {
        val narrativeRules = remoteConfigService.getNarrativeRules()
        val actProgress =
            saga.completedActs(narrativeRules).toFloat() / narrativeRules.maxActsLimit
        return TimelineDrawer(
            saga.data.title,
            saga.flatChapters().map {
                val chapterProgress =
                    it.events
                        .count { event -> event.isComplete(narrativeRules) }
                        .toFloat() / narrativeRules.chapterUpdateLimit
                TimelineDrawerGroup(
                    it.data.title.ifEmpty {
                        stringResourceHelper.getString(
                            R.string.chapter_number_label,
                            saga.chapterNumber(it.data).toRoman(),
                        )
                    },
                    progress = chapterProgress,
                    items =
                        it.events.map {
                            TimelineDrawerItem(
                                it.data.title,
                                it.data.createdAt.formatDate(),
                                it.isComplete(narrativeRules),
                            )
                        },
                )
            },
            progress = actProgress,
        )
    }

    suspend fun buildSection(
        sagaContent: SagaContent,
        section: RequestSection,
    ) = executeRequest {
        when (section) {
            RequestSection.START -> createInitialSection(sagaContent)
            else -> createInitialSection(sagaContent)
        }
    }

    suspend fun createInitialSection(saga: SagaContent): DetailSectionView {
        val narrativeRules = remoteConfigService.getNarrativeRules()
        val subtitle =
            if (saga.data.isEnded) {
                stringResourceHelper.getString(
                    R.string.saga_ended_on,
                    saga.data.endedAt.formatDate(),
                )
            } else {
                stringResourceHelper.getString(
                    R.string.saga_detail_status_created,
                    saga.data.createdAt.formatDate(),
                )
            }

        val segmentedImage =
            runCatching {
                if (saga.data.icon.isBlank()) {
                    null
                } else {
                    imageSegmentationHelper.processImage(saga.data.icon).getSuccess()
                }
            }.getOrNull()

        val emotionalCard = emotionalUseCase.getEmotionalCard(saga).getSuccess()
        val topCharacters =
            saga
                .flatMessages()
                .rankTopCharacters(saga.characters.map { it.data })
                .mapNotNull { saga.findCharacter(it.first.id) }

        val lastEvent = saga.flatEvents().lastOrNull { it.isComplete(narrativeRules) }

        val timelineCard =
            lastEvent?.let {
                if (it.isComplete(narrativeRules).not()) return@let null
                timelineMapper.buildTimeline(saga, lastEvent)
            } ?: run {
                null
            }

        return DetailSectionView.InitialSection(
            title = saga.data.title,
            subtitle = subtitle,
            saga = saga,
            segmentedImage = segmentedImage,
            emotionalCard = emotionalCard,
            starring = saga.mainCharacter,
            characters = topCharacters,
            relationships = saga.relationships,
            lastEvent = timelineCard,
            acts = saga.acts.filter { it.isComplete(narrativeRules) },
            chapters = saga.flatChapters().filter { it.isComplete(narrativeRules) },
            endMessage = saga.data.endMessage,
            readyToReview = saga.isComplete(narrativeRules),
        )
    }
}

data class TimelineDrawer(
    val title: String,
    val group: List<TimelineDrawerGroup>,
    val progress: Float,
)

data class TimelineDrawerGroup(
    val title: String,
    val progress: Float,
    val items: List<TimelineDrawerItem>,
)

data class TimelineDrawerItem(
    val title: String,
    val subtitle: String,
    val isComplete: Boolean,
)
