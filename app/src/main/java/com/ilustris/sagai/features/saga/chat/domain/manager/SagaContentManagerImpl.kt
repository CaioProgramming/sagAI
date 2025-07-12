package com.ilustris.sagai.features.saga.chat.domain.manager

import android.icu.util.Calendar
import android.util.Log
import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.ai.prompts.CharacterGuidelines
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.narrative.ActDirectives
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.domain.usecase.ActUseCase
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.domain.usecase.WikiUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

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
        override val contentUpdateMessages: MutableSharedFlow<Message> =
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        private val isProcessingNarrative = AtomicBoolean(false)
        override val endTrigger =
            MutableSharedFlow<Boolean>(
                0,
                1,
                BufferOverflow.DROP_OLDEST,
            )

        override suspend fun loadSaga(sagaId: String) {
            Log.d(javaClass.simpleName, "Loading saga: $sagaId")
            try {
                sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect { saga ->
                    Log.d(
                        javaClass.simpleName,
                        "Saga flow updated for saga -> $sagaId \n ${saga.toJsonFormat()}",
                    )
                    content.value = saga
                    checkNarrativeProgression(saga)
                }
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error loading saga $sagaId", e)
                content.value = null
            }
        }

        private fun lastEvents(saga: SagaContent): List<Timeline> {
            val lastChapter = saga.chapters.lastOrNull()
            val events =
                saga.timelines.sortedBy { it.createdAt }

            return lastChapter?.eventReference?.let { referenceId ->
                val referenceEventIndex = events.indexOfFirst { it.id == referenceId }
                if (referenceEventIndex != -1) {
                    events.subList(referenceEventIndex + 1, events.size)
                } else {
                    if (saga.chapters.isEmpty()) events else emptyList()
                }
            } ?: events
        }

        private suspend fun createNewChapter(
            currentSagaState: SagaContent,
            relevantEvents: List<Timeline>,
        ): RequestResult<Exception, Chapter> {
            Log.d(
                javaClass.simpleName,
                "internalCreateNewChapter called for saga: ${currentSagaState.data.id} with ${relevantEvents.size} relevant events.",
            )
            if (relevantEvents.isEmpty()) {
                Log.w(
                    javaClass.simpleName,
                    "Attempted to create chapter with no relevant events for saga ${currentSagaState.data.id}.",
                )
                return RequestResult.Error(IllegalArgumentException("Cannot create chapter with no relevant events."))
            }
            return try {
                val genChapter =
                    chapterUseCase
                        .generateChapter(
                            currentSagaState,
                            lastAddedEvents = relevantEvents,
                        ).success.value

                val newChapterData =
                    genChapter.chapter.copy(
                        sagaId = currentSagaState.data.id,
                        eventReference = relevantEvents.last().id,
                        messageReference = 0,
                        actId = currentSagaState.currentActInfo?.act?.id,
                    )
                val savedChapter = chapterUseCase.saveChapter(newChapterData)

                val featuredCharacters =
                    genChapter.featuredCharacters.mapNotNull { name ->
                        currentSagaState.characters.find { it.name.equals(name, true) }
                    }
                chapterUseCase.generateChapterCover(
                    savedChapter,
                    currentSagaState.data,
                    featuredCharacters,
                )
                Log.i(
                    javaClass.simpleName,
                    "New chapter created successfully: ${savedChapter.id} for saga: ${currentSagaState.data.id}",
                )
                contentUpdateMessages.emit(
                    Message(
                        text = "Capitulo '${savedChapter.title}' iniciado.",
                        senderType = SenderType.NEW_CHAPTER,
                        sagaId = content.value!!.data.id,
                        chapterId = savedChapter.id,
                    ),
                )
                savedChapter.asSuccess()
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error creating new chapter for saga ${currentSagaState.data.id}",
                    e,
                )
                e.asError()
            }
        }

        private suspend fun createAct() {
            try {
                val currentSaga = content.value!!

                val newAct = Act(sagaId = currentSaga.data.id, title = "", content = "")
                val savedAct = actUseCase.saveAct(newAct)
                sagaHistoryUseCase.updateSaga(currentSaga.data.copy(currentActId = savedAct.id))
                Log.i(
                    javaClass.simpleName,
                    "New act created successfully: ${savedAct.id} for saga: ${currentSaga.data.id}",
                )
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error creating act for saga ${content.value?.data?.id}",
                    e,
                )
            }
        }

        private suspend fun updateAct(): RequestResult<Exception, Act> =
            try {
                val saga = content.value!!
                Log.d(
                    javaClass.simpleName,
                    "updating act(${saga.currentActInfo?.act?.id})",
                )
                val currentAct = saga.currentActInfo!!
                val genAct = actUseCase.generateAct(saga).success.value
                val updatedActData =
                    currentAct.act.copy(
                        title = genAct.title,
                        content = genAct.content,
                    )
                val updateTransaction = actUseCase.updateAct(updatedActData)
                Log.i(javaClass.simpleName, "Act updated successfully: ${updateTransaction.id}")
                contentUpdateMessages.emit(
                    Message(
                        text = "Ato ${(saga.acts.indexOf(currentAct.act) + 1).toRoman()} finalizado.",
                        actId = currentAct.act.id,
                        senderType = SenderType.NEW_ACT,
                    ),
                )
                Log.i(javaClass.simpleName, "updateAct: Act ${updateTransaction.title} finished. reseting saga current act")
                sagaHistoryUseCase.updateSaga(saga.data.copy(currentActId = null))
                updateTransaction.asSuccess()
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error updating act.", e)
                e.asError()
            }

        private suspend fun checkNarrativeProgression(saga: SagaContent?) {
            if (saga == null) {
                Log.e(javaClass.simpleName, "checkNarrativeProgression: No saga founded to check progression")
                return
            }

            if (isProcessingNarrative.get()) {
                return
            }
            isProcessingNarrative.set(true)

            if (saga.data.isEnded) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Saga is already ended, skipping narrative progression.",
                )
                isProcessingNarrative.set(false)
                return
            }

            val currentAct = saga.currentActInfo
            val chapters = saga.chapters
            val events = saga.timelines

            if (saga.acts.isEmpty()) {
                Log.i(
                    javaClass.simpleName,
                    "No acts yet, creating first act.",
                )
                createAct()
                isProcessingNarrative.set(false)
                return
            }

            if (saga.currentActInfo == null) {
                Log.e(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Current act not found in saga.",
                )
                createAct()
                isProcessingNarrative.set(false)
                return
            }

            if (saga.acts.size == UpdateRules.ACT_UPDATE_LIMIT &&
                currentAct?.chapters?.size == UpdateRules.ACT_UPDATE_LIMIT &&
                lastEvents(saga).size == UpdateRules.CHAPTER_UPDATE_LIMIT
            ) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Act and Chapter limits reached, ending saga.",
                )
                endTrigger.emit(true)
                isProcessingNarrative.set(false)
                return
            }

            if (chapters.size == UpdateRules.ACT_UPDATE_LIMIT) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Chapter limit ${saga.currentActInfo.chapters.size} reached\nFinishing act(${saga.acts.size}.",
                )
                updateAct()
                    .onSuccessAsync {
                        isProcessingNarrative.set(false)
                    }.onFailure {
                        Log.e(javaClass.simpleName, "Error updating current act", it)
                        isProcessingNarrative.set(false)
                    }
                return
            }

            if (events.size >= UpdateRules.CHAPTER_UPDATE_LIMIT) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Event limit(${events.size}) reached, creating new chapter.",
                )
                createNewChapter(saga, lastEvents(saga))
                isProcessingNarrative.set(false)
                return
            }
        }

        override suspend fun updateLore(
            reference: Message,
            messageSubList: List<MessageContent>,
        ): RequestResult<Exception, Timeline> {
            val currentSagaState =
                content.value ?: return Exception("Saga not loaded for updateLore").asError()
            Log.d(
                javaClass.simpleName,
                "updateLore called for saga ${currentSagaState.data.id} with ${messageSubList.size} messages.",
            )

            return try {
                val newLore =
                    sagaHistoryUseCase
                        .generateLore(
                            currentSagaState,
                            reference.id,
                            messageSubList.map { it.joinMessage().formatToString() },
                        ).success.value

                val newTimeLine =
                    timelineUseCase.saveTimeline(
                        newLore.timeLine.copy(
                            sagaId = currentSagaState.data.id,
                            messageReference = reference.id,
                        ),
                    )
                Log.i(
                    javaClass.simpleName,
                    "New timeline event ${newTimeLine.id} saved for saga ${currentSagaState.data.id}",
                )
                updateCharacters(newLore, currentSagaState)
                updateWikis(lastEvents(currentSagaState))

                Log.i(
                    javaClass.simpleName,
                    "updateLore completed for saga ${currentSagaState.data.id}. Timeline event: ${newTimeLine.id}",
                )
                newTimeLine.asSuccess()
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error in updateLore for saga ${currentSagaState.data.id}",
                    e,
                )
                e.asError()
            }
        }

        private suspend fun updateCharacters(
            newLore: LoreGen,
            currentSaga: SagaContent,
        ) {
            Log.d(
                javaClass.simpleName,
                "Updating characters based on new lore for saga ${currentSaga.data.id}",
            )
            newLore.updatedCharacters.forEach { loreCharacter ->
                val characterToUpdate =
                    currentSaga.characters
                        .find { character ->
                            character.name.contentEquals(loreCharacter.name, true) ||
                                character.id == loreCharacter.id // Assuming loreCharacter might have an ID if it's an update
                        }
                if (characterToUpdate != null) {
                    Log.d(
                        javaClass.simpleName,
                        "Updating character: ${characterToUpdate.name} (ID: ${characterToUpdate.id})",
                    )
                    characterUseCase.updateCharacter(
                        loreCharacter.copy(
                            id = characterToUpdate.id, // Ensure correct ID from existing character
                            sagaId = currentSaga.data.id,
                        ),
                    )
                } else {
                    // If character from lore is not found, consider creating it
                    Log.i(
                        javaClass.simpleName,
                        "New character found in lore: ${loreCharacter.name}. Creating for saga ${currentSaga.data.id}.",
                    )
                    characterUseCase.generateCharacter(currentSaga, loreCharacter.toJsonFormat())
                }
            }
        }

        private suspend fun updateWikis(events: List<Timeline>) {
            val currentSaga =
                content.value ?: run {
                    Log.w(javaClass.simpleName, "updateWikis: Saga not loaded, cannot update wikis.")
                    return
                }
            Log.d(
                javaClass.simpleName,
                "Updating wikis based on ${events.size} events for saga ${currentSaga.data.id}",
            )

            if (events.isEmpty() && currentSaga.wikis.isEmpty()) {
                Log.d(
                    javaClass.simpleName,
                    "updateWikis: No events and no existing wikis for saga ${currentSaga.data.id}, skipping wiki generation.",
                )
                return
            }

            val wikisToUpdateOrAdd = wikiUseCase.generateWiki(currentSaga, events)
            wikisToUpdateOrAdd.forEach { generatedWiki ->
                val existingWiki =
                    currentSaga.wikis.find { wiki ->
                        wiki.title.contentEquals(generatedWiki.title, true)
                    }
                if (existingWiki != null) {
                    Log.d(
                        javaClass.simpleName,
                        "Updating existing wiki: ${existingWiki.title} (ID: ${existingWiki.id}) for saga ${currentSaga.data.id}",
                    )
                    wikiUseCase.updateWiki(
                        generatedWiki.copy(
                            id = existingWiki.id,
                            sagaId = currentSaga.data.id,
                        ),
                    )
                } else {
                    Log.d(
                        javaClass.simpleName,
                        "Saving new wiki: ${generatedWiki.title} for saga ${currentSaga.data.id}",
                    )
                    wikiUseCase.saveWiki(generatedWiki.copy(sagaId = currentSaga.data.id))
                }
            }
            if (wikisToUpdateOrAdd.isEmpty()) {
                Log.i(
                    javaClass.simpleName,
                    "updateWikis: No wiki updates generated for ${events.size} events in saga ${currentSaga.data.id}.",
                )
            }
        }

        override suspend fun generateCharacter(description: String) =
            try {
                val currentSaga =
                    content.value
                        ?: throw IllegalStateException("Saga not loaded for character generation")
                Log.d(
                    javaClass.simpleName,
                    "Generating character with description for saga ${currentSaga.data.id}",
                )
                characterUseCase.generateCharacter(
                    sagaContent = currentSaga,
                    description = description,
                )
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error generating character for saga ${content.value?.data?.id}",
                    e,
                )
                e.asError()
            }

        override suspend fun generateCharacterImage(character: Character): RequestResult<Exception, Character> =
            try {
                val currentSaga =
                    content.value
                        ?: throw IllegalStateException("Saga not loaded for character image generation")
                Log.d(
                    javaClass.simpleName,
                    "Generating image for character ${character.name} in saga ${currentSaga.data.id}",
                )
                val descriptionGen =
                    characterUseCase
                        .generateCharacterPrompt(
                            character = character,
                            guidelines =
                                CharacterGuidelines.imageDescriptionGuideLine(
                                    CharacterFraming.PORTRAIT,
                                    currentSaga.data.genre,
                                ),
                            genre = currentSaga.data.genre,
                        ).success.value
                characterUseCase.generateCharacterImage(character, descriptionGen, currentSaga.data)
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error generating character image for ${character.name} in saga ${content.value?.data?.id}",
                    e,
                )
                e.asError()
            }

        override suspend fun endSaga() {
            sagaHistoryUseCase.updateSaga(
                content.value!!.data.copy(
                    isEnded = true,
                    endedAt = Calendar.getInstance().timeInMillis,
                ),
            )
            endTrigger.emit(false)
        }

        override fun getDirective(): String {
            val currentSaga = content.value
            val actsCount = currentSaga?.acts?.size ?: 0
            Log.d(
                javaClass.simpleName,
                "Getting directive. Total acts count: $actsCount for saga ${currentSaga?.data?.id}",
            )
            return when (actsCount) {
                0, 1 -> ActDirectives.FIRST_ACT_DIRECTIVES
                2 -> ActDirectives.SECOND_ACT_DIRECTIVES
                3 -> ActDirectives.THIRD_ACT_DIRECTIVES
                else -> ActDirectives.FIRST_ACT_DIRECTIVES
            }
        }
    }
