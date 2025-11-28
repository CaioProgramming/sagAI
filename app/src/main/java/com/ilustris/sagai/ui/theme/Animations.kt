package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.components.buildWikiAndCharactersAnnotation
import com.ilustris.sagai.features.newsaga.data.model.Genre
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
    genre: Genre,
    mainCharacter: Character?,
    characters: List<Character>,
    wiki: List<Wiki>,
    onAnimationFinished: () -> Unit = { },
    onTextUpdate: (String) -> Unit = { },
    onAnnotationClick: (Any?) -> Unit = { },
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

    val wikiAnnotation =
        buildWikiAndCharactersAnnotation(
            currentText,
            genre,
            mainCharacter,
            characters,
            wiki,
            MaterialTheme.colorScheme.background,
        )
    ClickableText(
        text = wikiAnnotation,
        style = style,
        onClick = { offset ->
            if (textTarget == text.length) {
                val characterAnnotation =
                    wikiAnnotation
                        .getStringAnnotations(tag = "character_tag", start = offset, end = offset)
                        .firstOrNull()

                val wikiAnnotation =
                    wikiAnnotation
                        .getStringAnnotations(tag = "wiki_tag", start = offset, end = offset)
                        .firstOrNull()

                characterAnnotation?.let { annotation ->
                    val characterId = annotation.item.split(":").lastOrNull()
                    val character = characters.find { it.id.toString() == characterId }
                    onAnnotationClick(character)
                }
                wikiAnnotation?.let { annotation ->
                    val wikiId = annotation.item.split(":").lastOrNull()
                    val wikiData = wiki.find { it.id.toString() == wikiId }
                    onAnnotationClick(wikiData)
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
fun SimpleTypewriterText(
    text: String,
    style: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier,
    duration: Duration = 3.seconds,
    easing: Easing = EaseIn,
    isAnimated: Boolean = true,
    onAnimationFinished: () -> Unit = { },
    onTextUpdate: (String) -> Unit = { },
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

    Text(
        text = currentText,
        style = style,
        modifier = modifier,
    )
}

@Composable
fun Modifier.zoomAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite zoom")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        characters = listOf(Character(name = "test bro", hexColor = "#fe2a2f", profile = CharacterProfile(), details = Details())),
        wiki =
            listOf(
                Wiki(title = "wiki test", sagaId = 0),
            ),
        mainCharacter = null,
        genre = Genre.CYBERPUNK,
    )
}
