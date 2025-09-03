package com.ilustris.sagai.features.saga.chat.domain.manager

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.ActDirectives
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.FileCacheService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.domain.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.domain.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.domain.usecase.WikiUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SagaContentManagerImpl
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val characterUseCase: CharacterUseCase,
        private val chapterUseCase: ChapterUseCase,
        private val wikiUseCase: WikiUseCase,
        private val timelineUseCase: TimelineUseCase,
        private val actUseCase: ActUseCase,
        private val emotionalUseCase: EmotionalUseCase,
        private val fileCacheService: FileCacheService,
        private val remoteConfig: FirebaseRemoteConfig,
    ) : SagaContentManager {
        override val content = MutableStateFlow<SagaContent?>(null)
        override val endMessage = MutableStateFlow<String?>(null)
        override val contentUpdateMessages: MutableSharedFlow<Message> =
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

        override val ambientMusicFile = MutableStateFlow<File?>(null)

        private val isProcessingNarrative = AtomicBoolean(false)
        private val _narrativeProcessingUiState = MutableStateFlow(false)
        override val narrativeProcessingUiState: StateFlow<Boolean> =
            _narrativeProcessingUiState.asStateFlow()

        private var isDebugModeEnabled: Boolean = false
        private var isProcessing: Boolean = false

        private var progressionCounter = 0

        private fun setNarrativeProcessingStatus(isProcessing: Boolean) {
            isProcessingNarrative.set(isProcessing)
            _narrativeProcessingUiState.value = isProcessing
        }

        override fun setDebugMode(enabled: Boolean) {
            isDebugModeEnabled = enabled
            Log.i(javaClass.simpleName, "Debug mode ${if (enabled) "enabled" else "disabled"}")
        }

        override fun setProcessing(bool: Boolean) {
            setNarrativeProcessingStatus(bool)
            Log.i(javaClass.simpleName, "Processing mode ${if (bool) "enabled" else "disabled"}")
        }

        override fun isInDebugMode(): Boolean = isDebugModeEnabled

        override suspend fun loadSaga(sagaId: String) {
            Log.d(javaClass.simpleName, "Loading saga: $sagaId")
            try {
                sagaHistoryUseCase
                    .getSagaById(sagaId.toInt())
                    .debounce(500L)
                    .collectLatest { saga ->
                        Log.d(
                            javaClass.simpleName,
                            "Saga flow updated for saga -> $sagaId \n ${saga?.data.toJsonFormat()}",
                        )
                        content.value = saga
                        if (saga == null) {
                            return@collectLatest
                        }

                        val messages = saga.flatMessages()
                        if (messages.isNotEmpty() &&
                            messages
                                .last()
                                .message
                                .senderType == SenderType.ACTION &&
                            isDebugModeEnabled
                        ) {
                            return@collectLatest
                        }
                        checkNarrativeProgression(saga)

                        getAmbienceMusic(saga)
                    }
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error loading saga $sagaId", e)
                content.value = null
                setNarrativeProcessingStatus(false)
            }
        }

        private suspend fun getAmbienceMusic(saga: SagaContent) {
            val genre = saga.data.genre
            val fileUrl = remoteConfig.getString(genre.ambientMusicConfigKey)

            if (fileUrl.isEmpty()) {
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
            val currentSaga = content.value
            val timeLine = currentSaga?.currentActInfo?.currentChapterInfo?.currentEventInfo
            if (isDebugModeEnabled) {
                contentUpdateMessages.emit(
                    Message(
                        text = message,
                        senderType = SenderType.ACTION,
                        timelineId = timeLine?.data?.id ?: -1,
                    ),
                )
            } else {
                Log.d(javaClass.simpleName, "Debug message: $message")
            }
        }

        private suspend fun startChapter(act: ActContent) =
            executeRequest {
                val lastChapter = act.chapters.lastOrNull()
                if (lastChapter?.isComplete()?.not() == true) {
                    throw IllegalArgumentException("Chapter is already set at this act")
                }
                val chapterOperation = chapterUseCase.saveChapter(Chapter(actId = act.data.id))

                chapterUseCase.generateChapterIntroduction(content.value!!, chapterOperation, act)

                chapterOperation
            }

        private suspend fun updateChapter(
            saga: SagaContent,
            chapter: ChapterContent,
        ): RequestResult<Exception, Chapter> =
            try {
                val chapterGen =
                    chapterUseCase
                        .generateChapter(
                            saga,
                            chapter,
                        ).success.value

                val featuredCharacters =
                    chapter
                        .fetchChapterMessages()
                        .rankTopCharacters(saga.getCharacters())
                        .take(3)
                        .map { it.first.id }

                val emotionalReview =
                    generateEmotionalReview(
                        chapter.events.filter { it.isComplete() }.mapIndexed { i, event ->
                            """
                            ${i + 1} - ${event.data.title}
                            ${event.data.emotionalReview}   
                            """.trimIndent()
                        },
                    )

                val newChapter =
                    chapterUseCase
                        .generateChapterCover(
                            chapter.copy(
                                chapter.data.copy(
                                    title = chapterGen.title,
                                    overview = chapterGen.overview,
                                    emotionalReview = emotionalReview ?: emptyString(),
                                    featuredCharacters = featuredCharacters,
                                ),
                            ),
                            saga,
                        )
                endChapter(saga.currentActInfo!!)
                newChapter
            } catch (e: Exception) {
                e.asError()
            }

        private suspend fun endChapter(currentAct: ActContent?) =
            try {
                actUseCase
                    .updateAct(
                        currentAct!!.data.copy(
                            currentChapterId = null,
                        ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private suspend fun startTimeline(currentChapter: ChapterContent?) =
            executeRequest {
                val lastTimeline = currentChapter!!.events.lastOrNull()
                if (lastTimeline?.isComplete()?.not() == true) {
                    throw IllegalArgumentException("Timeline already set at this chapter")
                }
                val timeLineOperation =
                    timelineUseCase.saveTimeline(Timeline(chapterId = currentChapter.data.id))
                chapterUseCase.updateChapter(
                    currentChapter.data.copy(
                        currentEventId = timeLineOperation.id,
                    ),
                )
                timeLineOperation
            }

        private suspend fun endTimeline(currentChapter: ChapterContent?) =
            try {
                chapterUseCase
                    .updateChapter(
                        currentChapter!!.data.copy(
                            currentEventId = null,
                        ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private suspend fun generateEmotionalReview(content: List<String>) =
            emotionalUseCase
                .generateEmotionalReview(content.filter { it.isNotEmpty() })
                .getSuccess()

        private suspend fun updateTimeline(
            saga: SagaContent,
            content: TimelineContent,
        ) = try {
            val loreGen =
                sagaHistoryUseCase
                    .generateLore(
                        saga,
                        content,
                    ).getSuccess()!!

            val userMessages =
                content.messages.map { it.joinMessage(showType = true).formatToString(true) }

            val emotionalReview = generateEmotionalReview(userMessages)

            val newEvent =
                timelineUseCase
                    .updateTimeline(
                        content.data.copy(
                            id = content.data.id,
                            title = loreGen.title,
                            content = loreGen.content,
                            emotionalReview = emotionalReview ?: emptyString(),
                        ),
                    )
            endTimeline(saga.currentActInfo!!.currentChapterInfo)
            newEvent.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }

        private suspend fun createAct(currentSaga: SagaContent): RequestResult<Exception, Act> =
            executeRequest {
                val lastAct = currentSaga.acts.lastOrNull()
                if (lastAct?.isComplete()?.not() == true) {
                    throw IllegalArgumentException("Act is already set at this saga")
                }
                actUseCase
                    .saveAct(
                        Act(
                            sagaId = currentSaga.data.id,
                        ),
                    )
            }

        private suspend fun updateAct(currentAct: ActContent): RequestResult<Exception, Act> =
            executeRequest {
                val saga = content.value!!
                Log.d(
                    javaClass.simpleName,
                    "updating act(${saga.currentActInfo?.data?.id})",
                )
                val genAct =
                    if (isDebugModeEnabled) {
                        Log.i(
                            javaClass.simpleName,
                            "[DEBUG MODE] Generating fake act update data for saga ${saga.data.id}",
                        )
                        Act(
                            id = currentAct.data.id,
                            title = "Updated Act ${saga.acts.size}",
                            content = "This act was updated in debug mode.",
                            sagaId = saga.data.id,
                        )
                    } else {
                        actUseCase.generateAct(saga).success.value
                    }
                val emotionalReview =
                    generateEmotionalReview(
                        currentAct.chapters.mapIndexed { i, chapter ->
                            """
                        ${i + 1} - ${chapter.data.title}
                        ${chapter.data.emotionalReview}    
                        """
                        },
                    )
                val updatedActData =
                    currentAct.data.copy(
                        title = genAct.title,
                        content = genAct.content,
                        emotionalReview = emotionalReview ?: emptyString(),
                    )
                val newAct = actUseCase.updateAct(updatedActData)
                endAct(saga)
                newAct
            }

        private suspend fun endAct(saga: SagaContent) =
            try {
                sagaHistoryUseCase.updateSaga(saga.data.copy(currentActId = null)).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun checkNarrativeProgression(saga: SagaContent?) =
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(javaClass.simpleName, "Starting narrative progression check")
                progressionCounter++

                if (saga == null) {
                    Log.e(
                        javaClass.simpleName,
                        "checkNarrativeProgression: No saga founded to check progression",
                    )
                    return@launch
                }

                if (isProcessingNarrative.get() || isProcessing) {
                    Log.i(
                        javaClass.simpleName,
                        "checkNarrativeProgression: Narrative progression is already in progress or general processing flag is set, skipping.",
                    )
                    return@launch
                }

                if (!isProcessingNarrative.compareAndSet(false, true)) {
                    Log.i(
                        javaClass.simpleName,
                        "checkNarrativeProgression: Lock acquisition failed (race condition or already processing), skipping.",
                    )
                    return@launch
                }

                if (saga.mainCharacter == null && isDebugModeEnabled) {
                    generateCharacter("Main Debug Character").onSuccessAsync { newCharacter ->
                        sagaHistoryUseCase.updateSaga(saga.data.copy(mainCharacterId = newCharacter.id))
                    }
                    setNarrativeProcessingStatus(false)
                    return@launch
                }
                setNarrativeProcessingStatus(true)
                val narrativeStep = NarrativeCheck.validateProgression(saga)
                Log.d(javaClass.simpleName, "checkNarrativeProgression: Progression step $narrativeStep")

                val action: RequestResult<Exception, Any> =
                    when (narrativeStep) {
                        NarrativeStep.StartAct -> createAct(saga)
                        is NarrativeStep.GenerateSagaEnding -> generateEnding(saga)
                        is NarrativeStep.GenerateAct -> updateAct(narrativeStep.act)
                        is NarrativeStep.StartChapter -> startChapter(narrativeStep.act)
                        is NarrativeStep.GenerateChapter -> updateChapter(saga, narrativeStep.chapter)
                        is NarrativeStep.StartTimeline -> startTimeline(narrativeStep.chapter)
                        is NarrativeStep.GenerateTimeLine -> updateTimeline(saga, narrativeStep.timeline)
                        NarrativeStep.NoActionNeeded -> skipNarrative()
                    }

                val act = saga.currentActInfo
                val chapter = act?.currentChapterInfo
                val timeline = chapter?.currentEventInfo
                sendDebugMessage(
                    """
                    Narrative progression  #$progressionCounter completed, no limits reached.
                    acts: ${saga.acts.size} of ${UpdateRules.MAX_ACTS_LIMIT} per Saga.
                    chapters in current act(${saga.acts.size}): ${(act?.chapters?.size) ?: 0} of ${UpdateRules.ACT_UPDATE_LIMIT} per Act.
                    events: ${chapter?.events?.size} of ${UpdateRules.CHAPTER_UPDATE_LIMIT} per Chapter.
                    messages since last event: ${timeline?.messages?.size} of ${UpdateRules.LORE_UPDATE_LIMIT} per Event.
                    """.trimIndent(),
                )
                action
                    .onSuccess {
                        validatePostAction(saga, narrativeStep, action.success)
                    }.onFailure {
                        setNarrativeProcessingStatus(false)
                    }
            }

        private suspend fun skipNarrative() =
            executeRequest {
                Log.i(javaClass.simpleName, "skipNarrative: No action needed skipping narrative")
            }

        private fun validatePostAction(
            saga: SagaContent,
            step: NarrativeStep,
            result: RequestResult.Success<Any>,
        ) = CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(javaClass.simpleName, "validatePostAction: performing next step $step")
                when (step) {
                    is NarrativeStep.StartAct -> {
                        val data = result.value as Act
                        sagaHistoryUseCase.updateSaga(
                            saga.data.copy(currentActId = data.id),
                        )
                        actUseCase.generateActIntroduction(saga, data)
                        setNarrativeProcessingStatus(false)
                    }

                    is NarrativeStep.StartChapter -> {
                        val currentAct = saga.currentActInfo!!
                        val newChapterId = (result.value as Chapter).id
                        actUseCase.updateAct(
                            currentAct.data.copy(currentChapterId = newChapterId),
                        )
                        setNarrativeProcessingStatus(false)
                    }

                    is NarrativeStep.StartTimeline -> {
                        chapterUseCase.updateChapter(
                            saga.currentActInfo!!.currentChapterInfo!!.data.copy(
                                currentEventId = (result.value as Timeline).id,
                            ),
                        )
                        setNarrativeProcessingStatus(false)
                    }

                    is NarrativeStep.GenerateTimeLine -> {
                        (result.value as? Timeline)?.let {
                            delay(250L)
                            updateCharacters(it, saga)
                            updateWikis(it)
                            setNarrativeProcessingStatus(false)
                        } ?: run {
                            setNarrativeProcessingStatus(false)
                        }
                    }

                    else -> setNarrativeProcessingStatus(false)
                }

                // clearInvalidData(saga)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun cleanUpEmptyTimeLines(chapter: ChapterContent) =
            CoroutineScope(Dispatchers.IO).launch {
                setProcessing(true)
                val emptyEvents = chapter.events.filter { it.isComplete().not() }
                if (emptyEvents.isEmpty()) {
                    setNarrativeProcessingStatus(false)
                    return@launch
                }
                emptyEvents.forEach { timeline ->
                    timelineUseCase.deleteTimeline(timeline.data)
                    if (timeline == chapter.events.last()) {
                        delay(2.seconds)
                        setNarrativeProcessingStatus(false)
                    }
                }
            }

        private suspend fun createEndingMessage(saga: SagaContent) =
            try {
                if (isDebugModeEnabled) {
                    sendDebugMessage("Generating debug end message")
                    val message = "Congratulations on completing this saga!"
                    sagaHistoryUseCase
                        .updateSaga(
                            saga.data.copy(
                                endMessage = message,
                                isEnded = true,
                                endedAt = System.currentTimeMillis(),
                            ),
                        ).asSuccess()
                }
                val endingMessage = sagaHistoryUseCase.generateEndMessage(saga).getSuccess()!!
                val emotionalEnding =
                    emotionalUseCase.generateEmotionalProfile(saga).getSuccess()
                sagaHistoryUseCase
                    .updateSaga(
                        saga.data.copy(
                            endMessage = endingMessage,
                            isEnded = true,
                            endedAt = System.currentTimeMillis(),
                            emotionalReview = emotionalEnding ?: emptyString(),
                        ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private suspend fun updateCharacters(
            timeline: Timeline,
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

            characterUseCase.generateCharactersUpdate(timeline, currentSaga)

            characterUseCase.generateCharacterRelations(timeline, currentSaga)

            setNarrativeProcessingStatus(false)
        }

        private suspend fun updateWikis(lastEvent: Timeline) {
            val currentSaga =
                content.value ?: run {
                    Log.w(javaClass.simpleName, "updateWikis: Saga not loaded, cannot update wikis.")
                    return
                }
            Log.d(
                javaClass.simpleName,
                "Updating wikis based on recent events -> ${lastEvent.toJsonFormat()} for saga ${currentSaga.data.id}",
            )
            if (isDebugModeEnabled) {
                Log.i(javaClass.simpleName, "[DEBUG MODE] Skipping wiki updates.")
                return
            }

            val wikisToUpdateOrAdd = wikiUseCase.generateWiki(currentSaga, listOf(lastEvent))
            Log.d(javaClass.simpleName, "updateWikis: Updating wikis $wikisToUpdateOrAdd")
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
                    "updateWikis: No wiki updates generated for recnt events in saga ${currentSaga.data.id}.",
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

                return characterUseCase
                    .generateCharacterImage(
                        character,
                        currentSaga.data,
                    ).success.value.first
                    .asSuccess()
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error generating character image for ${character.name} in saga ${content.value?.data?.id}",
                    e,
                )
                return e.asError()
            }
        }

        suspend fun generateEnding(saga: SagaContent) = createEndingMessage(saga)

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
