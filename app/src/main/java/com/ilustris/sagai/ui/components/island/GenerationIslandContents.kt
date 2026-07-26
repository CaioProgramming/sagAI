package com.ilustris.sagai.ui.components.island

import androidx.compose.runtime.Composable
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.saga.chat.data.model.ChatGenerationUiState

/**
 * [IslandContent] mappings for the "persistent work" generation sources that currently render
 * through the top slot of `GlobalShellHost`. The streaming `reasoning` lives directly in the
 * compact pill (marquee-scrolling if long) — no separate expanded body, matching the pattern
 * established for image generation and the narrative advance trigger.
 */

/** Book (Act prose) generation — compact-only, reasoning streams straight into the pill. */
class BookGenerationIslandContent(
    private val state: BookGenerationUiState.Generating,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            label = state.reasoning,
            labelRes = R.string.book_generation_reasoning_placeholder,
            iconRes = state.genre.icon,
            isLoading = true,
            genre = state.genre,
        )
    override val expandsOnTap: Boolean = false

    @Composable
    override fun Expanded(scope: IslandScope) = Unit
}

/** Chat reply generation — compact-only, reasoning streams straight into the pill. */
class ChatGenerationIslandContent(
    private val state: ChatGenerationUiState.Generating,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            label = state.reasoning,
            labelRes = R.string.chat_generation_reasoning_placeholder,
            iconRes = state.genre.icon,
            isLoading = true,
            genre = state.genre,
        )
    override val expandsOnTap: Boolean = false

    @Composable
    override fun Expanded(scope: IslandScope) = Unit
}
