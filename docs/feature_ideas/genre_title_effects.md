# Genre Title Effects

Animated, per-genre treatment for the saga title (`Genre.stylisedText`). Explored in the
[Genre titles lab](../prototypes/README.md) (WebGL prototype). **Not built in Compose yet.**

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
| Heroes | `WordArtText` extrusion + outline + `levitate` + `lightningStorm` | Keeps extrusion/outline. Adds a soft pulsing glow, halftone shading **only inside** the letters, speed lines, metallic sweep, gold glints. | A square impact ring and outside halftone were tried and rejected. |
| Cyberpunk, Space Opera, Punk Rock | `glitch`+`crtScreen(Dream)`, `crtScreen(Element)`, ransom note + `chromaticAberration`+`glitch` | **Unchanged.** Optional: a light sweep and twinkles for Space Opera, xerox grain for Punk Rock. | They already cover the proposed ideas; they just must follow rules 3–4. |

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
| Heroes | A tap fires a punch; speed lines radiate from the touched point. |

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

## Entry animations (to integrate)

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
| Heroes | A slam: scale-in with the impact pulse | Glow and speed lines settle |
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
