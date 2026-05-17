@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalSharedTransitionApi::class,
)

package com.ilustris.sagai.features.saga.chat.ui
import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.audio.ui.AudioRecordingSheet
import com.ilustris.sagai.core.file.BACKUP_PERMISSION
import com.ilustris.sagai.core.file.backup.ui.BackupSheet
import com.ilustris.sagai.core.permissions.PermissionComponent
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionService.Companion.openAppSettings
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.act.ui.ActComponent
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.chapter.ui.ChapterContentView
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.subtitleActAndChapterOrdinals
import com.ilustris.sagai.features.home.data.model.toInfo
import com.ilustris.sagai.features.milestone.ui.MilestoneOverlay
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveBackground
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.manager.BackgroundTask
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChatState
import com.ilustris.sagai.features.saga.chat.presentation.ChatUiAction
import com.ilustris.sagai.features.saga.chat.presentation.ChatUiState
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel
import com.ilustris.sagai.features.saga.chat.presentation.MessageAction
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView
import com.ilustris.sagai.features.saga.chat.ui.components.DeleteConfirmationDialog
import com.ilustris.sagai.features.saga.chat.ui.components.MessageOptionsSheet
import com.ilustris.sagai.features.saga.chat.ui.components.ReactionsBottomSheet
import com.ilustris.sagai.features.saga.chat.ui.components.audio.AudioPlaybackState
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.AdvanceTrigger
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.NarrativeBackgroundBanner
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.ObjectiveOverlay
import com.ilustris.sagai.features.saga.detail.ui.RecapHeroCard
import com.ilustris.sagai.features.saga.detail.ui.SagaWikiView
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.ui.TimelineContentViewCard
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaHighlight
import com.ilustris.sagai.ui.theme.themeShimmer
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ChatView(
    padding: PaddingValues = PaddingValues(0.dp),
    sagaId: String? = null,
    isDebug: Boolean = false,
    onBack: () -> Unit = {},
    onSagaDetails: () -> Unit = {},
    onCharacterDetails: (Int) -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalActivity.current
    val activity = LocalActivity.current
    var requiredPermission by remember { mutableStateOf<String?>(null) }
    val requestPermissionLauncher = PermissionService.rememberPermissionLauncher()
    val content = uiState.sagaContent
    val onAction: (ChatUiAction) -> Unit =
        remember(viewModel) {
            { action ->
                viewModel.handleAction(action)
                when (action) {
                    is ChatUiAction.Back -> onBack()
                    is ChatUiAction.OpenSagaDetails -> onSagaDetails()
                    is ChatUiAction.OpenCharacter -> onCharacterDetails(action.characterId)
                    else -> doNothing()
                }
            }
        }

    SagAITheme(genre = uiState.sagaContent?.data?.genre) {
        LaunchedEffect(uiState.sagaContent) {
            uiState.sagaContent?.let {
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

        BackHandler(enabled = !drawerState.isOpen) {
            onBack()
        }

        DisposableEffect(lifecycleOwner, viewModel) {
            lifecycleOwner.lifecycle.addObserver(viewModel)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(viewModel)
            }
        }

        if (content != null) {
            Box(Modifier.fillMaxSize()) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerShape = RoundedCornerShape(0.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.background,
                        ) {
                            SagaWikiView(
                                sagaId = content.data.id.toString(),
                                onBack = { coroutineScope.launch { drawerState.close() } },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                interceptSystemBack = drawerState.isOpen,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    with(sharedTransitionScope) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val chatState = uiState.chatState
                            uiState.milestone

                            when (chatState) {
                                is ChatState.Error -> {
                                    AnimatedVisibility(uiState.isGenerating.not()) {
                                        EmptyMessagesView(
                                            text = stringResource(id = R.string.saga_not_found),
                                            brush =
                                                gradientAnimation(
                                                    holographicGradient,
                                                ),
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    }
                                }

                                is ChatState.Success -> {
                                    ChatContent(
                                        uiState = uiState,
                                        onAction = onAction,
                                        padding = padding,
                                        isDebug = isDebug,
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    )
                                }

                                else -> {
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
                }
            }

            if (showBackupSheet) {
                BackupSheet(onDismiss = { showBackupSheet = false })
            }

            if (uiState.showAudioTranscript) {
                AudioRecordingSheet(
                    uiState.sagaContent
                        ?.data
                        ?.genre
                        ?.colorPalette() ?: holographicGradient,
                    onDismiss = {
                        onAction(ChatUiAction.RequestAudioTranscript(false))
                    },
                ) {
                    onAction(ChatUiAction.UpdateInput(TextFieldValue(it)))
                    onAction(ChatUiAction.SendInput(userConfirmed = false, isAudio = true))
                    onAction(ChatUiAction.RequestAudioTranscript(false))
                }
            }

            val backupLauncher =
                PermissionService.rememberBackupLauncher { uri ->
                    onAction(ChatUiAction.EnableBackup(uri))
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

            uiState.sagaContent?.let {
                AnimatedVisibility(
                    (uiState.isLoading || uiState.isGenerating),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    StarryTextPlaceholder(
                        modifier =
                            Modifier
                                .reactiveShimmer(
                                    true,
                                    themeShimmer(),
                                    duration = 2.seconds,
                                ).fillMaxSize(),
                    )
                }

                ShareSheet(
                    saga = it.data,
                    isVisible = uiState.showShareSheet,
                    shareType = ShareType.CONVERSATION,
                    onDismiss = { onAction(ChatUiAction.ShareConversation(false)) },
                )
            }

            content.let {
                uiState.onboardingType?.let {
                    OnboardingDialog(
                        saga = content.data,
                        type = it,
                        genre = content.data.genre,
                        onDismiss = {
                            viewModel.onOnboardingDismissed()
                        },
                    )
                }
            }
        } else {
            if (uiState.isGenerating.not() && uiState.isLoading.not()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        stringResource(R.string.saga_not_found),
                    )

                    Button(onClick = {
                        onBack()
                    }, colors = ButtonDefaults.textButtonColors()) {
                        Text(stringResource(R.string.back_button_description))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatContent(
    uiState: ChatUiState,
    onAction: (ChatUiAction) -> Unit,
    padding: PaddingValues = PaddingValues(),
    isDebug: Boolean = false,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val content = uiState.sagaContent ?: return
    val saga = remember(content) { content.data }
    val listState = rememberLazyListState()

    var showReactions by remember {
        mutableStateOf<MessageContent?>(null)
    }
    var showDeleteConfirmDialog by remember {
        mutableStateOf<Message?>(null)
    }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val progressState =
        animateFloatAsState(
            targetValue = if (content.data.isEnded.not()) uiState.loreUpdateProgress else 1f,
            label = "loreUpdateProgress",
        )

    val milestoneState = uiState.milestone
    val resolvedColor = MaterialTheme.colorScheme.primary
    val resolvedIconColor = MaterialTheme.colorScheme.onPrimary

    with(sharedTransitionScope) {
        AnimatedContent(
            milestoneState,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                fadeIn(tween(1000, easing = EaseIn)) togetherWith
                    fadeOut(
                        tween(
                            1500,
                            easing = FastOutLinearInEasing,
                        ),
                    )
            },
        ) {
            val displayMilestone = it != null && it !is SagaMilestone.CurrentObjective
            if (displayMilestone) {
                milestoneState?.let { milestone ->
                    MilestoneOverlay(
                        milestone,
                        saga = content,
                        isLoading = uiState.isGenerating || uiState.isLoading,
                        reasoningChunk = uiState.reasoningChunk,
                        onDismiss = {
                            onAction(ChatUiAction.ContinueMilestone)
                        },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            } else {
                Box(
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .imePadding(),
                ) {
                    Image(
                        painter =
                            rememberAsyncImagePainter(
                                model =
                                    ImageRequest
                                        .Builder(LocalContext.current)
                                        .data(saga.genre.resolveBackground())
                                        .crossfade(true)
                                        .build(),
                            ),
                        null,
                        colorFilter =
                            ColorFilter.tint(
                                MaterialTheme.colorScheme.surfaceContainer.darker(.2f),
                            ),
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .reactiveShimmer(
                                    uiState.isPlaying,
                                    shimmerColors = themeShimmer(),
                                    duration = 10.seconds,
                                    targetValue = 1000f,
                                )
                                .size(100.dp)
                                .alpha(.4f),
                    )

                    ConstraintLayout(
                        Modifier
                            .padding(
                                top = padding.calculateTopPadding(),
                            )
                            .fillMaxSize(),
                    ) {
                        rememberCoroutineScope()
                        val (debugControls, messages, chatInput, topBar, bottomGradient, objectiveOverlay) = createRefs()

                        val onSendMessage: (Boolean) -> Unit =
                            remember(onAction, uiState.editingMessage) {
                                { userConfirmed ->
                                    if (uiState.editingMessage != null) {
                                        onAction(ChatUiAction.SaveEdit)
                                    } else {
                                        onAction(
                                            ChatUiAction.SendInput(
                                                userConfirmed,
                                                false,
                                            ),
                                        )
                                    }
                                }
                            }
                        val onUpdateInput: (TextFieldValue) -> Unit =
                            remember(onAction) {
                                { value ->
                                    onAction(
                                        ChatUiAction.UpdateInput(
                                            value,
                                        ),
                                    )
                                }
                            }
                        val onUpdateSender: (SenderType) -> Unit =
                            remember(onAction) {
                                { type ->
                                    onAction(
                                        ChatUiAction.UpdateSenderType(
                                            type,
                                        ),
                                    )
                                }
                            }

                        val onSelectCharacter: (Character) -> Unit =
                            remember(onAction) {
                                { character ->
                                    onAction(
                                        ChatUiAction.UpdateCharacter(
                                            character,
                                        ),
                                    )
                                }
                            }

                        val onRequestAudio: () -> Unit =
                            remember(onAction) {
                                {
                                    onAction(
                                        ChatUiAction.RequestAudioTranscript(true),
                                    )
                                }
                            }

                        val onCancelEdit: () -> Unit =
                            remember(onAction) {
                                { onAction(ChatUiAction.CancelEdit) }
                            }

                        ChatList(
                            saga = content,
                            actList = uiState.messages,
                            mainCharacter = uiState.mainCharacter,
                            characters = uiState.characters,
                            wikis = uiState.wikis,
                            activeGenre = uiState.activeGenre,
                            flatEvents = uiState.flatEvents,
                            listState = listState,
                            reasoningChunk = uiState.reasoningChunk,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            modifier =
                                Modifier
                                    .constrainAs(messages) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                        width = Dimension.fillToConstraints
                                        height = Dimension.fillToConstraints
                                    },
                            onMessageAction =
                                remember(onAction, content) {
                                    { action ->
                                        when (action) {
                                            is MessageAction.PlayAudio -> {
                                                onAction(ChatUiAction.PlayOrPauseAudio(action.message))
                                            }

                                            is MessageAction.RetryMessage -> {
                                                onAction(ChatUiAction.RetryAiResponse(action.message.message))
                                            }

                                            is MessageAction.ClickCharacter -> {
                                                action.character?.let {
                                                    onAction(
                                                        ChatUiAction.OpenCharacter(
                                                            it.id,
                                                        ),
                                                    )
                                                }
                                            }

                                            is MessageAction.RegenerateAudio -> {
                                                onAction(ChatUiAction.RegenerateAudio(action.message))
                                            }

                                            is MessageAction.ClickReactions -> {
                                                showReactions = action.message
                                            }

                                            is MessageAction.RequestNewCharacter -> {
                                                onAction(
                                                    ChatUiAction.RequestNewCharacter(
                                                        action.name,
                                                        action.message,
                                                    ),
                                                )
                                            }

                                            is MessageAction.ToggleSelection -> {
                                                onAction(ChatUiAction.ToggleMessageSelection(action.messageId))
                                            }

                                            is MessageAction.LongPress -> {
                                                val message =
                                                    content
                                                        .flatMessages()
                                                        .find { it.message.id == action.messageId }
                                                        ?.message
                                                onAction(ChatUiAction.OpenMessageOptions(message))
                                            }
                                        }
                                    }
                                },
                            onAction = onAction,
                            messageEffectsEnabled = uiState.messageEffectsEnabled,
                            originalBitmap = uiState.originalBitmap,
                            segmentedBitmap = uiState.segmentedBitmap,
                            isSelectionMode = uiState.selectionState.isSelectionMode,
                            selectedMessageIds = uiState.selectionState.selectedMessageIds,
                            audioPlaybackState = uiState.audioPlaybackState,
                        )

                        Box(
                            Modifier
                                .constrainAs(bottomGradient) {
                                    bottom.linkTo(parent.bottom)
                                }
                                .fillMaxWidth()
                                .fillMaxHeight(.2f)
                                .background(fadeGradientBottom(resolvedColor)),
                        )

                        val narrativeState = uiState.narrativeUiState
                        val bottomInputState =
                            when {
                                narrativeState.showAdvanceTrigger &&
                                    !uiState.selectionState.isSelectionMode -> {
                                    BottomInputState.Advance(narrativeState.pendingAction!!)
                                }

                                narrativeState.showBackgroundBanner &&
                                    !uiState.selectionState.isSelectionMode -> {
                                    BottomInputState.Background(narrativeState.backgroundTask!!)
                                }

                                else -> {
                                    BottomInputState.Chat
                                }
                            }

                        AnimatedContent(
                            targetState = bottomInputState,
                            modifier =
                                Modifier
                                    .padding(bottom = padding.calculateBottomPadding())
                                    .constrainAs(chatInput) {
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                        width = Dimension.fillToConstraints
                                    }.animateContentSize(),
                            transitionSpec = {
                                slideInVertically { it } + fadeIn() togetherWith
                                    slideOutVertically { it } + fadeOut()
                            },
                        ) { inputState ->
                            when (inputState) {
                                is BottomInputState.Advance -> {
                                    AdvanceTrigger(
                                        action = inputState.action,
                                        genre = saga.genre,
                                        onAdvance = { onAction(ChatUiAction.AdvanceNarrative) },
                                        Modifier.fillMaxWidth(),
                                        uiState.isGenerating || uiState.isLoading,
                                    )
                                }

                                is BottomInputState.Background -> {
                                    NarrativeBackgroundBanner(
                                        task = inputState.task,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }

                                BottomInputState.Chat -> {
                                    AnimatedVisibility(
                                        uiState.chatState !is ChatState.Loading &&
                                            saga.isDebug.not() && saga.isEnded.not() &&
                                            !uiState.selectionState.isSelectionMode,
                                        enter = slideInVertically(),
                                        exit = slideOutVertically { it },
                                    ) {
                                        ChatInputView(
                                            content = content,
                                            characters = uiState.characters,
                                            isGenerating = uiState.isGenerating || uiState.isLoading,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(),
                                            selectedCharacter = uiState.selectedCharacter,
                                            typoFix = uiState.typoFixMessage,
                                            inputField = uiState.inputValue,
                                            sendType = uiState.senderType,
                                            isSendingPending = uiState.isSendingPending,
                                            sendingProgress = uiState.sendingProgress,
                                            onSendMessage = onSendMessage,
                                            onUpdateInput = onUpdateInput,
                                            onUpdateSender = onUpdateSender,
                                            suggestions = uiState.suggestions,
                                            onSelectCharacter = onSelectCharacter,
                                            onRequestAudio = onRequestAudio,
                                            isEditing = uiState.editingMessage != null,
                                            onCancelEdit = onCancelEdit,
                                            maxContentLength = uiState.maxContentLength,
                                            onStopGeneration = { onAction(ChatUiAction.StopGeneration) },
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = uiState.selectionState.isSelectionMode,
                            modifier =
                                Modifier
                                    .constrainAs(createRef()) {
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                        width = Dimension.fillToConstraints
                                    }.padding(
                                        bottom = padding.calculateBottomPadding() + 16.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                    ),
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(resolvedColor)
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = { onAction(ChatUiAction.ClearSelection) },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        painterResource(R.drawable.round_close_24),
                                        contentDescription = stringResource(R.string.cancel),
                                        tint = Color.White,
                                        modifier =
                                            Modifier
                                                .padding(8.dp)
                                                .fillMaxSize(),
                                    )
                                }

                                Text(
                                    stringResource(
                                        R.string.messages_selected,
                                        uiState.selectionState.selectedMessageIds.size,
                                        10,
                                    ),
                                    style =
                                        MaterialTheme.typography.labelLarge.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            textAlign = TextAlign.Center,
                                        ),
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f),
                                )

                                IconButton(
                                    onClick = {
                                        onAction(ChatUiAction.ShareConversation(true))
                                    },
                                    enabled = uiState.selectionState.selectedMessageIds.isNotEmpty(),
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_share),
                                        contentDescription = stringResource(R.string.share),
                                        modifier =
                                            Modifier
                                                .padding(8.dp)
                                                .fillMaxSize(),
                                        tint =
                                            if (uiState.selectionState.selectedMessageIds.isNotEmpty()) {
                                                Color.White
                                            } else {
                                                Color.White.copy(
                                                    alpha = 0.5f,
                                                )
                                            },
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
                                    .background(MaterialTheme.colorScheme.background)
                                    .fillMaxWidth()
                                    .animateContentSize(tween(200, easing = EaseIn))
                                    .constrainAs(topBar) {
                                        top.linkTo(parent.top)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                    },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                painterResource(saga.genre.icon),
                                contentDescription = null,
                                colorFilter =
                                    ColorFilter.tint(
                                        MaterialTheme.colorScheme.onBackground.copy(
                                            alpha = .5f,
                                        ),
                                    ),
                                modifier =
                                    Modifier
                                        .genreVfx(
                                            saga.genre,
                                            isPlaying = uiState.isGenerating || uiState.isLoading,
                                        )
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            onAction(ChatUiAction.ShowObjective)
                                        }
                                        .gradientFill(
                                            progressiveBrush(
                                                resolvedColor,
                                                progressState.value,
                                            ),
                                        )
                                        .reactiveShimmer(
                                            uiState.isGenerating || uiState.isLoading,
                                            shimmerColors = themeShimmer(),
                                        ),
                            )

                            val subtitle =
                                if (saga.isEnded) {
                                    stringResource(id = R.string.chat_card_saga_ended)
                                } else {
                                    val (actOrd, chapterOrd) = content.subtitleActAndChapterOrdinals()
                                    stringResource(
                                        R.string.chat_view_subtitle,
                                        actOrd.toRoman(),
                                        chapterOrd.toRoman(),
                                    )
                                }

                            val topCharacters = uiState.topCharacters

                            SagaTopBar(
                                saga.title,
                                subtitle,
                                saga.genre,
                                isLoading = uiState.isGenerating || uiState.isLoading,
                                onBackClick = {
                                    onAction(ChatUiAction.Back)
                                },
                                modifier =
                                    Modifier
                                        .clickable {
                                            onAction(
                                                ChatUiAction.OpenSagaDetails,
                                            )
                                        }
                                        .fillMaxWidth()
                                        .padding(start = 8.dp),
                                titleModifier =
                                    Modifier.graphicsLayer(alpha = alpha),
                                actionContent = {
                                    AnimatedContent(topCharacters, transitionSpec = {
                                        slideInVertically() + fadeIn() togetherWith fadeOut()
                                    }) { chars ->
                                        CharactersTopIcons(
                                            chars,
                                            saga.genre,
                                            isLoading = uiState.isGenerating || uiState.isLoading,
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        ) { _ ->
                                            onAction(
                                                ChatUiAction.OpenSagaDetails,
                                            )
                                        }
                                    }
                                },
                            )

                            LinearProgressIndicator(
                                modifier =
                                    Modifier
                                        .alpha(alpha)
                                        .height(1.dp)
                                        .fillMaxWidth(),
                                progress = { progressState.value },
                                drawStopIndicator = {},
                                gapSize = 0.dp,
                                color = resolvedColor,
                                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                            )
                        }

                        AnimatedVisibility(
                            uiState.milestone is SagaMilestone.CurrentObjective,
                            enter = fadeIn() + slideInVertically { +it },
                            exit = fadeOut() + slideOutVertically { it },
                            modifier =
                                Modifier.constrainAs(objectiveOverlay) {
                                    top.linkTo(parent.top)
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                },
                        ) {
                            (uiState.milestone as? SagaMilestone.CurrentObjective)?.let {
                                ObjectiveOverlay(
                                    stringResource(it.title),
                                    it.subtitle,
                                    genre = saga.genre,
                                    Modifier,
                                ) {
                                    onAction(ChatUiAction.DismissMilestone)
                                }
                            }
                        }

                        if (BuildConfig.DEBUG && isDebug && saga.isEnded.not() && !uiState.selectionState.isSelectionMode) {
                            var fakeMessageCountText by rememberSaveable { mutableStateOf("3") }
                            val shape = RoundedCornerShape(content.data.genre.cornerSize())
                            Box(
                                Modifier
                                    .padding(16.dp)
                                    .constrainAs(debugControls) {
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                        width = Dimension.fillToConstraints
                                        height = Dimension.wrapContent
                                    },
                            ) {
                                val textStyle =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
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
                                                val count =
                                                    fakeMessageCountText.toIntOrNull() ?: 0
                                                if (count > 0) {
                                                    onAction(
                                                        ChatUiAction.InjectFakeMessages(
                                                            count,
                                                        ),
                                                    )
                                                }
                                            },
                                            modifier =
                                                Modifier
                                                    .background(
                                                        resolvedColor,
                                                        CircleShape,
                                                    )
                                                    .size(32.dp)
                                                    .padding(4.dp),
                                        ) {
                                            Icon(
                                                painterResource(R.drawable.ic_inject),
                                                null,
                                                tint = resolvedIconColor,
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

            showReactions?.let {
                ReactionsBottomSheet(it, content) {
                    showReactions = null
                }
            }

            uiState.showMessageOptions?.let { message ->
                MessageOptionsSheet(
                    message = message,
                    genre = content.data.genre,
                    isLastMessage =
                        content
                            .flatMessages()
                            .lastOrNull()
                            ?.message
                            ?.id == message.id,
                    isSafeToEdit = !uiState.isLoading && !uiState.isGenerating && !content.data.isEnded,
                    onEdit = {
                        onAction(ChatUiAction.EditMessage(message))
                    },
                    onDelete = {
                        showDeleteConfirmDialog = message
                        onAction(ChatUiAction.OpenMessageOptions(null))
                    },
                    onCopy = {
                        clipboardManager.setText(
                            androidx.compose.ui.text
                                .AnnotatedString(message.text),
                        )
                        onAction(ChatUiAction.OpenMessageOptions(null))
                    },
                    onSelect = {
                        onAction(ChatUiAction.ToggleSelectionMode)
                        onAction(ChatUiAction.ToggleMessageSelection(message.id))
                        onAction(ChatUiAction.OpenMessageOptions(null))
                    },
                    onDismiss = {
                        onAction(ChatUiAction.OpenMessageOptions(null))
                    },
                )
            }

            showDeleteConfirmDialog?.let { message ->
                DeleteConfirmationDialog(
                    genre = content.data.genre,
                    onConfirm = {
                        onAction(ChatUiAction.DeleteMessage(message))
                        showDeleteConfirmDialog = null
                    },
                    onDismiss = {
                        showDeleteConfirmDialog = null
                    },
                )
            }
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
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    Column(
        modifier.clickable {
            openSaga()
        },
    ) {
        if (saga.icon.isEmpty()) {
            saga.genre.stylisedText(
                saga.title,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp),
            )
        }
        AnimatedVisibility(saga.icon.isNotEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp),
            ) {
                with(sharedTransitionScope) {
                    if (originalBitmap != null && segmentedBitmap != null) {
                        DepthLayout(
                            originalImage = originalBitmap,
                            segmentedImage = segmentedBitmap,
                            modifier = Modifier.fillMaxSize(),
                            imageModifier =
                                Modifier
                                    .effectForGenre(saga.genre)
                                    .sagaHighlight(),
                        ) {
                            Text(
                                saga.title,
                                style =
                                    MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                    ),
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .genreVfx(saga.genre)
                                        .clickable {
                                            openSaga()
                                        },
                            )
                        }
                    } else {
                        AsyncImage(
                            model =
                                ImageRequest
                                    .Builder(LocalContext.current)
                                    .data(saga.icon)
                                    .crossfade(true)
                                    .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .effectForGenre(saga.genre)
                                    .sagaHighlight()
                                    .fillMaxSize(),
                        )
                    }
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            fadedGradientTopAndBottom(),
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
                    .plus(stringResource(id = R.string.read_more))
            },
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    color = textColor,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                ),
            textAlign = TextAlign.Start,
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
    saga: SagaMetadata,
    actList: List<ActDisplayData>,
    mainCharacter: CharacterContent?,
    characters: List<Character>,
    wikis: List<Wiki>,
    activeGenre: Genre?,
    flatEvents: List<Timeline>,
    modifier: Modifier,
    listState: LazyListState,
    onMessageAction: (MessageAction) -> Unit = {},
    onAction: (ChatUiAction) -> Unit = {},
    messageEffectsEnabled: Boolean = true,
    originalBitmap: Bitmap? = null,
    segmentedBitmap: Bitmap? = null,
    isSelectionMode: Boolean = false,
    selectedMessageIds: Set<Int> = emptySet(),
    audioPlaybackState: AudioPlaybackState? = null,
    reasoningChunk: String? = null,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val coroutineScope = rememberCoroutineScope()
    val genre = saga.data.genre
    val resolvedColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(saga.flatMessages().size) {
        coroutineScope.launch {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        reverseLayout = true,
    ) {
        item {
            Spacer(Modifier.height(100.dp))
        }

        reasoningChunk?.let {
            item(key = "reasoning") {
                AnimatedContent(
                    reasoningChunk,
                    transitionSpec = {
                        fadeIn(tween(1200)) + slideInVertically { it } togetherWith
                            fadeOut(tween(1500)) + slideOutVertically { it }
                    },
                ) {
                    Text(
                        text = it,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                textAlign = TextAlign.Center,
                                shadow =
                                    Shadow(
                                        MaterialTheme.colorScheme.primary,
                                        blurRadius = 10f,
                                    ),
                            ),
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier
                                .levitate()
                                .padding(16.dp)
                                .fillMaxWidth()
                                .alpha(.6f),
                    )
                }
            }
        }

        if (saga.data.isEnded && saga.data.endMessage.isNotEmpty()) {
            item {
                RecapHeroCard(
                    saga.data,
                    saga.flatChapters().size,
                    saga.characters.size,
                    saga.flatMessages().size,
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(150.dp),
                    onClick = { onAction(ChatUiAction.OpenSagaDetails) },
                )
            }

            item {
                Text(
                    saga.data.endMessage,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Justify,
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                )
            }

            item {
                Text(
                    stringResource(id = R.string.saga_ended_on, saga.data.endedAt.formatDate()),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        ),
                    modifier = Modifier,
                )
            }
        }
        actList.forEach { act ->

            if (act.isComplete) {
                item(key = "act-${act.content.data.id}") {
                    ActComponent(
                        act.content,
                        saga.acts.indexOf(act.content) + 1,
                        saga,
                        modifier = Modifier,
                    )
                }
            }

            act.chapters.forEach { chapter ->

                if (chapter.isComplete) {
                    item(key = "chapter-${chapter.chapter.data.id}") {
                        ChapterContentView(
                            chapter = chapter.chapter.toInfo(saga.data.id),
                            isLast = act.chapters.lastOrNull() == chapter,
                            imageSize = 400.dp,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAction(ChatUiAction.OpenSagaDetails)
                                    },
                        )
                    }
                }

                chapter.timelineSummaries.forEach { timelineDisplay ->
                    val timeline = timelineDisplay.timeline
                    timeline.let {
                        if (it.canShowData) {
                            item(key = "timeline-${timeline.timelineContent.data.id}") {
                                TimelineContentViewCard(
                                    saga = saga.data,
                                    eventCard = it,
                                    modifier = Modifier.fillMaxWidth(),
                                    onAction = { },
                                )
                            }
                        }
                    }

                    items(timeline.timelineContent.messages, key = { "message-${it.message.id}" }) {
                        ChatBubble(
                            it,
                            isLoading = false,
                            mainCharacter = mainCharacter,
                            characters = characters,
                            wikis = wikis,
                            genre = activeGenre ?: genre,
                            flatEvents = flatEvents,
                            canAnimate = true,
                            messageEffectsEnabled = messageEffectsEnabled,
                            audioPlaybackState = audioPlaybackState,
                            modifier = Modifier,
                            onAction = onMessageAction,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedMessageIds.contains(it.message.id),
                        )
                    }

                    timeline.let {
                        item(key = "timeline-${it.timelineContent.data.id}-spark") {
                            Box(
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    painterResource(genre.icon),
                                    null,
                                    colorFilter = ColorFilter.tint(resolvedColor),
                                    modifier =
                                        Modifier
                                            .size(24.dp)
                                            .padding(4.dp),
                                )
                            }
                        }
                    }
                }

                item(key = "chapter-${chapter.chapter.data.id}-intro") {
                    Text(
                        chapter.chapter.data.introduction,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                textAlign = TextAlign.Justify,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    )
                }

                item(key = "chapter-${chapter.chapter.data.id}-title") {
                    val title =
                        chapter.chapter.data.title.ifEmpty {
                            stringResource(
                                id = R.string.chapter_title_template,
                                saga.chapterNumber(chapter.chapter.data.id).toRoman(),
                            )
                        }
                    Text(
                        title,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                brush = genre.gradient(),
                                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
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
                item(key = "act-${act.content.data.id}-intro") {
                    Text(
                        act.content.data.introduction,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                textAlign = TextAlign.Justify,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    )
                }
            }

            item(key = "act-${act.content.data.id}-title") {
                val title =
                    act.content.data.title
                        .ifEmpty {
                            stringResource(
                                id = R.string.act_title_template,
                                saga.actNumber(act.content.data.id).toRoman(),
                            )
                        }
                Text(
                    title,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            brush = genre.gradient(true),
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
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
                openSaga = { onAction(ChatUiAction.OpenSagaDetails) },
                originalBitmap = originalBitmap,
                segmentedBitmap = segmentedBitmap,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}

private sealed interface BottomInputState {
    data object Chat : BottomInputState

    data class Advance(
        val action: NarrativeAction,
    ) : BottomInputState

    data class Background(
        val task: BackgroundTask,
    ) : BottomInputState
}

@Composable
fun CharactersTopIcons(
    characters: List<Character>,
    genre: Genre,
    isLoading: Boolean = false,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    onCharacterSelected: (Character?) -> Unit = {},
) {
    val cornerSize = genre.cornerSize()
    val overlapAmount = (-12).dp
    val density = LocalDensity.current
    val charactersToDisplay =
        characters.take(3)
    LazyRow(
        Modifier
            .clip(RoundedCornerShape(cornerSize))
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
            with(sharedTransitionScope) {
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
}
