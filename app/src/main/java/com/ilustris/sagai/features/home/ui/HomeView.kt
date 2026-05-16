@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.home.ui
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.file.backup.ui.BackupSheet
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.characters.ui.components.buildMessagePreviewAnnotatedString
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.home.ui.components.CreateSagaCard
import com.ilustris.sagai.features.home.ui.components.TrophyShelf
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.premium.PremiumCard
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.settings.ui.SettingsView
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.iridescentGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.themeShimmer
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeView(
    navToNewSaga: () -> Unit,
    navToSaga: (String, Boolean) -> Unit,
    navToProfile: () -> Unit,
    navToFAQ: () -> Unit,
    navToAuditLogs: () -> Unit,
    padding: PaddingValues = PaddingValues(0.dp),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val sagas by viewModel.sagas.collectAsStateWithLifecycle(emptyList())
    val showDebugButton by viewModel.showDebugButton.collectAsStateWithLifecycle()
    val startFakeSaga by viewModel.startDebugSaga.collectAsStateWithLifecycle()
    val dynamicNewSagaTexts by viewModel.dynamicNewSagaTexts.collectAsStateWithLifecycle()
    val isLoadingDynamicPrompts = dynamicNewSagaTexts == null
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    var showPremiumSheet by remember { mutableStateOf(false) }
    var showBackupSheet by remember { mutableStateOf(viewModel.showRecoverSheet.value) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isStarting by viewModel.isStarting.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val visualConfigs by viewModel.visualConfigs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkForBackups()
        viewModel.autoBackup()
    }

    BackHandler(enabled = drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch {
                drawerState.close()
            }
        }
    }

    with(sharedTransitionScope) {
        AnimatedContent(isStarting, transitionSpec = {
            fadeIn(tween(700)) togetherWith fadeOut(tween(400))
        }) {
            if (it) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painterResource(R.drawable.ic_spark),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier =
                            Modifier
                                .offset(y = 24.unaryMinus().dp)
                                .size(150.dp)
                                .sharedElement(
                                    rememberSharedContentState("spark_icon"),
                                    this@AnimatedContent,
                                ).reactiveShimmer(
                                    true,
                                    themeShimmer(),
                                    1.seconds,
                                    targetValue = 250f,
                                    repeatMode = RepeatMode.Restart,
                                ),
                    )
                }
            } else {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Box(
                        Modifier
                            .padding(padding)
                            .fillMaxSize(),
                    ) {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    ModalDrawerSheet(
                                        drawerContainerColor = MaterialTheme.colorScheme.background,
                                    ) {
                                        SettingsView(
                                            onBack = {
                                                coroutineScope.launch { drawerState.close() }
                                            },
                                            navToFAQ = navToFAQ,
                                            navToAuditLogs = navToAuditLogs,
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            onOpenPremiumOnboarding = {
                                                showPremiumSheet = true
                                                coroutineScope.launch {
                                                    drawerState.close()
                                                }
                                            },
                                        )
                                    }
                                }
                            },
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                val isPremium =
                                    billingState is BillingService.BillingState.SignatureEnabled

                                val visibleSagas =
                                    if (showDebugButton.not()) {
                                        sagas.filter { !it.data.isDebug }
                                    } else {
                                        sagas
                                    }

                                ChatList(
                                    sagas = visibleSagas,
                                    padding = padding,
                                    showDebugButton = showDebugButton,
                                    dynamicNewSagaTexts = dynamicNewSagaTexts,
                                    isLoadingDynamicPrompts = isLoadingDynamicPrompts,
                                    isPremium = isPremium,
                                    visualConfigs = visualConfigs,
                                    animatedContentScope = this@AnimatedContent,
                                    sharedTransitionScope = sharedTransitionScope,
                                    onCreateNewChat = {
                                        if (BuildConfig.DEBUG) {
                                            navToNewSaga()
                                            return@ChatList
                                        }
                                        val freeSagasCount =
                                            sagas.count { it.data.isEnded.not() }
                                        if (freeSagasCount <= 3 || isPremium) {
                                            navToNewSaga()
                                        } else {
                                            showPremiumSheet = true
                                        }
                                    },
                                    onSelectSaga = { sagaData ->
                                        navToSaga(sagaData.id.toString(), sagaData.isDebug)
                                    },
                                    createFakeSaga = {
                                        viewModel.createFakeSaga()
                                    },
                                    openPremiumSheet = {
                                        if (showPremiumSheet.not()) {
                                            showPremiumSheet = true
                                        }
                                    },
                                    recoverSagas = {
                                        showBackupSheet = true
                                    },
                                    openSettings = {
                                        coroutineScope.launch {
                                            drawerState.open()
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

    LaunchedEffect(startFakeSaga) {
        startFakeSaga?.let {
            navToSaga(it.id.toString(), true)
        }
    }

    if (showPremiumSheet && isLoading.not()) {
        OnboardingDialog(
            type = OnboardingType.PREMIUM_GUIDE,
            force = true,
            onDismiss = {
                showPremiumSheet = false
            },
        )
    }

    if (showBackupSheet) {
        BackupSheet(true, {
            showBackupSheet = false
        })
    }

    StarryLoader(
        isLoading,
        loadingMessage,
    )

    OnboardingDialog(type = OnboardingType.APP_INTRO)
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ChatList(
    sagas: List<SagaSummary>,
    padding: PaddingValues = PaddingValues(0.dp),
    showDebugButton: Boolean,
    dynamicNewSagaTexts: Pair<DynamicSagaPrompt?, GenreVisualConfig?>?,
    isLoadingDynamicPrompts: Boolean,
    isPremium: Boolean = false,
    backupAvailable: Boolean = false,
    visualConfigs: Map<Genre, GenreVisualConfig> = emptyMap(),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
    recoverSagas: () -> Unit = {},
    onCreateNewChat: () -> Unit = {},
    onSelectSaga: (Saga) -> Unit = {},
    createFakeSaga: () -> Unit = {},
    openPremiumSheet: () -> Unit = {},
    openSettings: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val activeSagas = remember(sagas) { sagas.filter { !it.data.isEnded } }
    val completedSagas = remember(sagas) { sagas.filter { it.data.isEnded } }

    LazyColumn(
        state = listState,
        modifier =
            modifier
                .animateContentSize()
                .padding(padding),
    ) {
        stickyHeader {
            with(sharedTransitionScope) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .statusBarsPadding(),
                ) {
                    Box(Modifier.size(24.dp))
                    AnimatedContent(
                        isPremium,
                        modifier =
                            Modifier
                                .align(Alignment.CenterVertically)
                                .weight(1f),
                    ) {
                        if (it) {
                            PremiumTitle(
                                modifier =
                                    Modifier
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) {
                                            openPremiumSheet()
                                        }.wrapContentWidth()
                                        .align(Alignment.CenterVertically),
                                iconModifier =
                                    Modifier.sharedElement(
                                        rememberSharedContentState("spark_icon"),
                                        animatedContentScope,
                                    ),
                                titleStyle =
                                    MaterialTheme.typography.titleLarge,
                                brush = Brush.linearGradient(holographicGradient),
                            )
                        } else {
                            SagaTitle(
                                iconModifier =
                                    Modifier.sharedElement(
                                        rememberSharedContentState("spark_icon"),
                                        animatedContentScope,
                                    ),
                            )
                        }
                    }

                    IconButton(onClick = {
                        openSettings()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        if (showDebugButton) {
            item {
                val debugBrush = Brush.verticalGradient(listOf(Color.DarkGray, Color.Gray))
                Row(
                    modifier =
                        Modifier
                            .clickable {
                                createFakeSaga()
                            }.padding(16.dp)
                            .gradientFill(debugBrush)
                            .clip(RoundedCornerShape(15.dp))
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_bug),
                        contentDescription = stringResource(R.string.home_debug_session_icon_desc),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            stringResource(R.string.home_start_debug_session_title),
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                ),
                        )

                        Text(
                            stringResource(R.string.home_test_with_fake_messages_subtitle),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Light,
                                    color = Color.White.copy(alpha = 0.8f),
                                ),
                        )
                    }
                }
            }
        }

        item {
            TrophyShelf(
                completedSagas = completedSagas,
                visualConfigs = visualConfigs,
                onCompletedSagaClicked = { onSelectSaga(it.data) },
            )
        }

        dynamicNewSagaTexts?.let {
            item {
                CreateSagaCard(
                    modifier = Modifier.padding(16.dp),
                    dynamicNewSagaTexts = dynamicNewSagaTexts,
                    onCreateNewChat = onCreateNewChat,
                )
            }
        }

        items(
            activeSagas,
            key = { it.data.id },
        ) {
            ChatCard(
                it,
                visualConfigs[it.data.genre],
                Modifier
                    .animateItem()
                    .clickable {
                        if (!it.data.isDebug || showDebugButton) {
                            onSelectSaga(it.data)
                        }
                    },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        }

        if (isLoadingDynamicPrompts.not()) {
            if (isPremium.not()) {
                item {
                    PremiumCard(
                        isPremium,
                        onClick = openPremiumSheet,
                        modifier =
                            Modifier
                                .animateItem()
                                .padding(16.dp),
                    )
                }
            }

            if (backupAvailable) {
                item {
                    Box(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Button(onClick = {
                            recoverSagas.invoke()
                        }, colors = ButtonDefaults.textButtonColors()) {
                            Icon(
                                painterResource(R.drawable.ic_restore),
                                null,
                                modifier =
                                    Modifier
                                        .padding(horizontal = 8.dp)
                                        .size(24.dp),
                            )
                            Text(
                                stringResource(id = R.string.restore_sagas),
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Light,
                                    ),
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    onCreateNewChat()
                },
                shape = MaterialTheme.shapes.large,
                colors =
                    ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Transparent,
                    ),
                modifier =
                    Modifier
                        .padding(32.dp)
                        .dropShadow(MaterialTheme.shapes.large) {
                            brush =
                                Brush.horizontalGradient(iridescentGradient)
                            radius = 10f
                            spread = 5f
                        }.background(
                            Brush.horizontalGradient(iridescentGradient),
                            MaterialTheme.shapes.large,
                        ).fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.home_create_new_saga_title).uppercase(),
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                        ),
                )
            }
        }

        item {
            Spacer(Modifier.size(24.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatCard(
    saga: SagaSummary,
    visualConfig: GenreVisualConfig? = null,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    SagAITheme(visualConfig = visualConfig, genre = saga.data.genre) {
        val sagaData = saga.data
        val genre = sagaData.genre
        val genreColor = genre.color
        val genreBrush = sagaBrush()
        Brush.sweepGradient(genre.colorPalette(visualConfig))
        with(sharedTransitionScope) {
            Column {
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .sharedElement(
                                    rememberSharedContentState(key = "saga_${saga.data.id}_icon"),
                                    animatedContentScope,
                                ).dropShadow(CircleShape) {
                                    radius = 5f
                                    color = genreColor
                                    brush = genreBrush
                                    spread = 5f
                                }.size(50.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AvatarTimelineIcon(
                            saga.data.icon,
                            saga.data.isEnded,
                            saga.data.genre,
                            saga.data.title
                                .first()
                                .uppercase(),
                            visualConfig = visualConfig,
                            borderWidth = 1.dp,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val color by animateColorAsState(
                        if (saga.data.isEnded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    )
                    Column(
                        modifier =
                            Modifier
                                .weight(1f),
                    ) {
                        Row {
                            Text(
                                text = sagaData.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                color = color,
                                modifier =
                                    Modifier
                                        .sharedElement(
                                            rememberSharedContentState(key = "saga_${saga.data.id}_title"),
                                            animatedContentScope,
                                        ).weight(1f),
                            )

                            val timeInMillis = saga.lastMessageTime
                            if (timeInMillis != null) {
                                val time =
                                    Calendar
                                        .getInstance()
                                        .apply { this.timeInMillis = timeInMillis }
                                val timeText =
                                    String.format(
                                        "%02d:%02d",
                                        time.get(Calendar.HOUR_OF_DAY),
                                        time.get(Calendar.MINUTE),
                                    )

                                Text(
                                    text = timeText,
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                        ),
                                    color = color.copy(alpha = .6f),
                                )
                            }
                        }

                        val message =
                            if (sagaData.isEnded) {
                                AnnotatedString(stringResource(R.string.chat_card_saga_ended))
                            } else {
                                if (saga.messagesCount == 0) {
                                    AnnotatedString(stringResource(R.string.chat_card_saga_begins))
                                } else {
                                    val isNarrator = saga.lastMessageSender == SenderType.NARRATOR
                                    val pair =
                                        (
                                            saga.lastMessageSpeaker
                                                ?: saga.lastMessageSender?.name
                                                ?: emptyString()
                                        ) to
                                            (saga.lastMessageText ?: emptyString())
                                    buildMessagePreviewAnnotatedString(
                                        pair.formatToString(
                                            !isNarrator,
                                        ),
                                    )
                                }
                            }
                        Text(
                            text =
                                message
                                    ?: AnnotatedString(stringResource(R.string.chat_card_saga_begins)),
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    textAlign = TextAlign.Start,
                                    color = color.copy(alpha = .6f),
                                ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .alpha(.8f),
                        )
                    }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    SagAITheme {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                },
                actions = {},
                navigationIcon = {
                    Box(modifier = Modifier.size(24.dp))
                },
            )
        }) { padding ->
            AnimatedContent(padding) {
                Box(modifier = Modifier.padding(it)) {
                    val previewChats =
                        List(10) {
                            SagaSummary(
                                data =
                                    Saga(
                                        title = "Chat ${it + 1}",
                                        description = "The journey of our lifes",
                                        genre = Genre.FANTASY,
                                        icon = "",
                                        isEnded = true,
                                        createdAt = Calendar.getInstance().timeInMillis,
                                        mainCharacterId = null,
                                    ),
                                lastMessageText = "Hello!",
                                lastMessageTime = System.currentTimeMillis(),
                                lastMessageSender = null,
                                lastMessageSpeaker = null,
                                messagesCount = 1,
                                chaptersCount = 2,
                            )
                        }
                    SharedTransitionLayout {
                        ChatList(
                            sagas = previewChats,
                            animatedContentScope = this@AnimatedContent,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            showDebugButton = true,
                            isPremium = true,
                            dynamicNewSagaTexts =
                                DynamicSagaPrompt(
                                    "Dynamic Title Preview",
                                    "Dynamic Subtitle Preview",
                                ) to null,
                            isLoadingDynamicPrompts = false,
                        )
                    }
                }
            }
        }
    }
}
