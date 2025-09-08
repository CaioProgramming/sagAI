package com.ilustris.sagai.ui.theme.components // Assuming your package

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.holographicGradient
import android.graphics.RenderEffect as FrameworkRenderEffect
import android.graphics.Shader as FrameworkShader

@Composable
fun BlurredGlowContainer(
    modifier: Modifier = Modifier,
    brush: Brush,
    blurSigma: Float = 40f,
    shape: Shape = RectangleShape, // Added shape parameter
    content: @Composable BoxScope.() -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val mainContentPlaceables: List<Placeable> =
            subcompose(ContentSlot.Main) {
                Box(modifier = Modifier.clip(shape)) {
                    // Clipped main content
                    content()
                }
            }.map { measurable -> measurable.measure(constraints) }

        val mainContentPlaceable = mainContentPlaceables.firstOrNull()

        val contentWidth = mainContentPlaceable?.width ?: 0
        val contentHeight = mainContentPlaceable?.height ?: 0

        val backgroundPlaceable =
            subcompose(ContentSlot.Background) {
                Box(
                    modifier =
                        Modifier
                            .then(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurSigma > 0f) {
                                    Modifier.graphicsLayer {
                                        val frameworkBlur: FrameworkRenderEffect =
                                            FrameworkRenderEffect.createBlurEffect(
                                                blurSigma,
                                                blurSigma,
                                                FrameworkShader.TileMode.DECAL,
                                            )
                                        renderEffect = frameworkBlur.asComposeRenderEffect()
                                    }
                                } else {
                                    Modifier // No blur effect if below S or sigma is 0
                                },
                            ).drawBehind {
                                // Draw shape instead of rect for glow origin
                                val outline = shape.createOutline(size, layoutDirection, this)
                                drawOutline(outline = outline, brush = brush)
                            },
                )
            }.map {
                it.measure(
                    constraints.copy(
                        minWidth = contentWidth,
                        minHeight = contentHeight,
                        maxWidth = contentWidth,
                        maxHeight = contentHeight,
                    ),
                )
            }.firstOrNull()

        layout(contentWidth, contentHeight) {
            backgroundPlaceable?.placeRelative(0, 0)
            mainContentPlaceable?.placeRelative(0, 0)
        }
    }
}

private enum class ContentSlot { Main, Background }

// --- Example Usage ---

@Preview(showBackground = true, widthDp = 360, heightDp = 700)
@Composable
fun BlurredGlowContainerPreview() {
    MaterialTheme {
        // Use your app's theme or MaterialTheme for previews
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp),
            ) {
                Text(
                    "Container Preview",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 10.dp),
                )

                // Example 1: Animated Gradient Glow with a Card
                BlurredGlowContainer(
                    modifier = Modifier.padding(vertical = 10.dp),
                    brush = gradientAnimation(holographicGradient),
                    blurSigma = 35f,
                    shape = RoundedCornerShape(16.dp), // Pass shape to container
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp), // Card has its own shape
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Animated Glow", style = MaterialTheme.typography.headlineSmall)
                            Text("This card has a blurred glow that animates behind it.")
                            Button(onClick = { /*TODO*/ }) {
                                Text("Click Me")
                            }
                        }
                    }
                }

                // Example 2: Static Radial Gradient with Simple Text
                BlurredGlowContainer(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(Color.Red.copy(alpha = 0.7f), Color.Transparent),
                            radius = 150f, // Adjust radius as needed, will be relative to component size
                        ),
                    blurSigma = 50f,
                    shape = RoundedCornerShape(8.dp), // Pass shape to container
                ) {
                    Text(
                        "Static Radial Glow",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier =
                            Modifier
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp)) // Text background has its own shape
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                }

                // Example 3: Different shape content
                BlurredGlowContainer(
                    brush = Brush.verticalGradient(listOf(Color.Blue, Color.Green)),
                    blurSigma = 20f,
                    shape = CircleShape, // Pass shape to container
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(100.dp)
                                .clip(CircleShape) // Box content is explicitly clipped
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(10.dp),
                        // Padding inside the circle
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Circle", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
