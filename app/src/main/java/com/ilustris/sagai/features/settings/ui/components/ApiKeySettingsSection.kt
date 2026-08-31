package com.ilustris.sagai.features.settings.ui.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.key.ApiKeyShape
import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.core.ai.key.QuotaStatusService
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.network.GeminiApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ApiKeySettingsViewModel
    @Inject
    constructor(
        private val userApiKeyStore: UserApiKeyStore,
        private val geminiApiClient: GeminiApiClient,
        quotaStatusService: QuotaStatusService,
    ) : ViewModel() {
        /**
         * Only ever the first four and last four characters — enough for the user to tell which
         * key this is, never enough to use it. The full value has no reason to reach the UI layer.
         */
        private val _maskedKey = MutableStateFlow("")
        val maskedKey: StateFlow<String> = _maskedKey.asStateFlow()

        private val _testResult = MutableStateFlow<Boolean?>(null)
        val testResult: StateFlow<Boolean?> = _testResult.asStateFlow()

        val quotaStatus: StateFlow<QuotaStatus> =
            quotaStatusService.status.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = QuotaStatus.Clear,
            )

        init {
            viewModelScope.launch {
                _maskedKey.value = userApiKeyStore.getKeyNow()?.let(ApiKeyShape::mask).orEmpty()
            }
        }

        fun testKey() {
            viewModelScope.launch {
                _testResult.value = null
                val key = userApiKeyStore.getKeyNow()
                if (key == null) {
                    _testResult.value = false
                    return@launch
                }
                _testResult.value =
                    executeRequest(reportCrash = false) {
                        geminiApiClient.listModels(key)
                    }.isSuccess
            }
        }

        fun removeKey() {
            viewModelScope.launch {
                userApiKeyStore.clear()
                _maskedKey.value = ""
            }
        }

        private fun String.mask(): String = if (length <= 12) "••••••" else "${take(4)}••••••${takeLast(4)}"
    }

/**
 * The API key controls in Settings.
 *
 * Also where the daily quota block is spelled out. Settings is where people go when the app "stopped
 * working", and it is the one surface with room to name the actual fix — enabling billing on the
 * key's Google Cloud project — rather than just reporting the wall.
 */
@Composable
fun ApiKeySettingsSection(
    onReplaceKey: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ApiKeySettingsViewModel = hiltViewModel()
    val maskedKey by viewModel.maskedKey.collectAsStateWithLifecycle()
    val testResult by viewModel.testResult.collectAsStateWithLifecycle()
    val quotaStatus by viewModel.quotaStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showRemoveConfirm by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {


            IconButton(
                onClick = { showRemoveConfirm = true },
                colors =
                    IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                modifier = Modifier.alpha(.5f),
            ) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    stringResource(R.string.api_key_settings_remove),
                    modifier = Modifier.size(12.dp),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {
                viewModel.testKey()
            }, modifier = Modifier.size(24.dp)) {
                AnimatedContent(quotaStatus) {
                    val icon =
                        when (it) {
                            QuotaStatus.Clear -> R.drawable.baseline_refresh_24
                            is QuotaStatus.CoolingDown -> R.drawable.ic_lamp
                            is QuotaStatus.DailyExhausted -> R.drawable.round_close_24
                        }
                    Icon(
                        painterResource(icon),
                        stringResource(R.string.api_key_settings_test),
                    )
                }
            }
            Text(
                maskedKey,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = onReplaceKey,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_edit),
                    stringResource(R.string.api_key_settings_replace),
                )
            }
        }

        (quotaStatus as? QuotaStatus.DailyExhausted)?.let { block ->
            val resetTime =
                remember(block.until) {
                    DateFormat.getTimeFormat(context).format(Date(block.until))
                }
            Text(
                stringResource(
                    R.string.api_key_settings_quota_active,
                    block.model,
                    resetTime,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        testResult?.let { passed ->
            Text(
                stringResource(
                    if (passed) {
                        R.string.api_key_settings_test_ok
                    } else {
                        R.string.api_key_setup_error_rejected
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (passed) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
        }
    }

    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            text = { Text(stringResource(R.string.api_key_settings_remove_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveConfirm = false
                        viewModel.removeKey()
                    },
                ) {
                    Text(
                        stringResource(R.string.api_key_settings_remove),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = false }) {
                    Text(stringResource(R.string.guardrail_dismiss))
                }
            },
        )
    }
}
