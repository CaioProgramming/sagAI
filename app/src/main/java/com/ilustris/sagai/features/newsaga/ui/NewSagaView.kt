package com.ilustris.sagai.features.newsaga.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.newsaga.data.model.isValid
import com.ilustris.sagai.features.newsaga.ui.components.NewSagaAIForm
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagaTitle

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

    var showExitDialog by remember { mutableStateOf(false) }

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
                        navHostController.popBackStack() // Or navigate to a specific route
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

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 50.dp).fillMaxWidth()) {
            Box(Modifier.size(32.dp))

            Box(Modifier.weight(1f)) {
                SagaTitle(
                    Modifier
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.background),
                )
            }

            val genre = form.saga.genre
            Button(
                onClick = {
                    createSagaViewModel.generateSaga()
                },
                enabled = !isGenerating && form.isValid(),
                colors =
                    ButtonDefaults.buttonColors().copy(
                        containerColor = genre?.color ?: MaterialTheme.colorScheme.primary,
                        contentColor = genre?.iconColor ?: MaterialTheme.colorScheme.onPrimary,
                    ),
                shape = RoundedCornerShape(50.dp),
            ) {
                Text(stringResource(R.string.save_saga))
            }
        }
        NewSagaAIForm(
            form,
            isLoading = isGenerating,
            aiState = aiFormState,
            savedSaga = state.saga,
            sendDescription = {
                if (it.isEmpty()) return@NewSagaAIForm
                createSagaViewModel.sendChatMessage(it)
            },
            selectGenre = {
                createSagaViewModel.updateGenre(it)
            },
        )
    }
}

