package com.ilustris.sagai.features.saga.chat.presentation

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.presentation.model.PendingAdvance
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.audio.AudioPlaybackState
import com.ilustris.sagai.features.timeline.domain.TimelineCardContent
import com.ilustris.sagai.features.wiki.data.model.WikiGroup
import com.ilustris.sagai.ui.components.SnackBarState

sealed class ChatState {
    data object Loading : ChatState()

    data object Success : ChatState()

    data class Error(
        val message: String,
    ) : ChatState()
}

@Immutable
data class ChatUiState(
    val chatState: ChatState = ChatState.Loading,
    val sagaContent: SagaContent? = null,
    val messages: List<ActDisplayData> = emptyList(),
    val characters: List<CharacterContent> = emptyList(),
    val topCharacters: List<CharacterContent> = emptyList(),
    val mainCharacter: CharacterContent? = null,
    val wikis: List<Wiki> = emptyList(),
    val activeGenre: Genre? = null,
    val flatEvents: List<Timeline> = emptyList(),
    val isGenerating: Boolean = false,
    val isLoading: Boolean = false,
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
    val sceneSummary: SceneSummary? = null,
    val milestone: SagaMilestone? = null,
    val editingMessage: Message? = null,
    val showMessageOptions: Message? = null,
    val visualConfig: GenreVisualConfig? = null,
    val onboardingType: OnboardingType? = null,
    val pendingAdvance: PendingAdvance? = null,
    val reasoningChunk: String? = null,
    val wikiGroups: List<WikiGroup> = emptyList(),
    val maxContentLength: Int = 2000,
)

@Immutable
data class MessageSelectionState(
    val isSelectionMode: Boolean = false,
    val selectedMessageIds: Set<Int> = emptySet(),
    val maxSelection: Int = 10,
)

@Immutable
data class ActDisplayData(
    val content: ActContent,
    val isComplete: Boolean,
    val chapters: List<ChapterDisplayData>,
)

@Immutable
data class ChapterDisplayData(
    val chapter: ChapterContent,
    val isComplete: Boolean,
    val timelineSummaries: List<TimelineDisplayData>,
)

@Immutable
data class TimelineDisplayData(
    val isComplete: Boolean,
    val timeline: TimelineCardContent,
)
