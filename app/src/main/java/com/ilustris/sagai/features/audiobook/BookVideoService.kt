package com.ilustris.sagai.features.audiobook

import com.ilustris.sagai.features.audiobook.data.model.AudioSection
import com.ilustris.sagai.features.audiobook.data.model.BookAudioSegment
import com.ilustris.sagai.features.audiobook.data.model.WordTiming
import com.ilustris.sagai.features.audiobook.video.BookVideoExporter
import com.ilustris.sagai.features.audiobook.video.VideoExportProgress
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class VideoExportJob(
    val sectionKey: String,
    /** 0..100, or null before the transformer can estimate. */
    val percent: Int?,
    val error: String? = null,
)

/**
 * Everything one export needs, resolved by the caller before handing it over — the timings are
 * copied in rather than passed as a lambda so a render outlives the screen that asked for it.
 */
data class VideoExportRequest(
    val section: AudioSection,
    val segments: List<BookAudioSegment>,
    val timings: Map<Long, List<WordTiming>>,
    val backgroundPath: String?,
    val genre: Genre,
    val sagaTitle: String,
)

/**
 * Renders audiobook videos in a singleton-scoped coroutine so leaving the reader does not throw
 * away a render that takes minutes. One at a time: the transformer is already using the device's
 * video encoder, and a second export would only make both slower.
 */
@Singleton
class BookVideoService
    @Inject
    constructor(
        private val exporter: BookVideoExporter,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private val _job = MutableStateFlow<VideoExportJob?>(null)

        /** The running export, or the last failed one until [dismissFailure] or a new export starts. */
        val job: StateFlow<VideoExportJob?> = _job.asStateFlow()

        private val _ready = MutableSharedFlow<File>(extraBufferCapacity = 1)

        /** One-shot: a finished file ready for the share sheet. */
        val ready = _ready.asSharedFlow()

        fun export(request: VideoExportRequest) {
            val current = _job.value
            if (current != null && current.error == null) return
            _job.value = VideoExportJob(request.section.key, null)
            scope.launch {
                exporter
                    .export(
                        section = request.section,
                        segments = request.segments,
                        timingsFor = { request.timings[it.id].orEmpty() },
                        backgroundPath = request.backgroundPath,
                        genre = request.genre,
                        sagaTitle = request.sagaTitle,
                    ).catch { error ->
                        _job.value = VideoExportJob(request.section.key, null, error.message.orEmpty())
                    }.collect { progress ->
                        when (progress) {
                            is VideoExportProgress.Running -> _job.value = VideoExportJob(request.section.key, progress.percent)
                            is VideoExportProgress.Done -> {
                                _job.value = null
                                _ready.tryEmit(progress.file)
                            }
                        }
                    }
            }
        }

        fun dismissFailure() {
            if (_job.value?.error != null) _job.value = null
        }
    }
