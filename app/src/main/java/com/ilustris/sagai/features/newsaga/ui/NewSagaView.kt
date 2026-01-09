@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.newsaga.ui

import android.graphics.Matrix
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.newsaga.ui.components.CharacterCreationView
import com.ilustris.sagai.features.newsaga.ui.components.GenreAvatar
import com.ilustris.sagai.features.newsaga.ui.components.GenreCard
import com.ilustris.sagai.features.newsaga.ui.components.NewSagaChat
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaViewModel
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private enum class FlowPages(
    val icon: Int,
) {
    CREATE_SAGA(R.drawable.ic_spark),
    CREATE_CHARACTER(R.drawable.ic_feather),
    ;

    fun isPageComplete(
        sagaReady: Boolean,
        characterReady: Boolean,
    ) = when (this) {
        CREATE_SAGA -> sagaReady
        CREATE_CHARACTER -> characterReady
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewSagaView(
    navHostController: NavHostController,
    viewModel: NewSagaViewModel = hiltViewModel(),
) {
    val sagaFormState by viewModel.sagaFormState.collectAsStateWithLifecycle()
    val characterState by viewModel.characterState.collectAsStateWithLifecycle()

    sagaFormState?.draft

    val isReadyToSave by viewModel.isReadyToSave.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val savingError by viewModel.savingError.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle()
    var recordingAudio by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    var inputField by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(initialPage = 0) { FlowPages.entries.size }
    val flow = FlowPages.entries[pagerState.currentPage]
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val peekHeight = 100.dp

    val startingUp =
        when (flow) {
            FlowPages.CREATE_SAGA -> sagaFormState?.messages?.isEmpty() == true
            FlowPages.CREATE_CHARACTER -> characterState?.message == null
        }

    BackHandler(enabled = isSaving) {
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
        viewModel.startSagaChat()
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1) {
            viewModel.startCharacterCreation()
        }
    }

    fun sendMessage() {
        val currentFlow = FlowPages.entries[pagerState.currentPage]
        when (currentFlow) {
            FlowPages.CREATE_SAGA -> {
                viewModel.sendSagaMessage(inputField)
            }

            FlowPages.CREATE_CHARACTER -> {
                viewModel.sendCharacterMessage(inputField)
            }
        }
        inputField = ""
    }

    SharedTransitionLayout {
        AnimatedContent(startingUp, transitionSpec = {
            fadeIn(tween(700)) togetherWith fadeOut()
        }) {
            if (it) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painterResource(R.drawable.ic_spark),
                        null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
                        modifier =
                            Modifier
                                .align(
                                    Alignment.Center,
                                ).sharedElement(
                                    rememberSharedContentState("spark_icon"),
                                    this@AnimatedContent,
                                ).size(64.dp)
                                .reactiveShimmer(
                                    true,
                                    holographicGradient,
                                    duration = 2.seconds,
                                    repeatMode = RepeatMode.Restart,
                                    targetValue = 200f,
                                ),
                    )
                }
            } else {
                val draft = sagaFormState?.draft
                if (draft == null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .size(50.dp)
                                    .reactiveShimmer(
                                        true,
                                        holographicGradient,
                                    ),
                        )
                    }
                } else {
                    val genre = draft.genre
                    val suggestions =
                        (
                            if (flow == FlowPages.CREATE_SAGA) {
                                sagaFormState?.suggestions
                            } else {
                                characterState?.suggestions
                            }
                        ) ?: emptyList()

                    val isLoading =
                        isSaving ||
                            characterState?.isLoading == true ||
                            sagaFormState?.isLoading == true

                    val hint =
                        when (flow) {
                            FlowPages.CREATE_SAGA -> sagaFormState?.hint ?: emptyString()
                            FlowPages.CREATE_CHARACTER -> characterState?.hint ?: emptyString()
                        }

                    val backgroundColor by animateColorAsState(
                        targetValue =
                            if (pagerState.currentPage == 0) {
                                genre.color
                            } else {
                                genre
                                    .colorPalette()
                                    .last()
                            },
                        animationSpec = tween(600),
                        label = "backgroundColor",
                    )
                    BottomSheetScaffold(
                        modifier = Modifier.imePadding(),
                        sheetContent = {
                            Column(
                                modifier =
                                    Modifier
                                        .background(fadeGradientTop())
                                        .animateContentSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                if (isReadyToSave) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                scaffoldState.bottomSheetState.show()
                                            }
                                        },
                                        modifier = Modifier.size(24.dp),
                                        colors = IconButtonDefaults.outlinedIconButtonColors(),
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.ic_spark),
                                            null,
                                            tint = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }

                                    SheetContent(
                                        flow,
                                        draft,
                                        characterState?.characterInfo,
                                    )
                                }

                                BottomContent(
                                    genre = genre,
                                    inputField = inputField,
                                    isLoading = isLoading,
                                    hint = hint,
                                    onUpdateInput = { inputField = it },
                                    onSendMessage = { sendMessage() },
                                    onStartAudioRecording = { recordingAudio = true },
                                )
                                val shape =
                                    genre.bubble(
                                        BubbleTailAlignment.BottomRight,
                                        0.dp,
                                        0.dp,
                                        true,
                                    )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(16.dp),
                                ) {
                                    items(suggestions) {
                                        Text(
                                            it,
                                            style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = genre.bodyFont(),
                                                    color = genre.iconColor,
                                                    textAlign = TextAlign.Start,
                                                ),
                                            modifier =
                                                Modifier
                                                    .padding(8.dp)
                                                    .clip(shape)
                                                    .border(
                                                        1.dp,
                                                        genre.color.gradientFade(),
                                                        shape,
                                                    ).background(
                                                        genre.color.gradientFade(),
                                                        shape,
                                                    ).clickable {
                                                        inputField = it
                                                    }.padding(16.dp),
                                        )
                                    }
                                }
                            }
                        },
                        sheetDragHandle = {},
                        scaffoldState = scaffoldState,
                        sheetShape = genre.shape(),
                        sheetPeekHeight = peekHeight,
                        sheetContainerColor = Color.Transparent,
                    ) {
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
                                genre = genre,
                                sagaReady = sagaFormState?.isReady == true,
                                characterReady = characterState?.isReady == true,
                                isLoading = isSaving,
                                currentPage = flow,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedContentScope = this@AnimatedContent,
                                navigateBack = {
                                    navHostController.popBackStack()
                                },
                                onSelectGenre = { viewModel.updateGenre(it) },
                                onTogglePage = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            1 - pagerState.currentPage,
                                        )
                                    }
                                },
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
                                        AnimatedContent(sagaFormState) { sagaState ->
                                            if (sagaState == null) {
                                                EmptyView()
                                            } else {
                                                NewSagaChat(
                                                    sagaState,
                                                )
                                            }
                                        }
                                    }

                                    1 -> {
                                        AnimatedContent(characterState) { characterForm ->
                                            if (characterForm == null) {
                                                EmptyView()
                                            } else {
                                                CharacterCreationView(
                                                    form = draft,
                                                    characterState = characterForm,
                                                    onContinueToSaga = {
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(0)
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    StarryLoader(
                        isLoading = isSaving,
                        loadingMessage = loadingMessage,
                        brushColors = genre.shimmerColors(),
                        textStyle =
                            MaterialTheme.typography.labelMedium.copy(
                                textAlign = TextAlign.Center,
                                fontFamily = genre.bodyFont(),
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                    )

                    // Error dialog
                    if (savingError != null) {
                        AlertDialog(
                            onDismissRequest = { /* Don't dismiss on outside click */ },
                            title = { Text(text = stringResource(R.string.unexpected_error)) },
                            text = { Text(text = savingError ?: "Unknown error") },
                            confirmButton = {
                                TextButton(
                                    onClick = { viewModel.retry() },
                                ) {
                                    Text(stringResource(R.string.try_again))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.reset()
                                        navHostController.popBackStack()
                                    },
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }
                            },
                        )
                    }

                    if (recordingAudio) {
                        AudioRecordingSheet(
                            brush = genre.colorPalette(),
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
            }
        }
    }
}

@Composable
private fun EmptyView() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.ic_spark),
            null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
                    .reactiveShimmer(
                        true,
                        holographicGradient,
                    ),
        )
    }
}

