@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.audiobook.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.media.SagaPlaybackService
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.theme.components.MorphingThemeIcon
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.morphingColor
import kotlin.time.Duration.Companion.seconds

/**
 * Spotify/Apple-Music-style now-playing screen for the audiobook, deliberately separate from the
 * page-turning reading experience: a slowly crossfading, softly blurred backdrop of art from this
 * volume, a lyrics-style scroller of the book's prose, and a simple transport bar.
 */
@Composable
fun AudiobookPlayerView(
    saga: SagaContent,
    act: ActContent,
    pages: List<PageItem>,
    audiobook: AudiobookUiState?,
    onAction: (AudiobookAction) -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onSeekBy: (Long) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    val backgrounds =
        remember(saga, act) {
            buildList {
                act.chapters.forEach { it.data.coverImage.takeIf(String::isNotBlank)?.let(::add) }
                saga.data.icon.takeIf(String::isNotBlank)?.let(::add)
                saga.characters.forEach { it.data.image.takeIf(String::isNotBlank)?.let(::add) }
            }.ifEmpty { listOf(saga.data.icon) }
        }
    var backgroundIndex by remember(backgrounds) { mutableStateOf(0) }
    LaunchedEffect(backgrounds) {
        while (true) {
            kotlinx.coroutines.delay(BACKGROUND_CYCLE.inWholeMilliseconds)
            backgroundIndex = (backgroundIndex + 1) % backgrounds.size
        }
    }

    val bookTitle = act.book?.actTitle?.takeIf(String::isNotBlank) ?: act.data.title
    val chapterTitle = audiobook?.playingSectionKey?.let { audiobook.section(it)?.title }.orEmpty()
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TOPBAR_HEIGHT

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = backgrounds.getOrNull(backgroundIndex),
            animationSpec = tween(BACKGROUND_CROSSFADE_MS),
            label = "audiobookBackground",
        ) { image ->
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(28.dp)
                        .scale(1.15f), // hides blur edge artifacts
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = .35f)),
        )

        val isGenerating = audiobook?.narration != null
        val error = audiobook?.error
        when {
            error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.audiobook_generation_failed_cd),
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            ),
                    )
                    Text(
                        text = error,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                                textAlign = TextAlign.Center,
                            ),
                    )
                    IconButton(
                        onClick = { onAction(AudiobookAction.DismissFailure) },
                        modifier =
                            Modifier
                                .background(MaterialTheme.colorScheme.errorContainer, shape = CircleShape)
                                .padding(4.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.round_close_24),
                            contentDescription = stringResource(R.string.audiobook_dismiss_error_cd),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            isGenerating -> {
                Text(
                    text = stringResource(R.string.audiobook_generating_cd),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .9f),
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                )
            }

            else -> {
                AudiobookLyricsColumn(
                    pages = pages,
                    characters = saga.characters,
                    audiobook = audiobook,
                    onNarrateNextSection = { sectionKey -> onAction(AudiobookAction.Listen(sectionKey, 0)) },
                    topInset = topInset,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(TOPBAR_HEIGHT),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Icon(
                    painterResource(R.drawable.ic_back_left),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.padding(8.dp)) {
                    Icon(
                        painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.book_options_cd),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    val playingSectionKey = audiobook?.playingSectionKey
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.audiobook_export_video_cd)) },
                        enabled = playingSectionKey != null,
                        onClick = {
                            showMenu = false
                            playingSectionKey?.let { onAction(AudiobookAction.ExportVideo(it)) }
                        },
                    )
                    if (audiobook?.showDebug == true) {
                        DropdownMenuItem(
                            text = { Text("Sync: ${audiobook.syncSource}") },
                            onClick = {
                                showMenu = false
                                onAction(AudiobookAction.ToggleSyncSource)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Realign") },
                            enabled = playingSectionKey != null,
                            onClick = {
                                showMenu = false
                                playingSectionKey?.let { onAction(AudiobookAction.Realign(it)) }
                            },
                        )
                    }
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(fadeGradientBottom(tintColor = morphingColor(duration = 3.seconds)))
                    .navigationBarsPadding()
                    .padding(top = 96.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
        ) {
            Text(
                text = bookTitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            var dragFraction by remember { mutableFloatStateOf(-1f) }
            val duration = audiobook?.durationMs ?: 0L
            val position = audiobook?.positionMs ?: 0L
            val fraction = if (dragFraction >= 0f) dragFraction else if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

            Slider(
                value = fraction,
                onValueChange = { dragFraction = it },
                onValueChangeFinished = {
                    onSeekToFraction(dragFraction)
                    dragFraction = -1f
                },
                thumb = { MorphingThemeIcon(modifier = Modifier.size(16.dp)) },
                track = { sliderState ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .2f)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(sliderState.value.coerceIn(0f, 1f))
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = (if (dragFraction >= 0f) (dragFraction * duration).toLong() else position).asClock(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                )
                Text(
                    text = duration.asClock(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                )
            }

            var musicMuted by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        musicMuted = !musicMuted
                        val action = if (musicMuted) SagaPlaybackService.ACTION_PAUSE else SagaPlaybackService.ACTION_RESUME
                        SagaPlaybackService.startSafely(context, SagaPlaybackService.playbackIntent(context, action))
                    },
                ) {
                    Icon(
                        painterResource(if (musicMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_up),
                        contentDescription =
                            stringResource(
                                if (musicMuted) R.string.audiobook_unmute_music_cd else R.string.audiobook_mute_music_cd,
                            ),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }

                IconButton(onClick = { onSeekBy(-SEEK_STEP_MS) }) {
                    Icon(
                        painterResource(R.drawable.ic_replay_arrow),
                        contentDescription = stringResource(R.string.audiobook_seek_back_cd),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.scale(1.4f),
                    )
                }

                IconButton(
                    onClick = { onAction(AudiobookAction.TogglePlayback) },
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .padding(4.dp),
                ) {
                    Icon(
                        painterResource(if (audiobook?.isPlaying == true) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24),
                        contentDescription =
                            stringResource(
                                if (audiobook?.isPlaying == true) R.string.audiobook_pause_cd else R.string.audiobook_play_cd,
                            ),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.scale(1.3f),
                    )
                }

                IconButton(onClick = { onSeekBy(SEEK_STEP_MS) }) {
                    Icon(
                        painterResource(R.drawable.ic_replay_arrow),
                        contentDescription = stringResource(R.string.audiobook_seek_forward_cd),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.scale(-1.4f, 1.4f),
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
    }
}

private val TOPBAR_HEIGHT = 56.dp
private val BACKGROUND_CYCLE = 8.seconds
private const val BACKGROUND_CROSSFADE_MS = 1_400
private const val SEEK_STEP_MS = 10_000L

private fun Long.asClock(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
