# Share Collector Cards

Rebuild the share feature around one genre-themed card that tilts in 3D and carries a holographic
foil, and remove the message-block share. Status: **phase 1 code written and compiling, not yet run on a device** (`features/share/ui/card/`, `assets/holo_foil.agsl`, `CharacterShareView`). Phases 2 to 4 not started. The
visual direction was settled in an HTML prototype (2026-10-11), kept at
`.claude/skills/sagas-prototyping/reference/share-cards.html`.

## Decisions already made

- **Character card**: the character's art is the full-bleed background (not a cutout on stars).
  Name and occupation on top, a 90° strip on the right (`SagaTitle`, genre, character id), and only a
  short text at the bottom over a gradient fade. No level number, no "Protagonista", no genre under
  the name.
- **Saga card**: the same chassis with the saga cover (`saga.icon`) as background, the title on top
  and a short synopsis. It takes the place of the `HISTORY` share, the same entry point in
  `SagaDetailInitialView`. The playstyle share is later re-skinned onto it with different text.
- **Emotional card**: no image, no 3D. A "page from the saga's book", as wide as possible instead of
  tall.
- Every card keeps the `SagaTitle` (S✦GAS) brand.
- Colors, fonts and corner radius come from the genre's `<genre>_visual_config`
  (`GenreVisualConfig`). Genre color is `primaryColor`; gradients and holo are `colorPalette` in
  stored order (what `genre.gradient()` already builds).
- The message-block share is removed. Relations and History shares stay as they are for now.
- Order: Character, Saga, Emotional, then the removal.

## Shared pieces (built in phase 1, reused after)

- `CollectorCard` composable under `features/share/ui/card/`. Slots: background content, title,
  subtitle, body text, strip data. It owns the frame, the radius, the tilt, the foil and the fade.
  Character and Saga only feed it data.
- `ShareSheet` wraps its content in `SagAITheme(genre = saga.genre)` so `MaterialTheme.typography`,
  `LocalGenreVisualConfig` and shapes already carry the genre. Check that the `Dialog` window
  receives them; if not, pass the resolved values in explicitly.
- Tilt: pointer drag plus the gravity sensor. `rememberTiltLook` (`ui/theme/components/mascot`)
  already reads that sensor; extract the sensor part so both the mascot and the card use it. Null
  sensor means drag only.
- 3D: `graphicsLayer { rotationX / rotationY; cameraDistance }`.
- Holo: AGSL `RuntimeShader` following the `HeatHaze.kt` pattern (API 33 gate, uniforms validated at
  construction, colors as `setFloatUniform` vec3). Below API 33 (minSdk is 27) fall back to a palette
  gradient brush offset by tilt. Foil colors are the palette with the near-black entries trimmed.
- Name and title brush: `primaryColor` lightened until it reads over art. Give the text box padding
  so gradient text does not clip glyph overhang (Crime's script font lost its first letters in the
  prototype).
- Strip: `SagaTitle` rotated 90° next to the genre name and the id, `maxLines = 1`, no height cap.
- Radius: `cornerSizeDp` of the genre as dp (Shinobi 0, Crime 25), same as the rest of the app's
  shapes; Android scales it with density.

## Phase 1: Character card, PNG only

1. **Text contract first.** `share_character_blueprint` (Remote Config) must return a short text,
   about 110 characters. Add a word-boundary truncation in Kotlin as a safety net, not as the fix.
   Instructions live in the blueprint, not in Kotlin.
2. Build `CollectorCard` and the character content: name from `name` + `lastName`, occupation from
   `profile.occupation`, id from `id`, body from `ShareText.text`.
3. Depth: `DepthLayout(originalImage, segmentedImage)` with parallax driven by the tilt. When
   segmentation fails or is still running, show the plain image, not a spinner-only dialog.
4. Replace `CharacterShareView` / `CharacterCard` in the share flow. The card inside the character
   screen (`CharacterCard`) may keep its own use; check before deleting.
5. **Export**: capture the existing `graphicsLayer` at neutral tilt with the foil at a fixed phase,
   never mid-motion. Reuse `SharePlayViewModel.saveBitmap`. Wait for the genre fonts to resolve
   before capturing, or the PNG bakes in the default font.
6. Test on device across all 9 genres: radius 0 and 25, script fonts (Crime), blackletter
   (Fantasy), the Shinobi body font (over 4 MB, slow first load), a device without a gravity
   sensor, API < 33.

Done when: the share opens, tilts, shows the foil in the genre's palette, exports a PNG that matches
the on-screen card at rest, in all 9 genres.

## Phase 2: Saga card (replaces the HISTORY share)

- Reuse `CollectorCard` with `saga.icon` as background, the title on top and the synopsis at the
  bottom.
