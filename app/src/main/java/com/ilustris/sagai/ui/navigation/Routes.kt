package com.ilustris.sagai.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chat.ui.ChatView
import com.ilustris.sagai.features.home.ui.HomeView
import com.ilustris.sagai.features.newsaga.ui.NewSagaView

enum class Routes(
    val view: @Composable (NavHostController) -> Unit = {
        Text("Sample View for Route ", modifier = Modifier.padding(16.dp))
    },
    val showBottomNav: Boolean = true,
    val showTitle: Boolean = true,
    @DrawableRes val icon: Int? = null,
    @StringRes val title: Int? = null,
    @StringRes val navigationTitle: Int? = R.string.saga,
    val arguments: List<String> = emptyList(),
    val deepLink: String? = null,
) {
    HOME(icon = R.drawable.ic_spark, view = {
        HomeView(it)
    }, title = R.string.home_title),
    CHAT(view = {
        val arguments = it.currentBackStackEntry?.arguments
        ChatView(it, arguments?.getString(CHAT.arguments.first()))
    }, arguments = listOf("sagaId"), deepLink = "saga://chat/{sagaId}"),
    PROFILE,
    SETTINGS,
    NEW_SAGA(title = R.string.new_saga_title, showBottomNav = false, view = {
        NewSagaView(it)
    }),
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
fun SagaNavGraph(navController: NavHostController) {
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
                    route.view(navController)
                }
            }
        }
    NavHost(navController, graph = graph)
}

fun NavHostController.navigateToRoute(
    route: Routes,
    arguments: Map<String, String> = mapOf(),
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

    navigate(link.replace("{", "").replace("}", ""))
}

fun String.findRoute(): Routes? =
    Routes.entries.find {
        val mappedDeepLink = it.deepLink?.substringBeforeLast("/")
        it.name == this || mappedDeepLink == this
    }
