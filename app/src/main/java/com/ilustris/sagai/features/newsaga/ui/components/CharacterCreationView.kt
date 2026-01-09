@file:OptIn(ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.manager.FormState.CharacterForm
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.shape

@Composable
fun CharacterCreationView(
    form: SagaDraft,
    characterState: CharacterForm,
    onContinueToSaga: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genre = form.genre
    val characterPrompt = characterState.message

    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SimpleTypewriterText(
                text = characterPrompt ?: emptyString(),
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
            )

            AnimatedVisibility(characterState.isReady) {
                Button(
                    onClick = onContinueToSaga,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = genre.color,
                            contentColor = genre.iconColor,
                        ),
                    shape = genre.shape(),
                ) {
                    Text(
                        stringResource(R.string.continue_to_saga),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = genre.bodyFont()),
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}
