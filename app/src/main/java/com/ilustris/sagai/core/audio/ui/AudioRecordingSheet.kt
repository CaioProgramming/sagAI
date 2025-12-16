@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.core.audio.ui

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.core.permissions.PermissionComponent
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.holographicGradient

@Composable
fun AudioRecordingSheet(
    brush: List<Color> = holographicGradient,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
) {
    val viewModel: AudioTranscriptionViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionLauncher =
        PermissionService.rememberPermissionLauncher {
            if (it) {
                viewModel.initTranscription()
            }
        }
    AnimatedContent(state) {
        when (it) {
            is AudioState.Error -> {
                StarryLoader(
                    true,
                    it.message,
                    brushColors = brush,
                )
            }

            is AudioState.Loading -> {
                StarryLoader(
                    it is AudioState.Loading,
                    it.message,
                    brushColors = brush,
                )
            }

            AudioState.PermissionRequired -> {
                PermissionComponent(onConfirm = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }, onDismiss = {
                    onDismiss()
                    viewModel.reset()
                })
            }

            else -> {
                Box {}
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initTranscription()
    }

    LaunchedEffect(state) {
        if (state is AudioState.TranscriptionEnded) {
            val message = (state as AudioState.TranscriptionEnded).message
            onSuccess(message)
            viewModel.reset()
        }
    }
}
