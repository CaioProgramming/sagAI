package com.ilustris.sagai.features.saga.detail.data.usecase.mapper

import coil3.Bitmap
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.saga.detail.ui.DetailAction

sealed class DetailSection(
    open val title: String,
    open val subtitle: String,
) {
    data class InitialSection(
        override val title: String,
        override val subtitle: String,
        val sagaResume: String,
        val segmentedImage: Pair<Bitmap, Bitmap>?,
        val emotionalCard: String?,
    ) : DetailSection(
            title,
            subtitle,
        )
}

enum class RequestSection {
    CHARACTERS,
    WIKI,
    EVENTS,
    CHAPTERS,
    ACTS,
    EMOTIONAL_PROFILE,
}

class SagaDetailUIMapper(
    private val stringResourceHelper: StringResourceHelper,
    private val remoteConfigService: RemoteConfigService,
    private val imageSegmentationHelper: ImageSegmentationHelper,
) {
    suspend fun buildDrawer(saga: SagaContent): List<TimelineDrawer> {
        val narrativeRules = remoteConfigService.getNarrativeRules()

        return saga.acts.map {
            val actProgress =
                it.chapters.filter { it.isComplete(narrativeRules) }.size - narrativeRules.actUpdateLimit / 100f

            TimelineDrawer(
                it.data.title.ifEmpty { saga.actNumber(it.data).toRoman() },
                it.chapters.map {
                    val chapterProgress =
                        it.events.filter { it.isComplete(narrativeRules) }.size - narrativeRules.chapterUpdateLimit / 100f
                    TimelineDrawerGroup(
                        it.data.title.ifEmpty {
                            saga.chapterNumber(it.data).toRoman()
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
                actProgress,
            )
        }
    }

    suspend fun buildSection(
        sagaContent: SagaContent,
        action: DetailAction,
    ) = executeRequest {
        when (action) {
            DetailAction.CHARACTERS -> TODO()
            DetailAction.TIMELINE -> TODO()
            DetailAction.CHAPTERS -> TODO()
            DetailAction.WIKI -> TODO()
            DetailAction.ACTS -> TODO()
            DetailAction.BACK -> TODO()
            DetailAction.DELETE -> TODO()
            DetailAction.REGENERATE -> TODO()
        }
    }

    suspend fun createInitialSection() {
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
