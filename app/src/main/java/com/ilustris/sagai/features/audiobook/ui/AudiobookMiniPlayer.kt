package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.themePainter

/**
 * Compact "now playing" bar for the book-reading screen — Spotify/Apple-Books style: stays
 * visible (paused or not) as long as a section is loaded, tapping it opens the full
 * [AudiobookPlayerView]. Scoped to this screen only, not a global app-wide bar.
 */
@Composable
fun AudiobookMiniPlayer(
    audiobook: AudiobookUiState,
    onClick: () -> Unit,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sectionTitle = audiobook.sections.find { it.key == audiobook.playingSectionKey }?.title

    AnimatedVisibility(
        visible = sectionTitle != null,
        enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 2 },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 },
        modifier = modifier,
    ) {
        val onPrimary = MaterialTheme.colorScheme.onPrimary
        val fraction =
            if (audiobook.durationMs > 0) {
                (audiobook.positionMs.toFloat() / audiobook.durationMs).coerceIn(0f, 1f)
            } else {
                0f
            }
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onClick),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(18.dp),
        ) {
            Column {
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = onPrimary,
                    trackColor = onPrimary.copy(alpha = .25f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = themePainter(),
                        contentDescription = null,
                        tint = onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = sectionTitle.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onTogglePlayback) {
                        Icon(
                            painterResource(
                                if (audiobook.isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                            ),
                            contentDescription =
                                stringResource(
                                    if (audiobook.isPlaying) R.string.audiobook_pause_cd else R.string.audiobook_play_cd,
                                ),
                            tint = onPrimary,
                        )
                    }
                }
            }
        }
    }
}
