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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.shape

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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onPlayPauseClick,
                modifier =
                    Modifier
                        .size(24.dp),
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    label = "play_pause_icon",
                    modifier = Modifier.fillMaxSize(),
                ) { playing ->
                    Icon(
                        painter =
                            painterResource(
                                if (playing) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                            ),
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = contentColor,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .gradientFill(genre.gradient(isPlaying)),
                color = contentColor,
                trackColor = contentColor.copy(alpha = .1f),
                gapSize = 0.dp,
                drawStopIndicator = {},
                strokeCap = StrokeCap.Round,
            )

            Text(
                text = audioPlaybackState?.formattedCurrentPosition ?: "0:00",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = contentColor.copy(alpha = .5f),
                    ),
            )
        }

        val text = if (showTranscription.not()) stringResource(R.string.show_transcription) else transcription

        Text(
            text = text,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = genre.bodyFont(),
                    fontWeight = FontWeight.Normal,
                    color = contentColor,
                    textAlign = TextAlign.Start,
                ),
            modifier =
                Modifier
                    .padding(8.dp)
                    .clip(genre.shape())
                    .clickable {
                        showTranscription = showTranscription.not()
                    }.fillMaxWidth()
                    .alpha(.5f),
            overflow = TextOverflow.Visible,
        )
    }
}
