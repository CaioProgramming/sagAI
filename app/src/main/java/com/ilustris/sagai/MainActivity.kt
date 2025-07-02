@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
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
import androidx.navigation.compose.rememberNavController
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.SagaNavGraph
import com.ilustris.sagai.ui.navigation.findRoute
import com.ilustris.sagai.ui.theme.SagAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SagAITheme {
                val navController = rememberNavController()
                val currentEntry by navController
                    .currentBackStackEntryFlow
                    .collectAsState(initial = navController.currentBackStackEntry)
                val route =
                    remember(currentEntry) {
                        currentEntry?.destination?.route?.findRoute() ?: Routes.HOME
                    }
                val snackbarHostState = remember { SnackbarHostState() }
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
                                                modifier = Modifier.align(Alignment.Center).size(24.dp).align(Alignment.Center),
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
                                                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
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
                    SharedTransitionLayout {
                        Box(modifier = Modifier.fillMaxSize()) {
                            SagaNavGraph(navController, padding, this@SharedTransitionLayout, snackbarHostState)
                        }
                    }
                }
            }
        }
    }
}
