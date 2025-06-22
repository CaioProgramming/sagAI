@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.ui.navigation

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.ui.CharacterDetailsView
import com.ilustris.sagai.features.characters.ui.CharacterGalleryView
import com.ilustris.sagai.features.home.ui.HomeView
import com.ilustris.sagai.features.newsaga.ui.NewSagaView
import com.ilustris.sagai.features.saga.chat.ui.ChatView
import com.ilustris.sagai.features.saga.detail.ui.SagaDetailView
import com.ilustris.sagai.features.timeline.ui.TimelineView

@OptIn(ExperimentalSharedTransitionApi::class)
enum class Routes(
    val view: @Composable (
        NavHostController,
        PaddingValues,
        SharedTransitionScope,
        SnackbarHostState,
    ) -> Unit = { nav, padding, transitionScope, snackState ->
        Text("Sample View for Route ", modifier = Modifier.padding(16.dp))
    },
    val topBarContent: (@Composable (NavHostController) -> Unit)? = null,
    val showBottomNav: Boolean = true,
    @DrawableRes val icon: Int? = null,
    @StringRes val title: Int? = null,
    val arguments: List<String> = emptyList(),
    val deepLink: String? = null,
) {
    HOME(icon = R.drawable.ic_spark, view = { nav, padding, _, _ ->
        HomeView(nav, padding)
    }, title = R.string.home_title, topBarContent = {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 16.dp, end = 16.dp, top = 50.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_spark),
                null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.home_title),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                    ),
            )
        }
    }),
    CHAT(
        view = { nav, padding, _, snack ->
            val arguments = nav.currentBackStackEntry?.arguments
            ChatView(
                navHostController = nav,
                padding,
                snack,
                sagaId = arguments?.getString(CHAT.arguments.first()),
            )
        },
        topBarContent = { Box {} },
        arguments = listOf("sagaId"),
        deepLink = "saga://chat/{sagaId}",
        showBottomNav = false,
    ),
    PROFILE,
    SETTINGS,
    NEW_SAGA(title = R.string.new_saga_title, showBottomNav = false, view = { nav, padding, _, _ ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            NewSagaView(nav)
        }
    }),
    CHARACTER_GALLERY(
        // Added Character Gallery Route
        view = { nav, padding, transitionScope, _ ->
            val arguments = nav.currentBackStackEntry?.arguments
            CharacterGalleryView(
                navController = nav,
                sagaId = arguments?.getString(CHARACTER_GALLERY.arguments.first()) ?: "",
            )
        },
        topBarContent = {
            Box {}
        },
        title = R.string.character_gallery_title, // Example title, ensure this exists
        arguments = listOf("sagaId"),
        deepLink = "saga://character_gallery/{sagaId}",
        showBottomNav = false, // Or true, depending on your desired UX
    ),
    SAGA_DETAIL(
        view = { nav, padding, _, _ ->
            val arguments = nav.currentBackStackEntry?.arguments
            SagaDetailView(
                navHostController = nav,
                paddingValues = padding,
                sagaId = arguments?.getString(SAGA_DETAIL.arguments.first()) ?: "",
            )
        },
        topBarContent = { Box {} },
        arguments = listOf("sagaId"),
        deepLink = "saga://saga_detail/{sagaId}",
        showBottomNav = false,
    ),
    TIMELINE(
        view = { nav, padding, _, _ ->
            val arguments = nav.currentBackStackEntry?.arguments
            TimelineView(
                sagaId = arguments?.getString(TIMELINE.arguments.first()) ?: "",
                navHostController = nav,
            )
        },
        topBarContent = { Box {} },
        arguments = listOf("sagaId"),
        deepLink = "saga://timeline/{sagaId}",
        showBottomNav = false,
    ),
    CHARACTER_DETAIL(
        arguments =
            listOf(
                "characterId",
                "sagaId",
            ),
        deepLink = "saga://character_detail/{characterId}/{sagaId}",
        showBottomNav = false,
        topBarContent = { Box {} },
        view = { nav, padding, _, _ ->
            CharacterDetailsView(
                navHostController = nav,
                characterId = nav.currentBackStackEntry?.arguments?.getString(CHARACTER_DETAIL.arguments.first()),
                sagaId = nav.currentBackStackEntry?.arguments?.getString(CHARACTER_DETAIL.arguments[1]),
            )
        },
    ),
}

@Composable
fun SagaBottomNavigation(
    navController: NavHostController,
    currentRoute: Routes?,
) {
    AnimatedVisibility(
        currentRoute?.showBottomNav == true,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
    ) {
        NavigationBar {
            Routes.entries.filter { it.icon != null }.forEach { route ->
                val isSelected = currentRoute == route
                val iconColor by animateColorAsState(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                )
                NavigationBarItem(
                    selected = isSelected,
                    label = {
                        Text(stringResource(route.title ?: R.string.app_name))
                    },
                    colors =
                        NavigationBarItemDefaults.colors().copy(
                            unselectedIconColor = Color.Transparent,
                            selectedIconColor = Color.Transparent,
                            selectedIndicatorColor = Color.Transparent,
                            selectedTextColor = iconColor,
                        ),
                    icon = {
                        route.icon?.let {
                            Image(
                                painterResource(it),
                                contentDescription = route.name,
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(iconColor),
                            )
                        }
                    },
                    onClick = {
                        navController.navigate(route.name)
                    },
                )
            }
        }
    }
}

@Composable
fun SagaNavGraph(
    navController: NavHostController,
    padding: PaddingValues,
    transitionScope: SharedTransitionScope,
    hazeState: SnackbarHostState,
) {
    val graph =
        navController.createGraph(startDestination = Routes.HOME.name) {
            Routes.entries.forEach { route ->
                composable(
                    route.deepLink ?: route.name,
                    arguments =
                        route.arguments.map {
                            navArgument(it) {
                                type = NavType.StringType
                            }
                        },
                ) {
                    route.view(navController, padding, transitionScope, hazeState)
                }
            }
        }
    NavHost(navController, graph = graph)
}

fun NavHostController.navigateToRoute(
    route: Routes,
    arguments: Map<String, String> = mapOf(),
    popUpToRoute: Routes? = null,
) {
    var link = route.deepLink ?: route.name
    if (arguments.isNotEmpty() && arguments.size == route.arguments.size) {
        route.arguments.forEach { arg ->
            val entry = arguments.entries.find { it.key == arg }
            entry?.let {
                if (link.contains(it.key)) {
                    link = link.replace(it.key, it.value)
                }
            }
        }
    }
    val newLink = link.replace("{", "").replace("}", "")

    if (popUpToRoute != null) {
        navigate(newLink) {
            popUpTo(popUpToRoute.name) {
                inclusive = true
            }
        }
    } else {
        navigate(newLink)
    }
}

fun String.findRoute(): Routes? =
    Routes.entries.find {
        Log.i("Route find:", "looking for route $this...")
        val mappedDeepLink = it.deepLink?.sanitizeDeepLink()
        val mappedRoute = this.sanitizeDeepLink()
        Log.d(
            "Route find:",
            "findRoute: trying to match(${it.name}) $mappedDeepLink with $mappedRoute",
        )
        it.name.lowercase() == this || mappedDeepLink == mappedRoute
    }

fun String.sanitizeDeepLink() = this.substringBeforeLast("/")
