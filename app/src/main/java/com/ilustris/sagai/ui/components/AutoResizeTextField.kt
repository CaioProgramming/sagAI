package com.ilustris.sagai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
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
    var targetFontSize by remember(textStyle) { mutableStateOf(textStyle.fontSize) }

    // Quando o texto muda, resetamos o alvo para o tamanho máximo para que ele possa "crescer" se houver espaço
    val lastValue = remember { mutableStateOf(value) }
    if (value != lastValue.value) {
        targetFontSize = textStyle.fontSize
        lastValue.value = value
    }

    val animatedFontSize by animateFloatAsState(
        targetValue = targetFontSize.value,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 500f // Um pouco mais rápido para acompanhar a digitação
        ),
        label = "FontSizeAnimation"
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle.copy(fontSize = animatedFontSize.sp),
        maxLines = maxLines,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(cursorColor),
        onTextLayout = { textLayoutResult ->
            // Se o texto transbordar usando o tamanho atual (animado ou alvo),
            // reduzimos o tamanho do ALVO.
            if (textLayoutResult.didOverflowHeight || textLayoutResult.didOverflowWidth) {
                if (targetFontSize > minFontSize) {
                    targetFontSize = (targetFontSize.value - 1f).sp
                }
            }
        },
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
