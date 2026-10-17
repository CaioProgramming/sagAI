package com.ilustris.sagai.ui.components.titlefx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.Typeface
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

private val shaderSources = ConcurrentHashMap<String, String>()

@RequiresApi(33)
private fun createTitleShader(
    context: Context,
    cfg: TitleFxConfig,
): RuntimeShader? =
    runCatching {
        val source =
            shaderSources.getOrPut(cfg.asset) {
                context.assets
                    .open(cfg.asset)
                    .bufferedReader()
                    .use { it.readText() }
            }
        RuntimeShader(source)
    }.onFailure { Timber.e(it, "Title effect shader '${cfg.asset}' failed, using the simple title") }
        .getOrNull()

/** Uniform setters that survive a name the compiler optimised away. */
@RequiresApi(33)
private class FxUniforms(
    private val shader: RuntimeShader,
) {
    private val missing = HashSet<String>()

    private inline fun guard(
        name: String,
        block: () -> Unit,
    ) {
        if (name in missing) return
        try {
            block()
        } catch (e: IllegalArgumentException) {
            missing += name
        }
    }

    fun f(
        name: String,
        a: Float,
    ) = guard(name) { shader.setFloatUniform(name, a) }

    fun f(
        name: String,
        a: Float,
        b: Float,
    ) = guard(name) { shader.setFloatUniform(name, a, b) }

    fun f(
        name: String,
        v: FloatArray,
    ) = guard(name) {
        if (v.size == 3) shader.setFloatUniform(name, v[0], v[1], v[2]) else shader.setFloatUniform(name, v)
    }

    fun f(
        name: String,
        a: Float,
        b: Float,
        c: Float,
        d: Float,
    ) = guard(name) { shader.setFloatUniform(name, a, b, c, d) }

    fun arr(
        name: String,
        v: FloatArray,
    ) = guard(name) { shader.setFloatUniform(name, v) }
}

private data class FxLayoutKey(
    val text: String,
    val fontPx: Float,
    val width: Int,
    val height: Int,
    val genre: Genre,
)

/** Everything layout decides and drawing needs; written in measure, read in draw. */
private class FxHolder {
    var layout: TextLayoutResult? = null
    var key: FxLayoutKey? = null
    var width = 0
    var height = 0
    var padTop = 0f
    var padBottom = 0f
    var padSide = 0f
    var topLeft = Offset.Zero
    var fontPx = 0f

    // built lazily from the layout, rebuilt when the key changes
    var builtKey: FxLayoutKey? = null
    var maskShader: BitmapShader? = null
    var fieldShader: BitmapShader? = null
    var textY = 0.5f
    var textH = 0.2f
    var txMin = 0.1f
    var txMax = 0.9f

    // punk rock: ransom-note layers repainted while the title animates
    var ransom: RansomNotePainter? = null
    var ransomMask: Bitmap? = null
    var ransomColor: Bitmap? = null
    var colorShader: BitmapShader? = null
    var lastT = 0f
}

/** The genre's own header font first, then system faces, so every tile looks cut from a different page. */
private fun ransomTypefaces(header: Typeface?): List<Typeface> =
    listOfNotNull(
        header,
        Typeface.create("serif", Typeface.BOLD),
        Typeface.create("sans-serif-black", Typeface.NORMAL),
        Typeface.create("sans-serif-condensed", Typeface.BOLD),
        Typeface.create("monospace", Typeface.BOLD),
        Typeface.create("serif", Typeface.BOLD_ITALIC),
    )

/**
 * Genre title drawn by an AGSL shader (API 33+): flames, fog, brand, katana cut, iridescence, comic
 * punch. The shader outputs light on a transparent layer, so the text sits on whatever is behind it
 * and everything stays inside this composable's bounds (the layout reserves a per-genre margin for
 * the effect to fade out in). Prototype and decisions: `docs/prototypes/genre-titles` and
 * `docs/features/genre_title_effects.md`.
 *
 * [fallback] is shown when the shader cannot be created (compile error on exotic GPUs).
 */
