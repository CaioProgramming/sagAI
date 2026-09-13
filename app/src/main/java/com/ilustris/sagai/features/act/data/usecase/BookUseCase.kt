package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import kotlinx.coroutines.flow.Flow

sealed interface VolumeProgress {
    data object Prologue : VolumeProgress

    data class Chapter(
        val title: String,
        val position: Int,
        val total: Int,
    ) : VolumeProgress

    data object Closure : VolumeProgress

    /** Everything this pass could write is written; [sealed] tells whether the volume is closed. */
    data class Done(
        val sealed: Boolean,
    ) : VolumeProgress

    data class Failed(
        val message: String,
    ) : VolumeProgress
}

interface BookUseCase {
    /**
     * Writes whatever the act's volume is missing, in reading order: prologue → each completed
     * chapter without pages → closure (only once the act is complete). Every step is idempotent and
     * persisted on its own, so a failure keeps what was written and the next pass resumes from it.
     * Stops at the first failure.
     */
    fun writeVolume(
        sagaId: Int,
        actId: Int,
    ): Flow<VolumeProgress>

    suspend fun writePrologue(
        sagaId: Int,
        actId: Int,
    ): RequestResult<Unit>

    suspend fun writeChapter(
        sagaId: Int,
        chapterId: Int,
    ): RequestResult<Unit>

    suspend fun closeVolume(
        sagaId: Int,
        actId: Int,
    ): RequestResult<Unit>

    /** Drops the act's written pages and conclusion so [writeVolume] rewrites the whole volume. */
    suspend fun resetVolume(actId: Int)

    /** Drops a single chapter's pages, e.g. after the chapter itself was re-synthesized. */
    suspend fun invalidateChapter(chapterId: Int)
}
