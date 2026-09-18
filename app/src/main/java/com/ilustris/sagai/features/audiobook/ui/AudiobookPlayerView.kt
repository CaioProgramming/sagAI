@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.audiobook.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.features.audiobook.data.usecase.NarrationProgress
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.ui.components.IosStyleMenu
import com.ilustris.sagai.ui.components.IosStyleMenuItem
import com.ilustris.sagai.ui.components.QuotaLimitNotice
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
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
    /** Collected directly inside the lyrics scroller, not here — see its own doc for why. */
    highlightFlow: StateFlow<AudioHighlight?>,
    waveform: Waveform?,
    onAction: (AudiobookAction) -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val bookTitle = act.book?.actTitle?.takeIf(String::isNotBlank) ?: act.data.title
    val chapterTitle = audiobook?.playingSectionKey?.let { audiobook.section(it)?.title }.orEmpty()
    val hasAnyReadySection = audiobook?.sections?.any { it.isReady } == true

    // Mirrors the old headset-click behavior: land on the player already playing when there is
    // narrated audio and nothing is loaded into the player yet (a fresh open, not a resume).
    LaunchedEffect(audiobook?.bookId) {
        val current = audiobook ?: return@LaunchedEffect
        if (current.playingSectionKey == null && current.narration == null) {
            current.sections.firstOrNull { it.isReady }?.let { section ->
                onAction(AudiobookAction.Listen(section.key, 0))
            }
        }
    }

    // One page per chapter: the readable run in order, plus the single pending chapter next in
    // line to narrate (front-to-back only — see AudiobookLyricsColumn's docs).
    val allSections = audiobook?.sections.orEmpty()
    val readableSectionKeys = remember(allSections) { allSections.takeWhile { it.isReady }.map { it.key }.toSet() }
    val pagerSections =
        remember(allSections, readableSectionKeys) {
            buildList {
                allSections.filterTo(this) { it.key in readableSectionKeys }
                allSections.getOrNull(readableSectionKeys.size)?.let(::add)
            }
        }
    val pagerState = rememberPagerState(pageCount = { pagerSections.size.coerceAtLeast(1) })
    val pagerScope = rememberCoroutineScope()
    var showQueue by remember { mutableStateOf(false) }
    var recenterSignal by remember { mutableIntStateOf(0) }

    val currentPlayingSectionKey = audiobook?.playingSectionKey

    // Narration crossing into the next chapter on its own slides the pager along with it.
    val playingPageIndex =
        remember(pagerSections, audiobook?.playingSectionKey) {
            pagerSections.indexOfFirst { it.key == audiobook?.playingSectionKey }.takeIf { it >= 0 }
        }
    LaunchedEffect(playingPageIndex) {
        playingPageIndex?.let { target ->
            if (pagerState.currentPage != target) pagerState.animateScrollToPage(target)
        }
    }

    // audiobook resolves asynchronously (bind() fetches config/sections before the first combine
    // emission), so the pager's own default initial page (0) settles *before* we know what's
    // really playing — e.g. reopening the screen from the mini player while the last chapter is
    // playing would otherwise read as "the reader picked page 0" and hijack playback back to
    // chapter 1. Ignore settle events until we've seen a real audiobook state at least once.
    var hasResolvedInitialState by remember { mutableStateOf(false) }
    LaunchedEffect(audiobook != null) {
        if (audiobook != null) hasResolvedInitialState = true
    }

    // Landing on a different chapter by hand — swipe or the skip buttons — jumps playback there
    // too, same as skipping tracks. Settling back on the chapter already playing (the pager's own
    // auto-follow above) is a no-op since the keys already match.
    LaunchedEffect(pagerState.settledPage) {
        if (!hasResolvedInitialState) return@LaunchedEffect
        val settled = pagerSections.getOrNull(pagerState.settledPage) ?: return@LaunchedEffect
        if (settled.key != audiobook?.playingSectionKey && settled.isReady) {
            onAction(AudiobookAction.Listen(settled.key, 0))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Deliberately just a slow-morphing genre-colored gradient, not the illustration crossfade
        // this used to be: real art competed with the text for attention instead of backing it.
        // A local, draw-phase-only version of morphingGradient() — see AudiobookMorphingBackground's
        // doc for why: this screen stays mounted far longer than morphingGradient()'s other callers.
        AudiobookMorphingBackground(
            colors = MaterialTheme.colorScheme.primary.darkerPalette(factor = .15f),
            duration = 6.seconds,
            modifier = Modifier.fillMaxSize(),
        )

        // Structured as a real Column instead of overlaying everything with align(): the lyrics
        // area is boxed in by weight(1f) between the topbar and the player, so it is physically
        // impossible for text to be laid out behind either one — no manual inset math needed.
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier =
                    Modifier
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

                // Stop/export/queue live in the bottom icon row now, Apple-Music-style — the only
                // thing left behind "..." is debug-only tooling, so it's hidden outside debug.
                if (audiobook?.showDebug == true) {
                    var showMenu by remember { mutableStateOf(false) }
                    val playingSectionKey = audiobook.playingSectionKey
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.padding(8.dp)) {
                            Icon(
                                painterResource(R.drawable.ic_more_vert),
                                contentDescription = stringResource(R.string.book_options_cd),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                        IosStyleMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            IosStyleMenuItem(
                                text = "Sync: ${audiobook.syncSource}",
                                icon = painterResource(R.drawable.ic_sync),
                                onClick = {
                                    showMenu = false
                                    onAction(AudiobookAction.ToggleSyncSource)
                                },
                            )
                            IosStyleMenuItem(
                                text = "Realign",
                                icon = painterResource(R.drawable.baseline_refresh_24),
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

            val isGenerating = audiobook?.narration != null
            val error = audiobook?.error
            val quotaResetAt = audiobook?.quotaResetAt
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    quotaResetAt != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            QuotaLimitNotice(until = quotaResetAt)
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

                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
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
                            // The raw exception text (a "Gemini HTTP 503: ..." or similar) never
                            // reaches the user — same as everywhere else in the app, a fixed
                            // friendly message stands in for it.
                            Text(
                                text = stringResource(R.string.audiobook_generation_failed_message),
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                                        textAlign = TextAlign.Center,
                                    ),
                            )
                            TextButton(
                                onClick = {
                                    val sectionKey = audiobook?.narration?.sectionKey
                                    onAction(AudiobookAction.DismissFailure)
                                    if (sectionKey != null) onAction(AudiobookAction.Listen(sectionKey, 0))
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            ) {
                                Icon(
                                    painterResource(R.drawable.baseline_refresh_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(R.string.audiobook_retry),
                                    modifier = Modifier.padding(start = 6.dp),
                                )
                            }
                        }
                    }

                    isGenerating -> {
                        val fallback = stringResource(R.string.audiobook_generating_cd)
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            AnimatedContent(
                                targetState = audiobook?.narratingReasoningText ?: fallback,
                                label = "audiobookReasoning",
                                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                            ) { text ->
                                Text(
                                    text = text,
                                    style =
                                        MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .9f),
                                            textAlign = TextAlign.Center,
                                        ),
                                    modifier = Modifier.reactiveShimmer(true, saga.data.genre.shimmerColors()),
                                )
                            }
                            NarrationStepProgress(audiobook?.narration?.progress)
                        }
                    }

                    else -> {
                        AudiobookChapterPager(
                            pagerState = pagerState,
                            pageSections = pagerSections,
                            pages = pages,
                            highlightFlow = highlightFlow,
                            playingSectionKey = currentPlayingSectionKey,
                            ttsQuotaResetAt = audiobook?.ttsQuotaResetAt,
                            onNarrateSection = { sectionKey -> onAction(AudiobookAction.Listen(sectionKey, 0)) },
                            recenterSignal = recenterSignal,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            if (hasAnyReadySection) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
                ) {
                    Text(
                        text = bookTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
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
                    val fraction =
                        if (dragFraction >= 0f) {
                            dragFraction
                        } else if (duration > 0) {
                            (position.toFloat() / duration).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                    WaveformSeekBar(
                        levels = waveform?.takeIf { it.sectionKey == audiobook?.playingSectionKey }?.levels,
                        fraction = fraction,
                        onFractionChange = { dragFraction = it },
                        onFractionChangeFinished = {
                            if (dragFraction >= 0f) onSeekToFraction(dragFraction)
                            dragFraction = -1f
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

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                val target = (pagerState.currentPage - 1).coerceAtLeast(0)
                                pagerScope.launch { pagerState.animateScrollToPage(target) }
                            },
                            enabled = pagerState.currentPage > 0,
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_previous),
                                contentDescription = stringResource(R.string.audiobook_previous_chapter_cd),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        IconButton(
                            onClick = { onAction(AudiobookAction.TogglePlayback) },
                            modifier =
                                Modifier
                                    .padding(4.dp),
                        ) {
                            Icon(
                                painterResource(
                                    if (audiobook?.isPlaying ==
                                        true
                                    ) {
                                        R.drawable.round_pause_24
                                    } else {
                                        R.drawable.round_play_arrow_24
                                    },
                                ),
                                contentDescription =
                                    stringResource(
                                        if (audiobook?.isPlaying == true) R.string.audiobook_pause_cd else R.string.audiobook_play_cd,
                                    ),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.scale(1.3f),
                            )
                        }

                        IconButton(
                            onClick = {
                                val target = (pagerState.currentPage + 1).coerceAtMost(pagerSections.lastIndex.coerceAtLeast(0))
                                pagerScope.launch { pagerState.animateScrollToPage(target) }
                            },
                            enabled = pagerState.currentPage < pagerSections.lastIndex,
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_next),
                                contentDescription = stringResource(R.string.audiobook_next_chapter_cd),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val playingSectionKey = audiobook?.playingSectionKey
                        val dimmed = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f)
                        IconButton(onClick = { showQueue = true }) {
                            Icon(
                                painterResource(R.drawable.round_queue_music_24),
                                contentDescription = stringResource(R.string.audiobook_queue_cd),
                                tint = dimmed,
                            )
                        }
                        IconButton(
                            onClick = { playingSectionKey?.let { onAction(AudiobookAction.ExportVideo(it)) } },
                            enabled = playingSectionKey != null,
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_share),
                                contentDescription = stringResource(R.string.audiobook_export_video_cd),
                                tint = dimmed,
                            )
                        }
                        IconButton(
                            onClick = { recenterSignal++ },
                            enabled = playingSectionKey != null,
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_sync),
                                contentDescription = stringResource(R.string.audiobook_recenter_cd),
                                tint = dimmed,
                            )
                        }
                    }
                }
            }
        }

        if (showQueue) {
            QueueBottomSheet(
                sections = audiobook?.sections.orEmpty(),
                playingSectionKey = audiobook?.playingSectionKey,
                onSelect = { sectionKey ->
                    showQueue = false
                    onAction(AudiobookAction.Listen(sectionKey, 0))
                },
                onDismiss = { showQueue = false },
            )
        }
    }
}

