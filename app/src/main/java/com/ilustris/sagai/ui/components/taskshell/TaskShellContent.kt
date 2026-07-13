package com.ilustris.sagai.ui.components.taskshell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

enum class TaskShellExpansion {
    Collapsed,
    Expanded,
    Full,
}

enum class TaskShellCompactClick {
    /** Toggle between collapsed and expanded. */
    Toggle,

    /** Jump straight to full expansion (e.g. premium onboarding). */
    RequestFull,

    /** Content handles its own click; shell does nothing. */
    None,
}

@Stable
class TaskShellScope internal constructor(
    val expansion: TaskShellExpansion,
    val onMinimize: () -> Unit,
    val onToggle: () -> Unit,
    val onRequestFull: () -> Unit,
)

/**
 * Self-describing shell content. Each implementation owns its compact and expanded UI,
 * plus metadata for how the shell scaffold should behave.
 */
interface TaskShellContent {
    val isExpandable: Boolean get() = true
    val isDraggable: Boolean get() = true
    val compactClick: TaskShellCompactClick get() = TaskShellCompactClick.Toggle

    @Composable
    fun Compact(scope: TaskShellScope)

    @Composable
    fun Expanded(scope: TaskShellScope)
}

data class TaskShellSlotState(
    val content: TaskShellContent,
    val expansion: TaskShellExpansion,
    val onExpansionChange: (TaskShellExpansion) -> Unit,
)

internal fun TaskShellSlotState.rememberScope(
    onMinimize: () -> Unit,
    onToggle: () -> Unit,
    onRequestFull: () -> Unit,
): TaskShellScope =
    TaskShellScope(
        expansion = expansion,
        onMinimize = onMinimize,
        onToggle = onToggle,
        onRequestFull = onRequestFull,
    )

internal fun TaskShellScope.handleCompactClick() {
    when (expansion) {
        TaskShellExpansion.Collapsed -> onToggle()
        else -> onMinimize()
    }
}
