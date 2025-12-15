package com.ilustris.sagai.features.newsaga.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.audio.RecordingState
import com.ilustris.sagai.ui.theme.SagAIScaffold
import kotlinx.coroutines.flow.MutableStateFlow
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
 * - Receives only recordingState (StateFlow) - clean separation
 * - Callbacks for start/stop recording and audio received
 * - Optional leading content (e.g., character avatar)
 * - Optional bottom content (e.g., action buttons)
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
    leadingContent: (@Composable () -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null,
) {
    val recordingStateValue = recordingState.collectAsState()

    val isRecording = recordingStateValue.value == RecordingState.RECORDING
    val hasContent = textValue.isNotEmpty() || audioFile != null
    val showAudioButton = textValue.isEmpty() && !isRecording

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            // Leading Content (optional - e.g., character avatar)
            if (leadingContent != null) {
                Box(
                    modifier = Modifier.padding(end = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    leadingContent()
                }
            }

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

        // Bottom Content (optional - e.g., action buttons)
        if (bottomContent != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                bottomContent()
            }
        }
    }
}

// ============= Previews =============

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AudioInputFieldPreviewEmpty() {
    SagAIScaffold {
        val textState = remember { mutableStateOf("") }
        AudioInputField(
            textValue = textState.value,
            onTextChange = { textState.value = it },
            audioFile = null,
            onAudioRecorded = {},
            onSend = { _, _ -> },
            recordingState = MutableStateFlow(RecordingState.IDLE),
            remainingTime = 60000L,
            onStartRecording = {},
            onStopRecording = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AudioInputFieldPreviewWithText() {
    SagAIScaffold {
        val textState = remember { mutableStateOf("Olá! Como você está?") }
        AudioInputField(
            textValue = textState.value,
            onTextChange = { textState.value = it },
            audioFile = null,
            onAudioRecorded = {},
            onSend = { _, _ -> },
            recordingState = MutableStateFlow(RecordingState.IDLE),
            remainingTime = 60000L,
            onStartRecording = {},
            onStopRecording = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AudioInputFieldPreviewRecording() {
    SagAIScaffold {
        val textState = remember { mutableStateOf("") }
        AudioInputField(
            textValue = textState.value,
            onTextChange = { textState.value = it },
            audioFile = null,
            onAudioRecorded = {},
            onSend = { _, _ -> },
            recordingState = MutableStateFlow(RecordingState.RECORDING),
            remainingTime = 35000L,
            onStartRecording = {},
            onStopRecording = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AudioInputFieldPreviewWithLeadingContent() {
    SagAIScaffold {
        val textState = remember { mutableStateOf("") }
        AudioInputField(
            textValue = textState.value,
            onTextChange = { textState.value = it },
            audioFile = null,
            onAudioRecorded = {},
            onSend = { _, _ -> },
            recordingState = MutableStateFlow(RecordingState.IDLE),
            remainingTime = 60000L,
            onStartRecording = {},
            onStopRecording = {},
            leadingContent = {
                // Simulating a character avatar
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✨",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AudioInputFieldPreviewWithBottomContent() {
    SagAIScaffold {
        val textState = remember { mutableStateOf("") }
        AudioInputField(
            textValue = textState.value,
            onTextChange = { textState.value = it },
            audioFile = null,
            onAudioRecorded = {},
            onSend = { _, _ -> },
            recordingState = MutableStateFlow(RecordingState.IDLE),
            remainingTime = 60000L,
            onStartRecording = {},
            onStopRecording = {},
            bottomContent = {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    horizontalArrangement =
                        androidx.compose.foundation.layout.Arrangement.spacedBy(
                            8.dp,
                        ),
                ) {
                    androidx.compose.material3.Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Sugestão 1")
                    }
                    androidx.compose.material3.Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Sugestão 2")
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AudioInputFieldPreviewFullFeatured() {
    SagAIScaffold {
        val textState = remember { mutableStateOf("") }
        val audioFileState = remember { mutableStateOf<File?>(null) }
        AudioInputField(
            textValue = textState.value,
            onTextChange = { textState.value = it },
            audioFile = audioFileState.value,
            onAudioRecorded = { audioFileState.value = it },
            onSend = { text, audio ->
                textState.value = ""
                audioFileState.value = null
            },
            recordingState = MutableStateFlow(RecordingState.IDLE),
            remainingTime = 60000L,
            onStartRecording = {},
            onStopRecording = {},
            leadingContent = {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🎭",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            },
            bottomContent = {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    horizontalArrangement =
                        androidx.compose.foundation.layout.Arrangement.spacedBy(
                            8.dp,
                        ),
                ) {
                    androidx.compose.material3.Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Ação 1")
                    }
                    androidx.compose.material3.Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Ação 2")
                    }
                }
            },
        )
    }
}
