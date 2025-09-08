package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun TypewriterText(
    text: String,
    style: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier,
    duration: Duration = 3.seconds,
    easing: Easing = EaseIn,
    isAnimated: Boolean = true,
    onAnimationFinished: () -> Unit = {  },
    onTextUpdate : (String) -> Unit = {  }
) {
    var textTarget by remember { mutableIntStateOf(0) }
    val charIndex by animateIntAsState(
        targetValue = textTarget,
        animationSpec = tween(duration.toInt(DurationUnit.MILLISECONDS),
            easing = easing),
        finishedListener = { onAnimationFinished() }
    )


    LaunchedEffect(Unit) {
        if (textTarget == 0 && isAnimated) {
            textTarget = text.length
        } else {
            onTextUpdate(text)
        }
    }

    LaunchedEffect(charIndex) {
        onTextUpdate(text.substring(0, charIndex))
    }

    Text(
        text = if (isAnimated) text.substring(0, charIndex) else text,
        modifier = modifier,
        style = style,
    )
}

@Preview(showBackground = true)
@Composable
fun TypewriterTextPreview() {
    val text = "Hello Typewritter!"
    TypewriterText(
        text = text,
        duration = 5.seconds,
        easing = EaseInBounce,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    )

}
