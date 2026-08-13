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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.components.BorderImage
import com.ilustris.sagai.ui.theme.components.chat.CyberpunkChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.HorrorChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.SpaceChatBubbleShape
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.rotate
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.solidGradient

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
    bubbleColor: Color,
): (@Composable ConstraintLayoutScope.(ConstrainedLayoutReference) -> Unit)? =
    when (this) {
        Genre.HORROR -> {
            { content -> HorrorCloudOverlay(content, isUser) }
        }

        Genre.FANTASY -> {
            { content -> FantasyFlamesBackground(content, isUser, bubbleColor) }
        }

        Genre.SHINOBI -> {
            { content -> ShinobiBlossomOverlay(content, isUser) }
        }

        Genre.PUNK_ROCK -> {
            { content -> PunkRockOverlay(content) }
        }

        Genre.COWBOY -> {
            { content -> CowboyOverlay(content, isUser) }
        }

        Genre.SPACE_OPERA -> {
            { content -> SpaceOperaOverlay(content, shape, isUser, bubbleColor) }
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
    bubbleColor: Color,
): (@Composable ConstraintLayoutScope.(ConstrainedLayoutReference) -> Unit)? =
    when (this) {
        Genre.HORROR -> {
            { content -> HorrorMoonBackground(content, isUser) }
        }

        Genre.SHINOBI -> {
            { content -> ShinobiBranchBackground(content, isUser) }
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
fun Genre.chatBubbleNameTag(
    name: String,
    characterColor: Color,
    bubbleColor: Color,
): (@Composable () -> Unit)? =
    when (this) {
        Genre.CYBERPUNK -> {
            { CyberpunkNameTag(name, characterColor) }
        }

        Genre.FANTASY -> {
            { FantasyNameTag(name, characterColor) }
        }

        Genre.SHINOBI -> {
            { ShinobiNameTag(name, characterColor) }
        }

        Genre.SPACE_OPERA -> {
            { SpaceOperaNameTag(name, characterColor, bubbleColor = bubbleColor) }
        }

        Genre.HORROR -> {
            { HorrorNameTag(name, characterColor) }
        }

        Genre.HEROES -> {
            { HeroesNameTag(name, characterColor) }
        }

        else -> {
            null
        }
    }

/**
 * Reuses [CyberpunkChatBubbleShape] with `drawTail = false` for the tag background — the same
 * chamfered-corner language as the bubble and the avatar (see [[avatarShape]]), instead of a
 * generic rounded rect, so the whole message row reads as one panel system.
 *
 * Tinted with [characterColor] (not the genre `primary`) so each speaking character reads as a
 * distinct color — reference "cyber purple" Twitch widget shows exactly this per-user tinting.
 * Positioned with a downward [Modifier.offset] so it visually overlaps the bubble's top edge
 * instead of floating as a fully separate label above it, matching that same reference.
 */
@Composable
private fun CyberpunkNameTag(
    name: String,
    characterColor: Color,
) {
    val tagShape = remember { CyberpunkChatBubbleShape(cornerRadius = 4.dp, drawTail = false) }
    Row(
        modifier =
            Modifier
                .zIndex(1f)
                .padding(start = 4.dp)
                .offset(y = 10.dp)
                .background(characterColor.darker(.3f), tagShape)
                .border(1.dp, characterColor, tagShape)
                .padding(horizontal = 8.dp, vertical = 3.dp),
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
private fun BoxScope.SpaceOperaBubbleOverlay(
    shape: Shape,
    bubbleColor: Color,
) {
    Box(
        Modifier
            .matchParentSize()
            .drawWithContent {
                drawContent()
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    brush = bubbleColor.solidGradient(),
                    style = Stroke(width = 2.dp.toPx()),
                )
            },
    )
}

/**
 * Rendered IN FRONT of the bubble — wraps the existing [SpaceOperaBubbleOverlay] (gradient
 * outline, unchanged) the same way [CyberpunkOverlay] wraps [CyberpunkBubbleOverlay], then adds a
 * decorative star straddling a top corner so it visually "breaks" the outline (roughly half
 * inside, half outside — a reference "screenshot frame with a sparkle poking through the corner"
 * template), mirrored by [isUser]. The star gets [com.ilustris.sagai.ui.components.BorderImage]'s flat offset-duplicate outline
 * instead of a real stroke/shadow API, for the same crash-avoidance reason documented on
 * [SpaceOperaBubbleOverlay].
 */
@Composable
private fun ConstraintLayoutScope.SpaceOperaOverlay(
    content: ConstrainedLayoutReference,
    shape: Shape,
    isUser: Boolean,
    bubbleColor: Color,
) {
    val accent = bubbleColor
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
        SpaceOperaBubbleOverlay(shape, accent)
    }

    val starPainter = painterResource(R.drawable.ic_space_star)

    // Top corner star — deeply overlapping into the bubble now, reading as part of the panel
    // itself rather than a decoration "breaking" the outline from outside.
    val starTop = createRef()
    BorderImage(
        painter = starPainter,
        tint = accent,
        borderColor = MaterialTheme.colorScheme.background,
        borderWidth = 2.dp,
        modifier =
            Modifier
                .constrainAs(starTop) {
                    bottom.linkTo(content.top, margin = (-20).dp)
                    if (isUser) {
                        end.linkTo(content.end, margin = (-20).dp)
                    } else {
                        start.linkTo(content.start, margin = (-20).dp)
                    }
                }.size(32.dp),
    )

    // Second star at the exact opposite (diagonal) corner — same overlap treatment, mirrored on
    // both axes so it lands bottom-opposite-side from the top star.
    val starBottom = createRef()
    BorderImage(
        painter = starPainter,
        tint = accent,
        borderColor = MaterialTheme.colorScheme.background,
        modifier =
            Modifier.constrainAs(starBottom) {
                bottom.linkTo(content.bottom, (-16).dp)
                if (isUser) {
                    start.linkTo(content.start, (-16).dp)
                } else {
                    end.linkTo(content.end, (-16).dp)
                }
                width = Dimension.value(32.dp)
                height = Dimension.value(32.dp)
            },
    )
}

/**
 * Same reuse pattern as [CyberpunkNameTag]: the bubble's own shape (already tail-less, see
 * [[avatarShape]]) as the tag background instead of a generic rounded rect, with the
 * [Color.gradientFade] border matching [SpaceOperaBubbleOverlay]'s "lit glass" edge. Overlaps the
 * bubble's top edge via `zIndex` + downward `offset`, same as [CyberpunkNameTag].
 */
@Composable
private fun SpaceOperaNameTag(
    name: String,
    characterColor: Color,
    bubbleColor: Color,
) {
    val tagShape = remember { SpaceChatBubbleShape(cutSize = 2.dp, largeCutSize = 4.dp) }
    Row(
        modifier =
            Modifier
                .zIndex(1f)
                .padding(start = 8.dp)
                .offset(y = 16.dp)
                .background(bubbleColor, tagShape)
                .background(characterColor.copy(alpha = .55f), tagShape)
                .border(1.dp, MaterialTheme.colorScheme.background, tagShape)
                .border(1.dp, characterColor, tagShape)
                .padding(horizontal = 8.dp, vertical = 3.dp),
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
    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    Box(
        Modifier
            .matchParentSize()
            .offset(4.dp, 4.dp)
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
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            },
    )
}

/**
 * "Persona"-style role pill — a bold rounded pill filled with [characterColor] and a thick black
 * ink border, matching the reference "Interactive Streaming" chat widget's colored name badges.
 * Doesn't touch [HeroesSpeechBalloonShape][com.ilustris.sagai.ui.theme.components.chat.HeroesSpeechBalloonShape]
 * at all — explicit brief was to adapt only the tag's style, not the balloon shape. Overlaps the
 * balloon's top edge the same way [CyberpunkNameTag] does (`zIndex` + downward `offset`) so the
 * pill visually sits on top of the panel instead of floating as a separate label above it.
 */
@Composable
private fun HeroesNameTag(
    name: String,
    characterColor: Color,
) {
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier =
            Modifier
                .zIndex(1f)
                .padding(start = 8.dp)
                .offset(y = 10.dp)
                .background(characterColor, pillShape)
                .border(2.5.dp, Color.Black, pillShape)
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
                }.gradientFill(sagaBrush()),
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
                }.gradientFill(sagaBrush()),
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
                }.gradientFill(sagaBrush()),
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
 * Rendered BEHIND the bubble (the [chatBubbleConstraintBackgroundDecoration] slot) — a real flame
 * illustration (`ic_fantasy_flame`, a user-sourced raster asset trimmed/downscaled into
 * `drawable-nodpi`) tucked behind each bottom corner instead of the earlier procedural gradient
 * teardrops (`drawFantasyFireEdge`, removed 2026-08-12): the flat single-tone gradient read as too
 * weak/subtle against the bubble, so this trades "always matches `bubbleColor` exactly" for "looks
 * like an actual flame" — deliberately left untinted (the asset's own orange/yellow shading is the
 * whole point of switching to a real illustration) rather than `ColorFilter.tint`-ed, unlike every
 * other icon in this file. Each flame's `top` anchors *above* the bubble's own bottom edge (negative
 * margin, same overlap technique as [HorrorMoonBackground]) so its upper portion tucks behind the
 * bubble's opaque fill and only the lower lick pokes out below — reads as the flame growing out from
 * behind the bubble rather than a sticker floating on top of it. The right-side flame is horizontally
 * flipped so both licks lean inward toward the bubble's center instead of mirroring outward. Not
 * gated by [isUser] — unlike the dragon below, the fire sits on both lateral corners regardless of
 * which side the bubble is aligned to. Also still carries the dragon body peeking from behind the
 * top corner, mirrored by [isUser] the same way [ShinobiBranchBackground] does — corner side *and* a
 * horizontal `scale` flip on the artwork itself, since the dragon has a clear directional curl.
 */
