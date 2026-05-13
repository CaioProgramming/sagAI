package com.ilustris.sagai.features.saga.chat.data.manager

import android.content.Context
import android.net.Uri
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileCacheService
import com.ilustris.sagai.core.file.ImageHelper
import com.ilustris.sagai.core.media.SoundFxService
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
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaEnding
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.currentActInfo
import com.ilustris.sagai.features.home.data.model.currentChapterInfo
import com.ilustris.sagai.features.home.data.model.currentEventInfo
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.newsaga.data.model.vibrationPattern
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
import timber.log.Timber
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
        private val remoteConfig: RemoteConfigService,
        private val backupService: BackupService,
        private val imageHelper: ImageHelper,
        private val messageUseCase: MessageUseCase,
        private val genreConfigService: GenreConfigService,
        private val genreVisualConfigService: GenreVisualConfigService,
        private val soundFxService: SoundFxService,
        private val reasoningSynthesizerService: com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService,
        private val messageDao: com.ilustris.sagai.features.saga.datasource.MessageDao,
        @ApplicationContext
        private val context: Context,
    ) : SagaContentManager {
        override val contentReasoning = MutableStateFlow<String?>(null)
        override val content = MutableStateFlow<SagaMetadata?>(null)
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
        override val replySfxFile = MutableStateFlow<File?>(null)

        private val isProcessingNarrative = AtomicBoolean(false)
        private val _narrativeProcessingUiState = MutableStateFlow(false)
        override val narrativeProcessingUiState: StateFlow<Boolean> =
            _narrativeProcessingUiState.asStateFlow()

        private var sagaJob: kotlinx.coroutines.Job? = null
        private var loadingObserverJob: kotlinx.coroutines.Job? = null
        private var milestoneObserverJob: kotlinx.coroutines.Job? = null
        private var reasoningObserverJob: kotlinx.coroutines.Job? = null

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
                            val timeline = pendingAdvance.timeline
                            updateTimeline(
                                currentSaga,
                                timeline,
                            )
                        }

                        is PendingAdvance.NewChapter -> {
                            updateChapter(
                                currentSaga,
                                pendingAdvance.chapter,
                            )
                        }

                        is PendingAdvance.NewAct -> {
                            val act = pendingAdvance.act
                            updateAct(act)
                        }

                        is PendingAdvance.StartAct -> {
                            createAct(currentSaga)
                        }

                        is PendingAdvance.NewActIntroduction -> {
                            val act = pendingAdvance.act
                            generateActIntroduction(act)
                        }

                        is PendingAdvance.NewChapterIntroduction -> {
                            val chapter = pendingAdvance.chapter
                            generateChapterIntroduction(chapter)
                        }

                        is PendingAdvance.StartChapter -> {
                            val act = pendingAdvance.act
                            startChapter(act)
                        }

                        is PendingAdvance.StartStory -> {
                            val chapter = pendingAdvance.chapter
                            startTimeline(chapter)
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

                            is PendingAdvance.StartAct -> {
                                NarrativeStep.StartAct
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

                            is PendingAdvance.StartChapter -> {
                                NarrativeStep.StartChapter(pendingAdvance.act)
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
                    checkNarrativeProgression(currentSaga, true)
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
            if (content.value
                    ?.data
                    ?.id
                    ?.toString() == sagaId
            ) {
                Timber.d("loadSaga: Saga $sagaId already loaded, skipping full load.")
                if (milestoneUpdate.value == null) {
                    checkNarrativeProgression(content.value)
                }
                return
            }
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
                        if (reasoningObserverJob == null || reasoningObserverJob?.isActive == false) {
                            reasoningObserverJob = observeReasoning()
                        }
                        managerScope.launch {
                            isOnboardingVisible.collect { isVisible ->
                                if (!isVisible) {
                                    checkNarrativeProgression(content.value)
                                }
                            }
                        }
                        sagaHistoryUseCase
                            .getSagaMetadata(sagaId.toInt())
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
                                val previousTimelineId =
                                    previousSaga?.getCurrentTimeLine()?.data?.id ?: -1
                                val currentTimelineId = saga.getCurrentTimeLine()?.data?.id ?: -1
                                val previousMessageCount =
                                    messageDao.getMessagesCount(sagaId.toInt()).first()
                                val currentMessageCount =
                                    messageDao.getMessagesCount(sagaId.toInt()).first()

                                val sceneChanged =
                                    previousSaga?.getCurrentTimeLine()?.data?.sceneSummary != saga.getCurrentTimeLine()?.data?.sceneSummary

                                if (previousSaga != null &&
                                    previousSaga.data.id == saga.data.id &&
                                    (previousSaga.data.playTimeMs != saga.data.playTimeMs || sceneChanged) &&
                                    previousMessageCount == currentMessageCount &&
                                    previousTimelineId == currentTimelineId
                                ) {
                                    Timber.d(
                                        "Saga update was subtle (playtime: ${previousSaga.data.playTimeMs != saga.data.playTimeMs}, scene: $sceneChanged). Skipping narrative check.",
                                    )
                                    saga.getCurrentTimeLine()?.data?.sceneSummary?.let {
                                        _sceneSummary.value = it
                                    }
                                    content.value = saga
                                    return@collectLatest
                                }

                                content.value = saga
                                saga.getCurrentTimeLine()?.data?.sceneSummary?.let {
                                    _sceneSummary.value = it
                                }

                                getAmbienceMusic(saga)
                                getReplySfx(saga)

                                checkMessageNotifications(
                                    previousSaga,
                                    saga,
                                    previousMessageCount,
                                    currentMessageCount,
                                )

                                if (previousMessageCount != currentMessageCount || previousTimelineId != currentTimelineId ||
                                    previousSaga == null
                                ) {
                                    checkNarrativeProgression(saga)
                                }

                                validateCharacters(saga)

                                if (previousSaga == null) {
                                    if (saga.data.isEnded.not()) {
                                        saga.getCurrentTimeLine()?.data?.sceneSummary?.let {
                                            emitMilestone(SagaMilestone.Loading)
                                            emitMilestone(
                                                SagaMilestone.Introduction(
                                                    type = IntroductionType.RESUME,
                                                    titleText = emptyString(),
                                                    introduction =
                                                        it.quote
                                                            ?: emptyString(),
                                                    number =
                                                        saga
                                                            .chapterNumber(
                                                                saga.currentChapterInfo?.data?.id
                                                                    ?: -1,
                                                            ).toRoman(),
                                                    sceneSummary = it,
                                                ),
                                            )
                                        } ?: run {
                                            emitMilestone(null)
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

        private suspend fun validateCharacters(saga: SagaMetadata) {
            withContext(Dispatchers.IO) {
                characterUseCase
                    .getCharactersBySaga(saga.data.id)
                    .first()
                    .filter { it.data.smartZoom == null && it.data.image.isNotEmpty() }
                    .forEach {
                        characterUseCase.createSmartZoom(it.data)
                    }
            }
        }

        private suspend fun checkMessageNotifications(
            previousSaga: SagaMetadata?,
            saga: SagaMetadata,
            previousMessageCount: Int,
            currentMessageCount: Int,
        ) {
            if (previousSaga != null &&
                currentMessageCount > previousMessageCount
            ) {
                val lastMessage = messageDao.getLastMessageWithContent(saga.data.id) ?: return
                val charIcon =
                    imageHelper
                        .getImageBitmap(lastMessage.character?.image, true)
                        .getSuccess()
                if (lastMessage.message.senderType == SenderType.CHARACTER) {
                    playSoundFx()
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

        private fun playSoundFx() {
            val selectedGenre = content.value?.data?.genre ?: return
            managerScope.launch {
                delay(300)
                val visualConfig = genreVisualConfigService.getVisualConfig(selectedGenre)
                val hapticPattern = selectedGenre.vibrationPattern(visualConfig)
                soundFxService.playWithHaptics(hapticPattern)
            }
        }

        private suspend fun getAmbienceMusic(saga: SagaMetadata) {
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

        private suspend fun getReplySfx(saga: SagaMetadata) {
            val genre = saga.data.genre
            withContext(Dispatchers.IO) {
                val sfxMap = remoteConfig.getJson<Map<String, String>>("reply_sfx_config")

                if (sfxMap.isNullOrEmpty()) {
                    Timber.w("getReplySfx: reply_sfx_config absent or empty")
                    return@withContext
                }

                val finalUrl = sfxMap[genre.name] ?: sfxMap["DEFAULT"]
                if (finalUrl.isNullOrEmpty()) {
                    Timber.w("getReplySfx: No URL found for ${genre.name} or DEFAULT")
                    if (replySfxFile.value != null) replySfxFile.emit(null)
                    return@withContext
                }

                val extension = Uri.parse(finalUrl).path?.substringAfterLast(".", "mp3") ?: "mp3"
                val sfxFile = fileCacheService.getFile(finalUrl, extension)

                if (sfxFile?.absolutePath != replySfxFile.value?.absolutePath) {
                    replySfxFile.emit(sfxFile)
                    sfxFile?.let {
                        soundFxService.prepare(it)
                        Timber.d("getReplySfx: SFX updated for ${genre.name} — ${it.name}")
                    }
                } else if (sfxFile == null && replySfxFile.value != null) {
                    replySfxFile.emit(null)
                }
            }
        }

        private suspend fun sendDebugMessage(message: String) {
            val currentSaga = content.value
            val timeLine = currentSaga?.currentEventInfo
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
                val currentSaga = getSagaContent() ?: error("Saga content not available")
                val latestAct =
                    currentSaga.acts.find { it.data.id == act.data.id } ?: act

                val lastChapter = latestAct.chapters.lastOrNull()
                if (lastChapter?.isComplete(fetchNarrativeRules())?.not() == true) {
                    actUseCase.updateAct(latestAct.data.copy(currentChapterId = lastChapter.data.id))
                    throw IllegalArgumentException("Chapter is already set at this act")
                }
                chapterUseCase.saveChapter(Chapter(actId = latestAct.data.id))
            }

        private suspend fun updateChapter(
            saga: SagaMetadata,
            chapter: ChapterContent,
        ) = executeRequest {
            dismissMilestone()
            var generated: GeneratedContent<Chapter>? = null
            val contextString = "Synthesizing chapter progression and weaving plot threads..."
            val style = genreConfigService.conversationBlueprint(saga.data.genre)

            reasoningSynthesizerService
                .synthesizeReasoning(
                    sourceFlow = chapterUseCase.synthesizeChapterEvolutionStream(chapter.data.id),
                    context = contextString,
                    conversationStyle = style,
                    genre = saga.data.genre.name,
                ).collect { state ->
                    when (state) {
                        is StreamingState.Reasoning -> {
                            contentReasoning.value = state.chunk
                        }

                        is StreamingState.Success -> {
                            generated = state.data
                            contentReasoning.value = null
                        }

                        is StreamingState.Error -> {
                            contentReasoning.value = null
                            error(state.message)
                        }
                    }
                }

            generated ?: error("Failed to generate chapter synthesis")
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

        override suspend fun reviewChapter(chapterContent: ChapterMetadata) {
            startProcessing {
                chapterUseCase.reviewChapter(chapterContent.data.id)
            }
        }

        override suspend fun reviewEvent(timelineContent: TimelineMetadata) {
            val saga = content.value ?: return
            startProcessing {
                timelineUseCase.generateFullLoreUpdate(
                    saga,
                    timelineContent.data,
                )
            }
        }

        override suspend fun backupSaga() {
            val sagaMetadata = content.value ?: return
            Timber.d("Backing up saga ${sagaMetadata.data.id}")

            val fullSaga = sagaHistoryUseCase.getSagaById(sagaMetadata.data.id).first() ?: return
            val backup = sagaHistoryUseCase.backupSaga(fullSaga)
            Timber.d("backupSaga: backup successfull? ${backup.isSuccess}")
        }

        override suspend fun enableBackup(uri: Uri?) {
            uri?.let {
                backupService.enableBackup(it)
            } ?: run {
                snackBarUpdate.emit(snackBar(context.getString(R.string.backup_disabled)))
            }
        }

        private suspend fun endChapter(currentAct: ActMetadata?) =
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

        private suspend fun endTimeline(currentChapter: ChapterContent) =
            executeRequest {
                chapterUseCase
                    .updateChapter(
                        currentChapter.data.copy(
                            currentEventId = null,
                        ),
                    )
                setProcessing(false)
            }

        private suspend fun updateTimeline(
            saga: SagaMetadata,
            content: TimelineContent,
        ) = executeRequest {
            messageDao.getMessagesCount(saga.data.id).first()
            if (content.isComplete(fetchNarrativeRules())) {
                endTimeline(
                    getSagaContent()?.currentActInfo?.currentChapterInfo
                        ?: error("Current chapter not found"),
                )
                error("Timeline already completed")
            } else {
                dismissMilestone()
                var generated: GeneratedContent<Timeline>? = null
                val contextString = "Evaluating actions and shaping consequences..."
                val style = genreConfigService.conversationBlueprint(saga.data.genre)

                reasoningSynthesizerService
                    .synthesizeReasoning(
                        sourceFlow =
                            timelineUseCase.generateFullLoreUpdateStream(
                                saga,
                                content.data,
                            ),
                        context = contextString,
                        conversationStyle = style,
                        genre = saga.data.genre.name,
                    ).collect { state ->
                        when (state) {
                            is StreamingState.Reasoning -> {
                                contentReasoning.value = state.chunk
                            }

                            is StreamingState.Success -> {
                                generated = state.data
                                contentReasoning.value = null
                            }

                            is StreamingState.Error -> {
                                contentReasoning.value = null
                                error(state.message)
                            }
                        }
                    }

                generated ?: error("Failed to generate timeline update")
            }
        }

        private suspend fun createAct(currentSaga: SagaMetadata) =
            executeRequest {
                val saga = content.value ?: currentSaga
                messageDao.getMessagesCount(saga.data.id).first()
                val lastAct = saga.acts.lastOrNull()
                if (lastAct?.isComplete(fetchNarrativeRules())?.not() == true) {
                    sagaHistoryUseCase.updateSaga(
                        saga.data.copy(currentActId = lastAct.data.id),
                    )
                    error("Act is already set at this saga")
                }
                actUseCase
                    .saveAct(
                        Act(
                            sagaId = saga.data.id,
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
                    dismissMilestone()
                    var generated: GeneratedContent<Act>? = null
                    val contextString = "Judging the player's choices and concluding the act..."
                    val style = genreConfigService.conversationBlueprint(saga.data.genre)

                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            sourceFlow =
                                actUseCase.synthesizeActEvolutionStream(
                                    getSagaContent()!!,
                                    currentAct,
                                ),
                                context = contextString,
                            conversationStyle = style,
                            genre = saga.data.genre.name,
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Reasoning -> {
                                    contentReasoning.value = state.chunk
                                }

                                is StreamingState.Success -> {
                                    generated = state.data
                                    contentReasoning.value = null
                                }

                                is StreamingState.Error -> {
                                    contentReasoning.value = null
                                    error(state.message)
                                }
                            }
                        }

                    generated ?: error("Failed to generate act synthesis")
                }
            }

        private suspend fun endAct(saga: SagaMetadata) =
            executeRequest {
                sagaHistoryUseCase.updateSaga(saga.data.copy(currentActId = null)).asSuccess()
            }

        private suspend fun generateActIntroduction(currentAct: ActContent) =
            executeRequest {
                val saga = content.value!!
                var finalAct: GeneratedContent<Act>? = null
                actUseCase
                    .generateActIntroductionStream(saga, currentAct.data)
                    .let { flow ->
                        reasoningSynthesizerService.synthesizeReasoning(
                            sourceFlow = flow,
                            context = "Generating act introduction for ${saga.data.title}",
                            conversationStyle = genreConfigService.conversationBlueprint(saga.data.genre),
                            genre = saga.data.genre.name,
                        )
                    }.collect { state ->
                        when (state) {
                            is StreamingState.Reasoning -> {
                                contentReasoning.value = state.chunk
                            }

                            is StreamingState.Success -> {
                                finalAct = state.data
                            }

                            is StreamingState.Error -> {
                                throw Exception(state.message)
                            }
                        }
                    }
                contentReasoning.value = null
                finalAct!!
            }

        private suspend fun generateChapterIntroduction(currentChapter: ChapterContent) =
            executeRequest {
                val saga = content.value!!
                var finalChapter: GeneratedContent<Chapter>? = null
                chapterUseCase
                    .generateChapterIntroductionStream(currentChapter.data.id)
                    .let { flow ->
                        reasoningSynthesizerService.synthesizeReasoning(
                            sourceFlow = flow,
                            context = "Generating chapter introduction for ${currentChapter.data.title}",
                            conversationStyle = genreConfigService.conversationBlueprint(saga.data.genre),
                            genre = saga.data.genre.name,
                        )
                    }.collect { state ->
                        when (state) {
                            is StreamingState.Reasoning -> {
                                contentReasoning.value = state.chunk
                            }

                            is StreamingState.Success -> {
                                finalChapter = state.data
                            }

                            is StreamingState.Error -> {
                                throw Exception(state.message)
                            }
                        }
                    }
                contentReasoning.value = null
                finalChapter!!
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

        private fun observeReasoning() =
            managerScope.launch {
                contentReasoning.collectLatest { reasoning ->
                    if (reasoning != null && milestoneUpdate.value != SagaMilestone.Loading) {
                        emitMilestone(SagaMilestone.Loading)
                    }
                }
            }

        override fun checkNarrativeProgression(
            saga: SagaMetadata?,
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

                    if (milestoneUpdate.value != null && milestoneUpdate.value !is SagaMilestone.Loading &&
                        milestoneUpdate.value !is SagaMilestone.Introduction
                    ) {
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

                    messageDao.getMessagesCount(currentSaga.data.id).first()

                    val narrativeStep =
                        NarrativeCheck.validateProgression(
                            getSagaContent()!!,
                            fetchNarrativeRules()
                        )
                    Timber.d("checkNarrativeProgression: Step ${narrativeStep.javaClass.simpleName}")

                    if (narrativeStep == NarrativeStep.NoActionNeeded) {
                        setProcessing(false)
                        return@withLock
                    }

                    var action: RequestResult<Any>? = null
                    startProcessing {
                        action =
                            when (narrativeStep) {
                                is NarrativeStep.GenerateTimeLine,
                                is NarrativeStep.GenerateChapter,
                                is NarrativeStep.GenerateAct,
                                is NarrativeStep.GenerateSagaEnding,
                                is NarrativeStep.GenerateActIntroduction,
                                is NarrativeStep.GenerateChapterIntroduction,
                                is NarrativeStep.StartTimeline,
                                -> {
                                    Timber.i(
                                        "checkNarrativeProgression: Milestone ${narrativeStep.javaClass.simpleName} detected. Waiting for user interaction.",
                                    )
                                    setProcessing(false)
                                    return@startProcessing
                                }

                                is NarrativeStep.StartAct,
                                is NarrativeStep.StartChapter,
                                -> {
                                    Timber.i(
                                        "checkNarrativeProgression: ${narrativeStep.javaClass.simpleName} detected. Waiting for user interaction.",
                                    )
                                    setProcessing(false)
                                    return@startProcessing
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
                            validatePostAction(currentSaga, narrativeStep, action.success)
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
            saga: SagaMetadata,
            timelineContent: TimelineMetadata,
        ) {
            startProcessing {
                timelineUseCase
                    .generateFullLoreUpdateStream(saga, timelineContent.data)
                    .collect { state ->
                        handleStreamingState(state)
                    }
            }
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

            dismissMilestone()
            when (milestone) {
                is SagaMilestone.Introduction -> {
                    doNothing()
                }

                is SagaMilestone.NewEvent -> {
                    getSagaContent()?.currentActInfo?.currentChapterInfo?.let {
                        endTimeline(it)
                    }
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
        }

        private fun emitMilestone(milestone: SagaMilestone?) =
            CoroutineScope(Dispatchers.Main.immediate).launch {
                if (milestone != null && milestone !is SagaMilestone.Loading) {
                    isMilestoneActive.value = true
                    playSoundFx()
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
            saga: SagaMetadata,
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
            saga: SagaMetadata,
            step: NarrativeStep,
            result: RequestResult.Success<Any>,
        ) {
            Timber.d("validatePostAction: performing next step $step")
            when (step) {
                is NarrativeStep.StartAct -> {
                    (result.value as? Act)?.let { data ->
                        startProcessing {
                            val currentSaga = content.value ?: saga
                            if (currentSaga.data.currentActId != saga.data.currentActId && currentSaga.data.currentActId != null) {
                                Timber.w("StartAct: Saga currentActId already updated. Skipping.")
                                return@startProcessing
                            }
                            sagaHistoryUseCase.updateSaga(
                                saga.data.copy(currentActId = data.id),
                            )
                        }
                        backupSaga()
                    }
                }

                is NarrativeStep.GenerateActIntroduction -> {
                    val generatedContent = result.value as? GeneratedContent<Act>
                    val act = generatedContent?.data ?: result.value as? Act
                    val message = generatedContent?.finalMessage
                    act?.let { a ->
                        emitMilestone(
                            SagaMilestone.Introduction(
                                type = IntroductionType.ACT,
                                titleText = a.title,
                                introduction = message ?: act.introduction,
                                number = saga.actNumber(a.id).toRoman(),
                                messageText = message,
                            ),
                        )
                    }
                }

                is NarrativeStep.StartChapter -> {
                    val currentAct = content.value?.currentActInfo ?: saga.currentActInfo!!
                    (result.value as? Chapter)?.let { chapter ->
                        startProcessing {
                            if (saga.currentChapterInfo != null && saga.currentChapterInfo!!.data.id != chapter.id) {
                                Timber.w("Chapter already set and different from generated one. Skipping update.")
                                return@startProcessing
                            }
                            actUseCase.updateAct(
                                currentAct.data.copy(currentChapterId = chapter.id),
                            )
                        }
                    } ?: run {
                        dismissMilestone()
                    }
                }

                is NarrativeStep.GenerateChapterIntroduction -> {
                    val generatedContent = result.value as? GeneratedContent<Chapter>
                    val chapterUpdate = generatedContent?.data ?: result.value as? Chapter
                    val message = generatedContent?.finalMessage
                    chapterUpdate?.let { c ->
                        emitMilestone(
                            SagaMilestone.Introduction(
                                type = IntroductionType.CHAPTER,
                                titleText = c.title,
                                introduction = message ?: emptyString(),
                                number = saga.chapterNumber(c.id).toRoman(),
                                messageText = message,
                            ),
                        )
                    } ?: run {
                        dismissMilestone()
                    }
                }

                is NarrativeStep.StartTimeline -> {
                    (result.value as? Timeline)?.let {
                        chapterUseCase.updateChapter(
                            saga.currentChapterInfo!!.data.copy(
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
                    } ?: run {
                        dismissMilestone()
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
                                    saga.data,
                                    saga.findTimeline(t.id)?.data,
                                ).getSuccess()

                        val fullSaga = getSagaContent()
                        fullSaga?.let {
                            emitMilestone(
                                SagaMilestone.NewEvent(
                                    timeline = t,
                                    emotionalMascot = mascotIcon,
                                    messageText = message,
                                    sagaContent = fullSaga,
                                ),
                            )
                        }
                        getSagaContent()?.currentActInfo?.currentChapterInfo?.let {
                            endTimeline(it)
                        }
                    } ?: run {
                        dismissMilestone()
                    }
                }

                is NarrativeStep.GenerateChapter -> {
                    val generatedContent =
                        result.value as? GeneratedContent<Chapter>
                    val chapter = generatedContent?.data ?: result.value as? Chapter
                    val message = generatedContent?.finalMessage
                    chapter?.let { c ->
                        emitMilestone(SagaMilestone.ChapterFinished(c, message, getSagaContent()!!))
                    } ?: run {
                        dismissMilestone()
                    }
                }

                is NarrativeStep.GenerateAct -> {
                    val generatedContent =
                        result.value as? GeneratedContent<Act>
                    val act = generatedContent?.data ?: result.value as? Act
                    val message = generatedContent?.finalMessage
                    act?.let { a ->
                        emitMilestone(SagaMilestone.ActFinished(a, message))
                    } ?: run {
                        dismissMilestone()
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
            messageDao.getMessagesCount(saga.data.id).first()
            val act = saga.currentActInfo ?: return
            val rules = fetchNarrativeRules()
            val invalidActs =
                saga.acts.filter {
                    it.data.id != act.data.id && it.isComplete(rules).not()
                }
            invalidActs.forEach {
                actUseCase.deleteAct(it.data)
            }

            act.let { currentAct ->
                val currentChapter = saga.currentChapterInfo
                val invalidChapters =
                    currentAct.chapters.filter {
                        it.data.id != currentChapter?.data?.id && it.isComplete(rules).not()
                    }
                invalidChapters.forEach {
                    chapterUseCase.deleteChapter(it.data)
                }

                currentChapter?.let { chapterMetadata ->
                    val currentEvent = saga.currentEventInfo

                    val invalidEvents =
                        chapterMetadata.events.filter {
                            it.data.id != currentEvent?.data?.id &&
                                it
                                    .isComplete(rules)
                                    .not()
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

        private suspend fun handleChapterPostActions(
            chapter: Chapter,
            saga: SagaMetadata,
        ) {
            endChapter(saga.currentActInfo)

            CoroutineScope(Dispatchers.IO).launch {
                val chapterContent =
                    saga
                        .flatChapters()
                        .find { it.data.id == chapter.id }!!
                        .copy(data = chapter)
                // Synthesis already handled Wiki and Arcs. Just generate cover.
                chapterUseCase.generateChapterCover(chapterContent.data.id)
            }
        }

        private suspend fun createEndingMessage(saga: SagaMetadata) =
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
                } else {
                    dismissMilestone()
                    val fullSaga =
                        sagaHistoryUseCase.getSagaById(saga.data.id).first() as SagaContent
                    val contextString =
                        "Concluding your legend and weaving the final threads of fate..."
                    val style = genreConfigService.conversationBlueprint(saga.data.genre)
                    var generated: GeneratedContent<SagaEnding>? = null
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            sourceFlow = sagaHistoryUseCase.generateSagaEndingStream(fullSaga),
                            context = contextString,
                            conversationStyle = style,
                            genre = saga.data.genre.name,
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Reasoning -> {
                                    contentReasoning.value = state.chunk
                                }

                                is StreamingState.Success -> {
                                    generated = state.data as? GeneratedContent<SagaEnding>
                                    contentReasoning.value = null
                                }

                                is StreamingState.Error -> {
                                    contentReasoning.value = null
                                    error(state.message)
                                }
                            }
                        }

                    val ending = generated?.data ?: error("Failed to generate ending")
                    emitMilestone(null)
                    sagaHistoryUseCase
                        .updateSaga(
                            saga.data.copy(
                                endMessage = ending.endingMessage,
                                isEnded = true,
                                endedAt = System.currentTimeMillis(),
                                emotionalProfile = ending.emotionalProfile,
                                emotionalReview = ending.emotionalProfile.emotionalContent,
                            ),
                        )
                }
            }

        override suspend fun generateCharacter(
            description: String,
            sceneSummary: SceneSummary?,
        ): RequestResult<Character> =

            executeRequest {
                setProcessing(true)
                try {
                    val currentSaga = getSagaContent()!!
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
                        emitMilestone(
                            SagaMilestone.NewCharacter(
                                fakeCharacter,
                                saga = currentSaga.data,
                            ),
                        )
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
                                genre = currentSaga.data.genre.name,
                            ).collect { state ->
                                when (state) {
                                    is StreamingState.Reasoning -> {
                                        contentReasoning.value = state.chunk
                                    }

                                    is StreamingState.Success -> {
                                        generated = state.data
                                        contentReasoning.value = null
                                    }

                                    is StreamingState.Error -> {
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
                                saga = currentSaga.data,
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

        suspend fun generateEnding(saga: SagaMetadata) = createEndingMessage(saga)

        override fun stopProcessing() {
            Timber.i("Stopping all narrative processing")
            setNarrativeProcessingStatus(false)
            isProcessing.set(false)
            emitMilestone(null)
        }

        override suspend fun getSagaContent(): SagaContent? = sagaHistoryUseCase.getSagaById(content.value?.data?.id).first()

        override suspend fun updateSummary(sceneSummary: SceneSummary) {
            val saga = content.value ?: return
            val currentTimeline = saga.flatEvents().lastOrNull() ?: return
            val updatedTimeline = currentTimeline.data.copy(sceneSummary = sceneSummary)
            timelineUseCase.updateTimeline(updatedTimeline)
        }

        private suspend fun handleStreamingState(state: StreamingState<GeneratedContent<*>>) {
            when (state) {
                is StreamingState.Reasoning -> {
                    contentReasoning.value = state.chunk
                }

                is StreamingState.Success -> {
                    contentReasoning.value = null
                    val data = state.data.data
                    if (data is Timeline) {
                        emitMilestone(
                            SagaMilestone.NewEvent(
                                data,
                                null,
                                state.data.finalMessage,
                                sagaContent = getSagaContent()!!,
                            ),
                        )
                    }
                }

                is StreamingState.Error -> {
                    contentReasoning.value = null
                    updateSnackBar(snackBar(state.message))
                }
            }
        }
    }
