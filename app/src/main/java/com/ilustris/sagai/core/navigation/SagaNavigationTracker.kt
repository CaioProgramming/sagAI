package com.ilustris.sagai.core.navigation

import com.ilustris.sagai.ui.navigation.BookReaderKey
import com.ilustris.sagai.ui.navigation.ChatKey
import com.ilustris.sagai.ui.navigation.CharacterDetailKey
import com.ilustris.sagai.ui.navigation.MilestoneKey
import com.ilustris.sagai.ui.navigation.SagaActsKey
import com.ilustris.sagai.ui.navigation.SagaDetailKey
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

        fun isOnSagaDetail(sagaId: Int): Boolean {
            val key = _currentKey.value
            return key is SagaDetailKey && key.sagaId == sagaId.toString()
        }

        fun isOnCharacterDetail(characterId: Int): Boolean {
            val key = _currentKey.value
            return key is CharacterDetailKey && key.characterId == characterId
        }

        fun isOnBookReader(sagaId: Int, actId: Int): Boolean {
            val key = _currentKey.value
            return key is BookReaderKey && key.sagaId == sagaId && key.initialActId == actId
        }

        fun isOnChronicle(sagaId: Int): Boolean {
            val key = _currentKey.value
            return key is SagaActsKey && key.sagaId == sagaId.toString()
        }

        fun isOnMilestone(sagaId: Int): Boolean {
            val key = _currentKey.value
            return key is MilestoneKey && key.sagaId == sagaId
        }
    }
