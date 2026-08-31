@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.google.android.gms.ads.MobileAds
import com.google.firebase.installations.FirebaseInstallations
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.data.SideEffect
import com.ilustris.sagai.core.globalshell.BookGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.ChatGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.core.globalshell.ImageGenerationWorkEffect
import com.ilustris.sagai.core.media.SagaPlaybackService
import com.ilustris.sagai.core.navigation.SagaNavigationTracker
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.core.ai.key.ApiKeyState
import com.ilustris.sagai.ui.components.ApiKeyTroubleSheet
import com.ilustris.sagai.ui.components.FeatureNeedsBillingSheet
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.features.onboarding.ui.apikey.ApiKeyOnboarding
import com.ilustris.sagai.core.network.ConnectivityObserver
import com.ilustris.sagai.core.network.ui.NoInternetScreen
import com.ilustris.sagai.core.services.AdsConsentService
import com.ilustris.sagai.core.services.AdsService
import com.ilustris.sagai.core.services.SideEffectService
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.features.act.BookGenerationService
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.imagegeneration.ImageGenerationService
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.saga.chat.data.usecase.ChatGenerationService
import com.ilustris.sagai.ui.components.BlurProvider
import com.ilustris.sagai.ui.components.BlurTarget
import com.ilustris.sagai.ui.components.SagaSnackBar
import com.ilustris.sagai.ui.components.globalshell.GlobalShellHost
import com.ilustris.sagai.ui.components.island.BookGenerationIslandContent
import com.ilustris.sagai.ui.components.island.ChatGenerationIslandContent
import com.ilustris.sagai.ui.components.island.ChatIslandService
import com.ilustris.sagai.ui.components.island.CompactIslandHeight
import com.ilustris.sagai.ui.components.island.DynamicBottomComponent
import com.ilustris.sagai.ui.components.island.DynamicIslandOverlay
import com.ilustris.sagai.ui.components.island.ImageGenerationIslandContent
import com.ilustris.sagai.ui.components.island.IslandContent
import com.ilustris.sagai.ui.components.island.IslandInsets
import com.ilustris.sagai.ui.components.island.LocalIslandInsets
import com.ilustris.sagai.ui.components.island.NotificationIslandContent
import com.ilustris.sagai.ui.components.island.islandPadding
import com.ilustris.sagai.ui.navigation.AuditLogsKey
import com.ilustris.sagai.ui.navigation.ChatKey
import com.ilustris.sagai.ui.navigation.FAQKey
import com.ilustris.sagai.ui.navigation.HomeKey
import com.ilustris.sagai.ui.navigation.MilestoneKey
import com.ilustris.sagai.ui.navigation.Navigator
import com.ilustris.sagai.ui.navigation.NewSagaKey
import com.ilustris.sagai.ui.navigation.PlaythroughKey
import com.ilustris.sagai.ui.navigation.SettingsKey
import com.ilustris.sagai.ui.navigation.createSagaEntryProvider
import com.ilustris.sagai.ui.navigation.findNavKey
import com.ilustris.sagai.ui.navigation.isSameDestinationAs
import com.ilustris.sagai.ui.navigation.rememberNavigationState
import com.ilustris.sagai.ui.navigation.toEntries
import com.ilustris.sagai.ui.theme.SAGA_THEME_TRANSITION_MS
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.sagaShape
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * What the app is allowed to show right now.
 *
 * A missing API key blanks the shell exactly like a missing connection — both mean nothing can be
 * generated, and a half-usable app is worse than an honest wall. [Resolving] covers the frame or
 * two before the encrypted store answers: showing either branch on a guess would flash the setup
 * screen at users who already have a key.
 *
 * Deliberately excludes a rejected key and a spent quota. Those keep the app up — the user still
 * has sagas to read — and are surfaced in place instead.
 */
private enum class AppGate {
    Resolving,
    Offline,
    NeedsApiKey,
    Ready,
}

