package com.ilustris.sagai.features.saga.chat.ui.components.decoration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.theme.components.chat.CyberpunkChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.SpaceChatBubbleShape
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.sagaBrush

/**
 * Picks the most vivid (saturation × brightness) stop out of the live
 * [com.ilustris.sagai.core.ai.model.GenreVisualConfig] palette (`Genre.colorPalette()`) instead of
 * a hardcoded hex or a fixed array index — indices aren't consistent across genres (some palettes
 * are 3 stops, some are 8+, with dark/pale filler stops at the ends), so a fixed index picked one
 * genre's accent correctly and another genre's near-black filler stop by accident. Falls back to
 * the theme-resolved primary only if the palette is ever completely empty.
 *
 * This is a good *default* when there's no specific reason to deviate — but it's fine for any
 * genre's decoration to use a deliberately hand-picked color instead (including tones that don't
 * appear in the live palette at all, or different tones for light/dark or user/NPC) when that
 * reads better. Only the bubble's own dominant fill color needs to trace back to remote config;
 * ornamental accents are a template design choice, not a config-managed property.
 */
@Composable
private fun genreVividAccent(genre: Genre): Color {
    val palette = genre.colorPalette()
    val vivid =
        palette.maxByOrNull { color ->
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(color.toArgb(), hsv)
            hsv[1] * hsv[2]
        }
    return vivid ?: MaterialTheme.colorScheme.primary
}

/**
 * Extra ornamentation for a genre's chat bubble, drawn OUTSIDE the bubble's clipped content as a
 * sibling in an unclipped [Box] — so it can hang past the shape's edge without ever covering the
 * message text, regardless of how long the text is.
 *
 * This `Box`-based slot doesn't reserve extra layout space for a decoration that overflows the
 * bubble's bounds (an `offset()`-ed child doesn't grow its parent) — see
 * [chatBubbleConstraintDecorationOverlay] for the `ConstraintLayout`-based alternative that does,
 * currently used by Horror only while that pattern is being validated in isolation.
 *
 * Returns null for genres that don't have a decoration yet; callers must fall back to today's
 * plain bubble rendering in that case (this is the safety net while genres are designed one at a
 * time — see the chat-bubble-decorations project notes).
 */
@Composable
fun Genre.chatBubbleDecorationOverlay(
    shape: Shape,
    isUser: Boolean,
): (@Composable BoxScope.() -> Unit)? =
    when (this) {
        Genre.HEROES -> {
            { HeroesInkOutline(shape, isUser) }
        }

        else -> {
            null
        }
    }

/**
 * Ornamentation drawn BEHIND the bubble's clipped content — a sibling rendered *before* it in the
 * same unclipped [Box], so the bubble's own opaque background naturally occludes whatever part of
 * this overlaps the shape. Only useful for elements meant to peek out from behind a corner,
 * unlike [chatBubbleDecorationOverlay] which always sits on top.
 *
 * Returns null for genres without a background layer.
 */
@Composable
fun Genre.chatBubbleBackgroundDecoration(
    shape: Shape,
    isUser: Boolean,
): (@Composable BoxScope.() -> Unit)? =
    when (this) {
        Genre.HEROES -> {
            { HeroesFlatShadow(shape, isUser) }
        }

        else -> {
            null
        }
    }

/**
 * `ConstraintLayout`-based sibling of [chatBubbleDecorationOverlay] — positioned via
 * `constrainAs`/`linkTo` against [content] (the bubble's own wrapping box) instead of `Box`+
 * `align`+`offset`, so the surrounding `ConstraintLayout` actually expands to include a
 * decoration that hangs past the bubble's edge, rather than silently drawing over/under
 * whatever space happened to already be there.
 *
 * First validated on Horror only (2026-07-29) — a first attempt converting every genre at once
 * broke all of them with a runtime `IllegalStateException: Path not defined` that turned out to be
 * an unrelated red herring (see project notes); the isolated Horror retry worked, and this slot
 * now also covers Shinobi, copying the same pattern.
 *
 * Returns null for genres not using this slot — callers must fall back to the plain `Box` path
 * (see [chatBubbleDecorationOverlay]) in that case.
 */
@Composable
fun Genre.chatBubbleConstraintDecorationOverlay(
    shape: Shape,
    isUser: Boolean,
): (@Composable ConstraintLayoutScope.(ConstrainedLayoutReference) -> Unit)? =
    when (this) {
        Genre.HORROR -> {
            { content -> HorrorCloudOverlay(content, isUser) }
        }

        Genre.SHINOBI -> {
            { content -> ShinobiBlossomOverlay(content, isUser) }
        }

        Genre.FANTASY -> {
            { content -> FantasyDragonOverlay(content, shape, isUser) }
        }

        Genre.PUNK_ROCK -> {
            { content -> PunkRockOverlay(content) }
        }

        Genre.COWBOY -> {
            { content -> CowboyOverlay(content, isUser) }
        }

        Genre.CYBERPUNK -> {
            { content -> CyberpunkOverlay(content, shape, isUser) }
        }

        Genre.SPACE_OPERA -> {
            { content -> SpaceOperaOverlay(content, shape, isUser) }
        }

        else -> {
            null
        }
    }

