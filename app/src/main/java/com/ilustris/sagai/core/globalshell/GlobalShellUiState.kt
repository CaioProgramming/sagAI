package com.ilustris.sagai.core.globalshell

/** UI state for the global island overlay host. */
data class GlobalShellUiState(
    val effect: GlobalShellEffect? = null,
    val expansion: GlobalShellExpansion = GlobalShellExpansion.Collapsed,
) {
    val isVisible: Boolean get() = effect != null
    val isPersistentWorkActive: Boolean
        get() = effect?.priority == GlobalShellPriority.PersistentWork
}

