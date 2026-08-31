package com.ilustris.sagai.features.onboarding.ui.apikey

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.data.model.OnboardingPage
import com.ilustris.sagai.features.player.ui.onboarding.UserNamePromptDialog
import kotlinx.coroutines.launch

private const val AI_STUDIO_URL = "https://aistudio.google.com/apikey"

/**
 * The paged introduction to bringing your own key, ending in the field that collects it.
 *
 * Built as a pager rather than one scrolling wall because the ask is unusual and a wall of
 * justification reads as an apology — the thing that most discourages someone at the door. Pages
 * let the reasoning arrive one idea at a time, and put the field at the end, after the "why".
 *
 * The explanatory copy comes from `onboarding_fallbacks` in Remote Config, like every other
 * onboarding in the app, so the pitch can be rewritten without shipping a build. Losing that config
 * costs the pitch and never the entrance: the key page is always present.
 *
 * @param dismissible false while the app is gated on a missing key — there is nowhere to dismiss
 *   to. True when opened from settings or the help button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyOnboarding(
    type: OnboardingType = OnboardingType.API_KEY_SETUP,
    dismissible: Boolean = false,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
) {
    val viewModel: ApiKeySetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val content by viewModel.pages.collectAsStateWithLifecycle()
    val isMigration by viewModel.isMigration.collectAsStateWithLifecycle()
    val needsName by viewModel.needsName.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.prepareForEntry()
        viewModel.loadPages()
        viewModel.keySaved.collect { onFinished() }
    }

    // Name first: a warm question with no stakes, before the one that sends the user to another
    // app entirely.
    if (needsName && !dismissible) {
        UserNamePromptDialog(
            onSaveName = viewModel::saveName,
            onDismiss = viewModel::skipName,
        )
    }

    val explanatoryPages = content.pages
    val pageCount = explanatoryPages.size + 1
    val pagerState = rememberPagerState { pageCount }
    val scope = rememberCoroutineScope()
    val isKeyPage = pagerState.currentPage == pageCount - 1

    ModalBottomSheet(
        onDismissRequest = { if (dismissible) onFinished() },
        sheetState =
            rememberModalBottomSheetState(
                // A sheet the user cannot dismiss must also refuse the drag, or it animates
                // halfway down and springs back — which reads as the app fighting them.
                confirmValueChange = { dismissible || it != SheetValue.Hidden },
            ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = if (dismissible) ({ BottomSheetDefaults.DragHandle() }) else null,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Dots above the content, not under the button: they say where you are, so they belong
            // with the thing that changes, not with the action.
            PagerDots(current = pagerState.currentPage, total = pageCount)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) { index ->
                if (index < explanatoryPages.size) {
                    ExplanatoryPage(explanatoryPages[index])
                } else {
                    ApiKeyPage(
                        isMigration = isMigration,
                        uiState = uiState,
                        onSubmit = viewModel::submit,
                        onEditing = viewModel::resetError,
                    )
                }
            }

            if (!isKeyPage) {
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(stringResource(R.string.api_key_onboarding_next))
                }
            }
        }
    }
}

/** One idea, centred, with room to breathe. The pitch is short on purpose. */
@Composable
private fun ExplanatoryPage(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            page.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PagerDots(
    current: Int,
    total: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Box(
                Modifier
                    .size(if (index == current) 8.dp else 6.dp)
                    .background(
                        if (index == current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = .25f)
                        },
                        CircleShape,
                    ),
            )
        }
    }
}

/** The last page: the only one that cannot be skipped, and the only one that asks for anything. */
@Composable
private fun ApiKeyPage(
    isMigration: Boolean,
    uiState: ApiKeySetupUiState,
    onSubmit: (String) -> Unit,
    onEditing: () -> Unit,
) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(
                if (isMigration) R.string.api_key_migration_title else R.string.api_key_setup_title,
            ),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            color = MaterialTheme.colorScheme.onBackground,
        )

        TextButton(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AI_STUDIO_URL)))
            },
        ) {
            Text(stringResource(R.string.api_key_setup_open_studio))
        }

        TextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                onEditing()
            },
            placeholder = {
                Text(
                    stringResource(R.string.api_key_setup_field_label),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
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
            onClick = { onSubmit(apiKey) },
            enabled = apiKey.isNotBlank() && uiState !is ApiKeySetupUiState.Validating,
            modifier = Modifier.fillMaxWidth(),
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

private fun ApiKeySetupUiState.errorMessage(): Int? =
    when (this) {
        ApiKeySetupUiState.Rejected -> R.string.api_key_setup_error_rejected
        ApiKeySetupUiState.Unreachable -> R.string.api_key_setup_error_network
        ApiKeySetupUiState.Empty -> R.string.api_key_setup_error_empty
        else -> null
    }
