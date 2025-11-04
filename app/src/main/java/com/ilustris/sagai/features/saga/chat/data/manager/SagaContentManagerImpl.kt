package com.ilustris.sagai.features.saga.chat.data.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileCacheService
import com.ilustris.sagai.core.narrative.ActDirectives
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCheck
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeStep
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import com.ilustris.sagai.ui.components.SnackBarState
import com.ilustris.sagai.ui.components.snackBar
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
        private val backupService: BackupService,
        @ApplicationContext
        private val context: Context,
    ) : SagaContentManager {
        override val content = MutableStateFlow<SagaContent?>(null)

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

        override var snackBarUpdate: MutableStateFlow<SnackBarState?> = MutableStateFlow(null)
        override val backupEnabled = backupService.backupEnabled()
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
                    .collectLatest { saga ->
                        Log.d(
                            javaClass.simpleName,
                            "Saga flow updated for saga -> $sagaId \n ${saga?.data.toJsonFormat()}",
                        )
                        content.emit(saga)
                        if (saga == null) {
                            Log.e(javaClass.simpleName, "loadSaga: Unexpected error loading saga($sagaId)")
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
                setNarrativeProcessingStatus(true)
                val lastChapter = act.chapters.lastOrNull()
                if (lastChapter?.isComplete()?.not() == true) {
                    actUseCase.updateAct(act.data.copy(currentChapterId = lastChapter.data.id))
                    throw IllegalArgumentException("Chapter is already set at this act")
                }
                chapterUseCase.saveChapter(Chapter(actId = act.data.id))
            }

        private suspend fun updateChapter(
            saga: SagaContent,
            chapter: ChapterContent,
        ): RequestResult<Chapter> =
            executeRequest {
                setNarrativeProcessingStatus(true)
                chapterUseCase
                    .updateChapter(
                        chapter.data.copy(
                            currentEventId = null,
                        ),
                    )

                val chapterUpdate =
                    chapterUseCase
                        .generateChapter(
                            saga,
                            chapter,
                        ).getSuccess()
                val genChapterCharacters =
                    chapterUpdate?.featuredCharacters?.mapNotNull { charactersNames ->
                        saga.characters
                            .find {
                                it.data.name.equals(
                                    charactersNames,
                                    ignoreCase = true,
                                )
                            }?.data
                            ?.id
                    }

                val featuredCharacters =
                    chapter
                        .fetchChapterMessages()
                        .rankTopCharacters(saga.getCharacters())
                        .take(3)
                        .map { it.first.id }

                delay(2.seconds)

                val emotionalReview =
                    generateEmotionalReview(
                        chapter.events.filter { it.isComplete() }.mapIndexed { i, event ->
                            """
                            ${i + 1} - ${event.data.title}: ${
                                event.messages.rankEmotionalTone().first().first.name
                            }
                            ${event.data.emotionalReview ?: "No emotional review created on this event."}   
                            """.trimIndent()
                        },
                        emotionalRanking =
                            chapter.events
                                .map { it.emotionalRanking(saga.mainCharacter?.data) }
                                .flatMap { it.entries }
                                .associate { it.key to it.value },
                    )

                val newChapter =
                    chapter.data.copy(
                        title = chapterUpdate?.title ?: chapter.data.title,
                        overview = chapterUpdate?.overview ?: chapter.data.overview,
                        featuredCharacters = genChapterCharacters ?: featuredCharacters,
                        emotionalReview = emotionalReview.getSuccess(),
                    )

                updateSnackBar(
                    snackBar(
                        message = context.getString(R.string.chapter_finished, newChapter.title),
                    ),
                )

                chapterUseCase.updateChapter(
                    newChapter,
                )
            }

        private suspend fun mergeWiki(
            saga: SagaContent,
            chapter: ChapterContent,
        ) = wikiUseCase.mergeWikis(
            saga,
            chapter.events.map { it.updatedWikis }.flatten(),
        )

        override suspend fun reviewWiki(wikiItems: List<Wiki>) {
            val saga = content.value ?: return
            startProcessing {
                wikiUseCase.mergeWikis(saga, wikiItems)
            }
        }

        override suspend fun reviewEvent(timelineContent: TimelineContent) {
            val saga = content.value ?: return
            startProcessing {
                timelineUseCase.generateTimelineContent(
                    saga,
                    timelineContent,
                )
            }
        }

        override suspend fun backupSaga() {
            val currentSaga = content.value ?: return
            Log.d(javaClass.simpleName, "Backing up saga ${currentSaga.data.id}")

            sagaHistoryUseCase.backupSaga(currentSaga)
        }

        override suspend fun enableBackup(uri: Uri?) {
            uri?.let {
                backupService.enableBackup(it)
            } ?: run {
                snackBarUpdate.emit(snackBar(context.getString(R.string.backup_disabled)))
            }
        }

        private suspend fun endChapter(currentAct: ActContent?) =
            executeRequest {
                actUseCase
                    .updateAct(
                        currentAct!!.data.copy(
                            currentChapterId = null,
                        ),
                    )
            }

        private suspend fun startTimeline(currentChapter: ChapterContent) =
            executeRequest {
                val lastTimeline = currentChapter.events.lastOrNull()
                if (lastTimeline?.isComplete()?.not() == true) {
                    chapterUseCase.updateChapter(
                        currentChapter.data.copy(
                            currentEventId = lastTimeline.data.id,
                        ),
                    )
                    throw IllegalArgumentException("Timeline already set at this chapter")
                }
                timelineUseCase.saveTimeline(
                    Timeline(
                        chapterId = currentChapter.data.id,
                    ),
                )
            }

        private suspend fun endTimeline(currentChapter: ChapterContent?) =
            executeRequest {
                setNarrativeProcessingStatus(true)
                chapterUseCase
                    .updateChapter(
                        currentChapter!!.data.copy(
                            currentEventId = null,
                        ),
                    )
            }

        private suspend fun generateEmotionalReview(
            content: List<String>,
            emotionalRanking: Map<String, Int>,
        ) = emotionalUseCase
            .generateEmotionalReview(content.filter { it.isNotEmpty() }, emotionalRanking)

        private suspend fun updateTimeline(
            saga: SagaContent,
            content: TimelineContent,
        ) = executeRequest {
            if (content.isComplete()) {
                endTimeline(saga.currentActInfo?.currentChapterInfo)
                error("Timeline already completed")
            } else {
                setNarrativeProcessingStatus(true)
                delay(2.seconds)
                val timeLineUpdate = timelineUseCase.generateTimeline(saga, content).getSuccess()!!

                updateSnackBar(
                    SnackBarState(
                        message = context.getString(R.string.timeline_updated, timeLineUpdate.title),
                    ),
                )

                timeLineUpdate
            }
        }

        private suspend fun createAct(currentSaga: SagaContent) =
            executeRequest {
                setNarrativeProcessingStatus(true)
                val lastAct = currentSaga.acts.lastOrNull()
                if (lastAct?.isComplete()?.not() == true) {
                    error("Act is already set at this saga")
                }
                actUseCase
                    .saveAct(
                        Act(
                            sagaId = currentSaga.data.id,
                        ),
                    )
            }

        private suspend fun updateAct(currentAct: ActContent) =
            executeRequest {
                val saga = content.value!!
                setNarrativeProcessingStatus(true)
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
                        actUseCase.generateAct(saga).getSuccess()!!
                    }
                val emotionalRanking =
                    currentAct.chapters
                        .map { it.events }
                        .map {
                            it.map { event ->
                                event.emotionalRanking(saga.mainCharacter?.data)
                            }
                        }.flatten()
                        .flatMap { it.entries }
                        .associate { it.key to it.value }

                val emotionalReview =
                    generateEmotionalReview(
                        currentAct.chapters.mapIndexed { i, chapter ->
                            """
                        ${i + 1} - ${chapter.data.title}
                        ${chapter.data.emotionalReview}    
                        """
                        },
                        emotionalRanking,
                    ).getSuccess()
                val updatedActData =
                    currentAct.data.copy(
                        title = genAct.title,
                        content = genAct.content,
                        emotionalReview = emotionalReview ?: emptyString(),
                    )
                val newAct = actUseCase.updateAct(updatedActData)
                updateSnackBar(
                    snackBar(
                        message = context.getString(R.string.chapter_finished, newAct.title),
                    ),
                )
                endAct(saga)
                newAct
            }

        private suspend fun endAct(saga: SagaContent) =
            executeRequest {
                sagaHistoryUseCase.updateSaga(saga.data.copy(currentActId = null)).asSuccess()
            }

        override fun checkNarrativeProgression(
            saga: SagaContent?,
            isRetrying: Boolean,
        ) {
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
                        "Narrative progression is already in progress,skipping.",
                    )
                    return@launch
                }

                if (!isProcessingNarrative.compareAndSet(false, true)) {
                    Log.i(
                        javaClass.simpleName,
                        "Lock acquisition failed (race condition or already processing), skipping.",
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
                val narrativeStep = NarrativeCheck.validateProgression(saga)
                Log.d(
                    javaClass.simpleName,
                    "checkNarrativeProgression: Progression step ${narrativeStep.javaClass.simpleName}",
                )

                startProcessing {
                    val action: RequestResult<Any> =
                        when (narrativeStep) {
                            NarrativeStep.StartAct -> createAct(saga)
                            is NarrativeStep.GenerateSagaEnding -> generateEnding(saga)
                            is NarrativeStep.GenerateAct -> updateAct(narrativeStep.act)
                            is NarrativeStep.StartChapter -> startChapter(narrativeStep.act)
                            is NarrativeStep.GenerateChapter ->
                                updateChapter(
                                    saga,
                                    narrativeStep.chapter,
                                )

                            is NarrativeStep.StartTimeline -> startTimeline(narrativeStep.chapter)
                            is NarrativeStep.GenerateTimeLine ->
                                updateTimeline(
                                    saga,
                                    narrativeStep.timeline,
                                )

                            is NarrativeStep.EndTimeLine -> endTimeline(narrativeStep.currentChapterContent)
                            NarrativeStep.NoActionNeeded -> skipNarrative()
                        }

                    val act = saga.currentActInfo
                    val chapter = act?.currentChapterInfo
                    val timeline = chapter?.currentEventInfo
                    sendDebugMessage(
                        """
                        Narrative progression  #$progressionCounter completed, no limits reached.
                        acts: ${saga.acts.size} of ${UpdateRules.MAX_ACTS_LIMIT} per Saga.
                        chapters in current act(${saga.acts.size}): ${
                            (
                                act?.chapters?.count {
                                    it.isComplete()
                                }
                            ) ?: 0
                        } of ${UpdateRules.ACT_UPDATE_LIMIT} per Act.
                        events: ${chapter?.events?.count { it.isComplete() }} of ${UpdateRules.CHAPTER_UPDATE_LIMIT} per Chapter.
                        messages since last event: ${timeline?.messages?.size} of ${UpdateRules.LORE_UPDATE_LIMIT} per Event.
                        """.trimIndent(),
                    )

                    action
                        .onSuccessAsync {
                            validatePostAction(saga, narrativeStep, action.success)
                        }.onFailureAsync {
                            updateSnackBar(
                                snackBar(
                                    context.getString(R.string.unexpected_error),
                                ) {
                                    action {
                                        revaluateSaga()
                                    }
                                },
                            )
                            if (isRetrying.not()) {
                                delay(3.seconds)
                                checkNarrativeProgression(saga, true)
                            }
                        }
                }
            }
        }

        private fun updateSnackBar(state: SnackBarState) {
            CoroutineScope(Dispatchers.IO).launch {
                snackBarUpdate.emit(state)
                delay(10.seconds)
                resetSnackBar()
            }
        }

        private fun resetSnackBar() {
            snackBarUpdate.value = null
        }

        override suspend fun regenerateTimeline(
            saga: SagaContent,
            timelineContent: TimelineContent,
        ) {
            setNarrativeProcessingStatus(true)
            generateTimelineContent(timelineContent.data, saga)
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
                        (result.value as? Act)?.let { data ->
                            sagaHistoryUseCase.updateSaga(
                                saga.data.copy(currentActId = data.id),
                            )
                            startProcessing {
                                delay(3.seconds)
                                actUseCase.generateActIntroduction(saga, data)
                            }

                            backupSaga()
                        }
                    }

                    is NarrativeStep.StartChapter -> {
                        val currentAct = saga.currentActInfo!!
                        (result.value as? Chapter)?.let {
                            actUseCase.updateAct(
                                currentAct.data.copy(currentChapterId = it.id),
                            )
                            startProcessing {
                                delay(3.seconds)
                                chapterUseCase
                                    .generateChapterIntroduction(
                                        saga = content.value!!,
                                        chapterContent = it,
                                        act = currentAct,
                                    )
                            }
                        }
                    }

                    is NarrativeStep.StartTimeline -> {
                        (result.value as? Timeline)?.let {
                            chapterUseCase.updateChapter(
                                saga.currentActInfo!!.currentChapterInfo!!.data.copy(
                                    currentEventId = (result.value as Timeline).id,
                                ),
                            )
                            startProcessing {
                                delay(2.seconds)
                                val objective =
                                    timelineUseCase.getTimelineObjective(content.value!!).getSuccess()
                                timelineUseCase.updateTimeline(
                                    it.copy(
                                        currentObjective = objective ?: emptyString(),
                                    ),
                                )
                            }
                        }
                    }

                    is NarrativeStep.GenerateTimeLine -> {
                        (result.value as? Timeline)?.let {
                            startProcessing {
                                generateTimelineContent(it, saga)
                            }
                        }
                    }

                    is NarrativeStep.GenerateChapter -> {
                        endChapter(saga.currentActInfo)
                        (result.value as? Chapter)?.let { chapter ->
                            startProcessing {
                                val chapterContent =
                                    saga.flatChapters().find { it.data.id == chapter.id }!!.copy(
                                        data = chapter,
                                    )
                                withContext(Dispatchers.IO) {
                                    delay(5.seconds)
                                    chapterUseCase.generateChapterCover(chapterContent, saga)
                                }
                                delay(3.seconds)
                                chapterUseCase.reviewChapter(saga, chapterContent)
                            }
                        }
                    }

                    is NarrativeStep.NoActionNeeded -> {
                        checkObjective()
                        setNarrativeProcessingStatus(false)
                    }

                    else -> setNarrativeProcessingStatus(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                setNarrativeProcessingStatus(false)
            }
        }

        private suspend fun checkObjective() =
            executeRequest {
                content.value?.currentActInfo?.currentChapterInfo?.currentEventInfo?.let { currentTimeline ->
                    if (currentTimeline.data.currentObjective.isNullOrEmpty()) {
                        timelineUseCase
                            .getTimelineObjective(content.value!!)
                            .getSuccess()
                            ?.let { newObjective ->
                                if (newObjective.isNotEmpty()) {
                                    timelineUseCase.updateTimeline(
                                        currentTimeline.data.copy(
                                            currentObjective = newObjective,
                                        ),
                                    )
                                }
                            }
                    }
                }
            }

        private suspend fun generateTimelineContent(
            timeline: Timeline,
            saga: SagaContent,
        ) {
            withContext(Dispatchers.IO) {
                delay(5.seconds)

                saga.flatEvents().find { it.data.id == timeline.id }?.let { content ->
                    timelineUseCase.generateTimelineContent(saga, content.copy(data = timeline))

                    updateSnackBar(
                        snackBar(
                            context.getString(R.string.timeline_updated, timeline.title),
                        ),
                    )
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
                setNarrativeProcessingStatus(true)
                val endingMessage = sagaHistoryUseCase.generateEndMessage(saga).getSuccess()!!
                val emotionalEnding = emotionalUseCase.generateEmotionalProfile(saga).getSuccess()
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

        override suspend fun generateCharacter(description: String): RequestResult<Character> =
            executeRequest {
                val currentSaga = content.value!!

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
                            profile = CharacterProfile(),
                        )
                    characterUseCase.insertCharacter(fakeCharacter)
                } else {
                    characterUseCase
                        .generateCharacter(
                            sagaContent = currentSaga,
                            description = description,
                        ).getSuccess()!!
                }
            }

        override suspend fun generateCharacterImage(character: Character): RequestResult<Character> =
            executeRequest {
                val currentSaga = content.value!!
                if (isDebugModeEnabled) {
                    Log.i(
                        javaClass.simpleName,
                        "[DEBUG MODE] Skipping image generation for character ${character.name}",
                    )
                    character
                } else {
                    characterUseCase
                        .generateCharacterImage(
                            character,
                            currentSaga.data,
                        ).success.value.first
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

        private suspend fun startProcessing(block: suspend () -> Unit) {
            if (isProcessing.not()) {
                setProcessing(true)
            }
            block()
            setProcessing(false)
        }
    }
