package com.ilustris.sagai.ui.animations

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.lighter
import kotlin.random.Random

@Composable
fun Modifier.glitch(
    isPlaying: Boolean = true,
    glitchFrequency: Float = 0.15f,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "glitch")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(120, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "glitchTicker",
        )

        val isGlitching = remember(ticker) { Random.nextFloat() < glitchFrequency }
        val glitchSeed = remember(ticker) { Random.nextLong() }

        this.drawWithContent {
            if (isGlitching) {
                val random = Random(glitchSeed)

                // 1. Chromatic Aberration-ish displacement (Red/Cyan shifts)
                drawIntoCanvas { canvas ->
                    canvas.save()
                    canvas.translate((random.nextFloat() - 0.5f) * 15f, 0f)
                    drawContent()
                    canvas.restore()
                }

                // 2. Draw "pixel blocks" or data corruption blocks
                val blockCount = random.nextInt(4, 10)
                repeat(blockCount) {
                    val blockWidth = random.nextFloat() * size.width * 0.4f
                    val blockHeight = random.nextFloat() * 12f + 2f
                    val blockX = random.nextFloat() * (size.width - blockWidth)
                    val blockY = random.nextFloat() * size.height

                    val blockColor =
                        when (random.nextInt(3)) {
                            0 -> Color.Cyan.copy(alpha = 0.6f)
                            1 -> Color.Magenta.copy(alpha = 0.6f)
                            else -> Color.Yellow.copy(alpha = 0.6f)
                        }

                    drawRect(
                        color = blockColor,
                        topLeft = Offset(blockX, blockY),
                        size = Size(blockWidth, blockHeight),
                        blendMode = BlendMode.Screen,
                    )
                }

                // 3. Scanline/Noise interference
                if (random.nextFloat() < 0.4f) {
                    val noiseHeight = 2f
                    repeat(5) {
                        val noiseY = random.nextFloat() * size.height
                        drawRect(
                            color = Color.White.copy(alpha = 0.2f),
                            topLeft = Offset(0f, noiseY),
                            size = Size(size.width, noiseHeight),
                        )
                    }
                }
            } else {
                drawContent()
            }
        }
    }

@Composable
fun Modifier.chromaticAberration(
    isPlaying: Boolean = true,
    intensity: Float = 3f,
    blurRadius: Float = 4f,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "chromatic")

        val pulse by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "pulse",
        )

        this.drawWithContent {
            val shift = intensity * pulse
            val softBlur = (blurRadius * pulse).coerceAtLeast(0.1f)

            drawIntoCanvas { canvas ->
                // 1. Draw Red Ghost (Left)
                val redPaint =
                    androidx.compose.ui.graphics.Paint().apply {
                        colorFilter =
                            ColorFilter
                                .tint(Color.Red.copy(alpha = 0.5f))
                        blendMode = BlendMode.Screen
                        asFrameworkPaint().maskFilter =
                            android.graphics.BlurMaskFilter(
                                softBlur,
                                android.graphics.BlurMaskFilter.Blur.NORMAL,
                            )
                    }
                canvas.save()
                canvas.translate(-shift, 0f)
                canvas.saveLayer(
                    androidx.compose.ui.geometry
                        .Rect(0f, 0f, size.width, size.height),
                    redPaint,
                )
                drawContent()
                canvas.restore()
                canvas.restore()

                // 2. Draw Cyan Ghost (Right)
                val cyanPaint =
                    androidx.compose.ui.graphics.Paint().apply {
                        colorFilter =
                            ColorFilter
                                .tint(Color.Cyan.copy(alpha = 0.5f))
                        blendMode = BlendMode.Screen
                        asFrameworkPaint().maskFilter =
                            android.graphics.BlurMaskFilter(
                                softBlur,
                                android.graphics.BlurMaskFilter.Blur.NORMAL,
                            )
                    }
                canvas.save()
                canvas.translate(shift, 0f)
                canvas.saveLayer(
                    androidx.compose.ui.geometry
                        .Rect(0f, 0f, size.width, size.height),
                    cyanPaint,
                )
                drawContent()
                canvas.restore()
                canvas.restore()
            }

            // 3. Draw Core Content
            drawContent()
        }
    }

@Composable
fun Modifier.sparkle(
    isPlaying: Boolean = true,
    sparkleCount: Int = 10,
    color: Color = Color.White,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "sparkleTicker",
        )

        this.drawBehind {
            val random = Random(ticker.hashCode())
            repeat(sparkleCount) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val scale = random.nextFloat()
                val alpha =
                    (
                        kotlin.math
                            .sin(ticker * 2 * kotlin.math.PI + random.nextFloat() * 10)
                            .toFloat() + 1f
                    ) / 2f

                drawCircle(
                    color = color.copy(alpha = alpha * 0.8f),
                    radius = 2f * scale,
                    center = Offset(x, y),
                )

                // Draw a tiny cross for that "twinkle" look
                val crossSize = 4f * scale * alpha
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = Offset(x - crossSize, y),
                    end = Offset(x + crossSize, y),
                    strokeWidth = 1f,
                )
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = Offset(x, y - crossSize),
                    end = Offset(x, y + crossSize),
                    strokeWidth = 1f,
                )
            }
        }
    }

@Composable
fun Modifier.dreamySparkle(
    isPlaying: Boolean = true,
    sparkleCount: Int = 8,
    color: Color = Color.White,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "dreamySparkle")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "sparkleTicker",
        )

        this.drawWithContent {
            drawContent()
            val random = Random(42) // Constant seed for stable random positions

            repeat(sparkleCount) { i ->
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height

                // Individual lifecycle for each spark using the ticker
                // We use different multipliers to make them pulse at different speeds
                val individualSpeed = 0.5f + (i % 3) * 0.2f
                val phaseOffset = (i * 0.15f)
                val rawPulse =
                    (
                        kotlin.math
                            .sin((ticker * individualSpeed + phaseOffset) * 2 * kotlin.math.PI)
                            .toFloat() + 1f
                    ) / 2f

                // Narrow the pulse so they stay invisible longer (shimmer effect)
                val pulse = kotlin.math.max(0f, (rawPulse - 0.6f) / 0.4f)

                if (pulse > 0f) {
                    val center = Offset(x, y)
                    val glowSize = 15f + (i % 5) * 5f
                    val starRange = glowSize * 1.5f * pulse
                    val radius = (glowSize * pulse).coerceAtLeast(0.1f)

                    // 1. Soft Central Glow
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        color.copy(alpha = pulse * 0.6f),
                                        color.copy(alpha = pulse * 0.2f),
                                        Color.Transparent,
                                    ),
                                center = center,
                                radius = radius,
                            ),
                        radius = radius,
                        center = center,
                        blendMode = BlendMode.Screen,
                    )

                    // 2. Soft "Glimmer" Cross (Diffuse star lines)
                    // Horizontal
                    drawRect(
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        color.copy(alpha = pulse * 0.4f),
                                        Color.Transparent,
                                    ),
                                startX = x - starRange,
                                endX = x + starRange,
                            ),
                        topLeft = Offset(x - starRange, y - 1f),
                        size =
                            Size(starRange * 2, 2f),
                        blendMode = BlendMode.Screen,
                    )
                    // Vertical
                    drawRect(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        color.copy(alpha = pulse * 0.4f),
                                        Color.Transparent,
                                    ),
                                startY = y - starRange,
                                endY = y + starRange,
                            ),
                        topLeft = Offset(x - 1f, y - starRange),
                        size =
                            Size(2f, starRange * 2),
                        blendMode = BlendMode.Screen,
                    )
                }
            }
        }
    }

@Composable
fun Modifier.aura(
    isPlaying: Boolean = true,
    color: Color = Color.White,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "aura")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "auraPulse",
        )

        this.drawBehind {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                        center = center,
                        radius = size.maxDimension * pulse,
                    ),
                radius = size.maxDimension * pulse,
                center = center,
                blendMode = BlendMode.Screen,
            )
        }
    }

