@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.file.backup.ui.BackupSheet
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.characters.ui.components.buildMessagePreviewAnnotatedString
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.home.ui.components.CreateSagaCard
import com.ilustris.sagai.features.home.ui.components.HomeSplashLoader
import com.ilustris.sagai.features.home.ui.components.TrophyShelf
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.premium.PremiumCard
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.SAGA_THEME_TRANSITION_MS
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.iridescentGradient
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.themeBrushColors
import java.util.Calendar

/** Theme and ambient audio are owned by [com.ilustris.sagai.MainActivity] + [com.ilustris.sagai.core.theme.SagaThemeManager], not this screen. */
@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeView(
    navToNewSaga: () -> Unit,
    navToSaga: (String, Boolean) -> Unit,
    navToSettings: () -> Unit,
    padding: PaddingValues = PaddingValues(0.dp),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                HomeNavigationEvent.NewSaga -> navToNewSaga()
                is HomeNavigationEvent.Saga -> navToSaga(event.sagaId, event.isDebug)
            }
        }
    }

    with(sharedTransitionScope) {
        AnimatedContent(
            targetState = uiState.screen,
            transitionSpec = {
                fadeIn(
                    tween(
                        SAGA_THEME_TRANSITION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                ) togetherWith
                    fadeOut(
                        tween(
                            SAGA_THEME_TRANSITION_MS,
                            delayMillis = SAGA_THEME_TRANSITION_MS / 2,
                            easing = LinearOutSlowInEasing,
                        ),
                    )
            },
            contentKey = { it },
            label = "home_screen",
        ) { screen ->
            when (screen) {
                HomeScreen.Splash -> {
                    HomeSplashLoader(
                        sharedTransitionScope = this@with,
                        animatedContentScope = this@AnimatedContent,
                    )
                }

                HomeScreen.Content -> {
                    HomeContent(
                        state = uiState,
                        onAction = viewModel::handleAction,
                        padding = padding,
                        sharedTransitionScope = sharedTransitionScope,
                        splashAnimatedContentScope = this@AnimatedContent,
                        navAnimatedVisibilityScope = animatedVisibilityScope,
                        openSettings = navToSettings,
                        modifier =
                            Modifier
                                .padding(padding)
                                .fillMaxSize(),
                    )
                }
            }
        }
    }

    if (uiState.showPremiumOnboarding && uiState.isLoading.not()) {
        OnboardingDialog(
            type = OnboardingType.PREMIUM_GUIDE,
            force = true,
            onDismiss = {
                viewModel.handleAction(HomeUiAction.DismissPremiumOnboarding)
            },
        )
    }

    if (uiState.showBackupSheet) {
        BackupSheet(true, {
            viewModel.handleAction(HomeUiAction.DismissBackupSheet)
        })
    }

    StarryLoader(
        uiState.isLoading,
        uiState.loadingMessage,
    )

    OnboardingDialog(type = OnboardingType.APP_INTRO)
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    padding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope,
    splashAnimatedContentScope: AnimatedContentScope,
    navAnimatedVisibilityScope: AnimatedContentScope,
    openSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChatList(
        state = state,
        onAction = onAction,
        padding = padding,
        sharedTransitionScope = sharedTransitionScope,
        splashAnimatedContentScope = splashAnimatedContentScope,
        navAnimatedVisibilityScope = navAnimatedVisibilityScope,
        openSettings = openSettings,
        modifier = modifier,
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ChatList(
    state: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    padding: PaddingValues = PaddingValues(0.dp),
    sharedTransitionScope: SharedTransitionScope,
    splashAnimatedContentScope: AnimatedContentScope,
    navAnimatedVisibilityScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
    openSettings: () -> Unit = {},
) {
    val listState = rememberLazyListState()

    with(sharedTransitionScope) {
        LazyColumn(
            state = listState,
            modifier =
                modifier
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
                            .background(MaterialTheme.colorScheme.background),
                ) {
                    Box(Modifier.size(24.dp))
                    AnimatedContent(
                        state.isPremium,
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
                                            onAction(HomeUiAction.OpenPremium)
                                        }.wrapContentWidth()
                                        .align(Alignment.CenterVertically),
                                iconModifier =
                                    Modifier.sharedElement(
                                        rememberSharedContentState("spark_icon"),
                                        splashAnimatedContentScope,
                                    ),
                                titleStyle =
                                    MaterialTheme.typography.titleLarge,
                                brush = Brush.linearGradient(themeBrushColors()),
                            )
                        } else {
                            SagaTitle(
                                iconModifier =
                                    Modifier.sharedElement(
                                        rememberSharedContentState("spark_icon"),
                                        splashAnimatedContentScope,
                                    ),
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            openSettings()
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            if (state.showDebugButton) {
                item {
                    val debugBrush = Brush.verticalGradient(listOf(Color.DarkGray, Color.Gray))
                    Row(
                        modifier =
                            Modifier
                                .clickable {
                                    onAction(HomeUiAction.CreateFakeSaga)
                                }
                                .padding(16.dp)
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
                    completedSagas = state.completedSagas,
                    onCompletedSagaClicked = { onAction(HomeUiAction.SelectSaga(it.data)) },
                )
            }

            item {
                AnimatedContent(state.dynamicNewSagaTexts, transitionSpec = {
                    fadeIn(tween(700)) togetherWith fadeOut(tween(400))
                }) {
                    it?.let { dynamicContent ->
                        CreateSagaCard(
                            dynamicNewSagaTexts = dynamicContent,
                            onCreateNewChat = { onAction(HomeUiAction.CreateNewSaga) },
                        )
                    }
                }
            }

            items(
                state.activeSagas,
                key = { saga -> saga.data.id },
            ) { saga ->
                ChatCard(
                    saga,
                    Modifier
                        .animateItem()
                        .clickable {
                            onAction(HomeUiAction.SelectSaga(saga.data))
                        },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = navAnimatedVisibilityScope,
                )
            }

            if (state.isLoadingDynamicPrompts.not()) {
                if (state.isPremium.not()) {
                    item {
                        PremiumCard(
                            state.isPremium,
                            onClick = { onAction(HomeUiAction.OpenPremium) },
                            modifier =
                                Modifier
                                    .animateItem()
                                    .padding(16.dp),
                        )
                    }
                }

                if (state.backupAvailable) {
                    item {
                        Box(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Button(onClick = {
                                onAction(HomeUiAction.RecoverSagas)
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
                        onAction(HomeUiAction.CreateNewSaga)
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
                                radius = 20f
                                spread = .4f
                            }
                            .background(
                                MaterialTheme.colorScheme.onBackground,
                                MaterialTheme.shapes.large,
                            )
                            .fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.home_create_new_saga_title).uppercase(),
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.background,
                            ),
                    )
                }
            }

            item {
                Spacer(Modifier.size(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatCard(
    saga: SagaSummary,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    SagAITheme(genre = saga.data.genre) {
        val sagaData = saga.data
        val genre = sagaData.genre
        val visualConfig = LocalGenreVisualConfig.current
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
                                .dropShadow(CircleShape) {
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
                            borderWidth = 1.dp,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .sharedBounds(
                                        rememberSharedContentState(key = "saga_${saga.data.id}_icon"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    ),
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
                                            animatedVisibilityScope,
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
                                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
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
                            state =
                                HomeUiState(
                                    screen = HomeScreen.Content,
                                    visibleSagas = previewChats,
                                    activeSagas = emptyList(),
                                    completedSagas = previewChats,
                                    showDebugButton = true,
                                    isPremium = true,
                                    dynamicNewSagaTexts =
                                        DynamicSagaPrompt(
                                            "Dynamic Title Preview",
                                            "Dynamic Subtitle Preview",
                                        ),
                                ),
                            onAction = {},
                            splashAnimatedContentScope = this@AnimatedContent,
                            navAnimatedVisibilityScope = this@AnimatedContent,
                            sharedTransitionScope = this@SharedTransitionLayout,
                        )
                    }
                }
            }
        }
    }
}
