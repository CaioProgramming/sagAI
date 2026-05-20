package com.ilustris.sagai.features.saga.chat.data.manager

import android.content.Context
import android.net.Uri
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.data.isFlowCancellation
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.ImageHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.theme.SagaImmersiveSession
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.SagaContent
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
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.manager.BackgroundTask
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeActionExecutor
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeActionMaterializer
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCheck
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCoordinator
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeEvaluationContext
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionEnvironment
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionResult
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativePhase
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeUiState
import com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import com.ilustris.sagai.ui.components.NotificationStyle
import com.ilustris.sagai.ui.components.SagaNotificationEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
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
        private val remoteConfig: RemoteConfigService,
        private val backupService: BackupService,
        private val imageHelper: ImageHelper,
        private val genreConfigService: GenreConfigService,
        private val messageDao: MessageDao,
        private val sagaThemeManager: SagaThemeManager,
        private val sagaImmersiveSession: SagaImmersiveSession,
        private val narrativeCoordinator: NarrativeCoordinator,
        private val narrativeActionExecutor: NarrativeActionExecutor,
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

        private val isProcessingNarrative = AtomicBoolean(false)
        private val _narrativeProcessingUiState = MutableStateFlow(false)
        override val narrativeProcessingUiState: StateFlow<Boolean> =
            _narrativeProcessingUiState.asStateFlow()

        override val narrativeUiState: StateFlow<NarrativeUiState> = narrativeCoordinator.uiState

        private var sagaJob: kotlinx.coroutines.Job? = null
        private var loadingObserverJob: kotlinx.coroutines.Job? = null
        private var milestoneObserverJob: kotlinx.coroutines.Job? = null
        private var reasoningObserverJob: kotlinx.coroutines.Job? = null

        override var notificationUpdate: MutableStateFlow<SagaNotificationEvent?> =
            MutableStateFlow(null)

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
            Timber.i("Message processing mode ${if (bool) "enabled" else "disabled"}")
        }

        override fun isInDebugMode(): Boolean = isDebugModeEnabled

        override suspend fun advanceNarrative() {
            val action = narrativeCoordinator.uiState.value.pendingAction ?: return
            Timber.d("Manually advancing narrative: ${action.javaClass.simpleName}")
            narrativeCoordinator.onUserAdvanceRequested(action)
            executeNarrativeAction(action, isRetry = false)
        }

        private suspend fun executeNarrativeAction(
            action: NarrativeAction,
            isRetry: Boolean,
        ) {
            val sagaMetadata = content.value ?: return
            setNarrativeProcessingStatus(true)
            narrativeCoordinator.markProcessing(true)
            try {
                val result =
                    narrativeActionExecutor.execute(
                        action,
                        buildExecutionEnvironment(),
                    )
                narrativeCoordinator.onActionCompleted(action, result)
                when (result) {
                    is NarrativeExecutionResult.Success -> {
                        handlePostAction(sagaMetadata, action, result.value)
                        awaitMilestoneDismissalIfNeeded()
                        requestNarrativeProgression(isRetry = false)
                    }

                    is NarrativeExecutionResult.Failure -> {
                        Timber.e("Failed narrative action: ${result.message}")
                        emitMilestone(null)
                        if (isRetry) {
                            sagaThemeManager.showSnackBar(
                                result.message,
                                context.getString(R.string.try_again) to {
                                    managerScope.launch {
                                        narrativeCoordinator.clearError()
                                        executeNarrativeAction(action, isRetry = true)
                                    }
                                },
                            )
                        } else {
                            executeNarrativeAction(action, isRetry = true)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e.isFlowCancellation()) {
                    throw e
                }
                Timber.e(e, "Unexpected error executing narrative action")
                narrativeCoordinator.onActionCompleted(
                    action,
                    NarrativeExecutionResult.Failure(e.message ?: "Unknown error"),
                )
                emitMilestone(null)
            } finally {
                narrativeCoordinator.markProcessing(false)
                setNarrativeProcessingStatus(false)
            }
        }

        private fun buildExecutionEnvironment() =
            NarrativeExecutionEnvironment(
                getSagaMetadata = { content.value },
                getSagaContent = { getSagaContent() },
                fetchNarrativeRules = { fetchNarrativeRules() },
                onReasoningChunk = { chunk -> contentReasoning.value = chunk },
                dismissMilestone = { dismissMilestone() },
                isDebugMode = { isDebugModeEnabled },
                getMessageCount = { sagaId -> messageDao.getMessagesCount(sagaId).first() },
            )

        private fun buildEvaluationContext(): NarrativeEvaluationContext {
            val milestone = milestoneUpdate.value
            return NarrativeEvaluationContext(
                isOnboardingVisible = isOnboardingVisible.value,
                isMilestoneActive = isMilestoneActive.value,
                isNarrativeProcessing = isProcessingNarrative.get(),
                hasActiveMilestoneOverlay = milestone?.isIntrusive == true,
            )
        }

        private suspend fun awaitMilestoneDismissalIfNeeded() {
            if (isMilestoneActive.value) {
                isMilestoneActive.first { !it }
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
                                sagaThemeManager.showSnackBar(readableMessage)
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

                                if (sagaImmersiveSession.isOwnerOnTop("chat")) {
                                    sagaThemeManager.updateTheme(saga.data.genre)
                                }

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
                                        } ?: emitMilestone(null)
                                    } else {
                                        emitMilestone(null)
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
                    notificationEvent(
                        message =
                            "${lastMessage.message.speakerName ?: emptyString()}: ${lastMessage.message.text}",
                        icon = charIcon,
                        style = NotificationStyle.CHAT,
                    )?.let { emitNotification(it) }
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

        override suspend fun reviewWiki(wikiItems: List<Wiki>) {
            val saga = content.value ?: return
            startProcessing {
                wikiUseCase.mergeWikis(saga, wikiItems).onSuccessAsync {
                    sagaThemeManager.showSnackBar(context.getString(R.string.wiki_updated))
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
                sagaThemeManager.showSnackBar(context.getString(R.string.backup_disabled))
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

        private suspend fun endAct(saga: SagaMetadata) =
            executeRequest {
                sagaHistoryUseCase.updateSaga(saga.data.copy(currentActId = null)).asSuccess()
            }

        private fun observeMilestone() =
            managerScope.launch {
                milestoneUpdate.collectLatest {
                    Timber.d("observeMilestone:\n$it")
                    if (it == null) {
                        Timber.i("observeMilestone: No milestone checking story...")
                        narrativeCoordinator.markMilestoneDismissed()
                        requestNarrativeProgression()
                    }
                }
            }

        private fun observeLoading() =
            managerScope.launch {
                narrativeProcessingUiState.collectLatest {
                    Timber.d("observeLoading: $it")
                    if (it.not() && milestoneUpdate.value == null) {
                        requestNarrativeProgression()
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
                requestNarrativeProgression(isRetry = isRetrying, fallbackSaga = saga)
            }
        }

        private suspend fun requestNarrativeProgression(
            isRetry: Boolean = false,
            fallbackSaga: SagaMetadata? = null,
        ) {
            if (progressionMutex.isLocked) {
                Timber.i("requestNarrativeProgression: already in progress, queueing reevaluation.")
                narrativeCoordinator.schedulePendingReevaluation()
                return
            }

            if (isOnboardingVisible.value) {
                Timber.i("requestNarrativeProgression: onboarding visible, skipping.")
                return
            }

            progressionMutex.withLock {
                val currentSaga = content.value ?: fallbackSaga ?: return@withLock

                if (isProcessingNarrative.get()) {
                    Timber.i("requestNarrativeProgression: narrative processing, skipping.")
                    return@withLock
                }

                Timber.d("Starting narrative progression check #${++progressionCounter}")

                if (currentSaga.mainCharacter == null && isDebugModeEnabled) {
                    generateCharacter("Main Debug Character").onSuccessAsync { newCharacter ->
                        sagaHistoryUseCase.updateSaga(currentSaga.data.copy(mainCharacterId = newCharacter.id))
                    }
                    return@withLock
                }

                val sagaContent = getSagaContent() ?: return@withLock
                val rules = fetchNarrativeRules()
                messageDao.getMessagesCount(currentSaga.data.id).first()

                val intent = NarrativeCheck.validateProgressionMetadata(currentSaga, rules)
                val hydrated =
                    intent?.let { NarrativeActionMaterializer.materialize(it, sagaContent) }
                if (intent != null && hydrated == null) {
                    Timber.w(
                        "requestNarrativeProgression: metadata implies progression (${intent.javaClass.simpleName}) but SagaContent hydration failed.",
                    )
                    narrativeCoordinator.schedulePendingReevaluation()
                    return@withLock
                }

                val uiState =
                    narrativeCoordinator.reevaluate(
                        nextResolvedAction = hydrated,
                        context = buildEvaluationContext(),
                    )

                if (uiState.phase is NarrativePhase.BackgroundProcessing &&
                    hydrated is NarrativeAction.CloseTimeline
                ) {
                    Timber.d("Auto-executing CloseTimeline")
                    narrativeCoordinator.onBackgroundTaskStarted(BackgroundTask.ClosingScene)
                    executeNarrativeAction(hydrated, isRetry = isRetry)
                }

                if (narrativeCoordinator.consumePendingReevaluation()) {
                    requestNarrativeProgression(isRetry = false)
                }
            }
        }

        private fun notificationEvent(
            message: String,
            style: NotificationStyle,
            icon: android.graphics.Bitmap? = null,
        ): SagaNotificationEvent? {
            val saga = content.value ?: return null
            return SagaNotificationEvent(
                sagaId = saga.data.id,
                sagaTitle = saga.data.title,
                genre = saga.data.genre,
                message = message,
                icon = icon,
                style = style,
            )
        }

        private fun emitNotification(event: SagaNotificationEvent) {
            managerScope.launch {
                notificationUpdate.emit(event)
            }
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

        override val isMilestoneActive = MutableStateFlow(false)

        override fun dismissMilestone() {
            isMilestoneActive.value = false
            milestoneUpdate.value = null
            narrativeCoordinator.markMilestoneDismissed()
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
                is SagaMilestone.Introduction,
                -> {
                    doNothing()
                }

                is SagaMilestone.NewEvent -> {
                    getSagaContent()?.currentActInfo?.currentChapterInfo?.let { chapter ->
                        managerScope.launch {
                            val closeAction = NarrativeAction.CloseTimeline(chapter)
                            narrativeCoordinator.onBackgroundTaskStarted(BackgroundTask.ClosingScene)
                            executeNarrativeAction(closeAction, isRetry = false)
                        }
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
                if (milestone != null && milestone.isIntrusive) {
                    isMilestoneActive.value = true
                    narrativeCoordinator.markMilestoneActive()
                    if (milestone.shouldPlaySoundFx) {
                        sagaThemeManager.playVfx()
                    }
                    milestoneBackgroundNotification(milestone)?.let { emitNotification(it) }
                }
                milestoneUpdate.emit(milestone)
            }

        private fun milestoneBackgroundNotification(milestone: SagaMilestone): SagaNotificationEvent? {
            val message =
                when (milestone) {
                    is SagaMilestone.ChapterFinished ->
                        context.getString(
                            R.string.notification_new_chapter_content,
                            milestone.chapter.title,
                        )

                    is SagaMilestone.ActFinished ->
                        context.getString(
                            R.string.notification_new_act_content,
                            milestone.act.title,
                            milestone.act.title,
                        )

                    is SagaMilestone.NewEvent ->
                        context.getString(
                            R.string.notification_timeline_event_content,
                            milestone.timeline.title,
                        )

                    is SagaMilestone.NewCharacter -> {
                        val name =
                            "${milestone.character.name} ${milestone.character.lastName ?: emptyString()}".trim()
                        context.getString(R.string.notification_new_character_content, name)
                    }

                    else -> return null
                }
            return notificationEvent(message, NotificationStyle.DEFAULT)
        }

        private suspend fun startProcessing(block: suspend () -> Unit) {
            if (isProcessing.get().not()) {
                setProcessing(true)
            }
            block()
            setProcessing(false)
        }

        private suspend fun handlePostAction(
            saga: SagaMetadata,
            action: NarrativeAction,
            resultValue: Any?,
        ) {
            Timber.d("handlePostAction: performing post action for $action")
            when (action) {
                NarrativeAction.CreateAct -> {
                    (resultValue as? Act)?.let { data ->
                        val latest = content.value?.data ?: saga.data
                        if (latest.currentActId != data.id) {
                            Timber.w(
                                "CreateAct: aligning saga.currentActId (${latest.currentActId}) to new act ${data.id}",
                            )
                            sagaHistoryUseCase.updateSaga(
                                latest.copy(currentActId = data.id),
                            )
                        }
                        backupSaga()
                    }
                }

                is NarrativeAction.GenerateActIntro -> {
                    val generatedContent = resultValue as? GeneratedContent<Act>
                    val act = generatedContent?.data ?: resultValue as? Act
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

                is NarrativeAction.CreateChapter -> {
                    val currentAct = content.value?.currentActInfo ?: saga.currentActInfo ?: return
                    (resultValue as? Chapter)?.let { chapter ->
                        if (saga.currentChapterInfo != null &&
                            saga.currentChapterInfo!!.data.id != chapter.id
                        ) {
                            Timber.w("CreateChapter: Chapter already set and different. Skipping update.")
                            return
                        }
                        actUseCase.updateAct(
                            currentAct.data.copy(currentChapterId = chapter.id),
                        )
                    } ?: dismissMilestone()
                }

                is NarrativeAction.GenerateChapterIntro -> {
                    val generatedContent = resultValue as? GeneratedContent<Chapter>
                    val chapterUpdate = generatedContent?.data ?: resultValue as? Chapter
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
                    } ?: dismissMilestone()
                }

                is NarrativeAction.CreateTimeline -> {
                    (resultValue as? Timeline)?.let { timeline ->
                        chapterUseCase.updateChapter(
                            saga.currentChapterInfo!!.data.copy(
                                currentEventId = timeline.id,
                            ),
                        )
                        val objective =
                            timelineUseCase
                                .getTimelineObjective(content.value!!, timeline)
                                .getSuccess()
                        objective?.let {
                            emitMilestone(SagaMilestone.CurrentObjective(it))
                        } ?: dismissMilestone()
                    } ?: dismissMilestone()
                }

                is NarrativeAction.EvolveTimeline -> {
                    val generatedContent = resultValue as? GeneratedContent<Timeline>
                    val timeline = generatedContent?.data ?: resultValue as? Timeline
                    val message = generatedContent?.finalMessage
                    timeline?.let { t ->
                        sagaThemeManager.showSnackBar(
                            context.getString(
                                R.string.timeline_updated,
                                t.title,
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
                    } ?: dismissMilestone()
                }

                is NarrativeAction.GenerateChapter -> {
                    val generatedContent = resultValue as? GeneratedContent<Chapter>
                    val chapter = generatedContent?.data ?: resultValue as? Chapter
                    val message = generatedContent?.finalMessage
                    chapter?.let { c ->
                        getSagaContent()?.let { fullSaga ->
                            emitMilestone(SagaMilestone.ChapterFinished(c, message, fullSaga))
                        }
                    } ?: dismissMilestone()
                }

                is NarrativeAction.GenerateAct -> {
                    val generatedContent = resultValue as? GeneratedContent<Act>
                    val act = generatedContent?.data ?: resultValue as? Act
                    val message = generatedContent?.finalMessage
                    act?.let { a ->
                        emitMilestone(SagaMilestone.ActFinished(a, message))
                    } ?: dismissMilestone()
                }

                is NarrativeAction.CloseTimeline,
                is NarrativeAction.GenerateEnding,
                -> {
                    doNothing()
                }
            }
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

        override suspend fun generateCharacter(
            description: String,
            sceneSummary: SceneSummary?,
            candidateName: String?,
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
                        "Evaluating potential characters for the story..."
                        genreConfigService.conversationBlueprint(currentSaga.data.genre)
                        characterUseCase
                            .generateCharacterStream(
                                currentSaga,
                                description,
                                sceneSummary ?: _sceneSummary.value,
                                candidateName = candidateName,
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

                        sagaThemeManager.showSnackBar(
                            context.getString(
                                R.string.new_character_message,
                                generatedCharacter.name,
                            ),
                        )

                        emitMilestone(
                            SagaMilestone.NewCharacter(
                                generatedCharacter,
                                generated.finalMessage,
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

        override fun stopProcessing() {
            Timber.i("Stopping all narrative processing")
            setNarrativeProcessingStatus(false)
            isProcessing.set(false)
            narrativeCoordinator.reset()
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
                    if (!state.isFlowCancellation()) {
                        sagaThemeManager.showSnackBar(state.message)
                    }
                }
            }
        }

        private companion object {
            val TITLE_SPLASH_DURATION = 2.5.seconds
        }
    }
