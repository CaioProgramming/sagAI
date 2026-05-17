package com.ilustris.sagai.features.act.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SagaStoryReaderViewModel
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val visualConfigService: GenreVisualConfigService,
    ) : ViewModel() {
        private val _saga = MutableStateFlow<SagaContent?>(null)
        val saga = _saga.asStateFlow()

        private val _visualConfig = MutableStateFlow<GenreVisualConfig?>(null)
        val visualConfig = _visualConfig.asStateFlow()

        fun loadSaga(sagaId: Int) {
            viewModelScope.launch {
                sagaRepository.getSagaById(sagaId).collectLatest {
                    _saga.value = it
                    it?.let { sagaContent ->
                        _visualConfig.value =
                            visualConfigService.getVisualConfig(sagaContent.data.genre)
                    }
                }
            }
        }
    }
