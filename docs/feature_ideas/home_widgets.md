# Home Widgets

## Concept

Widgets that keep Sagas close to the player without being invasive. They sit on the Android home
screen, look like part of the story (genre colors, character art, poetic lines) and invite the
player back instead of nagging. No countdowns, no "you've been away" guilt.

Replaces the old `saga_progress_widget.md` plan (see `docs/archive/`).

A clickable prototype exists as a private Artifact (widgets in every size, five genres, discreet
mode, and the creation flow). It is a visual reference, not a spec: fonts are stand-ins and the
sample texts are invented.

## Principles

1. **Update only when the player plays.** No polling, no clock-driven nudges.
2. **No AI at render time.** Widgets read data that already exists, or text generated once at
   creation.
3. **Text is always legible.** Text never sits on art without a solid scrim under it (see Visual
   rules).
4. **Discreet mode.** Blurs the art and replaces story text with neutral lines, for players who do
   not want the story on the home screen or lock screen.
5. **Icons over labels** on action buttons (play, plus).

## Two families

### Automatic (no AI, no player setup)

The player adds the widget from the launcher and it keeps itself up to date.

| Widget | Shows | Sizes | Data |
|---|---|---|---|
| **Continue** | Current objective over the saga artwork, with a play button. Becomes an invitation to write a new story when the saga ended or none is running | 1x1, 2x1, 4x1, 2x2, 4x2 | `Timeline.currentObjective` or `SceneSummary.immediateObjective`, `Saga.artwork`, `Saga.isEnded` |
| **Last message** | The last line from the chat, as written, with the speaker | 2x1, 4x1, 2x2, 4x2 | `Message.text`, `speakerName`, `Character.image` |
| **Your stories** | A shelf of finished sagas: one book each, title below | 2x2, 4x2, 4x3 | `Saga.isEnded`, `title`, `icon`, `genre` |

Notes:

- **Continue** follows the most recent *unfinished* saga. When it ends, the widget moves to the next
  unfinished one. If none is left it shows the invitation (a gradient built from the genre's
  `colorPalette`, a large faint genre glyph, a "+" button that opens saga creation).
- **Your stories** is genre-neutral on purpose: neutral dark surface, one neutral font. Only the
  book takes theme colors. If the saga has its own icon (`Saga.icon`) it fills the book; if not, the
  genre icon is drawn on the genre-colored background. There is no 4x1 version: at 76dp tall the
  books and titles fall below a readable size.
- The 2x2 shelf shows only two stories. Proposed rule: the most recent ones.

### Generated (the player creates it, the AI writes it)

Created from a button on the character screen (next to the existing share action).

| Widget | Shows | Sizes |
|---|---|---|
| **Quote** | A short poetic line the character would say, with the character portrait | 4x1, 2x2, 4x2, 4x3 |
| **Character hook** | The character calling the player back, as a chat bubble with the face as avatar | 1x1, 4x1, 2x2, 4x2 |

Each widget holds a small batch of approved lines and rotates between them.

## Generation

- **Input:** character profile (`backstory`, `personality`, `occupation`, `knowledge`, `voice`),
  `CharacterArc`, `CharacterEvent`, `summarizeRelationships()`, the genre `aesthetic`, and for hooks
  the saga's `currentObjective`.
- **Chat messages are not sent.** A finished saga can have hundreds of messages per character; the
  summarized data above is already enough and keeps the prompt size nearly constant.
- **Output:** a batch of about 10 lines. They are new lines in the character's voice, not quotes
  from the chat. The UI should make clear they are inspired by the story.
- **Moderation:** same `SafeGuard` filter as the chat before anything reaches the home screen.
- **Cost:** one call at creation. Rotation afterwards needs no AI.

## Creation flow

1. **Entry point:** widget button on `CharacterDetailsView`, beside the share action. Either a new
   `ShareType.WIDGET` in `ShareSheet` or its own screen.
2. **Style:** Quote or Hook, each card previewing the widget with the current character.
3. **Generating:** the existing `LoadingLines` pattern: lines in the character's voice that rotate,
   so the screen never looks frozen, plus an indeterminate bar. The player can leave: the dynamic
   island follows the request and a notification says when the lines are ready.
4. **Pick lines:** the player keeps, edits, regenerates one at a time, or asks for more. Live
   preview follows the selection. At least one line is required.
5. **Size and rotation:** real Android sizes for that type with minimum dp, rotation rule (each app
   open, daily, or a fixed line) and discreet mode.
6. **Pin:** `AppWidgetManager.requestPinAppWidget`, with the configuration id so the widget is born
   bound to its lines.