/** "Gerando áudio (2/5)" / "Transcrevendo (2/5)" — hidden until the first segment's progress arrives. */
@Composable
private fun NarrationStepProgress(progress: NarrationProgress?) {
    val (position, total, stepLabel) =
        when (progress) {
            is NarrationProgress.Narrating -> {
                Triple(progress.position, progress.total, stringResource(R.string.audiobook_progress_narrating))
            }

            is NarrationProgress.Aligning -> {
                Triple(progress.position, progress.total, stringResource(R.string.audiobook_progress_transcribing))
            }

            else -> {
                return
            }
        }
    val fraction by animateFloatAsState(
        targetValue = if (total > 0) position / total.toFloat() else 0f,
        label = "narrationStepProgress",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.audiobook_progress_format, stepLabel, position, total),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
        )
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.width(160.dp).height(3.dp).clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.onPrimary,
            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .15f),
        )
    }
}

private val TOPBAR_HEIGHT = 56.dp

/**
 * Same "living, genre-colored gradient" feel as [morphingGradient], but the color interpolation
 * is read only in the draw phase (the way [reactiveShimmer] already does), not in this
 * composable's own body. [morphingGradient] reads its animated `State.value` directly in its
 * function body, which forces a full recomposition of whoever calls it on every animation frame —
 * cheap everywhere else it's used (short-lived loaders, a single small element), but this screen
 * keeps it mounted for as long as the player is open, stacked on top of the lyrics scroller's own
 * per-word animations. Recomposing a full-screen Box that often for a background nobody reads
 * directly was a real, measurable cost worth cutting locally rather than reworking the shared
 * helper (used in ~20 other places) just for this one screen.
 */