/**
 * `ConstraintLayout`-based sibling of [chatBubbleBackgroundDecoration], rendered BEHIND
 * [content]. See [chatBubbleConstraintDecorationOverlay]'s doc for why this exists as a separate
 * slot rather than folding into the `Box`-based functions.
 */
@Composable
fun Genre.chatBubbleConstraintBackgroundDecoration(
    shape: Shape,
    isUser: Boolean,
): (@Composable ConstraintLayoutScope.(ConstrainedLayoutReference) -> Unit)? =
    when (this) {
        Genre.HORROR -> {
            { content -> HorrorMoonBackground(content, isUser) }
        }

        Genre.SHINOBI -> {
            { content -> ShinobiBranchBackground(content, isUser) }
        }

        Genre.FANTASY -> {
            { content -> FantasyFlamesBackground(content, isUser) }
        }

        else -> {
            null
        }
    }

/**
 * Name/role tag shown above a bubble. Only meaningful for left-aligned (NPC) messages — the
 * player's own right-aligned bubbles are always the same single character, so a tag there adds
 * no disambiguation value. Callers should only invoke this for non-user messages.
 *
 * Returns null for genres without a decoration yet.
 */
@Composable
fun Genre.chatBubbleNameTag(name: String): (@Composable () -> Unit)? =
    when (this) {
        Genre.CYBERPUNK -> {
            { CyberpunkNameTag(name) }
        }

        Genre.FANTASY -> {
            { FantasyNameTag(name) }
        }

        Genre.SHINOBI -> {
            { ShinobiNameTag(name) }
        }

        Genre.SPACE_OPERA -> {
            { SpaceOperaNameTag(name) }
        }

        else -> {
            null
        }
    }

/**
 * Outline-only overlay for the Cyberpunk bubble: a single thin, crisp stroke traced along the
 * *existing*
 * [CyberpunkChatBubbleShape][com.ilustris.sagai.ui.theme.components.chat.CyberpunkChatBubbleShape]
 * (reused as-is), tinted `primary`. No glow, no floating corner elements — a reference "Twitch
 * cyberpunk chat widget" template carries its whole identity through a crisp single-color border +
 * chamfered-corner panels, nothing hovering outside them. Corner brackets were tried first and
 * didn't read well at bubble scale; this crisp-outline approach matches the reference directly.
 */
@Composable
private fun BoxScope.CyberpunkBubbleOverlay(shape: Shape) {
    val accent = MaterialTheme.colorScheme.primary

    Box(
        Modifier
            .matchParentSize()
            .drawWithContent {
                drawContent()
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = accent,
                    style = Stroke(width = 1.2.dp.toPx()),
                )
            },
    )
}

/**
 * A small icon with a flat, hard-edged outline behind it — 4 tinted duplicates offset a couple of
 * px in each direction, then the real tinted icon drawn on top. Gives roughly the same "icon pops
 * off the background" read as a stroke/glow would, without touching [dropShadow] or any other
 * RenderEffect-based API (see [SpaceOperaBubbleOverlay]'s doc for why that's banned inside chat
 * bubble decorations — it crashed the app after sustained recomposition). Same idea as
 * [HeroesFlatShadow], just wrapping the icon itself instead of the whole bubble shape.
 */
@Composable
private fun FlatStrokedIcon(
    painter: Painter,
    tint: Color,
    strokeColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        listOf(
            (-1.4).dp to 0.dp,
            1.4.dp to 0.dp,
            0.dp to (-1.4).dp,
            0.dp to 1.4.dp,
        ).forEach { (dx, dy) ->
            Image(
                painter,
                null,
                colorFilter = ColorFilter.tint(strokeColor),
                modifier =
                    Modifier
                        .matchParentSize()
                        .offset(dx, dy),
            )
        }
        Image(
            painter,
            null,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.matchParentSize(),
        )
    }
}

/**
 * Rendered IN FRONT of the bubble (the [chatBubbleConstraintDecorationOverlay] slot) — wraps the
 * existing [CyberpunkBubbleOverlay] (crisp outline, unchanged) inside a `Box` constrained to fill
 * [content] exactly, then adds a small "signal bars" icon at a bottom corner, straddling the
 * bubble's edge slightly and mirrored by [isUser]. Needed on `ConstraintLayout` (not the plain
 * `Box` slot) specifically because the bars now hang past the bubble's own bounds — see the
 * project notes (2026-07-30) for the Punk Rock clipping bug this exact pattern fixes.
 */
