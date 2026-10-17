package com.ilustris.sagai.features.live.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether a live turn is still playing out for a saga (reply voicing or playback). The milestone
 * navigation waits on it so the Milestone screen never cuts a character mid-sentence.
 */
@Singleton
class LiveSessionTracker
    @Inject
    constructor() {
        private val _turnsInFlight = MutableStateFlow<Set<Int>>(emptySet())
        val turnsInFlight: StateFlow<Set<Int>> = _turnsInFlight.asStateFlow()

        fun setTurnInFlight(
            sagaId: Int,
            inFlight: Boolean,
        ) {
            _turnsInFlight.update { if (inFlight) it + sagaId else it - sagaId }
        }
    }
