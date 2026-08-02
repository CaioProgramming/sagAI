# Sagas — Documentation Map

Entry point for `/docs`. Written to be read by humans and by AI agents working in this repo alike —
if you (human or agent) need to know *where something lives* or *what's still true*, start here
instead of grepping around.

## Rule of thumb

Everything in `/docs` except `archive/` is meant to be **currently accurate**. If a doc describes a
decision, a system, or a plan that's no longer true, it belongs in `archive/`, not left stale in a
"live" folder. When you update a feature, update its doc in the same change — don't let this drift.

## Where to look

| Folder | What's there | Read it when |
|---|---|---|
| [`features/`](features/) | One doc per **shipped** feature (milestones, expressive messages, Saga Wrapped, image references, message editing) | You need to understand how something that already exists works |
| [`feature_ideas/`](feature_ideas/) | Backlog of ideas **not yet built** — specs, brainstorms, half-formed proposals | You're exploring what could be built next, or checking if an idea already has a writeup |
| [`feature_planning/`](feature_planning/) | The actual roadmap (`roadmap.md`) plus one folder per feature in planning, each with a `task.md`/`plan.md` (objective, what's reusable, what's missing, risk, recommendation) | You need to know what's prioritized, parked, cancelled, or proposed — and why |
| [`architecture/`](architecture/) | How the systems work: `image_generation/` and `character_generation/` (multi-pillar content generation), `ai/` (Gemma model tiering, anti-hallucination prompting), `audio/` (sound design), `notifications.md`, `analytics.md`, `release_debug_symbols.md` | You're building on top of an existing system and need the real mechanics, not just the feature description |
| [`marketing/`](marketing/) | `marketing_canvas.md` (live plan: what/why/target/platform/app-resources-needed), plus published output in `social_posts/` and `linkedin_posts/` | You're planning or writing marketing content |
| [`release_notes/`](release_notes/) | Per-version changelog, one file per release | You need to know what shipped in a given version |
| [`faq/`](faq/) | Live FAQ data (`faq_data_en.json`, `faq_data_pt.json`) shown in-app | You're updating the in-app FAQ |
| [`archive/`](archive/) | Superseded docs, kept for historical context only | You want to know *why* a decision was made, not *what's true now* |

## The three agents that write most of this

- **`product_agent`** (`.agent/workflows/product_agent.md`) — audits the real app and code, splits
  findings into UX improvements and premium opportunities (always additive, never a paywall on the
  core loop).
- **`marketing_strategy_agent`** (`.agent/workflows/marketing_strategy_agent.md`) — turns the
  product state into `marketing/marketing_canvas.md`. Opens cold campaigns with the simplest
  universal hook first, mechanics second.
- **`feature_planning_agent`** (`.agent/workflows/feature_planning_agent.md`) — takes an idea
  needing technical validation and files it into `feature_planning/roadmap.md` with a real
  reusable-vs-net-new assessment, not a guess.

## Conventions

- **Folder-per-topic with a `README.md` entry point** for anything with more than ~3 related files
  (see `architecture/image_generation/` as the reference example).
- **Archive, don't delete, don't leave stale.** If a doc no longer reflects reality, move it to
  `archive/` and add one line to `archive/README.md` saying what replaced it.
- **Snake_case filenames**, lowercase, no ALL-CAPS except acronyms.
