package com.ilustris.sagai.features.audiobook.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.media.SagaPlaybackService
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.audiobook.BookAudioService
import com.ilustris.sagai.features.audiobook.NarrationJob
import com.ilustris.sagai.features.audiobook.data.model.AlignmentStatus
import com.ilustris.sagai.features.audiobook.data.usecase.NarrationProgress
import com.ilustris.sagai.features.audiobook.data.model.AudioSection
import com.ilustris.sagai.features.audiobook.data.model.BookAudioSegment
import com.ilustris.sagai.features.audiobook.data.model.WordTiming
import com.ilustris.sagai.features.audiobook.data.usecase.BookAudioSegmenter
import com.ilustris.sagai.features.audiobook.data.usecase.BookAudioUseCase
import com.ilustris.sagai.features.audiobook.data.usecase.TimingAligner
import com.ilustris.sagai.features.audiobook.data.usecase.WaveformExtractor
import com.ilustris.sagai.features.audiobook.player.BookAudioPlayer
import com.ilustris.sagai.features.audiobook.player.PlaybackState
import com.ilustris.sagai.features.audiobook.player.PlaybackTrack
import com.ilustris.sagai.features.audiobook.BookVideoService
import com.ilustris.sagai.features.audiobook.VideoExportJob
import com.ilustris.sagai.features.audiobook.VideoExportRequest
import com.ilustris.sagai.features.audiobook.video.VideoExportProgress
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.SharePlayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioHighlight(
    val sectionKey: String,
    val pageIndex: Int,
    val charStart: Int,
    val charEnd: Int,
)

data class AudiobookSectionUi(
    val key: String,
    val title: String,
    val narrated: Int,
    val planned: Int,
    /** Lowest transcription match among narrated clips; null when none was transcribed. */
    val worstScore: Float?,
    val estimatedClips: Int,
    /** Sum of narrated clips' duration so far; grows as more segments are narrated. */
    val durationMs: Long,
) {
    val isReady get() = planned > 0 && narrated >= planned
}

enum class SyncSource { TRANSCRIBED, ESTIMATED }

data class VideoExportUi(
    val sectionKey: String,
    val percent: Int?,
    val error: String? = null,
)

data class AudiobookUiState(
    val bookId: Long,
    val sections: List<AudiobookSectionUi>,
    val narration: NarrationJob?,
    val playingSectionKey: String?,
    val isPlaying: Boolean,
    val highlight: AudioHighlight?,
    val syncSource: SyncSource,
    val showDebug: Boolean,
    val videoExport: VideoExportUi?,
    /** Elapsed / total for the whole section currently playing, not just its current clip. */
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null,
    /** Set when the error came from a spent daily TTS quota, so the UI can show a friendly reset
     * time instead of the raw exception text. */
    val quotaResetAt: Long? = null,
    /** Set even before the user tries narrating anything, whenever TTS is already known to be out
     * of quota for the day — lets the "listen to next chapter" prompt hide itself instead of
     * inviting a request that is already known to fail. */
    val ttsQuotaResetAt: Long? = null,
    /** In-character loading line for the "Narrando…" state, same mechanism every other generation
     * in the app uses. Null until the first one lands — the UI falls back to a static string. */
    val narratingReasoningText: String? = null,
) {
    fun section(key: String?) = sections.find { it.key == key }
}

private data class ExtraState(
    val syncSource: SyncSource,
    val videoExport: VideoExportJob?,
    val quotaStatus: QuotaStatus,
    val reasoningText: String?,
)