@Composable
private fun ConstraintLayoutScope.CyberpunkOverlay(
    content: ConstrainedLayoutReference,
    shape: Shape,
    isUser: Boolean,
) {
    val accent = MaterialTheme.colorScheme.primary
    val outlineOverlay = createRef()
    Box(
        Modifier.constrainAs(outlineOverlay) {
            top.linkTo(content.top)
            start.linkTo(content.start)
            end.linkTo(content.end)
            bottom.linkTo(content.bottom)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
        },
    ) {
        CyberpunkBubbleOverlay(shape)
    }

    val bars = createRef()
    FlatStrokedIcon(
        painter = painterResource(R.drawable.ic_cyberpunk_bars),
        tint = accent,
        strokeColor = MaterialTheme.colorScheme.background,
        modifier =
            Modifier.constrainAs(bars) {
                bottom.linkTo(content.bottom, margin = 16.dp)
                if (isUser) {
                    end.linkTo(content.end, margin = (-10).dp)
                } else {
                    start.linkTo(content.start, margin = (-10).dp)
                }
                width = Dimension.value(34.dp)
                height = Dimension.value(26.dp)
            },
    )
}

/**
 * Reuses [CyberpunkChatBubbleShape] with `drawTail = false` for the tag background — the same
 * chamfered-corner language as the bubble and the avatar (see [[avatarShape]]), instead of a
 * generic rounded rect, so the whole message row reads as one panel system.
 */
@Composable
private fun CyberpunkNameTag(name: String) {
    val accent = MaterialTheme.colorScheme.primary
    val tagShape = remember { CyberpunkChatBubbleShape(cornerRadius = 4.dp, drawTail = false) }
    Row(
        modifier =
            Modifier
                .padding(start = 4.dp, bottom = 4.dp)
                .background(accent.darker(.3f), tagShape)
                .border(1.dp, accent, tagShape)
                .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            name.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
        )
    }
}

/**
 * "Holographic CRT" overlay for the Space Opera bubble: traced along the *existing*
 * [SpaceChatBubbleShape][com.ilustris.sagai.ui.theme.components.chat.SpaceChatBubbleShape]
 * (reused as-is, already fully chamfered with no tail path — see [[avatarShape]]), combined with a
 * lower-opacity bubble fill (see `BubbleStyle.userBubble`/`characterBubble` in `ChatBubble.kt`,
 * genre-gated the same way) so the panel reads as translucent glass. The border uses
 * [Color.gradientFade] instead of a flat stroke — solid at the top, fading toward the bottom — for
 * a lit-glass-surface highlight instead of a uniform outline.
 *
 * Deliberately no [dropShadow] here (2026-07-29): it caused the app to crash after a few minutes
 * of an open chat screen with active typing — `dropShadow` recomputes a blur/RenderEffect on every
 * recomposition, and typing re-triggers recomposition on every keystroke, so the allocation churn
 * built up over time until the app died. Same crisp-outline-only tactic already used for
 * Cyberpunk's overlay (see `CyberpunkBubbleOverlay`), which never used `dropShadow` and has no
 * such issue. If a future genre wants a soft glow, cache/`remember` the `Shadow` object rather
 * than rebuilding it inline, or avoid `dropShadow` for anything inside a per-message, frequently
 * recomposed composable like a chat bubble.
 */
@Composable
private fun BoxScope.SpaceOperaBubbleOverlay(shape: Shape) {
    val accent = MaterialTheme.colorScheme.primary

    Box(
        Modifier
            .matchParentSize()
            .drawWithContent {
                drawContent()
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    brush = accent.gradientFade(),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            },
    )
}

/**
 * Rendered IN FRONT of the bubble — wraps the existing [SpaceOperaBubbleOverlay] (gradient
 * outline, unchanged) the same way [CyberpunkOverlay] wraps [CyberpunkBubbleOverlay], then adds a
 * decorative star straddling a top corner so it visually "breaks" the outline (roughly half
 * inside, half outside — a reference "screenshot frame with a sparkle poking through the corner"
 * template), mirrored by [isUser]. The star gets [FlatStrokedIcon]'s flat offset-duplicate outline
 * instead of a real stroke/shadow API, for the same crash-avoidance reason documented on
 * [SpaceOperaBubbleOverlay].
 */
