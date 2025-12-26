package com.ilustris.sagai.features.saga.chat.presentation

import android.graphics.Bitmap
import androidx.compose.ui.text.input.TextFieldValue
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.ui.components.audio.AudioPlaybackState
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.ui.components.SnackBarState

sealed class ChatState {
    data object Loading : ChatState()

    data object Success : ChatState()

    data class Error(
        val message: String,
    ) : ChatState()
}

data class ChatUiState(
    val chatState: ChatState = ChatState.Loading,
    val sagaContent: SagaContent? = null,
    val messages: List<ActDisplayData> = emptyList(),
    val characters: List<Character> = emptyList(),
    val isGenerating: Boolean = false,
    val isLoading: Boolean = false,
    val showTitle: Boolean = true,
    val isPlaying: Boolean = false,
    val audioPlaybackState: AudioPlaybackState? = null,
    val snackBarMessage: SnackBarState? = null,
    val suggestions: List<Suggestion> = emptyList(),
    val loreUpdateProgress: Float = 0f,
    val selectedCharacter: CharacterContent? = null,
    val revealCharacter: CharacterContent? = null,
    val inputValue: TextFieldValue = TextFieldValue(),
    val senderType: SenderType = SenderType.CHARACTER,
    val typoFixMessage: TypoFix? = null,
    val messageEffectsEnabled: Boolean = true,
    val originalBitmap: Bitmap? = null,
    val segmentedBitmap: Bitmap? = null,
    val newCharacterReveal: Int? = null,
    val selectionState: MessageSelectionState = MessageSelectionState(),
    val notificationsEnabled: Boolean = true,
    val smartSuggestionsEnabled: Boolean = true,
    val showShareSheet: Boolean = false,
    val showAudioTranscript: Boolean = false,
    val isSendingPending: Boolean = false,
    val sendingProgress: Float = 0f,
    val isAudioInput: Boolean = false,
)

data class MessageSelectionState(
    val isSelectionMode: Boolean = false,
    val selectedMessageIds: Set<Int> = emptySet(),
    val maxSelection: Int = 10,
)

data class ActDisplayData(
    val content: ActContent,
    val isComplete: Boolean,
    val chapters: List<ChapterDisplayData>,
)

data class ChapterDisplayData(
    val chapter: ChapterContent,
    val isComplete: Boolean,
    val timelineSummaries: List<TimelineContent>,
)
