package com.ilustris.sagai.ui.components.island

import androidx.compose.runtime.Composable
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.model.toUi

/**
 * Bottom-island content for the narrative advance trigger.
 *
 * Compact-only, no expand: the pill fires [onAction] directly on tap ([expandsOnTap] = false).
 * Idle, it shows the action's title plus a trailing arrow signaling "tap to act"; while
 * processing, it shows the streaming reasoning (marquee-scrolling if long) with the trailing
 * arrow swapped for the loading spinner — the same "reasoning lives in the compact pill, no
 * separate expanded body" pattern used for image generation.
 */
class AdvanceIslandContent(
    private val action: NarrativeAction,
    private val reasoning: String?,
    private val isProcessing: Boolean,
    private val genre: Genre?,
    override val onAction: () -> Unit,
) : IslandContent {
    private val actionUi = action.toUi()

    override val compact: CompactIslandData =
        CompactIslandData(
            label = if (isProcessing) reasoning else null,
            labelRes = if (isProcessing) actionUi.holdingTextRes else (actionUi.titleRes ?: R.string.continue_text),
            iconRes = genre?.icon,
            isLoading = isProcessing,
            actionIconRes = if (isProcessing) null else R.drawable.round_arrow_forward_ios_24,
            genre = genre,
            backgroundColor = IslandBackgroundColor.ThemePrimary,
        )

    // Tap commits the advance directly — never a toggle, so there's nothing to expand to.
    override val expandsOnTap: Boolean = false

    @Composable
    override fun Expanded(scope: IslandScope) = Unit
}
