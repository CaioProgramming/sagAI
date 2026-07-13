package com.ilustris.sagai.core.globalshell

/**
 * Shell expansion state for the global overlay host.
 *
 * Kept in core (instead of reusing the TaskShell UI enum) so effects/services don't
 * depend on UI-layer types.
 */
enum class GlobalShellExpansion {
    Collapsed,
    Expanded,
    Full,
}

