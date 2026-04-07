package com.ilustris.sagai.features.characters.relations.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline

interface CharacterRelationUseCase {
    suspend fun generateCharacterRelation(
        timeline: Timeline,
        saga: SagaContent,
    ): RequestResult<Unit>

    suspend fun updateRelation(
        saga: SagaContent,
        timelineId: Int,
        firstCharacterName: String,
        secondCharacterName: String,
        title: String,
        description: String,
        emoji: String,
    ): RequestResult<Unit>
}
