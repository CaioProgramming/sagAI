# Chat: mascot eyes instead of the objective island

Status: idea, decided with Caio on 2026-11-10, for 1.20. Nothing implemented. Prototype:
[`../prototypes/island-eyes/island-eyes.html`](../prototypes/island-eyes/island-eyes.html)
([artifact](https://claude.ai/artifact/GqbdEg1cpYFDWa6zgpmr3t)).

## What changes

- The objective island at the top of the chat is removed. The island stays only for image generation, as today.
- Above the saga title there are only the mascot's eyes: small, like a toolbar icon, solid neutral colour (never the
  emotional tone's colour), no frame, no shadow, no hint that they can be tapped (an easter egg).
- The eyes react to the `EmotionalTone` of the last message. The 14 tones reuse the v3 `MascotExpression` table from
  Remote Config: no new key. Neutral just blinks and looks around, which is enough to show something is there.
- Tones that look alike (Neutral/Determined, Angry/Frustrated/Cynical, Sad/Melancholic/Concerned) stay as they are.
  The player is not told the exact feeling; the movement and distortion of the eyes is enough.
- When the objective changes, the card opens by itself after 5 s and stays 15 s, unless the player is typing.
  Tapping the eyes also opens it. It is an `IosStyleMenu` popup that shows the whole mascot as if a bot were
  saying the objective.

## Needs

- `IosStyleMenu` / `IosStyleMenuHost` need a centre anchor (`Alignment.TopCenter`) so the card grows from where the
  eyes shrink.
- A "typing" signal from the chat input.
- A mode of `BlobMascot` that draws only the eyes (solid fill) next to the current one (holes in the head).

## Keep in mind: mascots by theme

The genre-mascots bestiary ([`../prototypes/genre-mascots/`](../prototypes/genre-mascots/genre-mascots.html)) gives each
genre its own silhouette and its own eye style (drop, visor, oval, slit, skull). The header eyes and the popup mascot
must follow the saga's genre, and the 14-expression table has to hold for each eye style (the bestiary scales the tilt
per style with `rotK`). The prototype ports only Cyberpunk (cursor), Fantasy (dragon) and Horror (ghost).
