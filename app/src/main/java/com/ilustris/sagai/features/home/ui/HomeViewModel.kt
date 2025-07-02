package com.ilustris.sagai.features.home.ui

import androidx.lifecycle.ViewModel
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.MessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val messageUseCase: MessageUseCase,
    ) : ViewModel() {
        val sagas =
            sagaHistoryUseCase.getSagas().map {
                it
                    .map { saga ->
                        saga.copy(messages = saga.messages.sortedByDescending { m -> m.message.timestamp })
                    }.sortedByDescending { saga ->
                        saga.messages.lastOrNull()?.message?.timestamp ?: 0
                    }
            }
    }
