package com.ilustris.sagai.features.debug.ui

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualImageFallbackSheet(
    prompt: String,
    debugImageFallbackService: DebugImageFallbackService,
    onDismiss: () -> Unit,
) {
    if (!BuildConfig.DEBUG) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            debugImageFallbackService.cancel()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        },
    ) {
        ManualImageFallbackContent(
            prompt = prompt,
            debugImageFallbackService = debugImageFallbackService,
            onSubmitted = onDismiss,
            onCancel = onDismiss,
            showHeader = true,
            modifier =
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
        )
    }
}
