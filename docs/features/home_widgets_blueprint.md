# Home widgets: the `widget_lines_blueprint`

Status: **draft, not published.** The widgets work today without it. This note says what the
blueprint is for, what to publish, and the small code change that makes the app read it.

## What it drives

Only the two **generated** widgets (Quote and Hook, created from the character screen) call the
model. One call writes a batch of 10 short lines in the character's voice; the player picks which
to keep. The automatic widgets (Continue, Last message, Your stories) never call the model.

The call is `WidgetLinesUseCase.generate` (`features/widgets/data/WidgetLinesUseCase.kt`):
`gemmaClient.generate<WidgetLinesGen>` with `ModelRequirement.LOW` and temperature `0.9`. The
response schema is just:

```json
{ "lines": ["...", "..."] }
```

## What is in the code today

`WidgetLinesPrompt.instructions(type)` builds the system-side instructions in Kotlin, and
`WidgetLinesPrompt.build(facts)` builds the user-side context. The `SplitPrompt` carries
`blueprintKey = "widget_lines"`, but nothing is fetched from Remote Config. Consequences:

- Publishing a blueprint changes nothing until the code reads it (see "Wiring" below).
- The key `widget_lines` does not follow the convention (keys end in `_blueprint`). Use
  **`widget_lines_blueprint`** and update `WidgetLinesPrompt.BLUEPRINT_KEY` when wiring.
- The AI audit screen looks the blueprint up by `blueprintKey`; today it shows "Blueprint not
  found" for these calls.

## What the model receives

Task context (built in code, never in the blueprint, per `docs/blueprints.md`):

| Section | Source | Limit |
| :-- | :-- | :-- |
| `CHARACTER` | name, occupation, personality, backstory | backstory 700 chars |
| `SAGA` | title, genre aesthetic from Remote Config | 700 chars |
| `HOW THE CHARACTER CHANGED` | last 3 character arcs | 260 chars each |
| `WHAT HAPPENED TO THE CHARACTER` | last 5 events | 260 chars each |
| `BONDS` | 4 relationships | 260 chars each |
| `WHERE THE STORY STANDS` | current objective, **Hook only** | |
| `ALREADY WRITTEN` | lines to avoid, on "generate more" | |
| `TASK` | count, type, max characters (Quote 160, Hook 140) | |

The chat is deliberately **not** sent. A finished saga can hold hundreds of messages per character;
arcs, events and bonds are already summarized in the database and carry what the lines need.

## Blueprint to publish

Key: `widget_lines_blueprint` (JSON string, `PromptBlueprint` shape). It mirrors the in-code text,
with the Quote/Hook difference split into two directives that the task line selects.

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

Notes on the choices:

- No `template` (deprecated). Everything variable arrives as task context.
- `LANGUAGE` is new. The in-code version has no locale instruction, so lines follow whatever the
  character data is written in. Keep it, or drop it if the profile language is already enough.
- The length limits are **not** in the blueprint on purpose. They are enforced in code by
  `WidgetLinesSanitizer` (lines over the limit are dropped), and the task line states them.

## Wiring (small follow-up change, not in PR #71)

1. Rename `BLUEPRINT_KEY` to `widget_lines_blueprint`.
2. In `WidgetLinesUseCase`, build the prompt through `PromptService.buildSplitBlueprint(...)` and
   keep `WidgetLinesPrompt.build(facts)` as the processed task context.
3. **Keep the in-code instructions as a fallback.** `buildSplitBlueprint` ends in
   `getJson<PromptBlueprint>(key)!!`, so a missing key throws; catch it and use
   `WidgetLinesPrompt.instructions(type)` so an unpublished or broken blueprint never breaks
   widget creation.
4. Add the row to the registry in `docs/blueprints.md` (category *Widget*).

## Publishing checklist

- [ ] Add `widget_lines_blueprint` in Firebase Remote Config with the JSON above (one string value).
- [ ] Publish. Remote Config is cached on the device, so test on a fresh fetch.
- [ ] Merge the wiring change.
- [ ] In the AI audit screen, confirm a widget generation shows the blueprint instead of "not found".
- [ ] Generate for a character with rich history and one with almost none (the creator warns when
      there is little to draw on) and read the lines.

The blueprint is safe to publish before or after the code: until step 2 is merged it is ignored,
and after it the fallback covers a missing key.
