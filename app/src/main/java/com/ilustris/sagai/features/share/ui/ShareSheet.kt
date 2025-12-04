@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.share.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel

@Composable
fun ShareSheet(
    content: SagaContent,
    isVisible: Boolean,
    shareType: ShareType,
    character: CharacterContent? = null,
    onDismiss: () -> Unit = {},
    viewModel: SharePlayViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = {
                viewModel.deleteSavedFile()
                onDismiss()
            },
        ) {
            AnimatedContent(
                shareType,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) {
                when (it) {
                    ShareType.PLAYSTYLE ->
                        PlayStyleShareView(
                            content,
                            viewModel,
                        )

                    ShareType.CHARACTER ->
                        character?.let { char ->
                            CharacterShareView(
                                content,
                                character = char,
                                viewModel,
                            )
                        }
                    ShareType.HISTORY -> HistoryShareView(content, viewModel)
                    ShareType.EMOTIONS -> EmotionShareView(content, viewModel)
                    ShareType.RELATIONS -> RelationsShareView(content, viewModel)
                    ShareType.CONVERSATION -> {
                        ConversationShareView(
                            sagaContent = content,
                            messages = chatViewModel.getSelectedMessages(),
                        )
                    }
                }
            }
        }
    }
}
