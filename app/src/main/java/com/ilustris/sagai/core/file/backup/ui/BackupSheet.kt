package com.ilustris.sagai.core.file.backup.ui

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.core.permissions.PermissionComponent
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.ui.components.StarryLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSheet(
    displayBackups: Boolean = false,
    onDismiss: () -> Unit = {},
    importUri: Uri? = null,
    onConfirmImport: (Uri) -> Unit = {},
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val backupEnabled by viewModel.backupEnabled.collectAsStateWithLifecycle(null)
    val backupLauncher =
        PermissionService.rememberBackupLauncher {
            viewModel.saveBackupFolder(it, displayBackups)
        }

    LaunchedEffect(importUri) {
        importUri?.let {
            viewModel.setImportConfirmation(it)
        }
    }

    AnimatedContent(state) {
        when (it) {
            BackupUiState.BackupEnabled, BackupUiState.Dimissed, BackupUiState.CheckingUp -> Box {}
            is BackupUiState.Empty -> {
                ModalBottomSheet(onDismissRequest = {
                    viewModel.dismiss()
                    onDismiss()
                }) {
                    Text(
                        it.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    )
                }
            }
            is BackupUiState.Loading -> StarryLoader(true, it.message, textStyle = MaterialTheme.typography.labelMedium)

            is BackupUiState.RequiresPermission ->
                PermissionComponent(
                    it.permission,
                    onConfirm = {
                        viewModel.dismiss()
                        backupLauncher.launch(null)
                    },
                    onDismiss = {
                        viewModel.dismiss()
                        onDismiss()
                    },
                )

            is BackupUiState.ImportConfirmation -> {
                ModalBottomSheet(onDismissRequest = {
                    viewModel.dismiss()
                    onDismiss()
                }) {
                    Text(
                        "Confirm import of ${it.uri.lastPathSegment}",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Button(onClick = {
                            onConfirmImport(it.uri)
                            viewModel.dismiss()
                            onDismiss()
                        }) {
                            Text("Import")
                        }
                        Button(onClick = {
                            viewModel.dismiss()
                            onDismiss()
                        }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }




}
