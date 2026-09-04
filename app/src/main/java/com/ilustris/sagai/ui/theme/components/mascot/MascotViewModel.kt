package com.ilustris.sagai.ui.theme.components.mascot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.services.MascotExpressionService
import com.ilustris.sagai.core.services.model.MascotExpression
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MascotViewModel
    @Inject
    constructor(
        private val mascotExpressionService: MascotExpressionService,
    ) : ViewModel() {
        private val _expressions = MutableStateFlow<Map<EmotionalTone, MascotExpression>>(emptyMap())

        /** The blob eye specs from Remote Config. Empty until loaded, and empty stays empty. */
        val expressions = _expressions.asStateFlow()

        init {
            viewModelScope.launch {
                _expressions.value = mascotExpressionService.getExpressions()
            }
        }
    }
