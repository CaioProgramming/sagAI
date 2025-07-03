package com.ilustris.sagai.features.saga.chat.domain.manager

import android.util.Log
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.narrative.ActDirectives
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.domain.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.emptyList

class SagaContentManagerImpl
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val characterUseCase: CharacterUseCase,
        private val chapterUseCase: ChapterUseCase,
        private val wikiUseCase: WikiUseCase,
        private val timelineUseCase: TimelineUseCase,
        private val actUseCase: ActUseCase,
    ) : SagaContentManager {
        override val content = MutableStateFlow<SagaContent?>(null)

        override suspend fun createAct(): RequestResult<Exception, Act> =
            try {
                val saga = content.value ?: throw Exception("Saga Not found.")
                val actTransaction = actUseCase.saveAct(Act(sagaId = saga.data.id))

                sagaHistoryUseCase.updateSaga(saga.data.copy(currentActId = actTransaction.id))

                actTransaction.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun loadSaga(sagaId: String) =
            sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect { saga ->
                content.value = saga
            }

        private fun lastEvents(): List<Timeline> {
            val saga = content.value ?: return emptyList()
            val lastChapter = saga.chapters.maxByOrNull { it.id }
            val events = saga.timelines
            val eventReference =
                lastChapter?.eventReference?.let { referenceId -> saga.timelines.find { it.id == referenceId } }
            return eventReference?.let {
                val referenceIndex = events.indexOf(it)
                events.subList(referenceIndex, events.size).takeLast(5)
            } ?: events
        }

        override suspend fun createNewChapter(): Chapter? {
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
                                    actId =
                                        content.value
                                            ?.currentActInfo
                                            ?.act
                                            ?.id,
                                ),
                        )
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
                return null
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

            val newTimeLine =
                timelineUseCase.saveTimeline(
                    newLore.timeLine.copy(
                        sagaId = currentSaga.data.id,
                        messageReference = reference.id,
                    ),
                )

            updateCharacters(newLore, currentSaga)

            newTimeLine.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }

        override suspend fun checkForChapter(): RequestResult<Exception, Chapter> {
            val lastEvents = lastEvents()
            return try {
                if (lastEvents.size >= UpdateRules.CHAPTER_UPDATE_LIMIT) {
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
                Log.w(
                    javaClass.simpleName,
                    "updateWikis: No wiki updates for the ${events.size} events.",
                )
            }
        }

        override suspend fun generateCharacter(message: Message) =
            try {
                characterUseCase
                    .generateCharacter(
                        sagaContent = content.value!!,
                        description = message.text,
                    )
            } catch (e: Exception) {
                e.asError()
            }

        override fun getDirective(): String {
            val currentActs = content.value?.acts
            return when (currentActs?.size) {
                1 -> ActDirectives.FIRST_ACT_DIRECTIVES
                2 -> ActDirectives.SECOND_ACT_DIRECTIVES
                3 -> ActDirectives.THIRD_ACT_DIRECTIVES
                else -> ActDirectives.FIRST_ACT_DIRECTIVES
            }
        }

    override suspend fun updateAct(): RequestResult<Exception, Act> {
        val currentSaga = content.value!!
        val currentAct = currentSaga.currentActInfo!!.act
        val genAct = actUseCase.generateAct(currentSaga).success.value
        return actUseCase.updateAct(
            currentAct.copy(
                title = genAct.title,
                content = genAct.content,
            )
        ).asSuccess()
    }
}
