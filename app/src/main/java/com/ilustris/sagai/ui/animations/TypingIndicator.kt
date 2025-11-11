package com.ilustris.sagai.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.SagAITheme
import kotlinx.coroutines.delay

@Composable
fun TypingIndicator(modifier: Modifier = Modifier, bubbleColor: Color = MaterialTheme.colorScheme.primary, ballColor: Color = MaterialTheme.colorScheme.onPrimary) {

    val animationValues = List(3) {
        remember { Animatable(0f) }
    }

    animationValues.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f, animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            )
        }
    }

    val yOffset = with(LocalDensity.current) {
        -8.dp.toPx()
    }

    Row(modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        animationValues.forEach { animatable ->
            Box(
                Modifier
                    .padding(2.dp)
                    .size(8.dp)
                    .graphicsLayer {
                        translationY = animatable.value * yOffset
                    }
                    .background(
                        color = ballColor,
                        shape = CircleShape
                    )
            )
        }

    }

}

@Preview
@Composable
fun TypingIndicatorPreview() {
    SagAITheme {
        TypingIndicator()
    }
}