@Composable
private fun ConstraintLayoutScope.SpaceOperaOverlay(
    content: ConstrainedLayoutReference,
    shape: Shape,
    isUser: Boolean,
) {
    val accent = MaterialTheme.colorScheme.primary
    val outlineOverlay = createRef()
    Box(
        Modifier.constrainAs(outlineOverlay) {
            top.linkTo(content.top)
            start.linkTo(content.start)
            end.linkTo(content.end)
            bottom.linkTo(content.bottom)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
        },
    ) {
        SpaceOperaBubbleOverlay(shape)
    }

    val starPainter = painterResource(R.drawable.ic_space_star)

    // Top corner star — deeply overlapping into the bubble now, reading as part of the panel
    // itself rather than a decoration "breaking" the outline from outside.
    val starTop = createRef()
    FlatStrokedIcon(
        painter = starPainter,
        tint = accent,
        strokeColor = MaterialTheme.colorScheme.background,
        modifier =
            Modifier.constrainAs(starTop) {
                bottom.linkTo(content.top, margin = (-18).dp)
                if (isUser) {
                    end.linkTo(content.end, margin = (-10).dp)
                } else {
                    start.linkTo(content.start, margin = (-10).dp)
                }
                width = Dimension.value(26.dp)
                height = Dimension.value(32.dp)
            },
    )

    // Second star at the exact opposite (diagonal) corner — same overlap treatment, mirrored on
    // both axes so it lands bottom-opposite-side from the top star.
    val starBottom = createRef()
    FlatStrokedIcon(
        painter = starPainter,
        tint = accent,
        strokeColor = MaterialTheme.colorScheme.background,
        modifier =
            Modifier.constrainAs(starBottom) {
                bottom.linkTo(content.bottom, margin = 18.dp)
                if (isUser) {
                    start.linkTo(content.start, margin = (-10).dp)
                } else {
                    end.linkTo(content.end, margin = (-10).dp)
                }
                width = Dimension.value(26.dp)
                height = Dimension.value(32.dp)
            },
    )
}

/**
 * Same reuse pattern as [CyberpunkNameTag]: the bubble's own shape (already tail-less, see
 * [[avatarShape]]) as the tag background instead of a generic rounded rect, with the
 * [Color.gradientFade] border matching [SpaceOperaBubbleOverlay]'s "lit glass" edge.
 */
@Composable
private fun SpaceOperaNameTag(name: String) {
    val accent = MaterialTheme.colorScheme.primary
    val tagShape = remember { SpaceChatBubbleShape(cutSize = 2.dp, largeCutSize = 4.dp) }
    Row(
        modifier =
            Modifier
                .padding(start = 4.dp, bottom = 4.dp)
                .background(accent.copy(alpha = .55f), tagShape)
                .border(1.dp, accent.gradientFade(), tagShape)
                .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            name.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
        )
    }
}

/**
 * A solid, hard-edged offset silhouette of the bubble shape — a classic comic-book "ink
 * separation" shadow, deliberately NOT a blurred [dropShadow]: real printed comic panels get a
 * flat spot-color shadow, not a soft glow. Pure black for both bubble types — real comic panels
 * don't color-code the balloon border/shadow by speaker, alignment + avatar already do that job.
 * [isUser] is accepted for signature symmetry with the other genres' decoration slots but unused.
 */
@Composable
private fun BoxScope.HeroesFlatShadow(
    shape: Shape,
    @Suppress("UNUSED_PARAMETER") isUser: Boolean,
) {
    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    Box(
        Modifier
            .matchParentSize()
            .offset(6.dp, 6.dp)
            .background(shadowColor, shape),
    )
}

/**
 * A thick, bold black ink outline traced along the *existing*
 * — pure black for both bubble types, matching real comic reference art directly (no per-speaker
 * tint; alignment + avatar already differentiate speakers). [isUser] is accepted for signature
 * symmetry with the other genres' decoration slots but unused.
 */
@Composable
private fun BoxScope.HeroesInkOutline(
    shape: Shape,
    @Suppress("UNUSED_PARAMETER") isUser: Boolean,
) {
    val outlineColor = MaterialTheme.colorScheme.onSurface
    Box(
        Modifier
            .matchParentSize()
            .drawWithContent {
                drawContent()
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = outlineColor,
                    style = Stroke(width = 2.5.dp.toPx()),
                )
            },
    )
}

/**
 * Three hand-drawn-style icons (lightning bolt, skull, stars — user-sourced Noun Project SVGs,
 * mechanically converted, sketchy/imperfect linework already fits Punk Rock's ink aesthetic as-is)
 * scattered at the bubble's corners, entirely outside the shape's own bounds — "ao redor da
 * bubble sem sobrepor" was the explicit brief, so unlike the dragon (Fantasy) or moon/cloud
 * (Horror) nothing here overlaps the bubble on purpose.
 *
 * Uses the `ConstraintLayout` slot (not plain `Box`+`align`+`offset`) even though nothing
 * overlaps — a `Box`-positioned child that hangs outside its parent's bounds via `offset()`
 * doesn't grow that parent, so the surrounding layout was clipping these icons down to slivers.
 * `ConstraintLayout` actually reserves the space these need, same reason Horror/Shinobi/Fantasy
 * use it. See project notes (2026-07-30) for this exact bug.
 */
