package com.ilustris.sagai.features.settings.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ilustris.sagai.R
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingHost
import com.ilustris.sagai.features.onboarding.ui.OnboardingPresentation
import com.ilustris.sagai.features.settings.ui.components.ApiKeySettingsSection
import com.ilustris.sagai.features.settings.ui.components.ApiUsageBoard

/**
 * Everything about the user's key on one screen, out of Settings.
 *
 * Split off rather than left inline because this is the surface that answers "what is this app
 * doing with my quota" — usage, limits, the key itself, and the explanation of why any of it is
 * needed. Buried among tutorials and backup toggles it read as one more preference row.
 */
@Composable
fun ApiSettingsView(
    onBack: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    var showApiKeySetup by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.ic_back_left),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Text(
                        stringResource(R.string.api_settings_title),
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                    // The explanation lives behind a question mark rather than on the screen: the
                    // people who need it are a minority, and spelling it out here would bury the
                    // numbers everyone else came for.
                    IconButton(onClick = { showOnboarding = true }) {
                        Icon(
                            painterResource(R.drawable.ic_faq),
                            contentDescription = stringResource(R.string.api_settings_help),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            item {
                ApiUsageBoard(
                    modifier =
                        Modifier.background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(15.dp),
                        ),
                )
            }

            item {
                ApiKeySettingsSection(
                    onReplaceKey = { showApiKeySetup = true },
                    modifier =
                        Modifier.background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(15.dp),
                        ),
                )
            }
        }
    }

    if (showApiKeySetup) {
        Dialog(
            onDismissRequest = { showApiKeySetup = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            OnboardingHost(
                type = OnboardingType.API_KEY_SETUP,
                presentation = OnboardingPresentation.Sheet,
                force = true,
                onDismiss = { showApiKeySetup = false },
            )
        }
    }

    if (showOnboarding) {
        Dialog(
            onDismissRequest = { showOnboarding = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            OnboardingHost(
                type = OnboardingType.API_KEY_SETUP,
                presentation = OnboardingPresentation.Sheet,
                force = true,
                onDismiss = { showOnboarding = false },
            )
        }
    }
}
