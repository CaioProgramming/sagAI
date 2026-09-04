package com.ilustris.sagai.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.sagaShape
import kotlinx.coroutines.delay
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Replaces a generation input while the user's key is out of daily quota.
 *
 * Shown instead of a disabled field because a spent daily allowance is not a momentary hiccup —
 * accepting a message that cannot possibly be answered until midnight Pacific is worse than saying
 * so plainly, and worst of all in saga creation, where the user has already invested choices before
 * anything fails.
 */
@Composable
fun QuotaLimitNotice(
    until: Long,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resetTime =
        remember(until) {
            DateFormat.getTimeFormat(context).format(Date(until))
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .35f),
                    sagaShape(),
                ).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_lightning_bolt),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(R.string.quota_daily_notice_title),
                style =
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(R.string.quota_daily_notice_message, resetTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f),
            )
        }
    }
}

/**
 * Live countdown for a per-minute throttle, rendered into the reasoning stream.
 *
 * The request behind this is still in flight — the existing backoff owns it. This only replaces a
 * mute spinner with a number, so the wait reads as the API pacing us rather than the app hanging.
 */
@Composable
fun quotaCooldownLabel(until: Long): String {
    var remainingMs by remember(until) {
        mutableLongStateOf((until - System.currentTimeMillis()).coerceAtLeast(0L))
    }

    LaunchedEffect(until) {
        while (remainingMs > 0) {
            delay(1_000)
            remainingMs = (until - System.currentTimeMillis()).coerceAtLeast(0L)
        }
    }

    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingMs)
    val formatted = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    return stringResource(R.string.quota_cooldown_reasoning, formatted)
}
