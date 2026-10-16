package com.ilustris.sagai.features.saga.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.progression.domain.CharacterProgressionUseCase
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** The current level of each character of the open saga, for the `LV N` next to their name in the chat. Empty when the setting is off. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatLevelsViewModel
    @Inject
    constructor(
        private val progressionUseCase: CharacterProgressionUseCase,
        private val settingsUseCase: SettingsUseCase,
    ) : ViewModel() {
        private val input = MutableStateFlow<Pair<Int, List<Character>>?>(null)

        val levels: StateFlow<Map<Int, Int>> =
            input
                .flatMapLatest { saga ->
                    if (saga == null) {
                        flowOf(emptyMap())
                    } else {
                        combine(progressionUseCase.observeBadges(saga.first, saga.second), settingsUseCase.getShowProgression()) { badges, show ->
                            if (show) badges.mapValues { it.value.level } else emptyMap()
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

        fun load(
            sagaId: Int,
            characters: List<Character>,
        ) {
            input.value = sagaId to characters
        }
    }
