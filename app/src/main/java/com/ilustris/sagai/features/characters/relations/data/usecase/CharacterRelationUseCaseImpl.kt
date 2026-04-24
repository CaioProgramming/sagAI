package com.ilustris.sagai.features.characters.relations.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationGenerationGen
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.timeline.data.model.Timeline
import timber.log.Timber
import javax.inject.Inject

class CharacterRelationUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val relationRepository: CharacterRelationRepository,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
    ) : CharacterRelationUseCase {
        override suspend fun generateCharacterRelation(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Unit> =
            executeRequest {
                val prompt =
                    CharacterPrompts.generateCharacterRelation(promptService, timeline, saga)
                val generatedRelationsData =
                    gemmaClient.generate<RelationGenerationGen>(prompt)!!

                val updatedRelations =
                    generatedRelationsData.relations.map { relationData ->
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
                            timelineId = timeline.id,
                            relationTitle = relationData.title,
                            relationDescription = relationData.description,
                            relationEmoji = relationData.relationEmoji,
                            firstCharacter = firstCharacter,
                            secondCharacter = secondCharacter,
                        )
                    }

                Timber.i("Generated relations: ${updatedRelations.filter { it.isSuccess }}")

                Timber.w("Failed relations: ${updatedRelations.filter { it.isFailure }}")
            }

        override suspend fun updateRelation(
            saga: SagaContent,
            timelineId: Int,
            firstCharacterName: String,
            secondCharacterName: String,
            title: String,
            description: String,
            emoji: String,
        ): RequestResult<Unit> =
            executeRequest {
                val firstChar = saga.characters.find { it.data.name.equals(firstCharacterName, true) }?.data
                val secondChar =
                    saga.characters.find { it.data.name.equals(secondCharacterName, true) }?.data
                processRelation(
                    saga,
                    timelineId,
                    title,
                    description,
                    emoji,
                    firstChar,
                    secondChar,
                ).getSuccess()!!
                Unit
            }

        private suspend fun processRelation(
            saga: SagaContent,
            timelineId: Int,
            relationTitle: String,
            relationDescription: String,
            relationEmoji: String,
            firstCharacter: Character?,
            secondCharacter: Character?,
        ) = executeRequest {
            checkNotNull(firstCharacter)
            checkNotNull(secondCharacter)
            if (firstCharacter.id == secondCharacter.id) {
                error("A character cannot have a relationship with themselves")
            }

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
                        emoji = relationEmoji,
                        description = relationDescription,
                        title = relationTitle,
                        sagaId = saga.data.id,
                    )
                relationRepository.insertRelationAndEvent(newCharacterRelation, timelineId)
            } else {
                val timelineContent = saga.findTimeline(timelineId)
                val relationAlreadyUpdatedAtTimeline =
                    timelineContent
                        ?.updatedRelationshipDetails
                        ?.find { it.data.id == existingRelationshipContent.data.id }

                if (relationAlreadyUpdatedAtTimeline != null) {
                    error(
                        "The relation between ${firstCharacter.name} and ${secondCharacter.name} has already been updated in this timeline.",
                    )
                }

                relationRepository.addEventToRelation(
                    relationId = existingRelationshipContent.data.id,
                    timelineId = timelineId,
                    title = relationTitle,
                    description = relationDescription,
                    emoji = relationEmoji,
                    timestamp = System.currentTimeMillis(),
                )

                existingRelationshipContent.data
            }
        }
    }
