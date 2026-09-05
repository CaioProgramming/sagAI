package com.ilustris.sagai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoResizeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = TextStyle.Default,
    minFontSize: TextUnit = 12.sp,
    maxLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    cursorColor: Color = LocalContentColor.current
) {
    val textMeasurer = rememberTextMeasurer()
    var containerWidth by remember { mutableStateOf(0) }
    var targetFontSize by remember(textStyle.fontSize) { mutableStateOf(textStyle.fontSize) }

    // Calculamos o tamanho ideal baseado na largura real do componente
    LaunchedEffect(value, containerWidth, textStyle) {
        if (containerWidth > 0 && value.isNotEmpty()) {
            val measuredWidth = textMeasurer.measure(
                text = value,
                style = textStyle,
                maxLines = 1,
                softWrap = false
            ).size.width

            if (measuredWidth > containerWidth) {
                // Calcula a proporção necessária para caber
                val ratio = containerWidth.toFloat() / measuredWidth.toFloat()
                val newSize = (textStyle.fontSize.value * ratio).sp
                targetFontSize = if (newSize < minFontSize) minFontSize else newSize
            } else {
                targetFontSize = textStyle.fontSize
            }
        } else {
            targetFontSize = textStyle.fontSize
        }
    }

    val animatedFontSize by animateFloatAsState(
        targetValue = targetFontSize.value,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = 1000f
        ),
        label = "FontSizeAnimation"
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onSizeChanged { containerWidth = it.width },
        textStyle = textStyle.copy(fontSize = animatedFontSize.sp),
        maxLines = maxLines,
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(cursorColor),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty() && placeholder != null) {
                    placeholder()
                }
                innerTextField()
            }
        }
    )
}
