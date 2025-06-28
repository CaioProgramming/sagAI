package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.components.buildCharactersAnnotatedString
import com.ilustris.sagai.features.characters.ui.components.buildWikiAndCharactersAnnotation
import com.ilustris.sagai.features.wiki.data.model.Wiki
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
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
    characters: List<Character>,
    wiki: List<Wiki>,
    onAnimationFinished: () -> Unit = { },
    onTextUpdate: (String) -> Unit = { },
    onTextClick: () -> Unit = { },
) {
    var textTarget by remember { mutableIntStateOf(0) }
    val charIndex by animateIntAsState(
        targetValue = textTarget,
        animationSpec =
            tween(
                duration.toInt(DurationUnit.MILLISECONDS),
                easing = easing,
            ),
        finishedListener = { onAnimationFinished() },
    )

    LaunchedEffect(Unit) {
        if (textTarget == 0 && isAnimated) {
            textTarget = text.length
        } else {
            textTarget = text.length
            onTextUpdate(text)
        }
    }
    val currentText = if (isAnimated) text.substring(0, charIndex) else text

    LaunchedEffect(charIndex) {
        onTextUpdate(text.substring(0, charIndex))
    }

    val annotatedText =
        buildCharactersAnnotatedString(
            currentText,
            characters,
        )
    val wikiAnnotation =
        buildWikiAndCharactersAnnotation(
            annotatedText.text,
            characters,
            wiki,
        )
    ClickableText(
        text = wikiAnnotation,
        style = style,
        onClick = { offset ->
            annotatedText
                .getStringAnnotations(tag = "character_tag", start = offset, end = offset)
                .firstOrNull()
                ?.let { annotation ->
                    if (textTarget == text.length) {
                        onTextClick()
                    }
                }
        },
        modifier = modifier,
    )
}

@Composable
fun Modifier.zoomAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite zoom")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1.minutes.toInt(DurationUnit.MILLISECONDS)),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "imageZoom",
    )

    return this
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            transformOrigin = TransformOrigin.Center,
        ).clipToBounds()
}

@Preview(showBackground = true)
@Composable
fun TypewriterTextPreview() {
    val text = "Hello test bro make a wiki test!"
    TypewriterText(
        text = text,
        duration = 2.seconds,
        easing = EaseInBounce,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        characters = listOf(Character(name = "test bro", hexColor = "#fe2a2f",details = Details())),
        wiki = listOf(
            Wiki(title = "wiki test", sagaId = 0)
        )
    )
}
