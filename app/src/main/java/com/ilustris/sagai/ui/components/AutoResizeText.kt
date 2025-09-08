package com.ilustris.sagai.ui.components // New package declaration

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoResizeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 10.sp,
    maxLines: Int = 1,
) {
    var currentTextStyle by remember(text, style) { mutableStateOf(style) }
    val fontSizeReductionStep = 1.sp

    Text(
        text = text,
        modifier = modifier,
        style = currentTextStyle,
        maxLines = maxLines,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight) {
                if (currentTextStyle.fontSize > minFontSize) {
                    val newSize = currentTextStyle.fontSize.value - fontSizeReductionStep.value
                    currentTextStyle =
                        currentTextStyle.copy(
                            fontSize = maxOf(newSize, minFontSize.value).sp,
                        )
                }
            }
        },
    )
}