private data class BoundBook(
    val saga: SagaContent,
    val act: ActContent,
    val bookId: Long,
    val sections: List<AudioSection>,
    /** Segments each section splits into, computed once: the UI state recombines on every playback tick. */
    val plannedSegments: Map<String, Int>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AudiobookViewModel
    @Inject
    constructor(
        private val bookAudioUseCase: BookAudioUseCase,
        private val bookAudioService: BookAudioService,
        private val player: BookAudioPlayer,
        private val bookVideoService: BookVideoService,
        private val sharePlayUseCase: SharePlayUseCase,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val bound = MutableStateFlow<BoundBook?>(null)
        /**
         * One-shot: a finished export ready to hand to the share sheet. The render itself lives in
         * [BookVideoService], so it survives leaving this screen; this only turns the file into a
         * shareable uri for whoever is on screen when it lands.
         */
        val videoReady: Flow<Uri> =
            bookVideoService.ready.mapNotNull { file ->
                (sharePlayUseCase.loadWithFileProvider(file) as? RequestResult.Success)?.value
            }
        private val syncSource = MutableStateFlow(SyncSource.TRANSCRIBED)
        private var segments: List<BookAudioSegment> = emptyList()
        private val estimatedCache = mutableMapOf<Long, List<WordTiming>>()

        private val segmentsFlow =
            bound.flatMapLatest { book -> book?.let { bookAudioUseCase.observeSegments(it.bookId) } ?: emptyFlow() }

        /** Whether TTS is already known to be out of daily quota, ahead of trying anything. */
        private val ttsQuotaStatus: Flow<QuotaStatus> = flow { emitAll(bookAudioUseCase.ttsQuotaStatus()) }

        /**
         * In-character loading lines while narration is running, same mechanism (and pool) every
         * other generation in the app uses — reusing [ReasoningSynthesizerService.synthesizeReasoning]
         * with a trigger flow that never completes while a job is active, instead of decorating
         * narrateSection's own NarrationProgress flow directly: that flow already carries delicate
         * per-segment resume logic and the friendly-quota-error plumbing, and threading it through
         * StreamingState<T> and back would risk both for a purely cosmetic addition. Cancelling the
         * trigger (a new job starting, or none running) tears down synthesizeReasoning's internal
         * coroutines the same way switching sagas would.
         */
        private val narratingReasoningText: Flow<String?> =
            bookAudioService.job
                .map { it != null }
                .distinctUntilChanged()
                .flatMapLatest { isNarrating ->
                    if (!isNarrating) {
                        flowOf(null)
                    } else {
                        val genre = bound.value?.saga?.data?.genre
                        val neverCompletes = flow<StreamingState<Unit>> { awaitCancellation() }
                        reasoningSynthesizerService
                            .synthesizeReasoning(neverCompletes, context = "Narrando o audiolivro", genre = genre)
                            .filterIsInstance<StreamingState.Reasoning>()
                            .map { it.chunk }
                    }
                }

        /**
         * The word highlight, decoupled from [state] on purpose: [player.state] ticks every
         * [BookAudioPlayer]'s TICK_MS while playing, and [state]'s combine can't avoid re-emitting
         * on every one of those (positionMs always differs). Word-boundary changes are far rarer
         * than that, so this flow is deduped right at the source — the lyrics scroller collects
         * *this*, not [state], so its per-word blur/animation work only ever runs on a real
         * boundary crossing instead of on every position tick.
         */
        val highlightFlow: StateFlow<AudioHighlight?> =
            combine(bound, segmentsFlow, player.state, syncSource) { book, bookSegments, playback, source ->
                if (book == null || playback == null || playback.current?.bookId != book.bookId) return@combine null
                highlight(book, playback, source, bookSegments)
            }.distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        /**
         * Loudness envelope of the section playing right now, for the waveform seek bar. Computed
         * once per (section, set of clips) off the main thread from the WAVs already on disk and
         * kept out of [state] on purpose: it is heavy to build and never changes between ticks.
         */
        val waveform: StateFlow<Waveform?> =
            combine(segmentsFlow, player.state.map { it?.current?.sectionKey }.distinctUntilChanged()) { bookSegments, sectionKey ->
                sectionKey?.let { key ->
                    key to
                        bookSegments
                            .filter { it.sectionKey == key }
                            .sortedBy { it.segmentIndex }
                            .map { it.audioPath }
                }
            }.distinctUntilChanged()
                .mapLatest { target ->
                    target?.takeIf { it.second.isNotEmpty() }?.let { (key, paths) ->
                        Waveform(key, WaveformExtractor.extract(paths))
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        val state: StateFlow<AudiobookUiState?> =
            combine(
                bound,
                segmentsFlow,
                bookAudioService.job,
                player.state,
                combine(syncSource, bookVideoService.job, ttsQuotaStatus, narratingReasoningText, ::ExtraState),
            ) {
                book,
                bookSegments,
                job,
                playback,
                extra,
                ->
                book ?: return@combine null
                segments = bookSegments
                val playing = playback?.current?.takeIf { it.bookId == book.bookId }
                val sectionTracks = playback?.tracks?.filter { it.sectionKey == playing?.sectionKey }.orEmpty()
                val elapsedBeforeCurrent =
                    playback
                        ?.tracks
                        ?.take(playback.index)
                        ?.filter { it.sectionKey == playing?.sectionKey }
                        ?.sumOf { it.durationMs } ?: 0L
                val currentJob = job?.takeIf { it.bookId == book.bookId }
                val failure = currentJob?.progress as? NarrationProgress.Failed
                AudiobookUiState(
                    bookId = book.bookId,
                    sections = book.sections.map { section -> sectionUi(book, section, bookSegments) },
                    narration = currentJob,
                    playingSectionKey = playing?.sectionKey,
                    isPlaying = playing != null && playback.isPlaying,
                    highlight = playing?.let { highlight(book, playback, extra.syncSource, bookSegments) },
                    syncSource = extra.syncSource,
                    showDebug = BuildConfig.DEBUG,
                    videoExport = extra.videoExport?.let { VideoExportUi(it.sectionKey, it.percent, it.error) },
                    positionMs = if (playing != null) elapsedBeforeCurrent + playback.positionMs else 0L,
                    durationMs = sectionTracks.sumOf { it.durationMs },
                    error = failure?.message,
                    quotaResetAt = failure?.quotaResetAt,
                    ttsQuotaResetAt = (extra.quotaStatus as? QuotaStatus.DailyExhausted)?.until,
                    narratingReasoningText = extra.reasoningText,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        init {
            // Duck the ambient track under narration; restore it the moment playback stops.
            viewModelScope.launch {
                state
                    .map { it?.isPlaying == true }
                    .distinctUntilChanged()
                    .collect { playing ->
                        val action = if (playing) SagaPlaybackService.ACTION_DUCK else SagaPlaybackService.ACTION_UNDUCK
                        SagaPlaybackService.startSafely(context, SagaPlaybackService.playbackIntent(context, action))
                    }
            }
        }

        override fun onCleared() {
            super.onCleared()
            SagaPlaybackService.startSafely(
                context,
                SagaPlaybackService.playbackIntent(context, SagaPlaybackService.ACTION_UNDUCK),
            )
        }

        /** Binds the volume on screen; the audiobook stays hidden when the remote config is missing. */
        fun bind(
            saga: SagaContent,
            act: ActContent,
        ) {
            val book = act.book?.takeIf { it.isSealed() } ?: return
            if (bound.value?.bookId == book.id && bound.value?.act == act) return
            viewModelScope.launch {
                val config = bookAudioUseCase.config() ?: return@launch
                val sections = bookAudioUseCase.sections(act)
                bound.value =
                    BoundBook(
                        saga = saga,
                        act = act,
                        bookId = book.id,
                        sections = sections,
                        plannedSegments = sections.associate { it.key to BookAudioSegmenter.plan(it.pages, config.maxSegmentChars).size },
                    )
            }
        }

        fun onAction(action: AudiobookAction) {
            when (action) {
                is AudiobookAction.Listen -> listen(action.sectionKey, action.pageIndex)
                AudiobookAction.TogglePlayback -> player.toggle()
                AudiobookAction.Stop -> player.stop()
                is AudiobookAction.SeekToText -> seekToText(action.sectionKey, action.pageIndex, action.charOffset)
                AudiobookAction.ToggleSyncSource -> toggleSyncSource()
                is AudiobookAction.Realign -> realign(action.sectionKey)
                is AudiobookAction.ExportVideo -> exportVideo(action.sectionKey)
                is AudiobookAction.DeleteNarration -> deleteNarration(action.sectionKey)
                AudiobookAction.DismissFailure -> {
                    bookAudioService.dismissFailure()
                    bookVideoService.dismissFailure()
                }
            }
        }

        /** Plays the section from [pageIndex] when it is fully narrated, otherwise narrates what is missing. */
        private fun listen(
            sectionKey: String,
            pageIndex: Int,
        ) {
            val book = bound.value ?: return
            val section = state.value?.section(sectionKey) ?: return
            if (section.isReady) {
                // A failure from a previous, different section (e.g. picked from the queue) must
                // not keep blocking the screen behind an error banner once playback has moved on.
                bookAudioService.dismissFailure()
                play(book, sectionKey, pageIndex)
            } else {
                bookAudioService.narrate(book.saga.data.id, book.act.data.id, book.bookId, sectionKey)
            }
        }

        /** Jumps narration to the word under [charOffset] of a page, e.g. after tapping the text. */
        private fun seekToText(
            sectionKey: String,
            pageIndex: Int,
            charOffset: Int,
        ) {
            val book = bound.value ?: return
            val sectionSegments = segments.filter { it.sectionKey == sectionKey }
            val match =
                sectionSegments.firstNotNullOfOrNull { segment ->
                    timingsFor(book, segment, syncSource.value)
                        .firstOrNull { it.pageIndex == pageIndex && charOffset < it.charEnd }
                        ?.let { segment to it }
                } ?: return
            if (state.value?.playingSectionKey == null) play(book, sectionKey, pageIndex)
            player.seekTo(match.first.id, match.second.startMs)
        }

        /** Seeks within the currently playing section only — [targetMs] is relative to its own start. */
        fun seekToSectionMs(targetMs: Long) {
            val playback = player.state.value ?: return
            val sectionKey = state.value?.playingSectionKey ?: return
            val sectionTracks = playback.tracks.filter { it.sectionKey == sectionKey }
            var remaining = targetMs.coerceAtLeast(0L)
            for (track in sectionTracks) {
                if (remaining < track.durationMs || track == sectionTracks.last()) {
                    player.seekTo(track.segmentId, remaining.coerceAtMost(track.durationMs))
                    return
                }
                remaining -= track.durationMs
            }
        }

        private fun toggleSyncSource() {
            syncSource.value = if (syncSource.value == SyncSource.TRANSCRIBED) SyncSource.ESTIMATED else SyncSource.TRANSCRIBED
        }

        private fun realign(sectionKey: String) {
            val book = bound.value ?: return
            bookAudioService.realign(book.saga.data.id, book.act.data.id, book.bookId, sectionKey)
        }

        private fun exportVideo(sectionKey: String) {
            val book = bound.value ?: return
            val section = book.sections.find { it.key == sectionKey } ?: return
            val sectionSegments = segments.filter { it.sectionKey == sectionKey }
            val background =
                AudioSection
                    .chapterIdOf(sectionKey)
                    ?.let { chapterId -> book.act.chapters.find { it.data.id == chapterId }?.data?.coverImage }
                    ?.takeIf { it.isNotBlank() }
                    ?: book.saga.data.icon.takeIf { it.isNotBlank() }

            bookVideoService.export(
                VideoExportRequest(
                    section = section,
                    segments = sectionSegments,
                    // Resolved here, while the book is bound: the render outlives this ViewModel.
                    timings = sectionSegments.associate { it.id to timingsFor(book, it, syncSource.value) },
                    backgroundPath = background,
                    genre = book.saga.data.genre,
                    sagaTitle = book.saga.data.title,
                ),
            )
        }

        /**
         * Drops a section's narration and its audio files. Narrating again costs the user's daily
         * TTS quota, so the UI confirms before this runs.
         */
        private fun deleteNarration(sectionKey: String) {
            val book = bound.value ?: return
            viewModelScope.launch {
                if (state.value?.playingSectionKey == sectionKey) player.stop()
                bookAudioUseCase.deleteSection(book.bookId, sectionKey)
            }
        }

        private fun play(
            book: BoundBook,
            sectionKey: String,
            pageIndex: Int,
        ) {
            val order = book.sections.map { it.key }
            val queue =
                segments
                    .sortedWith(compareBy({ order.indexOf(it.sectionKey) }, { it.segmentIndex }))
                    .map { PlaybackTrack(it.id, it.bookId, it.sectionKey, it.audioPath, it.durationMs) }
            val start =
                segments
                    .filter { it.sectionKey == sectionKey && pageIndex <= it.endPageIndex }
                    .minByOrNull { it.segmentIndex } ?: return
            val startMs =
                timingsFor(book, start, syncSource.value)
                    .firstOrNull { it.pageIndex >= pageIndex }
                    ?.startMs ?: 0L
            val title = book.sections.find { it.key == sectionKey }?.title.orEmpty()
            player.play(
                queue = queue,
                startIndex = queue.indexOfFirst { it.segmentId == start.id },
                startPositionMs = if (start.startPageIndex < pageIndex) startMs else 0L,
                title = title,
                subtitle = book.saga.data.title,
            )
        }

        private fun sectionUi(
            book: BoundBook,
            section: AudioSection,
            bookSegments: List<BookAudioSegment>,
        ): AudiobookSectionUi {
            val narrated = bookSegments.filter { it.sectionKey == section.key }
            return AudiobookSectionUi(
                key = section.key,
                title = section.title,
                narrated = narrated.size,
                planned = book.plannedSegments[section.key] ?: 0,
                worstScore = narrated.mapNotNull { it.alignmentScore }.minOrNull(),
                estimatedClips = narrated.count { it.alignmentStatus == AlignmentStatus.ESTIMATED },
                durationMs = narrated.sumOf { it.durationMs },
            )
        }

        private fun highlight(
            book: BoundBook,
            playback: PlaybackState,
            source: SyncSource,
            bookSegments: List<BookAudioSegment>,
        ): AudioHighlight? {
            val track = playback.current ?: return null
            val segment = bookSegments.find { it.id == track.segmentId } ?: return null
            val timings = timingsFor(book, segment, source)
            val word = timings.lastOrNull { it.startMs <= playback.positionMs } ?: timings.firstOrNull() ?: return null
            return AudioHighlight(segment.sectionKey, word.pageIndex, word.charStart, word.charEnd)
        }

        private fun timingsFor(
            book: BoundBook,
            segment: BookAudioSegment,
            source: SyncSource,
        ): List<WordTiming> {
            if (source == SyncSource.TRANSCRIBED && segment.timings.isNotEmpty()) return segment.timings
            return estimatedCache.getOrPut(segment.id) {
                val pages = book.sections.find { it.key == segment.sectionKey }?.pages ?: return emptyList()
                runCatching {
                    val plan =
                        BookAudioSegmenter.between(
                            pages,
                            segment.segmentIndex,
                            segment.startPageIndex,
                            segment.startChar,
                            segment.endPageIndex,
                            segment.endChar,
                        )
                    TimingAligner.estimate(pages, plan, segment.durationMs)
                }.getOrDefault(emptyList())
            }
        }
    }
