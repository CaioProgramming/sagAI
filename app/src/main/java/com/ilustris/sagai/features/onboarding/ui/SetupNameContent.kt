package com.ilustris.sagai.features.onboarding.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.onboarding.data.model.OnboardingPage
import com.ilustris.sagai.features.player.ui.onboarding.PlayerNameField
import kotlinx.coroutines.flow.MutableStateFlow

/** The name question as a page of the setup flow, styled like the pages around it. */
@Composable
fun SetupNameContent(draft: MutableStateFlow<String>) {
    val name by draft.collectAsStateWithLifecycle()
    Column(Modifier.padding(horizontal = 16.dp)) {
        OnboardingStandardContent(
            OnboardingPage(
                title = stringResource(R.string.player_name_prompt_title),
                description = stringResource(R.string.player_name_prompt_subtitle),
            ),
        )
        PlayerNameField(value = name, onValueChange = { draft.value = it })
    }
}
