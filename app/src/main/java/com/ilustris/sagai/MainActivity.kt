@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.installations.FirebaseInstallations
import com.ilustris.sagai.core.data.SideEffect
import com.ilustris.sagai.core.network.ConnectivityObserver
import com.ilustris.sagai.core.network.ui.NoInternetScreen
import com.ilustris.sagai.core.services.SideEffectService
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.ui.components.BlurProvider
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.SagaNavGraph
import com.ilustris.sagai.ui.navigation.findRoute
import com.ilustris.sagai.ui.theme.SagAITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val deepLinkChannel = Channel<String>(Channel.CONFLATED)

    @Inject
    lateinit var sideEffectService: SideEffectService

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        printFirebaseInstallationAuthToken()
        enableEdgeToEdge()

        val initialDeepLinkString = intent?.getStringExtra("deepLink")
        intent?.removeExtra("deepLink")
        Timber.i("onCreate: deeplinkExtra: $initialDeepLinkString")
        setContent {
            SagAITheme {
                val connectivityObserver = remember { ConnectivityObserver(applicationContext) }
                val isOnline by connectivityObserver.observe().collectAsState(initial = true)

                val navController = rememberNavController()
                val currentEntry by navController
                    .currentBackStackEntryFlow
                    .collectAsState(initial = navController.currentBackStackEntry)
                val route =
                    remember(currentEntry) {
                        currentEntry?.destination?.route?.findRoute() ?: Routes.HOME
                    }

                val snackbarHostState = remember { SnackbarHostState() }
                var activeSideEffect by remember { mutableStateOf<SideEffect?>(null) }

                LaunchedEffect(Unit) {
                    sideEffectService.sideEffects.collect { effect ->
                        Timber.d("Received global side effect: $effect")
                        activeSideEffect = effect
                    }
                }

                LaunchedEffect(navController, initialDeepLinkString) {
                    if (initialDeepLinkString.isNullOrBlank()) {
                        return@LaunchedEffect
                    }
                    Timber.d("Handling initial deep link: $initialDeepLinkString")
                    try {
                        val deepLinkRoute = initialDeepLinkString.findRoute()
                        if (route != deepLinkRoute) {
                            navController.navigate(initialDeepLinkString)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error navigating with initial deep link: $initialDeepLinkString")
                    }
                }

                BlurProvider {
                    Scaffold(
                        modifier =
                            Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxSize(),
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState) {
                                Snackbar(
                                    it,
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(15.dp),
                                )
                            }
                        },
                        topBar = {
                            AnimatedContent(route, transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }) {
                                if (it.topBarContent != null) {
                                    it.topBarContent(navController)
                                } else {
                                    TopAppBar(
                                        title = {
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                route.title?.let {
                                                    Text(
                                                        text = stringResource(it),
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Medium,
                                                        textAlign = TextAlign.Center,
                                                        modifier =
                                                            Modifier
                                                                .padding(16.dp)
                                                                .fillMaxWidth(),
                                                    )
                                                } ?: run {
                                                    Image(
                                                        painterResource(R.drawable.ic_spark),
                                                        contentDescription = stringResource(R.string.app_name),
                                                        modifier =
                                                            Modifier
                                                                .align(Alignment.Center)
                                                                .size(24.dp)
                                                                .align(Alignment.Center),
                                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                                    )
                                                }
                                            }
                                        },
                                        actions = {},
                                        navigationIcon = {
                                            AnimatedVisibility(route != Routes.HOME) {
                                                IconButton(onClick = {
                                                    navController.popBackStack()
                                                }) {
                                                    Icon(
                                                        painterResource(R.drawable.ic_back_left),
                                                        contentDescription = "Back",
                                                        tint = MaterialTheme.colorScheme.onBackground,
                                                    )
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            // SagaBottomNavigation(navController, route)
                        },
                    ) { padding ->
                        AnimatedContent(isOnline, transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) {
                            if (it) {
                                SharedTransitionLayout {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        SagaNavGraph(
                                            navController,
                                            padding,
                                            this@SharedTransitionLayout,
                                            snackbarHostState,
                                        )
                                    }
                                }
                            } else {
                                NoInternetScreen()
                            }
                        }
                    }

                    if (activeSideEffect == SideEffect.ShowPremiumOnboarding) {
                        OnboardingDialog(
                            type = OnboardingType.PREMIUM_GUIDE,
                            force = true,
                            onDismiss = { activeSideEffect = null },
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

                                Text(
                                    text = stringResource(effect.status.titleRes),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                Text(
                                    text = stringResource(effect.status.messageRes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

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
