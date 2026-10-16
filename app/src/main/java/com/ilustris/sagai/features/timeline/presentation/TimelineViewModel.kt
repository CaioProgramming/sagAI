package com.ilustris.sagai.features.timeline.presentation

import com.ilustris.sagai.features.progression.domain.CharacterProgressionUseCase
import com.ilustris.sagai.features.progression.domain.EventXp
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import kotlinx.coroutines.flow.flatMapLatest
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
        private val progressionUseCase: CharacterProgressionUseCase,
        private val settingsUseCase: SettingsUseCase,
    ) : ViewModel() {
        val timelineView = MutableStateFlow<TimelineViewContent?>(null)

        /** What each event paid each character (timelineId -> characterId -> XP); empty when the setting hides experience. */
        val eventXp = MutableStateFlow<Map<Int, Map<Int, EventXp>>>(emptyMap())

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        fun loadSaga(sagaId: Int) {
            viewModelScope.launch {
                sagaRepository
                    .getSagaById(sagaId)
                    .flatMapLatest { content ->
                        combine(
                            progressionUseCase.observeEventXp(sagaId, content?.characters?.map { it.data }.orEmpty()),
                            settingsUseCase.getShowProgression(),
                        ) { xp, show -> if (show) xp else emptyMap() }
                    }.collectLatest { eventXp.value = it }
            }
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
