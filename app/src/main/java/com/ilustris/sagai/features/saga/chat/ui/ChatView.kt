@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalSharedTransitionApi::class,
)

package com.ilustris.sagai.features.saga.chat.ui

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.file.BACKUP_PERMISSION
import com.ilustris.sagai.core.file.backup.ui.BackupSheet
import com.ilustris.sagai.core.permissions.PermissionComponent
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionService.Companion.openAppSettings
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.act.ui.ActComponent
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.ui.ChapterContentView
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterDetailsContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChatState
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel
import com.ilustris.sagai.features.saga.chat.ui.components.CharacterRevealOverlay
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView
import com.ilustris.sagai.features.saga.chat.ui.components.ReactionsBottomSheet
import com.ilustris.sagai.features.saga.detail.ui.RecapHeroCard
import com.ilustris.sagai.features.saga.detail.ui.WikiContent
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.ui.TimeLineCard
import com.ilustris.sagai.features.timeline.ui.TimeLineSimpleCard
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.components.SagaSnackBar
import com.ilustris.sagai.ui.components.SnackAction
import com.ilustris.sagai.ui.components.SnackBarState
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import effectForGenre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ChatView(
    navHostController: NavHostController,
    padding: PaddingValues = PaddingValues(0.dp),
    sagaId: String? = null,
    isDebug: Boolean = false,
    viewModel: ChatViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val showTitle by viewModel.showTitle.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val snackBarMessage by viewModel.snackBarMessage.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val loreProgress by viewModel.loreUpdateProgress.collectAsStateWithLifecycle()
    val context = LocalActivity.current
    val selectedCharacter by viewModel.selectedCharacter.collectAsStateWithLifecycle()
    val input by viewModel.inputValue.collectAsStateWithLifecycle()
    val senderType by viewModel.sendType.collectAsStateWithLifecycle()
    val typoFix by viewModel.typoFixMessage.collectAsStateWithLifecycle()
    val messageEffectsEnabled by viewModel.messageEffectsEnabled.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    var requiredPermission by remember { mutableStateOf<String?>(null) }
    val requestPermissionLauncher = PermissionService.rememberPermissionLauncher()
    val originalBitmap by viewModel.originalBitmap.collectAsStateWithLifecycle()
    val segmentedBitmap by viewModel.segmentedBitmap.collectAsStateWithLifecycle()
    var showCharacter by remember {
        mutableStateOf<CharacterContent?>(null)
    }
    val newCharacterReveal by viewModel.newCharacterReveal.collectAsStateWithLifecycle()
    val selectionState by viewModel.selectionState.collectAsStateWithLifecycle()
    var showShareSheet by remember { mutableStateOf(false) }

    LaunchedEffect(content) {
        content?.let {
            if (it.data.icon.isNotEmpty()) {
                viewModel.segmentSagaCover(it.data.icon)
            }
        }
    }

    LaunchedEffect(Unit) {
        PermissionService.requestPermission(
            activity,
            requiredPermission,
            requestPermissionLauncher,
        ) {
            requiredPermission = Manifest.permission.POST_NOTIFICATIONS
        }

        viewModel.initChat(sagaId, isDebug)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var showBackupSheet by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    fun navigateToSaga() {
        sagaId ?: return
        navHostController.navigateToRoute(Routes.SAGA_DETAIL, mapOf("sagaId" to sagaId))
    }

    Box {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = content != null,
            drawerContent = {
                ModalDrawerSheet(
                    drawerShape = RoundedCornerShape(0.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.background,
                ) {
                    content?.let {
                        WikiContent(it, onBackClick = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }, reviewWiki = viewModel::reviewWiki, onHoldWiki = {
                            viewModel.appendWiki(it)
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        })
                    }
                }
            },
        ) {
            with(sharedTransitionScope) {
                AnimatedContent(
                    state.value,
                    transitionSpec = {
                        fadeIn(tween(200)) with fadeOut(tween(700))
                    },
                    modifier =
                        Modifier
                            .fillMaxSize(),
                ) {
                    when (it) {
                        is ChatState.Error ->
                            AnimatedVisibility(isGenerating.not()) {
                                EmptyMessagesView(
                                    text = stringResource(id = R.string.saga_not_found),
                                    brush =
                                        gradientAnimation(
                                            holographicGradient,
                                        ),
                                    modifier = Modifier.align(Alignment.Center),
                                )
                            }

                        is ChatState.Success -> {
                            content?.let { cont ->
                                AnimatedContent(showTitle) {
                                    if (it) {
                                        Box(Modifier.fillMaxSize()) {
                                            val genre = remember { cont.data.genre }
                                            genre.stylisedText(
                                                cont.data.title,
                                                modifier =
                                                    Modifier
                                                        .align(Alignment.Center)
                                                        .reactiveShimmer(true)
                                                        .padding(16.dp)
                                                        .sharedElement(
                                                            rememberSharedContentState(
                                                                key = "saga-style-header",
                                                            ),
                                                            animatedVisibilityScope = this@AnimatedContent,
                                                        ),
                                            )
                                        }
                                    } else {
                                        ChatContent(
                                            state = state.value,
                                            content = cont,
                                            inputValue = input,
                                            actualSender = senderType,
                                            typoFix = typoFix,
                                            onUpdateInput = viewModel::updateInput,
                                            onUpdateSenders = viewModel::updateSendType,
                                            characters = characters,
                                            currentCharacter = selectedCharacter,
                                            titleModifier =
                                                Modifier.sharedElement(
                                                    rememberSharedContentState(
                                                        key = "saga-style-header",
                                                    ),
                                                    animatedVisibilityScope = this@AnimatedContent,
                                                ),
                                            messagesList = messages,
                                            suggestions = suggestions,
                                            isGenerating = isGenerating || isLoading,
                                            isLoading = isGenerating || isLoading,
                                            onRefresh = viewModel::checkSaga,
                                            padding = padding,
                                            isDebug = isDebug,
                                            isPlaying = isPlaying,
                                            sharedTransitionScope = sharedTransitionScope,
                                            updateProgress = loreProgress,
                                            snackBar = snackBarMessage,
                                            onSendMessage = viewModel::sendInput,
                                            onBack = navHostController::popBackStack,
                                            onRetryMessage = viewModel::retryAiResponse,
                                            requestNewCharacter = viewModel::requestNewCharacter,
                                            reviewEvent = viewModel::reviewEvent,
                                            reviewChapter = viewModel::reviewChapter,
                                            openSagaDetails = {
                                                navHostController.navigateToRoute(
                                                    Routes.SAGA_DETAIL,
                                                    mapOf("sagaId" to cont.data.id.toString()),
                                                )
                                            },
                                            onInjectFakeMessages = { count ->
                                                viewModel.sendFakeUserMessages(count)
                                            },
                                            selectCharacter = {
                                                showCharacter = it
                                            },
                                            updateCharacter = viewModel::updateCharacter,
                                            messageEffectsEnabled = messageEffectsEnabled,
                                            originalBitmap = originalBitmap,
                                            segmentedBitmap = segmentedBitmap,
                                            isSelectionMode = selectionState.isSelectionMode,
                                            selectedMessageIds = selectionState.selectedMessageIds,
                                            onToggleSelectionMode = viewModel::toggleSelectionMode,
                                            onToggleMessageSelection = viewModel::toggleMessageSelection,
                                            onClearSelection = viewModel::clearSelection,
                                            onShareConversation = {
                                                showShareSheet = true
                                            },
                                        )
                                    }
                                }
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

        SagaSnackBar(
            snackBarMessage,
            content?.data?.genre,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(content?.data?.genre?.shape() ?: RectangleShape)
                    .clickable {
                        if (snackBarMessage?.action == null) {
                            viewModel.dismissSnackBar()
                        }
                    },
        ) {
            when (it) {
                is SnackAction.OpenDetails -> {
                    when (val data = it.data) {
                        is Character -> {
                            showCharacter =
                                content?.findCharacter(data.id)
                        }

                        is Wiki -> {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }

                        is Chapter -> {
                            navigateToSaga()
                        }
                    }
                }

                is SnackAction.ResendMessage -> {
                    viewModel.retryAiResponse(it.message)
                }

                is SnackAction.RetryCharacter -> {
                    viewModel.requestNewCharacter(it.description)
                }

                is SnackAction.RevaluateSaga -> {
                    viewModel.checkSaga()
                }

                is SnackAction.EnableBackup -> {
                    requiredPermission = BACKUP_PERMISSION
                }
            }

            viewModel.dismissSnackBar()
        }

        content?.let {
            showCharacter?.let { character ->
                val bottomSheetState = rememberModalBottomSheetState()

                ModalBottomSheet(
                    onDismissRequest = { showCharacter = null },
                    sheetState = bottomSheetState,
                    containerColor = MaterialTheme.colorScheme.background,
                    dragHandle = {
                        Box {}
                    },
                ) {
                    CharacterDetailsContent(
                        it,
                        character,
                        openEvent = {
                            navigateToSaga()
                        },
                    )
                }
            }
        }

        if (showBackupSheet) {
            BackupSheet(onDismiss = { showBackupSheet = false })
        }

        val backupLauncher =
            PermissionService.rememberBackupLauncher { uri ->
                viewModel.enableBackup(uri)
                requiredPermission = null
            }

        PermissionComponent(onConfirm = {
            if (requiredPermission != BACKUP_PERMISSION) {
                context?.let {
                    openAppSettings(context)
                    requiredPermission = null
                }
            } else {
                backupLauncher.launch(null)
            }
        }) {
            requiredPermission = null
        }

        AnimatedVisibility(
            isGenerating,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(animationSpec = tween(700)) + shrinkOut()
        ) {
            val shimmerColors = content?.data?.genre?.shimmerColors() ?: holographicGradient
            StarryTextPlaceholder(
                modifier = Modifier.reactiveShimmer(true, shimmerColors),
            )
        }

        newCharacterReveal?.let { id ->
            content?.let { sagaContent ->
                val character = sagaContent.characters.find { it.data.id == id }

                CharacterRevealOverlay(
                    character = character,
                    sagaContent = sagaContent,
                    onDismiss = viewModel::dismissCharacterReveal
                )
            }
        }

        content?.let {
            ShareSheet(
                content = it,
                isVisible = showShareSheet,
                shareType = ShareType.CONVERSATION,
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatContent(
    state: ChatState = ChatState.Loading,
    content: SagaContent,
    characters: List<Character> = emptyList(),
    inputValue: TextFieldValue,
    actualSender: SenderType,
    typoFix: TypoFix?,
    titleModifier: Modifier = Modifier,
    messagesList: List<ActDisplayData> = emptyList(),
    suggestions: List<Suggestion> = emptyList(),
    isGenerating: Boolean = false,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    padding: PaddingValues = PaddingValues(),
    isDebug: Boolean = false,
    isPlaying: Boolean = false,
    updateProgress: Float = 0f,
    snackBar: SnackBarState? = null,
    sharedTransitionScope: SharedTransitionScope,
    currentCharacter: CharacterContent?,
    onSendMessage: (Boolean) -> Unit = { },
    onUpdateInput: (TextFieldValue) -> Unit = { },
    onUpdateSenders: (SenderType) -> Unit = { },
    onBack: () -> Unit = {},
    openSagaDetails: (Saga) -> Unit = {},
    onInjectFakeMessages: (Int) -> Unit = {},
    onRetryMessage: (Message) -> Unit = {},
    openDrawer: () -> Unit = {},
    selectCharacter: (CharacterContent) -> Unit = {},
    requestNewCharacter: (String) -> Unit = {},
    reviewEvent: (TimelineContent) -> Unit = {},
    reviewChapter: (ChapterContent) -> Unit = {},
    updateCharacter: (CharacterContent) -> Unit = {},
    messageEffectsEnabled: Boolean = true,
    originalBitmap: Bitmap? = null,
    segmentedBitmap: Bitmap? = null,
    isSelectionMode: Boolean = false,
    selectedMessageIds: Set<Int> = emptySet(),
    onToggleSelectionMode: () -> Unit = {},
    onToggleMessageSelection: (Int) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onShareConversation: () -> Unit = {},
) {
    val saga = remember { content.data }
    val timeline = remember { content.getCurrentTimeLine() }
    val listState = rememberLazyListState()

    var showReactions by remember {
        mutableStateOf<MessageContent?>(null)
    }

    var objectiveExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(timeline) {
        delay(3.seconds)
        objectiveExpanded = timeline
            ?.data
            ?.currentObjective
            ?.isEmpty()
            ?.not() == true

        delay(4.seconds)

        objectiveExpanded = false
    }
    val imeState = WindowInsets.isImeVisible
    val progress by animateFloatAsState(
        if (content.data.isEnded.not()) updateProgress else 1f,
    )

    LaunchedEffect(messagesList.size) {
        if (messagesList.isNotEmpty()) {
            if (objectiveExpanded) {
                delay(2.seconds)
            }
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && objectiveExpanded) {
            objectiveExpanded = false
        }
    }

    LaunchedEffect(imeState) {
        if (imeState && objectiveExpanded) {
            objectiveExpanded = false
        }
    }

    LaunchedEffect(inputValue) {
        objectiveExpanded = false
    }

    with(sharedTransitionScope) {
        Box {
            val blur by animateDpAsState(
                if (objectiveExpanded) 10.dp else 0.dp,
            )
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .blur(blur),
            ) {
                Image(
                    painterResource(saga.genre.background),
                    null,
                    colorFilter =
                        ColorFilter.tint(
                            MaterialTheme.colorScheme.surfaceContainer.darker(.2f),
                        ),
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .reactiveShimmer(
                                isPlaying,
                                shimmerColors = saga.genre.shimmerColors(),
                                duration = 10.seconds,
                                targetValue = 1000f,
                            )
                            .fillMaxSize(.5f)
                            .alpha(.3f),
                )

                ConstraintLayout(
                    Modifier
                        .padding(
                            top = padding.calculateTopPadding(),
                        )
                        .fillMaxSize(),
                ) {
                    rememberCoroutineScope()
                    val (debugControls, messages, chatInput, topBar, bottomFade, _, loreProgress) = createRefs()

                    ChatList(
                        saga = content,
                        actList = messagesList,
                        listState = listState,
                        isLoading = isLoading,
                        onRefresh = onRefresh,
                        objectiveExpanded = objectiveExpanded,
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
                            it?.let { character -> selectCharacter(character) }
                        },
                        openSaga = { openSagaDetails(saga) },
                        openWiki = {
                            openDrawer()
                        },
                        openReactions = {
                            showReactions = it
                        },
                        reviewChapter = reviewChapter,
                        onRetryMessage = onRetryMessage,
                        requestNewCharacter = requestNewCharacter,
                        reviewEvent = reviewEvent,
                        messageEffectsEnabled = messageEffectsEnabled,
                        originalBitmap = originalBitmap,
                        segmentedBitmap = segmentedBitmap,
                        isSelectionMode = isSelectionMode,
                        selectedMessageIds = selectedMessageIds,
                        onToggleSelectionMode = onToggleSelectionMode,
                        onToggleMessageSelection = onToggleMessageSelection,
                    )

                    Box(
                        Modifier
                            .constrainAs(bottomFade) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .fillMaxWidth()
                            .fillMaxHeight(.2f)
                            .background(fadeGradientBottom()),
                    )

                    AnimatedVisibility(
                        state !is ChatState.Loading && saga.isDebug.not() && saga.isEnded.not() && !isSelectionMode,
                        modifier =
                            Modifier
                                .constrainAs(chatInput) {
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    width = Dimension.fillToConstraints
                                }
                                .padding(vertical = padding.calculateBottomPadding())
                                .animateContentSize(),
                        enter = slideInVertically(),
                        exit = slideOutVertically { it },
                    ) {
                        ChatInputView(
                            content = content,
                            isGenerating = isGenerating,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                            selectedCharacter = currentCharacter,
                            typoFix = typoFix,
                            inputField = inputValue,
                            sendType = actualSender,
                            sharedTransitionScope = this@with,
                            onSendMessage = onSendMessage,
                            onUpdateInput = onUpdateInput,
                            onUpdateSender = onUpdateSenders,
                            suggestions = suggestions,
                            onSelectCharacter = updateCharacter,
                        )
                    }

                    // Floating action button for sharing conversation snippets
                    AnimatedVisibility(
                        visible = isSelectionMode,
                        modifier = Modifier
                            .constrainAs(createRef()) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            }
                            .padding(
                                bottom = padding.calculateBottomPadding() + 16.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(saga.genre.color)
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onClearSelection,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.round_close_24),
                                    contentDescription = stringResource(R.string.cancel),
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize(),
                                )
                            }

                            Text(
                                stringResource(
                                    R.string.messages_selected,
                                    selectedMessageIds.size,
                                    10
                                ),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = saga.genre.bodyFont(),
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = onShareConversation,
                                enabled = selectedMessageIds.isNotEmpty(),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_share),
                                    contentDescription = stringResource(R.string.share),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize(),
                                    tint = if (selectedMessageIds.isNotEmpty()) Color.White else Color.White.copy(
                                        alpha = 0.5f
                                    )
                                )
                            }
                        }
                    }

                    val alpha by animateFloatAsState(
                        if (listState.canScrollForward.not()) 0f else 1f,
                        animationSpec = tween(450, easing = EaseIn),
                    )
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .graphicsLayer(alpha = alpha)
                                .animateContentSize()
                                .constrainAs(topBar) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                                .background(MaterialTheme.colorScheme.background),
                    ) {
                        AnimatedVisibility(
                            objectiveExpanded.not(),
                            modifier =
                                Modifier.align(Alignment.CenterHorizontally),
                            enter = scaleIn() + fadeIn(),
                            exit = fadeOut() + slideOutVertically { -it },
                        ) {
                            val currentObjective =
                                content.getCurrentTimeLine()?.data?.currentObjective

                            val infiniteTransition = rememberInfiniteTransition()
                            val scaleAnimation =
                                infiniteTransition.animateFloat(
                                    1f,
                                    1.3f,
                                    infiniteRepeatable(
                                        tween(
                                            1.seconds.toInt(DurationUnit.MILLISECONDS),
                                            easing = EaseIn,
                                        ),
                                        repeatMode = Reverse,
                                    ),
                                )

                            Image(
                                painterResource(R.drawable.ic_spark),
                                contentDescription = null,
                                colorFilter =
                                    ColorFilter.tint(
                                        MaterialTheme.colorScheme.onBackground.copy(
                                            alpha = .3f,
                                        ),
                                    ),
                                modifier =
                                    Modifier
                                        .scale(if (isGenerating) scaleAnimation.value else 1f)
                                        .clip(CircleShape)
                                        .clickable(enabled = currentObjective?.isNotEmpty() == true) {
                                            objectiveExpanded = true
                                        }
                                        .size(24.dp)
                                        .gradientFill(
                                            progressiveBrush(
                                                content.data.genre.color,
                                                progress,
                                            ),
                                        )
                                        .reactiveShimmer(isGenerating)
                                        .sharedElement(
                                            rememberSharedContentState(
                                                key = "current_objective_${content.data.id}",
                                            ),
                                            animatedVisibilityScope = this@AnimatedVisibility,
                                        ),
                            )
                        }

                        val subtitle =
                            if (saga.isEnded) {
                                stringResource(id = R.string.chat_card_saga_ended)
                            } else {
                                stringResource(
                                    R.string.chat_view_subtitle,
                                    content.actNumber(content.currentActInfo?.data).toRoman(),
                                    content
                                        .chapterNumber(content.currentActInfo?.currentChapterInfo?.data)
                                        .toRoman(),
                                )
                            }

                        SagaTopBar(
                            saga.title,
                            subtitle,
                            saga.genre,
                            isLoading = isGenerating,
                            onBackClick = onBack,
                            modifier =
                                Modifier
                                    .clickable {
                                        openSagaDetails(saga)
                                    }
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
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
                    }

                    val backgroundColor by animateColorAsState(
                        if (snackBar != null) saga.genre.color else Color.Transparent,
                    )

                    Column(
                        Modifier
                            .background(
                                backgroundColor,
                            )
                            .constrainAs(loreProgress) {
                                top.linkTo(topBar.bottom)
                                start.linkTo(topBar.start)
                                end.linkTo(topBar.end)
                                width = Dimension.fillToConstraints
                            },
                    ) {
                        AnimatedVisibility(objectiveExpanded.not()) {
                            LinearProgressIndicator(
                                modifier =
                                    Modifier
                                        .sharedElement(
                                            rememberSharedContentState(
                                                key = "lore_progress_${content.data.id}",
                                            ),
                                            animatedVisibilityScope = this,
                                        )
                                        .alpha(alpha)
                                        .height(1.dp)
                                        .fillMaxWidth(),
                                progress = { progress },
                                drawStopIndicator = {},
                                gapSize = 0.dp,
                                color = content.data.genre.color,
                                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                            )
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
                                        stringResource(id = R.string.debug_controls),
                                        style = textStyle,
                                        modifier = Modifier.scale(.9f),
                                    )
                                },
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.fake_messages_placeholder),
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
                                        )
                                        .fillMaxWidth(),
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
                                                )
                                                .size(32.dp)
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

            val currentObjective =
                remember { content.getCurrentTimeLine()?.data?.currentObjective }

            currentObjective?.let {
                AnimatedVisibility(
                    objectiveExpanded,
                    enter = slideInVertically { +it },
                    exit = fadeOut(),
                ) {
                    val genre = content.data.genre
                    Column(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(vertical = 32.dp, horizontal = 16.dp)
                            .clip(genre.shape())
                            .fillMaxWidth()
                            .border(1.dp, genre.color.gradientFade(), genre.shape())
                            .background(MaterialTheme.colorScheme.background, genre.shape())
                            .clickable {
                                objectiveExpanded = false
                            }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            colorFilter = ColorFilter.tint(content.data.genre.color),
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .sharedElement(
                                        rememberSharedContentState(
                                            key = "current_objective_${content.data.id}",
                                        ),
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                    ),
                        )
                        Text(
                            stringResource(id = R.string.current_objective),
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    textAlign = TextAlign.Center,
                                ),
                            modifier = Modifier.alpha(.6f),
                        )
                        Text(
                            it,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    textAlign = TextAlign.Center,
                                ),
                        )

                        LinearProgressIndicator(
                            modifier =
                                Modifier
                                    .sharedElement(
                                        rememberSharedContentState(
                                            key = "lore_progress_${content.data.id}",
                                        ),
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                    )
                                    .clip(genre.shape())
                                    .height(4.dp)
                                    .fillMaxWidth()
                                    .reactiveShimmer(
                                        true,
                                        shimmerColors = genre.shimmerColors(),
                                    ),
                            gapSize = 0.dp,
                            progress = { progress },
                            drawStopIndicator = {},
                            color = genre.color,
                            trackColor = Color.Transparent,
                        )
                    }
                }
            }
        }
    }

    showReactions?.let {
        ReactionsBottomSheet(it, content) {
            showReactions = null
        }
    }
}

