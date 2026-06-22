package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.description
import com.ilustris.sagai.features.saga.chat.data.model.icon
import com.ilustris.sagai.features.saga.chat.data.model.title

private data class SpeechModeOption(
    val senderType: SenderType,
    val expressiveTag: ExpressiveTag?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechModeSheet(
    activeTag: ExpressiveTag?,
    accentColor: Color,
    canInsertTag: Boolean,
    onSelectSpeak: () -> Unit,
    onSelectTag: (ExpressiveTag) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val options =
        listOf(
            SpeechModeOption(SenderType.CHARACTER, null),
            SpeechModeOption(SenderType.NARRATOR, ExpressiveTag.NARRATOR),
            SpeechModeOption(SenderType.ACTION, ExpressiveTag.ACTION),
            SpeechModeOption(SenderType.THOUGHT, ExpressiveTag.THINK),
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.chat_input_speech_mode_title),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    ),
                modifier = Modifier.padding(vertical = 8.dp),
            )

            options.forEach { option ->
                val isActive =
                    when {
                        option.expressiveTag != null -> activeTag == option.expressiveTag
                        else -> activeTag == null
                    }
                val enabled = option.expressiveTag == null || canInsertTag

                SpeechModeItem(
                    senderType = option.senderType,
                    isActive = isActive,
                    enabled = enabled,
                    accentColor = accentColor,
                    onClick = {
                        if (option.expressiveTag != null) {
                            onSelectTag(option.expressiveTag)
                        } else {
                            onSelectSpeak()
                        }
                        onDismiss()
                    },
                )
            }
        }
    }
}

@Composable
private fun SpeechModeItem(
    senderType: SenderType,
    isActive: Boolean,
    enabled: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val background =
        if (isActive) {
            accentColor.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(background)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        senderType.icon()?.let {
            Icon(
                painterResource(it),
                contentDescription = null,
                tint = if (enabled) accentColor else accentColor.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                senderType.title(),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color =
                            if (enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            },
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    ),
            )
            Text(
                senderType.description(),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.35f),
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    ),
            )
        }
    }
}
