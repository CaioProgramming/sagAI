package com.ilustris.sagai.features.saga.chat.presentation

import com.ilustris.sagai.features.characters.data.model.CharacterContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatStateManager {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun updateState(updater: (ChatUiState) -> ChatUiState) {
        _uiState.update(updater)
    }

    fun updateSnackBar(snackBarState: com.ilustris.sagai.ui.components.SnackBarState?) {
        _uiState.update { it.copy(snackBarMessage = snackBarState) }
    }

    fun updateLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun updateGenerating(generating: Boolean) {
        _uiState.update { it.copy(isGenerating = generating) }
    }

    fun updateInput(value: androidx.compose.ui.text.input.TextFieldValue) {
        _uiState.update { it.copy(inputValue = value) }
    }

    fun updateSenderType(type: com.ilustris.sagai.features.saga.chat.data.model.SenderType) {
        _uiState.update { it.copy(senderType = type) }
    }

    fun toggleMessageSelection(messageId: Int) {
        _uiState.update { currentState ->
            val selection = currentState.selectionState
            val currentSelected = selection.selectedMessageIds
            val newSelected =
                if (currentSelected.contains(messageId)) {
                    currentSelected - messageId
                } else {
                    if (currentSelected.size < selection.maxSelection) {
                        currentSelected + messageId
                    } else {
                        currentSelected
                    }
                }
            currentState.copy(
                selectionState =
                    selection.copy(
                        selectedMessageIds = newSelected,
                        isSelectionMode = newSelected.isNotEmpty(),
                    ),
            )
        }
    }

    fun toggleSelectionMode() {
        _uiState.update { currentState ->
            val isSelectionMode = !currentState.selectionState.isSelectionMode
            currentState.copy(
                selectionState =
                    currentState.selectionState.copy(
                        isSelectionMode = isSelectionMode,
                        selectedMessageIds = if (isSelectionMode) currentState.selectionState.selectedMessageIds else emptySet(),
                    ),
            )
        }
    }

    fun clearSelection() {
        _uiState.update { currentState ->
            currentState.copy(
                selectionState =
                    currentState.selectionState.copy(
                        isSelectionMode = false,
                        selectedMessageIds = emptySet(),
                    ),
            )
        }
    }

    fun updateCharacter(content: CharacterContent?) {
        _uiState.update {
            it.copy(selectedCharacter = content)
        }
    }

    fun updateShareSheetVisibility(show: Boolean) {
        _uiState.update { it.copy(showShareSheet = show) }
    }

    fun updateAudioTranscriptVisibility(show: Boolean) {
        _uiState.update { it.copy(showAudioTranscript = show) }
    }

    fun updateSendingPending(pending: Boolean) {
        _uiState.update { it.copy(isSendingPending = pending) }
    }

    fun updateSendingProgress(progress: Float) {
        _uiState.update { it.copy(sendingProgress = progress) }
    }

    fun updateAudioInput(isAudio: Boolean) {
        _uiState.update { it.copy(isAudioInput = isAudio) }
    }

    fun updateMilestone(milestone: com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone?) {
        _uiState.update { it.copy(milestone = milestone) }
    }
}
