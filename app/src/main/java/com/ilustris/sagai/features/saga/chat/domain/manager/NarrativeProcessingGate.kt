package com.ilustris.sagai.features.saga.chat.domain.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight holder for narrative-processing busy state.
 * Extracted to break the Hilt cycle between [com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager]
 * and [com.ilustris.sagai.features.saga.detail.review.domain.ReviewGenerationCoordinator].
 */
@Singleton
class NarrativeProcessingGate
    @Inject
    constructor() {
        private val _isNarrativeProcessing = MutableStateFlow(false)
        val isNarrativeProcessing: StateFlow<Boolean> = _isNarrativeProcessing.asStateFlow()

        fun setNarrativeProcessing(processing: Boolean) {
            _isNarrativeProcessing.value = processing
        }
    }
