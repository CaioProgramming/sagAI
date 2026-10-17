# Prototypes — Reference Index

Every UI/visual prototype made for Sagas (HTML/WebGL mockups published as Claude artifacts, spikes
that explore a look before it is built in Compose) is registered here, so a new session or a new
teammate can find what was tried, what was decided and where the code lives.

## Rules

- **The HTML in this repo is the source of truth.** Artifact links are a convenience: they are
  private to the author's claude.ai account and can disappear. Always keep a copy of the prototype
  under `docs/prototypes/<slug>/`.
- **One row per prototype.** Before adding a row, search this file for the artifact URL (or its
  id, the part after `/artifact/`). If it is already listed, update that row instead of adding a
  second one.
- **Keep the explanation short** (2–3 lines): what the prototype explores, and its status.
- If the prototype led to decisions, link the doc that records them (usually in `../feature_ideas/`
  or `../features/`).
- The `prototype-registry` skill (`.claude/skills/prototype-registry/SKILL.md`) does this
  automatically when a prototype is published.

## Index

| Prototype | Artifact | Repo copy | Date | What it explores | Status |
|---|---|---|---|---|---|
| **Genre titles lab** | [claude.ai/artifact/5aeZt3hGjkxF3TwiDrQJbQ](https://claude.ai/artifact/5aeZt3hGjkxF3TwiDrQJbQ) | [`genre-titles/genre-titles.html`](genre-titles/genre-titles.html) | 2026-10-14 | One animated title effect per genre (fire, ghost, branded leather, katana slit, iridescent gradient, comic energy, plus the existing Cyberpunk, Space Opera and Punk Rock looks), all derived from a single text color, with a simulated layout box to check containment, touch interaction (drag to cut, lens, wind, liquid warp) and a per-genre entry animation (handwriting, typewriter, slam, materialize). Decisions: [`genre_title_effects.md`](../features/genre_title_effects.md). | Built in Compose (AGSL) for all nine genres, API 33+, and checked on a device against the lab (2026-10-16); the repo copy of the lab stays the source of the shaders (`scripts/genre-titles/port_agsl.py`) and is newer than the published artifact. |
| **UI icon pack** | [claude.ai/artifact/V2c8HMSfRtwTCVneS2tjkW](https://claude.ai/artifact/V2c8HMSfRtwTCVneS2tjkW) (private to the author's account) | [`ui-icon-pack/ui-icon-pack.html`](ui-icon-pack/ui-icon-pack.html) | 2026-10-17 | Review board for the redrawn UI icons: each of the 43 new icons next to the one it replaces, the old-to-new name map, the list of unreferenced drawables, and a rejected redraw of the nine genre icons. Decisions: [`ui_icon_pack.md`](../feature_ideas/ui_icon_pack.md). | UI icons applied in the app (PR into `feature/1.18.0`); the genre icon proposal was not adopted. The generator is `ui-icon-pack/gen_ui_icons.py`. |
| Fantasy fire text (first iteration) | [claude.ai/artifact/ALLFeHKRhJQYHn7rMgPTdN](https://claude.ai/artifact/ALLFeHKRhJQYHn7rMgPTdN) | superseded by the lab above (its Fantasy tab) | 2026-10-14 | First version of the flame-from-letters effect, color-driven by the text color. | Superseded |
