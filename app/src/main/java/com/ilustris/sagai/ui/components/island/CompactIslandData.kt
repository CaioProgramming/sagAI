package com.ilustris.sagai.ui.components.island

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.newsaga.data.model.Genre

/** Lazy color resolution for island backgrounds — avoids Composable lambdas in data class. */
sealed class IslandBackgroundColor {
    data class Fixed(val color: Color) : IslandBackgroundColor()
    object ThemePrimary : IslandBackgroundColor()
    object ThemeSurface : IslandBackgroundColor()
    object ThemeBackground : IslandBackgroundColor()
}

/**
 * Data-only description of an island's compact (collapsed) form.
 *
 * Unlike the legacy `TaskShellContent.Compact()` composable, the compact form is *not* a
 * free composable — it is a fixed structure rendered by [CompactIslandLayout]:
 *
 * ```
 * [ icon ]  [ label (weight) ]  [ loading / progress ]
 * ```
 *
 * Keeping it as immutable data (rather than a composable) makes the compact row consistent
 * across every effect, keeps sizing predictable for the expand/collapse animation, and makes
 * it trivially testable. All the per-effect freedom lives in [IslandContent.Expanded] instead.
 */
@Immutable
data class CompactIslandData(
    /** Literal label; takes precedence over [labelRes] when both are set. */
    val label: String? = null,
    /** String-resource label, resolved by [CompactIslandLayout]. Used when [label] is null. */
    @param:StringRes val labelRes: Int? = null,
    /**
     * String-resource label swapped in while expanded, in place of [label]/[labelRes] — lets the
     * always-visible compact row double as a header for the expanded content below it (e.g. the
     * objective shows the objective text when collapsed, but "Objetivo atual" once expanded,
     * where the actual text moves into the expanded body). Null = keep [label]/[labelRes] as-is.
     */
    @param:StringRes val expandedLabelRes: Int? = null,
    @param:DrawableRes val iconRes: Int? = null,
    val isLoading: Boolean = false,
    /** When non-null (0f..1f), a determinate progress indicator replaces the spinner. */
    val progress: Float? = null,
    /**
     * Trailing icon signaling "tap to act" (e.g. an arrow) for islands where tapping the compact
     * row directly commits [IslandContent.onAction] instead of expanding — only shown when
     * [isLoading] is false and [progress] is null (that trailing slot is otherwise the
     * loading/progress indicator).
     */
    @param:DrawableRes val actionIconRes: Int? = null,
    /** Optional genre used to theme the compact row via `SagAITheme`. */
    val genre: Genre? = null,
    /** Lazy background color; resolved at render time in the overlay. Null = use theme default. */
    val backgroundColor: IslandBackgroundColor? = null,
    /**
     * When `false`, the island's card (background + shadow + border) is transparent while
     * *collapsed* — the compact form floats bare (e.g. the objective's lone icon) — and fades
     * back in when expanded. When `true` (default), the card is always visible.
     */
    val showBackground: Boolean = true,
)
