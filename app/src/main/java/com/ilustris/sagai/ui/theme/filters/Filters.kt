import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.brightness
import com.ilustris.sagai.ui.theme.contrast
import com.ilustris.sagai.ui.theme.filters.CowboyColorTones
import com.ilustris.sagai.ui.theme.filters.CrimeColorTones
import com.ilustris.sagai.ui.theme.filters.FantasyColorTones
import com.ilustris.sagai.ui.theme.filters.HeroColorTones
import com.ilustris.sagai.ui.theme.filters.HorrorColorTones
import com.ilustris.sagai.ui.theme.filters.SciFiColorTones
import com.ilustris.sagai.ui.theme.filters.ShinobiColorTones
import com.ilustris.sagai.ui.theme.grayScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

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

fun Genre.colorTones() =
    when (this) {
        Genre.FANTASY -> FantasyColorTones.ETHEREAL_CYAN_STARLIGHT
        Genre.CYBERPUNK -> SciFiColorTones.CYBERPUNK_NEON_NIGHT
        Genre.HORROR -> HorrorColorTones.MOONLIGHT_MYSTIQUE
        Genre.HEROES -> HeroColorTones.URBAN_COMIC_VIBRANCY
        Genre.CRIME -> CrimeColorTones.MIAMI_NEON_SUNSET
        Genre.SPACE_OPERA -> FantasyColorTones.CLASSIC_WARM_SUNLIT_FANTASY
        Genre.SHINOBI -> ShinobiColorTones.BLOOD_MOON_ASSASSIN
        Genre.COWBOY -> CowboyColorTones.DESERT_SUNSET
    }

fun Genre.shaderParams(
    customGrain: Float? = null,
    focusRadius: Float? = null,
    pixelSize: Float? = null,
) = when (this) {
    Genre.FANTASY -> {
        ShaderParams(
            grainIntensity = customGrain ?: .2f,
            softFocusRadius = focusRadius ?: .04f,
            saturation = .4f,
            contrast = 1.3f,
            brightness = -0.05f,
            highlightTint = colorTones().highlightTint,
            shadowTint = colorTones().shadowTint,
            tintStrength = colorTones().defaultTintStrength,
            vignetteStrength = 0.2f,
            vignetteSoftness = 0.7f,
            pixelationBlockSize = 0f,
            colorTemperature = .1f,
        )
    }

    Genre.CYBERPUNK -> {
        ShaderParams(
            grainIntensity = customGrain ?: .15f,
            bloomThreshold = .3f,
            bloomIntensity = .2f,
            bloomRadius = 1.3f,
            softFocusRadius = focusRadius ?: .2f,
            saturation = .5f,
            contrast = 1.5f,
            brightness = -.02f,
            highlightTint = colorTones().highlightTint,
            shadowTint = colorTones().shadowTint,
            tintStrength = colorTones().defaultTintStrength,
            vignetteStrength = .3f,
            vignetteSoftness = 1f,
            pixelationBlockSize = 0.0f,
            colorTemperature = .05f.unaryMinus(), // Slightly cool for Sci-Fi
        )
    }

    Genre.HORROR -> {
        ShaderParams(
            grainIntensity = .1f,
            bloomThreshold = 0.4f,
            bloomIntensity = 0.1f,
            bloomRadius = 1.0f,
            softFocusRadius = 0f,
            saturation = .5f,
            contrast = 1.5f,
            brightness = .1f.unaryMinus(),
            highlightTint = colorTones().highlightTint,
            shadowTint = colorTones().shadowTint,
            tintStrength = colorTones().defaultTintStrength,
            vignetteStrength = 1f,
            vignetteSoftness = 0.8f,
            pixelationBlockSize = pixelSize ?: 3.5f,
            colorTemperature = .3f.unaryMinus(),
        )
    }

    Genre.HEROES -> {
        ShaderParams(
            grainIntensity = customGrain ?: .1f,
            bloomThreshold = 0f,
            bloomIntensity = 0f,
            bloomRadius = 0f,
            softFocusRadius = focusRadius ?: .2f,
            saturation = .9f,
            contrast = 1.3f,
            brightness = .05f,
            highlightTint = colorTones().highlightTint,
            shadowTint = colorTones().shadowTint,
            tintStrength = colorTones().defaultTintStrength,
            vignetteStrength = .1f,
            vignetteSoftness = 1f,
            pixelationBlockSize = 0.0f,
            colorTemperature = .15f,
        )
    }

    Genre.CRIME -> {
        ShaderParams(
            grainIntensity = customGrain ?: .1f,
            softFocusRadius = focusRadius ?: .1f,
            saturation = .7f,
            contrast = 1.5f,
            brightness = .01f.unaryMinus(),
            highlightTint = colorTones().highlightTint,
            shadowTint = colorTones().shadowTint,
            tintStrength = colorTones().defaultTintStrength,
            vignetteStrength = .1f,
            vignetteSoftness = 1f,
            pixelationBlockSize = 0.0f,
            colorTemperature = .2f.unaryMinus(),
        )
    }

    Genre.SPACE_OPERA -> {
        ShaderParams(
            grainIntensity = customGrain ?: .3f,
            bloomThreshold = .3f,
            bloomIntensity = .2f,
            bloomRadius = 1.0f,
            softFocusRadius = focusRadius ?: .3f,
            saturation = .85f,
            contrast = 1.5f,
            brightness = 0f,
            highlightTint = colorTones().highlightTint,
            shadowTint = colorTones().shadowTint,
            tintStrength = 0.3f,
            vignetteStrength = .2f,
            vignetteSoftness = 0.9f,
            pixelationBlockSize = 0.0f,
            colorTemperature = .1f.unaryMinus(),
        )
    }

    Genre.SHINOBI -> {
        ShaderParams(
            grainIntensity = customGrain ?: .25f,
            bloomThreshold = .6f,
            bloomIntensity = .1f,
            bloomRadius = .1f,
            softFocusRadius = focusRadius ?: .1f,
            saturation = .6f,
            contrast = 1.4f,
            brightness = .15f.unaryMinus(),
            highlightTint = colorTones().highlightTint,
            shadowTint = colorTones().shadowTint,
            tintStrength = colorTones().defaultTintStrength,
            vignetteStrength = .5f,
            vignetteSoftness = 1f,
            pixelationBlockSize = pixelSize ?: 0.0f,
            colorTemperature = .1f.unaryMinus(),
        )
    }

    Genre.COWBOY -> {
        ShaderParams(
            grainIntensity = customGrain ?: .2f,
            contrast = 1.4f,
            colorTemperature = .2f,
            vignetteStrength = .3f,
            highlightTint = colorTones().highlightTint,
            shadowTint = colorTones().shadowTint,
            tintStrength = colorTones().defaultTintStrength,
            saturation = 0.7f,
            brightness = -.03f,
            softFocusRadius = focusRadius ?: 0.1f,
            vignetteSoftness = 0.8f,
        )
    }

    else -> {
        ShaderParams()
    }
}

