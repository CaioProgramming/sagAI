package com.ilustris.sagai.core.ai.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R

@Keep
enum class SafeGuard(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    @DrawableRes val iconRes: Int = R.drawable.ic_spark,
    val color: (ColorScheme) -> Color = { it.error },
) {
    OK(0, 0, color = { Color.Transparent }),
    BLOCKED(
        R.string.guardrail_blocked_title,
        R.string.guardrail_blocked_message,
        color = { it.error },
        iconRes = R.drawable.ic_violation,
    ),
    AGE_RESTRICTED(
        R.string.guardrail_age_title,
        R.string.guardrail_age_message,
        color = { it.secondary },
        iconRes = R.drawable.ic_age,
    ),
    EXPLICIT_CONTENT(
        R.string.guardrail_explicit_title,
        R.string.guardrail_explicit_message,
        color = { it.error },
        iconRes = R.drawable.ic_censor,
    ),
}
