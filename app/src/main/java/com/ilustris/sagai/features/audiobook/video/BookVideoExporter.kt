package com.ilustris.sagai.features.audiobook.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.toArgb
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.CanvasOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.theme.GenreFontService
import com.ilustris.sagai.features.audiobook.data.model.AudioSection
import com.ilustris.sagai.features.audiobook.data.model.BookAudioSegment
import com.ilustris.sagai.features.audiobook.data.model.WordTiming
import com.ilustris.sagai.features.newsaga.data.model.Genre
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume

sealed interface VideoExportProgress {
    data class Running(
        /** 0..100, or null while the transformer can't estimate yet. */
        val percent: Int?,
    ) : VideoExportProgress

    data class Done(
        val file: File,
    ) : VideoExportProgress
}

/** A word on the export timeline: [startMs] counts from the start of the whole section. */
private data class TimelineWord(
    val startMs: Long,
    val timing: WordTiming,
)

/**
 * Renders a narrated section into a vertical MP4: the background image for the whole narration,
 * the section's WAV clips back to back as audio, and a canvas overlay drawing the lyrics-style
 * text, redrawn per frame from the same word timings the reader uses.
 */
@OptIn(UnstableApi::class)
class BookVideoExporter
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val genreFontService: GenreFontService,
        private val genreVisualConfigService: GenreVisualConfigService,
    ) {
        fun export(
            section: AudioSection,
            segments: List<BookAudioSegment>,
            timingsFor: (BookAudioSegment) -> List<WordTiming>,
            backgroundPath: String?,
            genre: Genre,
            sagaTitle: String,
        ): Flow<VideoExportProgress> =
            channelFlow {
                val ordered = segments.sortedBy { it.segmentIndex }
                require(ordered.isNotEmpty()) { "Section ${section.key} has no narration" }
                val totalMs = ordered.sumOf { it.durationMs }

                var offset = 0L
                val timeline =
                    ordered.flatMap { segment ->
                        val words = timingsFor(segment).map { TimelineWord(offset + it.startMs, it) }
                        offset += segment.durationMs
                        words
                    }

                val visualConfig = genreVisualConfigService.getVisualConfig(genre)
                val (headerTypeface, bodyTypeface) = genreFontService.getTypefaces(genre, visualConfig)
                val background = backgroundUri(backgroundPath, genre)
                val output =
                    File(context.cacheDir, "audiobook").apply { mkdirs() }.resolve("${section.key}_${System.currentTimeMillis()}.mp4")

                val overlay =
                    LyricsOverlay(
                        pages = section.pages,
                        timeline = timeline,
                        title = section.title,
                        subtitle = sagaTitle,
                        accent = genre.color.toArgb(),
                        headerTypeface = headerTypeface,
                        bodyTypeface = bodyTypeface,
                    )

                val video =
                    EditedMediaItem
                        .Builder(
                            MediaItem
                                .Builder()
                                .setUri(background)
                                .setImageDurationMs(totalMs)
                                .build(),
                        ).setFrameRate(FRAME_RATE)
                        .setEffects(
                            Effects(
                                emptyList(),
                                listOf(
                                    Presentation.createForWidthAndHeight(WIDTH, HEIGHT, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP),
                                    OverlayEffect(listOf(overlay)),
                                ),
                            ),
                        ).build()

                val audio =
                    ordered.map { segment ->
                        EditedMediaItem
                            .Builder(MediaItem.fromUri(Uri.fromFile(File(segment.audioPath))))
                            .setRemoveVideo(true)
                            .build()
                    }

                val composition =
                    Composition
                        .Builder(
                            EditedMediaItemSequence.Builder(video).build(),
                            EditedMediaItemSequence.Builder(audio).build(),
                        ).build()

                send(VideoExportProgress.Running(null))
                val result =
                    withContext(Dispatchers.Main) {
                        suspendCancellableCoroutine { continuation ->
                            val transformer =
                                Transformer
                                    .Builder(context)
                                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                                    .addListener(
                                        object : Transformer.Listener {
                                            override fun onCompleted(
                                                composition: Composition,
                                                exportResult: ExportResult,
                                            ) {
                                                if (continuation.isActive) continuation.resume(Result.success(output))
                                            }

                                            override fun onError(
                                                composition: Composition,
                                                exportResult: ExportResult,
                                                exportException: ExportException,
                                            ) {
                                                if (continuation.isActive) continuation.resume(Result.failure(exportException))
                                            }
                                        },
                                    ).build()

                            val progressJob =
                                launch {
                                    val holder = ProgressHolder()
                                    while (continuation.isActive) {
                                        if (transformer.getProgress(holder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                                            trySend(VideoExportProgress.Running(holder.progress))
                                        }
                                        delay(PROGRESS_POLL_MS)
                                    }
                                }
                            continuation.invokeOnCancellation {
                                progressJob.cancel()
                                transformer.cancel()
                                output.delete()
                            }
                            transformer.start(composition, output.absolutePath)
                        }
                    }
                send(VideoExportProgress.Done(result.getOrThrow()))
            }

        /** Transformer needs an image input; without artwork the frame is a flat genre-colored card. */
        private fun backgroundUri(
            path: String?,
            genre: Genre,
        ): Uri {
            path?.let(::File)?.takeIf { it.exists() }?.let { return Uri.fromFile(it) }
            val file = File(context.cacheDir, "audiobook").apply { mkdirs() }.resolve("background_${genre.name}.png")
            if (!file.exists()) {
                val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
                Canvas(bitmap).drawColor(genre.color.toArgb())
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
            return Uri.fromFile(file)
        }

        companion object {
            const val WIDTH = 1080
            const val HEIGHT = 1920
            private const val FRAME_RATE = 30
            private const val PROGRESS_POLL_MS = 250L
        }
    }

/**
 * Lyrics-style text over the frame: the current line bright with the spoken word in the genre
 * accent, neighbouring lines fading out with distance. Layouts are built once per page; each frame
 * only looks the word up and draws a handful of lines.
 */
@OptIn(UnstableApi::class)
private class LyricsOverlay(
    private val pages: List<String>,
    private val timeline: List<TimelineWord>,
    private val title: String,
    private val subtitle: String,
    private val accent: Int,
    headerTypeface: Typeface,
    bodyTypeface: Typeface,
) : CanvasOverlay(true) {
    private val margin = 96f
    private val bodyPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bodyTypeface
            textSize = 54f
            color = Color.WHITE
        }
    private val titlePaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = headerTypeface
            textSize = 72f
            color = accent
            textAlign = Paint.Align.CENTER
        }
    private val subtitlePaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bodyTypeface
            textSize = 36f
            color = Color.WHITE
            alpha = 180
            letterSpacing = .15f
            textAlign = Paint.Align.CENTER
        }
    private val shadePaint = Paint()
    private val layouts = mutableMapOf<Int, StaticLayout>()

    override fun onDraw(
        canvas: Canvas,
        presentationTimeUs: Long,
    ) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        if (shadePaint.shader == null) {
            shadePaint.shader =
                LinearGradient(0f, 0f, 0f, height, intArrayOf(0x66000000, 0x33000000, 0xE6000000.toInt()), floatArrayOf(0f, .35f, .7f), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width, height, shadePaint)
        canvas.drawText(subtitle.uppercase(), width / 2, 220f, subtitlePaint)
        canvas.drawText(title, width / 2, 320f, titlePaint)

        val word = currentWord(presentationTimeUs / 1000) ?: return
        val page = pages.getOrNull(word.timing.pageIndex) ?: return
        val layout = layouts.getOrPut(word.timing.pageIndex) { layoutFor(page, (width - margin * 2).toInt()) }
        val currentLine = layout.getLineForOffset(word.timing.charStart)
        val lineHeight = bodyPaint.fontSpacing * 1.25f
        val anchorY = height * .66f

        for (line in (currentLine - VISIBLE_BEFORE)..(currentLine + VISIBLE_AFTER)) {
            if (line < 0 || line >= layout.lineCount) continue
            val distance = kotlin.math.abs(line - currentLine)
            val y = anchorY + (line - currentLine) * lineHeight
            val start = layout.getLineStart(line)
            val end = layout.getLineEnd(line)
            val text = page.substring(start, end).trimEnd()

            if (distance != 0) {
                bodyPaint.color = Color.WHITE
                bodyPaint.alpha = (150 - distance * 35).coerceAtLeast(30)
                canvas.drawText(text, margin, y, bodyPaint)
                continue
            }

            val wordStart = (word.timing.charStart - start).coerceIn(0, text.length)
            val wordEnd = (word.timing.charEnd - start).coerceIn(wordStart, text.length)
            var x = margin
            listOf(
                text.substring(0, wordStart) to Color.WHITE,
                text.substring(wordStart, wordEnd) to accent,
                text.substring(wordEnd) to Color.WHITE,
            ).forEach { (piece, color) ->
                bodyPaint.color = color
                bodyPaint.alpha = 255
                canvas.drawText(piece, x, y, bodyPaint)
                x += bodyPaint.measureText(piece)
            }
        }
    }

    private fun currentWord(timeMs: Long): TimelineWord? {
        if (timeline.isEmpty()) return null
        var low = 0
        var high = timeline.lastIndex
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (timeline[mid].startMs <= timeMs) low = mid else high = mid - 1
        }
        return timeline[low]
    }

    private fun layoutFor(
        text: String,
        width: Int,
    ): StaticLayout =
        StaticLayout.Builder
            .obtain(text, 0, text.length, bodyPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()

    companion object {
        private const val VISIBLE_BEFORE = 4
        private const val VISIBLE_AFTER = 5
    }
}
