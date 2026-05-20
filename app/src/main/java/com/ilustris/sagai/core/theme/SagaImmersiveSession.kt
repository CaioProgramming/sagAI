package com.ilustris.sagai.core.theme

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks which saga screen is currently visible in the Nav 3 stack.
 * Prevents preserved/backstack screens (e.g. Chat under Home) from calling [SagaThemeManager]
 * while the user is on Home or another saga.
 */
@Singleton
class SagaImmersiveSession
    @Inject
    constructor() {
        private val stack = ArrayDeque<Pair<String, Int>>()

        fun push(
            owner: String,
            sagaId: Int,
        ) {
            stack.addLast(owner to sagaId)
            Timber.d("SagaImmersiveSession: push $owner saga=$sagaId (depth=${stack.size})")
        }

        fun pop(owner: String) {
            val index = stack.indexOfLast { it.first == owner }
            if (index < 0) return
            while (stack.size > index) {
                stack.removeLast()
            }
            Timber.d(
                "SagaImmersiveSession: pop $owner (depth=${stack.size}, active=${stack.lastOrNull()?.second})",
            )
        }

        fun isSagaActive(sagaId: Int): Boolean = stack.lastOrNull()?.second == sagaId
    }
