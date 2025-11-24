package com.ilustris.sagai.features.saga.chat.data.manager

import android.net.Uri
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.components.SnackBarState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface SagaContentManager {
    val content: MutableStateFlow<SagaContent?>
    val contentUpdateMessages: MutableSharedFlow<Message>
    val ambientMusicFile: StateFlow<File?>
    val narrativeProcessingUiState: StateFlow<Boolean>

    var snackBarUpdate: MutableStateFlow<SnackBarState?>

    suspend fun loadSaga(sagaId: String)

    suspend fun generateCharacter(description: String): RequestResult<Character>

    suspend fun generateCharacterImage(character: Character): RequestResult<Character>

    fun getDirective(): String

    fun setDebugMode(enabled: Boolean)

    fun isInDebugMode(): Boolean

    fun setProcessing(bool: Boolean)

    fun checkNarrativeProgression(
        saga: SagaContent?,
        isRetrying: Boolean = false,
    )

    suspend fun regenerateTimeline(
        saga: SagaContent,
        timelineContent: TimelineContent,
    )

    suspend fun reviewWiki(wikiItems: List<Wiki>)

    suspend fun reviewEvent(timelineContent: TimelineContent)

    suspend fun backupSaga()

    suspend fun enableBackup(uri: Uri?)

    suspend fun reviewChapter(chapterContent: ChapterContent)

    suspend fun updatePlaytime(
        sagaId: Int,
        timeInMillis: Long,
    )
}
