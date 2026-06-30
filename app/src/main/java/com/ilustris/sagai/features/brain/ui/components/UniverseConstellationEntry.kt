package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.animations.draw4PointCosmicStar
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.themeBrushColors

@Composable
fun UniverseConstellationEntry(
    completedActsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    genre: Genre? = null,
    magical: Boolean = false,
) {
    val defaultPalette = themeBrushColors()
    val palette = genre?.colorPalette() ?: defaultPalette
    val morphColors = morphingGradient(colors = palette)
    val labelBrush = remember(morphColors) { Brush.verticalGradient(morphColors) }
    val shimmerColors =
        remember(morphColors) {
            listOf(Color.Transparent) + morphColors + listOf(Color.Transparent)
        }
    val glowPrimary = genreUniverseGlow(genre)
    val glowSecondary = genreUniverseGlowSecondary(genre)
    val twinkle =
        if (rememberLifecycleAnimationsActive()) {
            rememberUniverseEntryTwinkle()
        } else {
            0.85f
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .then(
                        if (magical) {
                            Modifier.reactiveShimmer(
                                isPlaying = true,
                                shimmerColors = shimmerColors,
                            )
                        } else {
                            Modifier
                        },
                    ),
        ) {
            val centerX = size.width / 2f
            val centerY = size.height * 0.44f
            val horizontalSpread = size.minDimension * 0.34f
            val bottomOffset = size.height * 0.36f

            drawUniverseCosmicStar(
                center = Offset(centerX, centerY),
                isCenter = true,
                twinkle = twinkle,
                glowColor = glowPrimary,
                rotationDegrees = 0f,
            )

            if (completedActsCount >= 1) {
                drawUniverseCosmicStar(
                    center = Offset(centerX - horizontalSpread, centerY),
                    isCenter = false,
                    twinkle = twinkle * 0.92f,
                    glowColor = glowSecondary,
                    rotationDegrees = -12f,
                )
            }
            if (completedActsCount >= 2) {
                drawUniverseCosmicStar(
                    center = Offset(centerX + horizontalSpread, centerY),
                    isCenter = false,
                    twinkle = twinkle * 0.92f,
                    glowColor = glowSecondary,
                    rotationDegrees = 12f,
                )
            }
            if (completedActsCount >= 3) {
                drawUniverseCosmicStar(
                    center = Offset(centerX, centerY + bottomOffset),
                    isCenter = false,
                    twinkle = twinkle * 0.88f,
                    glowColor = glowSecondary,
                    rotationDegrees = 24f,
                )
            }
        }

        TextButton(onClick = onClick) {
            Text(
                text = stringResource(R.string.see_your_universe),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        brush = sagaBrush(),
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    ),
            )
        }
    }
}

private fun DrawScope.drawUniverseCosmicStar(
    center: Offset,
    isCenter: Boolean,
    twinkle: Float,
    glowColor: Color,
    rotationDegrees: Float,
) {
    val starSize = if (isCenter) 9.5f else 6.2f
    val coreAlpha = if (isCenter) 0.95f * twinkle else 0.9f
    val haloAlpha = if (isCenter) 0.92f * twinkle else 0.72f
    val glowBlur = if (isCenter) 2.1f else 1.65f
    val glowSpread = if (isCenter) 1.45f else 1.2f

    draw4PointCosmicStar(
        center = center,
        size = starSize,
        color = Color.White.copy(alpha = coreAlpha),
        glowColor = glowColor,
        glowAlpha = haloAlpha,
        glowBlurFactor = glowBlur,
        glowSpreadFactor = glowSpread,
        rotationDegrees = rotationDegrees,
    )

    if (isCenter) {
        draw4PointCosmicStar(
            center = center,
            size = starSize * 0.28f,
            color = Color.White.copy(alpha = 0.95f * twinkle),
            glowColor = glowColor.copy(alpha = 0.35f * twinkle),
            glowAlpha = 1f,
            glowBlurFactor = glowBlur * 0.6f,
            glowSpreadFactor = glowSpread * 0.7f,
            rotationDegrees = rotationDegrees + 18f,
        )
    }
}

private fun genreUniverseGlow(genre: Genre?): Color =
    when (genre) {
        Genre.FANTASY -> Color(0xFFB1A7F0)
        Genre.CYBERPUNK -> Color(0xFF90E0EF)
        Genre.HORROR -> Color(0xFF9B59B6)
        Genre.HEROES -> Color(0xFF64B5F6)
        Genre.CRIME -> Color(0xFFE57373)
        Genre.SHINOBI -> Color(0xFF81C784)
        Genre.SPACE_OPERA -> Color(0xFF4FC3F7)
        Genre.COWBOY -> Color(0xFFE8A838)
        Genre.PUNK_ROCK -> Color(0xFFFF7043)
        null -> Color(0xFF90E0EF)
    }

private fun genreUniverseGlowSecondary(genre: Genre?): Color =
    when (genre) {
        Genre.FANTASY -> Color(0xFF8B80D0)
        Genre.CYBERPUNK -> Color(0xFF4ECDC4)
        Genre.HORROR -> Color(0xFFBB86FC)
        Genre.HEROES -> Color(0xFF90CAF9)
        Genre.CRIME -> Color(0xFFFFAB91)
        Genre.SHINOBI -> Color(0xFFA5D6A7)
        Genre.SPACE_OPERA -> Color(0xFF81D4FA)
        Genre.COWBOY -> Color(0xFFFFCC80)
        Genre.PUNK_ROCK -> Color(0xFFFFAB91)
        null -> Color(0xFFB0BEC5)
    }

@Composable
private fun rememberUniverseEntryTwinkle(): Float {
    val twinkleTransition = rememberInfiniteTransition(label = "universe_entry_twinkle")
    val twinkle by twinkleTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "universe_entry_twinkle_alpha",
    )
    return twinkle
}
