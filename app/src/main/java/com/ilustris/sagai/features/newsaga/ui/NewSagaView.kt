package com.ilustris.sagai.features.newsaga.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.newsaga.ui.components.NewSagaChat
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade

@Composable
fun NewSagaView(
    navHostController: NavHostController,
    createSagaViewModel: CreateSagaViewModel = hiltViewModel(),
) {
    val form by createSagaViewModel.form.collectAsStateWithLifecycle()
    val state by createSagaViewModel.state.collectAsStateWithLifecycle()
    val effect by createSagaViewModel.effect.collectAsStateWithLifecycle()
    val aiFormState by createSagaViewModel.formState.collectAsStateWithLifecycle()
    val isGenerating by createSagaViewModel.isGenerating.collectAsStateWithLifecycle()
    val messages by createSagaViewModel.chatMessages.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    val callbackAction by createSagaViewModel.callbackAction.collectAsStateWithLifecycle()
    val isSaving by createSagaViewModel.isSaving.collectAsStateWithLifecycle()
    val loadingMessage by createSagaViewModel.loadingMessage.collectAsStateWithLifecycle()

    BackHandler(enabled = isGenerating) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = stringResource(R.string.dialog_exit_title_new_saga)) },
            text = { Text(text = stringResource(R.string.dialog_exit_message_new_saga)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        navHostController.popBackStack()
                    },
                ) {
                    Text(stringResource(R.string.dialog_exit_confirm_button_new_saga))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false },
                ) {
                    Text(stringResource(R.string.dialog_exit_dismiss_button_new_saga))
                }
            },
        )
    }

    LaunchedEffect(effect) {
        when (effect) {
            is Effect.Navigate -> {
                navHostController.navigateToRoute(
                    (effect as Effect.Navigate).route,
                    arguments = (effect as Effect.Navigate).arguments,
                    popUpToRoute = Routes.NEW_SAGA,
                )
            }

            else -> doNothing()
        }
    }

    LaunchedEffect(Unit) {
        createSagaViewModel.startChat()
    }

    NewSagaChat(
        messages = messages,
        onSendMessage = { createSagaViewModel.sendChatMessage(it) },
        isLoading = state.isLoading,
        callback = callbackAction,
        isGenerating = isGenerating,
        onRetry = { createSagaViewModel.retry() },
        saveSaga = { createSagaViewModel.saveSaga() },
        currentForm = form,
        userInputHint = aiFormState.hint,
        inputSuggestions = aiFormState.suggestions,
        updateGenre = { createSagaViewModel.updateGenre(it) },
        resetSaga = { createSagaViewModel.resetSaga() },
    )

    StarryLoader(
        isLoading = isSaving,
        loadingMessage = loadingMessage,
        textStyle =
            MaterialTheme.typography.labelMedium.copy(
                textAlign = TextAlign.Center,
                fontFamily = form.saga.genre?.bodyFont(),
                brush =
                    form.saga.genre?.gradient(true)
                        ?: MaterialTheme.colorScheme.onBackground.gradientFade(),
            ),
    )
}
