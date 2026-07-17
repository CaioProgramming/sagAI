package com.ilustris.sagai.ui.components.island

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges chat-scoped island contributions (which know local gating like onboarding/selection
 * mode) up to the global island overlays rendered in `MainActivity`.
 *
 * The chat UI publishes an [IslandContent] (with its callbacks) while it's on screen and clears
 * it on dispose; `MainActivity` observes and renders it in the global top/bottom overlays. This
 * keeps the chat's full gating context while letting a single global overlay own the surface.
 */
@Singleton
class ChatIslandService
    @Inject
    constructor() {
        private val _bottom = MutableStateFlow<IslandContent?>(null)

        /** Bottom island contributed by the active chat (e.g. the narrative advance trigger). */
        val bottom: StateFlow<IslandContent?> = _bottom.asStateFlow()

        private val _top = MutableStateFlow<IslandContent?>(null)

        /** Top island contributed by the active chat (e.g. the current objective). */
        val top: StateFlow<IslandContent?> = _top.asStateFlow()

        fun setBottom(content: IslandContent?) {
            _bottom.value = content
        }

        fun setTop(content: IslandContent?) {
            _top.value = content
        }
    }
