package com.ilustris.sagai.features.newsaga.ui

import androidx.activity.compose.BackHandler // Import BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.AlertDialog // Import AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Import TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf // Import mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue // Import setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Removed Dialog and DialogProperties as AlertDialog is used
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.ui.components.NewSagaAIForm
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPages
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPagesView
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.features.newsaga.ui.components.NewSagaChat
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun NewSagaView(
    navHostController: NavHostController,
    createSagaViewModel: CreateSagaViewModel = hiltViewModel(),
) {
    val form by createSagaViewModel.form.collectAsStateWithLifecycle()
    val state by createSagaViewModel.state.collectAsStateWithLifecycle()
    val effect by createSagaViewModel.effect.collectAsStateWithLifecycle()
    val messages by createSagaViewModel.chatMessages.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(0) { NewSagaPages.entries.size }
    val coroutineScope = rememberCoroutineScope()
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

    fun animateToPage(
        page: Int,
        delayTime: Duration = 0.seconds,
    ) {
        coroutineScope.launch {
            delay(delayTime)
            pagerState.animateScrollToPage(page)
        }
    }

    Box {
        NewSagaAIForm(
            form,
            isLoading = isGenerating,
            aiState = aiFormState,
            savedSaga = state.saga,
            sendDescription = {
                if (it.isEmpty()) return@NewSagaAIForm
                createSagaViewModel.sendChatMessage(it)
            },
            onSave = {
                createSagaViewModel.generateSaga()
            }
        )
        /*NewSagaFlow(
            pagerState = pagerState,
            form = form,
            updateContent = { page, data ->

                when (page) {
                    TITLE -> {
                        (data as? String)?.let {
                            createSagaViewModel.updateTitle(it)
                        } ?: run {
                            createSagaViewModel.updateTitle(form.title)
                        }
                        animateToPage(pagerState.currentPage + 1)
                    }

                    GENRE -> {
                        (data as? Genre)?.let {
                            createSagaViewModel.updateGenre(it)
                        } ?: run {
                            createSagaViewModel.updateGenre(form.genre)
                        }
                        animateToPage(pagerState.currentPage + 1)
                    }

                    DESCRIPTION -> {
                        (data as? String)?.let {
                            createSagaViewModel.updateDescription(it)
                        } ?: run {
                            createSagaViewModel.updateDescription(form.description)
                        }
                        animateToPage(pagerState.currentPage + 1)
                    }

                    CHARACTER -> {
                        (data as? Character)?.let {
                            createSagaViewModel.updateCharacterDescription(it)
                        } ?: run {
                            createSagaViewModel.updateCharacterDescription(form.character)
                        }
                        animateToPage(pagerState.currentPage + 1)
                    }
                }
            },
            changePage = {
                animateToPage(it)
            },
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .blur(blurRadius.value, edgeTreatment = BlurredEdgeTreatment.Unbounded),
        )
        NewSagaChat(
            currentForm = form,
            messages = messages,
            userInputHint = aiFormState.hint,
            inputSuggestions = aiFormState.suggestions,
            isLoading = state.isLoading, // You might want to use isGenerating here too for the chat UI
            isGenerating = isGenerating,
            sagaToReveal = state.saga,
            onSendMessage = {
                createSagaViewModel.sendChatMessage(it)
            },
            onRetry = {
                createSagaViewModel.retry()
            },
            saveSaga = {
                createSagaViewModel.generateSaga()
            },
        )*/
    }
}

@Composable
fun NewSagaFlow(
    pagerState: PagerState,
    form: SagaForm,
    modifier: Modifier = Modifier,
    updateContent: (NewSagaPages, Any?) -> Unit = { _, _ -> },
    changePage: (Int) -> Unit = { _ -> },
) {
    val currentPage = NewSagaPages.entries[pagerState.currentPage]
    var data by remember {
        mutableStateOf<Any?>(null)
    }
    Column(
        modifier = modifier.animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        currentPage.title?.let {
            Text(
                stringResource(it),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                modifier = Modifier.padding(16.dp),
            )
        }
        currentPage.subtitle?.let {
            Text(
                stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }

        NewSagaPagesView(
            pagerState,
            form,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) { page, newValue ->
            data = newValue
        }

        val brush = form.saga.genre?.gradient() ?: Brush.verticalGradient(holographicGradient)

        /*val pageEnabled =
            when (currentPage) {
                GENRE -> true
                TITLE -> (data as? String)?.isNotEmpty() == true || form.title.isNotEmpty()
                DESCRIPTION -> (data as? String)?.isNotEmpty() == true || form.description.isNotEmpty()
                CHARACTER -> {
                    (data as? Character)
                        ?.let { CharacterFormRules.validateCharacter(it) } == true &&
                        (form.description.isNotEmpty()) &&
                        form.title.isNotEmpty()
                }
            }*/

        Row(
            modifier =
                Modifier
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NewSagaPages.entries.forEachIndexed { page, index ->
                val isEnabled = page <= pagerState.currentPage
                val size =
                    animateDpAsState(
                        targetValue = if (isEnabled) 32.dp else 12.dp,
                        label = "indicatorSize",
                    )
                val dividerAlpha by animateFloatAsState(
                    if (isEnabled) 1f else .1f,
                    label = "dividerAlpha",
                )

                val indicatorWeight by animateFloatAsState(
                    if (isEnabled) 1f else .3f,
                    label = "indicatorWeight",
                    animationSpec = tween(500, easing = EaseIn),
                )

                val indicatorBrush =
                    if (isEnabled) {
                        brush
                    } else {
                        MaterialTheme.colorScheme.onBackground
                            .copy(alpha = .3f)
                            .solidGradient()
                    }
                Icon(
                    painterResource(R.drawable.ic_spark),
                    null,
                    tint =
                        MaterialTheme.colorScheme.onBackground.copy(
                            alpha = dividerAlpha,
                        ),
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .size(size.value)
                            .clickable(enabled = isEnabled) {
                                changePage(page)
                            }.gradientFill(
                                indicatorBrush,
                            ),
                )

                Box(
                    modifier =
                        Modifier
                            .background(
                                indicatorBrush,
                                RoundedCornerShape(25.dp),
                            ).height(5.dp)
                            .weight(indicatorWeight),
                )
            }
        }

        AnimatedVisibility(
            true,
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    updateContent(currentPage, data)
                },
                shape = RoundedCornerShape(15.dp),
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .border(2.dp, brush, RoundedCornerShape(15.dp)),
                colors =
                    ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Black,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.next),
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                        ),
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .gradientFill(brush),
                )

                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Next",
                    modifier =
                        Modifier
                            .gradientFill(brush)
                            .size(24.dp),
                    tint = Color.White,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewSagaFormPreview() {
    SagAIScaffold {
        // var fo // This line was incomplete, removing for now
    }
}