@Composable
private fun SheetContent(
    page: FlowPages,
    sagaDraft: SagaDraft,
    characterInfo: CharacterInfo?,
    togglePage: () -> Unit = {},
    onSave: () -> Unit = {},
) {
    val genre = sagaDraft.genre
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(page, transitionSpec = {
            scaleIn() togetherWith scaleOut()
        }) {
            Icon(
                painter =
                    painterResource(
                        it.icon,
                    ),
                contentDescription = "Toggle page",
                tint = sagaDraft.genre.iconColor,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .size(24.dp)
                        .clickable {
                            togglePage()
                        },
            )
        }

        if (page == FlowPages.CREATE_CHARACTER) {
            Text(
                stringResource(R.string.starring),
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Normal,
                    ),
            )
        }

        val mainText =
            when (page) {
                FlowPages.CREATE_SAGA -> sagaDraft.title
                FlowPages.CREATE_CHARACTER -> characterInfo?.name ?: emptyString()
            }

        Text(
            text = mainText,
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = genre.headerFont(),
                    fontWeight = FontWeight.Bold,
                    shadow =
                        androidx.compose.ui.graphics.Shadow(
                            color = genre.color,
                            blurRadius = 8f,
                        ),
                ),
        )

        val description =
            when (page) {
                FlowPages.CREATE_SAGA -> sagaDraft.description
                FlowPages.CREATE_CHARACTER -> characterInfo?.description ?: emptyString()
            }

        Text(
            text = description,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = genre.bodyFont(),
                    fontWeight = FontWeight.Normal,
                ),
            modifier = Modifier.weight(1f),
        )

        Button(
            onClick = {
                onSave()
            },
            modifier = Modifier.reactiveShimmer(true),
            shape = genre.shape(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                ),
        ) {
            Text(
                stringResource(R.string.save_saga),
                modifier = Modifier.gradientFill(genre.gradient(true)),
            )
        }
    }
}

