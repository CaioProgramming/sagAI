package com.ilustris.sagai.ui.components.island

import androidx.compose.runtime.Composable
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Bottom-island content for the narrative "Loading" milestone.
 *
 * Intentionally **compact-only** — no expanded body, no full-screen takeover. It's just a small
 * loading pill (optionally echoing the streaming reasoning) that sits in the widget while the
 * next beat generates; chat interaction stays blocked by the chat's own generating state, so the
 * island doesn't need to be invasive about it.
 */
class LoadingIslandContent(
    reasoning: String?,
    genre: Genre?,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            label = reasoning?.takeIf { it.isNotBlank() },
            iconRes = genre?.icon,
            isLoading = true,
            genre = genre,
        )

    // No expansion — tapping does nothing, and the host never grows it.
    override val expandsOnTap: Boolean = false

    @Composable
    override fun Expanded(scope: IslandScope) = Unit
}
