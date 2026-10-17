package com.ilustris.sagai.ui.components.titlefx

import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.math.max
import kotlin.math.min

/**
 * Colours handed to the title shaders. Every palette starts from the single text colour the genre
 * provides (so remote config recolours the effect), exactly like the HTML lab in
 * `docs/prototypes/genre-titles`. Each entry is an RGB triple in 0..1.
 */
internal class TitleFxPalette(
    val text: FloatArray,
    val c1: FloatArray,
    val c2: FloatArray,
    val c3: FloatArray,
    val c4: FloatArray,
    val halo: FloatArray,
)

private val WHITE = floatArrayOf(1f, 1f, 1f)

private fun rgb(color: Color) = floatArrayOf(color.red, color.green, color.blue)

private fun rgb2hsl(c: FloatArray): FloatArray {
    val r = c[0]
    val g = c[1]
    val b = c[2]
    val mx = max(r, max(g, b))
    val mn = min(r, min(g, b))
    val l = (mx + mn) / 2f
    var h = 0f
    var s = 0f
    val d = mx - mn
    if (d > 1e-6f) {
        s = if (l > 0.5f) d / (2f - mx - mn) else d / (mx + mn)
        h =
            when (mx) {
                r -> (g - b) / d + (if (g < b) 6f else 0f)
                g -> (b - r) / d + 2f
                else -> (r - g) / d + 4f
            }
        h *= 60f
    }
    return floatArrayOf(h, s, l)
}

private fun hsl(
    hDeg: Float,
    s: Float,
    l: Float,
): FloatArray {
    val h = (((hDeg % 360f) + 360f) % 360f) / 360f
    val a = s * min(l, 1f - l)

    fun f(n: Float): Float {
        val k = (n + h * 12f) % 12f
        return l - a * max(-1f, min(min(k - 3f, 9f - k), 1f))
    }
    return floatArrayOf(f(0f), f(8f), f(4f))
}

private fun mix(
    a: FloatArray,
    b: FloatArray,
    t: Float,
) = floatArrayOf(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t)

/** Dark brand colours (wine red, saddle brown) need lifting to read as light on a dark stage. */
private fun lift(t: FloatArray): FloatArray {
    val x = rgb2hsl(t)
    return if (x[2] < 0.58f) hsl(x[0], x[1], 0.62f) else t
}

/**
 * [color] is the title colour. Fantasy, Horror, Cowboy, Shinobi and Punk Rock derive their whole palette
 * from it. Crime, Heroes, Cyberpunk and Space Opera use the genre's remote palette, so they would ignore
 * it; when [tinted] says the caller chose that colour on purpose (a character's own colour), their
 * palette is turned toward its hue instead, keeping the genre's saturation and lightness.
 */
internal fun titleFxPalette(
    genre: Genre,
    color: Color,
    remote: List<Color>,
    tinted: Boolean = false,
): TitleFxPalette {
    val palette =
        when (genre) {
            Genre.FANTASY -> return paletteFire(color)
            Genre.HORROR -> return paletteHorror(color)
            Genre.COWBOY -> return paletteCowboy(color)
            Genre.SHINOBI -> return paletteShinobi(color)
            Genre.PUNK_ROCK -> return palettePunk(color)
            Genre.CRIME -> paletteCrime(color, remote)
            Genre.CYBERPUNK -> paletteCyber(color, remote)
            Genre.SPACE_OPERA -> paletteSpace(color, remote)
            else -> paletteHeroes(color, remote)
        }
    return if (tinted) palette.turnedToward(color) else palette
}

/** Rotates the hue of the gradient colours so its main one lands on [color]'s hue; the halo stays. */
private fun TitleFxPalette.turnedToward(color: Color): TitleFxPalette {
    val target = rgb2hsl(rgb(color))
    val pivot = rgb2hsl(c2)
    if (target[1] < 0.08f) return this // a grey has no hue to turn toward
    val delta = target[0] - pivot[0]

    fun turn(c: FloatArray): FloatArray {
        val x = rgb2hsl(c)
        return if (x[1] < 0.06f) c else hsl(x[0] + delta, x[1], x[2])
    }
    return TitleFxPalette(
        text = lift(rgb(color)),
        c1 = turn(c1),
        c2 = turn(c2),
        c3 = turn(c3),
        c4 = turn(c4),
        halo = halo,
    )
}

private fun paletteFire(color: Color): TitleFxPalette {
    val t = lift(rgb(color))
    val x = rgb2hsl(t)
    val hb = if (x[1] < 0.12f) 22f else x[0]
    val sb = max(x[1], 0.75f)
    val c3 = hsl(hb, sb, 0.62f)
    return TitleFxPalette(
        text = t,
        c1 = hsl(hb - 50f, sb * 0.9f, 0.24f),
        c2 = hsl(hb - 16f, sb, 0.5f),
        c3 = c3,
        c4 = mix(t, WHITE, 0.22f),
        halo = mix(hsl(hb + 170f, 0.8f, 0.68f), c3, 0.35f),
    )
}

private fun paletteHorror(color: Color): TitleFxPalette {
    val t = lift(rgb(color))
    val x = rgb2hsl(t)
    val h = if (x[1] < 0.06f) 200f else x[0]
    val sat = min(0.9f, max(0.3f, x[1] * 1.2f + 0.25f))
    return TitleFxPalette(
        text = t,
        c1 = hsl(h, sat, 0.07f),
        c2 = hsl(h, sat, 0.22f),
        c3 = hsl(h, sat, 0.42f),
        c4 = hsl(h + 6f, sat * 0.8f, 0.8f),
        halo = hsl(h, sat, 0.4f),
    )
}

