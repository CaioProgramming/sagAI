package com.ilustris.sagai.features.characters.relations.domain.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline

interface CharacterRelationUseCase {
    suspend fun generateCharacterRelation(
        timeline: Timeline,
        saga: SagaContent,
    ): RequestResult<Exception, Unit>
}
