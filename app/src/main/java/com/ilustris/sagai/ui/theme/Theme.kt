package com.ilustris.sagai.ui.theme
import ai.atick.material.MaterialColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.data.model.ImagePalette
import com.ilustris.sagai.core.theme.ResolvedGenreFonts
import com.ilustris.sagai.core.theme.rememberGenreThemeServices
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.filters.effectForGenre

const val SAGA_THEME_TRANSITION_MS = 500

private val DarkColorScheme =
    darkColorScheme(
        onPrimary = Color.White,
        primary = MaterialColor.BlueA400,
        secondary = MaterialColor.Blue400,
        tertiary = MaterialColor.Teal700,
        background = Color.Black,
        surfaceContainer = MaterialColor.Gray900,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = MaterialColor.Blue500,
        secondary = MaterialColor.Blue800,
        tertiary = MaterialColor.Teal300,
        onPrimary = MaterialColor.White,
        background = MaterialColor.White,
        surfaceContainer = MaterialColor.Gray50,
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
    val genre = LocalSagaGenre.current ?: return iridescentGradient
    val animatedPrimary = LocalAnimatedThemePrimary.current
    val palette = genre.colorPalette()
    if (animatedPrimary == Color.Unspecified || palette.isEmpty()) return palette
    return listOf(animatedPrimary) + palette.drop(1)
}

@Composable
@Deprecated("Use MorphingThemeIcon for interactive UI. Keep themeIcon() for Coil placeholders.")
fun themeIcon(): Painter {
    val genre = LocalSagaGenre.current
    val iconRes = genre?.icon ?: R.drawable.ic_spark
    return painterResource(iconRes)
}

@Composable
fun themeIconVector(): ImageVector {
    val genre = LocalSagaGenre.current
    return ImageVector.vectorResource(genre?.icon ?: R.drawable.ic_spark)
}

@Composable
fun themePainter() = painterResource(LocalSagaGenre.current?.icon ?: R.drawable.ic_spark)

@Composable
fun genreIconVector(genre: Genre): ImageVector = ImageVector.vectorResource(genre.icon)

@Composable
fun ThemeIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector = themeIconVector(),
    brush: Brush? = null,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null,
    glowBrush: Brush? = null,
    glowIntensity: Float = 0f,
    glowRadius: Dp = 14.dp,
    iconModifier: Modifier = Modifier,
) {
    val clampedGlow = glowIntensity.coerceIn(0f, 1f)
    val resolvedGlowBrush = glowBrush ?: brush

    Box(
        modifier = modifier.graphicsLayer { clip = false },
        contentAlignment = Alignment.Center,
    ) {
        if (clampedGlow > 0f && resolvedGlowBrush != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .gradientFill(resolvedGlowBrush)
                        .graphicsLayer {
                            clip = false
                            alpha = 0.8f * clampedGlow
                        }.blur(glowRadius * clampedGlow),
            )
        }
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (brush == null) tint else Color.Unspecified,
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(if (brush != null) Modifier.gradientFill(brush) else Modifier)
                    .then(iconModifier),
        )
    }
}

@Composable
fun Modifier.themeVfx(isPlaying: Boolean = true): Modifier {
    if (isPlaying.not()) return this
    val genre = LocalSagaGenre.current
    return this.genreVfx(genre)
}

@Composable
fun Modifier.themeFilter(
    useFallback: Boolean = false,
    selectiveHighlight: Boolean = false,
): Modifier {
    val genre = LocalSagaGenre.current
    return this.effectForGenre(genre, useFallBack = useFallback, enableSelectiveHighlight = selectiveHighlight)
}

private val themeColorAnimationSpec =
    tween<Color>(SAGA_THEME_TRANSITION_MS, easing = FastOutSlowInEasing)

private data class SagaThemeTargets(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val tertiaryContainer: Color,
    val background: Color,
    val surfaceContainer: Color,
    val onBackground: Color,
    val onSurface: Color,
    val cornerSize: Dp?,
)

private data class SagaThemeAnimationKey(
    val genre: Genre?,
    val darkTheme: Boolean,
    val targets: SagaThemeTargets,
)

private fun baseColorScheme(darkTheme: Boolean) = if (darkTheme) DarkColorScheme else LightColorScheme

private fun resolveCornerSize(
    genre: Genre?,
    visualConfig: GenreVisualConfig?,
): Dp? {
    if (genre == null) return null
    if (visualConfig == null) return null
    if (visualConfig.cornerSizeDp == 0) return 0.dp
    return visualConfig.cornerSizeDp.dp
}

private fun resolveSagaThemeTargets(
    genre: Genre?,
    visualConfig: GenreVisualConfig?,
    darkTheme: Boolean,
): SagaThemeTargets {
    val baseScheme = baseColorScheme(darkTheme)
    val genrePrimary = genre?.resolveColor(visualConfig)
    val primary = genrePrimary ?: baseScheme.primary
    return SagaThemeTargets(
        primary = primary,
        secondary = genrePrimary?.darker(.25f) ?: baseScheme.secondary,
        tertiary = genrePrimary?.lighter(.25f) ?: baseScheme.tertiary,
        onPrimary = genre?.resolveIconColor(visualConfig) ?: baseScheme.onPrimary,
        primaryContainer = primary.darker(.3f),
        tertiaryContainer = primary.lighter(.3f),
        background = baseScheme.background,
        surfaceContainer = baseScheme.surfaceContainer,
        onBackground = baseScheme.onBackground,
        onSurface = baseScheme.onSurface,
        cornerSize = resolveCornerSize(genre, visualConfig),
    )
}

