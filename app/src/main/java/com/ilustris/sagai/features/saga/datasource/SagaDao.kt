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
    fun getSagaContent(sagaId: Int): Flow<SagaContent>

    @Transaction
    @Query("SELECT * FROM sagas")
    fun getSagaContent(): Flow<List<SagaContent>>
}
