@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.newsaga.ui

import android.graphics.Matrix
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.audio.ui.AudioRecordingSheet
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.newsaga.ui.components.CharacterCreationView
import com.ilustris.sagai.features.newsaga.ui.components.GenreAvatar
import com.ilustris.sagai.features.newsaga.ui.components.GenreCard
import com.ilustris.sagai.features.newsaga.ui.components.NewSagaChat
import com.ilustris.sagai.features.newsaga.ui.presentation.CharacterCreationViewModel
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.launch

private enum class FlowPages(
    val icon: Int,
) {
    CREATE_SAGA(R.drawable.ic_spark),
    CREATE_CHARACTER(R.drawable.ic_feather),
    ;

    fun isPageComplete(sagaForm: SagaForm) =
        when (this) {
            CREATE_SAGA -> {
                sagaForm.saga.title.isNotEmpty() && sagaForm.saga.description.isNotEmpty()
            }

            CREATE_CHARACTER -> {
                sagaForm.character != null && sagaForm.character.name.isNotEmpty() &&
                    sagaForm.character.description.isNotEmpty()
            }
        }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewSagaView(
    navHostController: NavHostController,
    createSagaViewModel: CreateSagaViewModel = hiltViewModel(),
    characterCreationViewModel: CharacterCreationViewModel = hiltViewModel(),
) {
    val form by createSagaViewModel.form.collectAsStateWithLifecycle()
    val state by createSagaViewModel.state.collectAsStateWithLifecycle()
    val effect by createSagaViewModel.effect.collectAsStateWithLifecycle()
    val aiFormState by createSagaViewModel.formState.collectAsStateWithLifecycle()
    val isGenerating by createSagaViewModel.isGenerating.collectAsStateWithLifecycle()
    val messages by createSagaViewModel.chatMessages.collectAsStateWithLifecycle()
    var recordingAudio by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val callbackAction by createSagaViewModel.callbackAction.collectAsStateWithLifecycle()
    val isSaving by createSagaViewModel.isSaving.collectAsStateWithLifecycle()
    val loadingMessage by createSagaViewModel.loadingMessage.collectAsStateWithLifecycle()

    // Character creation states
    val characterPrompt by characterCreationViewModel.currentPrompt.collectAsStateWithLifecycle()
    val characterHint by characterCreationViewModel.currentHint.collectAsStateWithLifecycle()
    val characterSuggestions by characterCreationViewModel.suggestions.collectAsStateWithLifecycle()
    val characterGenerating by characterCreationViewModel.isGenerating.collectAsStateWithLifecycle()
    val characterCallback by characterCreationViewModel.callback.collectAsStateWithLifecycle()
    var inputField by remember { mutableStateOf("") }
    // Pager state
    val pagerState = rememberPagerState(initialPage = 0) { FlowPages.entries.size }
    val flow = remember { FlowPages.entries[pagerState.currentPage] }
    val coroutineScope = rememberCoroutineScope()
    val genre = form.saga.genre

    // Genre picker state

    // Animated background color
    val backgroundColor by animateColorAsState(
        targetValue = if (pagerState.currentPage == 0) genre.color else genre.colorPalette().last(),
        animationSpec = tween(600),
        label = "backgroundColor",
    )

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

            else -> {
                doNothing()
            }
        }
    }

    LaunchedEffect(Unit) {
        createSagaViewModel.startChat()
    }

    fun sendMessage() {
        val currentFlow = FlowPages.entries[pagerState.currentPage]
        when (currentFlow) {
            FlowPages.CREATE_SAGA -> {
                createSagaViewModel.sendChatMessage(inputField)
            }

            FlowPages.CREATE_CHARACTER -> {
                characterCreationViewModel.sendCharacterMessage(
                    inputField,
                    form,
                )
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = .5f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
    ) {
        TopBarContent(
            form = form,
            isLoading = state.isLoading,
            currentPage = flow,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            navigateBack = {
                navHostController.popBackStack()
            },
            onSelectGenre = { createSagaViewModel.updateGenre(it) },
            onTogglePage = { coroutineScope.launch { pagerState.animateScrollToPage(1 - pagerState.currentPage) } },
        )

        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) { page ->
            when (page) {
                0 -> {
                    // Saga creation page
                    NewSagaChat(
                        messages = messages,
                        isLoading = state.isLoading,
                        callback = callbackAction,
                        currentForm = form,
                        inputHint = aiFormState.hint ?: emptyString(),
                        inputSuggestions = aiFormState.suggestions,
                        onSendMessage = { createSagaViewModel.sendChatMessage(it) },
                        saveSaga = { createSagaViewModel.saveSaga() },
                        updateGenre = { createSagaViewModel.updateGenre(it) },
                    )
                }

                1 -> {
                    // Character creation page
                    LaunchedEffect(Unit) {
                        if (characterPrompt.isEmpty()) {
                            characterCreationViewModel.startCharacterCreation(form.saga)
                        }
                    }

                    CharacterCreationView(
                        genre = genre,
                        pagerState = pagerState,
                        currentPrompt = characterPrompt,
                        currentHint = characterHint,
                        suggestions = characterSuggestions,
                        isGenerating = characterGenerating,
                        callback = characterCallback,
                        onSendMessage = {
                            characterCreationViewModel.sendCharacterMessage(
                                it,
                                form,
                            )
                        },
                        onContinueToSaga = {
                            // Check if saga is complete
                            if (form.saga.title.isNotEmpty() && form.saga.description.isNotEmpty()) {
                                createSagaViewModel.saveSaga()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            }
                        },
                    )
                }
            }
        }

        BottomContent(
            genre = genre,
            inputField = inputField,
            isLoading = state.isLoading,
            hint = aiFormState.hint ?: emptyString(),
            onUpdateInput = { inputField = it },
            onSendMessage = { sendMessage() },
            onStartAudioRecording = { recordingAudio = true },
        )
    }

    StarryLoader(
        isLoading = isSaving,
        loadingMessage = loadingMessage,
        brushColors = form.saga.genre.shimmerColors(),
        textStyle =
            MaterialTheme.typography.labelMedium.copy(
                textAlign = TextAlign.Center,
                fontFamily = form.saga.genre.bodyFont(),
                color = MaterialTheme.colorScheme.onBackground,
            ),
    )

    if (recordingAudio) {
        AudioRecordingSheet(
            brush = form.saga.genre.colorPalette(),
            onDismiss = {
                recordingAudio = false
            },
        ) {
            inputField = it
            sendMessage()
            recordingAudio = false
        }
    }
}