@Composable
private fun ConstraintLayoutScope.PunkRockOverlay(content: ConstrainedLayoutReference) {
    val accent = MaterialTheme.colorScheme.primary

    val bolt = createRef()
    Image(
        painterResource(R.drawable.ic_lightning_bolt),
        null,
        colorFilter = ColorFilter.tint(accent),
        modifier =
            Modifier
                .constrainAs(bolt) {
                    bottom.linkTo(content.top, margin = 4.dp)
                    start.linkTo(content.start, margin = (-6).dp)
                    width = Dimension.value(16.dp)
                    height = Dimension.value(20.dp)
                }
                .gradientFill(sagaBrush()),
    )

    val stars = createRef()
    Image(
        painterResource(R.drawable.ic_punk_stars),
        null,
        colorFilter = ColorFilter.tint(accent),
        modifier =
            Modifier
                .constrainAs(stars) {
                    bottom.linkTo(content.top, margin = 2.dp)
                    end.linkTo(content.end, margin = (-6).dp)
                    width = Dimension.value(14.dp)
                    height = Dimension.value(17.dp)
                }
                .gradientFill(sagaBrush()),
    )

    val skull = createRef()
    Image(
        painterResource(R.drawable.ic_punk_skull),
        null,
        colorFilter = ColorFilter.tint(accent),
        modifier =
            Modifier
                .constrainAs(skull) {
                    top.linkTo(content.bottom, margin = 4.dp)
                    end.linkTo(content.end, margin = (-8).dp)
                    width = Dimension.value(18.dp)
                    height = Dimension.value(22.dp)
                }
                .gradientFill(sagaBrush()),
    )
}

/**
 * A cowboy hat perched above one top corner and a running horse below the opposite... actually the
 * *same* side, one top one bottom — "como se a bubble estivesse com um chapeuzinho" (the bubble
 * "wearing" a little hat) up top, and the horse below, both floating just outside the bubble with
 * a small gap rather than integrating into it like [FantasyDragonOverlay] or
 * [ShinobiBranchBackground] do — explicit brief: "não vão se integrar... ambos vão ficar acima"
 * (sit above/outside, not blended in). Mirrors by [isUser] the same side for both (end for the
 * player's own bubble, start for NPC), so hat+horse read as a matched pair near one corner rather
 * than scattered on opposite sides. Plain neutral silhouette color (`onBackground`), not a genre
 * accent — a hat/horse silhouette doesn't need to be "themed", it's just an icon.
 */
@Composable
private fun ConstraintLayoutScope.CowboyOverlay(
    content: ConstrainedLayoutReference,
    isUser: Boolean,
) {
    val silhouette = MaterialTheme.colorScheme.onBackground

    val hat = createRef()
    Image(
        painterResource(R.drawable.ic_cowboy_hat),
        null,
        colorFilter = ColorFilter.tint(silhouette),
        modifier =
            Modifier.constrainAs(hat) {
                bottom.linkTo(content.top, margin = 4.dp)
                if (isUser) {
                    end.linkTo(content.end, margin = (-6).dp)
                } else {
                    start.linkTo(content.start, margin = (-6).dp)
                }
                width = Dimension.value(30.dp)
                height = Dimension.value(24.dp)
            },
    )

    val horse = createRef()
    Image(
        painterResource(R.drawable.ic_cowboy_horse),
        null,
        colorFilter = ColorFilter.tint(silhouette),
        modifier =
            Modifier.constrainAs(horse) {
                top.linkTo(content.bottom, margin = 4.dp)
                if (isUser) {
                    end.linkTo(content.end, margin = (-8).dp)
                } else {
                    start.linkTo(content.start, margin = (-8).dp)
                }
                width = Dimension.value(28.dp)
                height = Dimension.value(22.dp)
            },
    )
}

@Composable
private fun fantasyAccentColor(): Color = genreVividAccent(Genre.FANTASY)

/**
 * Thin gilded outline traced along the *existing*
 * [FantasyChatBubbleShape][com.ilustris.sagai.ui.theme.components.chat.FantasyChatBubbleShape]
 * (reused as-is) plus a few small warm "magic mote" stars scattered near the border.
 *
 * No [dropShadow] anywhere in here (2026-07-29) — same reason as [SpaceOperaBubbleOverlay]'s doc:
 * it caused a crash after sustained recomposition (typing in chat re-triggers it repeatedly). The
 * outline now uses [Color.gradientFade] as a brush instead of a solid color + shadow, which keeps
 * a soft "warm glow" read without an actual blur effect. The stars lose their glow entirely rather
 * than trade it for a brush trick — a tinted icon alone still reads fine at this size.
 */
