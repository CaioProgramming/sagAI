# Gemini CLI Instructions

- Always use `./gradlew installDebug` instead of `./gradlew assembleDebug` when testing or verifying
  features to ensure they are installed directly on the user's device for testing.
- When you create or publish a UI/visual prototype, register it in `docs/prototypes/README.md`
  (check the artifact link is not already listed, add a row with a short explanation, and keep a
  copy of the HTML under `docs/prototypes/<slug>/`). See `.claude/skills/prototype-registry/SKILL.md`.
- Genre titles (`Genre.stylisedText`) use AGSL shaders from `app/src/main/assets/genre_title_*.agsl`, generated
  from the lab by `scripts/genre-titles/port_agsl.py`; do not hand-edit them and do not put `gradientFill` or
  `genreVfx` on titles where `genre.usesTitleFx()`. See `docs/features/genre_title_effects.md`.