@Composable
fun Modifier.distort(
    isPlaying: Boolean = true,
    intensity: Float = 5f,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "distort")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * kotlin.math.PI.toFloat(),
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "distortTicker",
        )

        this.graphicsLayer {
            translationY = kotlin.math.sin(ticker).toFloat() * intensity
            rotationZ = kotlin.math.cos(ticker).toFloat() * (intensity / 2f)
            scaleX = 1f + kotlin.math.sin(ticker * 2).toFloat() * 0.02f
            scaleY = 1f + kotlin.math.cos(ticker * 2).toFloat() * 0.02f
        }
    }

@Composable
fun Modifier.vhs(isPlaying: Boolean = true): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "vhs")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(5000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "vhsTicker",
        )

        this
            .drawWithContent {
                val time = ticker * 2 * kotlin.math.PI.toFloat()

                // 1. Dreamy Horizontal displacement (Classic VHS wobble)
                val wobbleX = kotlin.math.sin(time * 3f).toFloat() * 10f
                val wobbleY = kotlin.math.cos(time * 1.5f).toFloat() * 5f

                // Utility to draw tinted layer clipped to text
                fun drawTintedGhost(
                    dx: Float,
                    dy: Float,
                    tint: Color,
                    alpha: Float,
                ) {
                    drawIntoCanvas { canvas ->
                        val paint =
                            androidx.compose.ui.graphics
                                .Paint()
                        paint.blendMode = BlendMode.Plus

                        // We use saveLayer to isolate the color replacement (SrcIn)
                        // so it only affects the text content of this ghost, not the background.
                        canvas.saveLayer(
                            androidx.compose.ui.geometry
                                .Rect(0f, 0f, size.width, size.height),
                            paint,
                        )
                        canvas.translate(dx, dy)
                        drawContent()
                        // Replaces the ghost's pixels with the tint
                        drawRect(color = tint.copy(alpha = alpha), blendMode = BlendMode.SrcIn)
                        canvas.restore()
                    }
                }

                // Color Shift 1: Red/Orange Hallucination
                drawTintedGhost(wobbleX, wobbleY, Color(0xFFFF5722), 0.4f)

                // Color Shift 2: Cyan Distortion
                drawTintedGhost(-wobbleX * 0.8f, -wobbleY * 1.2f, Color(0xFF00BCD4), 0.3f)

                // 2. Dreamy Bloom (Ultra soft offset replicas)
                drawTintedGhost(wobbleX * 0.5f + 4f, wobbleY * 0.5f + 4f, Color.White, 0.12f)

                // 3. Main content on top
                drawContent()
            }
            .graphicsLayer {
                // Wavy scale/rotation from Horror for that unstable feeling
                val time = ticker * 2 * kotlin.math.PI.toFloat()
                scaleX = 1f + kotlin.math.sin(time * 2f).toFloat() * 0.02f
                scaleY = 1f + kotlin.math.cos(time * 1.5f).toFloat() * 0.02f
                rotationZ = kotlin.math.sin(time * 0.5f).toFloat() * 2f
            }
    }

