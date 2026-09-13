package com.ilustris.sagai.features.onboarding.ui.apikey

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.sagaBrush

/**
 * What sits behind the API key setup sheet on [com.ilustris.sagai.MainActivity]'s
 * `AppGate.NeedsApiKey` gate, so the sheet closing for any reason — a background error in
 * [com.ilustris.sagai.features.onboarding.ui.OnboardingViewModel] force-dismisses it regardless of
 * `dismissible`, and a stray system back-gesture might too — never leaves a bare screen with no way
 * back in. The button re-opens the same sheet rather than routing anywhere else: there is nowhere
 * else to route to without a key.
 */
@Composable
fun ApiKeyRequiredScreen(
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        StarryTextPlaceholder(
            starCount = 100,
            modifier =
                Modifier
                    .fillMaxSize()
                    .gradientFill(sagaBrush()),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.api_key_required_title),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.api_key_required_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onConfigureClick) {
                Text(stringResource(R.string.api_key_required_action))
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun ApiKeyRequiredScreenPreview() {
    SagAITheme {
        ApiKeyRequiredScreen(onConfigureClick = {})
    }
}
