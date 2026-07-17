package com.ilustris.sagai.ui.components.island

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.ObjectiveOverlay
import com.ilustris.sagai.ui.theme.SagAITheme

/**
 * Top-island content for the current narrative objective.
 *
 * Compact shows the objective label with a determinate progress ring; expanded reveals the full
 * objective card ([ObjectiveOverlay]). Fed to the global top overlay by the active chat.
 */
class ObjectiveIslandContent(
    private val titleRes: Int,
    private val objective: String,
    private val genre: Genre?,
) : IslandContent {
    // Compact is just the icon, floating with no card (showBackground = false); the card fades in
    // only when expanded, where the full objective lives. Uses surface color when expanded.
    override val compact: CompactIslandData =
        CompactIslandData(
            iconRes = genre?.icon,
            genre = genre,
            backgroundColor = IslandBackgroundColor.ThemeSurface,
            showBackground = false,
        )

    @Composable
    override fun Expanded(scope: IslandScope) {
        SagAITheme(genre) {
            ObjectiveOverlay(
                title = stringResource(titleRes),
                objective = objective,
                modifier = Modifier.fillMaxWidth(),
                onDismiss = scope.onCollapse,
            )
        }
    }
}
