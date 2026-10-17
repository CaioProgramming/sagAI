# Tech debt

What was left out of a release on purpose, why, and how to pick it back up. Each item says what is
wrong or missing today, the proposed way out, and what to decide first. Delete an item when it ships,
and update the doc that owns the feature in the same change.

Last review: 1.18.0 (2026-10-17).

## Before the 1.18.0 release (gates, not debt)

- **Publish `widget_lines_blueprint` to `sagai-ilustris`.** It is only in `sagai-hmg`. The Quote and Hook
  widgets are remote-only (no compiled instructions), so without the key their generation fails and the
  creator shows its error. Build the template from a fresh `firebase remoteconfig:get`, add only that key,
  `--dry-run`, deploy, read it back. Text and decisions: [`blueprints.md`](blueprints.md).

## Next release

### 1. Remote, cached AGSL title shaders

- **Today:** the nine title effects (`assets/genre_title_<genre>.agsl`) ship inside the app. Editing a
  shader needs a release. Horror never compiled for weeks (a duplicate `cov` in the port) and nobody
  noticed, because a compile failure falls back to the plain title without a trace.
- **Way out:** one Remote Config key per genre (or one map) with a URL and a uniform-contract version;
  download and cache the file the way fonts, sounds and music are cached; use a remote shader only when
  its contract version is known. Every `RuntimeShader` failure logs a Crashlytics non-fatal that names
  the shader and the error.
- **Decide first:** the fallback. The project rule is remote-only with no compiled copy, which makes a
  genre without its shader show the plain title (today's `SimpleStylisedText`). Also: do Cyberpunk, Space
  Opera and Punk Rock go remote at once, and who publishes to production.
- **Owner doc:** [`features/genre_title_effects.md`](features/genre_title_effects.md). A ready-made session
  brief exists (a card in the 1.18.0 session).

### 2. Genre fonts lost after opening the story reader

- **Today:** open a saga, tap "Ler sua história", go back, open a chapter: the whole chapters screen
  renders in the system font. Opening the chapter straight from the detail keeps the genre fonts. The
  chapters screen sets no theme of its own, so something above it changes (or fails to restore) the genre
  typography when the reader closes. It predates the sticky-header change.
- **Way out:** find where the genre typography is provided for a saga's destinations (`ui/theme/Theme.kt`
  and the nav host) and why the reader leaves it reset. Fix the cause so every destination of a saga keeps
  its fonts, not just the chapters screen.
- **Check:** reproduce on the device first (`android-device` skill), fix, then show the chapters screen
  with the genre fonts after visiting the reader.

### 3. Widget sizes the launcher reports are larger than the layouts declared

`SizeMode.Responsive` builds the layout for the nearest declared size and the launcher stretches it (a 4x3
declared at 250x180dp can be 291x256dp, or larger on a 6-column launcher). Art and covers now use weights,
but text widths in `ContinueWidget`/`LinesWidgets` are still fixed fractions of the declared width. Declaring
one or two larger sizes (for example 4x4) would give those layouts a truer fit. The Stories strip only shows
from 4x3; 4x2 and 2x2 show the saga on top alone.

### 4. The launcher icon in the hmg build does not follow the saga's theme

- **Today:** the older themed icons change the home icon with the saga's genre, but on the debug (hmg)
  build the icon stays the same dragon. `AppIconManager` does switch: logcat showed
  `AppIconManager: launcher icon is now HEROES` after the app went to the background, and
  `dumpsys package` lists every alias (`com.ilustris.sagai.launcher.*Icon`) under `com.ilustris.sagai.debug`.
  So the alias changes and the launcher keeps drawing the old one.
- **Leads:** the debug build replaces the dragon drawables with a "construction blueprint" set in
  `app/src/debug/res/drawable-nodpi` (commit `8bded517`), so check that every genre alias has its own
  debug art and that those files differ per genre. The launcher may also be holding a shortcut pinned to
  the previous alias, which the code comment already warns about, or caching the icon for a few seconds.
  The switch only runs when the app leaves the foreground and only with "follow the last saga" on
  (`applyOnBackground`, `followsLastSaga`).
- **Check:** on the phone, turn "follow the last saga" on, open a saga of another genre, leave the app and
  compare the home icon with the alias that `getComponentEnabledSetting` reports, on debug and on release.

## Smaller items

- **Recap statistics.** With the review ready, the lower `RecapHeroCard` (messages, characters, chapters) is
  hidden and the recap lives behind the fixed button. If those numbers are missed, show them inside the
  review or on the "Sobre" section.
- **CRT screen on Cyberpunk and Space Opera** is off in the app (it read as a blurred panel over the art).
  The lab keeps the switch (`opts.x`) if someone wants it back for one genre.
- **Published prototypes are older than the repo.** The genre titles lab artifact predates the app's fixes
  (white Fantasy letters, softer Crime, stronger touch, no Heroes speed lines). The repo copy
  (`docs/prototypes/genre-titles/`) is the source; republish the artifact when convenient.

## Not a debt, but worth remembering

- The title effects ignore `gradientFill` and `reactiveShimmer`: both recolour every non-transparent pixel
  (`SrcAtop`) and flatten the glow and halftone. Never put them on a title where `genre.usesTitleFx()`.
- Widget text is in the system font on purpose: Glance has no custom `Typeface` for `Text`, and drawing it into
  a bitmap loses font scaling and breaks when the launcher stretches the widget. If a touch of the theme font is
  wanted later, the safe place is short single-line text (character name, saga title) as an image of its own
  size using `GenreFontService.getTypefaces`, with a `contentDescription`.
- Accepted in 1.18.0: the constellation globe is a light drawing and only an entry point, the glass button blurs
  from Android 12 up, Punk Rock keeps its tiles, and the character colour reaches the title in every genre.
- A shader that does not compile does not crash; it silently shows the plain title. After any shader change
  read logcat for `Title effect shader ... failed`.
- iOS has none of the 1.18.0 visual work yet (title effects, recap button, widgets).