private fun paletteCowboy(color: Color): TitleFxPalette {
    val t = lift(rgb(color))
    val x = rgb2hsl(t)
    val h = if (x[1] < 0.12f) 24f else x[0]
    return TitleFxPalette(
        text = t,
        c1 = hsl(h - 18f, 0.9f, 0.2f),
        c2 = hsl(h - 8f, 1f, 0.46f),
        c3 = hsl(h + 6f, 1f, 0.58f),
        c4 = mix(hsl(h + 14f, 1f, 0.82f), WHITE, 0.3f),
        halo = hsl(h - 6f, 1f, 0.5f),
    )
}

private fun paletteShinobi(color: Color): TitleFxPalette {
    val t = lift(rgb(color))
    val x = rgb2hsl(t)
    val h = if (x[1] < 0.18f) 352f else x[0]
    return TitleFxPalette(
        text = t,
        c1 = floatArrayOf(0.1f, 0.1f, 0.14f),
        c2 = mix(floatArrayOf(0.40f, 0.42f, 0.48f), t, 0.14f),
        c3 = mix(floatArrayOf(0.68f, 0.70f, 0.76f), t, 0.16f),
        c4 = t,
        halo = hsl(h, 0.85f, 0.55f),
    )
}

/** Crime uses the remote palette `[bg, deep, pink, hot, light, mint]` when it is long enough. */
private fun paletteCrime(
    color: Color,
    remote: List<Color>,
): TitleFxPalette {
    val t = lift(rgb(color))
    if (remote.size >= 6) {
        val c2 = rgb(remote[4])
        return TitleFxPalette(
            text = t,
            c1 = rgb(remote[3]),
            c2 = c2,
            c3 = rgb(remote[5]),
            c4 = lift(rgb(remote[1])),
            halo = c2,
        )
    }
    val x = rgb2hsl(t)
    val h = if (x[1] < 0.12f) 320f else x[0]
    val c2 = hsl(h, 1f, 0.62f)
    return TitleFxPalette(
        text = t,
        c1 = hsl(h - 60f, 1f, 0.58f),
        c2 = c2,
        c3 = hsl(h + 45f, 1f, 0.66f),
        c4 = hsl(h + 90f, 0.95f, 0.7f),
        halo = c2,
    )
}

/** Heroes uses the remote palette `[bg, navy, blue, blue, azure, cyan, gold, ice]` when available. */
private fun paletteHeroes(
    color: Color,
    remote: List<Color>,
): TitleFxPalette {
    val t = rgb(color)
    if (remote.size >= 8) {
        return TitleFxPalette(
            text = t,
            c1 = rgb(remote[1]),
            c2 = rgb(remote[4]),
            c3 = rgb(remote[5]),
            c4 = rgb(remote[7]),
            halo = rgb(remote[6]),
        )
    }
    val h = rgb2hsl(t)[0]
    return TitleFxPalette(
        text = t,
        c1 = hsl(h, 0.8f, 0.16f),
        c2 = hsl(h, 0.9f, 0.58f),
        c3 = hsl(h - 36f, 1f, 0.55f),
        c4 = mix(hsl(h, 0.9f, 0.9f), WHITE, 0.4f),
        halo = hsl(h + 180f, 1f, 0.5f),
    )
}

/** Cyberpunk uses the remote palette `[bg, deep, deeper, primary, violet, cyan, magenta, lavender]`. */
private fun paletteCyber(
    color: Color,
    remote: List<Color>,
): TitleFxPalette {
    val t = lift(rgb(color))
    if (remote.size >= 8) {
        val c4 = rgb(remote[7])
        return TitleFxPalette(text = t, c1 = rgb(remote[4]), c2 = rgb(remote[5]), c3 = rgb(remote[6]), c4 = c4, halo = c4)
    }
    val h = rgb2hsl(t)[0]
    val c4 = mix(t, WHITE, 0.6f)
    return TitleFxPalette(
        text = t,
        c1 = hsl(h, 0.85f, 0.6f),
        c2 = hsl(h + 150f, 1f, 0.55f),
        c3 = hsl(h - 40f, 1f, 0.6f),
        c4 = c4,
        halo = c4,
    )
}

/** Space Opera uses the remote palette `[bg, deep, teal, primary, aqua, cyan, ice, lavender]`. */
private fun paletteSpace(
    color: Color,
    remote: List<Color>,
): TitleFxPalette {
    val t = lift(rgb(color))
    if (remote.size >= 8) {
        val c4 = rgb(remote[6])
        return TitleFxPalette(text = t, c1 = rgb(remote[3]), c2 = rgb(remote[5]), c3 = rgb(remote[7]), c4 = c4, halo = c4)
    }
    val h = rgb2hsl(t)[0]
    val c4 = hsl(h, 0.7f, 0.82f)
    return TitleFxPalette(
        text = t,
        c1 = hsl(h, 0.9f, 0.4f),
        c2 = hsl(h, 0.8f, 0.62f),
        c3 = hsl(h + 60f, 0.8f, 0.8f),
        c4 = c4,
        halo = c4,
    )
}

/** Punk Rock draws its colours in the ransom-note tiles; the shader only needs the text colour. */
private fun palettePunk(color: Color): TitleFxPalette {
    val t = lift(rgb(color))
    return TitleFxPalette(text = t, c1 = t, c2 = t, c3 = t, c4 = t, halo = t)
}
