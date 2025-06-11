package com.ilustris.sagai.features.saga.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

@Dao
interface SagaDao {
    @Query("SELECT * FROM sagas")
    fun getAllSagas(): Flow<List<SagaData>>

    @Query("SELECT * FROM sagas WHERE id = :sagaId")
    fun getSaga(sagaId: Int): Flow<SagaData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSagaData(sagaData: SagaData): Long

    @Update
    suspend fun updateSaga(sagaData: SagaData)

    @Delete
    suspend fun deleteSagaData(sagaData: SagaData)

    @Query("DELETE FROM sagas WHERE id = :sagaId")
    suspend fun deleteSagaData(sagaId: String)

    @Query("DELETE FROM sagas")
    suspend fun deleteAllSagas()

    @Transaction
    @Query("SELECT * FROM sagas WHERE id = :sagaId")
    fun getSagaContent(sagaId: Int): Flow<SagaContent>

    @Transaction
    @Query("SELECT * FROM sagas")
    fun getSagaContent(): Flow<List<SagaContent>>
}
