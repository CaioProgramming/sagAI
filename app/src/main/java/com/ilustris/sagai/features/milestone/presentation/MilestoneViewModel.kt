package com.ilustris.sagai.features.milestone.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MilestoneViewModel
    @Inject
    constructor() : ViewModel() {
        private val _congratsMessage = MutableStateFlow<String?>(null)
        val congratsMessage: StateFlow<String?> = _congratsMessage.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        fun clear() {
            _congratsMessage.value = null
            _isLoading.value = false
        }
    }
