package com.ilustris.sagai.features.audiobook.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.act.data.model.Book

/**
 * A section of a volume that is narrated as one unit: the prologue, one chapter or the epilogue.
 * [key] is stable across rewrites of the same section, so narrated segments can be matched back to it.
 */
data class AudioSection(
    val key: String,
    val title: String,
    val pages: List<String>,
) {
    companion object {
        const val PROLOGUE = "prologue"
        const val EPILOGUE = "epilogue"

        private const val CHAPTER_PREFIX = "chapter_"

        fun chapterKey(chapterId: Int) = "$CHAPTER_PREFIX$chapterId"

        fun chapterIdOf(key: String): Int? = key.takeIf { it.startsWith(CHAPTER_PREFIX) }?.removePrefix(CHAPTER_PREFIX)?.toIntOrNull()

        fun legacyKey(index: Int) = "legacy_$index"
    }
}

/**
 * Where a spoken word lives in the section text and when it is heard inside its segment clip.
 * Offsets are relative to the page content ([pageIndex] within the section), times to the clip start.
 */
data class WordTiming(
    val pageIndex: Int,
    val charStart: Int,
    val charEnd: Int,
    val startMs: Long,
    val endMs: Long,
)

object AlignmentStatus {
    /** Timings come from the transcription model and matched the written text. */
    const val ALIGNED = "ALIGNED"

    /** Transcription failed or was skipped; timings are estimated from character weights. */
    const val ESTIMATED = "ESTIMATED"

    /** The narration drifted from the written text beyond the configured tolerance. */
    const val DIVERGED = "DIVERGED"
}

/**
 * One narrated clip. Segments never overlap and are ordered by [segmentIndex] inside a section;
 * a segment may span several pages when the configured size allows it.
 */
@Entity(
    tableName = "book_audio_segments",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["bookId", "sectionKey", "segmentIndex"], unique = true)],
)
data class BookAudioSegment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val sectionKey: String,
    val segmentIndex: Int,
    val startPageIndex: Int,
    val startChar: Int,
    val endPageIndex: Int,
    val endChar: Int,
    /** Hash of the narrated text, so a rewritten section invalidates stale audio. */
    val textHash: Int,
    val audioPath: String,
    val durationMs: Long,
    val voice: String,
    val alignmentStatus: String,
    /** Match ratio between transcription and text, 0..1; null when no transcription ran. */
    val alignmentScore: Float? = null,
    val timings: List<WordTiming> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

/** Remote-only config (`book_audio_config`). No config means the audiobook feature stays hidden. */
data class BookAudioConfig(
    val transcribeModel: String = "",
    /** Upper bound of characters narrated per TTS call. Paragraphs are never split unless one alone exceeds it. */
    val maxSegmentChars: Int = 0,
    /** Voice ids ([com.ilustris.sagai.core.ai.model.Voice.id]) the narrator is drawn from. */
    val voices: List<String> = emptyList(),
    /** Pause between TTS calls, to stay under per-minute quotas. */
    val requestIntervalMs: Long = 0,
    /** Below this transcription match ratio the segment counts as diverged. */
    val minAlignmentScore: Float = 0f,
    /** How many times a diverged segment is narrated again before keeping it with estimated timings. */
    val maxRegenerations: Int = 0,
) {
    fun isValid() = maxSegmentChars > 0 && voices.isNotEmpty()
}
