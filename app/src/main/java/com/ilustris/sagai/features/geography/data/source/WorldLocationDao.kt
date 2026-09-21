package com.ilustris.sagai.features.geography.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.features.geography.data.model.WorldLocation
import com.ilustris.sagai.features.geography.data.model.WorldLocationVisit
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldLocationDao {
    @Query("SELECT * FROM world_locations WHERE sagaId = :sagaId ORDER BY name ASC")
    fun getBySaga(sagaId: Int): Flow<List<WorldLocation>>

    @Query("SELECT * FROM world_locations WHERE sagaId = :sagaId AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(
        sagaId: Int,
        name: String,
    ): WorldLocation?

    @Query("SELECT * FROM world_locations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): WorldLocation?

    @Query("SELECT * FROM world_locations WHERE parentLocationId = :parentId ORDER BY name ASC")
    fun getChildren(parentId: Int): Flow<List<WorldLocation>>

    @Query("SELECT * FROM world_locations WHERE sagaId = :sagaId AND parentLocationId IS NULL ORDER BY id ASC LIMIT 1")
    suspend fun getRootForSaga(sagaId: Int): WorldLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: WorldLocation): Long

    @Update
    suspend fun update(location: WorldLocation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: WorldLocationVisit): Long

    @Query("SELECT * FROM world_location_visits WHERE locationId = :locationId ORDER BY visitedAt DESC")
    fun getVisitsForLocation(locationId: Int): Flow<List<WorldLocationVisit>>

    @Query(
        """
        SELECT world_location_visits.* FROM world_location_visits
        INNER JOIN world_locations ON world_locations.id = world_location_visits.locationId
        WHERE world_locations.sagaId = :sagaId ORDER BY visitedAt DESC
        """,
    )
    fun getVisitsForSaga(sagaId: Int): Flow<List<WorldLocationVisit>>
}
