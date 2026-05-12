@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.share.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel

@Composable
fun ShareSheet(
    saga: Saga,
    isVisible: Boolean,
    shareType: ShareType,
    character: CharacterContent? = null,
    onDismiss: () -> Unit = {},
) {
    val viewModel: SharePlayViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()

    val content by viewModel.sagaContent.collectAsStateWithLifecycle()

    if (isVisible && content != null) {
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
                    ShareType.PLAYSTYLE -> {
                        PlayStyleShareView(
                            saga,
                            viewModel,
                        )
                    }

                    ShareType.CHARACTER -> {
                        character?.let { char ->
                            CharacterShareView(
                                saga,
                                character = char,
                                viewModel,
                            )
                        }
                    }

                    ShareType.HISTORY -> {
                        content?.let {
                            HistoryShareView(it, viewModel)
                        }
                    }

                    ShareType.EMOTIONS -> {
                        EmotionShareView(saga, viewModel)
                    }

                    ShareType.RELATIONS -> {
                        content?.let {
                            RelationsShareView(it, viewModel)
                        }
                    }

                    ShareType.CONVERSATION -> {
                        ConversationShareView(
                            sagaContent = saga,
                            messages = chatViewModel.getSelectedMessages(),
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}
