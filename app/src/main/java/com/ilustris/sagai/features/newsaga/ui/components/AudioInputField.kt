package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.audio.RecordingState
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * AudioInputField is a reusable text input component with integrated audio recording.
 *
 * Features:
 * - Text input when empty shows audio recording button
 * - Audio recording with visual feedback
 * - Max 60-second recording limit
 * - Real-time duration display
 * - Send button when has content (text or audio)
 * - Receives only recordingState from parent (clean separation of concerns)
 *
 * Usage:
 * ```kotlin
 * var textInput by remember { mutableStateOf("") }
 * var audioFile by remember { mutableStateOf<File?>(null) }
 *
 * AudioInputField(
 *     textValue = textInput,
 *     onTextChange = { textInput = it },
 *     audioFile = audioFile,
 *     onAudioRecorded = { audioFile = it },
 *     onSend = { text, audio -> },
 *     recordingState = viewModel.recordingState,
 *     remainingTime = viewModel.remainingTime,
 *     onStartRecording = { viewModel.startRecording() },
 *     onStopRecording = { viewModel.stopRecording() }
 * )
 * ```
 */
@Composable
fun AudioInputField(
    textValue: String,
    onTextChange: (String) -> Unit,
    audioFile: File?,
    onAudioRecorded: (File?) -> Unit,
    onSend: (String, File?) -> Unit,
    recordingState: StateFlow<RecordingState>,
    remainingTime: Long,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recordingStateValue = recordingState.collectAsState()

    val isRecording = recordingStateValue.value == RecordingState.RECORDING
    val hasContent = textValue.isNotEmpty() || audioFile != null
    val showAudioButton = textValue.isEmpty() && !isRecording

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        // Text Input Field
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (textValue.isEmpty() && !isRecording) {
                Text(
                    text = "Mensagem...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 16.dp),
                )
            }

            BasicTextField(
                value = textValue,
                onValueChange = onTextChange,
                textStyle =
                    TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                interactionSource = remember { MutableInteractionSource() },
                enabled = !isRecording,
            )
        }

        // Action Button (Audio or Send)
        if (showAudioButton) {
            // Audio Recording Button
            IconButton(
                onClick = {
                    if (!isRecording) {
                        onStartRecording()
                    } else {
                        onStopRecording()
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_spark),
                    contentDescription = "Record audio",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            // Send Button
            IconButton(
                onClick = {
                    onSend(textValue, audioFile)
                },
                modifier = Modifier.padding(start = 8.dp),
                enabled = hasContent,
            ) {
                Icon(
                    painterResource(R.drawable.ic_send),
                    contentDescription = "Send",
                    tint =
                        if (hasContent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                )
            }
        }
    }

    // Recording Duration Display (optional visual feedback)
    if (isRecording) {
        val remainingSeconds = (remainingTime / 1000).coerceAtLeast(0)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Gravando... ${remainingSeconds}s restantes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
