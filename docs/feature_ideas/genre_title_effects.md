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
| Shinobi | `levitate` + `sakuraWind` | Subtle smoke wisps plus a random katana cut: the blade crosses the text, halves slide apart and the slit shows a **negative** (white paper, black ink, speed lines). 1–3 cuts, random angle/place/side. | Replaced earlier ink-splat and full-frame impact-frame layers, which felt like separate animations. Inspiration: high-contrast anime impact frames. |
| Crime | `vhs` + `dreamySparkle` | Euphoria-style iridescent gradient clipped to the letters (pink, light pink, mint, violet from the palette), glitter, 80s film grain. | `vhs` already covers an "aged tape" look, so the aged-print idea was dropped. Watch it next to Cyberpunk (both neon). |
| Heroes | `WordArtText` extrusion + outline + `levitate` + `lightningStorm` | Keeps extrusion/outline. Adds a soft pulsing glow, halftone shading **only inside** the letters, speed lines, metallic sweep, gold glints. | A square impact ring and outside halftone were tried and rejected. |
| Cyberpunk, Space Opera, Punk Rock | `glitch`+`crtScreen(Dream)`, `crtScreen(Element)`, ransom note + `chromaticAberration`+`glitch` | **Unchanged.** Optional: a light sweep and twinkles for Space Opera, xerox grain for Punk Rock. | They already cover the proposed ideas; they just must follow rules 3–4. |

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

- Needs `RuntimeShader` (AGSL, API 33+). Decide the fallback below 33 (keep today's `genreVfx`
  look, or a simplified Canvas version). **Open question:** project `minSdk` and the fallback.
- The text mask comes from the same `TextLayoutResult` `WordArtText` already uses; a small
  precomputed field (text smeared along one direction, wide glow, tight halo) feeds the shader and is
  rebuilt only when the text changes.
- Stop animating with `rememberLifecycleAnimationsActive()`; consider a static first frame for
  list items.
