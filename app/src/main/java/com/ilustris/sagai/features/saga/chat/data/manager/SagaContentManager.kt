package com.ilustris.sagai.features.saga.chat.data.manager

import android.net.Uri
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.presentation.model.PendingAdvance
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.components.SnackBarState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface SagaContentManager {
    val content: MutableStateFlow<com.ilustris.sagai.features.home.data.model.SagaMetadata?>
    val sceneSummary: StateFlow<SceneSummary?>
    val contentUpdateMessages: MutableSharedFlow<Message>
    val ambientMusicFile: StateFlow<File?>
    val replySfxFile: StateFlow<File?>
    val narrativeProcessingUiState: StateFlow<Boolean>
    val contentReasoning: MutableStateFlow<String?>

    var snackBarUpdate: MutableStateFlow<SnackBarState?>

    val milestoneUpdate: MutableStateFlow<SagaMilestone?>
    val isOnboardingVisible: MutableStateFlow<Boolean>

    suspend fun advanceNarrative(pendingAdvance: PendingAdvance)

    suspend fun loadSaga(sagaId: String)

    suspend fun generateCharacter(
        description: String,
        sceneSummary: SceneSummary? = null,
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

    fun dismissMilestone()

    suspend fun continueMilestone()

    suspend fun updatePlaytime(
        sagaId: Int,
        timeInMillis: Long,
    )

    suspend fun showObjective()

    suspend fun getCurrentObjective(sceneSummary: SceneSummary)

    fun stopProcessing()

    suspend fun updateSummary(sceneSummary: SceneSummary)

    suspend fun getSagaContent(): SagaContent?
}
