package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.domain.usecase.WikiUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

const val LORE_UPDATE_THRESHOLD = 20

class SagaContentManagerImpl
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val characterUseCase: CharacterUseCase,
        private val chapterUseCase: ChapterUseCase,
        private val wikiUseCase: WikiUseCase,
        private val timelineUseCase: TimelineUseCase,
    ) : SagaContentManager {
        override val content = MutableStateFlow<SagaContent?>(null)

        override suspend fun loadSaga(sagaId: String) =
            sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect { saga ->
                content.value = saga
            }

        override suspend fun createNewChapter(
            messageReference: Int,
            messageList: List<MessageContent>,
        ): Chapter? {
            val saga = content.value!!
            return try {
                chapterUseCase
                    .generateChapter(
                        saga.data,
                        messageReference,
                        messageList.map { it.joinMessage() },
                        saga.chapters,
                        saga.characters,
                    ).success.value
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override suspend fun updateChapter(chapter: Chapter) {
            chapterUseCase.updateChapter(chapter)
        }

        override suspend fun updateLore(
            reference: Message,
            messageSubList: List<MessageContent>,
        ): LoreGen? {
            return try {
                val currentSaga = content.value!!
                val newLore =
                    sagaHistoryUseCase
                        .generateLore(
                            currentSaga,
                            reference.id,
                            messageSubList.map { it.joinMessage().formatToString() },
                        ).success.value

                timelineUseCase.saveTimeline(
                    newLore.timeLine.copy(
                        sagaId = currentSaga.data.id,
                        messageReference = reference.id,
                    ),
                )

                updateWikis(newLore, currentSaga)
                updateCharacters(newLore, currentSaga)

                newLore
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        private suspend fun updateCharacters(
            newLore: LoreGen,
            currentSaga: SagaContent,
        ) {
            newLore.updatedCharacters.forEach { loreCharacter ->
                currentSaga.characters
                    .find { character ->
                        character.name.contentEquals(loreCharacter.name, true) ||
                            character.id == loreCharacter.id
                    }?.let {
                        characterUseCase.updateCharacter(
                            it.copy(
                                name = loreCharacter.name,
                                backstory = loreCharacter.backstory,
                                status = loreCharacter.status,
                            ),
                        )
                    }
            }
        }

        private suspend fun updateWikis(
            newLore: LoreGen,
            currentSaga: SagaContent,
        ) {
            newLore.newEntries.forEach {
                val savedWiki =
                    currentSaga.wikis.find { wiki ->
                        wiki.title.contentEquals(it.title, true) ||
                            wiki.id == it.id
                    }
                savedWiki?.let { actualWiki ->
                    wikiUseCase.updateWiki(
                        Wiki(
                            id = actualWiki.id,
                            sagaId = currentSaga.data.id,
                            content = it.content,
                            title = it.title,
                        ),
                    )
                } ?: run {
                    wikiUseCase.saveWiki(it)
                }
            }
        }

        override suspend fun generateCharacter(message: Message): Character? {
            return try {
                characterUseCase
                    .generateCharacter(
                        sagaData = content.value!!.data,
                        description = message.text,
                    ).success.value
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
