package com.ilustris.sagai.features.onboarding.ui.apikey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.core.ai.key.ApiKeyVerification
import com.ilustris.sagai.R

/**
 * Replacing or removing the key, in one sheet.
 *
 * Updating a key used to reopen the whole onboarding, which meant reading five pages of pitch to
 * reach a field the user had already used once. Someone changing a key knows why they are here; the
 * explanation still lives one tap away behind the question mark, for the times they do not.
 *
 * Removal sits here rather than in the settings row because it is the same decision seen from the
 * other side, and splitting them meant two places to go depending on which way you wanted to go.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyManageSheet(onDismiss: () -> Unit) {
    val viewModel: ApiKeySetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val currentMaskedKey by viewModel.currentMaskedKey.collectAsStateWithLifecycle()
    val verification by viewModel.verification.collectAsStateWithLifecycle()

    var apiKey by remember { mutableStateOf("") }
    var confirmingRemoval by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.prepareForEntry()
        viewModel.observeCurrentKey()
        viewModel.keySaved.collect { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.api_key_manage_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            ApiKeyField(
                value = apiKey,
                onValueChange = {
                    apiKey = it
                    viewModel.resetError()
                },
                isValidating = uiState is ApiKeySetupUiState.Validating,
                isError = uiState.errorMessage() != null,
                onPaste = { clipboard.getText()?.text?.trim()?.let { pasted -> apiKey = pasted } },
                placeholder =
                    currentMaskedKey.ifEmpty {
                        stringResource(R.string.api_key_setup_field_label)
                    },
            )

            // The status the settings row already showed, carried in rather than checked again,
            // so the two never disagree about the same key. Suppressed once the field has an error
            // of its own, which is about what was just typed and outranks the stored key's state.
            if (uiState.errorMessage() == null) {
                verification.statusMessage()?.let { message ->
                    Text(
                        stringResource(message),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color =
                            if (verification == ApiKeyVerification.Valid) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                    )
                }
            }

            uiState.errorMessage()?.let {
                Text(
                    stringResource(it),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = { viewModel.submit(apiKey) },
                enabled = apiKey.isNotBlank() && uiState !is ApiKeySetupUiState.Validating,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            ) {
                if (uiState is ApiKeySetupUiState.Validating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.api_key_setup_validating))
                } else {
                    Text(stringResource(R.string.api_key_setup_save))
                }
            }

            TextButton(onClick = { confirmingRemoval = true }) {
                Text(
                    stringResource(R.string.api_key_settings_remove),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (confirmingRemoval) {
        AlertDialog(
            onDismissRequest = { confirmingRemoval = false },
            text = { Text(stringResource(R.string.api_key_settings_remove_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingRemoval = false
                        viewModel.removeKey()
                        onDismiss()
                    },
                ) {
                    Text(
                        stringResource(R.string.api_key_settings_remove),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingRemoval = false }) {
                    Text(stringResource(R.string.guardrail_dismiss))
                }
            },
        )
    }
}

private fun ApiKeyVerification.statusMessage(): Int? =
    when (this) {
        ApiKeyVerification.Checking -> R.string.api_key_status_checking
        ApiKeyVerification.Valid -> R.string.api_key_status_valid
        ApiKeyVerification.Invalid -> R.string.api_key_status_invalid
        ApiKeyVerification.Unreachable -> R.string.api_key_status_unreachable
        ApiKeyVerification.Unknown -> null
    }
