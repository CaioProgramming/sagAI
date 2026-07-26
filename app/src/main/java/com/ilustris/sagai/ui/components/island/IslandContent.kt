package com.ilustris.sagai.ui.components.island

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

enum class IslandExpansionState {
    Compact,
    Expanded,
}

/**
 * Interaction surface handed to [IslandContent.Expanded] so the body can drive its own
 * collapse (e.g. an in-content "Continue"/"Close" button) using the same path as a
 * tap-outside dismiss.
 */
@Stable
class IslandScope internal constructor(
    val expansion: IslandExpansionState,
    val onCollapse: () -> Unit,
)

/**
 * A single, unified content contract for both the top island and the bottom action island.
 * Whether a given content renders at the top or bottom is decided by the *effect* that carries
 * it (see the island overlay hosts), not by the content type — so one implementation can serve
 * either position.
 *
 * - [compact] — immutable data for the always-visible collapsed row ([CompactIslandData]).
 * - [onAction] — the primary action for this island, invoked by the host when the user taps the
 *   compact row's primary affordance (or an implementation-defined trigger). The caller that
 *   constructs the content supplies the behavior, keeping the island component agnostic.
 * - [Expanded] — full composable freedom for the detailed view shown once expanded.
 */
interface IslandContent {
    val compact: CompactIslandData

    /** Primary action for this island; behavior is supplied by whoever constructs the content. */
    val onAction: () -> Unit
        get() = {}

    /**
     * When `true` (default), tapping the compact row toggles the expanded state.
     * When `false`, tapping the compact row invokes [onAction] instead (e.g. the advance island,
     * where a tap commits the action directly rather than expanding).
     */
    val expandsOnTap: Boolean
        get() = true

    /**
     * When `true`, the host keeps the island expanded regardless of user toggling, and tap-to-
     * dismiss/collapse is suppressed (e.g. while an action is processing and streaming reasoning).
     */
    val forceExpanded: Boolean
        get() = false

    /**
     * When non-null, the host auto-expands this island this many ms after it's first published —
     * a one-shot nudge, not a persistent force (unlike [forceExpanded], the user can still
     * collapse it immediately after). Used for reveal content (milestones) that should present
     * itself without requiring a tap.
     */
    val autoExpandAfterMs: Long?
        get() = null

    /**
     * When non-null, the host invokes [onAction] this many ms after the island is first
     * published — for content that shouldn't wait indefinitely for a tap to clear itself
     * (e.g. an introduction recap that should step aside on its own).
     */
    val autoDismissAfterMs: Long?
        get() = null

    @Composable
    fun Expanded(scope: IslandScope)
}
