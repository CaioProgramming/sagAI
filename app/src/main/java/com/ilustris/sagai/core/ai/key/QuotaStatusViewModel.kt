package com.ilustris.sagai.core.ai.key

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Exposes [QuotaStatusService] to Compose without threading it through every feature ViewModel.
 *
 * Quota is global to the key, not to any one screen, so the alternative — adding a constructor
 * parameter and a state field to ChatViewModel, NewSagaViewModel and every other generation
 * surface — would spread one fact across a dozen files that have no other reason to change.
 */
@HiltViewModel
class QuotaStatusViewModel
    @Inject
    constructor(
        quotaStatusService: QuotaStatusService,
        private val gemmaClient: GemmaClient,
    ) : ViewModel() {
        /**
         * The aggregate across every model the key has ever hit a daily cap on — the single worst
         * entry, regardless of which tier it belongs to. Informational only (Settings' "here's
         * what's spent" summary): with a tier now able to name several candidate models, one of
         * them being out no longer means that tier itself is out, so nothing should gate a
         * generation surface's input on this anymore — use [tierStatus] for that.
         */
        val status: StateFlow<QuotaStatus> =
            quotaStatusService.status.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = QuotaStatus.Clear,
            )

        private val tierStatusFlows = mutableMapOf<ModelRequirement, StateFlow<QuotaStatus>>()

        /**
         * Whether [requirement] itself has actually run out — every one of its configured
         * candidate models is currently daily-exhausted, not just the first one anyone happened to
         * hit. This is what a generation surface should gate its input on.
         */
        fun tierStatus(requirement: ModelRequirement): StateFlow<QuotaStatus> =
            tierStatusFlows.getOrPut(requirement) {
                flow { emitAll(gemmaClient.tierQuotaStatus(requirement)) }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5_000),
                        initialValue = QuotaStatus.Clear,
                    )
            }
    }
