package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Everything one saga's characters earned and the player has not seen yet, as the pages of a single flow.
 * [onSeen] is what marks it as told; it runs only when the player finishes or closes the sheet.
 */
class CharacterRewards(
    val saga: Saga,
    val characters: Map<Int, Character>,
    val steps: List<RewardStep>,
    val onSeen: suspend () -> Unit,
    /** Runs after [onSeen] and once a new flow may be offered: what arrived meanwhile comes up next. */
    val onNext: suspend () -> Unit = {},
) {
    val sagaId: Int get() = saga.id
    val genre: Genre get() = saga.genre
}

/**
 * Holds the rewards waiting to be shown. The sheet appears only while the player is inside that saga's flows
 * (see `sagaFlowMatches`); until then the rewards stay here, untold, so nothing is lost by navigating elsewhere.
 */
@Singleton
class CharacterRewardsService
    @Inject
    constructor() {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        private val _pending = MutableStateFlow<CharacterRewards?>(null)
        val pending: StateFlow<CharacterRewards?> = _pending.asStateFlow()

        private var started = false

        /** A newer offer replaces one the player has not started; one in progress is left alone and the rest follows after it. */
        fun offer(rewards: CharacterRewards) {
            if (started) return
            _pending.value = rewards
        }

        /** The first page is on screen: from now on the content of this flow is fixed. */
        fun markStarted() {
            started = true
        }

        /** Finished or closed: tell the app it was seen. */
        fun finish() {
            val done = _pending.value ?: return
            _pending.value = null
            // Stays "started" until it is marked as told, so a rebuild in between cannot offer the same flow again.
            scope.launch {
                runCatching { done.onSeen() }
                started = false
                runCatching { done.onNext() }
            }
        }
    }