@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val deepLinkChannel = Channel<String>(Channel.CONFLATED)

    @Inject
    lateinit var sideEffectService: SideEffectService

    @Inject
    lateinit var userApiKeyStore: UserApiKeyStore

    // Injected eagerly (rather than only used where needed) so AdsService's
    // ActivityLifecycleCallbacks registers before this Activity's own onResume fires.
    @Inject
    lateinit var adsService: AdsService

    @Inject
    lateinit var adsConsentService: AdsConsentService

    @Inject
    lateinit var imageGenerationService: ImageGenerationService

    @Inject
    lateinit var bookGenerationService: BookGenerationService

    @Inject
    lateinit var chatGenerationService: ChatGenerationService

    @Inject
    lateinit var debugImageFallbackService: DebugImageFallbackService

    @Inject
    lateinit var sagaThemeManager: SagaThemeManager

    @Inject
    lateinit var sagaNavigationTracker: SagaNavigationTracker

    @Inject
    lateinit var sagaContentManager: SagaContentManager

    @Inject
    lateinit var globalShellService: GlobalShellService

    @Inject
    lateinit var chatIslandService: ChatIslandService

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        printFirebaseInstallationAuthToken()
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val initialDeepLinkString = intent?.getStringExtra("deepLink")
        intent?.removeExtra("deepLink")
        Timber.i("onCreate: deeplinkExtra: $initialDeepLinkString")

        // UMP consent must resolve before any ad request; MobileAds.initialize() only after,
        // per Google's documented ordering.
        lifecycleScope.launch {
            adsConsentService.requestConsentIfNeeded(this@MainActivity)
            if (adsConsentService.canRequestAds()) {
                MobileAds.initialize(this@MainActivity)
            }
        }
        setContent {
            Timber.d("MainActivity: setContent")
            val connectivityObserver = remember { ConnectivityObserver(applicationContext) }
            val isOnline by connectivityObserver.observe().collectAsState(initial = true)
            // Null until the store answers: rendering the setup screen during that gap would
            // flash it at every user who already has a key.
            val apiKeyState by userApiKeyStore
                .observeState()
                .collectAsState(initial = null)
            val appGate =
                when {
                    apiKeyState == null -> AppGate.Resolving
                    !isOnline -> AppGate.Offline
                    apiKeyState is ApiKeyState.Missing -> AppGate.NeedsApiKey
                    else -> AppGate.Ready
                }

            val navigationState =
                rememberNavigationState(
                    startRoute = HomeKey,
                    topLevelRoutes =
                        setOf(
                            HomeKey,
                            SettingsKey,
                            FAQKey,
                            NewSagaKey,
                            AuditLogsKey,
                        ),
                )
            val navigator = remember { Navigator(navigationState) }
            val currentKey =
                navigationState.stacksInUse
                    .lastOrNull()
                    ?.let { navigationState.backStacks[it]?.lastOrNull() } ?: HomeKey

            val isNeutralScreen =
                currentKey is HomeKey ||
                    currentKey is SettingsKey ||
                    currentKey is FAQKey ||
                    currentKey is NewSagaKey ||
                    currentKey is AuditLogsKey ||
                    currentKey is PlaythroughKey

            val currentGenre by sagaThemeManager.currentGenre.collectAsState(initial = null)
            val themeGenre = if (isNeutralScreen) null else currentGenre

            LaunchedEffect(isNeutralScreen) {
                sagaThemeManager.setNeutral(isNeutralScreen)
            }

            LaunchedEffect(currentKey) {
                sagaNavigationTracker.update(currentKey)
            }

            // The single place that turns "a narrative chain step is ready" into navigation.
            // Only opens the Milestone screen while the user is already on that saga's chat —
            // otherwise the existing GlobalShellEffect notification (posted from
            // SagaContentManagerImpl.postMilestoneEffect) covers it passively. No other screen
            // should react to this signal.
            //
            // Also waits out any in-flight chat reply for that saga first: hitting the message
            // limit is usually the very message still being replied to, so without this the
            // Milestone screen could open on a bare loading state while that reply is still
            // streaming in the background. This gating can't live in SagaContentManagerImpl
            // itself — ChatGenerationService transitively depends back on SagaContentManager (via
            // MessageUseCase), so injecting it there is a Dagger dependency cycle; MainActivity
            // can see both without one.
            LaunchedEffect(Unit) {
                sagaContentManager.milestoneChainReady.collect { sagaId ->
                    if (!sagaNavigationTracker.isOnChatForSaga(sagaId)) return@collect
                    chatGenerationService.activeGenerations.first { it[sagaId] == null }
                    if (sagaNavigationTracker.isOnChatForSaga(sagaId)) {
                        navigator.navigate(MilestoneKey(sagaId))
                    }
                }
            }

            SagAITheme(genre = themeGenre) {
                Timber.d("MainActivity: SagAITheme block")

                var activeSideEffect by remember { mutableStateOf<SideEffect?>(null) }
                val globalSnackBar by sagaThemeManager.snackBarMessage.collectAsState()
                val globalShellState by globalShellService.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    sideEffectService.sideEffects.collect { effect ->
                        Timber.d("Received global side effect: $effect")
                        activeSideEffect = effect
                    }
                }

                // Pause ambient when activity is no longer visible; restart on onStart (foreground-safe).
                val lifecycleOwner = LocalLifecycleOwner.current
                val ambientMusicFile by sagaThemeManager.ambientMusicFile.collectAsState()
                var hasAmbientTrack by remember { mutableStateOf(false) }
                DisposableEffect(lifecycleOwner, hasAmbientTrack, ambientMusicFile) {
                    val observer =
                        object : DefaultLifecycleObserver {
                            override fun onStop(owner: LifecycleOwner) {
                                if (!hasAmbientTrack) return
                                SagaPlaybackService.startSafely(
                                    applicationContext,
                                    SagaPlaybackService.playbackIntent(
                                        applicationContext,
                                        SagaPlaybackService.ACTION_PAUSE,
                                    ),
                                )
                            }

                            override fun onStart(owner: LifecycleOwner) {
                                if (!hasAmbientTrack) return
                                val path =
                                    ambientMusicFile
                                        ?.takeIf { it.exists() }
                                        ?.absolutePath
                                        ?: return
                                SagaPlaybackService.startSafely(
                                    applicationContext,
                                    SagaPlaybackService.playbackIntent(
                                        applicationContext,
                                        SagaPlaybackService.ACTION_START,
                                        path,
                                    ),
                                )
                            }
                        }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // Global Ambient Music Control
                LaunchedEffect(ambientMusicFile) {
                    hasAmbientTrack = ambientMusicFile != null && ambientMusicFile?.exists() == true
                    val file = ambientMusicFile
                    if (file != null && file.exists()) {
                        Timber.d("Global music update: Playing ${file.absolutePath}")
                        SagaPlaybackService.startSafely(
                            applicationContext,
                            SagaPlaybackService.playbackIntent(
                                applicationContext,
                                SagaPlaybackService.ACTION_START,
                                file.absolutePath,
                            ),
                        )
                    } else {
                        Timber.d("Global music update: Stopping playback")
                        SagaPlaybackService.startSafely(
                            applicationContext,
                            SagaPlaybackService.playbackIntent(
                                applicationContext,
                                SagaPlaybackService.ACTION_STOP,
                            ),
                        )
                    }
                }

                fun resolveCurrentKey(): NavKey =
                    navigationState.stacksInUse
                        .lastOrNull()
                        ?.let { navigationState.backStacks[it]?.lastOrNull() }
                        ?: HomeKey

                fun navigateDeepLink(deepLink: String) {
                    if (deepLink.isBlank()) return
                    Timber.d("Handling deep link: $deepLink")
                    try {
                        val key = deepLink.findNavKey() ?: return
                        val activeKey = resolveCurrentKey()
                        if (!key.isSameDestinationAs(activeKey)) {
                            navigator.navigate(key)
                        } else {
                            Timber.d("Deep link ignored — already on $activeKey")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error navigating with deep link: $deepLink")
                    }
                }

                LaunchedEffect(navigator, initialDeepLinkString) {
                    initialDeepLinkString?.let { navigateDeepLink(it) }
                }

                LaunchedEffect(navigator) {
                    deepLinkChannel.receiveAsFlow().collect { deepLink ->
                        navigateDeepLink(deepLink)
                    }
                }

                LaunchedEffect(navigator) {
                    chatIslandService.navigationRequests.collect { deepLink ->
                        navigateDeepLink(deepLink)
                    }
                }

                val imageGenState by imageGenerationService.uiState.collectAsState()
                val bookGenState by bookGenerationService.uiState.collectAsState()
                val chatGenState by chatGenerationService.activeGenerations.collectAsState()

                // Shell v2: top DynamicIslandOverlay owns image + book + chat generation.
                // Priority mirrors the legacy host: image > book > chat.
                val imageGenActive =
                    imageGenState is ImageGenerationUiState.Generating ||
                        imageGenState is ImageGenerationUiState.AwaitingManualFallback ||
                        imageGenState is ImageGenerationUiState.Reveal
                val visibleBookGen =
                    (bookGenState as? BookGenerationUiState.Generating)
                        ?.takeUnless { sagaNavigationTracker.isOnChronicle(it.sagaId) }
                val visibleChatGen =
                    chatGenState.values.firstOrNull { !sagaNavigationTracker.isOnChatForSaga(it.sagaId) }
                // Real notifications only — the generation "work effects" arbitrate priority in
                // GlobalShellService but render as their own islands (above), never here.
                val notificationEffect =
                    globalShellState.effect?.takeUnless {
                        it is ImageGenerationWorkEffect ||
                            it is BookGenerationWorkEffect ||
                            it is ChatGenerationWorkEffect
                    }
                // Top island contributed by the active chat (e.g. current objective) — lowest priority.
                val chatTopIsland by chatIslandService.top.collectAsState()
                val islandContent: IslandContent? =
                    when {
                        imageGenActive -> {
                            ImageGenerationIslandContent(
                                state = imageGenState,
                                debugImageFallbackService = debugImageFallbackService,
                                onCancel = imageGenerationService::cancelCurrent,
                                onDismissReveal = imageGenerationService::dismissReveal,
                            )
                        }

                        visibleBookGen != null -> {
                            BookGenerationIslandContent(visibleBookGen)
                        }

                        visibleChatGen != null -> {
                            ChatGenerationIslandContent(visibleChatGen)
                        }

                        notificationEffect != null -> {
                            NotificationIslandContent(
                                effect = notificationEffect,
                                onNavigate = { deepLink -> navigateDeepLink(deepLink) },
                                onDismiss = { globalShellService.dismiss() },
                            )
                        }

                        chatTopIsland != null -> {
                            chatTopIsland
                        }

                        else -> {
                            null
                        }
                    }
                // Stable per-source key: reset expansion only when the island's *source* changes,
                // so a persistent island (e.g. objective) reappears collapsed after a transient one
                // (generation/notification) clears — instead of inheriting its expanded state.
                val islandKey: String? =
                    when {
                        imageGenActive -> "image"
                        visibleBookGen != null -> "book"
                        visibleChatGen != null -> "chat:${visibleChatGen.sagaId}"
                        notificationEffect != null -> "notif:${notificationEffect.id}"
                        chatTopIsland != null -> "objective"
                        else -> null
                    }
                val islandInsets = remember { IslandInsets() }

                var islandExpanded by remember { mutableStateOf(false) }
                LaunchedEffect(islandKey) { islandExpanded = false }
                // Padding reserved for content is driven purely by presence — not by the
                // island's measured/expanded size — so it never re-animates while expanding.
                LaunchedEffect(islandContent != null) {
                    islandInsets.top = if (islandContent != null) CompactIslandHeight else 0.dp
                }
                // Reveal content (e.g. a milestone) that wants to present itself without a tap.
                LaunchedEffect(islandContent?.autoExpandAfterMs) {
                    val delayMs = islandContent?.autoExpandAfterMs ?: return@LaunchedEffect
                    delay(delayMs)
                    islandExpanded = true
                }
                // Content that shouldn't wait indefinitely for a tap to clear itself (e.g. an
                // introduction recap) — fires the same action a tap on it would.
                LaunchedEffect(islandContent?.autoDismissAfterMs) {
                    val delayMs = islandContent?.autoDismissAfterMs ?: return@LaunchedEffect
                    delay(delayMs)
                    islandContent?.onAction?.invoke()
                }

                // Shell v2 bottom island — contributed by the active chat (narrative advance).
                val bottomIsland by chatIslandService.bottom.collectAsState()
                var bottomExpanded by remember { mutableStateOf(false) }
                LaunchedEffect(bottomIsland != null) {
                    if (bottomIsland == null) bottomExpanded = false
                    islandInsets.bottom = if (bottomIsland != null) CompactIslandHeight else 0.dp
                }
                LaunchedEffect(bottomIsland?.autoExpandAfterMs) {
                    val delayMs = bottomIsland?.autoExpandAfterMs ?: return@LaunchedEffect
                    delay(delayMs)
                    bottomExpanded = true
                }
                // Image reveal / manual fallback are terminal, attention-worthy states — auto-expand.
                LaunchedEffect(imageGenState) {
                    if (imageGenState is ImageGenerationUiState.Reveal ||
                        imageGenState is ImageGenerationUiState.AwaitingManualFallback
                    ) {
                        islandExpanded = true
                    }
                }

                CompositionLocalProvider(LocalIslandInsets provides islandInsets) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BlurProvider {
                            Scaffold(
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .fillMaxSize(),
                                bottomBar = {
                                    // SagaBottomNavigation(navController, route)
                                },
                            ) { padding ->
                                AnimatedContent(appGate, transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                }) { gate ->
                                    if (gate == AppGate.Ready) {
                                        SharedTransitionLayout {
                                            val entryProvider =
                                                remember(
                                                    navigator,
                                                    padding,
                                                    this@SharedTransitionLayout,
                                                ) {
                                                    createSagaEntryProvider(
                                                        navigator,
                                                        padding,
                                                        this@SharedTransitionLayout,
                                                    )
                                                }
                                            GlobalShellHost(
                                                globalState = globalShellState,
                                                imageGenState = imageGenState,
                                                bookGenState = bookGenState,
                                                chatGenState = chatGenState,
                                                sagaNavigationTracker = sagaNavigationTracker,
                                                debugImageFallbackService = debugImageFallbackService,
                                                onImageSetExpansion = { expansion ->
                                                    imageGenerationService.setIslandExpansion(expansion)
                                                },
                                                onImageCancel = imageGenerationService::cancelCurrent,
                                                onImageDismissReveal = imageGenerationService::dismissReveal,
                                                onNavigate = { deepLink ->
                                                    navigateDeepLink(deepLink)
                                                },
                                                onDismiss = { globalShellService.dismiss() },
                                                onSetGlobalExpansion = { expansion ->
                                                    globalShellService.setExpansion(expansion)
                                                },
                                                modifier = Modifier.fillMaxSize(),
                                                content = {
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        // Chat already vacates its input area for the bottom
                                                        // island (advance trigger/objective), so it doesn't
                                                        // need the reserved bottom padding other screens do.
                                                        BlurTarget(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxSize()
                                                                    .islandPadding(bottom = currentKey !is ChatKey),
                                                        ) {
                                                            NavDisplay(
                                                                entries =
                                                                    navigationState.toEntries(
                                                                        entryProvider,
                                                                    ),
                                                                onBack = { navigator.goBack() },
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
                                                                                easing = FastOutSlowInEasing,
                                                                            ),
                                                                        )
                                                                },
                                                                popTransitionSpec = {
                                                                    slideInVertically(
                                                                        tween(
                                                                            SAGA_THEME_TRANSITION_MS,
                                                                            easing = FastOutSlowInEasing,
                                                                        ),
                                                                    ) { it / 4 } +
                                                                        fadeIn(
                                                                            tween(
                                                                                SAGA_THEME_TRANSITION_MS,
                                                                                easing = EaseIn,
                                                                            ),
                                                                        ) togetherWith
                                                                        slideOutVertically(
                                                                            tween(
                                                                                SAGA_THEME_TRANSITION_MS,
                                                                                easing = FastOutSlowInEasing,
                                                                            ),
                                                                        ) { it / 4 } +
                                                                        fadeOut(
                                                                            tween(
                                                                                SAGA_THEME_TRANSITION_MS,
                                                                                easing = EaseIn,
                                                                            ),
                                                                        )
                                                                },
                                                                predictivePopTransitionSpec = {
                                                                    slideInVertically(
                                                                        tween(
                                                                            SAGA_THEME_TRANSITION_MS,
                                                                            easing = FastOutSlowInEasing,
                                                                        ),
                                                                    ) { it / 4 } +
                                                                        fadeIn(
                                                                            tween(
                                                                                SAGA_THEME_TRANSITION_MS,
                                                                                easing = FastOutSlowInEasing,
                                                                            ),
                                                                        ) togetherWith
                                                                        slideOutVertically(
                                                                            tween(
                                                                                SAGA_THEME_TRANSITION_MS,
                                                                                easing = FastOutSlowInEasing,
                                                                            ),
                                                                        ) { it / 4 } +
                                                                        fadeOut(
                                                                            tween(
                                                                                SAGA_THEME_TRANSITION_MS,
                                                                                easing = FastOutSlowInEasing,
                                                                            ),
                                                                        )
                                                                },
                                                            )
                                                        }

                                                        SagaSnackBar(
                                                            snackBarMessage = globalSnackBar,
                                                            genre = currentGenre,
                                                            modifier =
                                                                Modifier
                                                                    .align(Alignment.BottomCenter)
                                                                    .navigationBarsPadding()
                                                                    .padding(
                                                                        horizontal = 16.dp,
                                                                        vertical = 16.dp,
                                                                    ).fillMaxWidth()
                                                                    .clip(sagaShape()),
                                                            onDismiss = { sagaThemeManager.dismissSnackBar() },
                                                        )
                                                    }
                                                },
                                            )
                                        }
                                    } else if (gate == AppGate.Offline) {
                                        NoInternetScreen()
                                    } else if (gate == AppGate.NeedsApiKey) {
                                        ApiKeyOnboarding()
                                    }
                                }
                            }
                        }

                        // Shell v2 bottom island — floats above the nav bar, no scrim/blur.
                        // Declared before the top island so it paints underneath: an expanded top
                        // island should cover the bottom pill, not have it poke through.
                        DynamicBottomComponent(
                            content = bottomIsland,
                            expanded = bottomExpanded,
                            onExpandedChange = { bottomExpanded = it },
                        )

                        // Shell v2 top island — floats above nav content, no scrim/blur.
                        DynamicIslandOverlay(
                            content = islandContent,
                            expanded = islandExpanded,
                            onExpandedChange = { expanded ->
                                // Collapsing a terminal image reveal clears the underlying state
                                // (it must not linger and re-expand); other states just toggle.
                                if (!expanded && imageGenState is ImageGenerationUiState.Reveal) {
                                    imageGenerationService.dismissReveal()
                                }
                                islandExpanded = expanded
                            },
                        )

                        if (activeSideEffect == SideEffect.ShowPremiumOnboarding) {
                            OnboardingDialog(
                                type = OnboardingType.PREMIUM_GUIDE,
                                force = true,
                                onDismiss = { activeSideEffect = null },
                                genre = currentGenre,
                            )
                        }

                        if (activeSideEffect is SideEffect.GuardrailBlock) {
                            val effect = activeSideEffect as SideEffect.GuardrailBlock
                            val sheetState = rememberModalBottomSheetState()
                            ModalBottomSheet(
                                onDismissRequest = { activeSideEffect = null },
                                sheetState = sheetState,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                dragHandle = {
                                    BottomSheetDefaults.DragHandle(
                                        color =
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.4f,
                                            ),
                                    )
                                },
                            ) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp)
                                            .padding(bottom = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        painter = painterResource(effect.status.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = effect.status.color(MaterialTheme.colorScheme),
                                    )

                                    Spacer(modifier = Modifier.size(16.dp))

                                    effect.status.titleRes?.let {
                                        Text(
                                            text = stringResource(it),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center,
                                        )
                                    }

                                    Spacer(modifier = Modifier.size(8.dp))

                                    effect.status.messageRes?.let {
                                        Text(
                                            text = stringResource(it),
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }

                                    Spacer(modifier = Modifier.size(32.dp))

                                    Button(
                                        onClick = { activeSideEffect = null },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                    ) {
                                        Text(stringResource(R.string.guardrail_dismiss))
                                    }
                                }
                            }
                        }

                        if (activeSideEffect is SideEffect.FeatureNeedsBilling) {
                            FeatureNeedsBillingSheet(
                                onDismiss = { activeSideEffect = null },
                            )
                        }

                        // Key trouble is state, not an event: a rejected key or a spent daily
                        // quota is still true after a backgrounded app comes back, so these read
                        // from the store rather than from SideEffectService's replay-less flow,
                        // which would drop the notice if it fired while the app was away.
                        ApiKeyTroubleSheet(
                            apiKeyState = apiKeyState,
                            onOpenSettings = { navigator.navigate(SettingsKey) },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent called")
        intent?.getStringExtra("deepLink")?.let { deepLink ->
            if (deepLink.isNotBlank()) {
                Timber.d("Deep link found in onNewIntent: $deepLink")
                lifecycleScope.launch {
                    deepLinkChannel.send(deepLink)
                }
                intent.removeExtra("deepLink")
            }
        }
    }

    private fun printFirebaseInstallationAuthToken() {
        FirebaseInstallations
            .getInstance()
            .id
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Timber.tag("FirebaseInstallations").d("COPY THIS TOKEN (FID Token) ->")
                    Timber.tag("FirebaseInstallations").d("$token")
                    Timber.tag("FirebaseInstallations").d("<- END OF TOKEN")
                } else {
                    Timber.tag("FirebaseInstallations").e(task.exception, "Failed to get Installation Auth Token")
                }
            }
    }
}
