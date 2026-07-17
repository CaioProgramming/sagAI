package com.ilustris.sagai.ui.components.island

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.reactiveShimmer

/** Reference height of a "full" compact row (icon + text + indicator). Icon-/text-only rows are shorter. */
val CompactIslandHeight = 56.dp

/**
 * Renders an island's compact row from its [CompactIslandData]. The row is **content-sized**
 * (wrap width + [animateContentSize]) so the island stays as small as what's actually present:
 *
 * - icon + label + indicator → full pill
 * - label only → text pill
 * - icon only → tiny icon
 * - loading only → just a spinner
 *
 * Each element renders only when its data is present; nothing reserves empty space. The label is
 * centered and shimmers while loading.
 */
@Composable
fun CompactIslandLayout(
    data: CompactIslandData,
    modifier: Modifier = Modifier,
) {
    val hasText = data.label != null || data.labelRes != null
    val hasIndicator = data.isLoading || data.progress != null

    SagAITheme(data.genre) {
        Row(
            modifier =
                modifier
                    .wrapContentWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (hasText) 10.dp else 0.dp),
        ) {
            data.iconRes?.let { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }

            if (hasText) {
                Text(
                    text = data.label ?: data.labelRes?.let { stringResource(it) } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.reactiveShimmer(data.isLoading, repeatMode = RepeatMode.Restart),
                )
            }

            if (hasIndicator) {
                CompactIslandIndicator(data)
            }
        }
    }
}

@Composable
private fun CompactIslandIndicator(data: CompactIslandData) {
    val progress = data.progress
    Box(
        modifier = Modifier.size(20.dp),
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
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    modifier = Modifier.size(18.dp),
                )
            }

            data.isLoading -> {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
