package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.CharacterStatus

/** The player correcting where a character stands, when the story model got it wrong. */
@Composable
fun CharacterStatusDialog(
    current: CharacterStatus?,
    currentNote: String?,
    onConfirm: (CharacterStatus, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var status by remember { mutableStateOf(current ?: CharacterStatus.ACTIVE) }
    var note by remember { mutableStateOf(currentNote.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.character_status_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CharacterStatus.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { status = option },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = status == option, onClick = { status = option })
                        Text(
                            stringResource(
                                when (option) {
                                    CharacterStatus.ACTIVE -> R.string.progression_status_active
                                    CharacterStatus.ABSENT -> R.string.progression_status_absent
                                    CharacterStatus.MISSING -> R.string.progression_status_missing
                                    CharacterStatus.DEAD -> R.string.progression_status_dead
                                },
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(80) },
                    label = { Text(stringResource(R.string.character_status_note_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(status, note.trim().takeIf { it.isNotEmpty() }) }) {
                Text(stringResource(R.string.character_status_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.progression_close)) } },
    )
}