- `ShareType.HISTORY` keeps its name and its entry in `SagaDetailInitialView`; `HistoryShareView`
  renders the new card instead of `GTAStyleCover`. `GTAStyleCover` has no other caller, so delete it
  with the swap.
- Text: rewrite `share_history_blueprint` (Remote Config) from movie-teaser taglines to a short
  synopsis, same length limit as the character text. Keep `ShareHistoryArgs` unless the new text needs
  more context. Update the row in `docs/blueprints.md`.
- Check what the saga title and the strip id should be (`saga.id`).
- Re-skin `PLAYSTYLE` on the same chassis later, with `share_playstyle_blueprint` text.

## Phase 3: Emotional note

- Rewrite `EmotionShareView` as the "page" card: palette-tinted background, `primaryColor` title
  brush, divider with `ic_spark`, drop cap in the header font, caption left and `SagaTitle` right.
- Lay it out wide: fixed width, type sizes derived from that width, height wraps the text. Add a
  character limit to `share_emotional_blueprint` so a long text cannot stretch it.
- It currently uses `MaterialTheme.colorScheme.background`, so the same share looks different on a
  light-theme and a dark-theme phone. The note must not depend on the device theme.

## Phase 4: Remove the message-block share

Do this last, as its own commit. The selection mode in chat only serves this share (its bar has
close and share, nothing else), so the whole thing goes.

- `ShareType.CONVERSATION`, and `REVIEW_ACTIVITY` (declared in `ShareText.kt`, no usages).
- `ConversationShareView.kt`, its branch in `ShareSheet.kt`, and the `ChatViewModel` that
  `ShareSheet` instantiates only to call `getSelectedMessages()`.
- Chat: `ChatUiAction.ToggleSelectionMode`, `ToggleMessageSelection`, `ClearSelection`,
  `ShareConversation`; `selectionState` in `ChatState` / `ChatStateManager`; the selection bar and
  the `ShareSheet` call in `ChatView`; `isSelectionMode` in `ChatBubble`; the "select" entry in the
  message options sheet; `ChatViewModel.getSelectedMessages`.
- Strings such as `messages_selected` (both locales).
- Confirm with a search that nothing else reads `selectionState` before deleting.

## Remote Config to-do

- `share_character_blueprint`: short text. `share_emotional_blueprint`: character limit.
  `share_history_blueprint`: becomes a short synopsis.
- `fantasy_visual_config.iconColor` is stored as `#FFFFF` (5 digits) and will not parse.

## Out of scope

- Looping MP4 export of the tilting card. The audiobook and teaser exporters are the starting point.
- Relations and History shares, and the genre-specific star backdrops.

## Risks

| Risk | Mitigation |
|---|---|
| Generated text too long for the card | limit in the blueprint, truncation fallback |
| Decorative genre fonts hard to read small | test all 9 genres on device; adjust sizes per genre if needed |
| Holo looks different in AGSL than in CSS | prototype just the shader in Compose before building the rest |
| Tint of the foil washes out skin tones (Heroes, Punk Rock) | intensity is a single constant; tune on device |
| PNG baked with fallback fonts | gate export on resolved fonts |

## Findings from the audit logs (2026-10-12)

From `ai_audit_logs` on a debug device: 13 character, 2 history and 2 emotional share calls.

- **The `template` in four share blueprints named variables the code does not send.** History's
  template asked for `{sagaContext}` and `{actSummaries}` while the code sends `sagaMainContext` and
  `historyContext`; emotional's asked for `{sagaEmotionalReview}` and `{emotionalHistory}` while the
  code sends `emotionalReview` and `historyEmotionalSummary`; the same for relations and playstyle.
  A blueprint with a `template` does not get the `# TASK CONTEXT` fallback, so those variables never
  reached the model (history: 698 prompt tokens for 15 to 70 thousand characters of data). The
  generic output ("Empires fall. Stars bleed.") is the symptom. Only the character blueprint matched.
- **About 1,000 thinking tokens and 6 s per call** for a 30-token answer, on the MEDIUM tier.
  `SharePlayUseCase` now asks for `ModelRequirement.LOW`.
- **Character copy parrots the saga description** (three of thirteen outputs reuse "telhados
  úmidos") and falls into one shape ("Entre X e Y, ela ..."), and "Lâmina e X" titles repeat.
- **Missing data**: gender (the model guessed), nicknames, status, the character's arcs. The event
  list grew without bound (388 to 1,638 characters); history sent every act summary.

Changes: the five blueprints in `blueprints/share_blueprints.json` drop `template`; character, history
and emotional get new rules for the text of their card; the code sends the missing data, caps events,
emotions and chapter titles, and adds `journeyStats` to history and emotional.