@Composable
private fun BoxScope.FantasyBubbleOverlay(shape: Shape) {
    val gold = fantasyAccentColor()

    Box(
        Modifier
            .matchParentSize()
            .drawWithContent {
                drawContent()
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    brush = gold.gradientFade(),
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
    )

    val starOffsets =
        listOf(
            Alignment.TopStart to Pair((-8).dp, (-4).dp),
            Alignment.TopEnd to Pair(4.dp, (-8).dp),
            Alignment.BottomEnd to Pair(6.dp, 6.dp),
        )
    starOffsets.forEach { (alignment, offsets) ->
        Image(
            painterResource(R.drawable.ic_spark),
            null,
            colorFilter = ColorFilter.tint(gold),
            modifier =
                Modifier
                    .align(alignment)
                    .offset(offsets.first, offsets.second)
                    .size(11.dp),
        )
    }
}

/**
 * Rendered IN FRONT of the bubble (the [chatBubbleConstraintDecorationOverlay] slot) — the dragon
 * body curling along the bottom of the panel, overlapping up into it like [HorrorCloudOverlay]
 * does. Wraps [FantasyBubbleOverlay] (the outline + stars) inside the same constrained `content`
 * box, since Fantasy switching to the `ConstraintLayout` slot means the old plain-`Box` overlay
 * slot no longer fires for this genre — this is the one place that combined visual now lives.
 *
 * Mirrors by [isUser] the same way [ShinobiBranchBackground] does: which corner it's anchored to
 * AND the artwork itself via a horizontal `scale` flip, since the dragon has a clear directional
 * curl and would read as backwards on the un-mirrored side otherwise.
 */
@Composable
private fun ConstraintLayoutScope.FantasyDragonOverlay(
    content: ConstrainedLayoutReference,
    shape: Shape,
    isUser: Boolean,
) {
    val gold = fantasyAccentColor()

    val dragon = createRef()
    Image(
        painterResource(R.drawable.ic_dragon_body),
        null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        modifier =
            Modifier
                .constrainAs(dragon) {
                    bottom.linkTo(content.bottom, margin = 6.dp)
                    if (isUser) {
                        start.linkTo(content.start, margin = (-14).dp)
                    } else {
                        end.linkTo(content.end, margin = (-14).dp)
                    }
                }.size(64.dp)
                .scale(scaleX = if (!isUser) -1f else 1f, scaleY = 1f),
    )
}

/**
 * Rendered BEHIND the bubble (the [chatBubbleConstraintBackgroundDecoration] slot) — three small
 * flame tips of varying size peeking up from behind the top edge, like [HorrorMoonBackground]'s
 * overlap technique, scattered across the width so the panel reads as "on fire" rather than
 * having one single flame. Not mirrored by [isUser] (unlike the dragon) — flames scattered across
 * the whole top edge look the same regardless of which side the bubble is aligned to.
 */
@Composable
private fun ConstraintLayoutScope.FantasyFlamesBackground(
    content: ConstrainedLayoutReference,
    isUser: Boolean,
) {
    val gold = fantasyAccentColor()

    val flameStart = createRef()
    Image(
        painterResource(R.drawable.ic_fire_flame),
        null,
        colorFilter = ColorFilter.tint(gold),
        modifier =
            Modifier.constrainAs(flameStart) {
                bottom.linkTo(content.top, margin = (-8).dp)
                start.linkTo(content.start, margin = 10.dp)
                width = Dimension.value(10.dp)
                height = Dimension.value(13.dp)
            },
    )

    val flameCenter = createRef()
    Image(
        painterResource(R.drawable.ic_fire_flame),
        null,
        colorFilter = ColorFilter.tint(gold),
        modifier =
            Modifier.constrainAs(flameCenter) {
                bottom.linkTo(content.top, margin = (-12).dp)
                centerHorizontallyTo(content)
                width = Dimension.value(13.dp)
                height = Dimension.value(17.dp)
            },
    )

    val flameEnd = createRef()
    Image(
        painterResource(R.drawable.ic_fire_flame),
        null,
        colorFilter = ColorFilter.tint(gold),
        modifier =
            Modifier.constrainAs(flameEnd) {
                bottom.linkTo(content.top, margin = (-6).dp)
                end.linkTo(content.end, margin = 14.dp)
                width = Dimension.value(9.dp)
                height = Dimension.value(11.dp)
            },
    )
}

/**
 * Small irregular/ragged silhouette — same fixed-offset jitter technique already proven in
 * [com.ilustris.sagai.ui.theme.components.chat.FantasyChatBubbleShape] for the bubble itself,
 * just applied to a much smaller tag shape. No randomness, no complex math: a handful of
 * hand-picked points instead of straight edges.
 */
private val fantasyParchmentTagShape =
    GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val j = 3f
        moveTo(0f, j)
        lineTo(w * 0.2f, 0f)
        lineTo(w * 0.8f, j * 0.6f)
        lineTo(w, 0f)
        lineTo(w - j, h * 0.5f)
        lineTo(w, h - j * 0.6f)
        lineTo(w * 0.75f, h)
        lineTo(w * 0.25f, h - j)
        lineTo(0f, h)
        lineTo(j * 0.6f, h * 0.5f)
        close()
    }

