package com.ilustris.sagai.features.saga.chat.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.file.backup.filterBackups
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.repository.ActRepository
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepository
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepository
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.repository.WikiRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

interface SagaBackupService {
    suspend fun restoreContent(sagaContent: RestorableSaga): RequestResult<SagaContent>

    suspend fun filterValidSagas(manifests: List<RestorableSaga>): RequestResult<List<RestorableSaga>>

    suspend fun backupSaga(sagaId: Int): RequestResult<Uri>

    suspend fun exportSaga(
        sagaId: Int,
        destinationUri: Uri,
    ): RequestResult<Unit>
}

class SagaBackupServiceImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val characterRepository: CharacterRepository,
        private val actRepository: ActRepository,
        private val chapterRepository: ChapterRepository,
        private val timelineRepository: TimelineRepository,
        private val wikiRepository: WikiRepository,
        private val relationRepository: CharacterRelationRepository,
        private val characterEventRepository: CharacterEventRepository,
        private val messageRepository: MessageRepository,
        private val reactionRepository: ReactionRepository,
        private val backupService: BackupService,
        private val fileHelper: FileHelper,
    ) : SagaBackupService {
        override suspend fun restoreContent(sagaContent: RestorableSaga) =
            executeRequest {
                val zipFile = sagaContent.manifest.zipFileName.toUri()

                val imageResources = backupService.unzipImageBytes(zipFile)

                val sagaContent =
                    backupService
                        .unzipAndParseSaga(zipFile)!!

                recoverOperation(
                    sagaContent,
                    imageResources,
                )

                sagaContent
            }

        private suspend fun recoverOperation(
            sagaContent: SagaContent,
            imageResources: List<Pair<String, ByteArray>>,
        ) {
            Log.d(javaClass.simpleName, "Recovering saga: ${sagaContent.data.title}")
            val newSaga = sagaContent.copy(data = recoverSaga(sagaContent.data).second)

            Log.d(javaClass.simpleName, "Recovering images[${imageResources.size}]...")
            val savedImages = backupService.saveExtractedImages(newSaga.data.id, imageResources)

            sagaRepository.updateChat(
                newSaga.data.copy(
                    icon =
                        savedImages.find { it.first == sagaContent.data.icon }?.second
                            ?: emptyString(),
                ),
            )

            val actsPairs = saveActs(newSaga.data.id, newSaga.acts.map { it.data })

            val chapterPairs =
                saveChapters(
                    sagaContent.flatChapters().map {
                        val coverImage =
                            if (it.data.coverImage.isEmpty()) {
                                it.data.coverImage
                            } else {
                                savedImages
                                    .find { (relativePath, _) ->

                                        it.data.coverImage == relativePath
                                    }?.second ?: emptyString()
                            }

                        it.data.copy(coverImage = coverImage)
                    },
                    actsPairs,
                )

            val eventPairs = saveEvents(sagaContent.flatEvents().map { it.data }, chapterPairs)

            val newCharacters =
                recoverCharacters(newSaga.data.id, sagaContent.characters, eventPairs, savedImages)

            val messagePairs =
                saveMessages(sagaContent.flatMessages().map { it.message }, eventPairs, newCharacters)

            val reactionPairs =
                recoverReactions(
                    sagaContent.flatMessages().map { it.reactions.map { it.data } }.flatten(),
                    messagePairs,
                    newCharacters,
                )

            val wikis = recoverWiki(newSaga.data.id, eventPairs, sagaContent.wikis)

            val charactersEventsPairs =
                recoverCharactersEvents(
                    sagaContent.characters
                        .map { it.events.map { it.event } }
                        .flatten(),
                    newCharacters,
                    eventPairs,
                )

            val charactersRelationsPairs =
                recoverCharactersRelations(
                    newSaga.data.id,
                    sagaContent.characters.map { it.relationships }.flatten(),
                    newCharacters,
                )
            val relationsEventsPairs =
                recoverCharactersRelationEvents(
                    sagaContent.characters
                        .map {
                            it.relationships.map { it.relationshipEvents }.flatten()
                        }.flatten(),
                    eventPairs,
                    charactersRelationsPairs,
                )

            chapterPairs
                .filter {
                    sagaContent.characters.any { character ->
                        it.first.featuredCharacters.any { featured -> featured == character.data.id }
                    }
                }.forEach {
                    val mappedFeaturedCharacters =
                        it.first.featuredCharacters.mapNotNull { featured ->
                            newCharacters.find { character -> character.first.id == featured }?.second?.id
                        }
                    chapterRepository.updateChapter(
                        it.second.copy(
                            featuredCharacters = mappedFeaturedCharacters,
                        ),
                    )
                }

            val backupLog =
                buildString {
                    appendLine("SAGA RESTORED")
                    appendLine(newSaga.data.toJsonFormat())
                    appendLine("${actsPairs.size} acts")
                    appendLine("${chapterPairs.size} chapters")
                    appendLine("${eventPairs.size} events")
                    appendLine("${messagePairs.size} messages")
                    appendLine("${reactionPairs.size} reactions")
                    appendLine("")
                    appendLine("Restored ${newCharacters.size} characters")
                    appendLine(" ${wikis.size} wikis")
                    appendLine("${messagePairs.size} messages")
                    appendLine("${charactersEventsPairs.size} character events")
                    appendLine("${charactersRelationsPairs.size} character relations")
                    appendLine("${relationsEventsPairs.size} relation events")
                }

            Log.i(javaClass.simpleName, "recoverOperation: Backup complete -> $backupLog")
        }

        private suspend fun recoverCharactersRelations(
            sagaId: Int,
            relations: List<RelationshipContent>,
            newCharacters: List<Pair<Character, Character>>,
        ) = relations
            .map {
                val characterOne =
                    newCharacters.find { pair -> pair.first.id == it.characterOne.id }
                        ?: return@map null
                val characterTwo =
                    newCharacters.find { pair -> pair.first.id == it.characterTwo.id }
                        ?: return@map null

                it.data to
                    relationRepository.insertRelation(
                        it.data.copy(
                            sagaId = sagaId,
                            characterOneId = characterOne.second.id,
                            characterTwoId = characterTwo.second.id,
                            id = 0,
                        ),
                    )
            }.filterNotNull()

        private suspend fun recoverCharactersRelationEvents(
            events: List<RelationshipUpdateEvent>,
            timelinePairs: List<Pair<Timeline, Timeline>>,
            relationshipPair: List<Pair<CharacterRelation, CharacterRelation>>,
        ) = events.map {
            val timeline =
                timelinePairs.find { timeline -> timeline.first.id == it.timelineId } ?: return@map null
            val relationship =
                relationshipPair.find { pair -> pair.first.id == it.relationId } ?: return@map null

            relationRepository.addEventToRelation(
                it.copy(
                    timelineId = timeline.second.id,
                    relationId = relationship.second.id,
                    id = 0,
                ),
            )
        }

        override suspend fun filterValidSagas(manifests: List<RestorableSaga>) =
            executeRequest {
                val sagas = sagaRepository.getChats().first()

                manifests.filterBackups(sagas.map { it.data })
            }

        override suspend fun backupSaga(sagaId: Int): RequestResult<Uri> =
            executeRequest {
                val saga = sagaRepository.getSagaById(sagaId).first() ?: error("Saga not found")
                backupService.backupSaga(saga).getSuccess() ?: error("Backup failed")
            }

        override suspend fun exportSaga(
            sagaId: Int,
            destinationUri: Uri,
        ): RequestResult<Unit> =
            executeRequest {
                val saga = sagaRepository.getSagaById(sagaId).first() ?: error("Saga not found")
                backupService.writeExportToUri(saga, destinationUri).getSuccess()
                    ?: error("Export failed")
            }

        private suspend fun recoverSaga(saga: Saga): Pair<Saga, Saga> =
            saga to
                sagaRepository.saveChat(
                    saga.copy(
                        id = 0,
                        createdAt = System.currentTimeMillis(),
                        mainCharacterId = null,
                    ),
                )

        private suspend fun recoverReactions(
            reactions: List<Reaction>,
            messagesPair: List<Pair<Message, Message>>,
            charactersPair: List<Pair<Character, Character>>,
        ) = reactions.map {
            val message =
                messagesPair.find { message -> message.first.id == it.messageId } ?: return@map null
            val character =
                charactersPair.find { character -> character.first.id == it.characterId }
                    ?: return@map null

            it to
                reactionRepository.saveReaction(
                    it.copy(
                        messageId = message.second.id,
                        characterId = character.second.id,
                        id = 0,
                    ),
                )
        }

        private suspend fun recoverCharacters(
            sagaId: Int,
            characters: List<CharacterContent>,
            eventPairs: List<Pair<Timeline, Timeline>>,
            imageResources: List<Pair<String, String>>,
        ) = characters.map {
            val timeline = eventPairs.find { eventPair -> eventPair.first.id == it.data.firstSceneId }
            it.data to
                characterRepository.insertCharacter(
                    it.data.copy(
                        sagaId = sagaId,
                        firstSceneId = timeline?.second?.id,
                        image =
                            if (it.data.image.isEmpty()) {
                                it.data.image
                            } else {
                                imageResources
                                    .find { (relativePath, _) ->
                                        relativePath ==
                                            it.data.image
                                    }?.second
                                    ?: emptyString()
                            },
                        id = 0,
                    ),
                )
        }

        private suspend fun recoverCharactersEvents(
            events: List<CharacterEvent>,
            charactersPair: List<Pair<Character, Character>>,
            timelines: List<Pair<Timeline, Timeline>>,
        ) = events
            .map {
                val timeLine =
                    timelines.find { timeline -> timeline.first.id == it.gameTimelineId }
                        ?: return@map null
                val character =
                    charactersPair.find { pair -> pair.first.id == it.characterId } ?: return@map null

                it to
                    characterEventRepository.insertCharacterEvent(
                        it.copy(
                            gameTimelineId = timeLine.second.id,
                            characterId = character.second.id,
                            id = 0,
                        ),
                    )
            }.filterNotNull()

        private suspend fun recoverWiki(
            sagaId: Int,
            timeLinePair: List<Pair<Timeline, Timeline>>,
            wikis: List<Wiki>,
        ) = wikis.map {
            val timeLine = timeLinePair.find { timeline -> timeline.first.id == it.timelineId }
            it to
                wikiRepository.insertWiki(
                    it.copy(
                        sagaId = sagaId,
                        timelineId = timeLine?.second?.id,
                        id = 0,
                    ),
                )
        }

        private suspend fun saveActs(
            sagaId: Int,
            acts: List<Act>,
        ) = acts.map { it to actRepository.saveAct(it.copy(sagaId = sagaId, id = 0)) }

        private suspend fun saveChapters(
            chapters: List<Chapter>,
            actsPairs: List<Pair<Act, Act>>,
        ) = chapters
            .map {
                val act = actsPairs.find { pair -> pair.first.id == it.actId } ?: return@map null
                it to chapterRepository.saveChapter(it.copy(actId = act.second.id, id = 0))
            }.filterNotNull()

        private suspend fun saveEvents(
            events: List<Timeline>,
            chapterPairs: List<Pair<Chapter, Chapter>>,
        ) = events
            .map {
                val chapter =
                    chapterPairs.find { pair -> pair.first.id == it.chapterId } ?: return@map null
                it to
                    timelineRepository.saveTimeline(
                        it.copy(
                            chapterId = chapter.second.id,
                            id = 0,
                        ),
                    )
            }.filterNotNull()

        private suspend fun saveMessages(
            messages: List<Message>,
            eventPairs: List<Pair<Timeline, Timeline>>,
            charactersPair: List<Pair<Character, Character>>,
        ) = messages
            .map {
                val event =
                    eventPairs.find { pair -> pair.first.id == it.timelineId } ?: return@map null
                val character = charactersPair.find { pair -> pair.first.id == it.characterId }
                it to
                    messageRepository.saveMessage(
                        it.copy(
                            timelineId = event.second.id,
                            characterId = character?.second?.id,
                            id = 0,
                        ),
                    )
            }.filterNotNull()
    }
