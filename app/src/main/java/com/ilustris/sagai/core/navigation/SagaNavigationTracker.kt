package com.ilustris.sagai.core.navigation

import com.ilustris.sagai.ui.navigation.ChatKey
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Tracks the visible Nav 3 destination for in-app vs system notification routing. */
@Singleton
class SagaNavigationTracker
    @Inject
    constructor() {
        private val _currentKey = MutableStateFlow<NavKey?>(null)
        val currentKey: StateFlow<NavKey?> = _currentKey.asStateFlow()

        fun update(key: NavKey) {
            _currentKey.value = key
        }

        fun isOnChatForSaga(sagaId: Int): Boolean {
            val key = _currentKey.value
            return key is ChatKey && key.sagaId == sagaId.toString()
        }
    }
