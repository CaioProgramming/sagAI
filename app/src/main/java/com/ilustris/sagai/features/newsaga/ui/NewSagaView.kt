@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.newsaga.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.CreationSuggestion
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveBackground
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.newsaga.ui.components.CardFace
import com.ilustris.sagai.features.newsaga.ui.components.FlipCard
import com.ilustris.sagai.features.newsaga.ui.components.GenreCard
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.features.newsaga.ui.presentation.FlowPages
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaViewModel
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.fadeColors
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewSagaView(
    navHostController: NavHostController,
    viewModel: NewSagaViewModel = hiltViewModel(),
) {
    val sagaFormState by viewModel.sagaFormState.collectAsStateWithLifecycle()
    val characterState by viewModel.characterState.collectAsStateWithLifecycle()

    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val savingError by viewModel.savingError.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle()
    val genreVisuals by viewModel.genresVisuals.collectAsStateWithLifecycle()
    val savedContent by viewModel.savedContent.collectAsStateWithLifecycle()
    val sagaAssist by viewModel.sagaAssist.collectAsStateWithLifecycle()
    val characterAssist by viewModel.characterAssist.collectAsStateWithLifecycle()
    val themeAssist by viewModel.themeAssist.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    var sagaInput by remember { mutableStateOf("") }
    var characterInput by remember { mutableStateOf("") }

    val pagerState =
        rememberPagerState {
            FlowPages.entries.size
        }

    val genrePagerState =
        rememberPagerState(
            initialPage = Genre.entries.indexOf(sagaFormState?.draft?.genre).coerceAtLeast(0),
        ) { Genre.entries.size }

    val flow =
        remember(pagerState.currentPage) {
            FlowPages.entries[pagerState.currentPage]
        }

    var sagaCardSide by remember {
        mutableStateOf(CardFace.Front)
    }

    var characterCardSide by remember {
        mutableStateOf(CardFace.Front)
    }

    var isSagaRefining by remember { mutableStateOf(false) }
    var isCharacterRefining by remember { mutableStateOf(false) }
    var showGenreCards by remember { mutableStateOf(false) }

    LaunchedEffect(themeAssist) {
        if (themeAssist.title.isNotEmpty() && pagerState.currentPage == FlowPages.SELECT_THEME.ordinal) {
            delay(800)
            showGenreCards = true
        } else {
            showGenreCards = false
        }
    }

    LaunchedEffect(sagaFormState?.isLoading) {
        if (sagaFormState?.isLoading == true) {
            isSagaRefining = true
        } else if (isSagaRefining) {
            if (sagaFormState?.draft?.title?.isNotBlank() == true) {
                sagaCardSide = CardFace.Back
            }
            isSagaRefining = false
        }
    }

    LaunchedEffect(sagaFormState) {
        Log.d("NewSaga", "Saga Form update -> ${sagaFormState.toJsonFormat()}")
    }

    LaunchedEffect(characterState?.isLoading) {
        if (characterState?.isLoading == true) {
            isCharacterRefining = true
        } else if (isCharacterRefining) {
            if (characterState?.characterInfo?.name?.isNotBlank() == true) {
                characterCardSide = CardFace.Back
            }
            isCharacterRefining = false
        }
    }

    // Sync ViewModel -> Pager (only when not swiping)
    LaunchedEffect(sagaFormState?.draft?.genre) {
        val targetGenre = sagaFormState?.draft?.genre
        val targetPage = Genre.entries.indexOf(targetGenre)
        if (targetPage >= 0 && targetPage != genrePagerState.currentPage && !genrePagerState.isScrollInProgress) {
            genrePagerState.animateScrollToPage(targetPage)
        }
    }

    // Sync Pager -> ViewModel (only when settled)
    LaunchedEffect(genrePagerState.settledPage) {
        if (pagerState.currentPage == FlowPages.SELECT_THEME.ordinal) {
            viewModel.updateGenre(Genre.entries[genrePagerState.settledPage])
        }
    }

    val coroutineScope = rememberCoroutineScope()
    rememberBottomSheetScaffoldState()

    fun backPage() {
        coroutineScope.launch {
            if (pagerState.currentPage > 0) {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            } else {
                showExitDialog = true
            }
        }
    }

    BackHandler(enabled = !isSaving) {
        backPage()
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

    LaunchedEffect(pagerState.currentPage) {
        viewModel.assistCreation(FlowPages.entries[pagerState.currentPage])
    }

    fun togglePage() {
        coroutineScope.launch {
            val flow = FlowPages.entries[pagerState.currentPage]

            when (flow) {
                FlowPages.SELECT_THEME,
                FlowPages.CREATE_SAGA, FlowPages.CREATE_CHARACTER,
                -> {
                    pagerState
                        .animateScrollToPage(pagerState.currentPage + 1)
                }

                else -> {
                    doNothing()
                }
            }
        }
    }

    val genre = sagaFormState?.draft?.genre
    val visualConfig = genreVisuals?.find { it.first == genre }?.second

    CompositionLocalProvider(LocalGenreVisualConfig provides visualConfig) {
        val draft = sagaFormState?.draft
        val characterDraft = characterState?.characterInfo
        val assist =
            when (flow) {
                FlowPages.SELECT_THEME -> themeAssist
                FlowPages.CREATE_SAGA -> sagaAssist
                FlowPages.CREATE_CHARACTER -> characterAssist
                else -> null
            }

        val contentColor = MaterialTheme.colorScheme.onBackground

        val isLoading =
            isSaving ||
                characterState?.isLoading == true ||
                sagaFormState?.isLoading == true

        val backgroundColor = MaterialTheme.colorScheme.background

        Box(Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                        .background(backgroundColor.copy(alpha = .2f))
                        .animateContentSize(),
            ) {
                AnimatedContent(genre, transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }) {
                    TopBarContent(
                        genre = it,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        navigateBack = {
                            backPage()
                        },
                    )
                }

                AnimatedContent(
                    targetState = genre,
                    modifier = Modifier.padding(16.dp),
                    label = "AssistAnimation",
                    transitionSpec = {
                        (fadeIn(tween(1000, 200)) + slideInVertically { it / 2 }) togetherWith
                            fadeOut(tween(500))
                    },
                ) {
                    assist?.let { currentAssist ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .reactiveShimmer(isLoading),
                        ) {
                            AutoResizeText(
                                currentAssist.title,
                                style =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontFamily = it?.headerFont(),
                                        letterSpacing = 2.sp,
                                        textAlign = TextAlign.Center,
                                        color = contentColor,
                                    ),
                            )
                            if (currentAssist.subtitle.isNotEmpty()) {
                                Text(
                                    currentAssist.subtitle,
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = it?.bodyFont(),
                                            fontStyle = FontStyle.Italic,
                                        ),
                                    textAlign = TextAlign.Center,
                                    color = contentColor.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }

                HorizontalPager(
                    pagerState,
                    userScrollEnabled = false,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .graphicsLayer(clip = false),
                ) { page ->
                    val flow = FlowPages.entries[page]
                    when (flow) {
                        FlowPages.SELECT_THEME -> {
                            AnimatedVisibility(
                                visible = showGenreCards,
                                enter = fadeIn(tween(1000)) + slideInVertically { it / 4 },
                                exit = fadeOut(tween(500)),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                GenrePicker(
                                    isLoading = isLoading,
                                    visuals = genreVisuals ?: emptyList(),
                                    currentGenre = genre,
                                    pagerState = genrePagerState,
                                ) {
                                    viewModel.updateGenre(it)
                                }
                            }
                        }

                        FlowPages.CREATE_SAGA -> {
                            FlipCardForm(
                                genre = genre,
                                textValue = sagaInput,
                                onTextChange = {
                                    sagaInput = it
                                },
                                title = sagaAssist.title,
                                subtitle = sagaAssist.subtitle,
                                message = sagaAssist.message,
                                inputHint = sagaAssist.inputHint,
                                suggestions = sagaAssist.suggestions,
                                content = draft,
                                face = sagaCardSide,
                                isLoading = isLoading,
                                onFlip = {
                                    sagaCardSide = it
                                },
                                onSeedClick = {
                                    val seed = it.description.ifEmpty { it.text }
                                    sagaInput = seed
                                    viewModel.sendSagaMessage(seed)
                                },
                                onTitleChange = {
                                    draft?.let { s ->
                                        viewModel.updateSagaDraft(s.copy(title = it))
                                    }
                                },
                                onDescriptionChange = {
                                    draft?.let { s ->
                                        viewModel.updateSagaDraft(s.copy(description = it))
                                    }
                                },
                            )
                        }

                        FlowPages.CREATE_CHARACTER -> {
                            FlipCardForm(
                                genre = genre,
                                textValue = characterInput,
                                onTextChange = {
                                    characterInput = it
                                },
                                title = characterAssist.title,
                                subtitle = characterAssist.subtitle,
                                message = characterAssist.message,
                                inputHint = characterAssist.inputHint,
                                suggestions = characterAssist.suggestions,
                                content = characterDraft,
                                face = characterCardSide,
                                isLoading = isLoading,
                                onFlip = {
                                    characterCardSide = it
                                },
                                onSeedClick = {
                                    val seed = it.description.ifEmpty { it.text }
                                    characterInput = seed
                                    viewModel.sendCharacterMessage(seed)
                                },
                                onTitleChange = {
                                    characterDraft?.let { c ->
                                        viewModel.updateCharacterDraft(c.copy(name = it))
                                    }
                                },
                                onDescriptionChange = {
                                    characterDraft?.let { c ->
                                        viewModel.updateCharacterDraft(c.copy(description = it))
                                    }
                                },
                            )
                        }

                        FlowPages.GENERATING -> {
                            var cardFace by remember {
                                mutableStateOf(CardFace.Front)
                            }

                            val shape =
                                genre?.bubble(isNarrator = true) ?: RoundedCornerShape(15.dp)
                            val borderBrush =
                                genre?.gradient(true) ?: SolidColor(Color.Transparent)
                            val contentColor =
                                genre?.resolveIconColor() ?: MaterialTheme.colorScheme.onBackground
                            val backgroundColor =
                                genre?.resolveColor()?.darker(.3f)
                                    ?: MaterialTheme.colorScheme.surfaceContainer
                            val primaryColor =
                                genre?.resolveColor() ?: MaterialTheme.colorScheme.primary
                            FlipCard(
                                cardFace,
                                onClick = {},
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(32.dp)
                                        .dropShadow(shape) {
                                            color = primaryColor
                                            radius = 20f
                                        }.border(1.dp, borderBrush, shape)
                                        .clip(shape)
                                        .background(backgroundColor, shape)
                                        .reactiveShimmer(true),
                                front = {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AnimatedContent(savedContent?.first) {
                                            if (it == null) {
                                                Icon(
                                                    painterResource(R.drawable.ic_spark),
                                                    null,
                                                    tint = contentColor.copy(alpha = .4f),
                                                    modifier =
                                                        Modifier
                                                            .size(64.dp)
                                                            .reactiveShimmer(true),
                                                )
                                            } else {
                                                AsyncImage(
                                                    model =
                                                        ImageRequest
                                                            .Builder(LocalContext.current)
                                                            .data(it.icon)
                                                            .crossfade(true)
                                                            .build(),
                                                    null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier =
                                                        Modifier
                                                            .fillMaxSize()
                                                            .effectForGenre(genre)
                                                            .selectiveColorHighlight(genre),
                                                )

                                                Box(
                                                    Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            MaterialTheme.colorScheme.background.copy(
                                                                alpha = .4f,
                                                            ),
                                                        ),
                                                )

                                                Column(
                                                    verticalArrangement =
                                                        Arrangement.spacedBy(
                                                            8.dp,
                                                        ),
                                                ) {
                                                    genre?.stylisedText(
                                                        it.title,
                                                    )

                                                    Text(
                                                        it.description,
                                                        style =
                                                            MaterialTheme.typography.bodyMedium.copy(
                                                                fontFamily = genre?.bodyFont(),
                                                            ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                back = {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AnimatedContent(savedContent?.second) {
                                            if (it == null) {
                                                Icon(
                                                    painterResource(R.drawable.ic_spark),
                                                    null,
                                                    tint = contentColor.copy(alpha = .4f),
                                                    modifier =
                                                        Modifier
                                                            .size(64.dp)
                                                            .reactiveShimmer(true),
                                                )
                                            } else {
                                                AsyncImage(
                                                    model =
                                                        ImageRequest
                                                            .Builder(LocalContext.current)
                                                            .data(it.image)
                                                            .crossfade(true)
                                                            .build(),
                                                    null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier =
                                                        Modifier
                                                            .fillMaxSize()
                                                            .effectForGenre(genre),
                                                )

                                                Box(
                                                    Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            MaterialTheme.colorScheme.background.copy(
                                                                alpha = .4f,
                                                            ),
                                                        ),
                                                )

                                                Column(
                                                    verticalArrangement =
                                                        Arrangement.spacedBy(
                                                            8.dp,
                                                        ),
                                                ) {
                                                    genre?.stylisedText(
                                                        it.name,
                                                    )

                                                    Text(
                                                        it.backstory,
                                                        style =
                                                            MaterialTheme.typography.bodyMedium.copy(
                                                                fontFamily = genre?.bodyFont(),
                                                            ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                            )
                        }
                    }
                }

                val buttonText =
                    when (flow) {
                        FlowPages.SELECT_THEME,
                        FlowPages.CREATE_SAGA,
                        FlowPages.CREATE_CHARACTER,
                        -> stringResource(R.string.continue_button)

                        FlowPages.GENERATING -> stringResource(R.string.save_saga)
                    }

                AnimatedContent(genre, transitionSpec = {
                    slideInVertically { -it } togetherWith slideOutVertically { it }
                }) {
                    val shape =
                        it?.bubble(isNarrator = true) ?: RoundedCornerShape(15.dp)
                    val contentColor =
                        genre?.resolveIconColor() ?: MaterialTheme.colorScheme.onBackground
                    val primaryColor = it?.resolveColor() ?: MaterialTheme.colorScheme.primary
                    val buttonAlpha by animateFloatAsState(
                        if (FlowPages.entries[pagerState.currentPage] == FlowPages.GENERATING ||
                            (FlowPages.entries[pagerState.currentPage] == FlowPages.SELECT_THEME && !showGenreCards)
                        ) {
                            0f
                        } else {
                            1f
                        },
                    )
                    val glowRadius by animateFloatAsState(
                        if (isLoading) 30f else 10f,
                        animationSpec = tween(500),
                        label = "buttonGlow",
                    )
                    Button(
                        enabled = isLoading.not() && flow != FlowPages.GENERATING,
                        onClick = {
                            when (flow) {
                                FlowPages.SELECT_THEME -> {
                                    togglePage()
                                }

                                FlowPages.CREATE_SAGA -> {
                                    if (sagaCardSide == CardFace.Front) {
                                        viewModel.sendSagaMessage(sagaInput)
                                    } else {
                                        togglePage()
                                    }
                                }

                                FlowPages.CREATE_CHARACTER -> {
                                    if (characterCardSide == CardFace.Front) {
                                        viewModel.sendCharacterMessage(characterInput)
                                    } else {
                                        viewModel.saveSaga()
                                        togglePage()
                                    }
                                }

                                FlowPages.GENERATING -> {
                                    doNothing()
                                }
                            }
                        },
                        colors =
                            ButtonDefaults.buttonColors().copy(
                                containerColor = primaryColor,
                                contentColor = contentColor,
                                disabledContainerColor = MaterialTheme.colorScheme.background,
                                disabledContentColor =
                                    MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.5f,
                                    ),
                            ),
                        shape = shape,
                        border =
                            BorderStroke(
                                1.dp,
                                Brush.linearGradient(
                                    it?.colorPalette() ?: primaryColor.fadeColors(),
                                ),
                            ),
                        modifier =
                            Modifier
                                .alpha(buttonAlpha)
                                .padding(16.dp)
                                .fillMaxWidth()
                                .dropShadow(shape) {
                                    if (isLoading) {
                                        color = primaryColor
                                        radius = glowRadius
                                        this.spread = glowRadius / 2
                                    }
                                },
                    ) {
                        Text(
                            buttonText,
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = genre?.bodyFont(),
                                ),
                        )
                    }
                }

                Spacer(Modifier.height(50.dp))
            }

            StarryLoader(
                isLoading = isSaving,
                loadingMessage = loadingMessage,
                brushColors = genre?.shimmerColors() ?: holographicGradient,
                textStyle =
                    MaterialTheme.typography.labelMedium.copy(
                        textAlign = TextAlign.Center,
                        fontFamily = genre?.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                        shadow =
                            androidx.compose.ui.graphics
                                .Shadow(Color.White, blurRadius = 5f),
                    ),
            )

            OnboardingDialog(type = OnboardingType.CREATION_GUIDE)

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
        }
    }
}

@Composable
private fun SuggestionsContent(
    suggestions: List<CreationSuggestion>,
    modifier: Modifier = Modifier,
    onSelect: (CreationSuggestion) -> Unit = {},
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        items(suggestions) {
            val genre = it.genre
            val shape = genre.bubble(tailHeight = 0.dp, tailWidth = 0.dp, isNarrator = true)
            val itemGradient = it.genre.gradient(true)
            Column(
                modifier =
                    Modifier
                        .fillParentMaxWidth(.7f)
                        .padding(8.dp)
                        .clip(shape)
                        .border(
                            1.dp,
                            itemGradient,
                            shape,
                        ).background(
                            MaterialTheme.colorScheme.background.copy(alpha = .2f),
                            shape,
                        )
                        .clickable {
                            onSelect(it)
                        }.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(genre.resolveBackground())
                                .crossfade(true)
                                .build(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(genre.resolveIconColor()),
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        it.title,
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontFamily = genre.bodyFont(),
                                color = genre.resolveIconColor(),
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }

                Text(
                    it.description.ifEmpty { it.text },
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = genre.resolveIconColor().copy(alpha = 0.8f),
                            textAlign = TextAlign.Start,
                        ),
                )
            }
        }
    }
}

@Composable
private fun TopBarContent(
    genre: Genre?,
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
                .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_back_left),
                null,
                tint = genre?.resolveIconColor() ?: MaterialTheme.colorScheme.onBackground,
                modifier =
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable {
                            navigateBack()
                        },
            )

            Text(
                stringResource(R.string.home_create_new_saga_title),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre?.bodyFont(),
                        textAlign = TextAlign.Center,
                    ),
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun GenrePicker(
    isLoading: Boolean,
    currentGenre: Genre?,
    visuals: List<Pair<Genre, GenreVisualConfig?>>,
    pagerState: PagerState,
    onPageChange: (Genre) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    HorizontalPager(
        pagerState,
        userScrollEnabled = !isLoading,
        contentPadding = PaddingValues(50.dp),
        pageSpacing = 16.dp,
        beyondViewportPageCount = 1,
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
    ) { page ->
        val genre = Genre.entries[page]
        val isSelected = genre == currentGenre

        val visual = visuals.find { it.first == genre }?.second
        CompositionLocalProvider(LocalGenreVisualConfig provides visual) {
            GenreCard(
                genre,
                isSelected,
                isLoading = isLoading && isSelected,
                modifier =
                    Modifier
                        .graphicsLayer {
                            val pageOffset =
                                (
                                        (pagerState.currentPage - page) +
                                                pagerState
                                            .currentPageOffsetFraction
                                ).absoluteValue
                            lerp(
                                start = 0.85f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f),
                            ).also { scale ->
                                scaleX = scale
                                scaleY = scale
                            }
                            alpha =
                                lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f),
                                )
                        }
                        .levitate(isSelected)
                        .fillMaxWidth()
                        .aspectRatio(0.8f),
            ) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(page)
                }
            }
        }
    }
}

