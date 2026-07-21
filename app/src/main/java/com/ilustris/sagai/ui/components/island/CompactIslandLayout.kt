package com.ilustris.sagai.ui.components.island

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.time.Duration.Companion.seconds

/** Reference height of a "full" compact row (icon + text + indicator). Icon-/text-only rows are shorter. */
val CompactIslandHeight = 56.dp

/**
 * Renders an island's compact row from its [CompactIslandData]. Fills a fraction of the pill's
 * available width when collapsed (a persistent, reduced-size pill, matching the Ember reference,
 * rather than one that grows with content) and the full width once [expanded] — animated via
 * [pillFill]. The label fills whatever space the icon/trailing slot don't use ([Modifier.weight])
 * and scrolls ([Modifier.basicMarquee]) if it doesn't fit, instead of growing the pill further.
 *
 * The label is centered and shimmers while loading. While [expanded] and
 * [CompactIslandData.expandedLabelRes] is set, the label swaps to that — letting this
 * always-visible row double as a header for the expanded content below it.
 */
@Composable
fun CompactIslandLayout(
    data: CompactIslandData,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    val expandedLabel = if (expanded) data.expandedLabelRes?.let { stringResource(it) } else null
    val label = expandedLabel ?: data.label ?: data.labelRes?.let { stringResource(it) }
    val hasLoadingIndicator = data.isLoading || data.progress != null
    // The action icon only shows in the trailing slot when there's no loading/progress to show
    // there instead — e.g. the advance trigger's arrow disappears in favor of a spinner once
    // tapping has committed and it's processing.
    val hasActionIcon = !hasLoadingIndicator && data.actionIconRes != null

    val pillFill by animateFloatAsState(
        if (expanded) 1f else .6f,
    )

    SagAITheme(data.genre) {
        Row(
            modifier =
                modifier
                    .padding(12.dp)
                    .fillMaxWidth(pillFill)
                    .reactiveShimmer(data.isLoading, repeatMode = RepeatMode.Restart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            data.iconRes?.let { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(12.dp),
                )
            }

            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .weight(1f)
                            .basicMarquee(),
                )
            }

            if (hasLoadingIndicator) {
                CompactIslandIndicator(data)
            } else if (hasActionIcon) {
                Icon(
                    painter = painterResource(data.actionIconRes!!),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CompactIslandIndicator(data: CompactIslandData) {
    val progress = data.progress
    Box(
        modifier =
            Modifier
                .size(20.dp)
                .reactiveShimmer(
                    data.isLoading,
                    duration = 5.seconds,
                    targetValue = 300f,
                    repeatMode = RepeatMode.Restart,
                ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            progress != null -> {
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    label = "islandProgress",
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    modifier = Modifier.size(18.dp),
                    gapSize = 0.dp,
                )
            }

            data.isLoading -> {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                    gapSize = 0.dp,
                )
            }
        }
    }
}
