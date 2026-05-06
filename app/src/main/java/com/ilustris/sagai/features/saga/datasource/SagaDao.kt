package com.ilustris.sagai.features.saga.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow

@Dao
interface SagaDao {
    @Query("SELECT * FROM sagas")
    fun getAllSagas(): Flow<List<Saga>>

    @Query("SELECT * FROM sagas WHERE id = :sagaId")
    fun getSaga(sagaId: Int): Flow<Saga?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSagaData(saga: Saga): Long

    @Update
    suspend fun updateSaga(saga: Saga)

    @Delete
    suspend fun deleteSagaData(saga: Saga)

    @Query("DELETE FROM sagas WHERE id = :sagaId")
    suspend fun deleteSagaData(sagaId: String)

    @Query("DELETE FROM sagas")
    suspend fun deleteAllSagas()

    @Transaction
    @Query("SELECT * FROM sagas WHERE id = :sagaId")
    fun getSagaContent(sagaId: Int): Flow<SagaContent?>

    @Transaction
    @Query("SELECT * FROM sagas")
    fun getSagaContent(): Flow<List<SagaContent>>

    @Transaction
    @Query(
        """
        SELECT sagas.*, 
               (SELECT text FROM messages WHERE sagaId = sagas.id ORDER BY timestamp DESC LIMIT 1) AS lastMessageText,
               (SELECT timestamp FROM messages WHERE sagaId = sagas.id ORDER BY timestamp DESC LIMIT 1) AS lastMessageTime,
               (SELECT senderType FROM messages WHERE sagaId = sagas.id ORDER BY timestamp DESC LIMIT 1) AS lastMessageSender,
               (SELECT speakerName FROM messages WHERE sagaId = sagas.id ORDER BY timestamp DESC LIMIT 1) AS lastMessageSpeaker,
               (SELECT COUNT(id) FROM messages WHERE sagaId = sagas.id) AS messagesCount,
               (SELECT COUNT(Chapter.id) FROM Chapter JOIN acts ON Chapter.actId = acts.id WHERE acts.sagaId = sagas.id) AS chaptersCount
        FROM sagas
    """,
    )
    fun getSagaSummaries(): Flow<List<com.ilustris.sagai.features.home.data.model.SagaSummary>>

    @Transaction
    @Query("SELECT * FROM sagas WHERE playTimeMs > 0 OR isEnded = 1")
    fun getPlaythroughData(): Flow<List<com.ilustris.sagai.features.playthrough.data.model.SagaPlaythrough>>
}
