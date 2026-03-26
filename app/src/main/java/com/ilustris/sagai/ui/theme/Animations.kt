package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
    val currentText = if (isAnimated) text.take(charIndex) else text

    LaunchedEffect(charIndex) {
        if (isAnimated) {
            onTextUpdate(text.take(charIndex))
        }
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
    textAlign: TextAlign? = null,
    strokeColor: Color? = null,
    strokeWidth: Float = 8f,
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
        label = "typewriterAnimation",
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

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (strokeColor != null) {
            Text(
                text = currentText,
                style =
                    style.copy(
                        color = strokeColor,
                        drawStyle =
                            Stroke(
                                miter = 10f,
                                width = strokeWidth,
                                join = StrokeJoin.Round,
                            ),
                    ),
                textAlign = textAlign,
            )
        }
        Text(
            text = currentText,
            style = style,
            textAlign = textAlign,
        )
    }
}

@Composable
fun Modifier.levitate(
    isPlaying: Boolean = true,
    duration: Duration = 2.seconds,
    easing: Easing = LinearEasing,
    yOffset: Float = 20f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "levitate")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -yOffset,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = easing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "levitateTranslationY",
    )
    return this.graphicsLayer(translationY = translationY)
}

@Composable
fun Modifier.pulse(
    isPlaying: Boolean = true,
    duration: Duration = 1.seconds,
    easing: Easing = LinearEasing,
    pulseScale: Float = 1.2f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseScale,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = easing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulseScale",
    )
    return this.graphicsLayer(scaleX = scale, scaleY = scale)
}

@Composable
fun Modifier.shake(
    isPlaying: Boolean = true,
    duration: Duration = 0.5.seconds,
    easing: Easing = LinearEasing,
    xOffset: Float = 10f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val translationX by infiniteTransition.animateFloat(
        initialValue = -xOffset,
        targetValue = xOffset,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = easing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "shakeTranslationX",
    )
    return this.graphicsLayer(translationX = translationX)
}

@Composable
fun Modifier.rotate(
    isPlaying: Boolean = true,
    duration: Duration = 5.seconds,
    easing: Easing = LinearEasing,
    degrees: Float = 360f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = degrees,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = easing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "rotateRotationZ",
    )
    return this.graphicsLayer(rotationZ = rotation)
}

@Composable
fun Modifier.blink(
    isPlaying: Boolean = true,
    duration: Duration = 1.seconds,
    easing: Easing = LinearEasing,
    minAlpha: Float = 0.3f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = minAlpha,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = easing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "blinkAlpha",
    )
    return this.graphicsLayer(alpha = alpha)
}

@Composable
fun Modifier.glow(
    color: Color,
    isPlaying: Boolean = true,
    duration: Duration = 2.seconds,
    maxAlpha: Float = 0.5f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = maxAlpha,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glowAlpha",
    )

    return this.drawBehind {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size.maxDimension / 1.5f,
            center = center,
        )
    }
}

@Composable
fun Modifier.jiggle(
    isPlaying: Boolean = true,
    duration: Duration = 0.4.seconds,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "jiggleRotation",
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "jiggleScale",
    )
    return this.graphicsLayer(rotationZ = rotation, scaleX = scale, scaleY = scale)
}

@Composable
fun Modifier.swing(
    isPlaying: Boolean = true,
    duration: Duration = 2.seconds,
    degrees: Float = 10f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "swing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -degrees,
        targetValue = degrees,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = EaseIn),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "swingRotation",
    )
    return this.graphicsLayer(rotationZ = rotation, transformOrigin = TransformOrigin(0.5f, 0f))
}

@Composable
fun Modifier.bounce(
    isPlaying: Boolean = true,
    duration: Duration = 0.8.seconds,
    yOffset: Float = 30f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -yOffset,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = EaseInBounce),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "bounceTranslationY",
    )
    return this.graphicsLayer(translationY = translationY)
}

@Composable
fun Modifier.tilt(
    isPlaying: Boolean = true,
    duration: Duration = 2.seconds,
    degrees: Float = 5f,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "tilt")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -degrees,
        targetValue = degrees,
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "tiltRotation",
    )
    return this.graphicsLayer(rotationZ = rotation)
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

@Composable
fun Modifier.divineAura(
    isPlaying: Boolean = true,
    color: Color = Color(0xFFFFD700),
    duration: Duration = 3.seconds,
): Modifier {
    if (!isPlaying) return this
    val infiniteTransition = rememberInfiniteTransition(label = "divineAura")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        duration.toInt(DurationUnit.MILLISECONDS),
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "divineAuraScale",
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        duration.toInt(DurationUnit.MILLISECONDS),
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "divineAuraAlpha",
    )

    return this.drawBehind {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = (size.maxDimension / 2) * scale,
            center = center,
            style = Stroke(width = 4.dp.toPx()),
        )
        drawCircle(
            color = color.copy(alpha = alpha / 2),
            radius = (size.maxDimension / 2) * (scale + 0.2f),
            center = center,
        )
    }
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
