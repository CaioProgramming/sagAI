package com.ilustris.sagai.features.saga.chat.repository

import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.repository.ActRepository
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepository
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
import kotlin.collections.forEach

class SagaBackupService
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
    ) {
        suspend fun restoreSaga(sagaContent: SagaContent) =
            executeRequest {
                recoverSaga(sagaContent.data)
                saveActs(sagaContent.data.id, sagaContent.acts.map { it.data })
                saveChapters(sagaContent.flatChapters().map { it.data })
                saveEvents(sagaContent.flatEvents().map { it.data })

                recoverCharacters(sagaContent.data.id, sagaContent.characters)
                recoverWiki(sagaContent.data.id, sagaContent.wikis)
                recoverReactions(sagaContent.flatMessages().map { it.reactions.map { it.data } }.flatten())

                sagaContent
            }

        private suspend fun recoverSaga(saga: Saga) {
            sagaRepository.saveChat(saga)
        }

        private suspend fun recoverReactions(reactions: List<Reaction>) {
            reactions.forEach {
                reactionRepository.saveReaction(it)
            }
        }

        private suspend fun recoverCharacters(
            sagaId: Int,
            characters: List<CharacterContent>,
        ) {
            characters.forEach {
                characterRepository.insertCharacter(it.data.copy(sagaId = sagaId))
            }

            characters.forEach {
                it.events.forEach { event ->
                    characterEventRepository.insertCharacterEvent(event.event)
                }

                relationRepository.insertRelations(it.relationships.map { it.data })

                it.relationships.forEach { relationship ->
                    relationship.relationshipEvents.forEach { event ->
                        relationRepository.addEventToRelation(event)
                    }
                }
            }
        }

        private suspend fun recoverWiki(
            sagaId: Int,
            wikis: List<Wiki>,
        ) {
            wikis.forEach {
                wikiRepository.insertWiki(it.copy(sagaId = sagaId))
            }
        }

        private suspend fun saveActs(
            sagaId: Int,
            acts: List<Act>,
        ) {
            acts.forEach {
                actRepository.saveAct(it)
            }
        }

        private suspend fun saveChapters(chapters: List<Chapter>) {
            chapters.forEach {
                chapterRepository.saveChapter(it)
            }
        }

        private suspend fun saveEvents(events: List<Timeline>) {
            events.forEach {
                timelineRepository.saveTimeline(it)
            }
        }

        private suspend fun saveMessages(messages: List<Message>) {
            messages.forEach {
                messageRepository.saveMessage(it)
            }
        }
    }
