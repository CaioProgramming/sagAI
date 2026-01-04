@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.home.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.file.backup.ui.BackupSheet
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.premium.PremiumCard
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.features.premium.PremiumView
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.settings.ui.SettingsView
import com.ilustris.sagai.features.stories.ui.StoriesRow
import com.ilustris.sagai.features.stories.ui.StorySheet
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeView(
    navController: NavHostController,
    padding: PaddingValues,
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
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val selectedSaga by viewModel.selectedSaga.collectAsStateWithLifecycle()
    val storyBriefing by viewModel.storyBriefing.collectAsStateWithLifecycle()
    val loadingStoryId by viewModel.loadingStoryId.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkForBackups()
    }

    BackHandler(enabled = drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch {
                drawerState.close()
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(Modifier.fillMaxSize()) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ModalDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.background,
                        ) { SettingsView(navController) }
                    }
                },
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    val isPremium = billingState is BillingService.BillingState.SignatureEnabled
                    ChatList(
                        sagas = if (showDebugButton.not()) sagas.filter { !it.data.isDebug } else sagas,
                        padding = padding,
                        showDebugButton = showDebugButton,
                        dynamicNewSagaTexts = dynamicNewSagaTexts,
                        isLoadingDynamicPrompts = isLoadingDynamicPrompts,
                        isPremium = isPremium,
                        loadingStoryId = loadingStoryId,
                        onCreateNewChat = {
                            if (BuildConfig.DEBUG) {
                                navController.navigateToRoute(Routes.NEW_SAGA)
                                return@ChatList
                            }
                            val freeSagasCount = sagas.count { it.data.isEnded.not() }
                            if (freeSagasCount <= 3 || isPremium) {
                                navController.navigateToRoute(Routes.NEW_SAGA)
                            } else {
                                showPremiumSheet = true
                            }
                        },
                        onSelectSaga = { sagaData ->
                            navController.navigateToRoute(
                                Routes.CHAT,
                                mapOf(
                                    "sagaId" to sagaData.id.toString(),
                                    "isDebug" to sagaData.isDebug.toString(),
                                ),
                            )
                        },
                        createFakeSaga = {
                            viewModel.createFakeSaga()
                        },
                        openPremiumSheet = {
                            showPremiumSheet = true
                        },
                        recoverSagas = {
                            showBackupSheet = true
                        },
                        openSettings = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                        onStoryClicked = {
                            viewModel.getBriefing(it)
                        },
                    )
                }
            }
        }
    }
    LaunchedEffect(startFakeSaga) {
        startFakeSaga?.let {
            navController.navigateToRoute(
                Routes.CHAT,
                mapOf(
                    "sagaId" to it.id.toString(),
                    "isDebug" to "true",
                ),
            )
        }
    }

    PremiumView(
        isVisible = showPremiumSheet,
        onDismiss = {
            showPremiumSheet = false
        },
    )

    if (showBackupSheet) {
        BackupSheet(true, {
            showBackupSheet = false
        })
    }

    val showBriefing = storyBriefing != null
    if (showBriefing) {
        StorySheet(storyBriefing, onDismiss = {
            viewModel.clearSelectedSaga()
        }, onContinue = {
            navController.navigateToRoute(
                Routes.CHAT,
                mapOf(
                    "sagaId" to selectedSaga!!.data.id.toString(),
                    "isDebug" to selectedSaga!!.data.isDebug.toString(),
                ),
            )
            viewModel.clearSelectedSaga()
        })
    }

    StarryLoader(
        isLoading,
        loadingMessage,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ChatList(
    sagas: List<SagaContent>,
    padding: PaddingValues = PaddingValues(0.dp),
    showDebugButton: Boolean,
    dynamicNewSagaTexts: DynamicSagaPrompt?,
    isLoadingDynamicPrompts: Boolean,
    isPremium: Boolean = false,
    backupAvailable: Boolean = false,
    loadingStoryId: Int? = null,
    recoverSagas: () -> Unit = {},
    onCreateNewChat: () -> Unit = {},
    onSelectSaga: (Saga) -> Unit = {},
    onStoryClicked: (SagaContent) -> Unit = {},
    createFakeSaga: () -> Unit = {},
    openPremiumSheet: () -> Unit = {},
    openSettings: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier =
            Modifier
                .animateContentSize()
                .padding(padding),
    ) {
        stickyHeader {
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
                            titleStyle =
                                MaterialTheme.typography.titleLarge,
                            brush = Brush.linearGradient(holographicGradient),
                        )
                    } else {
                        SagaTitle()
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
            val shimmerColors =
                remember {
                    Genre.entries.random().colorPalette()
                }
            Row(
                modifier =
                    Modifier
                        .animateItem()
                        .padding(16.dp)
                        .gradientFill(Brush.linearGradient(shimmerColors))
                        .reactiveShimmer(
                            true,
                            shimmerColors = shimmerColors,
                            duration = 10.seconds,
                        ).clip(RoundedCornerShape(15.dp))
                        .clickable {
                            onCreateNewChat()
                        }.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SparkLoader(
                    brush = MaterialTheme.colorScheme.onBackground.solidGradient(),
                    strokeSize = 1.dp,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .padding(4.dp)
                            .size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedContent(
                    targetState = isLoadingDynamicPrompts,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut(),
                        ) using
                            SizeTransform(
                                clip = false,
                            )
                    },
                    label = "AnimatedDynamicTexts",
                    modifier = Modifier.weight(1f),
                ) { isLoading ->
                    Column(
                        modifier =
                            Modifier
                                .weight(1f),
                    ) {
                        if (isLoading) {
                            StarryTextPlaceholder(
                                starCount = 100,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(MaterialTheme.typography.bodyMedium.lineHeight.value.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StarryTextPlaceholder(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(MaterialTheme.typography.labelSmall.lineHeight.value.dp),
                            )
                        } else {
                            Text(
                                text =
                                    dynamicNewSagaTexts?.title
                                        ?: stringResource(R.string.home_create_new_saga_title),
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                            )

                            Text(
                                text =
                                    dynamicNewSagaTexts?.subtitle
                                        ?: stringResource(R.string.home_create_new_saga_subtitle),
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Light,
                                    ),
                            )
                        }
                    }
                }
            }
        }

        item {
            StoriesRow(
                sagas = sagas,
                loadingStoryId = loadingStoryId,
                onStoryClicked = onStoryClicked,
                listState.canScrollBackward.not(),
            )
        }

        items(
            sagas,
        ) {
            ChatCard(
                it,
                Modifier
                    .animateItem()
                    .clickable {
                        if (!it.data.isDebug || showDebugButton) {
                            onSelectSaga(it.data)
                        }
                    },
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
    }
}

@Composable
fun ChatCard(
    saga: SagaContent,
    modifier: Modifier = Modifier,
) {
    val sagaData = saga.data
    Column {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarTimelineIcon(
                saga.data.icon,
                saga.data.isEnded,
                saga.data.genre,
                saga.data.title
                    .first()
                    .uppercase(),
                borderWidth = 2.dp,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            val lastMessage = saga.flatMessages().lastOrNull()
            val color by animateColorAsState(
                if (saga.data.isEnded) sagaData.genre.color else MaterialTheme.colorScheme.onBackground,
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
                        fontFamily = saga.data.genre.headerFont(),
                        color = color,
                        modifier = Modifier.weight(1f),
                    )

                    lastMessage?.let {
                        val time =
                            Calendar.getInstance().apply { timeInMillis = it.message.timestamp }
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
                                    fontFamily = saga.data.genre.bodyFont(),
                                ),
                            color = color.copy(alpha = .6f),
                        )
                    }
                }

                val message =
                    if (sagaData.isEnded) {
                        stringResource(R.string.chat_card_saga_ended)
                    } else {
                        if (saga.messagesSize() == 0) {
                            stringResource(R.string.chat_card_saga_begins)
                        } else {
                            lastMessage
                                ?.joinMessage()
                                ?.formatToString(lastMessage.message.senderType != SenderType.NARRATOR)
                        }
                    }
                Text(
                    text = message ?: emptyString(),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontFamily = saga.data.genre.bodyFont(),
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

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    SagAITheme {
        val route = Routes.HOME
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                    route.title?.let {
                        Text(
                            text = stringResource(it),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        )
                    } ?: run {
                        Box(Modifier.fillMaxWidth()) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                contentDescription = stringResource(R.string.app_name),
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .size(50.dp),
                            )
                        }
                    }
                },
                actions = {},
                navigationIcon = {
                    Box(modifier = Modifier.size(24.dp))
                },
            )
        }) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                val previewChats =
                    List(10) {
                        SagaContent(
                            Saga(
                                title = "Chat ${it + 1}",
                                description = "The journey of our lifes",
                                genre = Genre.FANTASY,
                                icon = "",
                                isEnded = true,
                                createdAt = Calendar.getInstance().timeInMillis,
                                mainCharacterId = null,
                            ),
                            mainCharacter = null,
                            acts = emptyList(),
                        )
                    }
                ChatList(
                    sagas = previewChats,
                    showDebugButton = true,
                    isPremium = true,
                    dynamicNewSagaTexts =
                        DynamicSagaPrompt(
                            "Dynamic Title Preview",
                            "Dynamic Subtitle Preview",
                        ),
                    isLoadingDynamicPrompts = false,
                )
            }
        }
    }
}
