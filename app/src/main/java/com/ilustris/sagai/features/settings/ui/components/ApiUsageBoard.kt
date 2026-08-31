package com.ilustris.sagai.features.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.key.ApiUsageTracker
import com.ilustris.sagai.core.database.model.ApiUsageDay
import com.ilustris.sagai.core.services.RemoteConfigService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

private const val DAILY_REQUEST_LIMITS_KEY = "model_daily_request_limits"

@HiltViewModel
class ApiUsageViewModel
    @Inject
    constructor(
        apiUsageTracker: ApiUsageTracker,
        private val remoteConfigService: RemoteConfigService,
    ) : ViewModel() {
        val usage: StateFlow<List<ApiUsageDay>> =
            apiUsageTracker.observeToday().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

        /**
         * Reference RPD per model, from Remote Config rather than the binary — Google moves these,
         * and a stale number in a shipped build would be worse than none at all.
         */
        private val _limits = MutableStateFlow<Map<String, Int>>(emptyMap())
        val limits: StateFlow<Map<String, Int>> = _limits.asStateFlow()

        init {
            viewModelScope.launch {
                _limits.value =
                    remoteConfigService
                        .getJsonMapStringAny(DAILY_REQUEST_LIMITS_KEY)
                        ?.mapNotNull { (model, value) ->
                            (value as? Number)?.let { model to it.toInt() }
                        }?.toMap()
                        .orEmpty()
            }
        }
    }

/**
 * What this app spent of the user's key today, per model.
 *
 * Requests, not tokens, are the headline: RPD is the only daily cap Google enforces — there is no
 * tokens-per-day — and it is by far the tightest, 500 a day on flash-lite against 250K tokens a
 * minute. Tokens are shown underneath because reasoning quietly dominates them, and the user is
 * paying for that too.
 */
@Composable
fun ApiUsageBoard(modifier: Modifier = Modifier) {
    val viewModel: ApiUsageViewModel = hiltViewModel()
    val usage by viewModel.usage.collectAsStateWithLifecycle()
    val limits by viewModel.limits.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (usage.isEmpty()) {
            Text(
                stringResource(R.string.api_usage_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
            )
        }

        usage.forEach { row ->
            val limit = limits[row.model]
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(12.dp),
                        ).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    row.model,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    if (limit != null) {
                        stringResource(R.string.api_usage_requests, row.requests, limit)
                    } else {
                        stringResource(R.string.api_usage_requests_unknown, row.requests)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (limit != null && limit > 0) {
                    LinearProgressIndicator(
                        progress = { (row.requests.toFloat() / limit).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    stringResource(
                        R.string.api_usage_tokens,
                        (row.promptTokens + row.candidatesTokens + row.thoughtsTokens).compact(),
                        row.thoughtsTokens.compact(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
                )
            }
        }
    }
}

/**
 * Time left until Google's counters roll over, phrased as a duration.
 *
 * Relative on purpose: the reset happens at midnight Pacific, which for most users lands at some
 * unremarkable hour of their own morning. "Resets at 04:00" invites the question of which day,
 * while a duration is always unambiguous and needs no timezone explanation.
 */
@Composable
fun rememberTimeUntilPacificMidnight(): String {
    val pacific = ZoneId.of("America/Los_Angeles")
    val now = ZonedDateTime.now(pacific)
    val reset = now.toLocalDate().plusDays(1).atStartOfDay(pacific)
    val duration = Duration.between(now, reset)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    return if (hours > 0) "${hours}h${minutes.toString().padStart(2, '0')}" else "${minutes}min"
}

private fun Long.compact(): String =
    when {
        this >= 1_000_000 -> "%.1fM".format(this / 1_000_000.0)
        this >= 1_000 -> "%.1fk".format(this / 1_000.0)
        else -> toString()
    }
