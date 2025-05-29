package com.ilustris.sagai.features.chat.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

@Dao
interface SagaDao {
    @Query("SELECT * FROM SagaData")
    fun getAllSagas(): Flow<List<SagaData>>

    @Query("SELECT * FROM SagaData WHERE id = :sagaId")
    fun getSaga(sagaId: String): Flow<SagaData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSagaData(sagaData: SagaData): Long

    @Delete
    suspend fun deleteSagaData(sagaData: SagaData)

    @Query("DELETE FROM SagaData WHERE id = :sagaId")
    suspend fun deleteSagaData(sagaId: String)

    @Query("DELETE FROM sagaData")
    suspend fun deleteAllSagas()
}
