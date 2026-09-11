package com.ilustris.sagai.features.geography.data.usecase

import com.ilustris.sagai.features.geography.data.model.WorldLocation
import com.ilustris.sagai.features.narrative.data.model.GeneratedLocationCheckpoint
import com.ilustris.sagai.features.narrative.data.model.LocationCheckpoint
import kotlinx.coroutines.flow.Flow

interface WorldLocationUseCase {
    fun getBySaga(sagaId: Int): Flow<List<WorldLocation>>

    suspend fun getRootForSaga(sagaId: Int): WorldLocation?

    suspend fun findOrCreate(
        sagaId: Int,
        name: String,
        history: String? = null,
        parentName: String? = null,
        emojiTag: String? = null,
        originChapterId: Int? = null,
    ): WorldLocation

    /** Resolves an AI-authored checkpoint into a persisted [LocationCheckpoint], creating the location if it's new. */
    suspend fun resolveCheckpoint(
        sagaId: Int,
        generated: GeneratedLocationCheckpoint,
        originChapterId: Int? = null,
    ): LocationCheckpoint

    suspend fun recordVisit(
        locationId: Int,
        chapterId: Int? = null,
        timelineId: Int? = null,
    )
}
