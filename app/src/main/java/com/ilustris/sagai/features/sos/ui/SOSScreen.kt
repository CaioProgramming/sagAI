package com.ilustris.sagai.features.sos.ui

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
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

    fun restartApp() {
        if (isDatabaseError) {
            showConfirmDialog = true
        } else {
            onRestart()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_spark),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(50.dp),
            )

            Text(
                text = stringResource(R.string.unexpected_error),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (BuildConfig.DEBUG) {
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
            }

            Text(
                text = "Vamos verificar o que aconteceu e você poderá voltar as suas sagas em breve.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Button(onClick = {
                restartApp()
            }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.continue_text),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            icon = {
                AnimatedContent(state.isLoading) {
                    if (it) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_spark),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            },
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Deseja restaurar suas sagas antes de continuar?") },
            text = {
                Column {
                    Text(
                        "Continuing without a backup will permanently delete all your existing sagas and chronicles. This action cannot be undone.",
                    )

                    state.error?.let {
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Nao foi possivel restaurar suas sagas: $it",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreFullDatabase(state.dbBackups.first())
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Restore sagas")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onRestart()
                }) {
                    Text("Continue")
                }
            },
        )
    }
}
