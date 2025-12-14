package com.ilustris.sagai.features.saga.chat.ui.components

import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.share.ui.CharacterCard
import com.ilustris.sagai.ui.components.LocalBlurState
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay

@Composable
fun CharacterRevealOverlay(
    character: CharacterContent?,
    sagaContent: SagaContent,
    onDismiss: () -> Unit
) {
    if (character == null) return

    val setBlur = LocalBlurState.current
    DisposableEffect(Unit) {
        setBlur(true)
        onDispose {
            setBlur(false)
        }
    }

    val genre = remember { sagaContent.data.genre }
    val brushColors = remember { genre.colorPalette() }

    LaunchedEffect(character) {
        delay(7000)
        onDismiss()
    }



    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = false,
            usePlatformDefaultWidth = false,
        )
    ) {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .reactiveShimmer(true)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "border_animation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "rotation"
            )

            val floatOffset by infiniteTransition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "floating"
            )

            val shape = genre.bubble(
                tailAlignment = BubbleTailAlignment.BottomLeft,
                0.dp,
                0.dp
            )

            Box(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(.65f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Um novo personagem juntou-se a história!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = genre.bodyFont(),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Box(Modifier
                        .weight(1f)
                        .offset(y = floatOffset.dp)) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(15.dp)
                        ) {
                            drawRect(
                                brush = object : ShaderBrush() {
                                    override fun createShader(size: Size): Shader {
                                        val shader =
                                            (sweepGradient(brushColors) as ShaderBrush).createShader(
                                                size
                                            )
                                        val matrix = Matrix()
                                        matrix.setRotate(rotation, size.width / 2, size.height / 2)
                                        shader.setLocalMatrix(matrix)
                                        return shader
                                    }
                                },
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }

                        CharacterCard(
                            character = character,
                            sagaContent = sagaContent,
                            modifier = Modifier
                                .fillMaxSize()
                                .dropShadow(
                                    shape,
                                    androidx.compose.ui.graphics.shadow.Shadow(
                                        10.dp,
                                        genre.gradient(),
                                    ),
                                )
                                .clip(shape)
                                .background(genre.color, shape),
                            showWatermark = false
                        )
                    }
                }

            }
        }

    }
}
