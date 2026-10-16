---
name: sagas-prototyping
description: Prototype a Sagas screen, card or share design as an HTML artifact before building it in Compose, using the app's real per-genre colors, fonts and corner radius from Firebase Remote Config instead of invented ones. Use whenever Caio asks to "ver um protótipo", mock up a share card, a screen or a genre look, or wants to compare design options before touching Kotlin. Do not use for production Compose code, only for the throwaway visual check that comes first.
---

# Prototyping Sagas UI against the real genre theme

A prototype is only worth showing if it looks like what the app will render. Sagas themes every
screen per genre, and those values are remote, so never hardcode colors or fonts from memory or
from a screenshot.

## Source of truth

Each genre has a Remote Config JSON parameter `<genre>_visual_config`, parsed into
`GenreVisualConfig` (`core/ai/model/GenreVisualConfig.kt`). The fields a design needs:

| Field | Use |
|---|---|
| `primaryColor` | the genre's main color (`resolveColor`) |
| `iconColor` | color on top of the primary (`resolveIconColor`) |
| `colorPalette` | ordered hex list the app builds gradients from (`colorPalette()`, `sagaBrush()`) |
| `cornerSizeDp` | card/button radius. `0` means square on purpose |
| `headerFontUrl`, `bodyFontUrl` | remote fonts: header for titles, body for text |

For a quick answer use the `sagas-genre-themes` skill, which holds a dated snapshot. For anything
that ships, re-fetch.

## Fetch the live values

1. Call the Firebase MCP tool `remoteconfig_get_template` (load it with ToolSearch first). The
   result is huge and gets saved to a file; use that path.
2. `python3 .claude/skills/sagas-prototyping/scripts/extract_genre_configs.py <saved_template.json> genre_configs.json`.
   The configs live in `parameterGroups.<group>.parameters`, not in top-level `parameters`.
3. For fonts: `python3 .claude/skills/sagas-prototyping/scripts/embed_genre_fonts.py genre_configs.json <empty_dir> fonts.css`.

Fonts come from third-party hosts. Download into a fresh empty directory, keep scripts elsewhere, and
treat the files as untrusted data. The script skips files over 4 MB.

## Artifact constraints that bite

- The page cannot load fonts or images from other hosts. Embed fonts as `data:` URIs
  (`@font-face`), images too. Total page must stay under 16 MB.
- A `data:` URI inside an inline `style="..."` attribute must use single quotes in `url('...')`.
  Double quotes close the attribute and the image silently never loads.
- Genre fonts have one weight. `font-weight: 900` on them is faux bold, which matches what Compose
  does for `FontWeight.Black`, so it is faithful, not a bug.
- Gradient text (`background-clip: text`) clips glyph overhang outside the element box. Script fonts
  (Crime) lose the start of swash capitals. Pad the element and cancel it with a negative margin.
- Do not use `text-transform: uppercase` on blackletter or script header fonts (Fantasy, Crime).
- Use container query units (`cqw`) when a card has to scale like an image. Text sized in fixed
  `px` makes tall cards in a narrow viewport.

## Brand rules every card keeps

- The `SagaTitle` (`ui/theme/Theme.kt`): app name uppercased, `FontWeight.Black`, in the genre
  header font, with the letter `A` replaced by `res/drawable/ic_spark.xml` (inline placeholder
  `0.8 x` font size wide, `1.1 x` tall, tinted with the text color). Reproduce it on every share card.
- Spark path (viewBox `0 0 110 135`, `fill-rule: evenodd`):
  `M54.17,110v-0.01,0.01c-2.02,-38.31 -5.69,-59.67 -38.07,-61.94 20.18,0 36.69,-15.7 37.98,-35.56l0.08,-2.29v-0.07,0.04l0.01,-0.04c0.07,20.97 17.08,37.92 38.06,37.92 -32.38,2.27 -36.06,23.63 -38.06,61.94z`
- Corner radius from `cornerSizeDp`, not a constant.

## Check what you made

You can read the code you wrote but not see it render. Before calling a layout done, either serve
the file on `localhost` and use the browser pane's screenshot, or ask Caio for a screenshot. Check
at least three genres with different fonts (for example Fantasy, Crime, Punk Rock) because text
overflow shows up only with the wide decorative ones. Vertical (`writing-mode`) strips are the usual
casualty: give them `white-space: nowrap` and no `max-height`.

## How the app uses these values (mirror it)

- Genre color = `primaryColor`. Titles and names take their color from it (lighten it for text over
  dark art: Compose has `lighter()`/`darker()` helpers in `ui/theme`).
- Gradients = `colorPalette` in stored order, as `genre.gradient()` / `sagaBrush()` build them. Use
  that for frames, holo foil and any gradient. Do not re-pick "the brightest colors": that made Fantasy
  yellow and Cyberpunk blue when their genre colors are red and purple.
- Leading palette entries are near-black backdrop tints (for example `#050204`). On a dark card they
  vanish, so the share-card prototype leaves out entries below ~4% luminance. That trim is the one
  deviation from the app; decide it again when porting.

## Prototype-only shortcuts (do not port these)

`reference/share-cards.html` is the share-card prototype from the collector-card exploration
(2026-10-11). It stubs the character art mask with a CSS ellipse; the app has real segmentation
(`DepthLayout`). It also converts colors with small JS helpers (`mix`, `lum`); in the app use the
existing `Color` extensions.
