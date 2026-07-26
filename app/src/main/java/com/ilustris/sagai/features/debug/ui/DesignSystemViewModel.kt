package com.ilustris.sagai.features.debug.ui

import androidx.lifecycle.ViewModel
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.ui.components.island.ChatIslandService
import com.ilustris.sagai.ui.components.island.IslandContent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DesignSystemViewModel @Inject constructor(
    private val chatIslandService: ChatIslandService,
    val debugImageFallbackService: DebugImageFallbackService
) : ViewModel() {

    fun testIsland(content: IslandContent?) {
        chatIslandService.setTop(content)
    }
}
