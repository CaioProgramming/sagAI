package com.ilustris.sagai.ui.theme

import ai.atick.material.MaterialColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre

private val DarkColorScheme =
    darkColorScheme(
        onPrimary = Color.White,
        primary = MaterialColor.BlueA400,
        secondary = MaterialColor.Blue400,
        tertiary = MaterialColor.Teal700,
        background = MaterialColor.Black,
        surfaceContainer = MaterialColor.Gray900.darker(.5f),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = MaterialColor.Blue500,
        secondary = MaterialColor.Blue800,
        tertiary = MaterialColor.Teal300,
        onPrimary = MaterialColor.White,
        background = MaterialColor.Gray100,
        surfaceContainer = MaterialColor.White,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
     */
    )

@Composable
fun themeBrushColors() =
    listOf(
        MaterialColor.Blue500,
        MaterialColor.BlueA700,
        MaterialColor.LightBlueA400,
    )

@Composable
fun SagAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SagAIScaffold(
    title: String? = null,
    showTopBar: Boolean = false,
    content: @Composable () -> Unit,
) {
    SagAITheme {
        Scaffold(topBar = {
            AnimatedVisibility(showTopBar) {
                TopAppBar(
                    title = {
                        title?.let {
                            Text(
                                text = it,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier =
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                            )
                        } ?: run {
                            SagaTitle(
                                Modifier.fillMaxWidth(),
                            )
                        }
                    },
                    actions = {
                        Box(Modifier.size(24.dp))
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.size(24.dp))
                    },
                )
            }
        }) {
            Box(modifier = Modifier.padding(it)) {
                content()
            }
        }
    }
}

@Composable
fun MorphShape(modifier: Modifier) {
    val shapeA =
        remember {
            RoundedPolygon(
                12,
                rounding = CornerRounding(0.2f),
            )
        }
    val shapeB =
        remember {
            RoundedPolygon.star(
                12,
                rounding = CornerRounding(0.2f),
            )
        }
    val morph =
        remember {
            Morph(shapeA, shapeB)
        }
    val infiniteTransition = rememberInfiniteTransition("infinite outline movement")
    val animatedProgress =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = (
                infiniteRepeatable(
                    tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            ),
            label = "animatedMorphProgress",
        )
    val animatedRotation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = (
                infiniteRepeatable(
                    tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            ),
            label = "animatedMorphProgress",
        )

    Box(
        modifier
            .background(MaterialColor.Red400)
            .clip(
                CustomRotatingMorphShape(
                    morph,
                    animatedProgress.value,
                    animatedRotation.value,
                ),
            ),
    )
}

@Composable
fun SagaTitle(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
) {
    val appName = stringResource(R.string.home_title).uppercase()
    val charToReplace = 'A'
    val iconId = "sagaTitleSparkIcon" // Unique ID for the inline content

    val annotatedString =
        buildAnnotatedString {
            val firstCharIndex = appName.indexOf(charToReplace)

            if (firstCharIndex != -1) {
                append(appName.substring(0, firstCharIndex))
                appendInlineContent(iconId, "[spark icon replacing 'a']")
                append(appName.substring(firstCharIndex + 1))
            } else {
                append(appName)
            }
        }

    val inlineContent =
        mapOf(
            iconId to
                InlineTextContent(
                    Placeholder(
                        width = textStyle.fontSize * .8f,
                        height = textStyle.fontSize * 1.1f,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center, // Changed here
                    ),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_spark),
                        contentDescription = null, // Accessibility handled by alternateText
                        tint = LocalContentColor.current, // Inherits color from Text composable
                    )
                },
        )

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style =
            textStyle.copy(
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            ),
        modifier = modifier,
    )
}
