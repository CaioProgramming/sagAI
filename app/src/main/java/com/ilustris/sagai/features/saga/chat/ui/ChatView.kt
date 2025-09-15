@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalHazeMaterialsApi::class,
)

package com.ilustris.sagai.features.saga.chat.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.ui.ActComponent
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.ui.ChapterContentView
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterDetailsContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChatAction
import com.ilustris.sagai.features.saga.chat.presentation.ChatState
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel
import com.ilustris.sagai.features.saga.chat.presentation.SnackBarState
import com.ilustris.sagai.features.saga.chat.presentation.TimelineSummaryData
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.saga.detail.ui.sharedElementTitleKey
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.ui.TimeLineSimpleCard
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import effectForGenre
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun ChatView(
    navHostController: NavHostController,
    padding: PaddingValues = PaddingValues(0.dp),
    sagaId: String? = null,
    isDebug: Boolean = false,
    viewModel: ChatViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val snackBarMessage by viewModel.snackBarMessage.collectAsStateWithLifecycle()
    val contentHaze = rememberHazeState()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val loreProgress by viewModel.loreUpdateProgress.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    val showTitle by viewModel.showTitle.collectAsStateWithLifecycle()
    val input by viewModel.inputValue.collectAsStateWithLifecycle()
    val senderType by viewModel.sendType.collectAsStateWithLifecycle()
    val typoFix by viewModel.typoFixMessage.collectAsStateWithLifecycle()
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("ChatView", "POST_NOTIFICATIONS permission GRANTED by user.")
            } else {
                Log.w("ChatView", "POST_NOTIFICATIONS permission DENIED by user.")
                // Optionally, show a message indicating that notifications will be disabled,
                // or guide the user on how to enable them later from app settings.
            }
        }

    if (showRationaleDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Notification Permission") },
            text = { Text(stringResource(R.string.notification_explanation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                ) {
                    Text(stringResource(R.string.continue_text))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Not now")
                }
            },
        )
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Check for Android 13+
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    Log.i("ChatView", "POST_NOTIFICATIONS permission already granted.")
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity, // Context needs to be an Activity for this check
                    Manifest.permission.POST_NOTIFICATIONS,
                ) -> {
                    // Show your custom rationale UI (e.g., the dialog)
                    Log.i("ChatView", "Showing rationale for POST_NOTIFICATIONS permission.")
                    showRationaleDialog = true
                }

                else -> {
                    // Permission has not been granted yet, request it.
                    Log.i(
                        "ChatView",
                        "Requesting POST_NOTIFICATIONS permission (first time or no rationale needed).",
                    )
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    LaunchedEffect(sagaId, isDebug) {
        viewModel.initChat(sagaId, isDebug)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    Box {
        AnimatedContent(
            state.value,
            transitionSpec = {
                fadeIn(tween(200)) with fadeOut(tween(700))
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .hazeSource(contentHaze),
        ) {
            when (it) {
                is ChatState.Error ->
                    AnimatedVisibility(isGenerating.not()) {
                        EmptyMessagesView(
                            text = "Saga não encontrada.",
                            brush =
                                gradientAnimation(
                                    holographicGradient,
                                ),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                is ChatState.Success -> {
                    content?.let { cont ->
                        ChatContent(
                            state = state.value,
                            content = cont,
                            inputValue = input,
                            actualSender = senderType,
                            typoFix = typoFix,
                            onUpdateInput = viewModel::updateInput,
                            onUpdateSenders = viewModel::updateSendType,
                            characters = characters,
                            titleModifier = (
                                sharedTransitionScope?.let { sts ->
                                    with(sts) {
                                        Modifier.sharedElement(
                                            rememberSharedContentState(
                                                key = DetailAction.BACK.sharedElementTitleKey(cont.data.id),
                                            ),
                                            animatedVisibilityScope = this@AnimatedContent,
                                        )
                                    }
                                } ?: Modifier
                            ),
                            messagesList = messages,
                            suggestions = suggestions,
                            isGenerating = isGenerating || isLoading,
                            padding = padding,
                            isDebug = isDebug,
                            isPlaying = isPlaying,
                            updateProgress = loreProgress,
                            snackBar = snackBarMessage,
                            onSendMessage = viewModel::sendInput,
                            onBack = navHostController::popBackStack,
                            openSagaDetails = {
                                navHostController.navigateToRoute(
                                    Routes.SAGA_DETAIL,
                                    mapOf("sagaId" to cont.data.id.toString()),
                                )
                            },
                            onInjectFakeMessages = { count ->
                                viewModel.sendFakeUserMessages(count)
                            },
                            onSnackAction = { triple ->
                                when (triple.first) {
                                    ChatAction.RESEND -> viewModel.dismissSnackBar()
                                    ChatAction.RETRY_AI_RESPONSE -> {
                                        viewModel.retryAiResponse(triple.third as? Message)
                                    }

                                    else -> {
                                        content?.data?.let { saga ->
                                            navHostController.navigateToRoute(
                                                Routes.SAGA_DETAIL,
                                                mapOf("sagaId" to saga.id.toString()),
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

                else ->
                    Box(Modifier.fillMaxSize()) {
                        SparkIcon(
                            brush = gradientAnimation(genresGradient()),
                            modifier =
                                Modifier
                                    .size(50.dp)
                                    .align(Alignment.Center),
                            duration = 1.seconds,
                            blurRadius = 1.dp,
                            tint = MaterialTheme.colorScheme.background,
                        )
                    }
            }
        }
    }
}

@Preview
@Composable
fun ChatListPreview() {
    val saga =
        SagaContent(
            data =
                Saga(
                    id = 0,
                    title = "Saga Title",
                    description = "Saga Description",
                    icon = "icon_url",
                    createdAt = System.currentTimeMillis(),
                    genre = Genre.FANTASY,
                    mainCharacterId = 1,
                    currentActId = 1,
                    isEnded = false,
                    endedAt = 0L,
                    isDebug = false,
                    endMessage = "Saga Ended",
                    review = null,
                ),
            mainCharacter = null,
            currentActInfo = null,
            characters = emptyList(),
            wikis = emptyList(),
            acts = emptyList(),
        )
    val actList =
        listOf(
            ActDisplayData(
                content =
                    ActContent(
                        data =
                            Act(
                                id = 1,
                                title = "Act 1",
                                content = "Act 1 content",
                                sagaId = 0,
                                currentChapterId = 1,
                            ),
                    ),
                isComplete = true,
                chapters =
                    listOf(
                        ChapterDisplayData(
                            isComplete = true,
                            chapter =
                                Chapter(
                                    id = 1,
                                    title = "Chapter 1",
                                    overview = "Chapter 1 overview",
                                    coverImage = "cover_image_url",
                                    actId = 1,
                                ),
                            timelineSummaries =
                                emptyList(),
                        ),
                    ),
            ),
        )
    ChatList(
        saga = saga,
        actList = actList,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
        listState = rememberLazyListState(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatContent(
    state: ChatState = ChatState.Loading,
    content: SagaContent,
    characters: List<Character> = emptyList(),
    inputValue: String,
    actualSender: SenderType,
    typoFix: TypoFix?,
    titleModifier: Modifier = Modifier,
    messagesList: List<ActDisplayData> = emptyList(),
    suggestions: List<Suggestion> = emptyList(),
    isGenerating: Boolean = false,
    padding: PaddingValues = PaddingValues(),
    isDebug: Boolean = false,
    isPlaying: Boolean = false,
    updateProgress: Float = 0f,
    snackBar: SnackBarState? = null,
    onSendMessage: (Boolean) -> Unit = { },
    onUpdateInput: (String) -> Unit = { },
    onUpdateSenders: (SenderType) -> Unit = { },
    onBack: () -> Unit = {},
    openSagaDetails: (Saga) -> Unit = {},
    onInjectFakeMessages: (Int) -> Unit = {},
    onSnackAction: (Triple<ChatAction, String, Any?>) -> Unit = {},
) {
    val saga = content.data
    val listState = rememberLazyListState()

    LaunchedEffect(messagesList.size) {
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val bottomSheetState = rememberModalBottomSheetState()
    var showCharacter by remember {
        mutableStateOf<CharacterContent?>(null)
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    ModalNavigationDrawer(drawerContent = {
        ModalDrawerSheet(
            drawerShape =
                RoundedCornerShape(0.dp),
            drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        "Wiki",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = saga.genre.headerFont(),
                                color = saga.genre.color,
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }

                items(content.wikis) {
                    WikiCard(
                        it,
                        saga.genre,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }, drawerState = drawerState) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            Image(
                painterResource(saga.genre.background),
                null,
                colorFilter =
                    ColorFilter.tint(
                        MaterialTheme.colorScheme.background,
                    ),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .reactiveShimmer(
                            isPlaying,
                            shimmerColors = saga.genre.colorPalette(),
                            duration = 10.seconds,
                            targetValue = 200f,
                        ).fillMaxSize(.5f)
                        .alpha(.6f),
            )

            ConstraintLayout(
                Modifier
                    .padding(
                        top = padding.calculateTopPadding(),
                    ).fillMaxSize(),
            ) {
                val coroutineScope = rememberCoroutineScope()
                val (debugControls, messages, chatInput, topBar, bottomFade, topFade, loreProgress) = createRefs()

                ChatList(
                    saga = content,
                    actList = messagesList,
                    listState = listState,
                    isLoading = isGenerating,
                    modifier =
                        Modifier
                            .constrainAs(messages) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom, 50.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                                height = Dimension.fillToConstraints
                            },
                    openCharacter = {
                        showCharacter = it
                    },
                    openSaga = { openSagaDetails(saga) },
                    openWiki = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                )

                Box(
                    Modifier
                        .constrainAs(bottomFade) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }.fillMaxWidth()
                        .fillMaxHeight(.2f)
                        .background(fadeGradientBottom()),
                ) {
                    StarryTextPlaceholder(
                        starCount = 100,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .gradientFill(content.data.genre.gradient()),
                    )
                }

                AnimatedVisibility(
                    state !is ChatState.Loading && saga.isDebug.not() && saga.isEnded.not(),
                    modifier =
                        Modifier
                            .constrainAs(chatInput) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            }.padding(vertical = padding.calculateBottomPadding())
                            .animateContentSize(),
                    enter = slideInVertically(),
                    exit = fadeOut(),
                ) {
                    ChatInputView(
                        content = content,
                        isGenerating = isGenerating,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                        typoFix = typoFix,
                        inputValue = inputValue,
                        sendType = actualSender,
                        onSendMessage = onSendMessage,
                        onUpdateInput = onUpdateInput,
                        onUpdateSender = onUpdateSenders,
                        suggestions = suggestions,
                    )
                }

                val alpha by animateFloatAsState(
                    if (listState.canScrollForward.not()) 0f else 1f,
                    animationSpec = tween(450, easing = EaseIn),
                )

                SagaTopBar(
                    saga.title,
                    "${content.flatMessages().size} mensagens",
                    saga.genre,
                    isLoading = isGenerating,
                    onBackClick = onBack,
                    modifier =
                        Modifier
                            .graphicsLayer(alpha = alpha)
                            .constrainAs(topBar) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }.background(MaterialTheme.colorScheme.background)
                            .padding(top = 50.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .clickable {
                                openSagaDetails(saga)
                            },
                    titleModifier = titleModifier,
                    actionContent = {
                        AnimatedContent(characters, transitionSpec = {
                            slideInVertically() + fadeIn() with fadeOut()
                        }) { chars ->
                            CharactersTopIcons(
                                chars,
                                saga.genre,
                                isLoading = isGenerating,
                            ) { _ -> openSagaDetails(saga) }
                        }
                    },
                )

                val progress by animateFloatAsState(
                    if (content.data.isEnded.not()) updateProgress else 1f,
                )
                val backgroundColor by animateColorAsState(
                    if (snackBar != null) saga.genre.color else Color.Transparent,
                )
                val progressColor by animateColorAsState(
                    if (snackBar == null) saga.genre.color else content.data.genre.iconColor,
                )

                Column(
                    Modifier
                        .background(
                            backgroundColor,
                        ).constrainAs(loreProgress) {
                            top.linkTo(topBar.bottom)
                            start.linkTo(topBar.start)
                            end.linkTo(topBar.end)
                            width = Dimension.fillToConstraints
                        },
                ) {
                    LinearProgressIndicator(
                        modifier =
                            Modifier
                                .alpha(alpha)
                                .height(2.dp)
                                .fillMaxWidth(),
                        progress = { progress },
                        drawStopIndicator = {
                        },
                        color = progressColor,
                        trackColor = Color.Transparent,
                    )

                    AnimatedVisibility(
                        snackBar != null,
                        modifier = Modifier.fillMaxWidth(),
                        enter = slideInVertically { +it } + fadeIn(),
                        exit = fadeOut() + slideOutVertically { it },
                    ) {
                        snackBar?.let { snackBar ->
                            var isExpanded by remember {
                                mutableStateOf(false)
                            }

                            val shape = RoundedCornerShape(content.data.genre.cornerSize())
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(
                                        animationSpec = tween(200, easing = EaseIn),
                                    ),
                            ) {
                                Button(
                                    onClick = {
                                        isExpanded = isExpanded.not()
                                    },
                                    colors =
                                        ButtonDefaults.textButtonColors().copy(
                                            contentColor = content.data.genre.iconColor,
                                        ),
                                    shape = shape,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_spark),
                                        null,
                                        modifier = Modifier.size(12.dp),
                                        tint = content.data.genre.iconColor,
                                    )
                                    Text(
                                        snackBar.title,
                                        style =

                                            MaterialTheme.typography.bodySmall.copy(
                                                color = content.data.genre.iconColor,
                                            ),
                                        fontFamily = content.data.genre.headerFont(),
                                        textAlign = TextAlign.Start,
                                        modifier =
                                            Modifier
                                                .padding(8.dp)
                                                .weight(1f),
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                ) {
                                    Text(
                                        snackBar.text,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = content.data.genre.bodyFont(),
                                        textAlign = TextAlign.Justify,
                                        color = content.data.genre.iconColor,
                                        modifier =
                                            Modifier
                                                .padding(horizontal = 16.dp)
                                                .fillMaxWidth(),
                                    )
                                }

                                AnimatedVisibility(isExpanded) {
                                    if (snackBar.redirectAction != null) {
                                        Button(
                                            onClick = {
                                                onSnackAction(snackBar.redirectAction)
                                            },
                                            colors =
                                                ButtonDefaults.textButtonColors(
                                                    contentColor = content.data.genre.iconColor,
                                                ),
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth(),
                                        ) {
                                            Text(
                                                snackBar.redirectAction.second,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontFamily = content.data.genre.bodyFont(),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isDebug && saga.isEnded.not()) {
                    var fakeMessageCountText by rememberSaveable { mutableStateOf("3") }
                    val blurRadius by animateFloatAsState(
                        if (isGenerating) 40f else 0f,
                    )
                    val shape = RoundedCornerShape(content.data.genre.cornerSize())
                    BlurredGlowContainer(
                        Modifier
                            .padding(16.dp)
                            .constrainAs(debugControls) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                                height = Dimension.wrapContent
                            },
                        content.data.genre.gradient(isGenerating),
                        shape = shape,
                        blurSigma = blurRadius,
                    ) {
                        val textStyle =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = content.data.genre.bodyFont(),
                            )
                        TextField(
                            value = fakeMessageCountText,
                            textStyle = textStyle,
                            onValueChange = { fakeMessageCountText = it },
                            label = {
                                Text(
                                    "Debug controls",
                                    style = textStyle,
                                    modifier = Modifier.scale(.9f),
                                )
                            },
                            placeholder = {
                                Text(
                                    "Number of Fake Messages",
                                    style = textStyle,
                                    modifier = Modifier.alpha(.7f),
                                )
                            },
                            shape = shape,
                            modifier =
                                Modifier
                                    .padding(2.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainer,
                                        shape,
                                    ).fillMaxWidth(),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val count = fakeMessageCountText.toIntOrNull() ?: 0
                                        if (count > 0) {
                                            onInjectFakeMessages(count)
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .background(
                                                content.data.genre.color,
                                                CircleShape,
                                            ).size(32.dp)
                                            .padding(4.dp),
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_inject),
                                        null,
                                        tint = content.data.genre.iconColor,
                                    )
                                }
                            },
                            colors =
                                TextFieldDefaults.colors().copy(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                ),
                        )
                    }
                }
            }
        }
    }

    showCharacter?.let { character ->
        ModalBottomSheet(
            onDismissRequest = { showCharacter = null },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = {
                Box {}
            },
        ) {
            CharacterDetailsContent(
                content,
                character,
                openEvent = {
                    openSagaDetails(saga)
                },
            )
        }
    }
}

@Composable
private fun EmptyMessagesView(
    text: String = "Comece a escrever sua saga!",
    brush: Brush,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        SparkIcon(
            modifier =
                Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally),
            description = "No messages",
            brush = brush,
        )

        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
        )
    }
}

@Composable
fun SagaHeader(
    saga: Saga,
    modifier: Modifier,
    openSaga: () -> Unit,
) {
    Column(modifier) {
        AnimatedVisibility(saga.icon.isNotEmpty()) {
            Box(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .height(350.dp),
            ) {
                AsyncImage(
                    saga.icon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .effectForGenre(saga.genre)
                            .selectiveColorHighlight(
                                saga.genre.selectiveHighlight(),
                            ).fillMaxSize(),
                )

                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(.25f)
                        .background(
                            fadeGradientTop(),
                        ),
                )

                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(.25f)
                        .background(
                            fadeGradientBottom(),
                        ),
                )
            }
        }

        Text(
            saga.title,
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontFamily = saga.genre.headerFont(),
                ),
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .background(fadeGradientTop())
                    .fillMaxWidth()
                    .padding(16.dp)
                    .gradientFill(saga.genre.gradient(true))
                    .clickable {
                        openSaga()
                    },
        )

        var isDescriptionExpanded by remember { mutableStateOf(false) }
        val textColor by animateColorAsState(
            targetValue =
                if (isDescriptionExpanded) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                },
        )
        Text(
            if (isDescriptionExpanded) {
                saga.description
            } else {
                saga.description
                    .take(200)
                    .plus("...")
            },
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    color = textColor,
                    fontFamily = saga.genre.bodyFont(),
                ),
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .clickable {
                        isDescriptionExpanded = !isDescriptionExpanded
                    }.animateContentSize(),
        )
    }
}

@Composable
fun ChatList(
    saga: SagaContent,
    actList: List<ActDisplayData>,
    modifier: Modifier,
    listState: LazyListState,
    isLoading: Boolean = false,
    openCharacter: (CharacterContent?) -> Unit = {},
    openSaga: () -> Unit = {},
    openWiki: () -> Unit = {},
) {
    val animatedMessages = remember { mutableSetOf<Int>() }

    LaunchedEffect(saga) {
        listState.animateScrollToItem(0)
    }

    LazyColumn(
        modifier.animateContentSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        reverseLayout = true,
    ) {
        item {
            Spacer(Modifier.height(64.dp))
        }
        if (saga.data.isEnded && saga.data.endMessage.isNotEmpty()) {
            item {
                Column(
                    modifier =
                        Modifier
                            .animateItem()
                            .padding(16.dp)
                            .fillMaxWidth()
                            .clickable {
                                openSaga()
                            },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.saga_detail_see_your_now),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = saga.data.genre.bodyFont(),
                            ),
                        modifier = Modifier.alpha(.4f),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.saga_detail_recap_button),
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = saga.data.genre.headerFont(),
                                fontWeight = FontWeight.Bold,
                                brush = saga.data.genre.gradient(),
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier.reactiveShimmer(
                                true,
                            ),
                    )
                }
            }

            item {
                Text(
                    saga.data.endMessage,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Justify,
                            fontFamily = saga.data.genre.bodyFont(),
                        ),
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(16.dp),
                )
            }

            item {
                Text(
                    "Sua saga chegou ao fim em ${saga.data.endedAt.formatDate()}",
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = saga.data.genre.bodyFont(),
                        ),
                )
            }
        }
        actList.reversed().forEach { act ->
            val isFirst = act == actList.firstOrNull()
            val genre = saga.data.genre
            val shape = RoundedCornerShape(genre.cornerSize())

            if (act.isComplete) {
                item {
                    ActComponent(
                        act.content,
                        saga.acts.indexOf(act.content) + 1,
                        saga,
                        modifier =
                            Modifier
                                .animateItem()
                                .fillParentMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                    )
                }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.animateItem(),
                    ) {
                        Text(
                            "Fim",
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    brush = genre.gradient(),
                                    fontFamily = genre.headerFont(),
                                    textAlign = TextAlign.Center,
                                ),
                            modifier = Modifier.alpha(.4f),
                        )
                        Text(
                            act.content.data.title,
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    brush = genre.gradient(),
                                    fontFamily = genre.headerFont(),
                                    textAlign = TextAlign.Center,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                        )
                    }
                }
            }

            act.chapters.reversed().forEach { chapter ->

                if (chapter.isComplete) {
                    item {
                        ChapterContentView(
                            chapter.chapter,
                            saga,
                            isLast = act.chapters.lastOrNull() == chapter,
                            imageSize = 400.dp,
                            modifier =
                                Modifier
                                    .animateItem()
                                    .clip(genre.shape())
                                    .fillMaxWidth()
                                    .clickable {
                                        openSaga()
                                    },
                        )
                    }
                }

                chapter.timelineSummaries.reversed().forEach { timeline ->

                    if (timeline.isComplete()) {
                        item {
                            TimeLineSimpleCard(
                                timeline,
                                saga,
                                modifier =
                                    Modifier
                                        .animateItem()
                                        .padding(16.dp)
                                        .clip(
                                            genre.shape(),
                                        ).clickable {
                                            openSaga()
                                        }.fillMaxWidth(),
                            )
                        }
                    }

                    items(timeline.messages.reversed(), key = { it.message.id }) {
                        ChatBubble(
                            it,
                            isLoading = false,
                            content = saga,
                            alreadyAnimatedMessages = animatedMessages,
                            canAnimate = timeline.messages.lastOrNull() == it,
                            modifier = Modifier.animateItem(),
                            openCharacters = { char -> openCharacter(char) },
                            openWiki = { openWiki() },
                        )
                    }
                }

                if (chapter.chapter.introduction.isNotEmpty()) {
                    item {
                        Text(
                            chapter.chapter.introduction,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    textAlign = TextAlign.Justify,
                                ),
                            modifier =
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .padding(16.dp),
                        )
                    }
                }

                item {
                    val title =
                        chapter.chapter.title.ifEmpty {
                            "Capitulo ${
                                saga.chapterNumber(chapter.chapter).toRoman()
                            }"
                        }
                    Text(
                        title,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                brush = genre.gradient(),
                                fontFamily = genre.headerFont(),
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    )
                }
            }

            if (act.content.data.introduction
                    .isNotEmpty()
            ) {
                item {
                    Text(
                        act.content.data.introduction,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                textAlign = TextAlign.Justify,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .padding(16.dp),
                    )
                }
            }

            item {
                val title =
                    act.content.data.title
                        .ifEmpty { "Ato ${saga.actNumber(act.content.data).toRoman()}" }
                Text(
                    title,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            brush = genre.gradient(true),
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                )
            }
        }
        item {
            SagaHeader(
                saga = saga.data,
                modifier =
                    Modifier
                        .fillMaxWidth(),
                openSaga = openSaga,
            )
        }
    }
}

@Composable
fun CharactersTopIcons(
    characters: List<Character>,
    genre: Genre,
    isLoading: Boolean = false,
    onCharacterSelected: (Character?) -> Unit = {},
) {
    val overlapAmount = (-10).dp
    val density = LocalDensity.current
    val charactersToDisplay =
        characters.take(3)
    LazyRow(
        Modifier
            .clip(RoundedCornerShape(genre.cornerSize()))
            .fillMaxWidth(.2f),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(charactersToDisplay) { index, character ->
            val overlapAmountPx = with(density) { overlapAmount.toPx() }
            CharacterAvatar(
                character,
                borderSize = 2.dp,
                borderColor = MaterialTheme.colorScheme.background,
                innerPadding = 0.dp,
                genre = genre,
                pixelation = .1f,
                modifier =
                    Modifier
                        .zIndex(
                            if (index ==
                                0
                            ) {
                                charactersToDisplay.size.toFloat()
                            } else {
                                (charactersToDisplay.size - 1 - index).toFloat()
                            },
                        ).graphicsLayer(
                            translationX = if (index > 0) (index * overlapAmountPx) else 0f,
                        ).clip(CircleShape)
                        .size(24.dp)
                        .clickable { onCharacterSelected(character) },
            )
        }
    }
}
