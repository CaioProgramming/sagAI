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
import com.ilustris.sagai.core.file.AVATAR_ICON_TARGET_PX
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.ImageHelper
import com.ilustris.sagai.core.globalshell.BookReadyEffect
import com.ilustris.sagai.core.globalshell.GlobalShellEffect
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.core.globalshell.NewChapterEffect
import com.ilustris.sagai.core.globalshell.NewCharacterEffect
import com.ilustris.sagai.core.navigation.SagaNavigationTracker
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.theme.SagaImmersiveSession
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.data.model.fullName
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
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.findCharacterStrict
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.data.model.AIReply
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.hasActiveSceneSummary
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeActionExecutor
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeActionMaterializer
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCheck
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCoordinator
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeEvaluationContext
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionEnvironment
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionResult
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativePhase
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeProcessingGate
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeUiState
import com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.MilestoneDashboardMapper
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.MilestoneDetailAction
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.toDeepLink
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import com.ilustris.sagai.ui.components.island.AdvanceIslandContent
import com.ilustris.sagai.ui.components.island.ChatIslandService
import com.ilustris.sagai.ui.components.island.IntroductionIslandContent
import com.ilustris.sagai.ui.components.island.IslandContent
import com.ilustris.sagai.ui.components.island.LoadingIslandContent
import com.ilustris.sagai.ui.components.island.NarrativeMilestoneIslandContent
import com.ilustris.sagai.ui.components.island.ObjectiveIslandContent
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
import kotlinx.coroutines.flow.combine
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
        private val narrativeProcessingGate: NarrativeProcessingGate,
        private val stringResourceHelper: StringResourceHelper,
        private val globalShellService: GlobalShellService,
        private val chatIslandService: ChatIslandService,
        private val milestoneDashboardMapper: MilestoneDashboardMapper,
        private val sagaNavigationTracker: SagaNavigationTracker,
        @ApplicationContext
        private val context: Context,
    ) : SagaContentManager {
        override val contentReasoning = MutableStateFlow<String?>(null)
        override val content = MutableStateFlow<SagaMetadata?>(null)
        private val _sceneSummary = MutableStateFlow<SceneSummary?>(null)
        override val sceneSummary: StateFlow<SceneSummary?> = _sceneSummary.asStateFlow()
        override val milestoneUpdate = MutableStateFlow<SagaMilestone?>(null)
        private val _showObjectiveOverlay = MutableStateFlow(false)
        override val showObjectiveOverlay: StateFlow<Boolean> = _showObjectiveOverlay.asStateFlow()
        override val isOnboardingVisible = MutableStateFlow(false)

        // Advance-trigger bottom island gating that's purely a UI concern (onboarding overlays,
        // message-selection mode, chat-reply generation) — forwarded by the chat screen via
        // setAdvanceTriggerSuppressed rather than derived here.
        private val _advanceTriggerSuppressed = MutableStateFlow(false)

        // Set once per milestone occurrence; consumed by onMilestoneRevealStarted() the moment the
        // reveal composable's own animation reaches its "spark" phase (mirrors the old
        // MilestoneViewModel.onRevealStarted/loadDashboardItems pairing).
        private var pendingRevealSfx = false

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
        private var islandObserverJob: kotlinx.coroutines.Job? = null

        private var isDebugModeEnabled: Boolean = false
        private val isProcessing = AtomicBoolean(false)

        private val progressionMutex = Mutex()
        private val managerJob = SupervisorJob()
        private val managerScope = CoroutineScope(managerJob + Dispatchers.IO)

        private var progressionCounter = 0
        private var lastObservedMessageCount = -1

        private fun setNarrativeProcessingStatus(isProcessing: Boolean) {
            isProcessingNarrative.set(isProcessing)
            _narrativeProcessingUiState.value = isProcessing
            narrativeProcessingGate.setNarrativeProcessing(isProcessing)
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
            if (isProcessingNarrative.get()) {
                Timber.d("advanceNarrative: already in progress, ignoring duplicate request")
                return
            }
            val action = narrativeCoordinator.uiState.value.pendingAction ?: return
            Timber.d("Manually advancing narrative: ${action.javaClass.simpleName}")
            narrativeCoordinator.onUserAdvanceRequested(action)
            executeNarrativeAction(action, isRetry = false)
        }

        override suspend fun completeGameplayOnboarding(saga: SagaMetadata?) {
            requestNarrativeProgression(isRetry = false, fallbackSaga = saga, force = false)
            isOnboardingVisible.value = false
            if (narrativeCoordinator.uiState.value.pendingAction == NarrativeAction.CreateAct) {
                advanceNarrative()
            }
        }

        private fun handleNarrativeActionFailure(
            action: NarrativeAction,
            canRetry: Boolean = true,
        ) {
            val userMessage = stringResourceHelper.getString(R.string.unexpected_error)
            narrativeCoordinator.onActionCompleted(
                action,
                NarrativeExecutionResult.Failure(
                    message = userMessage,
                    canRetry = canRetry,
                ),
            )
            dismissMilestone()
            sagaThemeManager.showSnackBar(
                userMessage,
                stringResourceHelper.getString(R.string.try_again) to {
                    managerScope.launch {
                        narrativeCoordinator.clearError()
                        executeNarrativeAction(action, isRetry = true)
                    }
                },
            )
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
                        requestNarrativeProgression(isRetry = false, force = true)
                    }

                    is NarrativeExecutionResult.Failure -> {
                        Timber.e("Failed narrative action: ${result.message}")
                        handleNarrativeActionFailure(action, result.canRetry)
                    }
                }
            } catch (e: Exception) {
                if (e.isFlowCancellation()) {
                    throw e
                }
                Timber.e(e, "Unexpected error executing narrative action")
                handleNarrativeActionFailure(action, canRetry = true)
            } finally {
                contentReasoning.value = null
                narrativeCoordinator.markProcessing(false)
                setNarrativeProcessingStatus(false)
                // reevaluate() bails out early while isNarrativeProcessing is true (still the case
                // for the Success branch's own requestNarrativeProgression call above, since that
                // runs before this block resets the flag) and just queues a pending reevaluation
                // instead. Nothing else proactively consumes that flag once processing actually
                // clears, so without this the advance trigger can silently never reappear until an
                // unrelated event (new message, saga reload) happens to ask again.
                if (narrativeCoordinator.consumePendingReevaluation()) {
                    requestNarrativeProgression(isRetry = false)
                }
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
                isMilestoneActive = isMilestoneActive.value,
                isNarrativeProcessing = isProcessingNarrative.get(),
                hasActiveMilestoneOverlay = milestone?.isIntrusive == true,
            )
        }

        private suspend fun awaitMilestoneDismissalIfNeeded() {
            if (isMilestoneActive.value) {
                isMilestoneActive.first { !it }
            }
            if (milestoneUpdate.value?.isIntrusive == true) {
                milestoneUpdate.first { it == null || !it.isIntrusive }
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
            val objective = currentTimeline.data.displayObjective()
            if (objective.isNullOrBlank().not()) {
                _showObjectiveOverlay.value = true
            }
        }

        override fun dismissObjective() {
            _showObjectiveOverlay.value = false
        }

        override fun resetSagaSession() {
            Timber.d("resetSagaSession: clearing chat session state")
            sagaJob?.cancel()
            sagaJob = null
            setNarrativeProcessingStatus(false)
            isProcessing.set(false)
            narrativeCoordinator.reset()
            contentReasoning.value = null
            _sceneSummary.value = null
            isMilestoneActive.value = false
            milestoneUpdate.value = null
            _showObjectiveOverlay.value = false
            isOnboardingVisible.value = false
            content.value = null
            lastObservedMessageCount = -1
        }

        override suspend fun loadSaga(sagaId: String) {
            lastObservedMessageCount = -1
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
            resetSagaSession()
            sagaJob =
                managerScope.launch {
                    Timber.d("Loading saga: $sagaId")
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
                        if (islandObserverJob == null || islandObserverJob?.isActive == false) {
                            islandObserverJob = observeIslands()
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
                                val previousMessageCount = lastObservedMessageCount
                                val currentMessageCount =
                                    messageDao.getMessagesCount(sagaId.toInt()).first()
                                lastObservedMessageCount = currentMessageCount

                                val sceneChanged =
                                    previousSaga?.getCurrentTimeLine()?.data?.sceneSummary != saga.getCurrentTimeLine()?.data?.sceneSummary
                                val playTimeChanged =
                                    previousSaga?.data?.playTimeMs != saga.data.playTimeMs

                                content.value = saga
                                saga.getCurrentTimeLine()?.data?.sceneSummary?.let {
                                    _sceneSummary.value = it
                                }
                                if (sagaImmersiveSession.isOwnerOnTop("chat")) {
                                    sagaThemeManager.updateTheme(saga.data.genre)
                                }

                                val skipNarrativeCheck =
                                    previousSaga != null &&
                                        previousSaga.data.id == saga.data.id &&
                                        previousMessageCount == currentMessageCount &&
                                        previousTimelineId == currentTimelineId &&
                                        previousSaga.acts == saga.acts &&
                                        (sceneChanged || playTimeChanged)

                                if (skipNarrativeCheck) {
                                    Timber.d(
                                        "Saga update was subtle (playtime: $playTimeChanged, scene: $sceneChanged). Skipping narrative check.",
                                    )
                                    return@collectLatest
                                }

                                if (previousMessageCount != currentMessageCount || previousTimelineId != currentTimelineId ||
                                    previousSaga == null
                                ) {
                                    checkNarrativeProgression(saga)
                                    if (currentMessageCount > previousMessageCount) {
                                        linkUnlinkedCharacterMessages(saga)
                                    }
                                }

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
                    when {
                        reasoning != null && milestoneUpdate.value != SagaMilestone.Loading -> {
                            emitMilestone(SagaMilestone.Loading)
                        }

                        reasoning == null && milestoneUpdate.value is SagaMilestone.Loading -> {
                            emitMilestone(null)
                        }
                    }
                }
            }

        override fun setAdvanceTriggerSuppressed(suppressed: Boolean) {
            _advanceTriggerSuppressed.value = suppressed
        }

        /** Tracks whether the currently-published milestone still owes its reveal SFX, mirroring
         * the old MilestoneViewModel.loadDashboardItems/onRevealStarted pairing — set once per
         * milestone occurrence, consumed by [onMilestoneRevealStarted]. */
        private fun observeMilestoneRevealSfx() =
            managerScope.launch {
                milestoneUpdate.collectLatest { milestone ->
                    pendingRevealSfx = milestone != null && milestone.shouldPlaySoundFx && milestone.playsRevealSfx
                }
            }

        private fun onMilestoneRevealStarted() {
            if (pendingRevealSfx) {
                sagaThemeManager.playVfx()
                pendingRevealSfx = false
            }
        }

        private data class IslandSnapshot(
            val saga: SagaMetadata?,
            val milestone: SagaMilestone?,
            val narrativeState: NarrativeUiState,
            val reasoning: String?,
            val advanceSuppressed: Boolean,
        )

        /**
         * Publishes the chat's top/bottom island content directly — no per-screen Composable host
         * needed. A milestone in flight takes over the top or bottom slot (matching the reveal's
         * intrusiveness); otherwise top falls back to the current objective and bottom to the
         * loading pulse or the narrative-advance trigger. Cleared whenever the user navigates away
         * from this saga's chat — islands published from here are only meaningful while it's the
         * visible screen, unlike the old per-Composable host's DisposableEffect(onDispose), this
         * reacts to navigation itself rather than the chat leaving composition.
         */
        private fun observeIslands(): kotlinx.coroutines.Job {
            observeMilestoneRevealSfx()
            return managerScope.launch {
                combine(
                    content,
                    milestoneUpdate,
                    narrativeCoordinator.uiState,
                    contentReasoning,
                    _advanceTriggerSuppressed,
                ) { saga, milestone, narrativeState, reasoning, advanceSuppressed ->
                    IslandSnapshot(saga, milestone, narrativeState, reasoning, advanceSuppressed)
                }.combine(sagaNavigationTracker.currentKey) { snapshot, _ -> snapshot }
                    .collectLatest { snapshot ->
                        val sagaId = snapshot.saga?.data?.id
                        if (sagaId == null || !sagaNavigationTracker.isOnChatForSaga(sagaId)) {
                            chatIslandService.setTop(null)
                            chatIslandService.setBottom(null)
                        } else {
                            publishIslands(
                                snapshot.saga,
                                snapshot.milestone,
                                snapshot.narrativeState,
                                snapshot.reasoning,
                                snapshot.advanceSuppressed,
                            )
                        }
                    }
            }
        }

        /** Same `messages / loreUpdateLimit` fraction the chat's own progress ring used to
         * compute — now feeding the objective island's compact progress ring instead. */
        private suspend fun currentObjectiveProgress(saga: SagaMetadata): Float {
            if (saga.data.isEnded) return 1f
            val timeline = saga.getCurrentTimeLine() ?: return 0f
            val updateLimit = fetchNarrativeRules().loreUpdateLimit
            return (timeline.messages.size.toFloat() / updateLimit.toFloat()).coerceIn(0f, 1f)
        }

        private suspend fun publishIslands(
            saga: SagaMetadata?,
            milestone: SagaMilestone?,
            narrativeState: NarrativeUiState,
            reasoning: String?,
            advanceSuppressed: Boolean,
        ) {
            if (saga == null) {
                chatIslandService.setTop(null)
                chatIslandService.setBottom(null)
                return
            }
            val genre = saga.data.genre
            val onDetailAction: (MilestoneDetailAction) -> Unit = { action ->
                chatIslandService.requestNavigation(action.toDeepLink())
            }
            val onContinue: () -> Unit = { managerScope.launch { continueMilestone() } }

            val milestoneOwnsBottom =
                milestone is SagaMilestone.NewEvent ||
                    milestone is SagaMilestone.ChapterFinished ||
                    milestone is SagaMilestone.ActFinished

            val topContent: IslandContent? =
                when {
                    milestone is SagaMilestone.Introduction ->
                        IntroductionIslandContent(milestone, saga.data, onContinue)

                    else -> {
                        val objectiveText = saga.getCurrentTimeLine()?.data?.displayObjective()
                        if (!objectiveText.isNullOrBlank()) {
                            ObjectiveIslandContent(
                                titleRes = R.string.current_objective,
                                objective = objectiveText,
                                genre = genre,
                                progress = currentObjectiveProgress(saga),
                            )
                        } else {
                            null
                        }
                    }
                }
            chatIslandService.setTop(topContent)

            val advanceAction = narrativeState.displayAdvanceAction
            val isProcessing = narrativeState.phase is NarrativePhase.Processing || narrativeState.isProcessing
            val showAdvance =
                narrativeState.showAdvanceTrigger && advanceAction != null && !advanceSuppressed

            val bottomContent: IslandContent? =
                when {
                    milestoneOwnsBottom && milestone != null -> {
                        val characters = when {
                            milestone is SagaMilestone.NewEvent -> milestone.characters
                            milestone is SagaMilestone.ChapterFinished -> milestone.characters
                            milestone is SagaMilestone.ActFinished -> milestone.characters
                            else -> emptyList()
                        }
                        val wikis = when {
                            milestone is SagaMilestone.NewEvent -> milestone.wikis
                            milestone is SagaMilestone.ChapterFinished -> milestone.wikis
                            milestone is SagaMilestone.ActFinished -> milestone.wikis
                            else -> emptyList()
                        }
                        val emotionalTone = when {
                            milestone is SagaMilestone.NewEvent -> milestone.emotionalTone
                            milestone is SagaMilestone.ChapterFinished -> milestone.emotionalTone
                            milestone is SagaMilestone.ActFinished -> milestone.emotionalTone
                            else -> EmotionalTone.NEUTRAL
                        }
                        NarrativeMilestoneIslandContent(
                            milestone = milestone,
                            genre = genre,
                            characters = characters,
                            wikis = wikis,
                            emotionalTone = emotionalTone,
                            onRevealStarted = ::onMilestoneRevealStarted,
                            onContinue = onContinue,
                        )
                    }

                    milestone is SagaMilestone.Loading ->
                        LoadingIslandContent(reasoning = reasoning, genre = genre)

                    showAdvance && advanceAction != null ->
                        AdvanceIslandContent(
                            action = advanceAction,
                            reasoning = reasoning,
                            isProcessing = isProcessing,
                            genre = genre,
                            onAction = {
                                if (!isProcessing) managerScope.launch { advanceNarrative() }
                            },
                        )

                    else -> null
                }
            chatIslandService.setBottom(bottomContent)
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
            force: Boolean = false,
        ) {
            if (progressionMutex.isLocked) {
                Timber.i("requestNarrativeProgression: already in progress, queueing reevaluation.")
                narrativeCoordinator.schedulePendingReevaluation()
                return
            }

            var hydrated: NarrativeAction? = null
            // Whether another reevaluation was queued *while we held the lock*. Consumed here but
            // acted on below, outside withLock — recursing from inside it would always see the
            // mutex as locked (by ourselves) and just re-queue forever without ever re-checking.
            var shouldReevaluateAgain = false
            progressionMutex.withLock {
                val currentSaga = content.value ?: fallbackSaga ?: return@withLock

                if (!force && isProcessingNarrative.get()) {
                    Timber.i("requestNarrativeProgression: narrative processing, queueing reevaluation.")
                    narrativeCoordinator.schedulePendingReevaluation()
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
                hydrated =
                    intent?.let { NarrativeActionMaterializer.materialize(it, sagaContent) }
                if (intent != null && hydrated == null) {
                    Timber.w(
                        "requestNarrativeProgression: metadata implies progression (${intent.javaClass.simpleName}) but SagaContent hydration failed.",
                    )
                    narrativeCoordinator.schedulePendingReevaluation()
                    return@withLock
                }

                val isAutomatic = hydrated is NarrativeAction.CreateTimeline
                narrativeCoordinator.reevaluate(
                    nextResolvedAction = hydrated,
                    context = buildEvaluationContext(),
                    isAutomatic = isAutomatic,
                )

                shouldReevaluateAgain = narrativeCoordinator.consumePendingReevaluation()
            }

            if (hydrated is NarrativeAction.CreateTimeline) {
                executeNarrativeAction(hydrated, isRetry = false)
            } else if (shouldReevaluateAgain) {
                requestNarrativeProgression(isRetry = false)
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
                            chapterUseCase.updateChapter(
                                chapter.data.copy(currentEventId = null),
                            )
                            requestNarrativeProgression()
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

        private suspend fun emitMilestone(milestone: SagaMilestone?) {
            withContext(Dispatchers.Main.immediate) {
                if (milestone != null && milestone.isIntrusive) {
                    isMilestoneActive.value = true
                    narrativeCoordinator.markMilestoneActive()
                    if (milestone.shouldPlaySoundFx && !milestone.playsRevealSfx) {
                        sagaThemeManager.playVfx()
                    }
                    postMilestoneEffect(milestone)
                }
                milestoneUpdate.emit(milestone)
            }
        }

        private suspend fun postMilestoneEffect(milestone: SagaMilestone) {
            milestoneToGlobalShellEffect(milestone)?.let { effect ->
                globalShellService.post(effect)
            }
        }

        private suspend fun milestoneToGlobalShellEffect(milestone: SagaMilestone): GlobalShellEffect? {
            val saga = content.value?.data ?: return null
            val sagaId = saga.id
            val sagaTitle = saga.title
            val genre = saga.genre

            val chatDeepLink = "saga://chat/$sagaId/false"
            val characterDeepLink: (Int) -> String = { characterId ->
                "saga://character_detail/$characterId"
            }
            val bookDeepLink: (Int) -> String = { actId ->
                "saga://book_reader/$sagaId/$actId"
            }

            return when (milestone) {
                is SagaMilestone.ChapterFinished -> {
                    NewChapterEffect(
                        chapterId = milestone.chapter.id,
                        sagaId = sagaId,
                        sagaTitle = sagaTitle,
                        genre = genre,
                        chapterTitle = milestone.chapter.title,
                        deepLink = chatDeepLink,
                    )
                }

                is SagaMilestone.ActFinished -> {
                    BookReadyEffect(
                        actId = milestone.act.id,
                        sagaId = sagaId,
                        sagaTitle = sagaTitle,
                        genre = genre,
                        actTitle = milestone.act.title,
                        deepLink = bookDeepLink(milestone.act.id),
                    )
                }

                is SagaMilestone.NewEvent -> {
                    NewChapterEffect(
                        chapterId = milestone.timeline.id,
                        sagaId = sagaId,
                        sagaTitle = sagaTitle,
                        genre = genre,
                        chapterTitle = milestone.timeline.title,
                        deepLink = chatDeepLink,
                    )
                }

                is SagaMilestone.NewCharacter -> {
                    val icon =
                        withContext(Dispatchers.IO) {
                            imageHelper
                                .getImageBitmap(
                                    milestone.character.image,
                                    cropToCircle = true,
                                    targetSizePx = AVATAR_ICON_TARGET_PX,
                                ).getSuccess()
                        }
                    val name =
                        "${milestone.character.name} ${milestone.character.lastName ?: emptyString()}".trim()
                    NewCharacterEffect(
                        characterId = milestone.character.id,
                        sagaId = sagaId,
                        sagaTitle = sagaTitle,
                        genre = genre,
                        characterName = name,
                        character = milestone.character,
                        icon = icon,
                        deepLink = characterDeepLink(milestone.character.id),
                    )
                }

                else -> {
                    null
                }
            }
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
                    val generatedContent = resultValue as? GeneratedContent<Act>
                    val act = generatedContent?.data ?: resultValue as? Act
                    val message = generatedContent?.finalMessage
                    act?.let { a ->
                        val latest = content.value?.data ?: saga.data
                        if (latest.currentActId != a.id) {
                            Timber.w(
                                "CreateAct: aligning saga.currentActId (${latest.currentActId}) to new act ${a.id}",
                            )
                            sagaHistoryUseCase.updateSaga(
                                latest.copy(currentActId = a.id),
                            )
                        }
                        backupSaga()
                        if (generatedContent != null) {
                            emitMilestone(
                                SagaMilestone.Introduction(
                                    type = IntroductionType.ACT,
                                    titleText = a.title,
                                    introduction = message ?: a.introduction,
                                    number = saga.actNumber(a.id).toRoman(),
                                    messageText = message,
                                ),
                            )
                        }
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
                    val timeline = resultValue as? Timeline
                    if (timeline != null && timeline.hasActiveSceneSummary()) {
                        timeline.sceneSummary?.let { _sceneSummary.value = it }
                        showObjective()
                    } else {
                        dismissMilestone()
                    }
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

                is NarrativeAction.CloseTimeline -> {
                    doNothing()
                }

                is NarrativeAction.GenerateEnding -> {
                    contentReasoning.value = null
                    emitMilestone(null)
                }
            }
        }

        override suspend fun getCurrentObjective(sceneSummary: SceneSummary) {
            val saga = content.value ?: return
            val event = saga.getCurrentTimeLine() ?: return
            if (!event.data.hasActiveSceneSummary()) {
                startProcessing {
                    val updatedTimeline =
                        event.data.copy(
                            sceneSummary = sceneSummary,
                            currentObjective = sceneSummary.immediateObjective,
                        )
                    timelineUseCase.updateTimeline(updatedTimeline)
                    _sceneSummary.value = sceneSummary
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
                        var generated: GeneratedContent<Character>? = null
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
                                generated!!.finalMessage,
                                saga = currentSaga.data,
                            ),
                        )

                        setProcessing(false)
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
            Timber.i("Stopping active generation without clearing saga session")
            contentReasoning.value = null
            isProcessing.set(false)
            setNarrativeProcessingStatus(false)
            narrativeCoordinator.markProcessing(false)
            if (milestoneUpdate.value is SagaMilestone.Loading) {
                dismissMilestone()
            }
        }

        override suspend fun getSagaContent(): SagaContent? = sagaHistoryUseCase.getSagaById(content.value?.data?.id).first()

        override fun linkUnlinkedCharacterMessages(saga: SagaMetadata) {
            managerScope.launch {
                linkUnlinkedCharacterMessagesInternal(saga)
            }
        }

        override fun resolveReplyCharacterLinks(
            saga: SagaMetadata,
            reply: AIReply,
            savedMessage: Message,
            sceneSummary: SceneSummary?,
        ) {
            managerScope.launch {
                val freshSaga =
                    sagaHistoryUseCase.getSagaMetadata(savedMessage.sagaId).first()
                        ?: content.value
                        ?: saga

                linkUnlinkedCharacterMessagesInternal(freshSaga)

                if (savedMessage.characterId != null) return@launch

                val linkCandidates =
                    listOfNotNull(
                        savedMessage.speakerName,
                        reply.newCharacter?.name,
                    ).distinctBy { it.trim().lowercase() }

                for (candidateName in linkCandidates) {
                    if (
                        linkMessageToExistingCharacter(
                            saga = freshSaga,
                            message = savedMessage,
                            candidateName = candidateName,
                        )
                    ) {
                        return@launch
                    }
                }

                val discovery = reply.newCharacter ?: return@launch
                if (reply.message.senderType == SenderType.NARRATOR) return@launch

                when (
                    val result =
                        generateCharacter(
                            description = discovery.toAINormalize(),
                            sceneSummary = sceneSummary,
                            candidateName = discovery.name,
                        )
                ) {
                    is RequestResult.Success -> {
                        val character = result.value
                        messageDao.updateMessage(
                            savedMessage.copy(
                                characterId = character.id,
                                speakerName = character.fullName(),
                            ),
                        )
                        sagaHistoryUseCase.getSagaMetadata(savedMessage.sagaId).first()?.let {
                            content.value = it
                        }
                    }

                    is RequestResult.Error -> {
                        Timber.w(
                            result.value,
                            "Failed to generate character for reply message ${savedMessage.id}",
                        )
                        for (candidateName in linkCandidates) {
                            if (
                                linkMessageToExistingCharacter(
                                    saga = freshSaga,
                                    message = savedMessage,
                                    candidateName = candidateName,
                                )
                            ) {
                                return@launch
                            }
                        }
                    }
                }
            }
        }

        private suspend fun linkUnlinkedCharacterMessagesInternal(saga: SagaMetadata) {
            val latestSaga =
                sagaHistoryUseCase.getSagaMetadata(saga.data.id).first()
                    ?: content.value
                    ?: saga
            for (messageContent in latestSaga.flatMessages()) {
                val message = messageContent.message
                if (message.characterId != null ||
                    message.senderType != SenderType.CHARACTER ||
                    message.speakerName.isNullOrBlank()
                ) {
                    continue
                }
                linkMessageToExistingCharacter(
                    saga = latestSaga,
                    message = message,
                    candidateName = message.speakerName,
                )
            }
        }

        private suspend fun linkMessageToExistingCharacter(
            saga: SagaMetadata,
            message: Message,
            candidateName: String?,
        ): Boolean {
            if (candidateName.isNullOrBlank()) return false
            val character =
                saga.findCharacterStrict(candidateName)
                    ?: saga.findCharacter(candidateName)
                    ?: return false
            if (message.characterId == character.id) return true
            messageDao.updateMessage(
                message.copy(
                    characterId = character.id,
                    speakerName = character.fullName(),
                ),
            )
            sagaHistoryUseCase.getSagaMetadata(message.sagaId).first()?.let { content.value = it }
            return true
        }

        override suspend fun updateSummary(sceneSummary: SceneSummary) {
            val currentTimeline = content.value?.getCurrentTimeLine() ?: return
            timelineUseCase.updateTimeline(currentTimeline.data.copy(sceneSummary = sceneSummary))
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
