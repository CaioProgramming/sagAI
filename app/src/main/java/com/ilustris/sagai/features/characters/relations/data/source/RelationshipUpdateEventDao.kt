package com.ilustris.sagai.features.characters.relations.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent

@Dao
interface RelationshipUpdateEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: RelationshipUpdateEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<RelationshipUpdateEvent>)

}