/**
 * CompositionLocal providing the currently active [Genre].
 * - On genre-immersed screens (SagaDetail, Chat): holds the saga's genre.
 * - On genre-neutral screens (Home shell): holds null (brand defaults).
 *
 * Use [sagaShape], [sagaBrush], etc. to access genre-specific visuals
 * without manually passing the genre around.
 */
val LocalSagaGenre = compositionLocalOf<Genre?> { null }

/** Animated primary color from [SagAITheme]; drives [themeBrushColors] during crossfade. */
internal val LocalAnimatedThemePrimary = compositionLocalOf { Color.Unspecified }

@Composable
fun SagAITheme(
    genre: Genre? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Null in Compose Preview (see rememberGenreThemeServices) — every lookup below already
    // treats "not loaded yet" as a valid state, so a null service just means we stay there.
    val themeServices = rememberGenreThemeServices()
    val activeGenre = genre

    var activeVisualConfig by remember(activeGenre) {
        mutableStateOf(activeGenre?.let { themeServices?.visualConfigService?.peekVisualConfig(it) })
    }

    LaunchedEffect(activeGenre) {
        activeVisualConfig =
            activeGenre?.let { themeServices?.visualConfigService?.getVisualConfig(it) }
    }

    LaunchedEffect(activeGenre, activeVisualConfig) {
        val g = activeGenre ?: return@LaunchedEffect
        val config = activeVisualConfig ?: return@LaunchedEffect
        themeServices?.fontService?.ensureLoaded(g, config)
    }

    val genreForFonts = activeGenre
    val resolvedFonts by
        if (genreForFonts != null && themeServices != null) {
            themeServices.fontService.fontsFor(genreForFonts).collectAsState()
        } else {
            remember { mutableStateOf<ResolvedGenreFonts?>(null) }
        }

    val targets = resolveSagaThemeTargets(activeGenre, activeVisualConfig, darkTheme)
    val animationKey =
        SagaThemeAnimationKey(
            genre = activeGenre,
            darkTheme = darkTheme,
            targets = targets,
        )
    val transition = updateTransition(animationKey, label = "SagAITheme")

    val animatedPrimary by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themePrimary",
    ) { it.targets.primary }
    val animatedSecondary by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themeSecondary",
    ) { it.targets.secondary }
    val animatedTertiary by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themeTertiary",
    ) { it.targets.tertiary }
    val animatedOnPrimary by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themeOnPrimary",
    ) { it.targets.onPrimary }
    val animatedPrimaryContainer by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themePrimaryContainer",
    ) { it.targets.primaryContainer }
    val animatedTertiaryContainer by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themeTertiaryContainer",
    ) { it.targets.tertiaryContainer }
    val animatedBackground by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themeBackground",
    ) { it.targets.background }
    val animatedSurfaceContainer by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themeSurfaceContainer",
    ) { it.targets.surfaceContainer }
    val animatedOnBackground by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themeOnBackground",
    ) { it.targets.onBackground }
    val animatedOnSurface by transition.animateColor(
        transitionSpec = { themeColorAnimationSpec },
        label = "themeOnSurface",
    ) { it.targets.onSurface }

    val colorScheme =
        baseColorScheme(darkTheme).copy(
            primary = animatedPrimary,
            secondary = animatedSecondary,
            tertiary = animatedTertiary,
            onPrimary = animatedOnPrimary,
            primaryContainer = animatedPrimaryContainer,
            tertiaryContainer = animatedTertiaryContainer,
            background = animatedBackground,
            surfaceContainer = animatedSurfaceContainer,
            onBackground = animatedOnBackground,
            onSurface = animatedOnSurface,
        )

    // Dynamic Typography: remote fonts when loaded, else system default
    val dynamicTypography =
        remember(activeGenre, resolvedFonts) {
            if (activeGenre == null) {
                Typography
            } else {
                val headerFamily = resolvedFonts?.header ?: FontFamily.Default
                val bodyFamily = resolvedFonts?.body ?: FontFamily.Default
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

    // Dynamic Shapes: corner radii from remote config or genre defaults (buttons use extraLarge, etc.)
    val targetCorner = targets.cornerSize
    val animatedCorner by animateDpAsState(
        targetValue = targetCorner ?: 16.dp,
        animationSpec = tween(SAGA_THEME_TRANSITION_MS, easing = FastOutSlowInEasing),
        label = "themeCorner",
    )
    val dynamicShapes =
        targetCorner?.let {
            Shapes(
                extraSmall = RoundedCornerShape(animatedCorner * 0.1f),
                small = RoundedCornerShape(animatedCorner * 0.2f),
                medium = RoundedCornerShape(animatedCorner * 0.3f),
                large = RoundedCornerShape(animatedCorner * 0.4f),
                extraLarge = RoundedCornerShape(animatedCorner),
            )
        }

    CompositionLocalProvider(
        LocalSagaGenre provides activeGenre,
        LocalGenreVisualConfig provides activeVisualConfig,
        LocalAnimatedThemePrimary provides animatedPrimary,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = dynamicTypography,
            shapes = dynamicShapes ?: MaterialTheme.shapes,
            content = content,
        )
    }
}