@Composable
fun Modifier.inkBleed(
    isPlaying: Boolean = true,
    color: Color = Color.Unspecified,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "inkBleed")

        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "inkTicker",
        )

        MaterialTheme.colorScheme.onBackground
        val inkColor =
            if (color == Color.Unspecified || color == Color.Black || color == Color.White) {
                MaterialTheme.colorScheme.primary
            } else {
                color
            }

        this.drawWithContent {
            drawContent()
            val blendMode = BlendMode.Difference

            repeat(3) { i ->
                val phaseOffset = i * 0.33f
                val progress = (ticker + phaseOffset) % 1f
                val activeWindow = 0.45f
                val internalProgress = progress / activeWindow

                if (progress < activeWindow) {
                    val cycleId = (ticker + phaseOffset).toInt()
                    val random = Random(cycleId + (i * 5000))

                    val alpha =
                        when {
                            internalProgress < 0.1f -> 1f
                            internalProgress < 0.7f -> 1f
                            else -> 1f - ((internalProgress - 0.7f) / 0.3f)
                        }

                    if (alpha > 0f) {
                        // Directional Slash properties
                        val isLtr = random.nextBoolean()
                        val strokeWidth = 15f + random.nextFloat() * 35f
                        val sweepLength = size.width * (0.3f + random.nextFloat() * 0.25f)

                        val centerX = size.width * (0.15f + random.nextFloat() * 0.7f)
                        val startX =
                            if (isLtr) centerX - (sweepLength / 2) else centerX + (sweepLength / 2)
                        val endX =
                            if (isLtr) centerX + (sweepLength / 2) else centerX - (sweepLength / 2)

                        val startY = size.height * (0.2f + random.nextFloat() * 0.6f)
                        val endY = startY + (random.nextFloat() - 0.5f) * size.height * 0.3f
                        val midY =
                            (startY + endY) / 2 + (random.nextFloat() - 0.5f) * size.height * 0.5f

                        val path =
                            androidx.compose.ui.graphics
                                .Path()
                        val segments = 40

                        for (step in 0..segments) {
                            val t = step.toFloat() / segments
                            val invT = 1f - t
                            val x =
                                invT * invT * startX + 2 * invT * t * ((startX + endX) / 2) + t * t * endX
                            val y = invT * invT * startY + 2 * invT * t * midY + t * t * endY

                            // Katana feel: Jitter that gets 'sharper' or 'thinner' along the path
                            val jitterMagnitude = strokeWidth * (0.5f + (1f - t) * 0.5f)
                            val jitterX = (random.nextFloat() - 0.5f) * jitterMagnitude * 0.5f
                            val jitterY = (random.nextFloat() - 0.5f) * jitterMagnitude * 0.8f

                            if (step == 0) {
                                path.moveTo(x + jitterX, y + jitterY)
                            } else {
                                path.lineTo(x + jitterX, y + jitterY)
                            }
                        }

                        drawPath(
                            path = path,
                            color = inkColor.copy(alpha = alpha * 0.85f),
                            style =
                                androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = strokeWidth,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Butt, // Sharper ends
                                    join = androidx.compose.ui.graphics.StrokeJoin.Miter,
                                ),
                            blendMode = blendMode,
                        )

                        // Momemtum flick: Droplets ONLY at the end of the stroke
                        repeat(15) { j ->
                            val dripSeed = Random(random.nextInt() + j)
                            // Biased towards the end of the path (t > 0.8)
                            val tEnd = 0.85f + dripSeed.nextFloat() * 0.15f
                            val invT = 1f - tEnd
                            val xEnd =
                                invT * invT * startX + 2 * invT * tEnd * ((startX + endX) / 2) + tEnd * tEnd * endX
                            val yEnd =
                                invT * invT * startY + 2 * invT * tEnd * midY + tEnd * tEnd * endY

                            // Flick direction: Continue the path's momentum
                            val dx = endX - startX
                            val dy = endY - startY
                            val angleBase = kotlin.math.atan2(dy, dx)
                            val sprayAngle = angleBase + (dripSeed.nextFloat() - 0.5f) * 0.8f

                            val sprayDist = strokeWidth * (0.5f + dripSeed.nextFloat() * 3.5f)
                            val rDot = 1.5f + dripSeed.nextFloat() * 4f

                            drawCircle(
                                color = inkColor.copy(alpha = alpha * 0.75f),
                                radius = rDot,
                                center =
                                    Offset(
                                        xEnd + kotlin.math.cos(sprayAngle.toFloat()) * sprayDist,
                                        yEnd + kotlin.math.sin(sprayAngle.toFloat()) * sprayDist,
                                    ),
                                blendMode = blendMode,
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun Modifier.livingTorch(
    isPlaying: Boolean = true,
    fireColor: Color = Color(0xFFFF5722),
): Modifier =
    composed {
        if (!isPlaying) return@composed this

        // We chain the new chromatic aberration for that premium "Cosmic" feel
        this
            .chromaticAberration(isPlaying, intensity = 4f, blurRadius = 15f)
            .composed {
                val infiniteTransition = rememberInfiniteTransition(label = "etherealTorch")

                val ticker by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart,
                        ),
                    label = "gasTicker",
                )

                val pulse by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    label = "pulseTicker",
                )

                this.drawWithContent {
                    val time = ticker * 2 * kotlin.math.PI.toFloat()

                    // 1. Ethereal Rising Blur
                    drawIntoCanvas { canvas ->
                        repeat(3) { i ->
                            val progress = (i + 1) / 3f
                            val invProgress = 1f - progress
                            val xOff = kotlin.math.sin(time + i) * (18f * progress)
                            val yOff = -progress * 60f * pulse

                            val layerPaint =
                                androidx.compose.ui.graphics.Paint().apply {
                                    colorFilter =
                                        ColorFilter.tint(
                                            fireColor.copy(alpha = 0.3f * invProgress * pulse),
                                        )
                                    blendMode = BlendMode.Plus
                                    asFrameworkPaint().maskFilter =
                                        android.graphics.BlurMaskFilter(
                                            (20f * progress + 6f).coerceAtLeast(0.1f),
                                            android.graphics.BlurMaskFilter.Blur.NORMAL,
                                        )
                                }
                            canvas.save()
                            canvas.translate(xOff, yOff)
                            canvas.saveLayer(
                                androidx.compose.ui.geometry.Rect(
                                    0f,
                                    0f,
                                    size.width,
                                    size.height,
                                ),
                                layerPaint,
                            )
                            drawContent()
                            canvas.restore()
                            canvas.restore()
                        }
                    }

                    // 2. Core Solid Text
                    drawContent()

                    // 3. Ethereal Inner Glow (White-hot core)
                    drawIntoCanvas { canvas ->
                        val glowPaint =
                            androidx.compose.ui.graphics.Paint().apply {
                                colorFilter =
                                    ColorFilter
                                        .tint(Color.White.copy(alpha = 0.15f * pulse))
                                blendMode = BlendMode.Plus
                            }
                        canvas.saveLayer(
                            androidx.compose.ui.geometry.Rect(
                                0f,
                                0f,
                                size.width,
                                size.height,
                            ),
                            glowPaint,
                        )
                        drawContent()
                        canvas.restore()
                    }

                    // 4. Draw Rising "Living" Sparks (Ember System)
                    drawIntoCanvas { canvas ->
                        val sparkCount = 14
                        val random = Random(42)
                        repeat(sparkCount) { i ->
                            val seed = random.nextFloat()
                            val sparkProgress = (ticker + seed) % 1.0f
                            val invProgress = 1f - sparkProgress
                            val startX = size.width * (0.05f + seed * 0.9f)
                            val baseY = size.height * 0.4f
                            val y = baseY - (sparkProgress * 220f)
                            val xDrift =
                                (
                                        kotlin.math.sin(time * 2.5f + seed * 12f) * 35f +
                                                kotlin.math.sin(time * 5f + seed * 8f) * 10f
                                        ).toFloat() * sparkProgress

                            val sparkWidth = (2.5f + seed * 3f) * invProgress
                            val sparkHeight = sparkWidth * (1.5f + sparkProgress * 2f)
                            val emberColor = if (seed > 0.6f) Color(0xFFFFCC00) else fireColor

                            canvas.save()
                            canvas.translate(startX + xDrift, y)
                            canvas.rotate(xDrift * 0.5f)

                            val mantlePaint =
                                androidx.compose.ui.graphics.Paint().apply {
                                    color = emberColor
                                    alpha = invProgress * pulse
                                    blendMode = BlendMode.Plus
                                    asFrameworkPaint().maskFilter =
                                        android.graphics.BlurMaskFilter(
                                            (sparkWidth * 0.8f).coerceAtLeast(0.1f),
                                            android.graphics.BlurMaskFilter.Blur.NORMAL,
                                        )
                                }
                            canvas.drawOval(
                                rect =
                                    androidx.compose.ui.geometry.Rect(
                                        -sparkWidth,
                                        -sparkHeight,
                                        sparkWidth,
                                        sparkHeight,
                                    ),
                                paint = mantlePaint,
                            )

                            if (invProgress > 0.3f) {
                                val corePaint =
                                    androidx.compose.ui.graphics.Paint().apply {
                                        color = Color.White
                                        alpha = (invProgress - 0.3f) * 1.4f * pulse
                                        blendMode = BlendMode.Plus
                                    }
                                canvas.drawOval(
                                    rect =
                                        androidx.compose.ui.geometry.Rect(
                                            -sparkWidth * 0.4f,
                                            -sparkHeight * 0.5f,
                                            sparkWidth * 0.4f,
                                            sparkHeight * 0.5f,
                                        ),
                                    paint = corePaint,
                                )
                            }
                            canvas.restore()
                        }
                    }
                }
            }
    }

private fun Color.lerp(
    target: Color,
    fraction: Float,
): Color =
    Color(
        red = red + (target.red - red) * fraction,
        green = green + (target.green - green) * fraction,
        blue = blue + (target.blue - blue) * fraction,
        alpha = alpha + (target.alpha - alpha) * fraction,
    )

private fun Brush.makeShader(size: Size): android.graphics.Shader {
    // Helper to generate a shader from a Brush if needed for canvas drawing
    return when (this) {
        is androidx.compose.ui.graphics.LinearGradient -> {
            // Approximation or use the native Brush.applyTo
            android.graphics.LinearGradient(
                0f,
                -260f,
                0f,
                size.height * 0.5f,
                intArrayOf(0, 0xFFFF5722.toInt(), 0xFFFFD600.toInt(), 0xFFFFFFFF.toInt()),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
        }

        else -> {
            android.graphics.LinearGradient(
                0f,
                0f,
                0f,
                size.height,
                intArrayOf(0, 0),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
        }
    }
}

@Composable
fun Modifier.heatHaze(
    isPlaying: Boolean = true,
    intensity: Float = 4f,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "heatHaze")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * kotlin.math.PI.toFloat(),
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "heatTicker",
        )

        this.graphicsLayer {
            val wave = kotlin.math.sin(ticker).toFloat()
            translationY = wave * intensity
            scaleX = 1f + (wave * 0.02f)
            scaleY = 1f + (wave * 0.015f)
        }
    }

@Composable
fun Modifier.heroChrome(
    isPlaying: Boolean = true,
    color: Color = Color.White,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "heroChrome")

        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "chromeTicker",
        )

        this.drawWithContent {
            // Increased sweep range to 6x width to create more separation (dead time) between glints
            val sweepX = ticker * (size.width * 6f) - (size.width * 2.5f)

            drawIntoCanvas { canvas ->
                // Force a layer to ensure SrcAtop doesn't bleed to the background
                canvas.saveLayer(
                    androidx.compose.ui.geometry
                        .Rect(0f, 0f, size.width, size.height),
                    androidx.compose.ui.graphics
                        .Paint(),
                )

                drawContent()

                // Cartoonish "Wide Metal Reflection"
                // Using hard stops to avoid smooth gradients, creating a "comic ink" feel
                val chromeBrush =
                    Brush.linearGradient(
                        0.35f to Color.Transparent,
                        0.40f to color.copy(alpha = 0.05f),
                        0.45f to color.copy(alpha = 0.15f),
                        0.48f to color.copy(alpha = 0.85f), // Lead Glint
                        0.50f to color.copy(alpha = 0.2f),
                        0.52f to color.copy(alpha = 0.95f), // Second hot glint
                        0.55f to color.copy(alpha = 0.15f),
                        0.60f to color.copy(alpha = 0.05f),
                        0.65f to Color.Transparent,
                        start = Offset(sweepX, 0f),
                        end = Offset(sweepX + size.width, size.height),
                    )

                drawRect(
                    brush = chromeBrush,
                    blendMode = BlendMode.SrcAtop,
                )

                canvas.restore()
            }
        }
    }