Error and edge states: generation failure (retry), offline (already-created widgets keep working),
a line adjusted by the safety filter (marked as adjusted, editable), a character with little
history (smaller batch, with a hint to play more), and the widget limit.

## Widget loading and failure states

Two neutral states shared by any widget that depends on the worker. They use the same neutral
surface as Your stories and never show story text.

- **Building:** after the player taps Add, the worker still renders the face crop, gradient and
  blur. The widget shows the genre book tile, "Montando seu widget" and an indeterminate bar
  (`LinearProgressIndicator` or `CircularProgressIndicator`; Glance only supports indeterminate
  progress and does not animate the rest). Sizes: 1x1, 4x1, 2x2, 4x2.
- **Broken:** if the worker fails or the configuration was deleted, the book loses its color, a
  small alert badge appears and the text reads "Toque para abrir a Sagas". Tapping opens the app.
  The widget is never empty and never shows a broken image.

## Visual rules

- **Theme per genre** comes from the remote config `*_visual_config`: `colorPalette`, `primaryColor`
  and `cornerSizeDp` (widget corner radius). Fonts also come from there; the prototype only
  approximates them.
- **Legibility rule:** text never sits on art unless a solid scrim is under it.
    - Wide widgets: text on the left half, art on the right, horizontal gradient from solid to
      transparent. The text column never overlaps the art.
    - Square widgets: art on top, vertical gradient that is solid from about 60% of the height down,
      and every piece of text lives in that solid part. The face is centered in the visible top part.
    - Tiny sizes (2x1): art only as a faint tint under a strong scrim.
- **Verified in the prototype:** the contrast of every text, measured on the pixels behind it with
  the text hidden, is at least 4.5:1 across five genres, with and without discreet mode. Worth
  repeating on the real bitmaps.
- **Light genres:** Shinobi artwork is ink on paper, so its widgets are light with dark text and a
  red accent.
- **Invitation gradient:** dark base, color glow only in the top-right corner, away from the text.
  Letting the glow reach the text dropped contrast to about 2.3:1 in Shinobi.

## Android sizes

Minimum size in dp is `70 x cells - 30`: 1 cell 40, 2 cells 110, 3 cells 180, 4 cells 250.

| Widget | 1x1 | 2x1 | 4x1 | 2x2 | 4x2 | 4x3 |
|---|---|---|---|---|---|---|
| Continue | yes | yes | yes | yes | yes | - |
| Last message | - | yes | yes | yes | yes | - |
| Your stories | - | - | - | yes | yes | yes |
| Quote | - | - | yes | yes | yes | yes |
| Character hook | yes | - | yes | yes | yes | - |

Glance declares `SizeMode.Responsive` with these as `DpSize` breakpoints and one layout per size.
`targetCellWidth` and `targetCellHeight` (Android 12+) let the launcher open the widget at the right
size, and `resizeMode` lets the player stretch it. Three-column widths are not designed: Glance
picks the closest layout below the available space.

## Technical notes

- **Glance limits:** no multi-stop gradients, masks, blur, shaders or custom animation. Gradients
  are XML drawables or bitmaps, and art, face crops, the discreet blur and the horizontal fade are
  rendered by a worker into bitmaps.
- **Automatic widgets:** a worker writes the latest values after each turn and calls
  `GlanceAppWidget.update`. The widget only reads that cached state.
- **Generated widgets:** one stored configuration per widget (type, `sagaId`, `characterId`, lines,
  selected lines, size, rotation rule, discreet flag). Room or DataStore.
- **Rotation:** on app open or from a daily worker. Without opening the app, "each app open" never
  advances, so the daily option needs the worker.
- **Offline:** widgets show their last cached state.

## Open decisions

- Entry point: `ShareType.WIDGET` in the existing sheet, or a dedicated screen.
- Widget limit (the prototype assumes 10) and lines per widget (assumes 10).
- Which stories the 2x2 shelf shows when there are more than two (proposed: most recent).
- Whether Continue pinned to a finished saga should stay that way or move on by itself (current
  proposal: it moves on, and the finished saga lives in Your stories).
- A secondary shortcut to the retrospective of a finished saga.
- A narrator-voiced Quote for the whole saga (the prototype covers characters only).

## Roadmap integration

Fits the Engagement track. Needs a `feature_planning` pass before scheduling: reusable
(`SceneSummary`, `Character*`, `visual_config`, `GemmaClient`, WorkManager already configured)
versus net new (Glance dependency, the bitmap renderer, the widget configuration store, the
creation screens).
