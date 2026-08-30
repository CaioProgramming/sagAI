package com.ilustris.sagai.features.onboarding.ui.apikey

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.player.ui.onboarding.UserNamePromptDialog
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.sagaBrush

private const val AI_STUDIO_URL = "https://aistudio.google.com/apikey"

/**
 * Asks the user for their own Gemini API key.
 *
 * Deliberately hand-written and static, unlike every other onboarding surface in the app:
 * [com.ilustris.sagai.features.onboarding.domain.OnboardingUseCaseImpl] generates its pages through
 * `GemmaClient`, and the screen whose entire job is collecting the key that makes generation
 * possible cannot itself require generation.
 *
 * Doubles as the migration screen for installs that predate BYOK — same flow, different framing, so
 * someone with fifty chapters of saga is told what happened rather than pitched at like a new user.
 */
@Composable
fun ApiKeySetupScreen(
    modifier: Modifier = Modifier,
    onKeySaved: () -> Unit = {},
) {
    val viewModel: ApiKeySetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isMigration by viewModel.isMigration.collectAsStateWithLifecycle()
    val needsName by viewModel.needsName.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var apiKey by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.prepareForEntry()
        viewModel.keySaved.collect { onKeySaved() }
    }

    // Name first, key second. The name is a warm question with no stakes; the key sends the user
    // off to Google AI Studio. Opening with the harder ask, before they have seen anything of the
    // app, is how you lose them at the door.
    if (needsName) {
        UserNamePromptDialog(
            onSaveName = viewModel::saveName,
            onDismiss = viewModel::skipName,
        )
    }

    Box(modifier.fillMaxSize()) {
        StarryTextPlaceholder(
            starCount = 100,
            modifier =
                Modifier
                    .fillMaxSize()
                    .gradientFill(sagaBrush()),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_spark),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .size(64.dp)
                        .levitate(),
            )

            Text(
                stringResource(
                    if (isMigration) {
                        R.string.api_key_migration_title
                    } else {
                        R.string.api_key_setup_title
                    },
                ),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                stringResource(
                    if (isMigration) {
                        R.string.api_key_migration_body
                    } else {
                        R.string.api_key_setup_body
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(
                    R.string.api_key_setup_step_one,
                    R.string.api_key_setup_step_two,
                    R.string.api_key_setup_step_three,
                ).forEachIndexed { index, step ->
                    Text(
                        "${index + 1}. ${stringResource(step)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
                    )
                }
            }

            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(AI_STUDIO_URL)),
                    )
                },
            ) {
                Text(stringResource(R.string.api_key_setup_open_studio))
            }

            TextField(
                value = apiKey,
                onValueChange = {
                    apiKey = it
                    viewModel.resetError()
                },
                placeholder = {
                    Text(
                        stringResource(R.string.api_key_setup_field_label),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                singleLine = true,
                enabled = uiState !is ApiKeySetupUiState.Validating,
                isError = uiState.errorMessage() != null,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                modifier = Modifier.fillMaxWidth(),
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor =
                            MaterialTheme.colorScheme.onBackground.copy(alpha = .3f),
                    ),
            )

            uiState.errorMessage()?.let { message ->
                Text(
                    stringResource(message),
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
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                if (uiState is ApiKeySetupUiState.Validating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.api_key_setup_validating))
                } else {
                    Text(stringResource(R.string.api_key_setup_save))
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun ApiKeySetupUiState.errorMessage(): Int? =
    when (this) {
        ApiKeySetupUiState.Rejected -> R.string.api_key_setup_error_rejected
        ApiKeySetupUiState.Unreachable -> R.string.api_key_setup_error_network
        ApiKeySetupUiState.Empty -> R.string.api_key_setup_error_empty
        else -> null
    }