/**
 * Layers an image-derived palette ([ImagePalette]) on top of whatever [SagAITheme] is already
 * active, overriding only `background`/`onBackground`/`surface`/`onSurface` — genre accents
 * (`primary`/`secondary`/`tertiary`, shapes, typography) are left untouched. Wraps content in a
 * [Surface] so descendant `Text`/`Icon` calls pick up the right color automatically via
 * [LocalContentColor], with no manual `contentColor` plumbing needed.
 *
 * When [imagePalette] is null (no image yet, or still loading), animates back to whatever
 * background/onBackground the enclosing theme already had — a no-op visually, not a fallback to
 * a hardcoded color.
 */
@Composable
fun PaletteTheme(
    imagePalette: ImagePalette?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val fallbackBackground = MaterialTheme.colorScheme.background
    val fallbackOnBackground = MaterialTheme.colorScheme.onBackground

    val animatedBackground by animateColorAsState(
        targetValue = imagePalette?.dominant ?: fallbackBackground,
        animationSpec = themeColorAnimationSpec,
        label = "paletteBackground",
    )
    val animatedOnBackground by animateColorAsState(
        targetValue = imagePalette?.onDominant ?: fallbackOnBackground,
        animationSpec = themeColorAnimationSpec,
        label = "paletteOnBackground",
    )

    val paletteColorScheme =
        MaterialTheme.colorScheme.copy(
            background = animatedBackground,
            primaryContainer = animatedBackground,
            primary = imagePalette?.vibrant ?: MaterialTheme.colorScheme.primary,
            onPrimary = imagePalette?.onVibrant ?: MaterialTheme.colorScheme.onPrimary,
            onBackground = animatedOnBackground,
            surface = animatedBackground,
            onSurface = animatedOnBackground,
        )

    MaterialTheme(
        colorScheme = paletteColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = animatedBackground,
            contentColor = animatedOnBackground,
        ) {
            content()
        }
    }
}

@Composable
fun ThemeCover(): String? {
    val genre = LocalSagaGenre.current
    val currentVisualConfig = LocalGenreVisualConfig.current

    return currentVisualConfig?.imageUrl
}

// ── Theme Extension Properties ─────────────────────────────────────────

/**
 * Access the current genre directly from the theme.
 */
val MaterialTheme.sagaGenre: Genre?
    @Composable
    @ReadOnlyComposable
    get() = LocalSagaGenre.current

/**
 * Access the current genre's visual configuration directly from the theme.
 */
val MaterialTheme.genreConfig: GenreVisualConfig?
    @Composable
    @ReadOnlyComposable
    get() = LocalGenreVisualConfig.current

/**
 * Returns the characteristic gradient brush for the current genre.
 */
val MaterialTheme.sagaBrush: Brush
    @Composable
    get() = sagaBrush()

/**
 * Returns the characteristic shape for the current genre.
 */
val MaterialTheme.sagaShape: Shape
    @Composable
    get() = sagaShape()

/**
 * Applies the genre's selective color highlight effect to this modifier.
 */
@Composable
fun Modifier.sagaHighlight(): Modifier =
    effectForGenre(
        LocalSagaGenre.current,
        LocalGenreVisualConfig.current,
        enableSelectiveHighlight = true,
    )

@Composable
fun Modifier.sagaShader(enableSelectiveHighlight: Boolean = false): Modifier =
    this.then(
        effectForGenre(
            LocalSagaGenre.current,
            LocalGenreVisualConfig.current,
            enableSelectiveHighlight = enableSelectiveHighlight,
        ),
    )

// ── Theme Extension Functions ─────────────────────────────────────────
// Use these on genre-immersed screens instead of manual genre extensions.

/**
 * Returns the current genre's shape, or [MaterialTheme.shapes.medium] on neutral screens.
 * Equivalent to `genre.shape()` but theme-driven.
 */
@Composable
fun sagaShape(): Shape = LocalSagaGenre.current.shape()

@Composable
fun themeBubble() = LocalSagaGenre.current.bubble(isNarrator = true)

/**
 * Returns a gradient [Brush] for the current genre, or the brand holographic gradient.
 * Equivalent to `genre.gradient()` but theme-driven.
 */
@Composable
fun sagaBrush(
    animated: Boolean = false,
    gradientType: GradientType = GradientType.LINEAR,
): Brush = gradientType.toBrush(themeBrushColors())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SagAIScaffold(
    title: String? = null,
    showTopBar: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    SagAITheme(darkTheme = darkTheme) {
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
    if (!rememberLifecycleAnimationsActive()) return

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