@RequiresApi(33)
@Composable
internal fun GenreTitleFx(
    text: String,
    genre: Genre,
    color: Color,
    style: TextStyle,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    playEntry: Boolean = true,
    fallback: @Composable () -> Unit,
) {
    val cfg = genre.titleFxConfig()
    val context = LocalContext.current
    val shader = remember(genre) { cfg?.let { createTitleShader(context, it) } }
    if (cfg == null || shader == null) {
        fallback()
        return
    }

    val remotePalette = genre.colorPalette()
    val palette = remember(genre, color, remotePalette) { titleFxPalette(genre, color, remotePalette) }
    val state = remember(genre, interactive, playEntry) { TitleFxState(cfg, interactive, playEntry) }
    val uniforms = remember(shader) { FxUniforms(shader) }
    val holder = remember { FxHolder() }
    val fontResolver = LocalFontFamilyResolver.current
    holder.ransom =
        remember(cfg.ransom, style.fontFamily) {
            if (!cfg.ransom) {
                null
            } else {
                val header = style.fontFamily?.let { runCatching { fontResolver.resolve(it).value as? Typeface }.getOrNull() }
                RansomNotePainter(ransomTypefaces(header))
            }
        }
    val paint = remember { Paint().apply { isAntiAlias = false } }
    val measurer = rememberTextMeasurer()
    val tick = remember(state) { mutableLongStateOf(0L) }
    val active = rememberLifecycleAnimationsActive()

    LaunchedEffect(active, state) {
        if (!active) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last == 0L) {
                    last = now
                } else {
                    val dt = (now - last) / 1_000_000_000f
                    if (dt >= MIN_FRAME_SECONDS) {
                        state.update(min(dt, 0.05f))
                        last = now
                        tick.longValue++
                    }
                }
            }
        }
    }

    Layout(
        modifier =
            modifier
                .semantics { contentDescription = text }
                .pointerInput(interactive, state) {
                    if (!interactive) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val h = size.height.toFloat().coerceAtLeast(1f)
                        state.aspect = w / h
                        state.onDown(down.position.x / w, 1f - down.position.y / h, down.uptimeMillis)
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            state.onMove(change.position.x / w, 1f - change.position.y / h, change.uptimeMillis)
                        }
                        state.onUp()
                    }
                }.drawBehind {
                    if (tick.longValue < 0L) return@drawBehind // read so every tick redraws
                    drawTitle(holder, state, uniforms, shader, paint, palette, cfg)
                },
    ) { _, constraints ->
        measureTitle(holder, measurer, text, style, genre, cfg, constraints)
        layout(holder.width, holder.height) {}
    }
}

private const val MIN_FRAME_SECONDS = 1f / 32f

