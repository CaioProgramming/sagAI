package com.ilustris.sagai.ui.components.island

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges chat-scoped island contributions (published from the data layer, which owns the
 * narrative/milestone state) up to the global island overlays rendered in `MainActivity`.
 *
 * The chat's manager publishes an [IslandContent] (with its callbacks) while a saga is loaded and
 * clears it when it isn't; `MainActivity` observes and renders it in the global top/bottom
 * overlays. This keeps the manager's full state context while letting a single global overlay own
 * the surface — no per-screen Composable host needed.
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

        private val _navigationRequests = MutableSharedFlow<String>(extraBufferCapacity = 1)

        /** Deep links requested by island content constructed outside Compose (e.g. milestone
         * detail actions), which can't hold a reference to the screen's [androidx.navigation3.runtime.NavKey]-based navigator. */
        val navigationRequests: SharedFlow<String> = _navigationRequests.asSharedFlow()

        fun setBottom(content: IslandContent?) {
            _bottom.value = content
        }

        fun setTop(content: IslandContent?) {
            _top.value = content
        }

        fun requestNavigation(deepLink: String) {
            _navigationRequests.tryEmit(deepLink)
        }
    }
