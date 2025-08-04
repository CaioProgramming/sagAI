
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.ilustris.sagai.features.newsaga.data.model.Genre // Your Genre class
import com.ilustris.sagai.ui.theme.brightness
import com.ilustris.sagai.ui.theme.contrast
import com.ilustris.sagai.ui.theme.filters.FantasyColorTones
import com.ilustris.sagai.ui.theme.filters.SciFiColorTones
import com.ilustris.sagai.ui.theme.grayScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

// Shader source string (can be loaded from assets)
// For simplicity here, let's assume it's loaded.
// In a real app, load this once, perhaps in a ViewModel or a top-level singleton.

@Composable
fun loadShaderFromAssetsOnce(assetFileName: String): String? {
    val context = LocalContext.current
    // produceState ensures this runs once and caches the result across recompositions
    // unless assetFileName changes.
    return produceState<String?>(initialValue = null, key1 = assetFileName) {
        Log.d("ShaderLoad", "Attempting to load shader: $assetFileName")
        value =
            try {
                withContext(Dispatchers.IO) {
                    // Perform file I/O on a background thread
                    context.assets.open(assetFileName).use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            reader.readText()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ShaderLoad", "Error loading shader '$assetFileName': ${e.message}", e)
                null
            }
    }.value
}

@Composable
fun Modifier.effectForGenre(
    genre: Genre,
    focusRadius: Float? = null,
    customGrain: Float? = null,
    useFallBack: Boolean = false,
): Modifier {
    // Check if the current Android version is below 33 (Android 13)
    // If it is, return a fallback effect as RenderEffect and RuntimeShader are not available.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || useFallBack) {
        return this.fallbackEffect(genre)
    }

    val agslShaderSource = loadShaderFromAssetsOnce("fantasy_shader.agsl")
    // If the shader source is null (failed to load), return the modifier unmodified
    // or apply a fallback visual.
    if (agslShaderSource == null) {
        Log.w("EffectForGenre", "AGSL Shader source is null. Applying no effect. Applying fallback")
        return this.fallbackEffect(genre)
    }

    // Remember the shader instance itself, it's cheap to create if source is available
    val runtimeShader =
        remember(agslShaderSource) {
            RuntimeShader(agslShaderSource)
        }

    var composableSize by remember { mutableStateOf(IntSize.Zero) }
    // For iTime, if you want animation. If not, you can pass a constant or omit setting it.
    var timeState by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        // This will run only once or if the key changes
        // This is a simple way to animate time. For more complex animations,
        // you might use Animatable or other animation APIs.
        // If your shader doesn't use iTime for visual changes, you can simplify this.
        while (true) {
            timeState = (System.currentTimeMillis() % 100000L) / 1000.0f
            kotlinx.coroutines.delay(16) // Aim for ~60 FPS
        }
    }

    // Define shader parameters based on Genre
    // These values will need careful tuning!
    // The values you had for saturation, brightness, contrast will need to be re-evaluated
    // in the context of how the AGSL shader handles them (e.g., 1.0 is neutral for contrast).
    val fantasyPalette = FantasyColorTones.ETHEREAL_CYAN_STARLIGHT
    val cyberpunkPalette = SciFiColorTones.CYBERPUNK_NEON_NIGHT
    val uniformValues =
        remember(genre) {
            // Recalculate only when genre changes
            when (genre) {
                Genre.FANTASY ->
                    ShaderParams(
                        grainIntensity = customGrain ?: 0.15f,
                        bloomThreshold = 0.30f,
                        bloomIntensity = 0.20f,
                        bloomRadius = 4.5f,
                        softFocusRadius = focusRadius ?: 1.25f,
                        saturation = 0.60f, // e.g., slightly desaturated
                        contrast = 1.55f, // e.g., slightly increased contrast
                        brightness = 0f, // e.g., slightly brighter
                        highlightTint = fantasyPalette.highlightTint, // Warm highlights
                        shadowTint = fantasyPalette.shadowTint, // Cool shadows
                        tintStrength = fantasyPalette.defaultTintStrength,
                        vignetteStrength = 0.2f,
                        vignetteSoftness = 0.7f,
                    )
                Genre.SCI_FI ->
                    ShaderParams(
                        grainIntensity = customGrain ?: 0.15f,
                        bloomThreshold = 0.30f,
                        bloomIntensity = 0.15f,
                        bloomRadius = 2.0f,
                        softFocusRadius = focusRadius ?: 0.10f,
                        saturation = 0.40f, // More desaturated for Sci-Fi
                        contrast = 1.50f, // Higher contrast
                        brightness = -0.10f, // Slightly darker
                        highlightTint = cyberpunkPalette.highlightTint, // Cyan/Cool highlights
                        shadowTint = cyberpunkPalette.shadowTint, // Slightly warm/muted shadows
                        tintStrength = cyberpunkPalette.defaultTintStrength,
                        vignetteStrength = 0.15f,
                        vignetteSoftness = 0.6f,
                    )
                // Add other genres and their specific shader parameters
                else -> ShaderParams() // Default parameters
            }
        }

    return this
        .onSizeChanged { newSize ->
            // Get the size of the composable this modifier is applied to
            composableSize = newSize
        }.graphicsLayer {
            // Apply the shader
            if (composableSize.width > 0 && composableSize.height > 0) {
                runtimeShader.setFloatUniform("iResolution", composableSize.width.toFloat(), composableSize.height.toFloat())
                runtimeShader.setFloatUniform("iTime", timeState) // Pass current time

                // Set all your custom uniforms based on ShaderParams
                runtimeShader.setFloatUniform("u_grainIntensity", uniformValues.grainIntensity)
                runtimeShader.setFloatUniform("u_bloomThreshold", uniformValues.bloomThreshold)
                runtimeShader.setFloatUniform("u_bloomIntensity", uniformValues.bloomIntensity)
                runtimeShader.setFloatUniform("u_bloomRadius", uniformValues.bloomRadius)
                runtimeShader.setFloatUniform("u_softFocusRadius", uniformValues.softFocusRadius)
                runtimeShader.setFloatUniform("u_saturation", uniformValues.saturation)
                runtimeShader.setFloatUniform("u_contrast", uniformValues.contrast)
                runtimeShader.setFloatUniform("u_brightness", uniformValues.brightness)
                runtimeShader.setFloatUniform(
                    "u_highlightTint",
                    uniformValues.highlightTint.first,
                    uniformValues.highlightTint.second,
                    uniformValues.highlightTint.third,
                )
                runtimeShader.setFloatUniform(
                    "u_shadowTint",
                    uniformValues.shadowTint.first,
                    uniformValues.shadowTint.second,
                    uniformValues.shadowTint.third,
                )
                runtimeShader.setFloatUniform("u_tintStrength", uniformValues.tintStrength)
                runtimeShader.setFloatUniform("u_vignetteStrength", uniformValues.vignetteStrength)
                runtimeShader.setFloatUniform("u_vignetteSoftness", uniformValues.vignetteSoftness)

                renderEffect =
                    RenderEffect
                        .createRuntimeShaderEffect(runtimeShader, "composable_shader")
                        .asComposeRenderEffect()
            } else {
                renderEffect = null // Avoid applying shader if size is invalid
            }
        }
}

