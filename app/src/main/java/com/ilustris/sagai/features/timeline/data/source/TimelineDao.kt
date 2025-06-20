package com.ilustris.sagai.features.timeline.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTimeline(timeline: Timeline): Long

    @Query("SELECT * FROM timelines WHERE id = :id")
    fun getTimeline(id: String): Flow<Timeline>

    @Query("SELECT * FROM timelines")
    fun getAllTimelines(): Flow<List<Timeline>>

    @Query("SELECT * FROM timelines WHERE sagaId = :sagaId")
    fun getTimelineForSaga(sagaId: String): Flow<List<Timeline>>

    @Delete
    suspend fun deleteTimeline(timeline: Timeline)

    @Update
    suspend fun updateTimeline(timeline: Timeline)
}
