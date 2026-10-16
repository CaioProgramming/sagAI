# Genre Title Effects

Animated, per-genre treatment for the saga title (`Genre.stylisedText`). Explored in the
[Genre titles lab](../prototypes/README.md) (WebGL prototype). **Built** for all nine genres (API 33+);
see [Implementation status](#implementation-status) and [Editing a shader](#editing-a-shader).

## Principles (agreed during prototyping)

1. **One color drives everything.** Each effect derives its whole palette from a single text color
   (the genre `primaryColor`, or `adaptiveColor` where a screen provides one). No gradient on top.
2. **Contained in the text.** The title is one layout box, so an effect must not look like a box
   being clipped. The composable reserves headroom in layout (margin per genre, in multiples of the
   text height) and every effect fades to nothing at the box edge. Effect extents are measured
   against the text height, not the screen.
3. **Always draw with alpha, never an opaque background.** `Modifier.gradientFill` and
   `Modifier.reactiveShimmer` both draw their brush with `BlendMode.SrcAtop` over an offscreen
   layer, i.e. they recolor every non-transparent pixel. An effect that paints a background rect
   would turn into a gradient/shimmer box; one that paints only its own pixels is safe.
4. **No stacking.** Titles for the six effect genres should not also get `genreVfx` inside
   `stylisedText`, and should not get `gradientFill` from the caller (pass a `color` instead).
   `reactiveShimmer` can stay: over transparent effect pixels it is only a moving highlight.
5. **One event at a time.** Effects that fire events (Shinobi cut, Cowboy mark) are a single
   unified animation, not layers of unrelated ones.

## Per genre

| Genre | Today | Proposed | Notes |
|---|---|---|---|
| Fantasy | `levitate` + `divineAura` | Flames rising from the letters, streaks that blend into the glyphs (no outline), color from the text color. Few sparks. | Needs the most headroom (top ~1.6× text height). |
| Horror | `psychosis` (jitter + ooze) | Cold haunting in the genre's cold palette: ground fog, breath rising off letters, ghost double image, short possession glitches that tear a band sideways. Optional ectoplasm drips. | The palette is cold (`#748B97`, `#4ADEFF`), so blood-red drips clashed; red stays as a swatch. |
| Cowboy | `cowboyBurn` (full-surface "fireplace") | A freshly applied brand: letters flash white-hot and cool over ~8 s to a dark scorch; only a few thin wisps of smoke around the text. Repeats every ~11 s. | Not fire. No background, leather surface or ambient glow behind the text, only the letters, a tight bloom and the smoke. `cowboyBurn` repaints everything and hurts legibility (see `BookRecapCard` note). |
| Shinobi | `levitate` + `sakuraWind` | No backdrop, subtle smoke wisps, and a random katana cut: the blade crosses the text and the halves open like scissors (wider in the middle). The negative (white paper, black ink, speed lines) is exactly the gap between the halves, with a red edge and a faint scar that lingers. A single cut from edge to edge of the text; the y entry point, angle and side it comes from are random. (An earlier version fired 1–3 short random cuts in a row, which read as several small animations.) | Replaced earlier ink-splat and full-frame impact-frame layers, which felt like separate animations. The slit used to be an independent lens shape, now it is tied to the gap. Inspiration: high-contrast anime impact frames. |
| Crime | `vhs` + `dreamySparkle` | Euphoria-style iridescent gradient clipped to the letters (pink, light pink, mint, violet from the palette), glitter, 80s film grain. | `vhs` already covers an "aged tape" look, so the aged-print idea was dropped. Watch it next to Cyberpunk (both neon). |
| Heroes | `WordArtText` extrusion + outline + `levitate` + `lightningStorm` | Keeps extrusion/outline. Adds a soft pulsing glow, halftone shading **only inside** the letters, metallic sweep, gold glints. | A square impact ring, outside halftone and speed lines around the title were tried and rejected. |
| Cyberpunk | `glitch` + `crtScreen(Dream)` | **A VHS tape on a CRT**: interlaced scanlines, per-line wobble, a tracking bar rolling up the screen, RGB channel bleed, static and glitch bursts. Optional CRT screen rectangle filling the layout box. | The first prototype felt too weak. Entry: the signal locks in while a beam walks down the text. |
| Space Opera | `crtScreen(Element)` | **A phosphor monitor**: RGB triads, scanline gaps, light tube curvature, a refresh bar rolling down, twinkling stars and a sweep of light. Optional CRT screen rectangle. | Entry: a TV power-on line that opens to the text height. |
| Punk Rock | ransom note + `chromaticAberration` + `glitch` | **Unchanged.** Optional: xerox grain. | Already covers the idea; it just must follow rules 3-4. |

## Interaction (prototyped)

**Design rule: the touch must be nearly invisible, an Easter egg.** Nothing marks where the finger is (no ring, no spark, no visible lens rim). The effect only shows as the text itself reacting. The titles can react to touch. The prototype feeds the shader the finger position, its velocity and a
short fading trail of recent positions.

| Genre | Interaction |
|---|---|
| Shinobi | Fruit-Ninja style: while dragging, a blade trail follows the finger; on release the cut plays along the gesture (direction and place), still edge to edge. Only a mostly horizontal, long enough drag that starts on the text counts, otherwise it is a scroll. |
| Cowboy | Smoke is pushed by the finger velocity and appears where it passes; the letters re-heat where touched. |
| Fantasy | Only the flames above the text lean and heat up near the finger (the base stays still), as if the fire senses the presence. No extra sparks. |
| Crime | Letters and gradient deform like liquid around the trail and relax slowly. |
| Horror | A very light magnification and a small clearing of the fog around the finger, no visible rim. |
| Heroes | A tap fires a punch; the glow lights up where the finger passes. |

The prototype also has Cyberpunk, Space Opera and Punk Rock tabs. They reproduce what the app already
does (with stand-in fonts) so the interaction can be tried there too: Cyberpunk, slices of text jump and
the RGB channels split only around the finger; Space Opera, the sweep of light follows the finger with a
very light lens warp; Punk Rock, ransom-note letters drift away and tilt slightly when the finger nears.
The Punk Rock tab renders its cut-out letters on a 2D canvas (per-letter font, case, tile colour, tilt)
and feeds it to the shader as a colour texture.

Compose notes: observe the pointer in `PointerEventPass.Initial` **without consuming**, so scroll,
`clickable` and pagers keep working (the saga detail title is already clickable). Keep the state in a
holder read in the draw phase, not in recomposition. Gate the Shinobi cut on: the drag starts inside the
effect box, is mostly horizontal (about < 55 degrees) and longer than about 30% of the text width,
otherwise it is a scroll. Pair the cut with the genre `vibrationPattern` haptics. Add an `interactive`
parameter, off by default, on only for hero titles (`HeroHeader`, `SagaDetailInitialView`, `StorySheet`),
and soften it with the system reduce-motion setting. Discoverability needs a hint (for example a one-time
sheen when the screen opens); the effect has to look good without interaction anyway.

## Entry animations

*Prototyped in the lab: each tab plays its entry when opened, with a "Reproduzir entrada" button and a switch to turn it off. The pointer interaction only turns on after the entry and the ignition finish. In the prototype the handwriting reveal is approximated by a pen sweeping left to right (outline first, fill behind it); in the app it is the real `HandwrittenText`.*

Today only two titles have an entry animation: Fantasy and Crime use `HandwrittenText` (a handwriting
reveal of the glyph outlines, 3.8 s by default, with a shadow that fades in after `onAnimationFinished`).
`TypewriterText` / `SimpleTypewriterText` exist but are used for body text (terminal pages, story beats,
chat), not for titles. The new effects should keep those entries and treat them as the *birth* of the
effect, not replace them.

Proposed sequence: **entry animation, then the effect ignites** (a ramp of about 1 s). Interaction is
enabled once the effect is lit.

| Genre | Entry | Then |
|---|---|---|
| Fantasy | Ignition, like the Cowboy mark: the letters appear white-hot with a small press, the fire bursts in a flare and settles. Replaces the handwriting reveal for this genre | Steady fire |
| Crime | Existing handwriting reveal | The iridescent gradient floods the letters, glitter starts |
| Cowboy | The brand press itself (heat starts at the first frame) | Cools, wisps leave |
| Shinobi | Handwriting reveal in white ink, like Crime | The red floods the letters, smoke appears, first cut after a few seconds |
| Horror | Materializes out of the fog with a flicker | Ghost double, breath and glitches |
| Heroes | A slam: scale-in with the impact pulse | Glow settles |
| Cyberpunk | Typewriter or decode (random glyphs resolving) with glitch | Neon, scanlines and glitch bursts |
| Space Opera | Typewriter, like the terminal boot, then phosphor power-on | Gradient, stars, sweep |
| Punk Rock | Letters slap in one by one in stop motion | Ransom-note jitter |

Technical notes:
- The effect must not depend on how the text is drawn. Capture the composable's final frame
  (`rememberGraphicsLayer()` + `GraphicsLayer.toImageBitmap()`, or draw the glyph path into a bitmap) once
  the entry finishes, then build the mask and the small field from that bitmap. This keeps custom fonts
  and `HandwrittenText` outlines exact, and works for typewriter text too. Check that the Compose version
  in the project has `GraphicsLayer.toImageBitmap()`.
- Reserve the margin (headroom) in layout from the first frame, so the layout does not jump when the
  effect starts.
- Entries play once per appearance and only when the caller asks for animation (hero titles). In lists
  and repeated screens skip the entry and go straight to the lit state (or no effect), as `isAnimated`
  already does for `HandwrittenText`.
- Below API 33 the entry animations still work (handwriting and typewriter are not AGSL), so the
  fallback for Fantasy and Crime stays the handwriting reveal plus shadow and shimmer. Fantasy loses the
  handwriting only on API 33+, where the ignition replaces it.
- Phase 2: let the effect follow the entry (flames growing from the strokes as they are written). It
  needs the mask and the field per frame, so it is more expensive; start with the sequential version.

## Note on the CRT screen rectangle (Cyberpunk, Space Opera)

**In the app it is off** (`opts.x = 0` in `TitleFxConfig`): a blurred dark panel behind the title read badly
over the hero art, so the title is only the letters and their effect. The lab keeps the switch.

The optional screen rectangle paints the layout box (rounded corners, vignette, scanlines). It is the one
place where an effect draws a surface on purpose, which goes against rule 3 (draw only what you paint). It
must stay a flag, and it must not be combined with `gradientFill`, which would tint the whole rectangle
through `SrcAtop`. With it off, the scanlines and phosphor only show on the letters and their glow.

## Known gotchas in the current code

- `gradientFill` also drops opacity to 0.90 and recolors outlines/extrusion (already visible on
  Heroes' white outline). Call sites combining it with `stylisedText`: `HeroHeader.kt` (`heroTitleOverlay`),
  `SagaDetailInitialView.kt` (two places). `reactiveShimmer` is also used on `HeroHeader.kt:190`, `StorySheet.kt`.
- `ChaptersGalleryContent.kt` passes `.genreVfx(genre)` into `stylisedText`, which already applies it
  internally, so the effect runs twice.
- `genreVfx` is also used on icons (`CosmicBook`, `BookReader`, `ReviewIntroAnimationPage`,
  `SectionLoading`) and as `themeVfx` (Horror police tape). Cowboy's `cowboyBurn` and Horror's
  `psychosis` repaint the whole surface there too. Icon effects are a second phase; the same mask
  approach works with the icon alpha as the mask, but needs a "lite" variant at 50–80 dp.
- Fonts come from remote config as URLs (`headerFontUrl`), so the prototype used Google Fonts
  stand-ins. The prototype has a file input to load the real font file.

## Implementation notes (for Compose)

- Needs `RuntimeShader` (AGSL, API 33+). The project `minSdk` is 27, so a fallback is required.
  **Decision:** below API 33 the title is a simple styled text: the genre font plus simple modifiers
  (for example `Shadow`) and the `reactiveShimmer` animation on top. No `genreVfx`, no Canvas
  re-implementation of the effects; everything else is left to AGSL. The existing code already follows
  this pattern (`Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU` in `CrtScreen.kt`, `Filters.kt`,
  `HeatHaze.kt`), so Cyberpunk, Space Opera and Punk Rock keep their current behavior.
- The text mask comes from the same `TextLayoutResult` `WordArtText` already uses; a small
  precomputed field (text smeared along one direction, wide glow, tight halo) feeds the shader and is
  rebuilt only when the text changes.
- Stop animating with `rememberLifecycleAnimationsActive()`; consider a static first frame for
  list items.

## Implementation status

Done: all nine genre titles (API 33+), in `ui/components/titlefx/` plus
`assets/genre_title_<genre>.agsl`. Checked on a device (2026-10-16) frame by frame against the lab:
entry, the effect over several seconds, and touch.

- `stylisedText(..., color, interactive, playEntry)` picks `GenreTitleFx` when `Genre.usesTitleFx()`;
  otherwise (or if the shader cannot be built) the old styled text without `genreVfx`.
- Shaders are the lab's GLSL ported to AGSL and made **emissive**: output is premultiplied light on a
  transparent layer (`finish()`), `boxMask()` fades everything to nothing at the composable's bounds, so
  the title sits on any background. Background colours and the debug box from the lab are gone.
- `GenreTitleFx` is a custom `Layout` that reserves the per-genre margin (pad t/b/s x text height),
  measures with `TextMeasurer` (shrinks up to 8x to fit two lines), draws the white-on-black mask
  with `CanvasDrawScope`, builds the small field bitmap on the CPU (`TitleFxField.kt`, port of
  `buildField`) and draws through a `Paint` with the `RuntimeShader` (`mask` and `field` as
  `BitmapShader` children). Clock: `withFrameNanos`, ~30 fps, paused with
  `rememberLifecycleAnimationsActive()`.
- Events and touch live in `TitleFxState` (cowboy brand every 11 s, shinobi cut every 7 s after the
  entry, drag cut on release, heroes tap punch, 8-point finger trail). Touch is observed in the
  `Initial` pass without consuming, so scrolling parents keep working. Only hero titles pass
  `interactive = true`.
- Call sites: `gradientFill` **and `reactiveShimmer`** are applied only when `!genre.usesTitleFx()`
  (`HeroHeader.kt` x2, `SagaDetailInitialView.kt` x2, `StorySheet.kt`). Both draw with `SrcAtop`, so over the emissive
  layer they recolour the glow, halftone and ghost pixels too (Heroes looked flat blue, Horror lost its
  pale letters). The duplicate `genreVfx` in `ChaptersGalleryContent.kt` is gone.
- Cyberpunk and Space Opera are the lab's VHS and phosphor shaders without the CRT screen. Punk Rock
  draws its ransom-note tiles on the CPU (`RansomNotePainter.kt`, port of `paintRansom`/`ransomStep`):
  a mask and a colour layer (`uCol`) repainted on a new 5 s identity, the 2 fps jitter, each letter of
  the entry, or when the finger pushes the tiles. Tile fonts are the genre header font plus system faces.
- The mask fill is drawn with an explicit `drawStyle = Fill`: `drawText` keeps the previous call's
  `Stroke` otherwise, and every title was only an outline.
- Extents in the field (glow, halo, smear) are measured per text line, so a title wrapped on two lines
  keeps the lab's glow size. A word is never broken between letters; the font shrinks instead.
- Palettes (`TitleFxPalette.kt`) derive from the title colour; Crime, Heroes, Cyberpunk and Space Opera
  use the remote `colorPalette` when it has enough entries, like the lab did with the app's own colours.
- Fake bold (`stroke`) is 0 for Cyberpunk, Space Opera and Punk Rock: their header fonts are already
  heavy, and the extra ink saturated the halo.

Deviations / not done yet:
- Horror never compiled until 2026-10-16 (a second `cov` declared by the port script; now `fxCov`), so
  before that it always showed the simple title. Check logcat for `Title effect shader ... failed` after
  any shader change.
- Tweaks made after the lab was published (Fantasy white letters and flames fading behind them, Crime
  less white, stronger touch on Fantasy/Horror/Cowboy/Crime/Heroes) live in the repo lab
  (`docs/prototypes/genre-titles/genre-titles.html`); the published artifact is older.
- The entry animation does not follow `HandwrittenText`'s own writing; the shader draws its own
  handwriting/ignition. Letter bounds are estimated (72% of the layout height) for the margins.
- Icon `genreVfx` replacements are still phase two.

## Using it in code

- Call `genre.stylisedText(text, modifier, fontSize, color, interactive, playEntry)`; do not call
  `GenreTitleFx` directly. `Genre.usesTitleFx()` says whether the genre gets the AGSL title (API 33+
  and a genre with a config in `titlefx/TitleFxConfig.kt`).
- **Never put `gradientFill` on the modifier when `genre.usesTitleFx()`**: the shader draws its own
  colour and a gradient over the composable would tint the whole box. Pass the colour with `color`
  and apply the gradient only to the fallback and to genres without the effect (see `HeroHeader.kt`,
  `SagaDetailInitialView.kt`). The same goes for `reactiveShimmer`.
- Do not add `genreVfx` on top of these titles; the effect replaces it. Icon VFX is a separate thing.
- `interactive = true` only for hero titles (touch easter egg); lists and cards leave it off.
- The composable reserves a per-genre margin around the letters (pads in `TitleFxConfig`, in text
  heights); the effect fades out inside it. Account for that extra space in layouts.
- Shader failures are caught (`createTitleShader`) and fall back to the simple title; look for
  `Title effect shader ... failed` in Timber.

## Editing a shader

The shaders are written and tuned in the browser lab, then converted:

1. Edit the `FS_*` template literals in `docs/prototypes/genre-titles/genre-titles.html` (`FS_FIRE` is
   the Fantasy shader) and open the file in a browser to see the result.
2. Run `python3 scripts/genre-titles/port_agsl.py`. It reads the lab and rewrites
   `app/src/main/assets/genre_title_<genre>.agsl` (`--only CYBER,SPACE` rewrites just those): GLSL to AGSL (`vecN` to `floatN`, `texture2D` to the
   child-shader helpers `sM`/`sS`), the emissive model (transparent background, `finish()` outputs
   premultiplied light, `boxMask()` fades to nothing at the box edge), grain gated by luminance.
   `scripts/genre-titles/boxmask.glsl` is the box-fade snippet it injects.
3. Build and run on an API 33+ device. AGSL compiles at runtime, so a typo shows up only there (log:
   `Title effect shader ... failed`, and the title falls back to plain text).

Do not hand-edit the `.agsl` files: the next conversion overwrites them. Uniforms and their units
(`uBox`, `uFw`, `uPtr`, `uTrail`, `uCut`, entry/event times) are set in `titlefx/GenreTitleFx.kt` and must
match the lab's `frame()` function.