private fun Density.measureTitle(
    holder: FxHolder,
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    genre: Genre,
    cfg: TitleFxConfig,
    constraints: Constraints,
) {
    val baseSize = if (style.fontSize.isSpecified) style.fontSize else 34.sp
    val available = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity

    fun measureAt(fontSize: TextUnit): TextLayoutResult {
        val side = cfg.padSide * fontSize.toPx() * 0.8f
        val inner = if (available == Constraints.Infinity) Constraints.Infinity else max(1, available - (2 * side).toInt())
        return measurer.measure(
            text = text,
            style = style.copy(fontSize = fontSize, color = Color.White),
            // the ransom note is a single row of tiles, like the lab
            maxLines = if (cfg.ransom) 1 else 2,
            constraints = Constraints(maxWidth = inner),
        )
    }

    // a word too long for the line gets broken between letters ("CYBERPU / NK"); shrink instead
    fun TextLayoutResult.breaksWord(): Boolean =
        (0 until lineCount - 1).any { line ->
            val end = getLineEnd(line)
            end in 1 until text.length && text[end - 1].isLetterOrDigit() && text[end].isLetterOrDigit()
        }

    // shrink until the title fits in at most two lines, like AutoResizeText did
    var size = baseSize
    var layout = measureAt(size)
    var tries = 0
    while ((layout.didOverflowWidth || layout.didOverflowHeight || layout.breaksWord()) && tries < 12) {
        size *= 0.9f
        layout = measureAt(size)
        tries++
    }

    var inkL = Float.MAX_VALUE
    var inkR = 0f
    for (i in 0 until layout.lineCount) {
        inkL = min(inkL, layout.getLineLeft(i))
        inkR = max(inkR, layout.getLineRight(i))
    }
    if (inkR <= inkL) {
        inkL = 0f
        inkR = layout.size.width.toFloat()
    }
    val inkW = inkR - inkL
    val th = layout.size.height * 0.72f
    val padTop = cfg.padTop * th
    val padBottom = cfg.padBottom * th
    val padSide = cfg.padSide * th

    val width =
        if (constraints.hasBoundedWidth && constraints.minWidth == constraints.maxWidth) {
            constraints.maxWidth
        } else {
            val natural = ceil(inkW + 2 * padSide).toInt()
            natural.coerceIn(constraints.minWidth, if (constraints.hasBoundedWidth) constraints.maxWidth else natural)
        }
    val height = ceil(padTop + layout.size.height + padBottom).toInt().coerceIn(constraints.minHeight, constraints.maxHeight.coerceAtLeast(constraints.minHeight))

    holder.layout = layout
    holder.width = width
    holder.height = height
    holder.padTop = padTop
    holder.padBottom = padBottom
    holder.padSide = padSide
    holder.fontPx = size.toPx()
    holder.topLeft = Offset(width / 2f - (inkL + inkR) / 2f, padTop)
    holder.key = FxLayoutKey(text, holder.fontPx, width, height, genre)
}

@RequiresApi(33)
private fun DrawScope.rebuildAssets(
    holder: FxHolder,
    layout: TextLayoutResult,
    cfg: TitleFxConfig,
    key: FxLayoutKey,
) {
    holder.builtKey = key
    holder.maskShader = null
    holder.fieldShader = null
    // a colour layer left from Punk Rock would hit a shader that has no uCol
    holder.colorShader = null
    holder.ransomMask = null
    holder.ransomColor = null
    runCatching {
        val w = holder.width
        val h = holder.height
        val ransom = holder.ransom
        val mask =
            if (ransom != null) {
                // the field (glow, halo) is built once from every tile shown; the layers repaint as it animates
                val m = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val c = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                ransom.paintIn(holder, m, c, layout, t = 0f, entry = 100f, textColor = floatArrayOf(1f, 1f, 1f), density = density)
                holder.ransomMask = m
                holder.ransomColor = c
                m
            } else {
                buildTitleMask(w, h, layout, holder.topLeft, holder.fontPx * cfg.stroke, this)
            }
        val field = buildTitleField(mask, cfg.smearLen, layout.lineCount)
        holder.maskShader =
            BitmapShader(mask, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
                setFilterMode(BitmapShader.FILTER_MODE_LINEAR)
            }
        holder.fieldShader =
            BitmapShader(field.bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
                setFilterMode(BitmapShader.FILTER_MODE_LINEAR)
                setLocalMatrix(
                    Matrix().apply {
                        setScale(w.toFloat() / field.bitmap.width, h.toFloat() / field.bitmap.height)
                    },
                )
            }
        holder.textY = 1f - (field.ink.minY + field.ink.maxY) / 2f
        holder.textH = max(0.05f, field.ink.maxY - field.ink.minY)
        holder.txMin = field.ink.minX
        holder.txMax = field.ink.maxX
    }.onFailure { Timber.e(it, "Title effect assets failed") }
}

