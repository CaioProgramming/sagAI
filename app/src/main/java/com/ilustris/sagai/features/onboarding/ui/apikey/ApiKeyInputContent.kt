package com.ilustris.sagai.features.onboarding.ui.apikey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.key.ApiKeyShape
import com.ilustris.sagai.features.onboarding.data.model.OnboardingPage
import com.ilustris.sagai.features.player.ui.onboarding.UserNamePromptDialog

/** Where a Gemini key is created. Used by the onboarding page's secondary button. */
const val AI_STUDIO_URL = "https://aistudio.google.com/apikey"

/**
 * The key field, shaped as onboarding page content.
 *
 * Lives inside the normal onboarding pipeline rather than in a screen of its own so this reads like
 * every other introduction in the app — same backgrounds, same pager, same rhythm. It was briefly
 * a bespoke pager, which meant a second implementation of the same experience that would have
 * drifted from the original on the first change to either.
 *
 * It owns its submit button instead of using the page's, because only the field knows whether what
 * was typed is worth sending, and the answer arrives asynchronously.
 */
@Composable
fun ApiKeyInputContent(page: OnboardingPage) {
    val viewModel: ApiKeySetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var apiKey by remember { mutableStateOf("") }
    var pastedFromClipboard by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.prepareForEntry()
        // The flow sends the user off to create a key and come back, so whatever is on the
        // clipboard when they arrive is almost certainly it. Only while the field is untouched, and
        // only when the text has the shape of a key, so nothing typed is ever overwritten.
        val clipped = clipboard.getText()?.text?.trim()
        if (apiKey.isEmpty() && ApiKeyShape.looksLikeKey(clipped)) {
            apiKey = clipped.orEmpty()
            pastedFromClipboard = true
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            page.title,
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            color = MaterialTheme.colorScheme.onBackground,
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
            placeholder = stringResource(R.string.api_key_setup_field_label),
        )

        if (pastedFromClipboard && uiState.errorMessage() == null) {
            Text(
                stringResource(R.string.api_key_pasted_hint),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        uiState.errorMessage()?.let {
            Text(
                stringResource(it),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Text(
            stringResource(R.string.api_key_setup_storage_note),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
        )

        Button(
            onClick = { viewModel.submit(apiKey) },
            enabled = apiKey.isNotBlank() && uiState !is ApiKeySetupUiState.Validating,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            shape = MaterialTheme.shapes.large,
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
    }
}

internal fun ApiKeySetupUiState.errorMessage(): Int? =
    when (this) {
        ApiKeySetupUiState.Rejected -> R.string.api_key_setup_error_rejected
        ApiKeySetupUiState.Unreachable -> R.string.api_key_setup_error_network
        ApiKeySetupUiState.Empty -> R.string.api_key_setup_error_empty
        else -> null
    }

/**
 * The name question, hosted outside the pager.
 *
 * Kept out of the page content deliberately, though not for the reason first suspected: the
 * `Cannot disable reuse` crash came from [SparkBackground], not from here. It stays hoisted anyway,
 * because a modal opened from inside a page the pager may be prefetching is a sub-composition built
 * under machinery that does not expect one — a hazard worth not having, even unproven.
 *
 * Shares the ViewModel with the field through the activity's store, so answering here is what makes
 * [ApiKeySetupViewModel.needsName] fall away for both.
 */
@Composable
fun ApiKeyNamePrompt() {
    val viewModel: ApiKeySetupViewModel = hiltViewModel()
    val needsName by viewModel.needsName.collectAsStateWithLifecycle()

    if (needsName) {
        UserNamePromptDialog(
            onSaveName = viewModel::saveName,
            onDismiss = viewModel::skipName,
        )
    }
}

/**
 * The key field itself, shared by the onboarding page and the settings sheet.
 *
 * Extracted so the two cannot drift: they show the same secret, mask it the same way, and offer the
 * same paste affordance, which would stop being true the first time one of them was tweaked alone.
 */
@Composable
fun ApiKeyField(
    value: String,
    onValueChange: (String) -> Unit,
    isValidating: Boolean,
    isError: Boolean,
    onPaste: () -> Unit,
    placeholder: String,
) {
    // A plain field in a filled box rather than the app's centred, auto-sizing style. A key
    // is one long unbroken token: shrinking the text to make it fit turns it illegible at
    // exactly the moment the user wants to check what landed. Monospaced and scrolling
    // sideways instead, with the container doing the work of saying "paste here".
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    RoundedCornerShape(16.dp),
                ).padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Placeholder art: neither a key nor a paste glyph exists in the project yet.
        Icon(
            painterResource(R.drawable.ic_key),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp),
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            singleLine = true,
            visualTransformation = ApiKeyMaskTransformation,
            enabled = !isValidating,
            isError = isError,
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            modifier = Modifier.weight(1f),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
        )

        IconButton(
            onClick = onPaste,
        ) {
            Icon(
                painterResource(R.drawable.ic_paste),
                contentDescription = stringResource(R.string.api_key_paste),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
