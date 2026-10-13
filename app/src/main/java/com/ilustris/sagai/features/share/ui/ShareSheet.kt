@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.share.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel

@Composable
fun ShareSheet(
    // Null only for the player share, which belongs to the player and not to a saga.
    saga: Saga?,
    isVisible: Boolean,
    shareType: ShareType,
    character: CharacterContent? = null,
    onDismiss: () -> Unit = {},
) {
    val viewModel: SharePlayViewModel = hiltViewModel()

    val content by viewModel.sagaContent.collectAsStateWithLifecycle()

    LaunchedEffect(isVisible, saga?.id, shareType, character?.data?.id) {
        if (!isVisible) return@LaunchedEffect
        if (shareType == ShareType.PLAYER) {
            viewModel.generatePlayerShareText()
        } else if (saga != null) {
            viewModel.generateShareText(saga, shareType, character)
        }
    }

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
                label = "share_sheet",
            ) {
                when (it) {
                    ShareType.PLAYSTYLE -> {
                        saga?.let { PlayStyleShareView(it, viewModel) }
                    }

                    ShareType.CHARACTER -> {
                        if (saga != null && character != null) {
                            CharacterShareView(saga, character = character, viewModel)
                        }
                    }

                    ShareType.PLAYER -> {
                        PlayerShareView(viewModel)
                    }

                    ShareType.HISTORY -> {
                        content?.let { sagaContent ->
                            HistoryShareView(sagaContent, viewModel)
                        }
                    }

                    ShareType.EMOTIONS -> {
                        saga?.let { EmotionShareView(it, viewModel) }
                    }

                    ShareType.RELATIONS -> {
                        content?.let { sagaContent ->
                            RelationsShareView(sagaContent, viewModel)
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
