package com.ilustris.sagai.features.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.domain.TimelineMapper
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.timeline.domain.TimelineViewContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TimelineAction {
    data class ReviewTimeline(
        val saga: SagaContent,
        val timeline: Timeline,
    ) : TimelineAction()
}

@HiltViewModel
class TimelineViewModel
    @Inject
    constructor(
        private val timelineUseCase: TimelineUseCase,
        private val timelineMapper: TimelineMapper,
    ) : ViewModel() {
        val timelineView = MutableStateFlow<TimelineViewContent?>(null)

        fun handleAction(timelineAction: TimelineAction) {
            viewModelScope.launch {
                when (timelineAction) {
                    is TimelineAction.ReviewTimeline -> {
                        val timelineContent =
                            timelineAction.saga.findTimeline(timelineAction.timeline.id)
                        timelineContent?.let {
                            timelineUseCase.generateTimelineContent(
                                timelineAction.saga,
                                it,
                            )
                        }
                    }
                }
            }
        }

        fun buildTimeline(sagaContent: SagaContent) {
            viewModelScope.launch(Dispatchers.IO) {
                timelineView.emit(null)
                timelineView.emit(timelineMapper.buildTimelines(sagaContent))
            }
        }
    }
