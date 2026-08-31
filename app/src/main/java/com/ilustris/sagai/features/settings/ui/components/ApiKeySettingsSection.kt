package com.ilustris.sagai.features.settings.ui.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
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

        val quotaStatus: StateFlow<QuotaStatus> =
            quotaStatusService.status.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = QuotaStatus.Clear,
            )

        init {
            // Observed rather than read once: the key can be replaced or removed from the sheet
            // while this row is on screen, and a stale mask would name a key that is gone.
            viewModelScope.launch {
                userApiKeyStore.observeState().collect {
                    _maskedKey.value =
                        userApiKeyStore.getKeyNow()?.let(ApiKeyShape::mask).orEmpty()
                }
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
    val quotaStatus by viewModel.quotaStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier =
            modifier
                .clickable {
                    onReplaceKey()
                }.fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painterResource(R.drawable.ic_key),
                stringResource(R.string.api_key_settings_section),
                modifier = Modifier.size(12.dp).alpha(.5f),
            )
            Text(
                maskedKey,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
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
    }
}
