# Home Widgets

Android home screen widgets, built with Jetpack Glance. The proposal and the decisions behind them
live in [`../feature_ideas/home_widgets.md`](../feature_ideas/home_widgets.md); the visual reference
is [`../prototypes/home-widgets/`](../prototypes/home-widgets/home-widgets.html).

> **Status:** first version, written without being able to build the Android project (no SDK or
> Google Maven in the environment it was written in). The pure logic is covered by unit tests that
> were run on the JVM; the Glance and Compose code has **not been compiled or run on a device**.
> Treat the first `./gradlew installDebug` as part of the review.

## What ships

| Widget | Kind | Sizes | Tap opens |
|---|---|---|---|
| Continue saga | automatic | 1x1, 2x1, 4x1, 2x2, 4x2 | the saga's chat (or new saga, when none is running) |
| Last message | automatic | 2x1, 4x1, 2x2, 4x2 | the saga's chat |
| Your stories | automatic | 2x2, 4x2, 4x3 | the top saga's chat (details when finished), or the tapped cover's |
| Character quote | generated | 4x1, 2x2, 4x2, 4x3 | the character |
| Character hook | generated | 1x1, 4x1, 2x2, 4x2 | the saga's chat |

Your stories is laid out like a music widget: the saga played last on top (its artwork melting in from the
right, its objective, a play button and its theme icon in the corner) and, from 4x3 up, a strip of square covers
with the other recent sagas, running and finished, newest first. With no running saga the top takes the newest finished one.

Automatic widgets need nothing from the player and redraw when the app goes to the background.
Generated widgets are created from **Create widget** in the overflow menu of the character screen.

## How it works

```
features/widgets/
  domain/   pure Kotlin: cells and size fitting, rotation, palette + contrast, prompt, sanitizer, links
  data/     WidgetDataSource (reads the database), WidgetLinesUseCase (one AI call per batch)
  glance/   the widgets, receivers, bitmaps, shared components, pin receiver
  ui/       WidgetCreatorViewModel + WidgetCreatorSheet (Compose)
  WidgetUpdater.kt
```

- **Data.** `WidgetDataSource` reads straight from Room: `SagaDao.getSagaSummaries()` (last message,
  saga state), a new `TimelineDao.getLatestTimelineBySagaOnce()` (the scene objective) and
  `CharacterDao` (portraits). No AI at render time.
- **Following a saga.** Continue and Last message follow the most recently touched unfinished saga.
  When none is left, both show the invitation to write the next story.
- **Loading and error.** `DataGlanceWidget` draws a neutral loading state at once, loads in the
  background and swaps in the content, or a neutral error state whose tap opens the app.
- **Sizes.** Each widget declares `SizeMode.Responsive` with the cell sizes it has a layout for. A
  cell count maps to dp as `70 x cells - 30`. `WidgetCells.bestFit` chooses the layout for whatever
  size the launcher reports.
- **Theme.** Colors and corner radius come from the genre's `*_visual_config` in Remote Config
  (`colorPalette`, `primaryColor`, `cornerSizeDp`) through `GenreVisualConfigService`, then
  `WidgetPaletteFactory` guarantees contrast. The dark base is the genre's `primaryColor` deepened until white text has room
  (luminance at most 0.06), so Heroes is a deep blue and Shinobi a deep red; the palette's own darkest entry is a
  near-black backdrop tint and is not used for the base. Artwork that is ink on paper (mean luminance above
  0.62) switches the widget to dark text on a paper base. Your stories takes the theme of the saga on top; each cover on the strip takes the colors of its genre, or the
  saga's own icon when it has one. With no sagas at all it shows one neutral surface.
- **Legibility rule.** Text never sits on artwork that is not melted into the base colour. The fade
  lives in the picture itself (`WidgetBitmaps.fade`, an eased ramp over many stops): wide layouts keep
  the text in the left part and the art fades in from the right (`ArtFade.LEFT`), square layouts put the
  art on top and every piece of text in the solid part (`ArtFade.BOTTOM`). A tinted drawable scrim was
  tried first and rendered as a hard edge on some launchers, and Glance has no multi-stop gradients.
- **Updates.** `WidgetUpdater` runs when the app comes forward (it counts an open, which advances the
  "each open" rotation) and when it goes away (so the widgets show what just happened). Generated
  widgets also set `updatePeriodMillis` to a day, which is what the "every day" rotation relies on.

### Generated widgets

1. The sheet loads the character's arcs and the saga's current objective.
2. `WidgetLinesUseCase` builds a prompt from the **profile, arcs, events and relationships**, never
   the chat, and asks `GemmaClient` for a batch of lines. The instructions (role and directives) come
   from the Remote Config blueprint `widget_lines_blueprint`, remote-only: there is no compiled copy,
   so a missing key fails the request and the creator shows its error. Published to `sagai-hmg`;
   **publish it to `sagai-ilustris` before releasing**. `WidgetLinesSanitizer` removes blanks,
   repeats, list markers, wrapping quotes and anything too long for the widget.
3. The player keeps lines, edits them or asks for more. At most 10 lines per widget.
4. `WidgetPinner` calls `requestPinGlanceAppWidget`. The configuration travels in the success
   callback; `WidgetPinnedReceiver` attaches it to the new widget's Glance state, so it lives and
   dies with the widget and there is nothing to clean up.
5. The line shown is chosen by `WidgetRotation` (each open, daily, or fixed).

A generated widget added from the launcher's list, instead of the sheet, shows a neutral "pick a
character" state.

## Limits worth knowing

- **The launcher decides the size.** The creator cannot choose it; the player resizes on the home
  screen. The sheet says so.
- **Fonts.** Glance cannot use the genre's custom fonts, so widgets use the system font.
- **Real size vs layout size.** `SizeMode.Responsive` builds the layout for the nearest declared size, and the
  launcher stretches it into the real box (a 4x3 declared at 250x180dp can be 291x256dp on screen). Fixed
  dp sizes do not stretch, so art strips use weights (half of the real width) and the covers on the
  strip are squares drawn for their box and fitted into an equal-weight slot, never stretched.
- **Faces.** The project has no face detection, so avatars and crops assume the face sits near the
  upper-middle of the picture (`WidgetFocus`).
- **Discreet mode** exists only on generated widgets (a per-widget switch). Automatic widgets show
  their text; a global switch is a follow-up.
- **Rotation without opening the app.** "Each open" only advances when the app is opened. "Every
  day" depends on the system honoring the daily update period.
- **Not included yet:** replacing a single line, a notification or dynamic island entry while lines
  are generated, and the narrator-voiced quote for a whole saga.
- **Widget limit.** 10 generated widgets, checked when the sheet opens.

## Tests

`app/src/test/.../features/widgets/domain/`: cell fitting, rotation, text cleanup, palette contrast
across all nine genre palettes (dark and light), prompt content and size, line sanitizing, the
configuration round trip and the deep links. The Glance and Compose layers have no tests.
