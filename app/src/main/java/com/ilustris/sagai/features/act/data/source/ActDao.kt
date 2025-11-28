package com.ilustris.sagai.features.act.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction // Required for queries returning relations
import androidx.room.Update
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent // Import ActContent
import kotlinx.coroutines.flow.Flow

@Dao
interface ActDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(act: Act): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(acts: List<Act>)

    @Update
    suspend fun update(act: Act)

    @Delete
    suspend fun delete(act: Act)

    @Query("SELECT * FROM acts WHERE sagaId = :sagaId ORDER BY title ASC")
    fun getActsForSaga(sagaId: Int): Flow<List<Act>>

    @Query("DELETE FROM acts WHERE sagaId = :sagaId")
    suspend fun deleteActsForSaga(sagaId: Int)

    @Transaction
    @Query("SELECT * FROM acts WHERE sagaId = :sagaId ORDER BY title ASC")
    fun getActContentsForSaga(sagaId: Int): Flow<List<ActContent>>

    // New query to get a specific ActContent by its ID
    @Transaction
    @Query("SELECT * FROM acts WHERE id = :actId")
    fun getActContent(actId: Int): Flow<ActContent?> // Nullable in case actId doesn't exist
}