/**
 * Name tag styled like a torn parchment strip, with a thin vine tendril curling from its
 * bottom-left edge down toward the bubble below (a decorative line, not a precisely-measured
 * connector between two separate composables — the tag and bubble sit only 4dp apart in the
 * Column, so a short curl reads as "connected" without needing cross-composable position math).
 */
@Composable
private fun FantasyNameTag(name: String) {
    val gold = fantasyAccentColor()
    Box(
        modifier =
            Modifier
                .padding(start = 6.dp, bottom = 14.dp)
                .drawWithContent {
                    drawContent()
                    val startX = 6.dp.toPx()
                    val startY = size.height
                    val vine =
                        Path().apply {
                            moveTo(startX, startY)
                            cubicTo(
                                startX - 5.dp.toPx(),
                                startY + 5.dp.toPx(),
                                startX + 3.dp.toPx(),
                                startY + 9.dp.toPx(),
                                startX - 2.dp.toPx(),
                                startY + 14.dp.toPx(),
                            )
                        }
                    drawPath(vine, color = gold.copy(alpha = .75f), style = Stroke(width = 1.3.dp.toPx()))
                },
    ) {
        Row(
            modifier =
                Modifier
                    .background(Color.Black.copy(alpha = .82f), fantasyParchmentTagShape)
                    .border(1.dp, gold.copy(alpha = .7f), fantasyParchmentTagShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                name.uppercase(),
                color = Color.White,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
            )
        }
    }
}

@Composable
private fun shinobiAccentColor(): Color = genreVividAccent(Genre.SHINOBI)

/**
 * Rendered BEHIND the bubble (the [chatBubbleConstraintBackgroundDecoration] slot) — a sakura
 * branch anchored at the bottom corner, overlapping up into the bubble's fill so it reads as
 * growing out of the panel itself rather than floating beside it. Deliberately restrained
 * compared to Cyberpunk/Fantasy: no glow, no outline trace — Shinobi's aesthetic is ink-wash/
 * sumi-e negative space ("Ma"), not neon.
 *
 * Mirrors with [isUser] on BOTH axes: which corner it's anchored to (same start/end flip as
 * [HorrorMoonBackground]/[HorrorCloudOverlay]) AND the artwork itself via a horizontal `scale`
 * flip. A branch has a clear directional lean (drawn drooping down toward one side) — repositioning
 * it to the opposite corner without also mirroring the sprite would read as backwards/broken on
 * that side, unlike the moon/cloud, which are visually symmetric enough not to need it.
 */
@Composable
private fun ConstraintLayoutScope.ShinobiBranchBackground(
    content: ConstrainedLayoutReference,
    isUser: Boolean,
) {
    val ink = shinobiAccentColor()
    val branch = createRef()
    Box(
        Modifier
            .constrainAs(branch) {
                bottom.linkTo(content.bottom, margin = 2.dp)
                if (isUser) {
                    end.linkTo(content.end, margin = (-16).dp)
                } else {
                    start.linkTo(content.start, margin = (-16).dp)
                }
            }.size(20.dp)
            .clipToBounds(),
    ) {
        Image(
            painterResource(R.drawable.ic_sakura_branch),
            null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier =
                Modifier
                    .fillMaxSize()
                    .scale(scaleX = if (isUser) -1f else 1f, scaleY = 1f)
                    .rotate(if (isUser) 15f else -15f),
        )
    }
}

/**
 * Rendered IN FRONT of the bubble (the [chatBubbleConstraintDecorationOverlay] slot) — a small
 * blossom sitting just outside the same corner as [ShinobiBranchBackground], like it bloomed right
 * at the branch's tip. Kept fully outside the bubble's bounds (unlike the branch, which overlaps
 * on purpose) so it never risks sitting over the message text. Mirrors corner with [isUser] the
 * same way the branch does; a blossom is round/symmetric so its artwork doesn't need flipping.
 */
