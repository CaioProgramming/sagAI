package com.ilustris.sagai.core.ai

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot: fired the moment a 503 sends a request to a substitute model.
 *
 * Deliberately not [com.ilustris.sagai.core.ai.key.QuotaStatusService], which this could easily
 * have been folded into — that service is specifically about where the user's key stands against
 * Gemini's quota, and a 503 is neither: it is Google's own model being overloaded, unrelated to
 * this key's standing. Blurring the two would make that service mean something looser than it
 * does today.
 *
 * A [SharedFlow] rather than a [kotlinx.coroutines.flow.StateFlow] on purpose: this has no "is
 * true" to hold between events the way a cooldown or a daily block does. It is an event that
 * happened once, not a state to reflect while it lasts, so there is nothing to clear afterward
 * either.
 */
@Singleton
class ModelFallbackNotifier
    @Inject
    constructor() {
        private val _fellBackToSubstitute = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val fellBackToSubstitute: SharedFlow<Unit> = _fellBackToSubstitute

        fun signalFallback() {
            _fellBackToSubstitute.tryEmit(Unit)
        }
    }
