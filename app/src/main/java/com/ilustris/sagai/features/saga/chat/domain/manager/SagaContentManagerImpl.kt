package com.ilustris.sagai.features.saga.chat.domain.manager

import android.icu.util.Calendar
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.ai.prompts.CharacterGuidelines
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.narrative.ActDirectives
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.FileCacheService
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.domain.usecase.ActUseCase
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterGen
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
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
        private val fileCacheService: FileCacheService,
        private val remoteConfig: FirebaseRemoteConfig,
    ) : SagaContentManager {
        override val content = MutableStateFlow<SagaContent?>(null)
        override val endMessage = MutableStateFlow<String?>(null) // Mismatch with interface (SharedFlow), kept as StateFlow from existing code
        override val contentUpdateMessages: MutableSharedFlow<Message> =
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

        // This was likely from a previous attempt, it aligns with the interface now.
        override val ambientMusicFile = MutableStateFlow<File?>(null)
        private val isProcessingNarrative = AtomicBoolean(false)

        private var isDebugModeEnabled: Boolean = false
        private var isProcessing: Boolean = false

        private var progressionCounter = 0

        override fun setDebugMode(enabled: Boolean) {
            isDebugModeEnabled = enabled
            Log.i(javaClass.simpleName, "Debug mode ${if (enabled) "enabled" else "disabled"}")
        }

        override fun setProcessing(bool: Boolean) {
            isProcessing = bool
            Log.i(javaClass.simpleName, "Processing mode ${if (bool) "enabled" else "disabled"}")
        }

        override fun isInDebugMode(): Boolean = isDebugModeEnabled

        override suspend fun loadSaga(sagaId: String) {
            Log.d(javaClass.simpleName, "Loading saga: $sagaId")
            try {
                sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect { saga ->
                    Log.d(
                        javaClass.simpleName,
                        "Saga flow updated for saga -> $sagaId \n ${saga.toJsonFormat()}",
                    )
                    content.value = saga
                    if (saga?.messages?.isNotEmpty() == true &&
                        saga
                            .messages
                            .last()
                            .message
                            .senderType == SenderType.ACTION &&
                        isDebugModeEnabled
                    ) {
                        return@collect
                    }
                    checkNarrativeProgression(saga)

                    saga?.let { getAmbienceMusic(it) }
                }
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error loading saga $sagaId", e)
                content.value = null
            }
        }

        private suspend fun getAmbienceMusic(saga: SagaContent) {
            val genre = saga.data.genre
            val fileUrl = remoteConfig.getString(genre.ambientMusicConfigKey)

            if (fileUrl.isNullOrEmpty()) {
                Log.e(javaClass.simpleName, "getAmbienceMusic: Invalid URL for ${genre.name}")
                return
            }

            withContext(Dispatchers.IO) {
                val newMusicFile = fileCacheService.getFile(fileUrl)
                if (newMusicFile?.absolutePath != ambientMusicFile.value?.absolutePath) {
                    ambientMusicFile.emit(newMusicFile)
                } else if (newMusicFile == null && ambientMusicFile.value != null) {
                    ambientMusicFile.emit(null)
                }
            }
        }

        private suspend fun sendDebugMessage(message: String) {
            if (isDebugModeEnabled) {
                contentUpdateMessages.emit(
                    Message(
                        text = message,
                        senderType = SenderType.ACTION,
                    ),
                )
            } else {
                Log.d(javaClass.simpleName, "Debug message: $message")
            }
        }

        private fun lastEvents(saga: SagaContent): List<Timeline> {
            val events = saga.timelines.sortedBy { it.createdAt }

            if (saga.chapters.isNotEmpty()) {
                val referenceIndex = saga.chapters.last().eventReference
                val eventAfterChapter = events.findLast { it.id == referenceIndex }
                if (eventAfterChapter != null) {
                    return events.subList(events.indexOf(eventAfterChapter) + 1, events.size)
                }
                return emptyList()
            } else {
                return events
            }
        }

        private suspend fun createNewChapter(
            currentSagaState: SagaContent,
            relevantEvents: List<Timeline>,
        ): RequestResult<Exception, Chapter> {
            if (currentSagaState.currentActInfo == null) {
                return RequestResult.Error(IllegalArgumentException("Cannot create chapter without an act."))
            }
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
                    if (isDebugModeEnabled) {
                        Log.i(
                            javaClass.simpleName,
                            "[DEBUG MODE] Generating fake chapter data for saga ${currentSagaState.data.id}",
                        )
                        ChapterGen(
                            chapter =
                                Chapter(
                                    title = "Debug Chapter #${currentSagaState.chapters.size + 1}",
                                    overview = "This is a fake chapter generated in debug mode.",
                                    actId = currentSagaState.currentActInfo.act.id,
                                    eventReference = relevantEvents.lastOrNull()?.id ?: 0,
                                    sagaId = currentSagaState.data.id,
                                    createdAt = System.currentTimeMillis(),
                                    messageReference = 0,
                                    coverImage = "",
                                ),
                            featuredCharacters = emptyList(),
                        )
                    } else {
                        chapterUseCase
                            .generateChapter(
                                currentSagaState,
                                lastAddedEvents = relevantEvents,
                            ).success.value
                    }

                val newChapterData =
                    genChapter.chapter.copy(
                        sagaId = currentSagaState.data.id,
                        eventReference = relevantEvents.last().id,
                        actId = currentSagaState.currentActInfo.act.id,
                    )
                val savedChapter = chapterUseCase.saveChapter(newChapterData)

                if (isDebugModeEnabled.not()) {
                    val featuredCharacters =
                        genChapter.featuredCharacters.mapNotNull { name ->
                            currentSagaState.characters.find { it.name.equals(name, true) }
                        }
                    withContext(Dispatchers.IO) {
                        chapterUseCase.generateChapterCover(
                            savedChapter,
                            currentSagaState.data,
                            featuredCharacters,
                        )
                    }
                } else {
                    Log.i(
                        javaClass.simpleName,
                        "[DEBUG MODE] Skipped chapter cover generation for ${savedChapter.title}",
                    )
                }
                Log.i(
                    javaClass.simpleName,
                    "New chapter created successfully: $savedChapter\nwith act ${currentSagaState.currentActInfo.act.id} ",
                )
                contentUpdateMessages.emit(
                    Message(
                        text = "Novo capítulo criado: ${savedChapter.title}",
                        chapterId = savedChapter.id,
                        senderType = SenderType.NEW_CHAPTER,
                    ),
                )
                isProcessingNarrative.set(false)
                savedChapter.asSuccess()
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error creating new chapter for saga ${currentSagaState.data.id}",
                    e,
                )
                isProcessingNarrative.set(false)
                e.asError()
            }
        }

        private suspend fun createAct(currentSaga: SagaContent): RequestResult<Exception, Act> =
            try {
                val savedAct = actUseCase.saveAct(Act())
                Log.i(
                    javaClass.simpleName,
                    "New act created successfully: ${savedAct.id} for saga: ${currentSaga.data.id}",
                )

                if (isDebugModeEnabled) {
                    sendDebugMessage(
                        "Created act: ${savedAct.toJsonFormat()}",
                    )
                }
                sagaHistoryUseCase.updateSaga(
                    currentSaga.data.copy(currentActId = savedAct.id),
                )
                actUseCase.updateAct(
                    savedAct.copy(
                        sagaId = currentSaga.data.id,
                    ),
                )
                isProcessingNarrative.set(false)
                savedAct.asSuccess()
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error creating act for saga ${content.value?.data?.id}",
                    e,
                )
                e.asError()
            }

        private suspend fun updateAct(): RequestResult<Exception, Act> =
            try {
                val saga = content.value!!
                Log.d(
                    javaClass.simpleName,
                    "updating act(${saga.currentActInfo?.act?.id})",
                )
                val currentAct = saga.currentActInfo!!
                val genAct =
                    if (isDebugModeEnabled) {
                        Log.i(
                            javaClass.simpleName,
                            "[DEBUG MODE] Generating fake act update data for saga ${saga.data.id}",
                        )
                        Act(
                            id = currentAct.act.id,
                            title = "Updated Act ${saga.acts.size}",
                            content = "This act was updated in debug mode.",
                            sagaId = saga.data.id,
                        )
                    } else {
                        actUseCase.generateAct(saga).success.value
                    }
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
                sagaHistoryUseCase.updateSaga(
                    saga.data.copy(currentActId = null),
                )
                isProcessingNarrative.set(false)
                updateTransaction.asSuccess()
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error updating act.", e)
                isProcessingNarrative.set(false)
                e.asError()
            }

        private suspend fun checkNarrativeProgression(saga: SagaContent?) {
            Log.d(javaClass.simpleName, "Starting narrative progression check")
            progressionCounter++

            if (saga == null) {
                Log.e(
                    javaClass.simpleName,
                    "checkNarrativeProgression: No saga founded to check progression",
                )
                return
            }

            if (isProcessingNarrative.get() || isProcessing) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Narrative progression is already in progress, skipping.",
                )
                return
            }
            isProcessingNarrative.set(true)

            if (saga.mainCharacter == null && isDebugModeEnabled) {
                generateCharacter("Main Debug Character").onSuccessAsync { newCharacter ->
                    sagaHistoryUseCase.updateSaga(saga.data.copy(mainCharacterId = newCharacter.id))
                }
                isProcessingNarrative.set(false)
                return
            }
            val currentActInfo = saga.currentActInfo
            val chapters = currentActInfo?.chapters
            val acts = saga.acts
            val events = lastEvents(saga)

            val messageEventReference =
                if (events.isNotEmpty()) {
                    saga.messages.findLast { it.message.id == events.last().messageReference }
                } else {
                    saga.messages.firstOrNull()
                }
            val messageIndex = saga.messages.indexOf(messageEventReference)
            val messageReferencesSublist =
                saga.messages.subList(if (messageIndex == -1) 0 else messageIndex, saga.messages.size)
            if (saga.data.isEnded && saga.data.endMessage.isNotEmpty()) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Saga is already ended, skipping narrative progression.",
                )
                sendDebugMessage(
                    """
                     SagaEnded.
                    Narrative progression  #$progressionCounter completed, no limits reached.
                    acts: ${saga.acts.size} of ${UpdateRules.MAX_ACTS_LIMIT} per Saga.
                    chapters in current act(${saga.acts.size}): ${(chapters?.size) ?: 0} of ${UpdateRules.ACT_UPDATE_LIMIT} per Act.
                    events: ${events.size} of ${UpdateRules.CHAPTER_UPDATE_LIMIT} per Chapter.
                    messages since last event: ${messageReferencesSublist.size} of ${UpdateRules.LORE_UPDATE_LIMIT} per Event.
                    """.trimIndent(),
                )
                isProcessingNarrative.set(false)
                return
            }

            if (saga.data.isEnded) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Saga is already ended, but no end message was provided.",
                )
                if (saga.data.endMessage.isEmpty()) {
                    createEndingMessage(saga)
                }
                isProcessingNarrative.set(false)
                return
            }

            if (acts.isEmpty()) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: No acts found, creating one.",
                )

                createAct(saga)
                return
            }

            if (currentActInfo == null) {
                Log.i(
                    javaClass.simpleName,
                    "No act in progress ${saga.acts.size}.",
                )
                createAct(saga)
                isProcessingNarrative.set(false)
                return
            }

            if (saga.acts.size == UpdateRules.MAX_ACTS_LIMIT &&
                chapters?.size == UpdateRules.ACT_UPDATE_LIMIT
            ) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Act and Chapter limits reached, ending saga.",
                )
                updateAct()
                endSaga()
                isProcessingNarrative.set(false)

                return
            }

            if (chapters?.size == UpdateRules.ACT_UPDATE_LIMIT) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Chapter limit ${currentActInfo.chapters.size} reached for act ${currentActInfo.act.title}.\nFinishing act(${saga.acts.size}).",
                )
                updateAct()

                return
            }

            if (events.size >= UpdateRules.CHAPTER_UPDATE_LIMIT) {
                Log.i(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Event limit(${events.size}) reached, creating new chapter.",
                )
                createNewChapter(saga, lastEvents(saga))

                return
            }

            isProcessingNarrative.set(false)
            Log.i(
                javaClass.simpleName,
                "checkNarrativeProgression: Narrative progression completed, no limits reached.",
            )

            sendDebugMessage(
                """
                Narrative progression  #$progressionCounter completed, no limits reached.
                acts: ${saga.acts.size} of ${UpdateRules.MAX_ACTS_LIMIT} per Saga.
                chapters in current act(${saga.acts.size}): ${(chapters?.size) ?: 0} of ${UpdateRules.ACT_UPDATE_LIMIT} per Act.
                events: ${events.size} of ${UpdateRules.CHAPTER_UPDATE_LIMIT} per Chapter.
                messages since last event: ${messageReferencesSublist.size} of ${UpdateRules.LORE_UPDATE_LIMIT} per Event.
                """.trimIndent(),
            )
        }

        private suspend fun createEndingMessage(saga: SagaContent) {
            if (isDebugModeEnabled) {
                sendDebugMessage("Generating debug end message")
                val message = "Congratulations on completing this saga!"
                sagaHistoryUseCase.updateSaga(saga.data.copy(endMessage = message))
                endMessage.emit(message)
                return
            }
            sagaHistoryUseCase.generateEndMessage(saga).onSuccessAsync {
                sagaHistoryUseCase.updateSaga(
                    saga.data.copy(endMessage = it),
                )
                endMessage.emit(it)
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

            if (isDebugModeEnabled) {
                Log.i(
                    javaClass.simpleName,
                    "[DEBUG MODE] Generating fake lore for saga ${currentSagaState.data.id}",
                )
                val fakeTimeline =
                    Timeline(
                        title = "Fake Timeline Event",
                        content = "Debug event generated in debug mode.",
                        createdAt = System.currentTimeMillis(),
                        sagaId = currentSagaState.data.id,
                        messageReference = reference.id,
                    )
                timelineUseCase.saveTimeline(fakeTimeline)
                Log.i(javaClass.simpleName, "[DEBUG MODE] Saved fake timeline ${fakeTimeline.id}")
                return fakeTimeline.asSuccess()
            }

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
                withContext(Dispatchers.IO) {
                    updateCharacters(newLore, currentSagaState)
                }

                withContext(Dispatchers.IO) {
                    updateWikis(lastEvents(currentSagaState))
                }

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
            if (isDebugModeEnabled) {
                Log.i(javaClass.simpleName, "[DEBUG MODE] Skipping character updates.")
                return
            }
            newLore.updatedCharacters.forEach { loreCharacter ->
                val characterToUpdate =
                    currentSaga.characters
                        .find { character ->
                            character.name.contentEquals(loreCharacter.name, true) ||
                                character.id == loreCharacter.id
                        }
                if (characterToUpdate != null) {
                    Log.d(
                        javaClass.simpleName,
                        "Updating character: ${characterToUpdate.name} (ID: ${characterToUpdate.id})",
                    )
                    characterUseCase.updateCharacter(
                        loreCharacter.copy(
                            id = characterToUpdate.id,
                            sagaId = currentSaga.data.id,
                        ),
                    )
                } else {
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
            if (isDebugModeEnabled) {
                Log.i(javaClass.simpleName, "[DEBUG MODE] Skipping wiki updates.")
                return
            }

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

        override suspend fun generateCharacter(description: String): RequestResult<Exception, Character> {
            val currentSaga =
                content.value
                    ?: return Exception("Saga not loaded for character generation").asError()
            if (isDebugModeEnabled) {
                Log.i(
                    javaClass.simpleName,
                    "[DEBUG MODE] Generating fake character for saga ${currentSaga.data.id}",
                )
                val fakeCharacter =
                    Character(
                        name = "Fake Character: $description",
                        backstory = "Generated in debug mode.",
                        sagaId = currentSaga.data.id,
                        details = Details(),
                    )
                return characterUseCase.insertCharacter(fakeCharacter).asSuccess()
            }
            try {
                Log.d(
                    javaClass.simpleName,
                    "Generating character with description for saga ${currentSaga.data.id}",
                )
                return characterUseCase.generateCharacter(
                    sagaContent = currentSaga,
                    description = description,
                )
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error generating character for saga ${content.value?.data?.id}",
                    e,
                )
                return e.asError()
            }
        }

        override suspend fun generateCharacterImage(character: Character): RequestResult<Exception, Character> {
            val currentSaga =
                content.value
                    ?: return Exception("Saga not loaded for character image generation").asError()
            if (isDebugModeEnabled) {
                Log.i(
                    javaClass.simpleName,
                    "[DEBUG MODE] Skipping image generation for character ${character.name}",
                )
                return character.asSuccess()
            }
            try {
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
                return characterUseCase.generateCharacterImage(
                    character,
                    descriptionGen,
                    currentSaga.data,
                )
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error generating character image for ${character.name} in saga ${content.value?.data?.id}",
                    e,
                )
                return e.asError()
            }
        }

        override suspend fun endSaga() {
            val currentSagaState = content.value ?: return
            sagaHistoryUseCase.updateSaga(
                currentSagaState.data.copy(
                    isEnded = true,
                    endedAt = Calendar.getInstance().timeInMillis,
                ),
            )
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
