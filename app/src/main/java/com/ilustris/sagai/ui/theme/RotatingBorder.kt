package com.ilustris.sagai.ui.theme

import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Drives a continuously rotating angle (0–360°, linear, restarting) for [Modifier.rotatingGradientBorder].
 *
 * Shared across every element that should rotate *in sync* (e.g. every chat bubble on screen) —
 * compute it once at the call site that owns that group and pass the same [Float] down, rather
 * than calling this once per element (which would give each its own independent phase).
 */
@Composable
fun rememberRotatingBorderAngle(
    isAnimating: Boolean = true,
    durationMillis: Int = 3000,
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "rotatingBorderAngle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "rotation",
    )
    return if (isAnimating) rotation else 0f
}

/**
 * Draws a rotating gradient stroke around [shape] — a `sweepGradient` shader whose local matrix
 * is rotated by [rotationDegrees] each frame, so the gradient itself spins in place around a
 * static outline instead of the whole layout node rotating (which would tumble a rounded-rect's
 * corners out of frame). Pair with [rememberRotatingBorderAngle] for the angle.
 *
 * Used for "AI is generating" affordances — the chat bubble border being the original case this
 * was extracted from — but is shape-agnostic, so it works for any bordered surface.
 */
fun Modifier.rotatingGradientBorder(
    shape: Shape,
    colors: List<Color>,
    rotationDegrees: Float,
    strokeWidth: Dp = 1.dp,
): Modifier =
    this.drawWithContent {
        drawContent()
        val outline = shape.createOutline(size, layoutDirection, this)
        val brush =
            object : ShaderBrush() {
                override fun createShader(size: Size): Shader {
                    val shader = (sweepGradient(colors) as ShaderBrush).createShader(size)
                    val matrix = Matrix()
                    matrix.setRotate(rotationDegrees, size.width / 2, size.height / 2)
                    shader.setLocalMatrix(matrix)
                    return shader
                }
            }
        drawOutline(outline = outline, brush = brush, style = Stroke(width = strokeWidth.toPx()))
    }
