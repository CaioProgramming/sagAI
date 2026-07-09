package com.ilustris.sagai.core.globalshell

/**
 * Visibility context used by effects to decide whether they should be suppressed
 * (e.g. user is already on the target screen).
 */
interface GlobalShellVisibilityContext {
    fun isOnChatForSaga(sagaId: Int): Boolean
    fun isOnSagaDetail(sagaId: Int): Boolean
    fun isOnCharacterDetail(characterId: Int): Boolean
    fun isOnBookReader(sagaId: Int, actId: Int): Boolean
    fun isOnChronicle(sagaId: Int): Boolean
    fun isReviewVisibleForSaga(sagaId: Int): Boolean
}

