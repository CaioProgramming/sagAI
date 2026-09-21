package com.ilustris.sagai.features.audiobook.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.audiobook.data.model.BookAudioSegment
import com.ilustris.sagai.features.audiobook.data.model.BookNarrationSummary
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

    /** Narrated duration per volume of a saga — the shelf shows it without loading every segment. */
    @Query(
        "SELECT s.bookId AS bookId, SUM(s.durationMs) AS durationMs FROM book_audio_segments s " +
            "JOIN books b ON b.id = s.bookId JOIN acts a ON a.id = b.actId " +
            "WHERE a.sagaId = :sagaId GROUP BY s.bookId",
    )
    fun observeSagaNarrations(sagaId: Int): Flow<List<BookNarrationSummary>>

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
