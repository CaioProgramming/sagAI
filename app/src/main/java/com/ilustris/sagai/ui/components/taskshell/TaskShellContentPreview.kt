package com.ilustris.sagai.ui.components.taskshell

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.SagAIScaffold

/**
 * Renders a [TaskShellContent] through the real [TaskShellLayout] chrome (rounded corners,
 * drag regions, padding, height animation targets), starting at [initialExpansion] — so
 * previews stay accurate to production layout without duplicating any of that logic. The
 * `content` slot (the main screen body in production, e.g. the chat message list) is filled
 * with a plain placeholder text instead of being left blank, so the preview reads as a real
 * screen composition — content box plus the shell sitting above/below it — not shell chrome
 * floating over emptiness.
 *
 * [background] defaults to the same reactive primary/background color transition
 * [com.ilustris.sagai.features.saga.chat.ui.components.milestone.ChatTaskShellHost] uses —
 * that's the layer real screens paint behind the shell via [TaskShellLayout]'s own
 * `background` slot, so previews show it too instead of shell chrome floating over nothing.
 * Pass a different lambda to match a host with its own background (e.g. `GlobalShellHost`'s
 * genre-reactive one).
 *
 * Expansion state is genuinely held (not a no-op callback), so Android Studio's Interactive
 * Preview mode can tap/drag the shell to see Collapsed/Expanded/Full for real instead of
 * needing a separate static preview per state. Always renders in dark mode — easier to read
 * shell chrome against than whatever the system theme happens to be.
 *
 * This doesn't shield content from genre theming: implementations that call `SagAITheme(genre)`
 * or fetch remote visual config internally will still attempt to, same as they would live. Use
 * this to check structure/spacing/composition, not final genre-specific art.
 */
@Composable
fun TaskShellContentPreview(
    content: TaskShellContent,
    initialExpansion: TaskShellExpansion = TaskShellExpansion.Collapsed,
    onTop: Boolean = true,
    background: @Composable BoxScope.(top: TaskShellSlotState?, bottom: TaskShellSlotState?) -> Unit = {
            top, bottom,
        ->
        val isActive =
            (top != null && top.expansion != TaskShellExpansion.Collapsed) ||
                (bottom != null && bottom.expansion != TaskShellExpansion.Collapsed)
        val backgroundColor by animateColorAsState(
            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        )
        Box(Modifier.fillMaxSize().background(backgroundColor))
    },
) {
    var expansion by remember { mutableStateOf(initialExpansion) }
    val slot =
        TaskShellSlotState(
            content = content,
            expansion = expansion,
            onExpansionChange = { expansion = it },
        )
    SagAIScaffold(darkTheme = true) {
        TaskShellLayout(
            modifier = Modifier.fillMaxSize(),
            topSlot = if (onTop) slot else null,
            bottomSlot = if (!onTop) slot else null,
            background = background,
        ) {
            TaskShellContentPreviewSampleContent()
        }
    }
}

@Composable
private fun TaskShellContentPreviewSampleContent() {
    // Matches the color TaskShellLayout's own content Box already paints behind this slot —
    // using a different token here (e.g. surfaceContainer) would expose that Box's top/bottom
    // padding as a visible seam, since the wrapper's background fills the full Box before the
    // padding is applied, not just the area content() actually occupies.
    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Conteúdo de exemplo\n(mensagens, telas, etc.)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
            textAlign = TextAlign.Center,
        )
    }
}
