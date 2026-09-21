package com.ilustris.sagai.features.audiobook.data.usecase

import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.audiobook.data.model.AudioSection
import com.ilustris.sagai.features.audiobook.data.model.BookAudioConfig
import com.ilustris.sagai.features.audiobook.data.model.BookAudioSegment
import com.ilustris.sagai.features.audiobook.data.model.BookNarrationSummary
import kotlinx.coroutines.flow.Flow

sealed interface NarrationProgress {
    data class Narrating(
        val position: Int,
        val total: Int,
    ) : NarrationProgress

    data class Aligning(
        val position: Int,
        val total: Int,
    ) : NarrationProgress

    data class Done(
        val segments: List<BookAudioSegment>,
    ) : NarrationProgress

    data class Failed(
        val message: String,
        /** Set when [message] came from a spent daily TTS quota, so the UI can show a friendly
         * reset time instead of the raw exception text. */
        val quotaResetAt: Long? = null,
    ) : NarrationProgress
}

interface BookAudioUseCase {
    /** Null when `book_audio_config` is missing or incomplete: the audiobook stays hidden. */
    suspend fun config(): BookAudioConfig?

    /** Narratable sections of a sealed volume, in the same reading order as the book reader. */
    fun sections(act: ActContent): List<AudioSection>

    fun observeSegments(bookId: Long): Flow<List<BookAudioSegment>>

    /** Narrated duration per volume of a saga, for surfacing the audiobook outside the reader. */
    fun observeSagaNarrations(sagaId: Int): Flow<List<BookNarrationSummary>>

    /**
     * Narrates whatever the section is missing, segment by segment. Every segment is persisted on
     * its own, so a failure (e.g. daily TTS quota) keeps what was narrated and the next call resumes.
     * Segments whose text changed since they were narrated are discarded first.
     */
    fun narrateSection(
        sagaId: Int,
        actId: Int,
        sectionKey: String,
    ): Flow<NarrationProgress>

    /** Runs transcription and alignment again over existing clips, without spending TTS quota. */
    fun realignSection(
        sagaId: Int,
        actId: Int,
        sectionKey: String,
    ): Flow<NarrationProgress>

    suspend fun deleteSection(
        bookId: Long,
        sectionKey: String,
    )

    /** Whether TTS is currently blocked by a spent daily quota, so callers can hide the narrate
     * button before wasting a request that is already known to fail. */
    suspend fun ttsQuotaStatus(): Flow<QuotaStatus>
}
