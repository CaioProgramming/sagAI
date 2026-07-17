package com.ilustris.sagai.ui.components.island

import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.model.toUi
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.themePainter

/**
 * Bottom-island content for the narrative advance trigger.
 *
 * Interaction (per the Shell v2 design decision): the compact pill fires [onAction] directly on
 * tap — there's no draggable "hold to commit" gesture anymore. While processing, the island is
 * [forceExpanded] and streams the holding text / reasoning; taps are inert until it resolves.
 */
class AdvanceIslandContent(
    private val action: NarrativeAction,
    private val reasoning: String?,
    private val isProcessing: Boolean,
    private val genre: Genre?,
    override val onAction: () -> Unit,
) : IslandContent {
    private val actionUi = action.toUi()
    private val labelRes: Int =
        if (isProcessing) actionUi.holdingTextRes else (actionUi.titleRes ?: R.string.continue_text)

    override val compact: CompactIslandData =
        CompactIslandData(
            labelRes = labelRes,
            iconRes = genre?.icon,
            isLoading = isProcessing,
            genre = genre,
            backgroundColor = IslandBackgroundColor.ThemePrimary,
        )

    // Tap commits the advance directly (not a toggle). While processing, force the expanded
    // reasoning view and swallow taps.
    override val expandsOnTap: Boolean = false
    override val forceExpanded: Boolean = isProcessing

    @Composable
    override fun Expanded(scope: IslandScope) {
        SagAITheme(genre) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .reactiveShimmer(true, repeatMode = RepeatMode.Restart),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val gradient = Brush.verticalGradient(morphingGradient())
                Icon(
                    themePainter(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = reasoning ?: stringResource(labelRes),
                    style = MaterialTheme.typography.labelLarge.copy(brush = gradient),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
