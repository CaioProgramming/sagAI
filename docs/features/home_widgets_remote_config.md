# Home widgets: everything to publish in Remote Config

Context for whoever (or whatever agent) publishes to Firebase Remote Config. Feature: Android home
widgets (PR #71, branch `feature/home-widgets`, base `feature/1.18.0`).

## Summary

**One new parameter. Nothing else changes.**

| Parameter key | Action | Type | Required for PR #71? |
| :-- | :-- | :-- | :-- |
| `widget_lines_blueprint` | **Add** | JSON (string value) | No, ignored until the wiring change lands |
| `*_visual_config` (every genre) | None. Read only: `colorPalette`, `primaryColor`, `cornerSizeDp` | existing | Already published |
| `prompt_directives`, other blueprints | None | existing | |

No conditions, no percentage rollout, no new parameter groups are needed. Do not touch existing
parameters; the widgets only read the genre `*_visual_config` values that already exist.

## Parameter to add

- Key: `widget_lines_blueprint` (must end in `_blueprint`)
- Value type: JSON
- Default value (paste exactly):

```json
{
  "title": "Widget lines",
  "role": "You write the short lines that appear on a player's phone home screen, in the voice of a character from the story they played.",
  "directives": {
    "VOICE": "Speak as the character, in first person, with the register their personality and world imply. Poetic, never generic.",
    "SPECIFIC": "Draw on the character's own history, wounds and bonds. A line that fits any character is a failed line.",
    "NO_SPOILERS_OF_THE_CHAT": "Do not quote or retell specific conversations. Evoke what the character lived through instead.",
    "FORMAT": "Plain text only. No quotation marks around the line, no numbering, no emojis, no hashtags, no stage directions. One sentence or two short ones.",
    "SAFETY": "Nothing graphic, sexual or hateful: these lines sit on a home screen anyone may glance at.",
    "KIND_QUOTE": "When the task asks for quotes: reflective, quotable, self-contained.",
    "KIND_HOOK": "When the task asks for hooks: warm, a little mysterious, an invitation. Never guilt the player for being away.",
    "LANGUAGE": "Write in the language of the player's app, matching the language of the character's profile."
  }
}
```

Shape is `PromptBlueprint` (`core/ai/model/PromptBlueprint.kt`): `title`, `role`, `directives`
(string to string). Do not add a `template` (deprecated); all variable data arrives from the app as
task context.

## What the app does with it

- Used by the two **generated** widgets (Quote, Hook) when the player creates one from the
  character screen. One model call returns `{ "lines": ["...", ...] }`, 10 lines.
- Per-type limits (Quote 160 chars, Hook 140) are enforced in code, not in the blueprint.
- The app currently builds equivalent instructions in Kotlin and does **not** fetch this key yet.
  A follow-up change will rename `WidgetLinesPrompt.BLUEPRINT_KEY` from `widget_lines` to
  `widget_lines_blueprint` and read it through `PromptService.buildSplitBlueprint`, keeping the
  in-code text as fallback. Publishing first is safe: it is ignored until then.

## Steps

1. Firebase console, Remote Config, **Add parameter** `widget_lines_blueprint`, type JSON, paste
   the value above.
2. Validate the JSON parses (no trailing commas, straight quotes).
3. **Publish changes.**
4. Update the registry in `docs/blueprints.md`: add a row `widget_lines_blueprint`, category
   *Widget*, "Short in-character lines for the generated home-screen widgets (Quote, Hook)."
   Mark it implemented only after the wiring change merges.

## Verification

- The parameter appears in the published template with the exact JSON.
- After the wiring change: open the AI audit screen on a widget generation; it should show the
  blueprint instead of "Blueprint not found for key".
- Generate a Quote and a Hook for a character with rich history and one with almost none; read the
  lines for voice, no quotation marks, no chat quotes.

## Rollback

Delete the parameter. With the wiring fallback in place, the app uses its in-code instructions and
nothing breaks.

More background: `docs/features/home_widgets_blueprint.md`, `docs/features/home_widgets.md`.
