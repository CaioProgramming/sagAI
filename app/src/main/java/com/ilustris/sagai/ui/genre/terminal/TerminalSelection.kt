package com.ilustris.sagai.ui.genre.terminal

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.launch

/**
 * The press feedback a text terminal actually gives: the cell block fills with the foreground
 * colour and the glyphs punch through it in the background colour. No ripple, no fade.
 *
 * A ripple is a Material affordance — ink spreading from a fingertip on paper. On a screen that is
 * pretending to be a character grid it is the one gesture that gives away the platform underneath,
 * because a terminal has no continuous surface for ink to spread across. Inverting the cell is
 * both the historically correct feedback and the only one that stays inside the fiction.
 *
 * The inversion is drawn with [BlendMode.Difference] rather than by re-colouring the text, so it
 * works on whatever the caller drew — glyphs, borders, an image — without needing to know about it.
 */
class TerminalSelectionIndication(
    private val color: Color,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): Modifier.Node =
        TerminalSelectionNode(interactionSource, color)

    override fun equals(other: Any?): Boolean =
        other is TerminalSelectionIndication && other.color == color

    override fun hashCode(): Int = color.hashCode()
}

private class TerminalSelectionNode(
    private val interactionSource: InteractionSource,
    private val color: Color,
) : Modifier.Node(),
    DrawModifierNode {
    private var selected = false

    override fun onAttach() {
        coroutineScope.launch {
            var presses = 0
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> presses++
                    is PressInteraction.Release, is PressInteraction.Cancel -> presses--
                    else -> Unit
                }
                val nowSelected = presses > 0
                if (nowSelected != selected) {
                    selected = nowSelected
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (selected) {
            drawRect(color = color, blendMode = BlendMode.Difference)
        }
    }
}

/** The classic terminal block-cursor selection, ready to hand to `clickable`'s `indication`. */
fun terminalSelection(color: Color): Indication = TerminalSelectionIndication(color)

/**
 * `clickable` with the terminal's own press feedback. Each call site needs its own
 * [MutableInteractionSource], so this keeps that bookkeeping out of the pages.
 */
@Composable
fun Modifier.terminalClickable(
    indication: Indication,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return clickable(
        interactionSource = interactionSource,
        indication = indication,
        onClick = onClick,
    )
}
