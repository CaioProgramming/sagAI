---
name: prototype-registry
description: Keep docs/prototypes/README.md in sync whenever a UI or visual prototype is created or published (HTML mockups, WebGL/shader spikes, anything published with the Artifact tool, or a prototype file saved for later). Use it every time you prototype something for Sagas, even if the user does not ask. Check whether the prototype's artifact link is already in the index, add a row with a short explanation if it is not, and keep a copy of the HTML in the repo.
---

# Prototype registry

Prototypes made in a session are easy to lose: artifact links are private to the author's claude.ai
account and the scratchpad is wiped with the container. The index in
`docs/prototypes/README.md` is how future sessions find what was tried and what was decided.

## When

Right after you publish (or update) a prototype with the Artifact tool, or whenever you finish a
standalone prototype file, even if the user did not ask. Do it once per prototype, not once per
republish.

## Steps

1. **Check the index.** Search `docs/prototypes/README.md` for the artifact URL or its id (the
   part after `/artifact/`). Use grep, do not rely on memory.
2. **Already listed:** update that row only if something changed (date, status, explanation).
   Never add a second row for the same URL, and do not edit rows of other prototypes.
3. **Not listed:** add one row to the Index table with:
   - a short name (bold),
   - the artifact link (`[claude.ai/artifact/<id>](url)`),
   - the repo copy path (step 4),
   - the date (`YYYY-MM-DD`),
   - **2–3 lines on what it explores** (the feature or look being prototyped, not how the code
     works),
   - a status (for example "Exploring", "Approved", "Superseded by …", "Built in Compose").
4. **Keep a copy.** Save the HTML (and any extra files) under `docs/prototypes/<slug>/` and link
   it in the row. If the prototype replaces an older one, mark the old row "Superseded" and say by
   what.
5. **Link decisions.** If the prototype produced decisions or rules, record them in a doc under
   `docs/feature_ideas/` (or `docs/features/` once built) and link it from the row. Do not repeat
   the decisions in the index.

## Notes

- Do not commit or push unless the user asked; just leave the files in the working tree and say
  what was added.
- The docs are in English; match that.
- If the prototype is private or sensitive, say so in the row instead of leaving the link out.
