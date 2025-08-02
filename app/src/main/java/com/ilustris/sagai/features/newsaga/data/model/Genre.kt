package com.ilustris.sagai.features.newsaga.data.model

import ai.atick.material.MaterialColor
import android.annotation.SuppressLint
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.EaseOutCubic // Added
import androidx.compose.animation.core.animateDpAsState // Added
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams
import com.ilustris.sagai.ui.theme.gradient
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class
Genre(
    val title: String,
    val icon: Int,
    val color: Color,
    val iconColor: Color,
    @DrawableRes
    val background: Int,
    val ambientMusicConfigKey: String,
    val backgroundChars: List<String>,
) {
    FANTASY(
        title = "Fantasia",
        icon = R.drawable.fantasy_icon,
        color = MaterialColor.Red800,
        iconColor = Color.Companion.White,
        background = R.drawable.fantasy,
        ambientMusicConfigKey = "fantasy_ambient_music_url",
        backgroundChars = listOf("🐲", "🦄", "🏰", "⚔️", "🧙", "📜", "🛡️", "🐉", "✨", "👑"),
    ),
    SCI_FI(
        title = "Cyberpunk",
        icon = R.drawable.scifi_icon,
        color = MaterialColor.DeepPurpleA200,
        iconColor = Color.Companion.White,
        background = R.drawable.scifi,
        ambientMusicConfigKey = "scifi_ambient_music_url",
        backgroundChars = listOf("🤖", "🚀", "💻", "🌌", "👽", "👾", "📡", "🌠", "🛰️", "🦾"),
    ),
}

@StringRes
fun Genre.getNamePlaceholderResId(): Int =
    when (this) {
        Genre.FANTASY -> R.string.character_form_placeholder_name_fantasy
        Genre.SCI_FI -> R.string.character_form_placeholder_name_scifi
        else -> R.string.character_form_placeholder_name
    }

fun Genre.selectiveHighlight(): SelectiveColorParams =
    when (this) {
        Genre.FANTASY ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .05f,
                saturationThreshold = .1f,
                highlightSaturationBoost = 1.2f,
            )

        Genre.SCI_FI ->
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .25f,
                saturationThreshold = .15f,
                lightnessThreshold = .2f,
                highlightSaturationBoost = 2f,
                highlightLightnessBoost = 0f,
                desaturationFactorNonTarget = .35f,
            )
    }

private data class EmojiConfig(
    val char: String,
    val size: TextUnit,
    val offsetX: Dp,
    val offsetY: Dp,
    val rotation: Float,
    val boundingBox: RectF,
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Genre.viewBackground(modifier: Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current.density
        val containerWidthPx = constraints.maxWidth
        val containerHeightPx = constraints.maxHeight

        // Calculate center coordinates in Dp
        val centerXDp = remember(constraints.maxWidth, density) { (containerWidthPx / 2f / density).dp }
        val centerYDp = remember(constraints.maxHeight, density) { (containerHeightPx / 2f / density).dp }

        var emojiConfigs by remember { mutableStateOf<List<EmojiConfig>>(emptyList()) }
        var animatedEmojiCount by remember { mutableStateOf(0) }

        LaunchedEffect(this@viewBackground.backgroundChars) {
            val newConfigs = mutableListOf<EmojiConfig>()
            val targetEmojiCount = 35
            val maxPlacementAttemptsPerSlot = 10

            repeat(targetEmojiCount) {
                run attemptLoop@{
                    repeat(maxPlacementAttemptsPerSlot) {
                        val char = this@viewBackground.backgroundChars.random()
                        val sizeSp = Random.nextInt(36, 72).sp
                        val approxEmojiVisualDiameterPx = (sizeSp.value * density * 0.8f)
                        val candidateOffsetXpx =
                            Random
                                .nextInt(
                                    0,
                                    (containerWidthPx - approxEmojiVisualDiameterPx).toInt().coerceAtLeast(1),
                                ).toFloat()
                        val candidateOffsetYpx =
                            Random
                                .nextInt(
                                    0,
                                    (containerHeightPx - approxEmojiVisualDiameterPx).toInt().coerceAtLeast(1),
                                ).toFloat()
                        val candidateRotation = Random.nextFloat() * 90f - 45f
                        val candidateBoundingBox =
                            RectF(
                                candidateOffsetXpx,
                                candidateOffsetYpx,
                                candidateOffsetXpx + approxEmojiVisualDiameterPx,
                                candidateOffsetYpx + approxEmojiVisualDiameterPx,
                            )
                        var hasOverlap = false
                        for (existingConfig in newConfigs) {
                            if (RectF.intersects(candidateBoundingBox, existingConfig.boundingBox)) {
                                hasOverlap = true
                                break
                            }
                        }
                        if (!hasOverlap) {
                            newConfigs.add(
                                EmojiConfig(
                                    char = char,
                                    size = sizeSp,
                                    offsetX = (candidateOffsetXpx / density).dp,
                                    offsetY = (candidateOffsetYpx / density).dp,
                                    rotation = candidateRotation,
                                    boundingBox = candidateBoundingBox,
                                ),
                            )
                            return@attemptLoop
                        }
                    }
                }
            }
            emojiConfigs = newConfigs
            animatedEmojiCount = 0
        }

        LaunchedEffect(emojiConfigs.size) {
            if (emojiConfigs.isNotEmpty()) {
                if (animatedEmojiCount < emojiConfigs.size) {
                    for (i in animatedEmojiCount until emojiConfigs.size) {
                        delay(150L) // Stagger the start of each animation
                        animatedEmojiCount++
                    }
                } else if (animatedEmojiCount > emojiConfigs.size) {
                    animatedEmojiCount = emojiConfigs.size
                }
            }
        }

        emojiConfigs.forEachIndexed { index, config ->
            val isVisible = index < animatedEmojiCount

            val scale by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f, // Scale from 0 to 1
                animationSpec = tween(durationMillis = 400), // Slightly shorter scale anim
                label = "emojiScaleAnimation",
            )
            val alpha by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 800),
                label = "emojiAlphaAnimation",
            )
            val currentOffsetX by animateDpAsState(
                targetValue = if (isVisible) config.offsetX else centerXDp,
                animationSpec = tween(durationMillis = 700, easing = EaseOutCubic), // Longer movement
                label = "emojiOffsetX",
            )
            val currentOffsetY by animateDpAsState(
                targetValue = if (isVisible) config.offsetY else centerYDp,
                animationSpec = tween(durationMillis = 700, easing = EaseOutCubic),
                label = "emojiOffsetY",
            )

            Text(
                text = config.char,
                modifier =
                    Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .offset(x = currentOffsetX, y = currentOffsetY) // Use animated offsets
                        .rotate(config.rotation),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        brush = gradient(),
                        fontSize = config.size,
                    ),
            )
        }
    }
}

fun Genre.colorPalette() =
    when (this) {
        Genre.FANTASY ->
            listOf(
                MaterialColor.Red900,
                MaterialColor.Pink200,
                MaterialColor.DeepOrange600,
                MaterialColor.Red500,
            )
        Genre.SCI_FI ->
            listOf(
                MaterialColor.Purple500,
                MaterialColor.Indigo600,
                MaterialColor.DeepPurple800,
                MaterialColor.DeepPurpleA100,
            )
    }
