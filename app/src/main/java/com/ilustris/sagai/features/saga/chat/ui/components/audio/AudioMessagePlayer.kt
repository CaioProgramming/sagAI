package com.ilustris.sagai.features.saga.chat.ui.components.audio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont

/**
 * Audio player component for chat bubbles.
 * Displays a linear progress bar with play/pause controls and timing information.
 * Below it shows a collapsible transcription.
 */
@Composable
fun AudioMessagePlayer(
    transcription: String,
    audioPlaybackState: AudioPlaybackState?,
    genre: Genre,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onPlayPauseClick: () -> Unit = {},
) {
    var showTranscription by remember { mutableStateOf(false) }

    val isPlaying = audioPlaybackState?.isPlaying == true
    val progress by animateFloatAsState(
        targetValue = audioPlaybackState?.progress ?: 0f,
        animationSpec = tween(durationMillis = 100),
        label = "audio_progress",
    )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Audio Player Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Play/Pause Button
            IconButton(
                onClick = onPlayPauseClick,
                modifier =
                    Modifier
                        .size(32.dp)
                        .padding(4.dp),
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    label = "play_pause_icon",
                ) { playing ->
                    Icon(
                        painter =
                            painterResource(
                                if (playing) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                            ),
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // Progress Bar
            Column(
                modifier = Modifier.weight(1f),
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = audioPlaybackState?.formattedCurrentPosition ?: "0:00",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = genre.bodyFont(),
                                color = contentColor.copy(alpha = 0.7f),
                            ),
                    )

                    Text(
                        text = audioPlaybackState?.formattedDuration ?: "0:00",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = genre.bodyFont(),
                                color = contentColor.copy(alpha = 0.7f),
                            ),
                    )
                }
            }
        }

        // Transcription Toggle Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .clickable { showTranscription = !showTranscription }
                    .padding(vertical = 4.dp),
        ) {
            Icon(
                painter =
                    painterResource(
                        if (showTranscription) R.drawable.ic_shrink else R.drawable.ic_expand,
                    ),
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )

            Text(
                text =
                    if (showTranscription) {
                        "Hide transcription"
                    } else {
                        "See transcription"
                    },
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Medium,
                        color = contentColor.copy(alpha = 0.6f),
                    ),
            )
        }

        // Collapsible Transcription
        AnimatedVisibility(
            visible = showTranscription,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text = transcription,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Normal,
                        color = contentColor,
                        textAlign = TextAlign.Start,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                        .clickable { showTranscription = false },
                overflow = TextOverflow.Visible,
            )
        }
    }
}