@Composable
private fun TopBarContent(
    genre: Genre,
    sagaReady: Boolean,
    characterReady: Boolean,
    isLoading: Boolean,
    currentPage: FlowPages,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onSelectGenre: (Genre) -> Unit,
    onTogglePage: () -> Unit,
    navigateBack: () -> Unit = {},
) {
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
                spacingBetweenTooltipAndAnchor = 4.dp,
            )
        val coroutineScope = rememberCoroutineScope()
        val shape =
            genre.bubble(
                BubbleTailAlignment.BottomRight,
                0.dp,
                0.dp,
                true,
            )

        Icon(
            painterResource(R.drawable.ic_back_left),
            null,
            tint = genre.iconColor,
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
            with(sharedTransitionScope) {
                IconButton(
                    onClick = {
                        onTogglePage()
                    },
                    modifier =
                        Modifier
                            .sharedElement(
                                rememberSharedContentState("spark_icon"),
                                animatedContentScope,
                            ).reactiveShimmer(isLoading)
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
            }

            val showBadge = currentPage.isPageComplete(sagaReady, characterReady)

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
                        .padding(16.dp)
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            genre.color.gradientFade(),
                            shape,
                        ).background(
                            MaterialTheme.colorScheme.background,
                            shape,
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
                                    coroutineScope.launch {
                                        tooltipState.dismiss()
                                        onSelectGenre(g)
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
    val shape =
        genre.bubble(
            BubbleTailAlignment.BottomRight,
            0.dp,
            0.dp,
            true,
        )

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
                            spread = 2.dp,
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
                            brush = genre.iconColor.copy(alpha = .1f).solidGradient(),
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
                MaterialTheme.typography.labelMedium.copy(
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
