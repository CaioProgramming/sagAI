package com.ilustris.sagai.features.saga.detail.data.usecase.mapper

import android.graphics.Bitmap
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaInfo
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
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
    suspend fun buildDrawer(
        saga: Saga,
        chapters: List<com.ilustris.sagai.features.chapter.data.model.ChapterContent>,
        completedActsCount: Int,
    ): TimelineDrawer {
        val narrativeRules = remoteConfigService.getNarrativeRules()
        val actProgress = completedActsCount.toFloat() / narrativeRules.maxActsLimit
        return TimelineDrawer(
            saga.title,
            chapters.mapIndexed { index, it ->
                val chapterProgress =
                    it.events
                        .count { event -> event.isComplete(narrativeRules) }
                        .toFloat() / narrativeRules.chapterUpdateLimit
                TimelineDrawerGroup(
                    it.data.title.ifEmpty {
                        stringResourceHelper.getString(
                            R.string.chapter_number_label,
                            (index + 1).toRoman(),
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
        resume: SagaDetailResume,
        section: RequestSection,
        existingSegmentedImage: Pair<Bitmap, Bitmap>? = null,
    ) = executeRequest {
        when (section) {
            RequestSection.START -> createInitialSection(resume, existingSegmentedImage)
            else -> createInitialSection(resume, existingSegmentedImage)
        }
    }

    suspend fun createInitialSection(
        resume: SagaDetailResume,
        existingSegmentedImage: Pair<Bitmap, Bitmap>? = null,
    ): DetailSectionView {
        val saga = resume.saga
        remoteConfigService.getNarrativeRules()
        val subtitle =
            if (saga.isEnded) {
                stringResourceHelper.getString(
                    R.string.saga_ended_on,
                    saga.endedAt.formatDate(),
                )
            } else {
                stringResourceHelper.getString(
                    R.string.saga_detail_status_created,
                    saga.createdAt.formatDate(),
                )
            }

        val segmentedImage =
            existingSegmentedImage ?: runCatching {
                if (saga.icon.isBlank()) {
                    null
                } else {
                    imageSegmentationHelper.processImage(saga.icon).getSuccess()
                }
            }.getOrNull()

        val emotionalCard =
            if (saga.emotionalReview != null) {
                remoteConfigService.getString("mental_card_icon")
            } else {
                null
            }

        return DetailSectionView.InitialSection(
            title = saga.title,
            subtitle = subtitle,
            saga = saga,
            segmentedImage = segmentedImage,
            emotionalCard = emotionalCard,
            starring = resume.starringCharacter,
            topCharacters = resume.topCharacters,
            relationships = emptyList(),
            lastEvent =
                resume.latestEvent?.let {
                    timelineMapper.buildTimeline(
                        SagaInfo(
                            id = saga.id,
                            title = saga.title,
                            genre = saga.genre,
                            variationId = saga.variationId,
                            icon = saga.icon,
                        ),
                        it.timelineContent.toMetaData(),
                    )
                },
            latestWikis = resume.latestWikis,
            books = resume.generatedBooks,
            chaptersCount = resume.chaptersCount,
            hasActs = resume.hasActs,
            endMessage = saga.endMessage,
            readyToReview = false,
        )
    }
}

fun TimelineContent.toMetaData(): TimelineMetadata =
    TimelineMetadata(
        data = data,
    )

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
