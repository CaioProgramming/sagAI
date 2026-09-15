package com.ilustris.sagai.features.audiobook.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.audiobook.data.model.BookAudioSegment
import kotlinx.coroutines.flow.Flow

@Dao
interface BookAudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSegment(segment: BookAudioSegment): Long

    @Query("SELECT * FROM book_audio_segments WHERE bookId = :bookId ORDER BY sectionKey, segmentIndex")
    fun observeSegments(bookId: Long): Flow<List<BookAudioSegment>>

    @Query("SELECT * FROM book_audio_segments WHERE bookId = :bookId AND sectionKey = :sectionKey ORDER BY segmentIndex")
    suspend fun getSectionSegments(
        bookId: Long,
        sectionKey: String,
    ): List<BookAudioSegment>

    @Query("DELETE FROM book_audio_segments WHERE bookId = :bookId AND sectionKey = :sectionKey")
    suspend fun deleteSection(
        bookId: Long,
        sectionKey: String,
    )

    /**
     * Plain UPDATE on purpose: saving the whole [com.ilustris.sagai.features.act.data.model.Book]
     * uses REPLACE, which deletes the row first and would cascade away every narrated segment.
     */
    @Query("UPDATE books SET narrationVoice = :voice WHERE id = :bookId")
    suspend fun setNarrationVoice(
        bookId: Long,
        voice: String,
    )
}
