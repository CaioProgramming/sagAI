---
name: sagas-genre-themes
description: Ready-made reference of each Sagas genre's colors, palette, corner radius and font URLs, copied from Firebase Remote Config. Use when a task needs a genre's look right now (a mockup, a design doc, a quick check, a prompt that names colors) and re-fetching Remote Config would be overkill. Snapshot only; for anything that ships, re-fetch with the sagas-prototyping skill.
---

# Genre themes (snapshot)

Snapshot of `<genre>_visual_config` from Firebase Remote Config **v1113, 2026-10-11**. The full
data, including font URLs, is in `genre_themes.json` next to this file. The app reads the live
values through `GenreVisualConfigService`, so remote edits win over this file.

| Genre | Primary | Radius dp | Palette (in stored order) |
|---|---|---|---|
| fantasy | `#8B2635` | 10 | `#050204 #1A0810 #5E1A24 #8B2635 #D44D63 #FFB4C2 #FFD166` |
| crime | `#E91E63` | 25 | `#1A0F2E #3D1B5C #C2185B #E91E63 #FF6B9D #7FFFEE` |
| cyberpunk | `#6E48AA` | 10 | `#03020A #0A0420 #1A0A3D #6E48AA #9D50BB #00F5FF #FF2EFF #E8DCFF` |
| space_opera | `#0081A7` | 5 | `#000510 #001219 #004E63 #0081A7 #00AFB9 #48CAE4 #90E0EF #C9B1FF` |
| horror | `#748B97` | 6 | `#020408 #0B132B #1C2541 #3A506B #748B97 #4ADEFF #6FFFE9 #E8FFFE` |
| shinobi | `#8B0000` | 0 | `#5C2751 #431C3A #7A336B #F39237` |
| cowboy | `#8B4513` | 12 | `#1A0A00 #A84520 #FCE8A8 #FF99BB #D4612F #8B3A0F` |
| heroes | `#2845D6` | 4 | `#020412 #0D1A63 #1A2CA3 #2845D6 #4169F5 #00D9FF #FDC500 #E6E9FF` |
| punk_rock | `#729656` | 2 | `#0A0C0F #1A1F2A #00E676 #FF7E00 #FAFF00` |

Notes:
- Palette order is as stored, not sorted by brightness. Shinobi and cowboy have few or unordered
  tones; do not assume the last entry is the lightest.
- The enum is `COWBOY`, `PUNK_ROCK`, `SPACE_OPERA`: the config key is the lowercase enum name
  (`cowboy_visual_config`).
- Fonts: `headerFontUrl` for titles, `bodyFontUrl` for text, hosted on file.garden and catbox. The
  Shinobi body font is over 4 MB. Each font file has a single weight.
- `iconColor` is empty for most genres. Fantasy's is stored as `#FFFFF` (5 digits), which will not
  parse as a color; fix it in Remote Config before relying on it.
- Genre display names used in the app UI (pt-BR): Fantasia, Crime, Cyberpunk, Space Opera, Terror,
  Shinobi, Cowboy, Heróis, Punk Rock.

To re-fetch and refresh this file, follow the `sagas-prototyping` skill ("Fetch the live values").
