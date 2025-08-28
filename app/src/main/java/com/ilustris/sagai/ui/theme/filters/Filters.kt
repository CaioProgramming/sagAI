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
import com.ilustris.sagai.ui.theme.filters.HeroColorTones
import com.ilustris.sagai.ui.theme.filters.HorrorColorTones // Import HorrorColorTones
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
    pixelSize: Float? = null,
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
    val fantasyPalette = FantasyColorTones.ETHEREAL_CYAN_STARLIGHT
    val cyberpunkPalette = SciFiColorTones.CYBERPUNK_NEON_NIGHT
    val horrorPalette = HorrorColorTones.MOONLIGHT_MYSTIQUE // Use the defined horror palette
    val heroPalette = HeroColorTones.URBAN_COMIC_VIBRANCY
    val uniformValues =
        remember(genre, pixelSize) {
            // Add pixelSize as a key for remember
            // Recalculate only when genre or pixelSize changes
            when (genre) {
                Genre.FANTASY ->
                    ShaderParams(
                        grainIntensity = customGrain ?: .25f,
                        bloomThreshold = .4f,
                        bloomIntensity = .3f,
                        bloomRadius = 1f,
                        softFocusRadius = focusRadius ?: .7f,
                        saturation = .5f,
                        contrast = 1.5f,
                        brightness = -.03f,
                        highlightTint = fantasyPalette.highlightTint,
                        shadowTint = fantasyPalette.shadowTint,
                        tintStrength = fantasyPalette.defaultTintStrength,
                        vignetteStrength = 0.2f,
                        vignetteSoftness = 0.7f,
                        pixelationBlockSize = 0f,
                        colorTemperature = .15f, // Slightly warm for Fantasy
                    )
                Genre.SCI_FI ->
                    ShaderParams(
                        grainIntensity = customGrain ?: .15f,
                        bloomThreshold = .3f,
                        bloomIntensity = .2f,
                        bloomRadius = 1.3f,
                        softFocusRadius = focusRadius ?: .5f,
                        saturation = .2f,
                        contrast = 1.5f,
                        brightness = -.1f,
                        highlightTint = cyberpunkPalette.highlightTint,
                        shadowTint = cyberpunkPalette.shadowTint,
                        tintStrength = cyberpunkPalette.defaultTintStrength,
                        vignetteStrength = .2f,
                        vignetteSoftness = 1f,
                        pixelationBlockSize = 0.0f,
                        colorTemperature = -.1f, // Slightly cool for Sci-Fi
                    )
                Genre.HORROR ->
                    ShaderParams(
                        grainIntensity = .1f,
                        bloomThreshold = 0.4f, // Less bloom for horror
                        bloomIntensity = 0.1f,
                        bloomRadius = 1.0f,
                        softFocusRadius = 0f, // Subtle soft focus
                        saturation = .5f, // Very desaturated
                        contrast = 1.5f, // High contrast
                        brightness = .1f.unaryMinus(), // Darker mood
                        highlightTint = horrorPalette.highlightTint, // From MOONLIGHT_MYSTIQUE
                        shadowTint = horrorPalette.shadowTint, // From MOONLIGHT_MYSTIQUE
                        tintStrength = horrorPalette.defaultTintStrength, // From MOONLIGHT_MYSTIQUE
                        vignetteStrength = 1f, // Stronger vignette
                        vignetteSoftness = 0.8f,
                        pixelationBlockSize = pixelSize ?: 3.5f,
                        colorTemperature = .3f.unaryMinus(),
                    )
                Genre.HEROES -> {
                    ShaderParams(
                        grainIntensity = customGrain ?: .2f,
                        bloomThreshold = 0f,
                        bloomIntensity = 0f,
                        bloomRadius = 0f,
                        softFocusRadius = focusRadius ?: .2f,
                        saturation = .9f,
                        contrast = 1.3f,
                        brightness = .05f,
                        highlightTint = heroPalette.highlightTint,
                        shadowTint = heroPalette.shadowTint,
                        tintStrength = heroPalette.defaultTintStrength,
                        vignetteStrength = .1f,
                        vignetteSoftness = 1f,
                        pixelationBlockSize = 0.0f,
                        colorTemperature = .15f,
                    )
                }
                else ->
                    ShaderParams()
            }
        }

    return this
        .onSizeChanged { newSize ->
            composableSize = newSize
        }.graphicsLayer {
            if (composableSize.width > 0 && composableSize.height > 0) {
                runtimeShader.setFloatUniform("iResolution", composableSize.width.toFloat(), composableSize.height.toFloat())
                runtimeShader.setFloatUniform("iTime", timeState)
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
                runtimeShader.setFloatUniform("u_pixelationBlockSize", uniformValues.pixelationBlockSize)
                runtimeShader.setFloatUniform("u_colorTemperature", uniformValues.colorTemperature) // Set the new uniform

                renderEffect =
                    RenderEffect
                        .createRuntimeShaderEffect(runtimeShader, "composable_shader")
                        .asComposeRenderEffect()
            } else {
                renderEffect = null
            }
        }
}

data class ShaderParams(
    val grainIntensity: Float = 0.0f,
    val bloomThreshold: Float = 0.8f,
    val bloomIntensity: Float = 0.0f,
    val bloomRadius: Float = 3.0f,
    val softFocusRadius: Float = 0.0f,
    val saturation: Float = 1.0f,
    val contrast: Float = 1.0f,
    val brightness: Float = 0.0f,
    val highlightTint: Triple<Float, Float, Float> = Triple(1f, 1f, 1f),
    val shadowTint: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val tintStrength: Float = 0.0f,
    val vignetteStrength: Float = 0.0f,
    val vignetteSoftness: Float = 0.5f,
    val pixelationBlockSize: Float = 0.0f,
    val colorTemperature: Float = 0.0f,
)

@Composable
fun Modifier.fallbackEffect(genre: Genre): Modifier {
    val saturation =
        when (genre) {
            Genre.FANTASY -> .6f
            Genre.SCI_FI -> .4f
            Genre.HORROR -> 0.1f
            else -> 1.0f
        }

    val brightnessValue =
        when (genre) {
            Genre.FANTASY -> .02f
            Genre.SCI_FI -> -0.01f
            Genre.HORROR -> -0.1f
            else -> 0f
        }

    val contrastValue =
        when (genre) {
            Genre.FANTASY -> 1.2f
            Genre.SCI_FI -> 1.4f
            Genre.HORROR -> 1.6f
            else -> 1.0f
        }

    var modifier: Modifier = this

    if (saturation != 1.0f) {
        modifier = modifier.grayScale(saturation)
    }
    if (brightnessValue != 0f) {
        modifier = modifier.brightness(brightnessValue)
    }
    if (contrastValue != 1.0f) { // Normal contrast is 1.0f
        modifier = modifier.contrast(contrastValue)
    }
    return modifier
}
