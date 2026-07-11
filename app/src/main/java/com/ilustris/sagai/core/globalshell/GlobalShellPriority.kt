package com.ilustris.sagai.core.globalshell

/**
 * Persistent work should not be replaced by transient effects.
 * For example, image generation can keep running while a "new message" arrives.
 */
enum class GlobalShellPriority {
    PersistentWork,
    Transient,
}

