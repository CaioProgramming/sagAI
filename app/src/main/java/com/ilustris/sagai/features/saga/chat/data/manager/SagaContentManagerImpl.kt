package com.ilustris.sagai.features.saga.chat.data.manager

import android.content.Context
import android.net.Uri
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileCacheService
import com.ilustris.sagai.core.file.ImageHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toRoman
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
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCheck
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeStep
import com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType
import com.ilustris.sagai.features.saga.chat.presentation.model.PendingAdvance
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import com.ilustris.sagai.ui.components.NotificationStyle
import com.ilustris.sagai.ui.components.SnackBarState
import com.ilustris.sagai.ui.components.snackBar
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import timber.log.Timber

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
        private val remoteConfig: RemoteConfigService,
        private val backupService: BackupService,
        private val imageHelper: ImageHelper,
        private val messageUseCase: MessageUseCase,
        private val genreConfigService: GenreConfigService,
        private val reasoningSynthesizerService: com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService,
        @ApplicationContext
        private val context: Context,
    ) : SagaContentManager {
        override val contentReasoning = MutableStateFlow<String?>(null)
        override val content = MutableStateFlow<SagaContent?>(null)
        private val _sceneSummary = MutableStateFlow<SceneSummary?>(null)
        override val sceneSummary: StateFlow<SceneSummary?> = _sceneSummary.asStateFlow()
        override val milestoneUpdate = MutableStateFlow<SagaMilestone?>(null)
        override val isOnboardingVisible = MutableStateFlow(false)

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

        private var sagaJob: kotlinx.coroutines.Job? = null
        private var loadingObserverJob: kotlinx.coroutines.Job? = null
        private var milestoneObserverJob: kotlinx.coroutines.Job? = null

        override var snackBarUpdate: MutableStateFlow<SnackBarState?> = MutableStateFlow(null)

        private var isDebugModeEnabled: Boolean = false
        private val isProcessing = AtomicBoolean(false)

        private val progressionMutex = Mutex()
        private val managerJob = SupervisorJob()
        private val managerScope = CoroutineScope(managerJob + Dispatchers.IO)

        private var progressionCounter = 0

        private fun setNarrativeProcessingStatus(isProcessing: Boolean) {
            isProcessingNarrative.set(isProcessing)
            _narrativeProcessingUiState.value = isProcessing
        }

        override fun setDebugMode(enabled: Boolean) {
            isDebugModeEnabled = enabled
            Timber.i("Debug mode ${if (enabled) "enabled" else "disabled"}")
        }

        override suspend fun setProcessing(bool: Boolean) {
            isProcessing.set(bool)
            setNarrativeProcessingStatus(bool)
            Timber.i("Processing mode ${if (bool) "enabled" else "disabled"}")
        }

        override fun isInDebugMode(): Boolean = isDebugModeEnabled

        override suspend fun advanceNarrative(pendingAdvance: PendingAdvance) {
            val currentSaga = content.value ?: return
            Timber.d("Manually advancing narrative: ${pendingAdvance.javaClass.simpleName}")

            var action: RequestResult<Any>? = null
            startProcessing {
                action =
                    when (pendingAdvance) {
                        is PendingAdvance.NewEvent -> {
                            updateTimeline(
                                currentSaga,
                                pendingAdvance.timeline,
                            )
                        }

                        is PendingAdvance.NewChapter -> {
                            updateChapter(
                                currentSaga,
                                pendingAdvance.chapter,
                            )
                        }

                        is PendingAdvance.NewAct -> {
                            updateAct(pendingAdvance.act)
                        }

                        is PendingAdvance.NewActIntroduction -> {
                            emitMilestone(SagaMilestone.Loading)
                            generateActIntroduction(pendingAdvance.act)
                        }

                        is PendingAdvance.NewChapterIntroduction -> {
                            emitMilestone(SagaMilestone.Loading)
                            generateChapterIntroduction(pendingAdvance.chapter)
                        }

                        is PendingAdvance.StartStory -> {
                            startTimeline(pendingAdvance.chapter)
                        }

                        is PendingAdvance.SagaEnding -> {
                            generateEnding(currentSaga)
                        }
                    }
            }

            action
                ?.onSuccessAsync {
                    val step =
                        when (pendingAdvance) {
                            is PendingAdvance.NewEvent -> {
                                NarrativeStep.GenerateTimeLine(pendingAdvance.timeline)
                            }

                            is PendingAdvance.NewChapter -> {
                                NarrativeStep.GenerateChapter(pendingAdvance.chapter)
                            }

                            is PendingAdvance.NewAct -> {
                                NarrativeStep.GenerateAct(pendingAdvance.act)
                            }

                            is PendingAdvance.NewActIntroduction -> {
                                NarrativeStep.GenerateActIntroduction(
                                    pendingAdvance.act,
                                )
                            }

                            is PendingAdvance.NewChapterIntroduction -> {
                                NarrativeStep.GenerateChapterIntroduction(
                                    pendingAdvance.chapter,
                                )
                            }

                            is PendingAdvance.StartStory -> {
                                NarrativeStep.StartTimeline(pendingAdvance.chapter)
                            }

                            is PendingAdvance.SagaEnding -> {
                                NarrativeStep.GenerateSagaEnding(
                                    pendingAdvance.saga,
                                )
                            }
                        }
                    validatePostAction(currentSaga, step, action.success)
                }?.onFailureAsync {
                    Timber.e("Failed to advance narrative: ${it.message}")
                    emitMilestone(null)
                }
        }

        override suspend fun updatePlaytime(
            sagaId: Int,
            timeInMillis: Long,
        ) {
            val currentSaga = content.value ?: return
            if (currentSaga.data.id != sagaId) return
            if (currentSaga.data.isEnded) return

            val updatedSaga =
                currentSaga.data.copy(
                    playTimeMs = currentSaga.data.playTimeMs + timeInMillis,
                )
            sagaHistoryUseCase.updateSaga(updatedSaga)
        }

        override suspend fun showObjective() {
            val saga = content.value ?: return
            val currentTimeline = saga.getCurrentTimeLine() ?: return
            val objective = currentTimeline.data.currentObjective
            if (objective?.isNotBlank() == true) {
                milestoneUpdate.emit(SagaMilestone.CurrentObjective(currentTimeline.data))
            }
        }

        override suspend fun loadSaga(sagaId: String) {
            sagaJob?.cancel()
            sagaJob =
                managerScope.launch {
                    Timber.d("Loading saga: $sagaId")
                    content.value = null
                    try {
                        if (loadingObserverJob == null || loadingObserverJob?.isActive == false) {
                            loadingObserverJob = observeLoading()
                        }
                        if (milestoneObserverJob == null || milestoneObserverJob?.isActive == false) {
                            milestoneObserverJob = observeMilestone()
                        }
                        managerScope.launch {
                            isOnboardingVisible.collect { isVisible ->
                                if (!isVisible) {
                                    checkNarrativeProgression(content.value)
                                }
                            }
                        }
                        sagaHistoryUseCase
                            .getSagaById(sagaId.toInt())
                            .catch { e ->
                                val readableMessage =
                                    when {
                                        e is IllegalArgumentException && e.message?.contains("No enum constant") == true -> {
                                            "⚠️ A story record contains corrupted data (invalid enum value: ${
                                                e.message?.substringAfterLast(
                                                    ".",
                                                )
                                            }).\nTry reinstalling the app or contact support if the issue persists."
                                        }

                                        else -> {
                                            "⚠️ Failed to load story data: ${e.message}"
                                        }
                                    }
                                Timber.e(e, "loadSaga: Room Flow error for saga $sagaId — ${e.message}")
                                updateSnackBar(snackBar(readableMessage))
                                content.value = null
                                setNarrativeProcessingStatus(false)
                            }.collectLatest { saga ->
                                Timber.d("Saga flow updated for saga -> $sagaId")

                                if (saga == null) {
                                    Timber.e("loadSaga: Unexpected error loading saga($sagaId)")
                                    content.emit(null)
                                    return@collectLatest
                                }

                                val previousSaga = content.value
                                if (previousSaga == null) {
                                    emitMilestone(SagaMilestone.Loading)
                                }
                                content.value = saga

                                if (previousSaga != null &&
                                    previousSaga.data.id == saga.data.id &&
                                    previousSaga.data.playTimeMs != saga.data.playTimeMs &&
                                    previousSaga.acts == saga.acts &&
                                    previousSaga.characters == saga.characters
                                ) {
                                    Timber.d("Saga update was only playtime. Skipping narrative check.")
                                    return@collectLatest
                                }

                                checkMessageNotifications(previousSaga, saga)

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

                                validateCharacters(saga)

                                val rules = fetchNarrativeRules()
                                val narrativeStep = NarrativeCheck.validateProgression(saga, rules)
                                if (previousSaga == null) {
                                    emitMilestone(null)
                                    if (saga.data.isEnded.not() && saga
                                            .flatEvents()
                                            .filter { it.isComplete(rules) }
                                            .size > 1 &&
                                        narrativeStep == NarrativeStep.NoActionNeeded
                                    ) {
                                        saga.currentActInfo?.currentChapterInfo?.let { chapter ->
                                            emitMilestone(SagaMilestone.Loading)
                                            startProcessing {
                                                val sceneContext =
                                                    _sceneSummary.value
                                                        ?: messageUseCase
                                                            .getSceneContext(saga)
                                                            .getSuccess()
                                                sceneContext?.let { summary ->
                                                    saga.getCurrentTimeLine()?.data?.let {
                                                        if (it.currentObjective.isNullOrEmpty()) {
                                                            timelineUseCase.updateTimeline(
                                                                it.copy(
                                                                    currentObjective = summary.immediateObjective,
                                                                ),
                                                            )
                                                        }
                                                    }
                                                    emitMilestone(
                                                        SagaMilestone.Introduction(
                                                            type = IntroductionType.RESUME,
                                                            titleText = emptyString(),
                                                            introduction =
                                                                summary.immediateObjective
                                                                    ?: summary.currentConflict
                                                                    ?: summary.mood
                                                                    ?: emptyString(),
                                                            number =
                                                                saga
                                                                    .chapterNumber(
                                                                        chapter.data,
                                                                    ).toRoman(),
                                                            sceneSummary = summary,
                                                        ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        Timber.e(e, "Error loading saga $sagaId")
                        content.value = null
                        setNarrativeProcessingStatus(false)
                        emitMilestone(null)
                    }
                }
        }

        private suspend fun validateCharacters(saga: SagaContent) {
            withContext(Dispatchers.IO) {
                saga.characters
                    .filter { it.data.smartZoom == null && it.data.image.isNotEmpty() }
                    .forEach {
                        characterUseCase.createSmartZoom(it.data)
                    }
            }
        }

        private suspend fun checkMessageNotifications(
            previousSaga: SagaContent?,
            saga: SagaContent,
        ) {
            if (previousSaga != null &&
                saga.flatMessages().size > previousSaga.flatMessages().size
            ) {
                saga.flatMessages().size - previousSaga.flatMessages().size
                val lastMessage = saga.flatMessages().last()
                val isFromUser = lastMessage.character == saga.mainCharacter?.data
                val charIcon =
                    imageHelper
                        .getImageBitmap(lastMessage.character?.image, true)
                        .getSuccess()
                if (isFromUser.not()) {
                    updateSnackBar(
                        snackBar(
                            "${lastMessage.message.speakerName ?: emptyString()}: ${lastMessage.message.text}",
                        ) {
                            showInUi = false
                            icon = charIcon
                            notificationStyle = NotificationStyle.CHAT
                        },
                    )
                }
            }
        }

        private suspend fun getAmbienceMusic(saga: SagaContent) {
            val genre = saga.data.genre
            val fileUrl = genreConfigService.getGenreConfig(genre).ambientMusicUrl

            if (fileUrl.isEmpty()) {
                Timber.e("getAmbienceMusic: Invalid URL for ${genre.name}")
                return
            }

            withContext(Dispatchers.IO) {
                val extension = Uri.parse(fileUrl).path?.substringAfterLast(".", "mp3") ?: "mp3"
                val newMusicFile = fileCacheService.getFile(fileUrl, extension)
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
                Timber.d("Debug message: $message")
            }
        }

        private suspend fun fetchNarrativeRules() = remoteConfig.getNarrativeRules()

        private suspend fun startChapter(act: ActContent) =
            executeRequest {
                setNarrativeProcessingStatus(true)

                val lastChapter = act.chapters.lastOrNull()
                if (lastChapter?.isComplete(fetchNarrativeRules())?.not() == true) {
                    actUseCase.updateAct(act.data.copy(currentChapterId = lastChapter.data.id))
                    throw IllegalArgumentException("Chapter is already set at this act")
                }
                chapterUseCase.saveChapter(Chapter(actId = act.data.id))
            }

        private suspend fun updateChapter(
            saga: SagaContent,
            chapter: ChapterContent,
        ) = executeRequest {
            emitMilestone(SagaMilestone.Loading)
            var generated: GeneratedContent<Chapter>? = null
            val contextString = "Synthesizing chapter progression and weaving plot threads..."
            val style = genreConfigService.conversationBlueprint(saga.data.genre)

            reasoningSynthesizerService
                .synthesizeReasoning(
                    sourceFlow = chapterUseCase.generateChapterStream(saga, chapter),
                    context = contextString,
                    conversationStyle = style,
                ).collect { state ->
                    when (state) {
                        is com.ilustris.sagai.core.ai.StreamingState.Reasoning -> {
                            contentReasoning.value = state.chunk
                        }

                        is com.ilustris.sagai.core.ai.StreamingState.Success -> {
                            generated = state.data
                            contentReasoning.value = null
                        }

                        is com.ilustris.sagai.core.ai.StreamingState.Error -> {
                            contentReasoning.value = null
                            error(state.message)
                        }
                    }
                }

            generated ?: error("Failed to generate chapter")
        }

        override suspend fun reviewWiki(wikiItems: List<Wiki>) {
            val saga = content.value ?: return
            startProcessing {
                wikiUseCase.mergeWikis(saga, wikiItems).onSuccessAsync {
                    updateSnackBar(
                        SnackBarState(
                            message = context.getString(R.string.wiki_updated),
                        ),
                    )
                }
            }
        }

        override suspend fun reviewChapter(chapterContent: ChapterContent) {
            val saga = content.value ?: return
            startProcessing {
                chapterUseCase.reviewChapter(saga, chapterContent)
            }
        }

        override suspend fun reviewEvent(timelineContent: TimelineContent) {
            val saga = content.value ?: return
            startProcessing {
                timelineUseCase
                    .generateTimelineContent(
                        saga,
                        timelineContent,
                    ).onSuccessAsync {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                characterUseCase.findAndSuggestNicknames(saga, timelineContent)
                                characterUseCase.updateCharacterKnowledge(
                                    timelineContent.data,
                                    saga,
                                )
                                if (timelineContent.updatedWikis.isEmpty()) {
                                    wikiUseCase.generateWiki(saga, timelineContent.data)
                                }
                                Timber.i("Nickname analysis completed successfully.")
                            } catch (e: Exception) {
                                Timber.e("Error during nickname analysis: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                        SnackBarState(
                            message =
                                context.getString(
                                    R.string.timeline_updated,
                                    timelineContent.data.title,
                                ),
                        )
                    }
            }
        }

        override suspend fun backupSaga() {
            val currentSaga = content.value ?: return
            Timber.d("Backing up saga ${currentSaga.data.id}")

            val backup = sagaHistoryUseCase.backupSaga(currentSaga)
            Timber.d("backupSaga: backup successfull? ${backup.isSuccess}")
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
                if (lastTimeline?.isComplete(fetchNarrativeRules())?.not() == true) {
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
                chapterUseCase
                    .updateChapter(
                        currentChapter!!.data.copy(
                            currentEventId = null,
                        ),
                    )
                setProcessing(false)
            }

        private suspend fun updateTimeline(
            saga: SagaContent,
            content: TimelineContent,
        ) = executeRequest {
            if (content.isComplete(fetchNarrativeRules())) {
                endTimeline(saga.currentActInfo?.currentChapterInfo)
                error("Timeline already completed")
            } else {
                emitMilestone(SagaMilestone.Loading)
                var generated: GeneratedContent<Timeline>? = null
                val contextString = "Evaluating actions and shaping consequences..."
                val style = genreConfigService.conversationBlueprint(saga.data.genre)

                reasoningSynthesizerService
                    .synthesizeReasoning(
                        sourceFlow = timelineUseCase.generateTimelineStream(saga, content),
                        context = contextString,
                        conversationStyle = style,
                    ).collect { state ->
                        when (state) {
                            is com.ilustris.sagai.core.ai.StreamingState.Reasoning -> {
                                contentReasoning.value = state.chunk
                            }

                            is com.ilustris.sagai.core.ai.StreamingState.Success -> {
                                generated = state.data
                                contentReasoning.value = null
                            }

                            is com.ilustris.sagai.core.ai.StreamingState.Error -> {
                                contentReasoning.value = null
                                error(state.message) // Throws wrapped in RequestResult.Failure
                            }
                        }
                    }

                generated ?: error("Failed to generate timeline")
            }
        }

        private suspend fun createAct(currentSaga: SagaContent) =
            executeRequest {
                val lastAct = currentSaga.acts.lastOrNull()
                if (lastAct?.isComplete(fetchNarrativeRules())?.not() == true) {
                    sagaHistoryUseCase.updateSaga(
                        currentSaga.data.copy(currentActId = lastAct.data.id),
                    )
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
                Timber.d("updating act(${saga.currentActInfo?.data?.id})")
                if (isDebugModeEnabled) {
                    Timber.i("[DEBUG MODE] Generating fake act update data for saga ${saga.data.id}")
                    GeneratedContent(
                        Act(
                            id = currentAct.data.id,
                            title = "Updated Act ${saga.acts.size}",
                            content = "This act was updated in debug mode.",
                            sagaId = saga.data.id,
                        ),
                        "Fake act finished!",
                    )
                } else {
                    emitMilestone(SagaMilestone.Loading)
                    var generated: GeneratedContent<Act>? = null
                    val contextString = "Judging the player's choices and concluding the act..."
                    val style = genreConfigService.conversationBlueprint(saga.data.genre)

                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            sourceFlow = actUseCase.generateActStream(saga, currentAct),
                            context = contextString,
                            conversationStyle = style,
                        ).collect { state ->
                            when (state) {
                                is com.ilustris.sagai.core.ai.StreamingState.Reasoning -> {
                                    contentReasoning.value = state.chunk
                                }

                                is com.ilustris.sagai.core.ai.StreamingState.Success -> {
                                    generated = state.data
                                    contentReasoning.value = null
                                }

                                is com.ilustris.sagai.core.ai.StreamingState.Error -> {
                                    contentReasoning.value = null
                                    error(state.message)
                                }
                            }
                        }

                    generated ?: error("Failed to generate act")
                }
            }

        private suspend fun endAct(saga: SagaContent) =
            executeRequest {
                sagaHistoryUseCase.updateSaga(saga.data.copy(currentActId = null)).asSuccess()
            }

        private suspend fun generateActIntroduction(currentAct: ActContent) =
            executeRequest {
                val saga = content.value!!
                actUseCase.generateActIntroduction(saga, currentAct.data).getSuccess()!!
            }

        private suspend fun generateChapterIntroduction(currentChapter: ChapterContent) =
            executeRequest {
                val saga = content.value!!
                val currentAct = saga.currentActInfo!!
                chapterUseCase
                    .generateChapterIntroduction(saga, currentChapter.data, currentAct)
                    .getSuccess()!!
            }

        private fun observeMilestone() =
            managerScope.launch {
                milestoneUpdate.collectLatest {
                    Timber.d("observeMilestone:\n$it")
                    if (it == null) {
                        Timber.i("observeMilestone: No milestone checking story...")
                        checkNarrativeProgression(content.value)
                        return@collectLatest
                    }
                }
            }

        private fun observeLoading() =
            managerScope.launch {
                narrativeProcessingUiState.collectLatest {
                    Timber.d("observeLoading: $it")
                    if (it.not() && milestoneUpdate.value == null) {
                        checkNarrativeProgression(content.value)
                    }
                }
            }

        override fun checkNarrativeProgression(
            saga: SagaContent?,
            isRetrying: Boolean,
        ) {
            managerScope.launch {
                if (progressionMutex.isLocked) {
                    Timber.i("checkNarrativeProgression: already in progress, skipping.")
                    return@launch
                }

                if (isOnboardingVisible.value) {
                    Timber.i("checkNarrativeProgression: onboarding visible, skipping.")
                    return@launch
                }

                progressionMutex.withLock {
                    val currentSaga = content.value ?: saga ?: return@withLock

                    if (isProcessingNarrative.get() || isProcessing.get()) {
                        Timber.i("Narrative check: currently processing, skipping recursive check.")
                        return@withLock
                    }

                    if (milestoneUpdate.value != null && milestoneUpdate.value !is SagaMilestone.Loading) {
                        Timber.i("checkNarrativeProgression: milestone active waiting for user interaction")
                        return@withLock
                    }

                    Timber.d("Starting narrative progression check #${++progressionCounter}")

                    if (currentSaga.mainCharacter == null && isDebugModeEnabled) {
                        generateCharacter("Main Debug Character").onSuccessAsync { newCharacter ->
                            sagaHistoryUseCase.updateSaga(currentSaga.data.copy(mainCharacterId = newCharacter.id))
                        }
                        return@withLock
                    }

                    val narrativeStep =
                        NarrativeCheck.validateProgression(currentSaga, fetchNarrativeRules())
                    Timber.d("checkNarrativeProgression: Step ${narrativeStep.javaClass.simpleName}")

                    if (narrativeStep == NarrativeStep.NoActionNeeded) {
                        setProcessing(false)
                        return@withLock
                    }

                    var action: RequestResult<Any>? = null
                    startProcessing {
                        action =
                            when (narrativeStep) {
                                NarrativeStep.StartAct -> {
                                    createAct(currentSaga)
                                }

                                is NarrativeStep.GenerateTimeLine,
                                is NarrativeStep.GenerateChapter,
                                is NarrativeStep.GenerateAct,
                                is NarrativeStep.GenerateSagaEnding,
                                is NarrativeStep.GenerateActIntroduction,
                                is NarrativeStep.GenerateChapterIntroduction,
                                is NarrativeStep.StartTimeline,
                                -> {
                                    Timber.i("checkNarrativeProgression: Milestone ${narrativeStep.javaClass.simpleName} detected. Waiting for user interaction.")
                                    setProcessing(false)
                                    return@startProcessing
                                }

                                is NarrativeStep.StartChapter -> {
                                    startChapter(narrativeStep.act)
                                }

                                is NarrativeStep.EndTimeLine -> {
                                    endTimeline(narrativeStep.currentChapterContent)
                                }

                                NarrativeStep.NoActionNeeded -> {
                                    skipNarrative()
                                }
                            }
                    }

                    action
                        ?.onSuccessAsync {
                            validatePostAction(currentSaga, narrativeStep, action!!.success)
                        }?.onFailureAsync {
                            emitMilestone(null)
                            if (isRetrying) {
                                updateSnackBar(
                                    snackBar(context.getString(R.string.unexpected_error)) {
                                        action { revaluateSaga() }
                                    },
                                )
                            } else {
                                checkNarrativeProgression(currentSaga, true)
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
        ) = startProcessing {
            generateTimelineContent(timelineContent.data, saga)
        }

        private suspend fun skipNarrative() =
            executeRequest {
                Timber.i("skipNarrative: No action needed skipping narrative")
            }

        override val isMilestoneActive = MutableStateFlow(false)

        override fun dismissMilestone() {
            isMilestoneActive.value = false
            milestoneUpdate.value = null
        }

        override suspend fun continueMilestone() {
            val milestone =
                milestoneUpdate.value ?: run {
                    dismissMilestone()
                    return
                }

            // Capture saga state BEFORE any operations to avoid race conditions
            val saga =
                content.value ?: run {
                    dismissMilestone()
                    return
                }

            // Prevent restarting if already processing
            if (isProcessing.get()) {
                Timber.d("Already processing milestone, ignoring continue request")
                dismissMilestone()
                return
            }

            Timber.d("User continued from milestone: ${milestone.javaClass.simpleName}")

            startProcessing {
                when (milestone) {
                    is SagaMilestone.Introduction -> {
                        doNothing()
                    }

                    is SagaMilestone.NewEvent -> {
                        generateTimelineContent(milestone.timeline, saga)
                    }

                    is SagaMilestone.ChapterFinished -> {
                        handleChapterPostActions(milestone.chapter, saga)
                    }

                    is SagaMilestone.ActFinished -> {
                        endAct(saga)
                    }

                    else -> {
                        doNothing()
                    }
                }

                delay(milestone.delay)
                dismissMilestone()
            }
        }

        private fun emitMilestone(milestone: SagaMilestone?) =
            CoroutineScope(Dispatchers.Main.immediate).launch {
                if (milestone != null && milestone !is SagaMilestone.Loading) {
                    isMilestoneActive.value = true
                }
                milestoneUpdate.emit(milestone)
            }

        private suspend fun startProcessing(block: suspend () -> Unit) {
            if (isProcessing.get().not()) {
                setProcessing(true)
            }
            block()
            setProcessing(false)
        }

        private suspend fun validatePostAction(
            saga: SagaContent,
            step: NarrativeStep,
            result: RequestResult.Success<Any>,
        ) {
            try {
                if (isMilestoneActive.value) {
                    Timber.d("Waiting for milestone dismissal...")
                    isMilestoneActive.first { !it }
                    Timber.d("Milestone dismissed, resuming narrative.")
                    proceedWithPostAction(saga, step, result)
                } else {
                    proceedWithPostAction(saga, step, result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                setNarrativeProcessingStatus(false)
                milestoneUpdate.emit(null)
            }
        }

        private suspend fun proceedWithPostAction(
            saga: SagaContent,
            step: NarrativeStep,
            result: RequestResult.Success<Any>,
        ) {
            Timber.d("validatePostAction: performing next step $step")
            when (step) {
                is NarrativeStep.StartAct -> {
                    (result.value as? Act)?.let { data ->
                        startProcessing {
                            sagaHistoryUseCase.updateSaga(
                                saga.data.copy(currentActId = data.id),
                            )
                        }
                        backupSaga()
                    }
                }

                is NarrativeStep.GenerateActIntroduction -> {
                    (result.value as? Act)?.let { act ->
                        emitMilestone(
                            SagaMilestone.Introduction(
                                type = IntroductionType.ACT,
                                titleText = act.title,
                                introduction = act.introduction,
                                number = saga.actNumber(act).toRoman(),
                            ),
                        )
                    }
                }

                is NarrativeStep.StartChapter -> {
                    val currentAct = saga.currentActInfo!!
                    (result.value as? Chapter)?.let { chapter ->
                        startProcessing {
                            if (currentAct.currentChapterInfo != null) error("Chapter already set")
                            actUseCase.updateAct(
                                currentAct.data.copy(currentChapterId = chapter.id),
                            )
                        }
                    }
                }

                is NarrativeStep.GenerateChapterIntroduction -> {
                    (result.value as? Chapter)?.let { chapterUpdate ->
                        emitMilestone(
                            SagaMilestone.Introduction(
                                type = IntroductionType.CHAPTER,
                                titleText = chapterUpdate.title,
                                introduction = chapterUpdate.introduction,
                                number = saga.chapterNumber(chapterUpdate).toRoman(),
                            ),
                        )
                    }
                }

                is NarrativeStep.StartTimeline -> {
                    (result.value as? Timeline)?.let {
                        chapterUseCase.updateChapter(
                            saga.currentActInfo!!.currentChapterInfo!!.data.copy(
                                currentEventId = (it).id,
                            ),
                        )
                        startProcessing {
                            val objective =
                                timelineUseCase
                                    .getTimelineObjective(content.value!!, it)
                                    .getSuccess()

                            objective?.let {
                                emitMilestone(SagaMilestone.CurrentObjective(it))
                            } ?: run {
                                dismissMilestone()
                            }
                        }
                    }
                }

                is NarrativeStep.GenerateTimeLine -> {
                    val generatedContent =
                        result.value as? GeneratedContent<Timeline>
                    val timeline = generatedContent?.data ?: result.value as? Timeline
                    val message = generatedContent?.finalMessage
                    timeline?.let { t ->
                        updateSnackBar(
                            SnackBarState(
                                message =
                                    context.getString(
                                        R.string.timeline_updated,
                                        t.title,
                                    ),
                            ),
                        )

                        val mascotIcon =
                            emotionalUseCase
                                .getEmotionalMascot(
                                    saga,
                                    saga.findTimeline(t.id)!!,
                                ).getSuccess()

                        emitMilestone(SagaMilestone.NewEvent(t, mascotIcon, message))
                        endTimeline(saga.currentActInfo?.currentChapterInfo)
                    }
                }

                is NarrativeStep.GenerateChapter -> {
                    val generatedContent =
                        result.value as? GeneratedContent<Chapter>
                    val chapter = generatedContent?.data ?: result.value as? Chapter
                    val message = generatedContent?.finalMessage
                    chapter?.let { c ->
                        emitMilestone(SagaMilestone.ChapterFinished(c, message))
                    }
                }

                is NarrativeStep.GenerateAct -> {
                    val generatedContent =
                        result.value as? GeneratedContent<Act>
                    val act = generatedContent?.data ?: result.value as? Act
                    val message = generatedContent?.finalMessage
                    act?.let { a ->
                        emitMilestone(SagaMilestone.ActFinished(a, message))
                    }
                }

                is NarrativeStep.NoActionNeeded -> {
                    checkObjective()
                }

                else -> {
                    setNarrativeProcessingStatus(false)
                }
            }
        }

        private suspend fun clearInvalidContent() {
            val saga = content.value ?: return
            val act = saga.currentActInfo ?: return
            val invalidActs =
                saga.acts.filter {
                    it.data.id != act.data.id && it.isComplete(fetchNarrativeRules()).not()
                }
            invalidActs.forEach {
                actUseCase.deleteAct(it.data)
            }

            act.let { currentAct ->
                val rules = fetchNarrativeRules()
                val currentChapter = currentAct.currentChapterInfo
                val invalidChapters =
                    currentAct.chapters.filter {
                        it.data.id != currentChapter?.data?.id && it.isComplete(rules).not()
                    }
                invalidChapters.forEach {
                    chapterUseCase.deleteChapter(it.data)
                }

                currentChapter?.let {
                    val currentEvent = it.currentEventInfo

                    val invalidEvents =
                        it.events.filter {
                            it.data.id != currentEvent?.data?.id &&
                                it
                                    .isComplete(
                                        fetchNarrativeRules(),
                                    ).not()
                        }

                    Timber.w("Invalid events -> ${invalidEvents.size} ")

                    invalidEvents.forEach {
                        timelineUseCase.deleteTimeline(it.data)
                    }
                }
            }
        }

        private suspend fun checkObjective(showMilestone: Boolean = false) =
            executeRequest {
                val saga = content.value ?: return@executeRequest null
                saga.currentActInfo ?: return@executeRequest null

                clearInvalidContent()
            }

        override suspend fun getCurrentObjective(sceneSummary: SceneSummary) {
            val saga = content.value ?: return
            val event = saga.getCurrentTimeLine() ?: return
            if (event.data.currentObjective.isNullOrEmpty()) {
                startProcessing {
                    val updatedTimeline =
                        event.data.copy(
                            currentObjective = sceneSummary.immediateObjective,
                        )
                    timelineUseCase.updateTimeline(updatedTimeline)
                    showObjective()
                }
            }
        }

        private suspend fun generateTimelineContent(
            timeline: Timeline,
            saga: SagaContent,
        ) {
            saga.flatEvents().find { it.data.id == timeline.id }?.let { content ->
                timelineUseCase.generateTimelineContent(saga, content.copy(data = timeline))

                characterUseCase.updateCharacterKnowledge(timeline, saga)

                updateSnackBar(
                    snackBar(
                        context.getString(R.string.timeline_generated_successfully, timeline.title),
                    ),
                )
            }
        }

        private suspend fun handleChapterPostActions(
            chapter: Chapter,
            saga: SagaContent,
        ) {
            val chapterContent =
                saga
                    .flatChapters()
                    .find { it.data.id == chapter.id }!!
                    .copy(data = chapter)

            CoroutineScope(Dispatchers.IO).launch {
                chapterUseCase.generateChapterCover(chapterContent, saga)
            }
            chapterUseCase.reviewChapter(saga, chapterContent)
            endChapter(saga.currentActInfo)
        }

        private suspend fun createEndingMessage(saga: SagaContent) =
            executeRequest {
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
                    emotionalUseCase.generateEmotionalConclusion(saga).getSuccess()
                emitMilestone(null)
                sagaHistoryUseCase
                    .updateSaga(
                        saga.data.copy(
                            endMessage = endingMessage,
                            isEnded = true,
                            endedAt = System.currentTimeMillis(),
                            emotionalReview = emotionalEnding,
                        ),
                    )
            }

        override suspend fun generateCharacter(
            description: String,
            sceneSummary: SceneSummary?,
        ): RequestResult<Character> =

            executeRequest {
                setProcessing(true)
                emitMilestone(content.value?.data?.let { SagaMilestone.Loading })
                try {
                    val currentSaga = content.value!!
                    if (isDebugModeEnabled) {
                        Timber.i("[DEBUG MODE] Generating fake character for saga ${currentSaga.data.id}")
                        val fakeCharacter =
                            Character(
                                name = "Fake Character: $description",
                                backstory = "Generated in debug mode.",
                                sagaId = currentSaga.data.id,
                                details = Details(),
                                profile = CharacterProfile(),
                            )
                        characterUseCase.insertCharacter(fakeCharacter)
                        emitMilestone(SagaMilestone.NewCharacter(fakeCharacter))
                        fakeCharacter
                    } else {
                        var generated: GeneratedContent<Character>? =
                            null
                        val contextString = "Evaluating potential characters for the story..."
                        val style = genreConfigService.conversationBlueprint(currentSaga.data.genre)

                        reasoningSynthesizerService
                            .synthesizeReasoning(
                                sourceFlow =
                                    characterUseCase.generateCharacterStream(
                                        currentSaga,
                                        description,
                                        sceneSummary ?: _sceneSummary.value,
                                    ),
                                context = contextString,
                                conversationStyle = style,
                            ).collect { state ->
                                when (state) {
                                    is com.ilustris.sagai.core.ai.StreamingState.Reasoning -> {
                                        contentReasoning.value = state.chunk
                                    }

                                    is com.ilustris.sagai.core.ai.StreamingState.Success -> {
                                        generated = state.data
                                        contentReasoning.value = null
                                    }

                                    is com.ilustris.sagai.core.ai.StreamingState.Error -> {
                                        contentReasoning.value = null
                                        error(state.message)
                                    }
                                }
                            }

                        val generatedCharacter =
                            generated?.data ?: error("Failed to generate character")

                        updateSnackBar(
                            snackBar(
                                context.getString(
                                    R.string.new_character_message,
                                    generatedCharacter.name,
                                ),
                            ),
                        )

                        emitMilestone(
                            SagaMilestone.NewCharacter(
                                generatedCharacter,
                                generated?.finalMessage,
                            ),
                        )

                        generatedCharacter
                    }
                } catch (e: Exception) {
                    emitMilestone(null)
                    throw e
                } finally {
                    setProcessing(false)
                }
            }

        override suspend fun generateCharacterImage(character: Character): RequestResult<Character> =
            executeRequest {
                setProcessing(true)
                try {
                    val currentSaga = content.value!!
                    if (isDebugModeEnabled) {
                        Timber.i("[DEBUG MODE] Skipping image generation for character ${character.name}")
                        emitMilestone(null)
                        character
                    } else {
                        val result =
                            characterUseCase
                                .generateCharacterImage(
                                    character,
                                    currentSaga.data,
                                ).success.value.first
                        emitMilestone(null)
                        result
                    }
                } catch (e: Exception) {
                    emitMilestone(null)
                    throw e
                } finally {
                    setProcessing(false)
                }
            }

        suspend fun generateEnding(saga: SagaContent) = createEndingMessage(saga)
    }
