package com.ilustris.sagai.core.file.backup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.database.backup.BackupMetadata
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.formatFileSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseRestoreSheet(
    isVisible: Boolean,
    backups: List<BackupMetadata>,
    onRequestRestore: (BackupMetadata) -> Unit,
    onDismiss: () -> Unit,
) {
    if (isVisible) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            LazyColumn(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        stringResource(R.string.restore_database_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    Text(
                        stringResource(R.string.restore_database_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                items(backups) { backup ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onRequestRestore(backup) }
                                .padding(16.dp),
                    ) {
                        Text(
                            "Version ${backup.version} - ${backup.appVersion}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            backup.timestamp.formatDate(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            backup.fileSize.formatFileSize(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            backups.firstOrNull()?.let { onRequestRestore(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_restore),
                            null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(stringResource(R.string.restore_database_button))
                    }
                }
            }
        }
    }
}
