@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai

import android.content.Intent // Added
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect // Added
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.ilustris.sagai.core.network.ConnectivityObserver
import com.ilustris.sagai.core.network.ui.NoInternetScreen
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.SagaNavGraph
import com.ilustris.sagai.ui.navigation.findRoute
import com.ilustris.sagai.ui.theme.SagAITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val deepLinkChannel = Channel<String>(Channel.CONFLATED)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        printFirebaseInstallationAuthToken()
        enableEdgeToEdge()

        val initialDeepLinkString = intent?.getStringExtra("deepLink")
        intent?.removeExtra("deepLink")
        Log.i(javaClass.simpleName, "onCreate: deeplinkExtra: $initialDeepLinkString")
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

                LaunchedEffect(navController, initialDeepLinkString) {
                    if (initialDeepLinkString.isNullOrBlank()) {
                        return@LaunchedEffect
                    }
                    Log.d("MainActivity", "Handling initial deep link: $initialDeepLinkString")
                    try {
                        val deepLinkRoute = initialDeepLinkString.findRoute()
                        if (route != deepLinkRoute) {
                            navController.navigate(initialDeepLinkString)
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error navigating with initial deep link: $initialDeepLinkString", e)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) {
                        Snackbar(
                            it,
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(15.dp),
                        )
                    }
                }, topBar = {
                    AnimatedContent(route) {
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
                }, bottomBar = {
                    // SagaBottomNavigation(navController, route)
                }) { padding ->
                    AnimatedContent(isOnline, transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }) {
                        if (it) {
                            SharedTransitionLayout {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    SagaNavGraph(navController, padding, this@SharedTransitionLayout, snackbarHostState)
                                }
                            }
                        } else {
                            NoInternetScreen()
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called")
        intent?.getStringExtra("deepLink")?.let { deepLink ->
            if (deepLink.isNotBlank()) {
                Log.d("MainActivity", "Deep link found in onNewIntent: $deepLink")
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
                    Log.d("FirebaseInstallations", "COPY THIS TOKEN (FID Token) ->")
                    Log.d("FirebaseInstallations", "$token")
                    Log.d("FirebaseInstallations", "<- END OF TOKEN")
                } else {
                    Log.e("FirebaseInstallations", "Failed to get Installation Auth Token", task.exception)
                }
            }
    }
}
