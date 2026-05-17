package com.ilustris.sagai.features.saga.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.wiki.data.model.Wiki

sealed class ChatUiAction {
    data object AdvanceNarrative : ChatUiAction()

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
        val message: Message,
    ) : ChatUiAction()

    data class ReviewEvent(
        val timelineContent: TimelineMetadata,
    ) : ChatUiAction()

    data class ReviewChapter(
        val chapterContent: ChapterMetadata,
    ) : ChatUiAction()

    data class UpdateCharacter(
        val character: Character?,
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

    data class DeleteMessage(
        val message: Message,
    ) : ChatUiAction()

    data class EditMessage(
        val message: Message,
    ) : ChatUiAction()

    data class OpenMessageOptions(
        val message: Message?,
    ) : ChatUiAction()

    data object SaveEdit : ChatUiAction()

    data object CancelEdit : ChatUiAction()

    data object Back : ChatUiAction()

    data object OpenSagaDetails : ChatUiAction()

    data class InjectFakeMessages(
        val count: Int,
    ) : ChatUiAction()

    data object RefreshSaga : ChatUiAction()

    data class EnableBackup(
        val uri: android.net.Uri?,
    ) : ChatUiAction()

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

    data object ContinueMilestone : ChatUiAction()

    data object ShowObjective : ChatUiAction()

    data object StopGeneration : ChatUiAction()

    data class OpenCharacter(
        val characterId: Int,
    ) : ChatUiAction()
}
