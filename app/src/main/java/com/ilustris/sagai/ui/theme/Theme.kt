package com.ilustris.sagai.ui.theme
import ai.atick.material.MaterialColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette

private val DarkColorScheme =
    darkColorScheme(
        onPrimary = Color.White,
        primary = MaterialColor.BlueA400,
        secondary = MaterialColor.Blue400,
        tertiary = MaterialColor.Teal700,
        background = Color.Black,
        surfaceContainer = MaterialColor.Gray800.darker(.5f),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = MaterialColor.Blue500,
        secondary = MaterialColor.Blue800,
        tertiary = MaterialColor.Teal300,
        onPrimary = MaterialColor.White,
        background = MaterialColor.Gray100,
        surfaceContainer = Color.White,
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
fun themeBrushColors(): List<Color> {
    val genre = LocalSagaGenre.current
    return genre?.colorPalette() ?: listOf(
        MaterialColor.Blue500,
        MaterialColor.BlueA700,
        MaterialColor.LightBlueA400,
    )
}

private const val THEME_ANIMATION_DURATION = 600

/**
 * CompositionLocal providing the currently active [Genre] from [SagaThemeManager].
 * - On genre-immersed screens (SagaDetail, Chat): holds the saga's genre.
 * - On genre-neutral screens (Home, NewSaga): holds null (brand defaults).
 *
 * Use [sagaShape], [sagaBrush], etc. to access genre-specific visuals
 * without manually passing the genre around.
 */
val LocalSagaGenre = compositionLocalOf<Genre?> { null }

/**
 * CompositionLocal providing the currently active [GenreVisualConfig] from [SagaThemeManager].
 */
val LocalGenreVisualConfig = compositionLocalOf<GenreVisualConfig?> { null }

@Composable
fun SagAITheme(
    sagaThemeManager: SagaThemeManager? = null,
    genre: Genre? = null,
    visualConfig: GenreVisualConfig? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val activeGenreState: State<Genre?> =
        if (sagaThemeManager != null) {
            sagaThemeManager.currentGenre.collectAsState(initial = null)
        } else {
            remember(genre) { mutableStateOf(genre) }
        }
    val activeGenre = activeGenreState.value

    val activeVisualConfigState =
        if (sagaThemeManager != null) {
            sagaThemeManager.visualConfig.collectAsState(initial = null)
        } else {
            remember(visualConfig) { mutableStateOf(visualConfig) }
        }
    val activeVisualConfig = activeVisualConfigState.value

    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Target colors: genre-driven or brand defaults
    val targetPrimary = activeGenre?.color ?: baseScheme.primary
    val targetSecondary = activeGenre?.color?.copy(alpha = 0.7f) ?: baseScheme.secondary
    val targetTertiary = activeGenre?.color?.copy(alpha = 0.5f) ?: baseScheme.tertiary
    val onPrimary = activeGenre?.iconColor ?: baseScheme.onPrimary

    // Smooth animated transitions
    val animatedPrimary =
        animateColorAsState(
            targetValue = targetPrimary,
            animationSpec = tween(THEME_ANIMATION_DURATION),
            label = "themePrimary",
        )
    val animatedSecondary =
        animateColorAsState(
            targetValue = targetSecondary,
            animationSpec = tween(THEME_ANIMATION_DURATION),
            label = "themeSecondary",
        )
    val animatedTertiary =
        animateColorAsState(
            targetValue = targetTertiary,
            animationSpec = tween(THEME_ANIMATION_DURATION),
            label = "themeTertiary",
        )

    val colorScheme =
        baseScheme.copy(
            primary = animatedPrimary.value,
            secondary = animatedSecondary.value,
            tertiary = animatedTertiary.value,
            onPrimary = onPrimary,
        )

    // Dynamic Typography: genre fonts baked into the theme
    val dynamicTypography =
        remember(activeGenre) {
            if (activeGenre == null) {
                Typography
            } else {
                val headerFamily = activeGenre.headerFont()
                val bodyFamily = activeGenre.bodyFont()
                Typography(
                    displayLarge = Typography.displayLarge.copy(fontFamily = headerFamily),
                    displayMedium = Typography.displayMedium.copy(fontFamily = headerFamily),
                    displaySmall = Typography.displaySmall.copy(fontFamily = headerFamily),
                    headlineLarge = Typography.headlineLarge.copy(fontFamily = headerFamily),
                    headlineMedium = Typography.headlineMedium.copy(fontFamily = headerFamily),
                    headlineSmall = Typography.headlineSmall.copy(fontFamily = headerFamily),
                    titleLarge = Typography.titleLarge.copy(fontFamily = headerFamily),
                    titleMedium = Typography.titleMedium.copy(fontFamily = headerFamily),
                    titleSmall = Typography.titleSmall.copy(fontFamily = headerFamily),
                    bodyLarge = Typography.bodyLarge.copy(fontFamily = bodyFamily),
                    bodyMedium = Typography.bodyMedium.copy(fontFamily = bodyFamily),
                    bodySmall = Typography.bodySmall.copy(fontFamily = bodyFamily),
                    labelLarge = Typography.labelLarge.copy(fontFamily = bodyFamily),
                    labelMedium = Typography.labelMedium.copy(fontFamily = bodyFamily),
                    labelSmall = Typography.labelSmall.copy(fontFamily = bodyFamily),
                )
            }
        }

    CompositionLocalProvider(
        LocalSagaGenre provides activeGenre,
        LocalGenreVisualConfig provides activeVisualConfig,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = dynamicTypography,
            content = content,
        )
    }
}

// ── Theme Extension Functions ─────────────────────────────────────────
// Use these on genre-immersed screens instead of manual genre extensions.

/**
 * Returns the current genre's shape, or [MaterialTheme.shapes.medium] on neutral screens.
 * Equivalent to `genre.shape()` but theme-driven.
 */
@Composable
fun sagaShape(): Shape = LocalSagaGenre.current.shape()

/**
 * Returns a gradient [Brush] for the current genre, or the brand holographic gradient.
 * Equivalent to `genre.gradient()` but theme-driven.
 */
@Composable
fun sagaBrush(
    animated: Boolean = false,
    gradientType: GradientType = GradientType.LINEAR,
): Brush = LocalSagaGenre.current.gradient(animated = animated, gradientType = gradientType)

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
    iconModifier: Modifier = Modifier,
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
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                    ),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_spark),
                        contentDescription = null,
                        tint = LocalContentColor.current,
                        modifier = iconModifier,
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