@Composable
private fun TopBarContent(
    form: SagaForm,
    isLoading: Boolean,
    currentPage: FlowPages,
    modifier: Modifier = Modifier,
    onSelectGenre: (Genre) -> Unit,
    onTogglePage: () -> Unit,
    navigateBack: () -> Unit = {},
) {
    val genre = remember { form.saga.genre }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
                .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var showThemes by remember { mutableStateOf(false) }
        val tooltipState = rememberTooltipState(isPersistent = true)
        val tooltipPositionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                positioning = TooltipAnchorPosition.Below,
                spacingBetweenTooltipAndAnchor = 8.dp,
            )
        val coroutineScope = rememberCoroutineScope()

        Icon(
            painterResource(R.drawable.ic_back_left),
            null,
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable {
                        navigateBack()
                    },
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .weight(1f)
                    .animateContentSize(),
        ) {
            val size by animateDpAsState(if (isLoading) 50.dp else 40.dp)
            IconButton(
                onClick = {
                    onTogglePage()
                },
                modifier =
                    Modifier
                        .reactiveShimmer(isLoading)
                        .size(size)
                        .clip(CircleShape)
                        .padding(8.dp),
            ) {
                AnimatedContent(currentPage, transitionSpec = {
                    scaleIn() togetherWith scaleOut()
                }) {
                    Icon(
                        painter =
                            painterResource(
                                it.icon,
                            ),
                        contentDescription = "Toggle page",
                        tint = genre.iconColor,
                    )
                }
            }

            val showBadge = currentPage.isPageComplete(form)

            if (showBadge) {
                Box(
                    modifier =
                        Modifier
                            .size(4.dp)
                            .background(genre.iconColor, CircleShape),
                )
            }
        }

        TooltipBox(
            positionProvider = tooltipPositionProvider,
            state = tooltipState,
            tooltip = {
                Column(
                    Modifier
                        .padding(18.dp)
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            genre.color.gradientFade(),
                            genre.shape(),
                        ).background(
                            MaterialTheme.colorScheme.background,
                            genre.shape(),
                        ),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                stringResource(R.string.saga_genre),
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = genre.bodyFont(),
                                    ),
                                modifier = Modifier.weight(1f),
                            )

                            Text(
                                stringResource(R.string.see_more),
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = genre.bodyFont(),
                                        color = genre.color,
                                    ),
                                modifier =
                                    Modifier
                                        .clickable {
                                            coroutineScope.launch {
                                                tooltipState.dismiss()
                                            }
                                            showThemes = true
                                        },
                            )
                        }

                        LazyRow(
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(Genre.entries) { g ->
                                GenreAvatar(
                                    g,
                                    true,
                                    48.dp,
                                    genre == g,
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    onSelectGenre(g)
                                    coroutineScope.launch {
                                        tooltipState.dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            },
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        tooltipState.show()
                    }
                },
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .padding(8.dp),
            ) {
                AnimatedContent(genre) {
                    Icon(
                        painter = painterResource(it.background),
                        contentDescription = "Select genre",
                        tint = genre.iconColor,
                    )
                }
            }
        }

        if (showThemes) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch {
                        showThemes = false
                    }
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            stringResource(R.string.saga_genre),
                            style =
                                MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        )
                    }

                    items(Genre.entries) { genr ->
                        GenreCard(
                            genre = genr,
                            isSelected = genr == genre,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .aspectRatio(.5f),
                        ) {
                            onSelectGenre(genr)
                            showThemes = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomContent(
    genre: Genre,
    inputField: String,
    isLoading: Boolean,
    hint: String,
    onUpdateInput: (String) -> Unit,
    onSendMessage: () -> Unit,
    onStartAudioRecording: () -> Unit,
) {
    AnimatedVisibility(isLoading.not(), modifier = Modifier.fillMaxWidth()) {
        val shape = remember { genre.shape() }
        val glowRadius by animateDpAsState(
            targetValue = if (isLoading) 10.dp else 5.dp,
            label = "glowRadius",
            animationSpec = tween(500),
        )
        val infiniteTransition = rememberInfiniteTransition(label = "border_animation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "rotation",
        )

        Row(
            modifier =
                Modifier
                    .imePadding()
                    .animateContentSize()
                    .padding(16.dp)
                    .dropShadow(
                        shape = shape,
                        shadow =
                            Shadow(
                                radius = glowRadius,
                                spread = 4.dp,
                                color = genre.color,
                                offset = DpOffset.Zero,
                            ),
                    ).clip(shape)
                    .drawWithContent {
                        drawContent()
                        val outline = shape.createOutline(size, layoutDirection, this)
                        if (isLoading) {
                            val brush =
                                object : ShaderBrush() {
                                    override fun createShader(size: Size): Shader {
                                        val shader =
                                            (
                                                sweepGradient(
                                                    genre.colorPalette(),
                                                ) as ShaderBrush
                                            ).createShader(size)
                                        val matrix = Matrix()
                                        matrix.setRotate(
                                            rotation,
                                            size.width / 2,
                                            size.height / 2,
                                        )
                                        shader.setLocalMatrix(matrix)
                                        return shader
                                    }
                                }
                            drawOutline(
                                outline = outline,
                                brush = brush,
                                style = Stroke(width = 1.dp.toPx()),
                            )
                        } else {
                            drawOutline(
                                outline = outline,
                                brush = genre.iconColor.solidGradient(),
                                style = Stroke(width = 1.dp.toPx()),
                            )
                        }
                    }.background(MaterialTheme.colorScheme.surfaceContainer, shape)
                    .padding(8.dp)
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = inputField,
                onValueChange = {
                    onUpdateInput(it)
                },
                enabled = isLoading.not(),
                cursorBrush = genre.color.solidGradient(),
                textStyle =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier =
                    Modifier
                        .weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (inputField.isEmpty()) {
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.labelLarge,
                                color =
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f,
                                    ),
                                fontFamily = genre.bodyFont(),
                            )
                        }
                        innerTextField()
                    }
                },
            )

            IconButton(
                onClick = {
                    if (inputField.isEmpty()) {
                        onStartAudioRecording()
                    } else {
                        onSendMessage()
                    }
                },
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = genre.color,
                        contentColor = genre.iconColor,
                        disabledContentColor =
                            MaterialTheme.colorScheme.onBackground.copy(
                                alpha = .3f,
                            ),
                    ),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .size(32.dp),
            ) {
                val icon =
                    if (inputField.isEmpty()) R.drawable.ic_mic else R.drawable.ic_send
                AnimatedContent(icon) {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = "Send message",
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}
