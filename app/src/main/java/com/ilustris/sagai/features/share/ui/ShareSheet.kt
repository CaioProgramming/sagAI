@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.share.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel

@Composable
fun ShareSheet(
    content: SagaContent,
    isVisible: Boolean,
    shareType: ShareType,
    onDismiss: () -> Unit = {},
    viewModel: SharePlayViewModel = hiltViewModel(),
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                onDismiss()
                viewModel.deleteSavedFile()
            },
            sheetState = rememberModalBottomSheetState(true),
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            AnimatedContent(
                shareType,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) {
                when (it) {
                    ShareType.PLAYSTYLE ->
                        PlayStyleShareView(
                            content,
                        )

                    ShareType.HISTORY -> HistoryShareView(content)
                    ShareType.EMOTIONS -> EmotionShareView(content)
                    ShareType.RELATIONS -> RelationsShareView(content)
                }
            }
        }
    }
}
