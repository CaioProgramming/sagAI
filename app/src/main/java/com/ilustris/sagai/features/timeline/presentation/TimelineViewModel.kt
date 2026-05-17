package com.ilustris.sagai.features.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.timeline.domain.TimelineMapper
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.timeline.domain.TimelineViewContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TimelineAction {
    data class ReviewTimeline(
        val sagaId: Int,
        val timelineId: Int,
    ) : TimelineAction()
}

@HiltViewModel
class TimelineViewModel
    @Inject
    constructor(
        private val timelineUseCase: TimelineUseCase,
        private val timelineMapper: TimelineMapper,
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        val timelineView = MutableStateFlow<TimelineViewContent?>(null)

        fun loadSaga(sagaId: Int) {
            viewModelScope.launch {
                combine(
                    sagaRepository.getSagaInfo(sagaId),
                    timelineUseCase.getTimelineWithActBySaga(sagaId),
                ) { sagaInfo, timelineData ->
                    if (sagaInfo != null) {
                        timelineMapper.buildTimelines(sagaInfo, timelineData)
                    } else {
                        null
                    }
                }.collectLatest {
                    timelineView.value = it
                }
            }
        }
    }
