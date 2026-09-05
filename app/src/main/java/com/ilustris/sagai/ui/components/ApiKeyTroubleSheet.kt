package com.ilustris.sagai.ui.components

import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.key.ApiKeyFailure
import com.ilustris.sagai.core.ai.key.ApiKeyState
import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.core.ai.key.QuotaStatusViewModel
import com.ilustris.sagai.ui.theme.SagAITheme
import java.util.Date

/**
 * Where a user sees the limits that actually apply to them.
 *
 * Taken from the API's own 429 body rather than a docs page: the published rate-limit tables moved
 * behind this panel, and the numbers are per account, so a static doc could not answer "why did it
 * stop for me".
 */
const val AI_STUDIO_RATE_LIMIT_URL = "https://ai.dev/rate-limit"

/** Google's own explanation of what an API key is and how to make one. */
const val AI_STUDIO_DOCS_URL = "https://ai.google.dev/gemini-api/docs/api-key"

/**
 * One global explanation for "the key can't generate right now", mirroring the guardrail sheet.
 *
 * Global rather than per-screen on purpose. Generation has far more entry points than the two with
 * a visible text field — book export, image generation, character creation, milestone beats — and
 * gating them individually would leave every path nobody remembered failing in silence. The
 * pre-flight check in `AIClient.ensureQuotaAvailable` refuses those calls; this is where the user
 * finds out why.
 *
 * Driven by state rather than `SideEffectService`, whose `SharedFlow` has no replay: a notice that
 * fired while the app was backgrounded would simply be lost, and both of these conditions outlive
 * the moment they happened.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyTroubleSheet(
    apiKeyState: ApiKeyState?,
    onOpenSettings: () -> Unit,
) {
    val quotaViewModel: QuotaStatusViewModel = hiltViewModel()
    val quotaStatus by quotaViewModel.status.collectAsStateWithLifecycle()

    val rejection = (apiKeyState as? ApiKeyState.Invalidated)?.reason
    val dailyBlock = quotaStatus as? QuotaStatus.DailyExhausted

    // A rejected key outranks a spent quota: replacing the key is the only move that helps, and
    // the quota block would resolve itself anyway.
    val failure =
        rejection ?: ApiKeyFailure.QUOTA_DAILY.takeIf { dailyBlock != null } ?: return

    // Dismissal is remembered per condition, so the sheet does not reappear on every recomposition
    // once the user has read it — but a *new* condition gets its own chance to be seen.
    var dismissedFor by rememberSaveable { mutableStateOf<String?>(null) }
    val conditionKey = "${failure.name}:${dailyBlock?.until ?: 0L}"
    if (dismissedFor == conditionKey) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val resetTime =
        remember(dailyBlock?.until) {
            dailyBlock?.until?.let { DateFormat.getTimeFormat(context).format(Date(it)) }
        }

    // Forced to the neutral palette, not the ambient saga genre: this is a technical safety
    // notice, and reading as plain/serious regardless of whatever genre the user was immersed
    // in is the point — same reasoning as the guardrail sheet in MainActivity.
    SagAITheme(null) {
        ModalBottomSheet(
            onDismissRequest = { dismissedFor = conditionKey },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(failure.titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.size(8.dp))

                Text(
                    text =
                        if (resetTime != null) {
                            stringResource(failure.messageRes, resetTime)
                        } else {
                            stringResource(failure.messageRes)
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.size(8.dp))

                TextButton(
                    onClick = {
                        // Quota sends you to your own numbers; a rejected key sends you to what a
                        // key is and how to make another. Same slot, different question being asked.
                        val url =
                            if (failure.requiresNewKey) {
                                AI_STUDIO_DOCS_URL
                            } else {
                                AI_STUDIO_RATE_LIMIT_URL
                            }
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                ) {
                    Text(
                        stringResource(
                            if (failure.requiresNewKey) {
                                R.string.api_key_docs
                            } else {
                                R.string.api_key_learn_more
                            },
                        ),
                    )
                }

                Spacer(Modifier.size(16.dp))

                Button(
                    onClick = {
                        dismissedFor = conditionKey
                        // A spent quota fixes itself; only a rejected key needs the user to act.
                        if (failure.requiresNewKey) onOpenSettings()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        stringResource(
                            if (failure.requiresNewKey) {
                                R.string.api_key_settings_replace
                            } else {
                                R.string.guardrail_dismiss
                            },
                        ),
                    )
                }
            }
        }
    }
}
