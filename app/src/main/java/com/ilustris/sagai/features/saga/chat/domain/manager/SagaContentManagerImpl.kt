package com.ilustris.sagai.features.saga.chat.domain.manager

import android.util.Log
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
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
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.domain.usecase.WikiUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.collections.emptyList

const val LORE_UPDATE_THRESHOLD = 20
const val CHAPTER_UPDATE_THRESHOLD = 5

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

        private fun lastEvents(): List<Timeline> {
            val saga = content.value ?: return emptyList()
            val lastChapter = saga.chapters.maxByOrNull { it.id }
            val events = saga.timelines
            val eventReference = lastChapter?.eventReference?.let { referenceId -> saga.timelines.find { it.id == referenceId } }
            return eventReference?.let {
                val referenceIndex = events.indexOf(it)
                events.subList(referenceIndex, events.size).takeLast(5)
            } ?: events
        }

        override suspend fun createNewChapter(): Chapter? {
            var chapterOperation: Chapter? = null
            return try {
                val saga = content.value!!
                val lastEvents = lastEvents()

                val genChapter =
                    chapterUseCase
                        .generateChapter(
                            saga,
                            lastAddedEvents = lastEvents,
                        ).success.value

                updateWikis(lastEvents)
                val newChapter =
                    chapterUseCase
                        .saveChapter(
                            genChapter.chapter
                                .copy(
                                    sagaId = saga.data.id,
                                    eventReference = lastEvents().last().id,
                                    messageReference = 0,
                                ),
                        )
                chapterOperation = newChapter
                val featuredCharacters =
                    genChapter.featuredCharacters.mapNotNull { name ->
                        saga.characters.find { it.name.equals(name, true) }
                    }
                chapterUseCase
                    .generateChapterCover(
                        newChapter,
                        saga.data,
                        featuredCharacters,
                    ).success.value
            } catch (e: Exception) {
                e.printStackTrace()
                return chapterOperation
            }
        }

        override suspend fun updateChapter(chapter: Chapter) {
            chapterUseCase.updateChapter(chapter)
        }

        override suspend fun updateLore(
            reference: Message,
            messageSubList: List<MessageContent>,
        ) = try {
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

            updateCharacters(newLore, currentSaga)

            newLore.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }

        override suspend fun checkForChapter(): RequestResult<Exception, Chapter> {
            val lastEvents = lastEvents()
            return try {
                if (lastEvents.size >= CHAPTER_UPDATE_THRESHOLD) {
                    createNewChapter()!!.asSuccess()
                } else {
                    RequestResult.Error(Exception("Not enough events to create a new chapter"))
                }
            } catch (e: Exception) {
                e.asError()
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

        private suspend fun updateWikis(events: List<Timeline>) {
            val currentSaga = content.value ?: return
            val wikis = wikiUseCase.generateWiki(currentSaga, events)
            wikis.forEach {
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
            if (wikis.isEmpty()) {
                Log.w(javaClass.simpleName, "updateWikis: No wiki updates for the ${events.size} events.")
            }
        }

        override suspend fun generateCharacter(message: Message): Character? {
            return try {
                characterUseCase
                    .generateCharacter(
                        sagaContent = content.value!!,
                        description = message.text,
                    ).success.value
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
