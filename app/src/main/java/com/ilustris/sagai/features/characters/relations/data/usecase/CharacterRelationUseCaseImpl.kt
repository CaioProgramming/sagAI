package com.ilustris.sagai.features.characters.relations.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationGeneration
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepository
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
        ): RequestResult<Unit> =
            executeRequest {
                val prompt = CharacterPrompts.generateCharacterRelation(timeline, saga)
                val generatedRelationsData = gemmaClient.generate<List<RelationGeneration>>(prompt)!!

                val updatedRelations =
                    generatedRelationsData.map { relationData ->
                        val firstCharacter =
                            saga.characters
                                .find { c ->
                                    c.data.name.equals(relationData.firstCharacter, ignoreCase = true)
                                }?.data
                        val secondCharacter =
                            saga.characters
                                .find { c ->
                                    c.data.name.equals(relationData.secondCharacter, ignoreCase = true)
                                }?.data

                        processRelation(
                            saga = saga,
                            timeline = timeline,
                            relationData = relationData,
                            firstCharacter = firstCharacter,
                            secondCharacter = secondCharacter,
                        )
                    }

                Log.i(javaClass.simpleName, "Generated relations: ${updatedRelations.filter { it.isSuccess }}")

                Log.w(javaClass.simpleName, "Failed relations: ${updatedRelations.filter { it.isFailure }}")
            }

        private suspend fun processRelation(
            saga: SagaContent,
            timeline: Timeline,
            relationData: RelationGeneration,
            firstCharacter: Character?,
            secondCharacter: Character?,
        ) = executeRequest {
            checkNotNull(firstCharacter)
            checkNotNull(secondCharacter)
            assert(firstCharacter.id != secondCharacter.id)

            val existingRelationshipContent =
                saga.relationships.find { rc ->
                    (
                        rc.data.characterOneId == firstCharacter.id &&
                            rc.data.characterTwoId == secondCharacter.id
                    ) ||
                        (
                            rc.data.characterOneId == secondCharacter.id &&
                                rc.data.characterTwoId == firstCharacter.id
                        )
                }

            if (existingRelationshipContent == null) {
                val newCharacterRelation =
                    CharacterRelation.create(
                        char1Id = firstCharacter.id,
                        char2Id = secondCharacter.id,
                        emoji = relationData.relationEmoji,
                        description = relationData.description,
                        title = relationData.title,
                        sagaId = saga.data.id,
                    )
                relationRepository.insertRelationAndEvent(newCharacterRelation, timeline.id)
            } else {
                relationRepository.addEventToRelation(
                    relationId = existingRelationshipContent.data.id,
                    timelineId = timeline.id,
                    title = relationData.title,
                    description = relationData.description,
                    emoji = relationData.relationEmoji,
                    timestamp = System.currentTimeMillis(),
                )

                existingRelationshipContent.data
            }
        }
    }
