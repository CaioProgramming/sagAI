package com.ilustris.sagai.ui.components

import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient

@Composable
fun StarryLoader(
    isLoading: Boolean,
    loadingMessage: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    brushColors: List<Color> = holographicGradient,
    useAsDialog: Boolean = true
) {
    val setBlur = LocalBlurState.current
    DisposableEffect(isLoading && useAsDialog) {
        setBlur(isLoading)
        onDispose {
            setBlur(false)
        }
    }

    if (isLoading) {
        if (useAsDialog) {
            Dialog(
                onDismissRequest = { },
                properties =
                    DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                        decorFitsSystemWindows = false,
                        usePlatformDefaultWidth = false,
                    ),
            ) {
                val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
                SideEffect {
                    dialogWindowProvider?.window?.setDimAmount(0f) // 0f is transparent, default is around 0.6f
                }
                Box(Modifier.fillMaxSize()) {
                    val starsAlpha by animateFloatAsState(
                        targetValue = if (loadingMessage == null) 1f else .7f,
                        animationSpec = tween(500),
                    )
                    StarryTextPlaceholder(
                        modifier =
                            Modifier
                                .alpha(starsAlpha)
                                .fillMaxSize()
                                .gradientFill(Brush.verticalGradient(brushColors)),
                    )

                    val infiniteTransition = rememberInfiniteTransition(label = "border_animation")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec =
                            infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart,
                            ),
                        label = "rotation",
                    )

                    Canvas(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .blur(15.dp),
                    ) {
                        drawRect(
                            brush =
                                object : ShaderBrush() {
                                    override fun createShader(size: Size): Shader {
                                        val shader =
                                            (sweepGradient(brushColors) as ShaderBrush).createShader(
                                                size,
                                            )
                                        val matrix = Matrix()
                                        matrix.setRotate(rotation, size.width / 2, size.height / 2)
                                        shader.setLocalMatrix(matrix)
                                        return shader
                                    }
                                },
                            style = Stroke(width = 10.dp.toPx()),
                        )
                    }

                    AnimatedContent(
                        loadingMessage,
                        modifier = Modifier.align(Alignment.Center),
                        transitionSpec = {
                            fadeIn(tween(500)) togetherWith slideOutVertically { it }
                        },
                    ) {
                        it?.let { message ->
                            Text(
                                message,
                                style = textStyle,
                            )
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                val starsAlpha by animateFloatAsState(
                    targetValue = if (loadingMessage == null) 1f else .7f,
                    animationSpec = tween(500),
                )
                StarryTextPlaceholder(
                    modifier =
                        Modifier
                            .alpha(starsAlpha)
                            .fillMaxSize()
                            .gradientFill(Brush.verticalGradient(brushColors)),
                )

                val infiniteTransition = rememberInfiniteTransition(label = "border_animation")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart,
                        ),
                    label = "rotation",
                )

                Canvas(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .blur(15.dp),
                ) {
                    drawRect(
                        brush =
                            object : ShaderBrush() {
                                override fun createShader(size: Size): Shader {
                                    val shader =
                                        (sweepGradient(brushColors) as ShaderBrush).createShader(
                                            size,
                                        )
                                    val matrix = Matrix()
                                    matrix.setRotate(rotation, size.width / 2, size.height / 2)
                                    shader.setLocalMatrix(matrix)
                                    return shader
                                }
                            },
                        style = Stroke(width = 10.dp.toPx()),
                    )
                }

                AnimatedContent(
                    loadingMessage,
                    modifier = Modifier.align(Alignment.Center),
                    transitionSpec = {
                        fadeIn(tween(500)) togetherWith slideOutVertically { it }
                    },
                ) {
                    it?.let { message ->
                        Text(
                            message,
                            style = textStyle,
                        )
                    }
                }
            }
        }
    }
}
