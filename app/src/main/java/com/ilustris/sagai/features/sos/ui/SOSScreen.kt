package com.ilustris.sagai.features.sos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.sos.presentation.SOSViewModel

@Composable
fun SOSScreen(
    errorMessage: String,
    isDatabaseError: Boolean,
    exceptionClass: String,
    viewModel: SOSViewModel,
    onRestart: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isDatabaseError) {
            viewModel.loadBackups()
        }
    }

    LaunchedEffect(state.recoverySuccess) {
        if (state.recoverySuccess) {
            onRestart()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        ),
                    ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.unexpected_error),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = exceptionClass,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(4.dp),
                        ).padding(horizontal = 8.dp, vertical = 2.dp),
            )

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text =
                    if (isDatabaseError) {
                        "It seems your story chronicle has been corrupted. This can happen due to unexpected system failures."
                    } else {
                        "An unexpected error occurred in the tapestry of your sagas. We need to stabilize the system."
                    },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.size(32.dp))

            if (!state.isLoading) {
                if (isDatabaseError) {
                    // Saga Backups
                    if (state.sagaBackups.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.importSagaBackups() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(painterResource(R.drawable.ic_restore), contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Import ${state.sagaBackups.size} Sagas from Backup")
                        }

                        Spacer(modifier = Modifier.size(8.dp))
                    }

                    // Full DB Backup
                    if (state.dbBackups.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.restoreFullDatabase(state.dbBackups.first()) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text("Restore Full DB Auto-Backup")
                        }

                        Spacer(modifier = Modifier.size(8.dp))
                    }

                    OutlinedButton(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                    ) {
                        Text("Continue without Backup (Fresh Start)")
                    }
                } else {
                    Button(
                        onClick = onRestart,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(painterResource(R.drawable.ic_restore), contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Restart Application")
                    }
                }

                state.error?.let {
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = state.loadingMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (!state.isLoading) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Are you sure?") },
            text = {
                Text(
                    "Continuing without a backup will permanently delete all your existing sagas and chronicles. This action cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.freshStart()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
