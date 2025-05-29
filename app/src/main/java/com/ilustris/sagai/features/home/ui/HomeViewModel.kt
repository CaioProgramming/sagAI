package com.ilustris.sagai.features.home.ui

import androidx.lifecycle.ViewModel
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
    ) : ViewModel() {
        val chats = sagaHistoryUseCase.getSagas()
    }
