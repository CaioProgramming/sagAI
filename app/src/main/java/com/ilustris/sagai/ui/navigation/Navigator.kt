package com.ilustris.sagai.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(
    val state: NavigationState,
) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
            val stack = state.backStacks[route]
            while (stack?.lastOrNull() != null && stack.lastOrNull() != route) {
                stack.removeLastOrNull()
            }
        } else {
            val currentStack = state.backStacks[state.topLevelRoute]
            if (currentStack?.lastOrNull() != route) {
                currentStack?.add(route)
            }
        }
    }

    fun canGoBack(): Boolean {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return false
        if (currentStack.size > 1) return true
        // Root of a non-start top-level tab (e.g. settings, audit): back should return to home.
        return state.topLevelRoute != state.startRoute
    }

    fun goBack() {
        val currentStack =
            state.backStacks[state.topLevelRoute]
                ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.lastOrNull() ?: return

        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
            return
        }

        // Single entry on current stack: pop to start route when leaving another top-level root.
        if (currentRoute == state.topLevelRoute && state.topLevelRoute != state.startRoute) {
            state.topLevelRoute = state.startRoute
        }
    }
}
