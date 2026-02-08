package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.ui.theme.bodyFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageOptionsSheet(
    message: Message,
    genre: Genre,
    isLastMessage: Boolean,
    isSafeToEdit: Boolean, // !isLoading && !sagaEnded and !isGenerating
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onSelect: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isLastMessage && isSafeToEdit) {
                OptionItem(
                    icon = painterResource(R.drawable.ic_edit),
                    label = stringResource(R.string.edit_message),
                    genre = genre,
                    onClick = onEdit,
                )
            }

            OptionItem(
                icon = painterResource(R.drawable.ic_copy),
                label = stringResource(R.string.copy_message),
                genre = genre,
                onClick = onCopy,
            )

            OptionItem(
                icon = painterResource(R.drawable.ic_check_circle),
                label = stringResource(R.string.select_message),
                genre = genre,
                onClick = onSelect,
            )

            if (isSafeToEdit) { // We can delete anytime unless loading
                OptionItem(
                    icon = painterResource(R.drawable.ic_delete),
                    label = stringResource(R.string.delete_message),
                    genre = genre,
                    color = MaterialTheme.colorScheme.error,
                    onClick = onDelete,
                    showDivider = false,
                )
            }
        }
    }
}

@Composable
private fun OptionItem(
    icon: Painter,
    label: String,
    genre: Genre,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showDivider: Boolean = true,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = label,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontFamily = genre.bodyFont(),
                    ),
                modifier = Modifier.weight(1f),
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),
            )
        }
    }
}
