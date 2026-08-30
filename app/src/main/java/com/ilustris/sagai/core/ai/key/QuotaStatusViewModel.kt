package com.ilustris.sagai.core.ai.key

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    ) : ViewModel() {
        val status: StateFlow<QuotaStatus> =
            quotaStatusService.status.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = QuotaStatus.Clear,
            )
    }