@Composable
private fun ConstraintLayoutScope.ShinobiBlossomOverlay(
    content: ConstrainedLayoutReference,
    isUser: Boolean,
) {
    val ink = shinobiAccentColor()
    val blossom = createRef()
    Image(
        painterResource(R.drawable.ic_sakura_blossom),
        null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
        modifier =
            Modifier.constrainAs(blossom) {
                bottom.linkTo(content.bottom, margin = (-6).dp)
                if (isUser) {
                    end.linkTo(content.end, margin = (-4).dp)
                } else {
                    start.linkTo(content.start, margin = (-4).dp)
                }
                width = Dimension.value(14.dp)
                height = Dimension.value(14.dp)
            },
    )
}

/**
 * Sharp corners (no [RoundedCornerShape], matches the bubble's own live 0dp corner size), a thin
 * ink border instead of a glow, and a small blossom as a seal-like prefix next to the name.
 */
@Composable
private fun ShinobiNameTag(name: String) {
    val ink = shinobiAccentColor()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .padding(start = 4.dp, bottom = 4.dp)
                .background(MaterialTheme.colorScheme.primary.darker(.3f), MaterialTheme.shapes.small)
                .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Image(
            painterResource(R.drawable.ic_sakura_blossom),
            null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier.size(10.dp),
        )
        Text(
            name.uppercase(),
            color = Color.White,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
        )
    }
}

/**
 * Hand-picked, not sourced from the live palette (see [genreVividAccent]'s doc) — a cool, sickly
 * pale-yellow reads as moonlight better than whatever happens to be the most saturated stop in
 * Horror's live gradient. Deliberately desaturated/pale to feel "wan" rather than vivid.
 */
private val HorrorMoonColor = Color(0xFFF4E9C1)

/**
 * Hand-picked: a pale, near-white grey (Material grey_200) reads as a lit fog wisp far better than
 * the earlier blue-gray, which was too close to the bubble's own fill to stay legible in front of
 * it — this sits in front of the bubble, so it needs more contrast than a background element does.
 */
private val HorrorCloudColor = Color(0xFFEEEEEE)

/**
 * Rendered BEHIND the bubble (see [chatBubbleConstraintBackgroundDecoration]) so the shape's own
 * opaque fill naturally occludes the part that sinks into it, reading as a moon glimpsed behind
 * the panel rather than an icon drawn on top of it. Positioned with `constrainAs`/`linkTo`
 * against [content] (not `Box`+`align`+`offset`) so the enclosing `ConstraintLayout` actually
 * grows to include it instead of just drawing past whatever space happened to already be there.
 *
 * Deliberately overlaps the bubble's top corner (negative bottom margin) rather than floating
 * clear above it. Anchored to the *opposite* horizontal corner from [HorrorCloudOverlay] and
 * flipped by [isUser] — for the player's own bubble the moon sits at the end corner and the cloud
 * at the start corner; for an NPC bubble it's mirrored (moon at start, cloud at end) — so the two
 * elements never stack on the same corner regardless of which side the bubble is aligned to.
 */
@Composable
private fun ConstraintLayoutScope.HorrorMoonBackground(
    content: ConstrainedLayoutReference,
    isUser: Boolean,
) {
    val moon = createRef()
    Image(
        painterResource(R.drawable.ic_pixel_moon),
        null,
        colorFilter = ColorFilter.tint(HorrorMoonColor.copy(alpha = .9f)),
        modifier =
            Modifier.constrainAs(moon) {
                bottom.linkTo(content.top, margin = (-16).dp)
                if (isUser) {
                    end.linkTo(content.end, margin = (-8).dp)
                } else {
                    start.linkTo(content.start, margin = (-8).dp)
                }
                width = Dimension.value(30.dp)
                height = Dimension.value(38.dp)
            },
    )
}

/**
 * Rendered IN FRONT of the bubble (the [chatBubbleConstraintDecorationOverlay] slot) — a small
 * pixel-art cloud drifting across the bottom corner, on top of the shape. Deliberately overlaps
 * the bubble (positive bottom margin pulls it up into the shape) instead of floating below it.
 * See [HorrorMoonBackground]'s doc for the [isUser] corner-mirroring rule shared by both.
 */
@Composable
private fun ConstraintLayoutScope.HorrorCloudOverlay(
    content: ConstrainedLayoutReference,
    isUser: Boolean,
) {
    val cloud = createRef()
    Image(
        painterResource(R.drawable.ic_pixel_cloud),
        null,
        colorFilter = ColorFilter.tint(HorrorCloudColor),
        modifier =
            Modifier.constrainAs(cloud) {
                bottom.linkTo(content.bottom, margin = 8.dp)
                if (isUser) {
                    start.linkTo(content.start, margin = (-10).dp)
                } else {
                    end.linkTo(content.end, margin = (-10).dp)
                }
                width = Dimension.value(30.dp)
                height = Dimension.value(19.dp)
            },
    )
}
