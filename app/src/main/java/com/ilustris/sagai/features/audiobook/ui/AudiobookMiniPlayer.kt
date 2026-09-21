package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R

private val WAVE_HEIGHT = 24.dp
private val PLAY_SIZE = 40.dp
private const val REMAINING_ALPHA = .35f
private const val PULSE_MS = 900

/**
 * Compact "now playing" bar for the book-reading screen — a voice note rather than a music player:
 * the chapter shows up as its own recorded waveform, filling in as the narrator reads. Stays
 * visible (paused or not) as long as a section is loaded; tapping it opens the full
 * [AudiobookPlayerView]. Scoped to this screen only, not a global app-wide bar.
 */
@Composable
fun AudiobookMiniPlayer(
    audiobook: AudiobookUiState,
    waveform: Waveform?,
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
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onClick),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(PLAY_SIZE)
                            .clip(CircleShape)
                            .background(onPrimary.copy(alpha = .16f))
                            .clickable(onClick = onTogglePlayback),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(
                            if (audiobook.isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                        ),
                        contentDescription =
                            stringResource(
                                if (audiobook.isPlaying) R.string.audiobook_pause_cd else R.string.audiobook_play_cd,
                            ),
                        tint = onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OnAirDot(isPlaying = audiobook.isPlaying, color = onPrimary)
                        Text(
                            text = sectionTitle.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = "${audiobook.positionMs.asClock()} — ${audiobook.durationMs.asClock()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                            color = onPrimary.copy(alpha = .7f),
                            maxLines = 1,
                        )
                    }

                    WaveformBars(
                        levels = waveform?.takeIf { it.sectionKey == audiobook.playingSectionKey }?.levels,
                        fraction = fraction,
                        playedColor = onPrimary,
                        remainingColor = onPrimary.copy(alpha = REMAINING_ALPHA),
                        modifier = Modifier.height(WAVE_HEIGHT),
                    )
                }
            }
        }
    }
}

/** The recording light: breathes while the narration runs, holds steady when it is paused. */
@Composable
private fun OnAirDot(
    isPlaying: Boolean,
    color: androidx.compose.ui.graphics.Color,
) {
    val pulse =
        rememberInfiniteTransition(label = "onAir").animateFloat(
            initialValue = 1f,
            targetValue = .25f,
            animationSpec = infiniteRepeatable(tween(PULSE_MS), RepeatMode.Reverse),
            label = "onAirAlpha",
        )
    Box(
        modifier =
            Modifier
                .size(6.dp)
                .alpha(if (isPlaying) pulse.value else .4f)
                .clip(CircleShape)
                .background(color),
    )
}
