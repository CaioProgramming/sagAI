package com.ilustris.sagai.features.playthrough

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.playthrough.data.model.PlayThroughData
import com.ilustris.sagai.features.playthrough.data.model.SagaPlaythrough
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PlaythroughUiState {
    data object Loading : PlaythroughUiState()

    data class Success(
        val data: PlayThroughData,
        val completedSagas: List<SagaPlaythrough>,
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
        private val stringResourceHelper: StringResourceHelper,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<PlaythroughUiState>(PlaythroughUiState.Loading)
        val uiState = _uiState.asStateFlow()
        val sagas = playthroughUseCase.availableSagas()

        fun loadPlaythroughData() {
            viewModelScope.launch {
                _uiState.value = PlaythroughUiState.Loading

                when (val result = playthroughUseCase.invoke()) {
                    is RequestResult.Success -> {
                        _uiState.value =
                            PlaythroughUiState.Success(
                                result.value,
                                sagas.first(),
                            )
                    }

                    is RequestResult.Error -> {
                        _uiState.value =
                            PlaythroughUiState.Empty(
                                stringResourceHelper.getString(R.string.playthrough_load_error),
                            )
                    }
                }
            }
        }
    }
