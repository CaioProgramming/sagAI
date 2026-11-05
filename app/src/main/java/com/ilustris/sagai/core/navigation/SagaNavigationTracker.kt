package com.ilustris.sagai.core.navigation

import com.ilustris.sagai.ui.navigation.AudiobookPlayerKey
import com.ilustris.sagai.ui.navigation.BookReaderKey
import com.ilustris.sagai.ui.navigation.CharacterBrainKey
import com.ilustris.sagai.ui.navigation.EpilogueChatKey
import com.ilustris.sagai.ui.navigation.EpilogueLiveKey
import com.ilustris.sagai.ui.navigation.SagaBrainKey
import com.ilustris.sagai.ui.navigation.SagaChaptersKey
import com.ilustris.sagai.ui.navigation.SagaCharactersKey
import com.ilustris.sagai.ui.navigation.SagaEmotionalProfileKey
import com.ilustris.sagai.ui.navigation.SagaEventsKey
import com.ilustris.sagai.ui.navigation.SagaStoryReaderKey
import com.ilustris.sagai.ui.navigation.SagaWikiKey
import com.ilustris.sagai.ui.navigation.ChatKey
import com.ilustris.sagai.ui.navigation.CharacterDetailKey
import com.ilustris.sagai.ui.navigation.LiveConversationKey
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

        fun isOnLiveConversation(sagaId: Int): Boolean {
            val key = _currentKey.value
            return key is LiveConversationKey && key.sagaId == sagaId
        }

        /**
         * The player is in this saga's conversation, typed (chat) or spoken (live mode). Milestones,
         * the generation island and in-app notifications treat both the same.
         */
        fun isInConversation(sagaId: Int): Boolean = isOnChatForSaga(sagaId) || isOnLiveConversation(sagaId)

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

        /** The player is inside one of this saga's flows, where the character rewards may come up. */
        fun isInSagaFlow(
            sagaId: Int,
            characterIds: Set<Int>,
        ): Boolean = _currentKey.value.belongsToSagaFlow(sagaId, characterIds)

        fun isOnMilestone(sagaId: Int): Boolean {
            val key = _currentKey.value
            return key is MilestoneKey && key.sagaId == sagaId
        }
    }

/**
 * Whether [this] is a screen of the saga's own flows (conversation, detail and its sections, books, epilogues) or the
 * sheet of one of [characterIds]. Home, other sagas, the milestone reveal and debug screens are not.
 */
fun NavKey?.belongsToSagaFlow(
    sagaId: Int,
    characterIds: Set<Int>,
): Boolean {
    val id = sagaId.toString()
    return when (this) {
        is ChatKey -> sagaId == this.sagaId.toIntOrNull()
        is SagaDetailKey -> this.sagaId == id
        is SagaCharactersKey -> this.sagaId == id
        is SagaWikiKey -> this.sagaId == id
        is SagaEventsKey -> this.sagaId == id
        is SagaActsKey -> this.sagaId == id
        is SagaChaptersKey -> this.sagaId == id
        is SagaStoryReaderKey -> this.sagaId == id
        is SagaBrainKey -> this.sagaId == id
        is SagaEmotionalProfileKey -> this.sagaId == id
        is CharacterBrainKey -> this.sagaId == id
        is EpilogueChatKey -> this.sagaId == id
        is BookReaderKey -> this.sagaId == sagaId
        is AudiobookPlayerKey -> this.sagaId == sagaId
        is LiveConversationKey -> this.sagaId == sagaId
        is EpilogueLiveKey -> this.sagaId == sagaId
        is CharacterDetailKey -> characterId in characterIds
        else -> false
    }
}
