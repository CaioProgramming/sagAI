package com.ilustris.sagai.features.characters.relations.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.rag.EmbeddingSourceType
import com.ilustris.sagai.core.ai.rag.SemanticIndexService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationGenerationGen
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
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
        private val semanticIndexService: SemanticIndexService,
    ) : CharacterRelationUseCase {
        override suspend fun generateCharacterRelation(
            timeline: Timeline,
            saga: SagaContent,
        ): RequestResult<Unit> =
            executeRequest {
                val prompt =
                    CharacterPrompts.generateCharacterRelation(promptService, timeline, saga)
                val generatedRelationsData =
                    gemmaClient.generate<RelationGenerationGen>(promptSplit = prompt)!!

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
                val firstChar = saga.findCharacter(firstCharacterName)?.data
                val secondChar =
                    saga.findCharacter(secondCharacterName)?.data
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
                relationRepository.insertRelationAndEvent(newCharacterRelation, timelineId).also { saved ->
                    indexRelationEvent(
                        sagaId = saga.data.id,
                        relationId = saved.id,
                        timelineId = timelineId,
                        firstCharacter = firstCharacter,
                        secondCharacter = secondCharacter,
                        title = saved.title,
                        description = saved.description,
                    )
                }
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
                indexRelationEvent(
                    sagaId = saga.data.id,
                    relationId = existingRelationshipContent.data.id,
                    timelineId = timelineId,
                    firstCharacter = firstCharacter,
                    secondCharacter = secondCharacter,
                    title = relationTitle,
                    description = relationDescription,
                )

                existingRelationshipContent.data
            }
        }

        /**
         * Keeps the RAG index in step with `relationship_update_events` — each update is its own
         * atomic fact (the relation's base row never changes after creation, only new events get
         * appended), so this indexes per-event rather than upserting a single "current state" key.
         * Retrieved via semantic search in [com.ilustris.sagai.core.ai.prompts.ChatPrompts.replyMessagePrompt].
         */
        private fun indexRelationEvent(
            sagaId: Int,
            relationId: Int,
            timelineId: Int,
            firstCharacter: Character,
            secondCharacter: Character,
            title: String,
            description: String,
        ) {
            semanticIndexService.index(
                sagaId = sagaId,
                sourceKey = "characterRelation:$relationId:$timelineId",
                sourceType = EmbeddingSourceType.CHARACTER_RELATION,
                text = "${firstCharacter.name} & ${secondCharacter.name} — $title\n$description",
            )
        }
    }
