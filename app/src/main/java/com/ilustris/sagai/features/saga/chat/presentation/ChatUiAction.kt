package com.ilustris.sagai.features.saga.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki

sealed class ChatUiAction {
    data class SendInput(
        val userConfirmed: Boolean = false,
        val isAudio: Boolean = false,
    ) : ChatUiAction()

    data class UpdateInput(
        val value: TextFieldValue,
    ) : ChatUiAction()

    data class UpdateSenderType(
        val type: SenderType,
    ) : ChatUiAction()

    data class RetryAiResponse(
        val message: Message,
    ) : ChatUiAction()

    data class PlayOrPauseAudio(
        val messageContent: MessageContent,
    ) : ChatUiAction()

    data class RequestNewCharacter(
        val name: String,
    ) : ChatUiAction()

    data class ReviewEvent(
        val timelineContent: TimelineContent,
    ) : ChatUiAction()

    data class ReviewChapter(
        val chapterContent: ChapterContent,
    ) : ChatUiAction()

    data class UpdateCharacter(
        val characterContent: CharacterContent?,
    ) : ChatUiAction()

    data class ShowCharacter(
        val characterContent: CharacterContent?,
    ) : ChatUiAction()

    data class RegenerateAudio(
        val messageContent: MessageContent,
    ) : ChatUiAction()

    data object ToggleSelectionMode : ChatUiAction()

    data class ToggleMessageSelection(
        val messageId: Int,
    ) : ChatUiAction()

    data object ClearSelection : ChatUiAction()

    data class ShareConversation(
        val show: Boolean,
    ) : ChatUiAction()

    data object Back : ChatUiAction()

    data object OpenSagaDetails : ChatUiAction()

    data class InjectFakeMessages(
        val count: Int,
    ) : ChatUiAction()

    data object RefreshSaga : ChatUiAction()

    data object DismissSnackBar : ChatUiAction()

    data class EnableBackup(
        val uri: android.net.Uri?,
    ) : ChatUiAction()

    data object DismissCharacterReveal : ChatUiAction()

    data class RequestAudioTranscript(
        val show: Boolean,
    ) : ChatUiAction()

    data class ReviewWiki(
        val wikis: List<Wiki>,
    ) : ChatUiAction()

    data class AppendWiki(
        val wiki: Wiki,
    ) : ChatUiAction()

    data object DismissMilestone : ChatUiAction()
}
