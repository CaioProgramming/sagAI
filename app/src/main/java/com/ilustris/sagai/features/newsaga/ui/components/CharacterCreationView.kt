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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.shape

@Composable
fun CharacterCreationView(
    genre: Genre,
    pagerState: PagerState,
    currentPrompt: String,
    currentHint: String?,
    suggestions: List<String>,
    isGenerating: Boolean,
    callback: CallBackAction?,
    onSendMessage: (String) -> Unit,
    onContinueToSaga: () -> Unit,
    modifier: Modifier = Modifier,
) {
    genre.colorPalette().last()
    var inputField by remember { mutableStateOf(TextFieldValue("")) }

    // Clear input when page changes
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 1) {
            inputField = TextFieldValue("")
        }
    }

    {
        if (inputField.text.isNotEmpty()) {
            onSendMessage(inputField.text)
            inputField = TextFieldValue("")
        }
    }

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
                text = currentPrompt,
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
            )

            AnimatedVisibility(callback == CallBackAction.CHARACTER_READY) {
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
