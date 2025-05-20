@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.SagaBottomNavigation
import com.ilustris.sagai.ui.navigation.SagaNavGraph
import com.ilustris.sagai.ui.theme.SagAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SagAITheme {
                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry)
                val route =
                    remember(currentEntry) {
                        currentEntry?.destination?.route?.let { Routes.valueOf(it) }
                            ?: Routes.HOME
                    }
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    TopAppBar(
                        title = {
                            route.title?.let {
                                Text(
                                    text = stringResource(it),
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                )
                            }?: run {
                                Image(
                                    painterResource(R.drawable.ic_spark),
                                    contentDescription = stringResource(R.string.app_name),
                                    modifier = Modifier.size(24.dp)
                                )
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
                }, bottomBar = {
                    SagaBottomNavigation(navController, bottomPadding = 50.dp)
                }) { _ ->
                    SagaNavGraph(navController)
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SagAITheme {
        Greeting("Android")
    }
}
