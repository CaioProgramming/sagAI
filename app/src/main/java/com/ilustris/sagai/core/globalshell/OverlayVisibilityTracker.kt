package com.ilustris.sagai.core.globalshell

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks the visibility of UI overlays that are not driven by NavKey.
 *
 * This is needed for global effect suppression (e.g. don't show ReviewReady while
 * the Review overlay is already open).
 */
@Singleton
class OverlayVisibilityTracker
    @Inject
    constructor() {
    private val _reviewVisibleSagaId = MutableStateFlow<Int?>(null)
    val reviewVisibleSagaId: StateFlow<Int?> = _reviewVisibleSagaId.asStateFlow()

    fun setReviewVisible(sagaId: Int, visible: Boolean) {
        _reviewVisibleSagaId.value = if (visible) {
            sagaId
        } else {
            _reviewVisibleSagaId.value?.takeIf { it == sagaId }
        }
    }

    fun isReviewVisibleForSaga(sagaId: Int): Boolean =
        _reviewVisibleSagaId.value == sagaId
}

