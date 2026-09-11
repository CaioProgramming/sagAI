package com.ilustris.sagai.features.geography.data.usecase

import com.ilustris.sagai.features.geography.data.model.WorldLocation
import com.ilustris.sagai.features.geography.data.model.WorldLocationVisit
import com.ilustris.sagai.features.geography.data.repository.WorldLocationRepository
import com.ilustris.sagai.features.narrative.data.model.GeneratedLocationCheckpoint
import com.ilustris.sagai.features.narrative.data.model.LocationCheckpoint
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorldLocationUseCaseImpl
    @Inject
    constructor(
        private val worldLocationRepository: WorldLocationRepository,
    ) : WorldLocationUseCase {
        override fun getBySaga(sagaId: Int): Flow<List<WorldLocation>> = worldLocationRepository.getBySaga(sagaId)

        override suspend fun getRootForSaga(sagaId: Int): WorldLocation? = worldLocationRepository.getRootForSaga(sagaId)

        override suspend fun findOrCreate(
            sagaId: Int,
            name: String,
            history: String?,
            parentName: String?,
            emojiTag: String?,
            originChapterId: Int?,
        ): WorldLocation {
            worldLocationRepository.findByName(sagaId, name)?.let { return it }

            val parentId =
                parentName
                    ?.takeIf { it.isNotBlank() && !it.equals(name, ignoreCase = true) }
                    ?.let { resolvedParentName ->
                        worldLocationRepository.findByName(sagaId, resolvedParentName)?.id
                            ?: worldLocationRepository
                                .insert(WorldLocation(sagaId = sagaId, name = resolvedParentName))
                                .id
                    }

            return worldLocationRepository.insert(
                WorldLocation(
                    sagaId = sagaId,
                    name = name,
                    history = history.orEmpty(),
                    parentLocationId = parentId,
                    emojiTag = emojiTag,
                    originChapterId = originChapterId,
                ),
            )
        }

        override suspend fun resolveCheckpoint(
            sagaId: Int,
            generated: GeneratedLocationCheckpoint,
            originChapterId: Int?,
        ): LocationCheckpoint {
            if (generated.locationName.isBlank()) {
                return LocationCheckpoint(
                    timeOfDay = generated.timeOfDay,
                    elapsedNote = generated.elapsedNote,
                    timeGap = generated.timeGap,
                )
            }
            val location =
                findOrCreate(
                    sagaId = sagaId,
                    name = generated.locationName,
                    history = generated.newLocationHistory,
                    originChapterId = originChapterId,
                )
            return LocationCheckpoint(
                locationId = location.id,
                locationName = location.name,
                timeOfDay = generated.timeOfDay,
                elapsedNote = generated.elapsedNote,
                timeGap = generated.timeGap,
            )
        }

        override suspend fun recordVisit(
            locationId: Int,
            chapterId: Int?,
            timelineId: Int?,
        ) {
            worldLocationRepository.recordVisit(
                WorldLocationVisit(locationId = locationId, chapterId = chapterId, timelineId = timelineId),
            )
        }
    }
