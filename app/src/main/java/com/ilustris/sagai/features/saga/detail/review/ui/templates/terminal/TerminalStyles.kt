package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.ui.theme.hexToColor

/** A soft colored blur behind text, for the terminal's neon-CRT look. */
fun TextStyle.neonGlow(
    color: Color,
    blurRadius: Float = 14f,
): TextStyle = copy(shadow = Shadow(color, Offset.Zero, blurRadius))

/** This character's own theme color, falling back to the saga's accent when unset/invalid. */
fun Character.terminalColor(fallback: Color): Color = hexColor.hexToColor() ?: fallback