@Composable
fun Modifier.effectForGenre(
    genre: Genre,
    focusRadius: Float? = null,
    customGrain: Float? = null,
    pixelSize: Float? = null,
    useFallBack: Boolean = false,
): Modifier {
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
    FantasyColorTones.ETHEREAL_CYAN_STARLIGHT
    SciFiColorTones.CYBERPUNK_NEON_NIGHT
    HorrorColorTones.MOONLIGHT_MYSTIQUE
    HeroColorTones.URBAN_COMIC_VIBRANCY
    CrimeColorTones.MIAMI_NEON_SUNSET
    FantasyColorTones.CLASSIC_WARM_SUNLIT_FANTASY
    val uniformValues =
        remember(genre, pixelSize) {
            genre.shaderParams(
                customGrain = customGrain,
                focusRadius = focusRadius,
                pixelSize = pixelSize,
            )
        }

    return this
        .onSizeChanged { newSize ->
            composableSize = newSize
        }.graphicsLayer {
            if (composableSize.width > 0 && composableSize.height > 0) {
                runtimeShader.setFloatUniform(
                    "iResolution",
                    composableSize.width.toFloat(),
                    composableSize.height.toFloat(),
                )
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
                runtimeShader.setFloatUniform(
                    "u_pixelationBlockSize",
                    uniformValues.pixelationBlockSize,
                )
                runtimeShader.setFloatUniform(
                    "u_colorTemperature",
                    uniformValues.colorTemperature,
                ) // Set the new uniform

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
    val shaderParams = genre.shaderParams()
    val saturation = shaderParams.saturation
    val brightnessValue = shaderParams.brightness
    val contrastValue = shaderParams.contrast

    var modifier: Modifier = this

    if (saturation != 1.0f) {
        modifier = modifier.grayScale(saturation)
    }
    if (brightnessValue != 0f) {
        modifier = modifier.brightness(brightnessValue)
    }
    if (contrastValue != 1.0f) {
        modifier = modifier.contrast(contrastValue)
    }
    return modifier
}