@Composable
private fun FlipCardForm(
    genre: Genre?,
    textValue: String,
    onTextChange: (String) -> Unit,
    title: String = "",
    subtitle: String = "",
    message: String = "",
    inputHint: String?,
    suggestions: List<CreationSuggestion>,
    content: Any?,
    face: CardFace,
    isLoading: Boolean = false,
    onEnhanceClick: () -> Unit = {},
    onSeedClick: (CreationSuggestion) -> Unit = {},
    onFlip: (CardFace) -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
) {
    val shape = genre?.bubble(isNarrator = true) ?: RoundedCornerShape(24.dp)
    val borderBrush = genre?.gradient(true) ?: SolidColor(Color.Transparent)
    val contentColor = genre?.resolveIconColor() ?: MaterialTheme.colorScheme.onBackground
    val backgroundColor =
        genre?.resolveColor()?.darker(.4f) ?: MaterialTheme.colorScheme.surfaceContainer
    val primaryColor = genre?.resolveColor() ?: MaterialTheme.colorScheme.primary

    FlipCard(
        modifier =
            Modifier
                .padding(8.dp)
                .fillMaxSize(),
        cardFace = face,
        onClick = {
            if (face == CardFace.Back) {
                onFlip(CardFace.Front)
            } else {
                onFlip(CardFace.Back)
            }
        },
        front = {
            if (title.isEmpty()) {
                Box(
                    Modifier
                        .imePadding()
                        .fillMaxSize()
                        .reactiveShimmer(true),
                ) {
                    val icon = genre?.resolveBackground() ?: R.drawable.ic_spark
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(icon)
                                .crossfade(true)
                                .build(),
                        null,
                        modifier =
                            Modifier
                                .size(64.dp)
                                .align(Alignment.Center)
                                .reactiveShimmer(isLoading, primaryColor.shimmerize()),
                        colorFilter =
                            ColorFilter.tint(
                                contentColor.copy(alpha = .4f),
                            ),
                    )
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .dropShadow(shape, {
                                color = primaryColor
                                radius = 25f
                            })
                            .border(1.dp, borderBrush, shape)
                            .clip(shape)
                            .background(backgroundColor, shape)
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title.ifBlank { "DESCRIBE YOUR STORY" },
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontFamily = genre?.bodyFont(),
                                fontWeight = FontWeight.SemiBold,
                            ),
                        color = contentColor,
                    )

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = genre?.bodyFont(),
                                ),
                            color = contentColor.copy(alpha = 0.6f),
                        )
                    }

                    // Main Text Input
                    BasicTextField(
                        enabled = !isLoading,
                        value = textValue,
                        onValueChange = onTextChange,
                        textStyle =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = genre?.bodyFont(),
                                color = contentColor,
                            ),
                        cursorBrush = SolidColor(contentColor),
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .reactiveShimmer(isLoading),
                        decorationBox = { innerTextField ->
                            Box {
                                if (textValue.isEmpty()) {
                                    Text(
                                        text = inputHint ?: "Once upon a time...",
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = genre?.bodyFont(),
                                            ),
                                        color = contentColor.copy(alpha = 0.15f),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )

                    // Suggestions Section
                    if (suggestions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_lamp),
                                    null,
                                    modifier = Modifier.size(24.dp),
                                    tint = contentColor.copy(alpha = .3f),
                                )

                                Text(
                                    text = "Sugestões",
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = genre?.bodyFont(),
                                        ),
                                    color = contentColor.copy(alpha = .3f),
                                )
                            }

                            SuggestionsContent(
                                suggestions = suggestions,
                                modifier = Modifier.reactiveShimmer(isLoading),
                                onSelect = {
                                    if (!isLoading) {
                                        onSeedClick(it)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        },
        back = {
            val isValidData =
                when (content) {
                    is SagaDraft -> {
                        val data = content
                        data.title.isNotBlank() || data.description.isNotBlank()
                    }

                    is CharacterInfo -> {
                        val data = content
                        data.name.isNotBlank() || data.gender.isNotBlank() || data.description.isNotBlank()
                    }

                    else -> {
                        false
                    }
                }

            AnimatedContent(
                isValidData,
                Modifier
                    .fillMaxSize()
                    .border(1.dp, borderBrush, shape)
                    .clip(shape)
                    .background(backgroundColor, shape),
            ) {
                if (it) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        when (content) {
                            is SagaDraft -> {
                                val genre = content.genre

                                BasicTextField(
                                    value = content.title,
                                    onValueChange = onTitleChange,
                                    textStyle =
                                        MaterialTheme.typography.displaySmall.copy(
                                            fontFamily = genre.headerFont(),
                                            textAlign = TextAlign.Center,
                                            brush = Brush.verticalGradient(genre.colorPalette()),
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    cursorBrush = SolidColor(genre.resolveIconColor()),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.Center) {
                                            if (content.title.isEmpty()) {
                                                Text(
                                                    "Saga Title",
                                                    style =
                                                        MaterialTheme.typography.displaySmall.copy(
                                                            fontFamily = genre.headerFont(),
                                                            textAlign = TextAlign.Center,
                                                            color =
                                                                genre
                                                                    .resolveIconColor()
                                                                    .copy(alpha = 0.3f),
                                                        ),
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                )

                                BasicTextField(
                                    value = content.description,
                                    onValueChange = onDescriptionChange,
                                    textStyle =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = genre.bodyFont(),
                                            textAlign = TextAlign.Center,
                                            color = genre.resolveIconColor(),
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp),
                                    cursorBrush = SolidColor(genre.resolveIconColor()),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.Center) {
                                            if (content.description.isEmpty()) {
                                                Text(
                                                    "Saga Description",
                                                    style =
                                                        MaterialTheme.typography.bodyMedium.copy(
                                                            fontFamily = genre.bodyFont(),
                                                            textAlign = TextAlign.Center,
                                                            color =
                                                                genre
                                                                    .resolveIconColor()
                                                                    .copy(alpha = 0.3f),
                                                        ),
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                )
                            }

                            is CharacterInfo -> {
                                BasicTextField(
                                    value = content.name,
                                    onValueChange = onTitleChange,
                                    textStyle =
                                        MaterialTheme.typography.displaySmall.copy(
                                            fontFamily =
                                                genre?.headerFont()
                                                    ?: MaterialTheme.typography.displaySmall.fontFamily,
                                            textAlign = TextAlign.Center,
                                            brush =
                                                Brush.verticalGradient(
                                                    genre?.colorPalette() ?: listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.secondary,
                                                    ),
                                                ),
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    cursorBrush =
                                        SolidColor(
                                            genre?.resolveIconColor()
                                                ?: MaterialTheme.colorScheme.onSurface,
                                        ),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.Center) {
                                            if (content.name.isEmpty()) {
                                                Text(
                                                    "Character Name",
                                                    style =
                                                        MaterialTheme.typography.displaySmall.copy(
                                                            fontFamily = genre?.headerFont(),
                                                            textAlign = TextAlign.Center,
                                                            color =
                                                                (
                                                                    genre?.resolveIconColor()
                                                                        ?: MaterialTheme.colorScheme.onSurface
                                                                ).copy(
                                                                    alpha = 0.3f,
                                                                ),
                                                        ),
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                )

                                Text(
                                    content.gender,
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = genre?.bodyFont(),
                                            color =
                                                genre?.resolveColor()
                                                    ?: MaterialTheme.colorScheme.primary,
                                        ),
                                )

                                BasicTextField(
                                    value = content.description,
                                    onValueChange = onDescriptionChange,
                                    textStyle =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = genre?.bodyFont(),
                                            textAlign = TextAlign.Center,
                                            color =
                                                genre?.resolveIconColor()
                                                    ?: MaterialTheme.colorScheme.onSurface,
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp),
                                    cursorBrush =
                                        SolidColor(
                                            genre?.resolveIconColor()
                                                ?: MaterialTheme.colorScheme.onSurface,
                                        ),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.Center) {
                                            if (content.description.isEmpty()) {
                                                Text(
                                                    "Character Description",
                                                    style =
                                                        MaterialTheme.typography.bodyMedium.copy(
                                                            fontFamily = genre?.bodyFont(),
                                                            textAlign = TextAlign.Center,
                                                            color =
                                                                (
                                                                    genre?.resolveIconColor()
                                                                        ?: MaterialTheme.colorScheme.onSurface
                                                                ).copy(
                                                                    alpha = 0.3f,
                                                                ),
                                                        ),
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                )
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize()) {
                        val icon = genre?.resolveBackground() ?: R.drawable.ic_spark
                        AsyncImage(
                            model =
                                ImageRequest
                                    .Builder(LocalContext.current)
                                    .data(icon)
                                    .crossfade(true)
                                    .build(),
                            null,
                            modifier =
                                Modifier
                                    .size(64.dp)
                                    .align(Alignment.Center),
                            colorFilter =
                                ColorFilter.tint(
                                    contentColor.copy(alpha = .4f),
                                ),
                        )
                    }
                }
            }
        },
    )
}