@Composable
private fun ConstraintLayoutScope.FantasyFlamesBackground(
    content: ConstrainedLayoutReference,
    isUser: Boolean,
    bubbleColor: Color,
) {
    val leftFlame = createRef()
    Image(
        painterResource(R.drawable.ic_fantasy_flame),
        null,
        colorFilter = ColorFilter.tint(bubbleColor),
        modifier =
            Modifier
                .constrainAs(leftFlame) {
                    top.linkTo(content.bottom, margin = (-24).dp)
                    start.linkTo(content.start, margin = (-6).dp)
                    width = Dimension.value(15.dp)
                    height = Dimension.value(24.dp)
                }.rotate(-60f),
    )

    val rightFlame = createRef()
    Image(
        painterResource(R.drawable.ic_fantasy_flame),
        null,
        colorFilter = ColorFilter.tint(bubbleColor),
        modifier =
            Modifier
                .constrainAs(rightFlame) {
                    top.linkTo(content.bottom, margin = (-24).dp)
                    end.linkTo(content.end, margin = (-6).dp)
                    width = Dimension.value(13.dp)
                    height = Dimension.value(21.dp)
                }.scale(scaleX = -1f, scaleY = 1f)
                .rotate(-60f),
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
 * bottom-left edge down toward the bubble below. Overlaps the bubble's top edge via `zIndex` +
 * downward `offset` — same technique as [CyberpunkNameTag]/[HeroesNameTag] — so the vine now
 * curls straight into the panel itself instead of dangling in the gap above it.
 */
@Composable
private fun FantasyNameTag(
    name: String,
    characterColor: Color,
) {
    Box(
        modifier =
            Modifier
                .zIndex(1f)
                .offset(y = 10.dp)
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
                    drawPath(
                        vine,
                        color = characterColor.copy(alpha = .75f),
                        style = Stroke(width = 1.3.dp.toPx()),
                    )
                },
    ) {
        Row(
            modifier =
                Modifier
                    .background(Color.Black.copy(alpha = .82f), fantasyParchmentTagShape)
                    .border(1.dp, characterColor.copy(alpha = .7f), fantasyParchmentTagShape)
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
private fun ShinobiNameTag(
    name: String,
    characterColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .padding(start = 4.dp, bottom = 4.dp)
                .background(characterColor.darker(.3f), MaterialTheme.shapes.small)
                .border(1.dp, characterColor, MaterialTheme.shapes.small)
                .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Image(
            painterResource(R.drawable.ic_sakura_blossom),
            null,
            colorFilter = ColorFilter.tint(Color.White),
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
 * Retro pixel-chat-widget style tag — reuses [HorrorChatBubbleShape] with `drawTail = false` for
 * the tag background (same "reuse the bubble's own shape for the tag" pattern as
 * [CyberpunkNameTag]/[SpaceOperaNameTag]), which already gives it the small stepped-pixel corners
 * from the reference "Twitch Pixel Chat" template — no separate pixel shape needed. Tinted with
 * [characterColor] so each speaker reads distinctly, same as every other genre's tag now.
 * Overlaps the bubble's top edge via `zIndex` + downward `offset`, same as [CyberpunkNameTag].
 */
@Composable
private fun HorrorNameTag(
    name: String,
    characterColor: Color,
) {
    val tagShape = remember { HorrorChatBubbleShape(pixelSize = 2.dp, drawTail = false) }
    Row(
        modifier =
            Modifier
                .zIndex(1f)
                .padding(start = 4.dp)
                .offset(y = 10.dp)
                .background(characterColor.darker(.4f), tagShape)
                .border(1.5.dp, characterColor, tagShape)
                .padding(horizontal = 8.dp, vertical = 3.dp),
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
