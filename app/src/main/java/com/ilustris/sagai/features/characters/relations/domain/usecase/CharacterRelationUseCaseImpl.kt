package com.ilustris.sagai.features.characters.relations.domain.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepository
import com.ilustris.sagai.features.characters.relations.domain.data.RelationGeneration
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import javax.inject.Inject

class CharacterRelationUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val relationRepository: CharacterRelationRepository,
    ) : CharacterRelationUseCase {
        override suspend fun generateCharacterRelation(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Exception, Unit> =
            executeRequest {
                val prompt = CharacterPrompts.generateCharacterRelation(timeline, saga)
                val request = gemmaClient.generate<List<RelationGeneration>>(prompt)!!
                val relations =
                    request
                        .map {
                            val firstCharacter =
                                saga.characters
                                    .find { c ->
                                        c.data.name.equals(it.firstCharacter, ignoreCase = true)
                                    }?.data
                            val secondCharacter =
                                saga.characters
                                    .find { c ->
                                        c.data.name.equals(it.secondCharacter, ignoreCase = true)
                                    }?.data
                            if (firstCharacter?.id == secondCharacter?.id) {
                                throw IllegalArgumentException("Character cannot be related to itself")
                            }
                            if (firstCharacter == null || secondCharacter == null) {
                                Log.w(javaClass.simpleName, "generateCharacterRelation: Characters not found, skipping relation")
                                return@map null
                            }
                            CharacterRelation.create(
                                char1Id = firstCharacter.id,
                                char2Id = secondCharacter.id,
                                emoji = it.relationEmoji,
                                description = it.description,
                                title = it.title,
                                sagaId = saga.data.id,
                            )
                        }.filterNotNull()
                Log.i(javaClass.simpleName, "generateCharacterRelation: Saving ${relations.size} relations.")
                relationRepository.insertRelations(relations)
                Unit.asSuccess()
            }
    }