@Composable
private fun AudiobookMorphingBackground(
    colors: List<Color>,
    duration: Duration,
    modifier: Modifier = Modifier,
) {
    val shift =
        rememberInfiniteTransition(label = "audiobookBackground").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(duration.inWholeMilliseconds.toInt()),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "audiobookBackgroundShift",
        )
    Box(
        modifier =
            modifier.drawWithCache {
                onDrawBehind {
                    val f = shift.value
                    val stops =
                        listOf(
                            lerp(colors[0], colors[2], f),
                            lerp(colors[1], colors[3], f),
                            lerp(colors[2], colors[0], f),
                        )
                    drawRect(Brush.verticalGradient(stops))
                }
            },
    )
}

private fun Long.asClock(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

/** Full chapter list of the current volume — a track list, tapping a row seeks/narrates it. */
@Composable
private fun QueueBottomSheet(
    sections: List<AudiobookSectionUi>,
    playingSectionKey: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .4f))
        },
    ) {
        Text(
            text = stringResource(R.string.audiobook_queue_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            items(sections, key = { it.key }) { section ->
                QueueSectionRow(
                    section = section,
                    isPlaying = section.key == playingSectionKey,
                    onClick = { onSelect(section.key) },
                )
            }
        }
    }
}

@Composable
private fun QueueSectionRow(
    section: AudiobookSectionUi,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.round_play_arrow_24),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.bodyLarge,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!section.isReady) {
                Text(
                    text =
                        if (section.narrated == 0) {
                            stringResource(R.string.audiobook_narrate)
                        } else {
                            stringResource(R.string.audiobook_continue_narration, section.narrated, section.planned)
                        },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f),
                )
            }
        }
        if (section.durationMs > 0) {
            Text(
                text = section.durationMs.asClock(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f),
            )
        }
    }
}