@Composable
fun Modifier.comicLines(
    isPlaying: Boolean = true,
    color: Color = Color.White,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "comicLines")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "comicTicker",
        )

        this.drawBehind {
            val random = Random(ticker.hashCode())
            val center = Offset(size.width / 2, size.height / 2)

            repeat(12) { i ->
                val angle = (i * 30f) + (random.nextFloat() * 15f)
                val angleRad = Math.toRadians(angle.toDouble()).toFloat()

                val innerRadius = size.maxDimension * 0.3f
                val outerRadius = size.maxDimension * (0.6f + random.nextFloat() * 0.4f)

                val start =
                    Offset(
                        center.x + kotlin.math.cos(angleRad) * innerRadius,
                        center.y + kotlin.math.sin(angleRad) * innerRadius,
                    )
                val end =
                    Offset(
                        center.x + kotlin.math.cos(angleRad) * outerRadius,
                        center.y + kotlin.math.sin(angleRad) * outerRadius,
                    )

                drawLine(
                    color = color.copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = 3f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
        }
    }

@Composable
fun Modifier.spaceVoyage(isPlaying: Boolean = true): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "spaceVoyage")

        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "vhsTicker",
        )

        val noiseTicker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(150, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "noiseTicker",
        )

        this
            .chromaticAberration(isPlaying, intensity = 8f, blurRadius = 3f)
            .drawWithContent {
                val time = ticker * 2 * kotlin.math.PI.toFloat()
                val noiseRandom = Random(noiseTicker.hashCode())

                // 1. Draw Phosphor Glow (Base)
                drawIntoCanvas { canvas ->
                    val glowPaint =
                        androidx.compose.ui.graphics.Paint().apply {
                            colorFilter =
                                ColorFilter.tint(
                                    Color.White.copy(alpha = 0.15f),
                                )
                            blendMode = BlendMode.Plus
                            asFrameworkPaint().maskFilter =
                                android.graphics.BlurMaskFilter(
                                    15f,
                                    android.graphics.BlurMaskFilter.Blur.NORMAL,
                                )
                        }
                    canvas.saveLayer(
                        androidx.compose.ui.geometry
                            .Rect(0f, 0f, size.width, size.height),
                        glowPaint,
                    )
                    drawContent()
                    canvas.restore()
                }

                // 2. Main Content with Jitter Logic
                drawIntoCanvas { canvas ->
                    val segments = 12
                    val segmentHeight = size.height / segments
                    repeat(segments) { i ->
                        val jitterX =
                            if (noiseRandom.nextFloat() < 0.15f) {
                                (noiseRandom.nextFloat() - 0.5f) * 15f
                            } else {
                                kotlin.math.sin(time * 5f + i) * 3f
                            }

                        canvas.save()
                        canvas.clipRect(0f, i * segmentHeight, size.width, (i + 1) * segmentHeight)
                        canvas.translate(jitterX, 0f)
                        drawContent()
                        canvas.restore()
                    }
                }

                // 3. Scanlines Overlay
                val scanlineCount = 45
                val scanlineHeight = size.height / scanlineCount
                repeat(scanlineCount) { i ->
                    val y = i * scanlineHeight
                    val alpha = 0.1f + kotlin.math.sin(time * 2f + i * 0.5f) * 0.05f
                    drawRect(
                        color = Color.Black.copy(alpha = alpha.coerceIn(0f, 0.25f)),
                        topLeft = Offset(0f, y),
                        size = Size(size.width, scanlineHeight * 0.5f),
                    )
                }

                // 4. Random VHS "Interference" blocks
                if (noiseRandom.nextFloat() < 0.1f) {
                    val blockY = noiseRandom.nextFloat() * size.height
                    val blockHeight = 2f + noiseRandom.nextFloat() * 15f
                    drawRect(
                        color = Color.White.copy(alpha = 0.15f),
                        topLeft = Offset(0f, blockY),
                        size = Size(size.width, blockHeight),
                        blendMode = BlendMode.Overlay,
                    )
                }
            }
    }

@Composable
fun Modifier.cowboyBurn(isPlaying: Boolean = true): Modifier =
    composed {
        MaterialTheme.colorScheme.background
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "cowboyBurn")

        // 1. Desperado Ticker (15 FPS stop-motion feel)
        val jitterFrame by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(66, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "jitterTicker",
        )

        // 2. Heat Wave Ticker
        val heatTicker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "heatTicker",
        )

        // 3. Expansion Ticker (15 seconds for a slow, centered breathe)
        val expansionTicker by
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(15000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "expansionTicker",
            )

        // 4. Burn Pulse (Flickering intensity)
        val burnPulse by
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "burnPulse",
            )

        // Unique rip properties (tightened to stay near center)
        remember { Random.nextFloat() * 0.1f - 0.05f }
        remember { Random.nextFloat() * 0.1f - 0.05f }
        remember { Random.nextInt() }

        this.drawWithContent {
            val random = Random(jitterFrame.hashCode())
            val hazeTime = heatTicker * 2 * kotlin.math.PI.toFloat()

            // Hypnotic Fireplace (Atmospheric approach)
            drawIntoCanvas { canvas ->
                canvas.saveLayer(
                    androidx.compose.ui.geometry
                        .Rect(0f, 0f, size.width, size.height),
                    androidx.compose.ui.graphics
                        .Paint(),
                )

                // 1. Hypnotic Heat Haze (Smooth layered distortion)
                val segments = 20
                val segH = size.height / segments

                repeat(segments) { i ->
                    val progress = i.toFloat() / segments
                    val wave = kotlin.math.sin(hazeTime + progress * 5f) * 3f

                    canvas.save()
                    canvas.clipRect(0f, i * segH, size.width, (i + 1) * segH)
                    canvas.translate(wave, 0f)
                    drawContent()
                    canvas.restore()
                }

                // 2. Fireplace Gradient (Animated glow)
                val fireBrush =
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFD54F), // Yellow Heat
                            Color(0xFFFF5722), // Orange Fire
                            Color(0xFFDD2C00), // Deep Red Ember
                        ),
                        startY = size.height * (0.2f - expansionTicker * 0.4f),
                        endY = size.height * (1.2f - expansionTicker * 0.4f),
                    )

                drawRect(
                    brush = fireBrush,
                    blendMode = BlendMode.SrcAtop,
                    alpha = 0.8f + burnPulse * 0.2f,
                )

                canvas.restore()
            }

            // C. Film Noir Grain (Subtle)
            if (random.nextFloat() < 0.2f) {
                repeat(4) {
                    val grainX = random.nextFloat() * size.width
                    val grainY = random.nextFloat() * size.height
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = 1f + random.nextFloat() * 1.5f,
                        center = Offset(grainX, grainY),
                    )
                }
            }
        }
    }

/**
 * A specialized Glitch effect for drawText operations
 */
fun drawGlitchText(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    textLayoutResult: TextLayoutResult,
    color: Color,
    glitchFrequency: Float = 0.2f,
    offset: Offset = Offset.Zero,
) {
    with(drawScope) {
        val shouldGlitch = Random.nextFloat() < glitchFrequency

        if (shouldGlitch) {
            val glitchX = (Random.nextFloat() - 0.5f) * 15f
            val glitchY = (Random.nextFloat() - 0.5f) * 5f

            // Draw red channel
            drawText(
                textLayoutResult = textLayoutResult,
                color = Color.Red.copy(alpha = 0.5f),
                topLeft =
                    offset +
                        Offset(glitchX, glitchY),
                blendMode = BlendMode.Screen,
            )

            // Draw blue channel
            drawText(
                textLayoutResult = textLayoutResult,
                color = Color.Cyan.copy(alpha = 0.5f),
                topLeft =
                    offset +
                        Offset(-glitchX, -glitchY),
                blendMode = BlendMode.Screen,
            )
        }

        // Draw main text
        drawText(
            textLayoutResult = textLayoutResult,
            color = color,
            topLeft = offset,
        )
    }
}

