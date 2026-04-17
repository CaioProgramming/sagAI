package com.ilustris.sagai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.theme.gradientFill

data class CosmicInputField(
    val id: String,
    val label: String,
    val value: String,
    val isMultiline: Boolean = false,
    val hint: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmicEditorSheet(
    title: String,
    fields: List<CosmicInputField>,
    genre: Genre? = null,
    onSave: (Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fieldStates = remember(fields) { fields.associate { it.id to mutableStateOf(it.value) } }
    val themePalette =
        genre?.colorPalette() ?: listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = themePalette.first().copy(alpha = 0.5f),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier
                            .weight(1f)
                            .gradientFill(Brush.horizontalGradient(themePalette)),
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.round_close_24),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Fields
            fields.forEach { field ->
                val state = fieldStates[field.id]!!

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = field.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = themePalette.first().copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold,
                    )

                    OutlinedTextField(
                        value = state.value,
                        onValueChange = { state.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                field.hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            )
                        },
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themePalette.first(),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                focusedLabelColor = themePalette.first(),
                            ),
                        minLines = if (field.isMultiline) 3 else 1,
                        maxLines = if (field.isMultiline) 8 else 1,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        keyboardOptions =
                            KeyboardOptions(
                                imeAction = if (field.isMultiline) ImeAction.Default else ImeAction.Next,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action
            Button(
                onClick = {
                    onSave(fieldStates.mapValues { it.value.value })
                    onDismiss()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = themePalette.first(),
                        contentColor = Color.White,
                    ),
                shape = MaterialTheme.shapes.large,
            ) {
                Text(stringResource(R.string.save_changes), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
