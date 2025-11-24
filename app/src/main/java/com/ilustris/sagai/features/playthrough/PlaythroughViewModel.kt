package com.ilustris.sagai.features.playthrough

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PlaythroughUiState {
    data object Loading : PlaythroughUiState()

    data class Success(
        val data: PlayThroughData,
    ) : PlaythroughUiState()

    data class Empty(
        val message: String,
    ) : PlaythroughUiState()
}

@HiltViewModel
class PlaythroughViewModel
    @Inject
    constructor(
        private val playthroughUseCase: PlaythroughUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<PlaythroughUiState>(PlaythroughUiState.Loading)
        val uiState = _uiState.asStateFlow()

        fun loadPlaythroughData() {
            viewModelScope.launch {
                _uiState.value = PlaythroughUiState.Loading

                when (val result = playthroughUseCase.invoke()) {
                    is RequestResult.Success -> {
                        _uiState.value = PlaythroughUiState.Success(result.value)
                    }

                    is RequestResult.Error -> {
                        _uiState.value =
                            PlaythroughUiState.Empty(
                                "Erro ao carregar dados de jogatina",
                            )
                    }
                }
            }
        }
    }