@Composable
fun Modifier.divineAura(
    isPlaying: Boolean = true,
    auraColor: Color = Color(0xFFFFD700), // Gold
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "divineAura")

        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing), // Slower, more majestic
                    repeatMode = RepeatMode.Restart,
                ),
            label = "divineTicker",
        )

        this.drawWithContent {
            // 1. Draw main text
            drawContent()

            // 2. Divine Stars (Smooth, sparse, and floating)
            val random = Random(42) // Stable seed
            repeat(6) { i ->
                val startX = random.nextFloat() * size.width
                val startY = random.nextFloat() * size.height

                // Individual speed and offset for each star
                val speed = 0.8f + (i % 3) * 0.2f
                val phase = i * 0.2f
                val rawPulse =
                    kotlin.math.sin((ticker * speed + phase) * 2 * kotlin.math.PI.toFloat())

                // Narrow the pulse: Invisible for 70% of the time, then a soft fade in/out
                val pulse = ((rawPulse + 1f) / 2f - 0.7f).coerceAtLeast(0f) / 0.3f

                if (pulse > 0f) {
                    // Tiny floating drift
                    val driftY = -pulse * 15f
                    val center = Offset(startX, startY + driftY)

                    val coreRadius = (1.5f * pulse + 0.5f)
                    val glowRadius = coreRadius * 8f

                    // Blurry Divine Glow
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                0f to auraColor.copy(alpha = pulse * 0.5f),
                                0.6f to auraColor.copy(alpha = pulse * 0.1f),
                                1f to Color.Transparent,
                                center = center,
                                radius = glowRadius,
                            ),
                        radius = glowRadius,
                        center = center,
                        blendMode = BlendMode.Plus,
                    )

                    // Pure Light Center
                    drawCircle(
                        color = Color.White.copy(alpha = pulse * 0.8f),
                        radius = coreRadius,
                        center = center,
                        blendMode = BlendMode.Plus,
                    )
                }
            }
        }
    }

@Composable
fun Modifier.psychosis(isPlaying: Boolean = true): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "psychosis")

        // Psychosis Jitter (Violent twitching)
        val jitterTicker by
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(100, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "jitterTicker",
            )

        // Ooze Ticker (Slow melting/bleeding)
        val oozeTicker by
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "oozeTicker",
            )

        this.drawWithContent {
            val random = Random(jitterTicker.hashCode())
            val oozeProgress = oozeTicker

            drawIntoCanvas { canvas ->
                canvas.saveLayer(
                    androidx.compose.ui.geometry
                        .Rect(0f, 0f, size.width, size.height),
                    androidx.compose.ui.graphics
                        .Paint(),
                )

                // 1. Ghostly Echoes (Unstable afterimages)
                if (random.nextFloat() < 0.3f) {
                    val echoX = (random.nextFloat() - 0.5f) * 20f
                    val echoY = (random.nextFloat() - 0.5f) * 10f
                    canvas.save()
                    canvas.translate(echoX, echoY)
                    drawContent()

                    // Draw a pale cyan "ghost" on top
                    drawRect(
                        color = Color(0xFF6FFFE9).copy(alpha = 0.15f),
                        blendMode = BlendMode.SrcAtop,
                    )
                    canvas.restore()
                }

                // 2. Vertical Ooze (Melting slices)
                val segments = 15
                val segW = size.width / segments
                repeat(segments) { i ->
                    val segRandom = Random(jitterTicker.hashCode() + i)
                    val offsetY =
                        if (segRandom.nextFloat() < 0.2f) {
                            segRandom.nextFloat() * 8f // Sudden drop
                        } else {
                            kotlin.math.sin(oozeProgress * 2 * kotlin.math.PI.toFloat() + i) * 3f
                        }
                    val jitterX = (segRandom.nextFloat() - 0.5f) * 4f

                    canvas.save()
                    canvas.clipRect(i * segW, 0f, (i + 1) * segW, size.height)
                    canvas.translate(jitterX, offsetY)
                    drawContent()
                    canvas.restore()
                }

                // 3. Bleeding/Dripping (Masked to text)
                repeat(4) { i ->
                    val dripRandom = Random(oozeTicker.hashCode() + i * 100)
                    val dripX = dripRandom.nextFloat() * size.width
                    val dripState = (oozeTicker + i * 0.25f) % 1f
                    val dripY = dripState * size.height * 1.5f

                    if (dripState > 0.1f) {
                        drawCircle(
                            color = Color(0xFF8B2635), // Blood Red
                            radius = 2f + dripRandom.nextFloat() * 3f,
                            center = Offset(dripX, dripY),
                            blendMode = BlendMode.SrcAtop, // ONLY DRIP ON TEXT
                        )
                    }
                }

                // 4. Dread Flash (Subtle high-contrast flicker)
                if (random.nextFloat() < 0.05f) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.1f),
                        blendMode = BlendMode.Plus,
                    )
                }

                canvas.restore()
            }
        }
    }

@Composable
fun Modifier.katanaSlice(
    isPlaying: Boolean = true,
    color: Color = Color.Red,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "katanaSlice")

        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(3500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "katanaTicker",
        )

        this.drawWithContent {
            // Randomize based on cycle
            val cycle = (System.currentTimeMillis() / 3500).toInt()
            val random = Random(cycle)
            val angle = (random.nextFloat() - 0.5f) * 30f // -15 to 15 degrees
            val startY = size.height * (0.35f + random.nextFloat() * 0.3f)
            val angleRad = (angle * kotlin.math.PI / 180f).toFloat()
            val tanA = kotlin.math.tan(angleRad)

            // Animation Phases
            val slashDuration = 0.12f // Very fast slash
            val splitStart = 0.08f // Start splitting slightly after slash starts
            val splitEnd = 0.6f // Fully split and held until here
            val recoverStart = 0.7f // Start coming back
            val recoverEnd = 0.9f // Back to normal

            val slashProgress = (ticker / slashDuration).coerceIn(0f, 1f)

            val splitProgress =
                when {
                    ticker < splitStart -> {
                        0f
                    }

                    ticker < recoverStart -> {
                        // Slide out fast
                        val p = (ticker - splitStart) / (slashDuration - splitStart)
                        p.coerceIn(0f, 1f)
                    }

                    ticker < recoverStart + (splitEnd - splitStart) -> {
                        1f
                    }

                    ticker < recoverEnd -> {
                        // Recover slow
                        1f - (ticker - recoverStart) / (recoverEnd - recoverStart)
                    }

                    else -> {
                        0f
                    }
                }

            // Draw Logic
            if (ticker < splitStart || ticker > recoverEnd) {
                // Just draw normal content
                drawContent()
            } else {
                // Split effect
                val splitDist = 12f * splitProgress
                // Normal vector to the slash line for displacement
                val nx = -kotlin.math.sin(angleRad)
                val ny = kotlin.math.cos(angleRad)

                // Top Half
                val topPath =
                    androidx.compose.ui.graphics.Path().apply {
                        moveTo(-200f, -200f)
                        lineTo(size.width + 200f, -200f)
                        lineTo(size.width + 200f, tanA * (size.width + 200f) + startY)
                        lineTo(-200f, tanA * (-200f) + startY)
                        close()
                    }

                // Bottom Half
                val bottomPath =
                    androidx.compose.ui.graphics.Path().apply {
                        moveTo(-200f, size.height + 200f)
                        lineTo(size.width + 200f, size.height + 200f)
                        lineTo(size.width + 200f, tanA * (size.width + 200f) + startY)
                        lineTo(-200f, tanA * (-200f) + startY)
                        close()
                    }

                // Draw Top Half Displaced
                drawIntoCanvas { canvas ->
                    canvas.save()
                    canvas.translate(-nx * splitDist, -ny * splitDist)
                    canvas.clipPath(topPath)
                    drawContent()
                    canvas.restore()

                    // Draw Bottom Half Displaced
                    canvas.save()
                    canvas.translate(nx * splitDist, ny * splitDist)
                    canvas.clipPath(bottomPath)
                    drawContent()
                    canvas.restore()
                }
            }

            // Blade Sweep Overlay (Clipped to Text)
            if (ticker < slashDuration) {
                val bladeSweepX = -size.width * 0.5f + slashProgress * (size.width * 2.5f)
                val bladeWidth = size.width * 0.4f

                drawIntoCanvas { canvas ->
                    // Mask to text content
                    canvas.saveLayer(
                        androidx.compose.ui.geometry.Rect(
                            0f,
                            0f,
                            size.width,
                            size.height,
                        ),
                        androidx.compose.ui.graphics
                            .Paint(),
                    )
                    drawContent()

                    androidx.compose.ui.graphics.Paint().apply {
                        blendMode = BlendMode.SrcAtop
                    }

                    // The "Blade" gradient
                    val bladeBrush =
                        Brush.linearGradient(
                            0f to Color.Transparent,
                            0.4f to color.copy(alpha = 0.6f),
                            0.5f to Color.White, // Sharp edge
                            0.6f to color.copy(alpha = 0.6f),
                            1f to Color.Transparent,
                            start = Offset(bladeSweepX, tanA * bladeSweepX + startY),
                            end =
                                Offset(
                                    bladeSweepX + bladeWidth,
                                    tanA * (bladeSweepX + bladeWidth) + startY,
                                ),
                        )

                    drawRect(
                        brush = bladeBrush,
                        blendMode = BlendMode.SrcAtop,
                    )

                    canvas.restore()
                }

                // Extra White Flash during impact
                if (slashProgress > 0.3f && slashProgress < 0.7f) {
                    drawIntoCanvas { canvas ->
                        canvas.saveLayer(
                            androidx.compose.ui.geometry.Rect(
                                0f,
                                0f,
                                size.width,
                                size.height,
                            ),
                            androidx.compose.ui.graphics
                                .Paint(),
                        )
                        drawContent()
                        drawRect(
                            color = Color.White.copy(alpha = 0.4f),
                            blendMode = BlendMode.SrcAtop,
                        )
                        canvas.restore()
                    }
                }
            }
        }
    }

