package com.ilustris.sagai.features.saga.chat.data.manager

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.GeneratedContentWithLore
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.data.isFlowCancellation
import com.ilustris.sagai.core.database.SagaDatabase
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
import com.ilustris.sagai.features.home.data.model.toNarrativeMetadata
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.data.model.AIReply
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
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionMode
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeExecutionResult
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativePhase
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeProcessingGate
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeUiState
import com.ilustris.sagai.features.saga.chat.domain.manager.TIMELINE_ALREADY_ACTIVE_MESSAGE
import com.ilustris.sagai.features.saga.chat.domain.manager.executionMode
import com.ilustris.sagai.features.saga.chat.domain.manager.narrativelyCompleteTimeline
import com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import com.ilustris.sagai.ui.components.island.ChatIslandService
import com.ilustris.sagai.ui.components.island.IslandContent
import com.ilustris.sagai.ui.components.island.LoadingIslandContent
import com.ilustris.sagai.ui.components.island.ObjectiveIslandContent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        private val database: SagaDatabase,
        private val sagaThemeManager: SagaThemeManager,
        private val sagaImmersiveSession: SagaImmersiveSession,
        private val narrativeCoordinator: NarrativeCoordinator,
        private val narrativeActionExecutor: NarrativeActionExecutor,
        private val narrativeProcessingGate: NarrativeProcessingGate,
        private val stringResourceHelper: StringResourceHelper,
        private val globalShellService: GlobalShellService,
        private val chatIslandService: ChatIslandService,
        private val sagaNavigationTracker: SagaNavigationTracker,
        @ApplicationContext
        private val context: Context,
    ) : SagaContentManager {
        override val contentReasoning = MutableStateFlow<String?>(null)
        override val content = MutableStateFlow<SagaMetadata?>(null)
        private val _sceneSummary = MutableStateFlow<SceneSummary?>(null)
        override val sceneSummary: StateFlow<SceneSummary?> = _sceneSummary.asStateFlow()
        override val milestoneUpdate = MutableStateFlow<SagaMilestone?>(null)
        private val _milestoneChainReady =
            MutableSharedFlow<Int>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        override val milestoneChainReady: SharedFlow<Int> = _milestoneChainReady
        private val _showObjectiveOverlay = MutableStateFlow(false)
        override val showObjectiveOverlay: StateFlow<Boolean> = _showObjectiveOverlay.asStateFlow()

        // Advance-trigger bottom island gating that's purely a UI concern (onboarding overlays,
        // message-selection mode, chat-reply generation) — forwarded by the chat screen via
        // setAdvanceTriggerSuppressed rather than derived here.
        private val _advanceTriggerSuppressed = MutableStateFlow(false)

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
        private var milestoneReadinessObserverJob: kotlinx.coroutines.Job? = null
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
            // Detached onto managerScope (then joined) rather than run straight on the caller's
            // coroutine — MilestoneViewModel's auto-advance effect calls this from its own
            // viewModelScope, and if that scope dies mid-generation (screen torn down,
            // backgrounded and trimmed) before executeNarrativeAction reaches
            // onActionCompleted(), narrativeCoordinator's phase gets stuck at Processing(action)
            // forever — every later reevaluate() early-returns on a Processing phase, so nothing
            // ever progresses again until the process restarts. managerScope is @Singleton-scoped
            // (mirrors ChatGenerationService/BookGenerationService's own reasoning for why their
            // generation is singleton-scoped, not screen-scoped) so it keeps running to a real
            // completion regardless of what happens to the caller.
            managerScope.launch { executeNarrativeAction(action, isRetry = false) }.join()
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
            // False only when called from inside requestNarrativeProgression()'s own automatic-
            // action loop (progressionMutex already held on this coroutine) — Mutex.withLock
            // isn't reentrant, so self-chaining back into requestNarrativeProgression() from
            // there would deadlock. The loop re-decides the next step itself once this call
            // returns, so the self-chain would be redundant anyway.
            chainNext: Boolean = true,
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

                // CreateTimeline is automatic — several reactive triggers (milestone dismissal,
                // loading state, the explicit continue call) can each independently resolve it
                // before this manager's cached saga snapshot catches up with the first one's
                // write. The executor throws when it finds a timeline already active as a
                // self-healing signal, not a real failure: the desired end state (chapter has a
                // current timeline) is already true, so surfacing an error + retry snackbar here
                // would be actively wrong. Treat it as a silent no-op instead.
                if (result is NarrativeExecutionResult.Failure &&
                    action is NarrativeAction.CreateTimeline &&
                    result.message == TIMELINE_ALREADY_ACTIVE_MESSAGE
                ) {
                    Timber.i("CreateTimeline raced another trigger and found a timeline already active — ignoring.")
                    narrativeCoordinator.onActionCompleted(
                        action,
                        NarrativeExecutionResult.Success(value = null, shouldEmitMilestone = false),
                    )
                    return
                }

                narrativeCoordinator.onActionCompleted(action, result)
                when (result) {
                    is NarrativeExecutionResult.Success -> {
                        handlePostAction(sagaMetadata, action, result.value)
                        awaitMilestoneDismissalIfNeeded()
                        if (chainNext) {
                            requestNarrativeProgression(isRetry = false)
                        }
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
                if (chainNext && narrativeCoordinator.consumePendingReevaluation()) {
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
                        if (milestoneReadinessObserverJob == null || milestoneReadinessObserverJob?.isActive == false) {
                            milestoneReadinessObserverJob = observeMilestoneChainReadiness()
                        }
                        if (reasoningObserverJob == null || reasoningObserverJob?.isActive == false) {
                            reasoningObserverJob = observeReasoning()
                        }
                        if (islandObserverJob == null || islandObserverJob?.isActive == false) {
                            islandObserverJob = observeIslands()
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
                                        // quote is the only thing this milestone has to show (see
                                        // MilestoneIntroductionContent — titleText is blank for
                                        // RESUME by design). A null/blank quote used to still
                                        // reveal the milestone with an empty introduction string,
                                        // which meant SimpleTypewriterText never rendered, its
                                        // onAnimationFinished callback that unlocks the Continue
                                        // button never fired, and the player got stuck on a blank,
                                        // un-backable screen forever. Skip the reveal entirely
                                        // instead — same as the already-null-sceneSummary case —
                                        // and let the player land straight in chat.
                                        val sceneSummary = saga.getCurrentTimeLine()?.data?.sceneSummary
                                        val quote = sceneSummary?.quote?.takeIf { it.isNotBlank() }
                                        if (sceneSummary != null && quote != null) {
                                            emitMilestone(
                                                SagaMilestone.Introduction(
                                                    type = IntroductionType.RESUME,
                                                    titleText = emptyString(),
                                                    introduction = quote,
                                                    number =
                                                        stringResourceHelper.getString(
                                                            R.string.chapter_number_label,
                                                            saga
                                                                .chapterNumber(
                                                                    saga.currentChapterInfo?.data?.id,
                                                                ).toRoman(),
                                                        ),
                                                    sceneSummary = sceneSummary,
                                                ),
                                            )
                                        } else {
                                            emitMilestone(null)
                                        }
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

        /**
         * The single source that turns "a narrative chain step is ready" into a one-shot signal.
         * Replaces the old observeMilestone()/observeLoading() reactive re-triggers, which each
         * independently called requestNarrativeProgression() and duplicated the retrigger
         * executeNarrativeAction() already performs itself once a milestone is dismissed (see its
         * awaitMilestoneDismissalIfNeeded() call, below). Detection stays reactive; execution is
         * now driven exclusively by explicit advanceNarrative()/continueMilestone() calls from
         * whoever owns the Milestone screen — nothing here auto-executes anything.
         *
         * Two independent sources feed the same signal: (1) the coordinator entering
         * AwaitingAdvance — a chain step (event/chapter/act close, or their intros) is ready to
         * execute; (2) milestoneUpdate itself going non-null/intrusive — covers the resume
         * "welcome back" Introduction emitted directly from loadSaga(), which never touches the
         * coordinator's phase at all. NewCharacter is deliberately excluded — it stays a passive
         * GlobalShellEffect toast, not a chain step. Redundant emissions while already on the
         * Milestone screen are harmless: the MainActivity collector only acts while
         * isOnChatForSaga is true, which stops being the case the moment it navigates there.
         *
         * Doesn't gate on chat-reply generation here — ChatGenerationService transitively depends
         * back on SagaContentManager (via MessageUseCase), so injecting it here is a Dagger
         * dependency cycle. That gating lives in MainActivity's collector instead, which can see
         * both without the cycle.
         *
         * Also recombined with [SagaNavigationTracker.currentKey] rather than only with the
         * readiness edge: the emitted signal has no replay, so if it fired while the user was on a
         * different screen (mid-reply, on another saga, on the home screen), MainActivity's
         * isOnChatForSaga gate silently swallowed it and nothing ever asked again — the only way
         * back in was a fresh ChatViewModel (leave the saga, come back) forcing a full re-init.
         * Folding navigation into this combine means returning to this saga's chat re-derives
         * "ready" from the persisted coordinator/milestone state and re-emits, instead of relying
         * on a one-shot pulse that's already been discarded.
         */
        private fun observeMilestoneChainReadiness() =
            managerScope.launch {
                val awaitingAdvance =
                    narrativeCoordinator.uiState
                        .map { it.phase is NarrativePhase.AwaitingAdvance }
                        .distinctUntilChanged()
                val freshReveal =
                    milestoneUpdate
                        .map { milestone ->
                            milestone != null &&
                                milestone.isIntrusive &&
                                milestone !is SagaMilestone.Loading &&
                                milestone !is SagaMilestone.NewCharacter
                        }.distinctUntilChanged()

                val ready =
                    combine(awaitingAdvance, freshReveal) { advance, reveal -> advance || reveal }
                        .distinctUntilChanged()

                combine(ready, content, sagaNavigationTracker.currentKey) { isReady, saga, _ ->
                    val sagaId = saga?.data?.id
                    if (isReady && sagaId != null && sagaNavigationTracker.isOnChatForSaga(sagaId)) {
                        sagaId
                    } else {
                        null
                    }
                }.distinctUntilChanged()
                    .collectLatest { sagaId ->
                        if (sagaId != null) {
                            _milestoneChainReady.emit(sagaId)
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
        private fun observeIslands(): kotlinx.coroutines.Job =
            managerScope.launch {
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

        /** Same `messages / loreUpdateLimit` fraction the chat's own progress ring used to
         * compute — now feeding the objective island's compact progress ring instead. */
        private suspend fun currentObjectiveProgress(saga: SagaMetadata): Float {
            if (saga.data.isEnded) return 1f
            val timeline = saga.getCurrentTimeLine() ?: return 0f
            val updateLimit = fetchNarrativeRules().loreUpdateLimit
            return (timeline.messages.size.toFloat() / updateLimit.toFloat()).coerceIn(0f, 1f)
        }

        /**
         * Ambient status only — loading pulse or the current objective. Milestone reveals
         * (Introduction/NewEvent/ChapterFinished/ActFinished) and the mandatory narrative-advance
         * step now live entirely on the dedicated Milestone screen (see [MilestoneViewModel]),
         * auto-driven from [milestoneChainReady] rather than shown/gated from the island. The
         * island never blocks progress again — if it isn't ambient, it doesn't belong here.
         */
        private suspend fun publishIslands(
            saga: SagaMetadata?,
            milestone: SagaMilestone?,
            @Suppress("UNUSED_PARAMETER") narrativeState: NarrativeUiState,
            reasoning: String?,
            @Suppress("UNUSED_PARAMETER") advanceSuppressed: Boolean,
        ) {
            if (saga == null) {
                chatIslandService.setTop(null)
                chatIslandService.setBottom(null)
                return
            }
            val genre = saga.data.genre

            val topContent: IslandContent? =
                when {
                    milestone is SagaMilestone.Loading -> {
                        LoadingIslandContent(reasoning = reasoning, genre = genre)
                    }

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
            chatIslandService.setBottom(null)
        }

        override fun checkNarrativeProgression(
            saga: SagaMetadata?,
            isRetrying: Boolean,
        ) {
            managerScope.launch {
                requestNarrativeProgression(isRetry = isRetrying, fallbackSaga = saga)
            }
        }

        override suspend fun pruneOrphanTimelines() {
            val chapter = content.value?.currentChapterInfo ?: return
            val rules = fetchNarrativeRules()
            val currentId = chapter.data.currentEventId
            val orphans =
                chapter.events.filter { event ->
                    event.data.id != currentId && !event.narrativelyCompleteTimeline(rules)
                }
            if (orphans.isEmpty()) return
            Timber.w(
                "pruneOrphanTimelines: chapter ${chapter.data.id} has ${orphans.size} orphaned " +
                    "timeline(s) (neither current nor closed) — deleting, likely a leftover from a " +
                    "past CreateTimeline race.",
            )
            orphans.forEach { timelineUseCase.deleteTimeline(it.data) }
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

            // Whether another reevaluation was queued *while we held the lock*. Consumed here but
            // acted on below, outside withLock — recursing from inside it would always see the
            // mutex as locked (by ourselves) and just re-queue forever without ever re-checking.
            var shouldReevaluateAgain = false
            progressionMutex.withLock {
                // Automatic actions (currently only CreateTimeline — see NarrativeCoordinator's
                // executionMode()) are decided AND executed inside this single lock hold, in a
                // loop, instead of deciding here and executing back in the caller after releasing
                // the lock like before. That gap used to be a real race: a second concurrent
                // caller (e.g. ChatViewModel's edge-trigger firing right as this chain's
                // CloseTimeline finished) could read the exact same "no active timeline" DB
                // snapshot this call had already decided to act on, and kick off its own
                // duplicate CreateTimeline — the executor's "timeline already active" guard only
                // catches the case where the loser's write lands after the winner's; if it lands
                // first instead, both succeed and one timeline is orphaned (inactive, silently
                // eating messages nobody reads back). Executing here means no other caller can
                // ever observe the pre-execution snapshot as "current" again.
                var automaticStepGuard = 0
                while (true) {
                    val currentSaga = content.value ?: fallbackSaga ?: return@withLock

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

                    // sagaContent is a fresh DB read (getSagaContent() above); currentSaga is the
                    // cached content StateFlow, which is fed by a Room Flow and can lag a write
                    // from a sibling coroutine by a beat (e.g. continueMilestone() clearing
                    // currentEventId right before this runs). Deciding off the stale snapshot here
                    // is what let a moment-long CloseTimeline/EvolveTimeline resolve flash the
                    // advance pill before the correct CreateTimeline resolve (from the next,
                    // now-caught-up call) replaced it — same underlying race as the
                    // duplicate-timeline guard, just cosmetic instead of thrown.
                    val intent = NarrativeCheck.validateProgressionMetadata(sagaContent.toNarrativeMetadata(), rules)
                    val hydrated = intent?.let { NarrativeActionMaterializer.materialize(it, sagaContent) }
                    if (intent != null && hydrated == null) {
                        Timber.w(
                            "requestNarrativeProgression: metadata implies progression (${intent.javaClass.simpleName}) but SagaContent hydration failed.",
                        )
                        narrativeCoordinator.schedulePendingReevaluation()
                        return@withLock
                    }

                    val isAutomatic = hydrated?.executionMode() == NarrativeExecutionMode.Automatic
                    narrativeCoordinator.reevaluate(
                        nextResolvedAction = hydrated,
                        context = buildEvaluationContext(),
                        isAutomatic = isAutomatic,
                    )
                    shouldReevaluateAgain = narrativeCoordinator.consumePendingReevaluation()

                    if (isAutomatic && hydrated != null) {
                        if (++automaticStepGuard > 10) {
                            Timber.e(
                                "requestNarrativeProgression: automatic-action loop exceeded 10 steps " +
                                    "(likely stuck re-materializing the same action) — bailing to avoid " +
                                    "hanging progressionMutex forever.",
                            )
                            narrativeCoordinator.schedulePendingReevaluation()
                            return@withLock
                        }
                        executeNarrativeAction(hydrated, isRetry = false, chainNext = false)
                        continue
                    }

                    return@withLock
                }
            }

            if (shouldReevaluateAgain) {
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
                    // Unlike the other milestone types, an Introduction never goes through
                    // executeNarrativeAction — the "welcome back" RESUME case in particular is
                    // emitted directly from loadSaga(), so nothing else re-checks progression
                    // after it's dismissed. Without this, dismissing it while the message limit
                    // was already hit before the saga even opened just silently drops back to
                    // chat instead of continuing into the pending EvolveTimeline. Detached onto
                    // managerScope for the same reason as advanceNarrative() above — this can
                    // tail into an automatic CreateTimeline execution, and that shouldn't get cut
                    // short just because the caller's screen went away mid-call.
                    managerScope.launch { requestNarrativeProgression(isRetry = false, fallbackSaga = saga) }.join()
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
                    if (milestone.shouldPlaySoundFx) {
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
                    // Silent scaffold step — this timeline is empty (no messages, no generated
                    // lore yet), so there's nothing to reveal. Emitting a NewEvent milestone here
                    // (like a prior version of this code did) made continueMilestone() clear the
                    // brand-new currentEventId right back to null, cascading into another
                    // CreateTimeline and so on. The inherited scene summary/objective already
                    // surfaces on its own via the top island's ObjectiveIslandContent once
                    // `content` catches up with the chapter's new currentEventId.
                    val timeline = resultValue as? Timeline
                    if (timeline != null) {
                        timeline.sceneSummary?.let { _sceneSummary.value = it }
                    } else {
                        dismissMilestone()
                    }
                }

                is NarrativeAction.EvolveTimeline -> {
                    val generatedContent = resultValue as? GeneratedContentWithLore<Timeline>
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
                                    characters = generatedContent?.characters ?: emptyList(),
                                    wikis = generatedContent?.wikis ?: emptyList(),
                                ),
                            )
                        }
                    } ?: dismissMilestone()
                }

                is NarrativeAction.GenerateChapter -> {
                    val generatedContent = resultValue as? GeneratedContentWithLore<Chapter>
                    val chapter = generatedContent?.data ?: resultValue as? Chapter
                    val message = generatedContent?.finalMessage
                    chapter?.let { c ->
                        getSagaContent()?.let { fullSaga ->
                            emitMilestone(
                                SagaMilestone.ChapterFinished(
                                    chapter = c,
                                    messageText = message,
                                    sagaContent = fullSaga,
                                    characters = generatedContent?.characters ?: emptyList(),
                                    wikis = generatedContent?.wikis ?: emptyList(),
                                ),
                            )
                        }
                    } ?: dismissMilestone()
                }

                is NarrativeAction.GenerateAct -> {
                    val generatedContent = resultValue as? GeneratedContentWithLore<Act>
                    val act = generatedContent?.data ?: resultValue as? Act
                    val message = generatedContent?.finalMessage
                    act?.let { a ->
                        emitMilestone(
                            SagaMilestone.ActFinished(
                                act = a,
                                messageText = message,
                                characters = generatedContent?.characters ?: emptyList(),
                                wikis = generatedContent?.wikis ?: emptyList(),
                            ),
                        )
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

                val discovery = reply.newCharacter
                val speaker = savedMessage.speakerName
                // Whether the newcomer is also the one holding this line. They often are, but not
                // always: a character can enter the fiction on a turn where someone else speaks —
                // the player touches a stranger's shoulder and that stranger now exists, without
                // having said a word yet. Treating the two as the same event is what made this
                // function either lose the newcomer or hand them somebody else's dialogue.
                val discoveryIsSpeaker =
                    discovery != null &&
                        speaker != null &&
                        speaker.trim().equals(discovery.name.trim(), ignoreCase = true)

                val linkCandidates =
                    listOfNotNull(
                        savedMessage.speakerName,
                        discovery?.name.takeIf { discoveryIsSpeaker },
                    ).distinctBy { it.trim().lowercase() }

                var lineAttributed = savedMessage.characterId != null
                if (!lineAttributed) {
                    for (candidateName in linkCandidates) {
                        if (
                            linkMessageToExistingCharacter(
                                saga = freshSaga,
                                message = savedMessage,
                                candidateName = candidateName,
                            )
                        ) {
                            lineAttributed = true
                            break
                        }
                    }
                }

                if (discovery == null) return@launch
                // Attributing the line to an existing speaker used to end the whole routine, which
                // silently dropped any newcomer introduced on that same turn.
                if (lineAttributed && discoveryIsSpeaker) return@launch
                if (freshSaga.findCharacter(discovery.name) != null) return@launch
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
                        // The line only becomes theirs when they are the one who spoke. Relabelling
                        // unconditionally is how a newcomer ended up credited with dialogue another
                        // character had just delivered.
                        if (discoveryIsSpeaker && !lineAttributed) {
                            // No manual content.value re-publish here: the write below already
                            // invalidates the messages table, and loadSaga()'s collector re-emits on
                            // its own. Doing both meant two UI updates for one change.
                            messageDao.updateMessage(
                                savedMessage.copy(
                                    characterId = character.id,
                                    speakerName = character.fullName(),
                                ),
                            )
                        }
                    }

                    is RequestResult.Error -> {
                        Timber.w(
                            result.value,
                            "Failed to generate character for reply message ${savedMessage.id}",
                        )
                        if (lineAttributed) return@launch
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
            val unlinked =
                latestSaga.flatMessages().filter { messageContent ->
                    val message = messageContent.message
                    message.characterId == null &&
                        message.senderType == SenderType.CHARACTER &&
                        !message.speakerName.isNullOrBlank()
                }
            if (unlinked.isEmpty()) return
            // One transaction for the whole backfill. Left as separate writes this loop fired one
            // Room invalidation per message, and each of those pushed a full chat-list rebuild.
            database.withTransaction {
                for (messageContent in unlinked) {
                    val message = messageContent.message
                    linkMessageToExistingCharacter(
                        saga = latestSaga,
                        message = message,
                        candidateName = message.speakerName,
                    )
                }
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
            // The Room flow in loadSaga() picks this write up by itself — re-publishing
            // content.value here just doubled every emission.
            messageDao.updateMessage(
                message.copy(
                    characterId = character.id,
                    speakerName = character.fullName(),
                ),
            )
            return true
        }

        override suspend fun updateSummary(sceneSummary: SceneSummary) {
            val currentTimeline = content.value?.getCurrentTimeLine() ?: return
            timelineUseCase.updateTimeline(currentTimeline.data.copy(sceneSummary = sceneSummary))
        }

        private suspend fun handleStreamingState(state: StreamingState<GeneratedContentWithLore<*>>) {
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
                                timeline = data,
                                emotionalMascot = null,
                                messageText = state.data.finalMessage,
                                sagaContent = getSagaContent()!!,
                                characters = state.data.characters,
                                wikis = state.data.wikis,
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
