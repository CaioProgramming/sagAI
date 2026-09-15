package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.audiobook.data.usecase.NarrationProgress
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.cornerSize

sealed interface AudiobookAction {
    data class Listen(
        val sectionKey: String,
        val pageIndex: Int,
    ) : AudiobookAction

    data object TogglePlayback : AudiobookAction

    data object Stop : AudiobookAction

    data class SeekToText(
        val sectionKey: String,
        val pageIndex: Int,
        val charOffset: Int,
    ) : AudiobookAction

    data object ToggleSyncSource : AudiobookAction

    data class Realign(
        val sectionKey: String,
    ) : AudiobookAction

    data class ExportVideo(
        val sectionKey: String,
    ) : AudiobookAction

    data object DismissFailure : AudiobookAction
}

/**
 * Audiobook controls for the section on screen: narrate what is missing, follow narration progress,
 * or play/pause. Debug builds add the sync comparison (transcribed vs. estimated timings).
 */
@Composable
fun AudiobookBar(
    state: AudiobookUiState,
    sectionKey: String,
    pageIndex: Int,
    genre: Genre,
    onAction: (AudiobookAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val section = state.section(sectionKey) ?: return
    val narration = state.narration
    val isThisSectionPlaying = state.playingSectionKey == sectionKey

    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AnimatedContent(
            targetState = Triple(narration?.progress, narration?.sectionKey, isThisSectionPlaying to state.isPlaying),
            label = "audiobookBar",
        ) { (progress, narratingKey, playing) ->
            when {
                progress is NarrationProgress.Failed -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.audiobook_failed, progress.message),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        TextButton(onClick = {
                            onAction(AudiobookAction.DismissFailure)
                            onAction(AudiobookAction.Listen(narratingKey ?: sectionKey, pageIndex))
                        }) {
                            Text(stringResource(R.string.audiobook_retry))
                        }
                    }
                }

                narratingKey != null -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                        Text(
                            when (progress) {
                                is NarrationProgress.Narrating -> stringResource(R.string.audiobook_narrating, progress.position, progress.total)
                                is NarrationProgress.Aligning -> stringResource(R.string.audiobook_syncing, progress.position, progress.total)
                                else -> stringResource(R.string.audiobook_preparing)
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                playing.first -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onAction(AudiobookAction.TogglePlayback) }) {
                            Icon(
                                painterResource(if (playing.second) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24),
                                contentDescription =
                                    stringResource(if (playing.second) R.string.audiobook_pause_cd else R.string.audiobook_play_cd),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = { onAction(AudiobookAction.Stop) }) {
                            Icon(
                                painterResource(R.drawable.ic_stop),
                                contentDescription = stringResource(R.string.audiobook_stop_cd),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }

                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { onAction(AudiobookAction.Listen(sectionKey, pageIndex)) },
                            shape = RoundedCornerShape(genre.cornerSize()),
                            colors = ButtonDefaults.textButtonColors().copy(contentColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(
                                painterResource(R.drawable.round_play_arrow_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                when {
                                    section.isReady -> stringResource(R.string.audiobook_listen)
                                    section.narrated > 0 -> stringResource(R.string.audiobook_continue_narration, section.narrated, section.planned)
                                    else -> stringResource(R.string.audiobook_narrate)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                        if (section.isReady) {
                            IconButton(onClick = { onAction(AudiobookAction.ExportVideo(sectionKey)) }) {
                                Icon(
                                    painterResource(R.drawable.ic_share),
                                    contentDescription = stringResource(R.string.audiobook_export_video_cd),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }
            }
        }

        state.videoExport?.takeIf { it.sectionKey == sectionKey }?.let { export ->
            Text(
                when {
                    export.error != null -> stringResource(R.string.audiobook_export_failed, export.error)
                    export.percent != null -> stringResource(R.string.audiobook_exporting_percent, export.percent)
                    else -> stringResource(R.string.audiobook_exporting)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (export.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }

        if (state.showDebug && section.narrated > 0) {
            DebugSyncRow(state, section, onAction)
        }
    }
}

/** Debug only: flips the highlight between transcription and character estimates to compare by ear. */
@Composable
private fun DebugSyncRow(
    state: AudiobookUiState,
    section: AudiobookSectionUi,
    onAction: (AudiobookAction) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { onAction(AudiobookAction.ToggleSyncSource) }) {
            Text(
                "sync: ${state.syncSource.name.lowercase()}",
                style = MaterialTheme.typography.labelSmall,
            )
        }
        TextButton(onClick = { onAction(AudiobookAction.Realign(section.key)) }) {
            Text("realign", style = MaterialTheme.typography.labelSmall)
        }
        Text(
            "min score ${section.worstScore?.let { "%.2f".format(it) } ?: "—"} · estimated ${section.estimatedClips}/${section.narrated}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
        )
    }
}