// Data class to hold shader parameters for clarity
data class ShaderParams(
    val grainIntensity: Float = 0.0f,
    val bloomThreshold: Float = 0.8f,
    val bloomIntensity: Float = 0.0f,
    val bloomRadius: Float = 3.0f,
    val softFocusRadius: Float = 0.0f,
    val saturation: Float = 1.0f,
    val contrast: Float = 1.0f,
    val brightness: Float = 0.0f,
    val highlightTint: Triple<Float, Float, Float> = Triple(1f, 1f, 1f), // R, G, B
    val shadowTint: Triple<Float, Float, Float> = Triple(0f, 0f, 0f), // R, G, B
    val tintStrength: Float = 0.0f,
    val vignetteStrength: Float = 0.0f,
    val vignetteSoftness: Float = 0.5f,
    // Add any other uniforms your shader uses
)

@Composable
fun Modifier.fallbackEffect(genre: Genre): Modifier {
    val saturation =
        when (genre) {
            Genre.FANTASY -> .6f
            Genre.SCI_FI -> .4f
        }

    val brightness =
        when (genre) {
            Genre.FANTASY -> .02f
            Genre.SCI_FI -> (.01f).unaryMinus()
        }

    val contrast =
        when (genre) {
            Genre.FANTASY -> .2f
            Genre.SCI_FI -> .85f
        }

    return this
        .grayScale(saturation)
        .brightness(brightness)
        .contrast(contrast)
}
