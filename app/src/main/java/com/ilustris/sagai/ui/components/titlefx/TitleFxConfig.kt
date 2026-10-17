package com.ilustris.sagai.ui.components.titlefx

import android.os.Build
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Per-genre settings of the animated title. Values come straight from the HTML lab
 * (`docs/prototypes/genre-titles`); pads are the layout margins, in multiples of the text height,
 * that the effect is allowed to live in.
 */
internal class TitleFxConfig(
    val asset: String,
    /** Seconds of the entry animation (0 = the effect itself is the entry). */
    val entryDur: Float,
    /** Period of the recurring event A (cowboy brand, shinobi katana cut), 0 = none. */
    val evtPeriod: Float = 0f,
    val tapEvt: Boolean = false,
    val dragCut: Boolean = false,
    val padTop: Float,
    val padBottom: Float,
    val padSide: Float,
    val smearLen: Float,
    /** Fake-bold stroke, as a fraction of the font size, so thin display fonts survive the glow. */
    val stroke: Float,
    val opts: FloatArray,
    /** Punk Rock: the mask and a colour layer are ransom-note tiles painted by [RansomNotePainter]. */
    val ransom: Boolean = false,
)

internal fun Genre.titleFxConfig(): TitleFxConfig? =
    when (this) {
        Genre.FANTASY ->
            TitleFxConfig(
                "genre_title_fantasy.agsl", 1.6f, padTop = 1.6f, padBottom = 0.3f, padSide = 0.3f,
                smearLen = 1.5f, stroke = 0.02f, opts = floatArrayOf(0f, 0f, 0f),
            )
        Genre.HORROR ->
            TitleFxConfig(
                "genre_title_horror.agsl", 2.6f, padTop = 0.6f, padBottom = 1.4f, padSide = 0.3f,
                smearLen = 1.2f, stroke = 0.03f, opts = floatArrayOf(1f, 1f, 0f),
            )
        Genre.COWBOY ->
            TitleFxConfig(
                "genre_title_cowboy.agsl", 0f, evtPeriod = 11f, padTop = 1.0f, padBottom = 0.4f, padSide = 0.3f,
                smearLen = 1.1f, stroke = 0.02f, opts = floatArrayOf(1f, 0f, 0f),
            )
        Genre.SHINOBI ->
            TitleFxConfig(
                "genre_title_shinobi.agsl", 3.0f, evtPeriod = 7f, dragCut = true, padTop = 1.6f, padBottom = 0.4f,
                padSide = 0.3f, smearLen = 1.1f, stroke = 0.015f, opts = floatArrayOf(1f, 1f, 0f),
            )
        Genre.CRIME ->
            TitleFxConfig(
                "genre_title_crime.agsl", 3.0f, padTop = 0.5f, padBottom = 0.5f, padSide = 0.4f,
                smearLen = 0.45f, stroke = 0.035f, opts = floatArrayOf(0f, 0f, 0f),
            )
        Genre.HEROES ->
            TitleFxConfig(
                "genre_title_heroes.agsl", 0.8f, tapEvt = true, padTop = 0.7f, padBottom = 0.7f, padSide = 0.6f,
                smearLen = 0.6f, stroke = 0.015f, opts = floatArrayOf(0f, 0f, 0f),
            )
        // opts.x would show the lab's CRT screen (dark glass filling the title box): off, the title is only
        // the text and its effect. The app's header fonts are already heavy, so no fake bold (the lab's
        // 0.05 was for its thin stand-in fonts)
        Genre.CYBERPUNK ->
            TitleFxConfig(
                "genre_title_cyberpunk.agsl", 1.6f, padTop = 0.5f, padBottom = 0.5f, padSide = 0.4f,
                smearLen = 0.5f, stroke = 0f, opts = floatArrayOf(0f, 0f, 0f),
            )
        Genre.SPACE_OPERA ->
            TitleFxConfig(
                "genre_title_space_opera.agsl", 1.4f, padTop = 0.5f, padBottom = 0.5f, padSide = 0.4f,
                smearLen = 0.5f, stroke = 0f, opts = floatArrayOf(0f, 0f, 0f),
            )
        Genre.PUNK_ROCK ->
            TitleFxConfig(
                "genre_title_punk_rock.agsl", 1.9f, padTop = 0.4f, padBottom = 0.4f, padSide = 0.35f,
                smearLen = 0.3f, stroke = 0f, opts = floatArrayOf(0f, 0f, 0f), ransom = true,
            )
        else -> null
    }

/**
 * Whether [stylisedText][com.ilustris.sagai.ui.components.stylisedText] draws this genre with the
 * AGSL title effect. AGSL needs API 33; below that a simple styled text (font, shadow, shimmer)
 * is used instead.
 */
fun Genre.usesTitleFx(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && titleFxConfig() != null