@RequiresApi(33)
private fun DrawScope.drawTitle(
    holder: FxHolder,
    state: TitleFxState,
    u: FxUniforms,
    shader: RuntimeShader,
    paint: Paint,
    palette: TitleFxPalette,
    cfg: TitleFxConfig,
) {
    val layout = holder.layout ?: return
    val key = holder.key ?: return
    val w = holder.width
    val h = holder.height
    if (w <= 0 || h <= 0) return
    if (holder.builtKey != key) rebuildAssets(holder, layout, cfg, key)
    val ransom = holder.ransom
    if (ransom != null) updateRansom(holder, ransom, layout, state, palette)
    val mask = holder.maskShader ?: return
    val field = holder.fieldShader ?: return

    state.aspect = w.toFloat() / h
    state.textWOverH = (holder.txMax - holder.txMin) * w / h

    u.f("uTime", state.t)
    u.f("uIntensity", 1f)
    u.f("uHeight", 0.85f)
    u.f("uTextY", holder.textY)
    u.f("uTextH", holder.textH)
    u.f("uRes", w.toFloat(), h.toFloat())
    u.f("uC1", palette.c1)
    u.f("uC2", palette.c2)
    u.f("uC3", palette.c3)
    u.f("uC4", palette.c4)
    u.f("uHalo", palette.halo)
    u.f("uText", palette.text)
    u.f("uOpt", cfg.opts)
    u.f("uEvtA", state.evtA)
    u.f("uEvtB", state.evtB)
    u.f("uSeedB", state.seedB)
    u.f("uSeedA", state.seedA)
    u.f("uEntry", state.entryTime)
    u.f("uEntryDur", cfg.entryDur)
    u.f("uTx", holder.txMin, holder.txMax)
    // the whole composable is the box: the effect fades out over the reserved margins
    u.f("uBox", 0f, 0f, 1f, 1f)
    u.f("uFw", holder.padSide * 0.85f, holder.padTop * 0.85f, holder.padBottom * 0.85f, 0f)
    u.f("uBoxCfg", 1f, 1f)
    u.arr("uTrail", state.trailBuf)
    u.arr("uTrailV", state.trailVBuf)
    u.f("uPtr", state.pointerX, state.pointerY, state.pointerDown, state.pointerActive)
    u.f("uCut", state.cutX, state.cutY, state.cutAng, if (state.cutUser && cfg.dragCut) 1f else 0f)

    runCatching {
        shader.setInputShader("uMask", mask)
        shader.setInputShader("uS", field)
        holder.colorShader?.let { shader.setInputShader("uCol", it) }
    }.onFailure { return }
    paint.shader = shader
    drawIntoCanvas { it.nativeCanvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint) }
}

private fun RansomNotePainter.paintIn(
    holder: FxHolder,
    mask: Bitmap,
    color: Bitmap,
    layout: TextLayoutResult,
    t: Float,
    entry: Float,
    textColor: FloatArray,
    density: Float,
) = paint(
    mask = mask,
    color = color,
    text = layout.layoutInput.text.text,
    cx = holder.width / 2f,
    cy = holder.topLeft.y + layout.size.height / 2f,
    maxWidth = holder.width - 2f * holder.padSide,
    lineHeight = layout.size.height.toFloat(),
    t = t,
    entry = entry,
    textColor = textColor,
    density = density,
)

/** Pushes the tiles away from the finger and repaints the ransom layers when they change. */
@RequiresApi(33)
private fun DrawScope.updateRansom(
    holder: FxHolder,
    ransom: RansomNotePainter,
    layout: TextLayoutResult,
    state: TitleFxState,
    palette: TitleFxPalette,
) {
    val mask = holder.ransomMask ?: return
    val color = holder.ransomColor ?: return
    val dt = (state.t - holder.lastT).coerceIn(0f, 0.1f)
    holder.lastT = state.t
    val moved = ransom.step(dt, state.pointerX * holder.width, (1f - state.pointerY) * holder.height, state.pointerActive)
    if (!ransom.needsPaint(state.t, state.entryTime, moved, System.currentTimeMillis()) && holder.colorShader != null) return
    ransom.paintIn(holder, mask, color, layout, state.t, state.entryTime, palette.text, density)
    holder.maskShader = linearShader(mask)
    holder.colorShader = linearShader(color)
}

private fun linearShader(bitmap: Bitmap) =
    BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
        setFilterMode(BitmapShader.FILTER_MODE_LINEAR)
    }
