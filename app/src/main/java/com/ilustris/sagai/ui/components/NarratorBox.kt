package com.ilustris.sagai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Styled box for narrator text embedded in character messages
 *
 * Displays narrator text with:
 * - Bordered box with genre color
 * - Semi-transparent background
 * - Italic text style
 * - Slightly reduced font size (90%)
 *
 * Note: This is different from full SenderType.NARRATOR bubbles.
 * This is for inline narrator context within character messages.
 *
 * Example: "She smiled. <narrator>She had no idea what awaited.</narrator> 'Let's go!'"
 *
 * @param text The narrator text content
 * @param style Base text style
 * @param genre Genre for color theming
 * @param modifier Optional modifier
 */
@Composable
fun NarratorBox(
    text: String,
    style: TextStyle,
    genre: Genre,
    modifier: Modifier = Modifier
) {
    val narratorShape = RoundedCornerShape(6.dp)
    val narratorStyle = style.copy(
        fontStyle = FontStyle.Italic,
        fontSize = (style.fontSize.value * 0.9f).sp,
        color = MaterialTheme.colorScheme.onSurface
    )

    Box(
        modifier = modifier
            .clip(narratorShape)
            .background(
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f),
                narratorShape
            )
            .border(
                BorderStroke(1.dp, genre.color),
                narratorShape
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = narratorStyle
        )
    }
}

