package com.ilustris.sagai.core.ai.key

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R

/**
 * Why the user's API key stopped working.
 *
 * Deliberately shaped like [com.ilustris.sagai.core.ai.model.SafeGuard] — same string/icon/color
 * carriers — so the bottom sheet in `MainActivity` renders both from one layout.
 *
 * [QUOTA_DAILY] is the odd one out: it is not a broken key, it is a working key that ran out of
 * free tier for the day. It never asks for a replacement key, and it clears itself.
 */
@Keep
enum class ApiKeyFailure(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    @DrawableRes val iconRes: Int = R.drawable.ic_violation,
    val color: (ColorScheme) -> Color = { it.error },
    /** Whether the remedy is "give us another key" — false for the self-clearing quota case. */
    val requiresNewKey: Boolean = true,
) {
    INVALID(
        R.string.api_key_invalid_title,
        R.string.api_key_invalid_message,
        iconRes = R.drawable.ic_violation,
        color = { it.error },
    ),
    FORBIDDEN(
        R.string.api_key_forbidden_title,
        R.string.api_key_forbidden_message,
        iconRes = R.drawable.ic_warning,
        color = { it.error },
    ),
    QUOTA_DAILY(
        R.string.api_key_quota_daily_title,
        R.string.api_key_quota_daily_message,
        iconRes = R.drawable.ic_lightning_bolt,
        color = { it.secondary },
        requiresNewKey = false,
    ),
    ;

    companion object {
        fun findValue(value: String?): ApiKeyFailure? = entries.find { it.name == value }
    }
}