@Composable
private fun EmptyMessagesView(
    text: String = stringResource(id = R.string.start_writing_saga),
    brush: Brush,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        SparkIcon(
            modifier =
                Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally),
            description = stringResource(id = R.string.no_messages_cd),
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
    originalBitmap: Bitmap? = null,
    segmentedBitmap: Bitmap? = null,
) {
    Column(modifier) {
        AnimatedVisibility(saga.icon.isNotEmpty()) {
            Box(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .fillMaxHeight(.4f),
            ) {
                if (originalBitmap != null && segmentedBitmap != null) {
                    DepthLayout(
                        originalImage = originalBitmap,
                        segmentedImage = segmentedBitmap,
                        modifier = Modifier.fillMaxSize(),
                        imageModifier = Modifier
                            .effectForGenre(saga.genre)
                            .selectiveColorHighlight(saga.genre.selectiveHighlight())
                    ) {
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
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .gradientFill(saga.genre.gradient(true))
                                    .clickable {
                                        openSaga()
                                    },
                        )
                    }
                } else {
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
                                )
                                .fillMaxSize(),
                    )
                }

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
                    }
                    .animateContentSize(),
        )
    }
}

@Composable
fun ChatList(
    saga: SagaContent,
    actList: List<ActDisplayData>,
    modifier: Modifier,
    listState: LazyListState,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    objectiveExpanded: Boolean,
    openCharacter: (CharacterContent?) -> Unit = {},
    openSaga: () -> Unit = {},
    openWiki: () -> Unit = {},
    onRetryMessage: (Message) -> Unit = {},
    openReactions: (MessageContent) -> Unit = {},
    requestNewCharacter: (String) -> Unit = {},
    reviewEvent: (TimelineContent) -> Unit = {},
    reviewChapter: (ChapterContent) -> Unit = {},
    messageEffectsEnabled: Boolean = true,
    originalBitmap: Bitmap? = null,
    segmentedBitmap: Bitmap? = null,
    isSelectionMode: Boolean = false,
    selectedMessageIds: Set<Int> = emptySet(),
    onToggleSelectionMode: () -> Unit = {},
    onToggleMessageSelection: (Int) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(saga.messagesSize()) {
        coroutineScope.launch {
            delay(3.seconds)
            if ((listState.canScrollForward && listState.isScrollInProgress.not()) && objectiveExpanded.not()) {
                listState.animateScrollToItem(0)
            }
        }
    }

    LazyColumn(
        modifier,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        reverseLayout = true,
    ) {
        item {
            Spacer(Modifier.height(64.dp))
        }
        if (saga.data.isEnded && saga.data.endMessage.isNotEmpty()) {
            item {
                RecapHeroCard(
                    saga,
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    onClick = { openSaga() },
                    originalBitmap = originalBitmap,
                    segmentedBitmap = segmentedBitmap,
                )
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
                    stringResource(id = R.string.saga_ended_on, saga.data.endedAt.formatDate()),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = saga.data.genre.bodyFont(),
                        ),
                    modifier = Modifier.animateItem(),
                )
            }
        }
        actList.reversed().forEach { act ->
            val genre = saga.data.genre

            if (act.isComplete) {
                item(key = act.content.data.id) {
                    ActComponent(
                        act.content,
                        saga.acts.indexOf(act.content) + 1,
                        saga,
                        modifier =
                            Modifier
                                .animateItem(),
                    )
                }
            }

            act.chapters.reversed().forEach { chapter ->

                if (chapter.isComplete) {
                    item(key = chapter.chapter.data.id) {
                        ChapterContentView(
                            chapter.chapter,
                            saga,
                            isLast = act.chapters.lastOrNull() == chapter,
                            imageSize = 400.dp,
                            openCharacters = {
                                openSaga()
                            },
                            requestReview = reviewChapter,
                            modifier =
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .clickable {
                                        openSaga()
                                    },
                        )
                    }
                }

                chapter.timelineSummaries.reversed().forEach { timeline ->

                    if (timeline.isComplete()) {
                        item(key = timeline.data.id) {
                            var isExpanded by remember { mutableStateOf(false) }
                            Box(
                                Modifier
                                    .clip(genre.shape())
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                awaitRelease()
                                                isExpanded = false
                                            },
                                            onLongPress = {
                                                isExpanded = true
                                            },
                                        ) {
                                            openSaga()
                                        }
                                    },
                            ) {
                                SharedTransitionLayout {
                                    AnimatedContent(isExpanded) {
                                        if (it) {
                                            TimeLineCard(
                                                timeline,
                                                saga,
                                                isLast = true,
                                                modifier =
                                                    Modifier.sharedElement(
                                                        rememberSharedContentState(
                                                            key = "timeline_${timeline.data.id}",
                                                        ),
                                                        this,
                                                    ),
                                            )
                                        } else {
                                            TimeLineSimpleCard(
                                                timeline,
                                                saga,
                                                modifier =
                                                    Modifier
                                                        .animateItem()
                                                        .sharedElement(
                                                            rememberSharedContentState(
                                                                key = "timeline_${timeline.data.id}",
                                                            ),
                                                            this,
                                                        )
                                                        .padding(16.dp)
                                                        .clip(
                                                            genre.shape(),
                                                        )
                                                        .fillMaxWidth(),
                                                requestReview = reviewEvent,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(timeline.messages.reversed(), key = { it.message.id }) {
                        ChatBubble(
                            it,
                            isLoading = false,
                            content = saga,
                            canAnimate = timeline.messages.lastOrNull() == it,
                            messageEffectsEnabled = messageEffectsEnabled,
                            modifier =
                                Modifier.animateItem(
                                    fadeInSpec = tween(400, easing = EaseIn),
                                    fadeOutSpec = tween(400, easing = EaseIn),
                                ),
                            openCharacters = { char -> openCharacter(char) },
                            openWiki = { openWiki() },
                            onReactionsClick = { openReactions(it) },
                            onRetry = {
                                onRetryMessage(it.message)
                            },
                            requestNewCharacter = {
                                it.message.speakerName?.let {
                                    requestNewCharacter(it)
                                }
                            },
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedMessageIds.contains(it.message.id),
                            onToggleSelection = {
                                onToggleMessageSelection(it.message.id)
                            },
                            onLongPress = {
                                onToggleSelectionMode()
                                onToggleMessageSelection(it.message.id)
                            },
                        )
                    }

                    item(key = "${timeline.data.id}-spark") {
                        Box(
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                null,
                                colorFilter = ColorFilter.tint(genre.color),
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .padding(4.dp),
                            )
                        }
                    }
                }

                item(key = "${chapter.chapter.data.id}-intro") {
                    Text(
                        chapter.chapter.data.introduction,
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

                item(key = "${chapter.chapter.data.id}-title") {
                    val title =
                        chapter.chapter.data.title.ifEmpty {
                            stringResource(
                                id = R.string.chapter_title_template,
                                saga.chapterNumber(chapter.chapter.data).toRoman(),
                            )
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
                item(key = "${act.content.data.id}-intro") {
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

            item(key = "${act.content.data.id}-title") {
                val title =
                    act.content.data.title
                        .ifEmpty {
                            stringResource(
                                id = R.string.act_title_template,
                                saga.actNumber(act.content.data).toRoman(),
                            )
                        }
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
        item(key = "saga-${saga.data.id}-header") {
            SagaHeader(
                saga = saga.data,
                modifier =
                    Modifier
                        .fillMaxWidth(),
                openSaga = openSaga,
                originalBitmap = originalBitmap,
                segmentedBitmap = segmentedBitmap,
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
    val overlapAmount = (-12).dp
    val density = LocalDensity.current
    val charactersToDisplay =
        characters.take(3)
    LazyRow(
        Modifier
            .clip(RoundedCornerShape(genre.cornerSize()))
            .fillMaxWidth(.15f),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(
            charactersToDisplay,
            key = { index, character -> "character-$index-${character.id}-${character.name}" },
        ) { index, character ->
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
                        )
                        .graphicsLayer(
                            translationX = if (index > 0) (index * overlapAmountPx) else 0f,
                        )
                        .clip(CircleShape)
                        .size(24.dp)
                        .clickable { onCharacterSelected(character) },
            )
        }
    }
}
