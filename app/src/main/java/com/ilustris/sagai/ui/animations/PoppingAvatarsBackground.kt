package com.ilustris.sagai.ui.animations

// ... other imports ...
import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring // Added import
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring // Added import
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.delay
import kotlin.random.Random

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PoppingAvatarsBackground(
    characters: List<Character>,
    genre: Genre,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 80.dp,
    popDuration: Long = 2000L,
    moveDuration: Int = 700,
    onCharacterPopped: (count: Int) -> Unit = {},
) {
    if (characters.isEmpty()) return

    val density = LocalDensity.current
    val avatarSizePx = remember(avatarSize) { with(density) { avatarSize.toPx() } }

    var activeCharacterIndex by remember { mutableIntStateOf(-1) }
    val characterAnimTargets = remember { mutableStateMapOf<String, Triple<Offset, Float, Float>>() }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val centerOffset =
            remember(constraints.maxWidth, constraints.maxHeight) {
                Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
            }

        LaunchedEffect(characters, constraints.maxWidth, constraints.maxHeight) {
            if (constraints.maxWidth == 0 || constraints.maxHeight == 0) return@LaunchedEffect

            characters.forEach { char ->
                val charIdStr = char.id.toString()
                if (!characterAnimTargets.containsKey(charIdStr)) {
                    characterAnimTargets[charIdStr] = Triple(centerOffset, 0f, 0f)
                }
            }

            for (i in characters.indices) {
                activeCharacterIndex = i
                onCharacterPopped(i + 1) // Invoke callback
                val newCenterCharacter = characters[i]
                val newCenterCharacterIdStr = newCenterCharacter.id.toString()

                for (j in 0 until i) {
                    val charToPush = characters[j]
                    val charToPushIdStr = charToPush.id.toString()
                    var candidateOffset: Offset
                    var attempts = 0
                    val maxAttempts = 10

                    val otherPushedTargets =
                        characterAnimTargets
                            .filterKeys { key ->
                                key != newCenterCharacterIdStr && key != charToPushIdStr
                            }.mapNotNull { entry ->
                                val (offset, scale, alpha) = entry.value
                                if (scale == 0.8f && alpha == 1f) offset else null
                            }

                    do {
                        candidateOffset =
                            Offset(
                                Random.nextFloat() * (constraints.maxWidth - avatarSizePx) + avatarSizePx / 2f,
                                Random.nextFloat() * (constraints.maxHeight - avatarSizePx) + avatarSizePx / 2f,
                            )
                        attempts++

                        val tooClose =
                            otherPushedTargets.any { existingOffset ->
                                val dx = existingOffset.x - candidateOffset.x
                                val dy = existingOffset.y - candidateOffset.y
                                (dx * dx + dy * dy) < ((avatarSizePx * 1.2f) * (avatarSizePx * 1.2f))
                            }

                        if (!tooClose) break
                    } while (attempts < maxAttempts)

                    characterAnimTargets[charToPushIdStr] =
                        Triple(
                            candidateOffset,
                            0.8f,
                            1f,
                        )
                }
                characterAnimTargets[newCenterCharacterIdStr] = Triple(centerOffset, 1.0f, 1f)
                delay(popDuration)
            }
        }

        characterAnimTargets.keys.mapNotNull { charId -> characters.find { it.id.toString() == charId } }.forEach { character ->
            val (targetOffset, targetScale, targetAlpha) =
                characterAnimTargets[character.id.toString()] ?: Triple(centerOffset, 0f, 0f)

            val animatedOffset by animateOffsetAsState(
                targetValue = targetOffset,
                animationSpec = tween(durationMillis = moveDuration, easing = LinearOutSlowInEasing),
                label = "offset_${character.id}",
            )
            val animatedScaleMain by animateFloatAsState(
                targetValue = targetScale,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                label = "scale_main_${character.id}",
            )
            val animatedAlpha by animateFloatAsState(
                targetValue = targetAlpha,
                animationSpec = tween(durationMillis = moveDuration / 2),
                label = "alpha_${character.id}",
            )

            val infiniteTransition = rememberInfiniteTransition(label = "infinite_anim_${character.id}")

            val breathingScaleMultiplier by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "breathing_scale_${character.id}",
            )

            val dynamicRotationAngle by infiniteTransition.animateFloat( // Renamed from rotationAngle
                initialValue = -8f,
                targetValue = 8f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 2200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "dynamic_rotation_angle_${character.id}",
            )

            val idleOffsetX by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = Random.nextInt(2500, 3500), easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "idle_offset_x_${character.id}",
            )

            val idleOffsetY by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = Random.nextInt(2500, 3500), easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "idle_offset_y_${character.id}",
            )

            // Static base rotation unique to each character
            val baseRandomRotation = remember(character.id) {
                Random.nextInt(-15, 16).toFloat() // Range -15 to 15 degrees
            }

            val isIdlePushedOut = targetScale == 0.8f && targetAlpha == 1f && targetOffset != centerOffset

            CharacterAvatar(
                character = character,
                genre = genre,
                modifier =
                    Modifier
                        .size(avatarSize)
                        .graphicsLayer {
                            var finalTranslationX = animatedOffset.x - avatarSizePx / 2f
                            var finalTranslationY = animatedOffset.y - avatarSizePx / 2f

                            if (isIdlePushedOut) {
                                scaleX = animatedScaleMain * breathingScaleMultiplier
                                scaleY = animatedScaleMain * breathingScaleMultiplier
                                rotationZ = baseRandomRotation + dynamicRotationAngle // Combined rotation
                                finalTranslationX += idleOffsetX
                                finalTranslationY += idleOffsetY
                            } else {
                                scaleX = animatedScaleMain
                                scaleY = animatedScaleMain
                                rotationZ = 0f
                            }
                            translationX = finalTranslationX
                            translationY = finalTranslationY
                            alpha = animatedAlpha
                        },
            )
        }
    }
}