@Composable
fun Modifier.sakuraWind(
    isPlaying: Boolean = true,
    petalColor: Color = Color(0xFFFFB7C5), // Default Sakura Pink
    petalCount: Int = 12,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "sakuraWind")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "windTicker",
        )

        val leafPainter = rememberVectorPainter(ImageVector.vectorResource(id = R.drawable.ic_leaf))

        this.drawWithContent {
            // 1. Draw Text Content
            drawContent()

            // 2. Draw Petals
            val random = Random(42) // Stable seed

            repeat(petalCount) { i ->
                val seed = random.nextFloat()
                val speedMult = 0.7f + (seed * 0.6f)
                val delayOffset = seed * 5f
                val progress = (ticker * speedMult + delayOffset) % 1f

                val x = -60f + progress * (size.width + 120f)
                val baseY = (i.toFloat() / petalCount) * size.height
                val waveAmplitude = 10f + (seed * 20f)
                val waveFrequency = 1f + (seed * 1.5f)
                val phase = (progress * waveFrequency * 2 * kotlin.math.PI.toFloat()) + (seed * 10f)
                val y = baseY + kotlin.math.sin(phase) * waveAmplitude

                val alpha =
                    when {
                        progress < 0.15f -> progress / 0.15f
                        progress > 0.85f -> (1f - progress) / 0.15f
                        else -> 1f
                    }

                if (alpha > 0f) {
                    val scale = 0.4f + (seed * 0.8f)
                    val rotation = progress * 1080f * (if (i % 2 == 0) 1f else -1f)
                    val leafSize = Size(20f, 20f)

                    // Motion Blur: Draw trailing echoes
                    repeat(2) { echoIndex ->
                        val echoOffset = (echoIndex + 1) * 12f * speedMult
                        val echoAlpha = alpha * (0.25f / (echoIndex + 1))

                        withTransform({
                            translate(x - echoOffset, y)
                            rotate(rotation - (echoIndex * 2f))
                            scale(scale, scale)
                        }) {
                            with(leafPainter) {
                                draw(
                                    size = leafSize,
                                    colorFilter = ColorFilter.tint(petalColor.copy(alpha = echoAlpha)),
                                )
                            }
                        }
                    }

                    withTransform({
                        translate(x, y)
                        rotate(rotation)
                        scale(scale, scale)
                    }) {
                        // 1. Petal Glow
                        val glowSize = 25f
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            petalColor.copy(alpha = 0.4f * alpha),
                                            petalColor.copy(alpha = 0.1f * alpha),
                                            Color.Transparent,
                                        ),
                                    center = Offset(10f, 10f),
                                    radius = glowSize,
                                ),
                            radius = glowSize,
                            center = Offset(10f, 10f),
                            blendMode = BlendMode.Screen,
                        )

                        // 2. Main Petal
                        with(leafPainter) {
                            draw(
                                size = leafSize,
                                colorFilter = ColorFilter.tint(petalColor.copy(alpha = 0.85f * alpha)),
                            )
                        }

                        // 3. Subtle edge highlight for detail
                        with(leafPainter) {
                            draw(
                                size = leafSize,
                                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.15f * alpha)),
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun Modifier.lightningStorm(
    isPlaying: Boolean = true,
    lightningColor: Color = Color.Cyan,
): Modifier =
    composed {
        if (!isPlaying) return@composed this

        val infiniteTransition = rememberInfiniteTransition(label = "lightningStorm")

        // 2500ms Loop
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "lightningProgress",
        )

        val strikeDuration = 0.2f
        val holdDuration = 0.2f
        val dischargeDuration = 0.3f

        val strikeEnd = strikeDuration
        val holdEnd = strikeEnd + holdDuration
        val dischargeEnd = holdEnd + dischargeDuration

        val seed = remember { mutableIntStateOf(0) }
        val lastCycle = remember { mutableLongStateOf(0L) }

        if (progress < 0.02f) {
            val now = System.currentTimeMillis()
            if (now - lastCycle.longValue > 1000) {
                seed.intValue = now.toInt()
                lastCycle.longValue = now
            }
        }

        this.drawWithContent {
            // Draw content first
            drawContent()

            // Only draw lightning during active phases
            if (progress < dischargeEnd) {
                val random = Random(seed.intValue)

                // --- 3-Zone Path Generation ---
                val path =
                    androidx.compose.ui.graphics
                        .Path()
                val joints = mutableListOf<Offset>()
                val branches = mutableListOf<androidx.compose.ui.graphics.Path>()

                val zone = random.nextInt(3)

                val startX: Float
                val startY: Float
                val endX: Float
                val endY: Float

                // Canvas Dimensions
                val w = size.width
                val h = size.height

                when (zone) {
                    0 -> { // LEFT ZONE (Vertical/Diagonal Strike)
                        // Start: Strictly inside left 10%
                        startX = random.nextFloat() * w * 0.1f
                        startY = -h * 0.1f
                        // End: Strictly inside left 10%
                        endX = random.nextFloat() * w * 0.1f
                        endY = h * 1.1f
                    }

                    2 -> { // RIGHT ZONE (Vertical/Diagonal Strike)
                        // Start: Strictly inside right 10%
                        startX = w * 0.9f + random.nextFloat() * w * 0.1f
                        startY = -h * 0.1f
                        // End: Strictly inside right 10%
                        endX = w * 0.9f + random.nextFloat() * w * 0.1f
                        endY = h * 1.1f
                    }

                    else -> { // CENTER ZONE (Horizontal Strike Behind)
                        // Start/End clamped to near-bounds
                        startX = -w * 0.1f
                        endX = w * 1.1f

                        // Vertical Position: Top 25% or Bottom 25%
                        val isHigh = random.nextBoolean()

                        if (isHigh) {
                            startY = h * 0.2f + (random.nextFloat() - 0.5f) * h * 0.1f
                            endY = h * 0.2f + (random.nextFloat() - 0.5f) * h * 0.1f
                        } else {
                            startY = h * 0.8f + (random.nextFloat() - 0.5f) * h * 0.1f
                            endY = h * 0.8f + (random.nextFloat() - 0.5f) * h * 0.1f
                        }
                    }
                }

                path.moveTo(startX, startY)
                joints.add(
                    Offset(startX, startY),
                )

                // Recursive subdivision
                val fragments = mutableListOf<Fragment>() // List to hold lightning fragments

                fun subdivide(
                    x1: Float,
                    y1: Float,
                    x2: Float,
                    y2: Float,
                    displacement: Float,
                    iteration: Int,
                ) {
                    if (iteration <= 0) {
                        path.lineTo(x2, y2)
                        // Fragment Generation Chance
                        if (random.nextFloat() < 0.25f) { // 25% chance at joints
                            val fPath =
                                androidx.compose.ui.graphics
                                    .Path()
                            val fx = x2
                            val fy = y2
                            fPath.moveTo(0f, 0f) // Relative to (fx, fy)

                            // Small random jag
                            val jagX = (random.nextFloat() - 0.5f) * 20f
                            val jagY = (random.nextFloat() - 0.5f) * 20f
                            fPath.lineTo(jagX, jagY)
                            fPath.lineTo(
                                jagX + (random.nextFloat() - 0.5f) * 20f,
                                jagY + (random.nextFloat() - 0.5f) * 20f,
                            )

                            fragments.add(
                                Fragment(
                                    x = fx,
                                    y = fy,
                                    path = fPath,
                                    dx = (random.nextFloat() - 0.5f) * 40f, // Reduced drift (was w*0.1)
                                    dy = (random.nextFloat() - 0.5f) * 40f, // Reduced drift (was h*0.1)
                                    rotation = random.nextFloat() * 360f,
                                    scale = random.nextFloat() * 0.5f + 0.3f,
                                ),
                            )
                        }
                    } else {
                        val midX = (x1 + x2) / 2
                        val midY = (y1 + y2) / 2

                        // Jitter
                        val offsetX = (random.nextFloat() - 0.5f) * displacement
                        val offsetY = (random.nextFloat() - 0.5f) * displacement

                        val newX = midX + offsetX
                        val newY = midY + offsetY

                        // Branching Logic (Spikes)
                        if (iteration < 4 && random.nextFloat() < 0.25f) { // Slightly higher chance
                            val branchPath =
                                androidx.compose.ui.graphics
                                    .Path()
                            branchPath.moveTo(newX, newY)

                            // Branch Direction: Generally perpendicular to the main flow?
                            // Simple random is chaotic but effective for lightning.
                            // Let's try to base it on segment vector.
                            // Vector (dx, dy) = (x2-x1, y2-y1)
                            // Perp = (-dy, dx) or (dy, -dx)

                            val dx = x2 - x1
                            val dy = y2 - y1
                            val len = kotlin.math.sqrt(dx * dx + dy * dy)

                            // Normalized Perp
                            val px = -dy / len
                            val py = dx / len

                            // Random Side
                            val side = if (random.nextBoolean()) 1f else -1f
                            val branchLen = w * (0.1f + random.nextFloat() * 0.2f)

                            // Jitter the angle
                            val angleJitterX = (random.nextFloat() - 0.5f) * 0.5f
                            val angleJitterY = (random.nextFloat() - 0.5f) * 0.5f

                            val bx = newX + (px * side + angleJitterX) * branchLen
                            val by = newY + (py * side + angleJitterY) * branchLen

                            branchPath.lineTo(bx, by)
                            branches.add(branchPath)
                        }

                        subdivide(x1, y1, newX, newY, displacement / 2, iteration - 1)
                        subdivide(newX, newY, x2, y2, displacement / 2, iteration - 1)
                    }
                }

                // Displacement relative to distance
                val dist = kotlin.math.hypot(endX - startX, endY - startY)

                // Initial Curve Bias!
                val curveControlX = (startX + endX) / 2 + (random.nextFloat() - 0.5f) * w * 0.4f
                val curveControlY = (startY + endY) / 2 + (random.nextFloat() - 0.5f) * h * 0.4f

                // Subdivide Start->Control and Control->End to create the base arc
                subdivide(startX, startY, curveControlX, curveControlY, dist * 0.25f, 5)
                subdivide(curveControlX, curveControlY, endX, endY, dist * 0.25f, 5)

                // --- Animation Calculation ---
                val androidPath = path.asAndroidPath()
                val measure = android.graphics.PathMeasure(androidPath, false)
                val totalLength = measure.length

                val visibleStart: Float
                val visibleEnd: Float

                when {
                    progress < strikeEnd -> {
                        // Strike: 0 -> Length
                        val localP = progress / strikeDuration
                        val eased = FastOutSlowInEasing.transform(localP)
                        visibleStart = 0f
                        visibleEnd = totalLength * eased
                    }

                    progress < holdEnd -> {
                        // Hold: Full path
                        visibleStart = 0f
                        visibleEnd = totalLength
                    }

                    else -> {
                        // Discharge: Start -> Length
                        val localP = (progress - holdEnd) / dischargeDuration
                        // Ease out the tail
                        val eased = FastOutSlowInEasing.transform(localP)
                        visibleStart = totalLength * eased
                        visibleEnd = totalLength
                    }
                }

                if (visibleEnd > visibleStart) {
                    val visiblePath = android.graphics.Path()
                    measure.getSegment(visibleStart, visibleEnd, visiblePath, true)
                    val composeVisiblePath = visiblePath.asComposePath()

                    // --- Drawing ---

                    // Helper to draw a sharp triangle cap
                    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSharpCap(
                        pos: FloatArray,
                        tan: FloatArray,
                        width: Float,
                        color: Color,
                        isHead: Boolean,
                    ) {
                        val x = pos[0]
                        val y = pos[1]
                        val tx = tan[0]
                        val ty = tan[1]

                        // Perpendicular vector for base
                        // (-ty, tx)
                        val px = -ty
                        val py = tx

                        // normalize tangent? PosTan should be normalized usually, checks docs?
                        // PathMeasure.getPosTan: "The returned tangent is normalized." Great.

                        val halfWidth = width / 2

                        // Triangle Base points
                        val base1X = x - px * halfWidth
                        val base1Y = y - py * halfWidth
                        val base2X = x + px * halfWidth
                        val base2Y = y + py * halfWidth

                        // Tip point
                        // Length of cap ~ width
                        val tipLength = width * 1.5f
                        val direction = if (isHead) 1f else -1f
                        val tipX = x + tx * tipLength * direction
                        val tipY = y + ty * tipLength * direction

                        val capPath =
                            androidx.compose.ui.graphics.Path().apply {
                                moveTo(base1X, base1Y)
                                lineTo(base2X, base2Y)
                                lineTo(tipX, tipY)
                                close()
                            }

                        drawPath(capPath, color, blendMode = BlendMode.Screen)
                    }

                    // 1. Native Glow (Atmosphere) - Softer/Wider
                    drawIntoCanvas { canvas ->
                        val nativeCanvas = canvas.nativeCanvas
                        val glowPaint =
                            android.graphics.Paint().apply {
                                color = lightningColor.toArgb()
                                alpha = 100 // Lower alpha for atmosphere
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = 50f
                                strokeCap = android.graphics.Paint.Cap.ROUND
                                strokeJoin = android.graphics.Paint.Join.ROUND
                                maskFilter =
                                    android.graphics.BlurMaskFilter(
                                        20f,
                                        android.graphics.BlurMaskFilter.Blur.OUTER,
                                    )
                            }
                        nativeCanvas.drawPath(visiblePath, glowPaint)
                    }

                    // 2. Soft Body (Color) - Natural Light Blur
                    drawIntoCanvas { canvas ->
                        val nativeCanvas = canvas.nativeCanvas
                        val bodyPaint =
                            android.graphics.Paint().apply {
                                color = lightningColor.toArgb()
                                alpha = 255
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = 15f
                                strokeCap = android.graphics.Paint.Cap.ROUND
                                strokeJoin = android.graphics.Paint.Join.ROUND
                                maskFilter =
                                    android.graphics.BlurMaskFilter(
                                        15f,
                                        android.graphics.BlurMaskFilter.Blur.NORMAL,
                                    )
                            }
                        nativeCanvas.drawPath(visiblePath, bodyPaint)
                    }

                    // 3. Core (White) - Hot Center
                    drawPath(
                        path = composeVisiblePath,
                        color = Color.White,
                        style =
                            androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 5f, // Reverted Core (10 -> 5)
                                cap = androidx.compose.ui.graphics.StrokeCap.Butt,
                                join = androidx.compose.ui.graphics.StrokeJoin.Miter,
                                miter = 10f,
                            ),
                    )

                    // Draw Triangle Caps for Main Bolt
                    val posTan = FloatArray(2)
                    val tan = FloatArray(2)

                    // Head Cap (at visibleEnd)
                    // Head Cap (at visibleEnd)
                    if (visibleEnd < totalLength) {
                        measure.getPosTan(visibleEnd, posTan, tan)
                        drawSharpCap(posTan, tan, 5f, Color.White, true) // Core
                    } else {
                        measure.getPosTan(totalLength, posTan, tan)
                        drawSharpCap(posTan, tan, 5f, Color.White, true)
                    }

                    // Tail Cap (at visibleStart) - Only if discharging
                    if (visibleStart > 0f) {
                        measure.getPosTan(visibleStart, posTan, tan)
                        drawSharpCap(posTan, tan, 5f, Color.White, false)
                    }

                    // 4. Branches (Spikes) -> Now Tapered Wedges
                    branches.forEachIndexed { index, bPath ->
                        val branchLifeStart = strikeEnd - 0.05f
                        val branchLifeEnd = holdEnd + 0.1f

                        if (progress in branchLifeStart..branchLifeEnd) {
                            val bProgress = (progress - branchLifeStart) / (0.1f)
                            val clampedBProgress = bProgress.coerceIn(0f, 1f)

                            val fadeProgress = (progress - holdEnd) / 0.1f
                            val bAlpha =
                                if (progress > holdEnd) (1f - fadeProgress).coerceIn(0f, 1f) else 1f

                            if (bAlpha > 0f) {
                                // Extract Start/End from bPath
                                // We know bPath is just moveTo(x1,y1) lineTo(x2,y2)
                                // But Path API doesn't expose points easily in Compose.
                                // We can use PathMeasure for the branch too.
                                val bAndroid = bPath.asAndroidPath()
                                val bMeasure = android.graphics.PathMeasure(bAndroid, false)
                                val bLength = bMeasure.length

                                val bPos = FloatArray(2)
                                val bTan = FloatArray(2)
                                bMeasure.getPosTan(0f, bPos, bTan) // Start

                                val startX = bPos[0]
                                val startY = bPos[1]
                                val bx = bTan[0]
                                val by = bTan[1]

                                // Current End based on growth
                                val currentLen = bLength * clampedBProgress
                                val endX = startX + bx * currentLen
                                val endY = startY + by * currentLen

                                // Draw as Tapered Triangle (Wedge)
                                // Base width at start: 8f -> 0f at end
                                val baseWidth = 8f
                                val perpX = -by
                                val perpY = bx

                                val wedge =
                                    androidx.compose.ui.graphics.Path().apply {
                                        moveTo(
                                            startX + perpX * baseWidth / 2,
                                            startY + perpY * baseWidth / 2,
                                        )
                                        moveTo(
                                            startX - perpX * baseWidth / 2,
                                            startY - perpY * baseWidth / 2,
                                        )
                                        lineTo(endX, endY) // Pointy end
                                        close()
                                    }

                                drawPath(
                                    path = wedge,
                                    color = lightningColor.copy(alpha = bAlpha),
                                    blendMode = BlendMode.Screen,
                                )
                                // White Core for Branch
                                val coreWedge =
                                    androidx.compose.ui.graphics.Path().apply {
                                        moveTo(startX + perpX * 2f, startY + perpY * 2f)
                                        moveTo(startX - perpX * 2f, startY - perpY * 2f)
                                        lineTo(endX, endY)
                                        close()
                                    }
                                drawPath(
                                    path = coreWedge,
                                    color = lightningColor.lighter(.8f).copy(alpha = bAlpha),
                                    blendMode = BlendMode.Screen,
                                )
                            }
                        }
                    }

                    // 5. FRAGMENTS (Jagged Electricity Particles)
                    fragments.forEach { frag ->
                        // Animate - Drift
                        val t = progress / dischargeEnd
                        val curX = frag.x + frag.dx * t
                        val curY = frag.y + frag.dy * t

                        val fAlpha =
                            if (progress > holdEnd) {
                                (1f - (progress - holdEnd) / dischargeDuration).coerceIn(0f, 1f)
                            } else {
                                1f
                            }

                        if (fAlpha > 0f) {
                            withTransform({
                                translate(curX, curY)
                                rotate(frag.rotation + t * 45f)
                                scale(frag.scale * (1f - t * 0.3f), frag.scale * (1f - t * 0.3f))
                            }) {
                                // Draw Fragment Glow
                                drawPath(
                                    path = frag.path,
                                    color = lightningColor.copy(alpha = fAlpha),
                                    style =
                                        androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 4f, // Reduced Fragment Glow (8 -> 4)
                                            cap = androidx.compose.ui.graphics.StrokeCap.Butt,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Miter,
                                        ),
                                )
                                // Draw Fragment White Core
                                drawPath(
                                    path = frag.path,
                                    color = Color.White.copy(alpha = fAlpha),
                                    style =
                                        androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 2f, // Reduced Fragment Core (4 -> 2)
                                            cap = androidx.compose.ui.graphics.StrokeCap.Butt,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Miter,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

// Data class for Fragments
data class Fragment(
    val x: Float,
    val y: Float,
    val path: androidx.compose.ui.graphics.Path,
    val dx: Float,
    val dy: Float,
    val rotation: Float,
    val scale: Float,
)

@Composable
fun Modifier.genreVfx(
    genre: Genre?,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
    isPlaying: Boolean = true,
): Modifier {
    if (genre == null) return this
    if (isPlaying.not()) return this
    val finalPrimary = primaryColor ?: MaterialTheme.colorScheme.primary
    secondaryColor ?: MaterialTheme.colorScheme.secondary

    return when (genre) {
        Genre.FANTASY -> {
            this
                .levitate()
                .divineAura(auraColor = genre.colorPalette().last().lighter(.4f))
        }

        Genre.CYBERPUNK -> {
            this
                .chromaticAberration(intensity = 10f, blurRadius = 10f)
                .glitch()
        }

        Genre.HORROR -> {
            this
                .psychosis()
        }

        Genre.COWBOY -> {
            this
                .cowboyBurn(true)
        }

        Genre.CRIME -> {
            this
                .vhs()
                .dreamySparkle(color = finalPrimary.lighter(0.6f))
        }

        Genre.HEROES -> {
            this
                .levitate(yOffset = 10f)
                .divineAura(auraColor = MaterialTheme.colorScheme.primary.lighter(.3f))
                .lightningStorm(lightningColor = finalPrimary.lighter(0.6f))
        }

        Genre.SPACE_OPERA -> {
            this
                .spaceVoyage(true)
        }

        Genre.SHINOBI -> {
            this
                .levitate()
                .sakuraWind(true, finalPrimary, petalCount = 10)
        }

        Genre.PUNK_ROCK -> {
            this
                .glitch(true)
        }
    }
}
