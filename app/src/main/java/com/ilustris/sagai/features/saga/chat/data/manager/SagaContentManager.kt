package com.ilustris.sagai.features.saga.chat.data.manager

import android.net.Uri
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.AIReply
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeUiState
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.wiki.data.model.Wiki
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SagaContentManager {
    val content: MutableStateFlow<com.ilustris.sagai.features.home.data.model.SagaMetadata?>
    val sceneSummary: StateFlow<SceneSummary?>
    val contentUpdateMessages: MutableSharedFlow<Message>
    val narrativeProcessingUiState: StateFlow<Boolean>
    val narrativeUiState: StateFlow<NarrativeUiState>
    val contentReasoning: MutableStateFlow<String?>

    val milestoneUpdate: MutableStateFlow<SagaMilestone?>
    val showObjectiveOverlay: StateFlow<Boolean>
    val isOnboardingVisible: MutableStateFlow<Boolean>

    /**
     * One-shot signal (sagaId), emitted once per rising edge into
     * [com.ilustris.sagai.features.saga.chat.domain.manager.NarrativePhase.AwaitingAdvance] — a
     * narrative chain step is ready and waiting. The only consumer should be the single
     * navigation collector that opens the Milestone screen; nothing else should react to this.
     */
    val milestoneChainReady: SharedFlow<Int>

    suspend fun advanceNarrative()

    suspend fun completeGameplayOnboarding(saga: com.ilustris.sagai.features.home.data.model.SagaMetadata?)

    suspend fun loadSaga(sagaId: String)

    suspend fun generateCharacter(
        description: String,
        sceneSummary: SceneSummary? = null,
        candidateName: String? = null,
    ): RequestResult<Character>

    suspend fun generateCharacterImage(character: Character): RequestResult<Character>

    fun setDebugMode(enabled: Boolean)

    fun isInDebugMode(): Boolean

    suspend fun setProcessing(bool: Boolean)

    fun checkNarrativeProgression(
        saga: com.ilustris.sagai.features.home.data.model.SagaMetadata?,
        isRetrying: Boolean = false,
    )

    suspend fun regenerateTimeline(
        saga: com.ilustris.sagai.features.home.data.model.SagaMetadata,
        timelineContent: com.ilustris.sagai.features.home.data.model.TimelineMetadata,
    )

    suspend fun reviewWiki(wikiItems: List<Wiki>)

    suspend fun reviewEvent(timelineContent: com.ilustris.sagai.features.home.data.model.TimelineMetadata)

    suspend fun backupSaga()

    suspend fun enableBackup(uri: Uri?)

    suspend fun reviewChapter(chapterContent: com.ilustris.sagai.features.home.data.model.ChapterMetadata)

    val isMilestoneActive: StateFlow<Boolean>

    /**
     * Suppresses the advance-trigger bottom island while `true` — for gating that's purely a UI
     * concern the manager wouldn't otherwise know about (onboarding overlays, message-selection
     * mode). The chat screen forwards these as they change; the manager owns everything else
     * about when/what island to publish.
     */
    fun setAdvanceTriggerSuppressed(suppressed: Boolean)

    fun dismissMilestone()

    suspend fun continueMilestone()

    suspend fun updatePlaytime(
        sagaId: Int,
        timeInMillis: Long,
    )

    suspend fun showObjective()

    fun dismissObjective()

    suspend fun getCurrentObjective(sceneSummary: SceneSummary)

    fun stopProcessing()

    fun resetSagaSession()

    suspend fun updateSummary(sceneSummary: SceneSummary)

    suspend fun getSagaContent(): SagaContent?

    /** Links unlinked character messages and generates new characters from [reply] in background. */
    fun resolveReplyCharacterLinks(
        saga: com.ilustris.sagai.features.home.data.model.SagaMetadata,
        reply: AIReply,
        savedMessage: Message,
        sceneSummary: SceneSummary?,
    )

    fun linkUnlinkedCharacterMessages(saga: com.ilustris.sagai.features.home.data.model.SagaMetadata)
}
