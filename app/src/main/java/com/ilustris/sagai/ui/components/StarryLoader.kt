package com.ilustris.sagai.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Matrix
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutQuad
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.random.Random

private data class WarpStar(
    var x: Float,
    var y: Float,
    var z: Float,
    var baseRotation: Float = Random.nextFloat() * 360f,
    var rotationSpeed: Float = Random.nextFloat() * 1f - 0.5f, // Slower rotation
)

@Composable
fun StarryLoader(
    isLoading: Boolean,
    loadingMessage: String? = null,
    textStyle: TextStyle =
        MaterialTheme.typography.labelMedium.copy(
            textAlign = TextAlign.Center,
        ),
    subtitle: String? = null,
    subtitleStyle: TextStyle =
        MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
        ),
    brushColors: List<Color> = holographicGradient,
    useAsDialog: Boolean = true,
) {
    val setBlur = LocalBlurState.current
    DisposableEffect(isLoading && useAsDialog) {
        setBlur(isLoading)
        onDispose {
            setBlur(false)
        }
    }

    if (isLoading) {
        val paint =
            remember {
                Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 20f
                        maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
                    }
                }
            }

        val infiniteTransition = rememberInfiniteTransition(label = "border_animation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing), // Slower rotation for border
                    repeatMode = RepeatMode.Restart,
                ),
            label = "rotation",
        )

        val borderDraw: DrawScope.() -> Unit = {
            drawIntoCanvas { canvas ->
                val shader = (Brush.sweepGradient(brushColors) as ShaderBrush).createShader(size)
                val matrix = Matrix()
                matrix.setRotate(rotation, size.width / 2, size.height / 2)
                shader.setLocalMatrix(matrix)

                paint.asFrameworkPaint().shader = shader

                canvas.drawRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    paint = paint,
                )
            }
        }

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
                    dialogWindowProvider?.window?.setDimAmount(.2f)
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .reactiveShimmer(true, brushColors),
                ) {
                    val starsAlpha by animateFloatAsState(
                        targetValue = if (loadingMessage == null) 1f else .5f,
                        animationSpec = tween(1500, easing = EaseInOutQuad),
                    )

                    WarpSpeedStarField(
                        modifier =
                            Modifier
                                .alpha(starsAlpha)
                                .fillMaxSize(),
                        starColor = brushColors.firstOrNull() ?: Color.White,
                    )

                    Canvas(
                        modifier = Modifier.fillMaxSize(),
                        onDraw = borderDraw,
                    )

                    AnimatedContent(
                        loadingMessage,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                        transitionSpec = {
                            fadeIn(tween(800, easing = EaseInOutQuad)) togetherWith
                                slideOutVertically(
                                    animationSpec = tween(800, easing = EaseInOutQuad),
                                ) { it }
                        },
                    ) {
                        it?.let { message ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    message,
                                    style = textStyle,
                                    modifier = Modifier.alpha(.6f),
                                )
                                subtitle?.let {
                                    Text(
                                        it,
                                        style = subtitleStyle,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                val starsAlpha by animateFloatAsState(
                    targetValue = if (loadingMessage == null) 1f else .7f,
                    animationSpec = tween(1000, easing = EaseInOutQuad),
                )

                WarpSpeedStarField(
                    modifier =
                        Modifier
                            .alpha(starsAlpha)
                            .fillMaxSize()
                            .gradientFill(Brush.verticalGradient(brushColors)),
                    starColor = brushColors.firstOrNull() ?: Color.White,
                )

                Canvas(
                    modifier = Modifier.fillMaxSize(),
                    onDraw = borderDraw,
                )

                AnimatedContent(
                    loadingMessage,
                    modifier = Modifier.align(Alignment.Center),
                    transitionSpec = {
                        fadeIn(tween(800, easing = EaseInOutQuad)) togetherWith
                            slideOutVertically(
                                animationSpec = tween(800, easing = EaseInOutQuad),
                            ) { it }
                    },
                ) {
                    it?.let { message ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                message,
                                style = textStyle,
                            )
                            subtitle?.let {
                                Text(
                                    it,
                                    style = subtitleStyle,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WarpSpeedStarField(
    modifier: Modifier = Modifier,
    starColor: Color = MaterialTheme.colorScheme.onBackground,
    starCount: Int = 200, // More stars for density since they are smaller
) {
    val stars = remember { mutableStateListOf<WarpStar>() }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    // Animation constants - slower and smoother
    val speed = 300f // Reduced speed for organic feel
    val maxDepth = 2000f

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { nanos ->
                val time = nanos / 1_000_000L
                if (lastFrameTime == 0L) {
                    lastFrameTime = time
                }
                val deltaSeconds = (time - lastFrameTime) / 1000f
                lastFrameTime = time

                // Update stars
                stars.forEach { star ->
                    star.z -= speed * deltaSeconds
                    star.baseRotation += star.rotationSpeed

                    if (star.z <= 0) {
                        // Reset star to back
                        star.z = maxDepth
                        star.x = Random.nextFloat() * 4000f - 2000f
                        star.y = Random.nextFloat() * 4000f - 2000f
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Initialize stars if needed
        if (stars.isEmpty()) {
            repeat(starCount) {
                stars.add(
                    WarpStar(
                        x = Random.nextFloat() * 4000f - 2000f,
                        y = Random.nextFloat() * 4000f - 2000f,
                        z = Random.nextFloat() * maxDepth,
                    ),
                )
            }
        }

        stars.forEach { star ->
            val safeZ = star.z.coerceAtLeast(1f)

            val k = 500f // Field of View
            val px = (star.x / safeZ) * k + centerX
            val py = (star.y / safeZ) * k + centerY

            if (px in -50f..size.width + 50f && py in -50f..size.height + 50f) {
                val scale = (1f - (safeZ / maxDepth)).coerceIn(0f, 1f)
                // Much smaller stars: max 4.dp
                val maxStarSizePx = 4.dp.toPx()
                val starSize = scale * maxStarSizePx

                // Smoother fading logic
                val alpha =
                    if (safeZ < 400f) {
                        // Fade out smoothly as it approaches camera to avoid popping
                        (safeZ / 400f).coerceIn(0f, 1f)
                    } else {
                        // Fade in at the back
                        scale.coerceIn(0f, 1f)
                    }

                if (alpha > 0.05f) {
                    val color = starColor.copy(alpha = alpha)

                    rotate(degrees = star.baseRotation, pivot = Offset(px, py)) {
                        draw4PointStar(
                            center = Offset(px, py),
                            size = starSize,
                            color = color,
                            glow = true,
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.draw4PointStar(
    center: Offset,
    size: Float,
    color: Color,
    glow: Boolean = false,
) {
    // Draw Glow - True Neon Bloom using BlurMaskFilter
    if (glow) {
        drawIntoCanvas { canvas ->
            val glowPaint =
                Paint().apply {
                    this.color = color
                    // Use a brighter alpha for the glow core logic, but let the mask filter spread it
                    this.alpha = (color.alpha * 0.8f).coerceIn(0f, 1f)
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        style = android.graphics.Paint.Style.FILL
                        // The blur radius determines the spread of the glow
                        maskFilter = BlurMaskFilter(size * 1.5f, BlurMaskFilter.Blur.NORMAL)
                    }
                }

            // Draw a circle for the glow base
            canvas.drawCircle(center, size * 1.2f, glowPaint)
        }
    }

    // Core Star - Sharp 4-point (Draw this ON TOP of the glow)
    val sharpPath =
        Path().apply {
            val innerRadius = size * 0.2f
            moveTo(center.x, center.y - size)
            lineTo(center.x + innerRadius, center.y - innerRadius)
            lineTo(center.x + size, center.y)
            lineTo(center.x + innerRadius, center.y + innerRadius)
            lineTo(center.x, center.y + size)
            lineTo(center.x - innerRadius, center.y + innerRadius)
            lineTo(center.x - size, center.y)
            lineTo(center.x - innerRadius, center.y - innerRadius)
            close()
        }

    // Draw core with full opacity (or close to it) to simulate the hot light source
    drawPath(sharpPath, color.copy(alpha = (color.alpha + 0.2f).coerceIn(0f, 1f)))
}
