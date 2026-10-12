package com.ilustris.sagai.features.characters.presentation

import com.ilustris.sagai.features.progression.domain.CharacterBadge
import com.ilustris.sagai.features.progression.domain.CharacterProgressionUseCase
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CharacterViewModel
    @Inject
    constructor(
        private val characterUseCase: CharacterUseCase,
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val stringResourceHelper: StringResourceHelper,
        private val progressionUseCase: CharacterProgressionUseCase,
        private val settingsUseCase: SettingsUseCase,
    ) : ViewModel() {
        private val _badges = MutableStateFlow<Map<Int, CharacterBadge>>(emptyMap())
        val badges: StateFlow<Map<Int, CharacterBadge>> = _badges.asStateFlow()
        private var badgesJob: kotlinx.coroutines.Job? = null

        private val _saga = MutableStateFlow<SagaContent?>(null)
        val saga: StateFlow<SagaContent?> = _saga.asStateFlow()

        private val _characters = MutableStateFlow<List<Character>>(emptyList())
        val characters: StateFlow<List<Character>> = _characters.asStateFlow()
        val state = MutableStateFlow<State>(State.Loading)

        fun loadCharacters(sagaId: Int?) {
            if (sagaId == null) {
                state.value = State.Error(stringResourceHelper.getString(R.string.saga_not_found))
                return
            }
            if (badgesJob == null) {
                badgesJob =
                    viewModelScope.launch {
                        sagaHistoryUseCase
                            .getSagaById(sagaId)
                            .flatMapLatest { content ->
                                val characters = content?.characters?.map { it.data }.orEmpty()
                                combine(progressionUseCase.observeBadges(sagaId, characters), settingsUseCase.getShowProgression()) { badges, show ->
                                    if (show) badges else emptyMap()
                                }
                            }.collect { _badges.value = it }
                    }
            }
            viewModelScope.launch {
                state.value = State.Loading
                sagaHistoryUseCase.getSagaById(sagaId).collect { currentSaga ->
                    _saga.value = currentSaga
                    currentSaga?.let {
                        val sortedCharacters = sortCharactersByMessageCount(it.getCharacters(), it.flatMessages())
                        _characters.value = sortedCharacters
                    }

                    state.value = State.Success(Unit)
                }
            }
        }

        fun addCharacter(character: Character) {
            viewModelScope.launch {
                characterUseCase.insertCharacter(character)
            }
        }

        fun updateCharacter(character: Character) {
            viewModelScope.launch {
                characterUseCase.updateCharacter(character)
            }
        }

        fun deleteCharacter(characterId: Int) {
            viewModelScope.launch {
                characterUseCase.deleteCharacter(characterId)
            }
        }
    }
