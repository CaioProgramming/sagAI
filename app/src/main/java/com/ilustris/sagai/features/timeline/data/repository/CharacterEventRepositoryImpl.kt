package com.ilustris.sagai.features.timeline.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.data.model.CharacterEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterEventRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : CharacterEventRepository {
        override suspend fun insertCharacterEvent(characterEvent: CharacterEvent) =
            characterEvent.copy(
                id = database.characterEventDao().insertCharacterEvent(characterEvent).toInt(),
            )

        override suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>) =
            database.characterEventDao().insertCharacterEvents(characterEvents)
    }
