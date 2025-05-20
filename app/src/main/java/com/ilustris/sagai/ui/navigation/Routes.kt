package com.ilustris.sagai.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.ui.HomeView

enum class Routes(
    val view: @Composable (NavHostController) -> Unit = {},
    val showBottomNav: Boolean = true,
    val showTitle: Boolean = true,
    @DrawableRes val icon: Int? = null,
    @StringRes val title: Int? = null,
    @StringRes val navigationTitle: Int? = R.string.saga
) {
    HOME(icon = R.drawable.ic_spark, view = {
       HomeView(it)
    }),
    CHAT,
    PROFILE,
    SETTINGS,
    NEW_CHAT(),
}

@Composable
fun SagaBottomNavigation(
    navController: NavHostController,
    bottomPadding: Dp,
) {
    NavigationBar {
        Routes.entries.filter { it.icon != null }.forEach { route ->
            NavigationBarItem(
                selected = true,
                label = {
                    Text(stringResource(route.title ?: R.string.app_name))
                },
                icon = {
                    route.icon?.let {
                        Image(painterResource(it), contentDescription = route.name)
                    }
                },
                onClick = {
                    navController.navigate(route.name)
                },
            )
        }
    }
}

@Composable
fun SagaNavGraph(navController: NavHostController) {
    val graph =
        navController.createGraph(startDestination = Routes.HOME) {
            Routes.entries.forEach { route ->
                composable(route.name) {
                    route.view(navController)
                }
            }
        }
    NavHost(navController, graph = graph)
}
