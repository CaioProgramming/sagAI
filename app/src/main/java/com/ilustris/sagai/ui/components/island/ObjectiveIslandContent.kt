package com.ilustris.sagai.ui.components.island

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.ObjectiveOverlay
import com.ilustris.sagai.ui.theme.SagAITheme

/**
 * Top-island content for the current narrative objective.
 *
 * It's just a shortcut to a short piece of text, so the compact row does double duty as its own
 * header: collapsed, it shows the objective text itself (scrolling via marquee if it's long);
 * expanded, the label swaps to [titleRes] ("Objetivo atual") and the actual text moves into
 * [Expanded] below it — no separate icon/title header needed there.
 */
class ObjectiveIslandContent(
    private val titleRes: Int,
    private val objective: String,
    private val genre: Genre?,
    private val progress: Float?,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            label = objective,
            expandedLabelRes = titleRes,
            iconRes = genre?.icon,
            progress = progress,
            genre = genre,
            backgroundColor = IslandBackgroundColor.ThemeBackground,
        )

    @Composable
    override fun Expanded(scope: IslandScope) {
        SagAITheme(genre) {
            ObjectiveOverlay(
                objective = objective,
                modifier = Modifier.fillMaxWidth(),
                onDismiss = scope.onCollapse,
            )
        }
    }
}
