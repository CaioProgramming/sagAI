# Archive: Old Image Generation Documentation

**Last Updated:** December 16, 2025

## Purpose

This folder contains **superseded documentation** from the image generation system development
process. These files have been consolidated into the new organized structure at
`/docs/image_generation/`.

---

## Archived Files

### three_pillars_analysis.md

**Status:** Superseded by individual pillar docs  
**Replacement:**

- `/docs/image_generation/01_director_pillar.md`
- `/docs/image_generation/02_artist_pillar.md`
- `/docs/image_generation/03_reviewer_pillar.md`

**Content:** Initial analysis of all three pillars with identified flaws and imbalances

---

### three_pillars_quick_reference.md

**Status:** Superseded by system flow doc  
**Replacement:** `/docs/image_generation/system_flow.md`

**Content:** Quick reference guide showing system flow and key improvements

---

### accent_color_enforcement_complete.md

**Status:** Merged into comprehensive docs  
**Replacement:**

- `/docs/image_generation/02_artist_pillar.md` (Accent Color section)
- `/docs/image_generation/03_reviewer_pillar.md` (B6/B7 checks)

**Content:** Documentation of accent color enforcement implementation

---

### background_requirements_complete.md

**Status:** Merged into comprehensive docs  
**Replacement:**

- `/docs/image_generation/02_artist_pillar.md` (Background section)
- `/docs/image_generation/03_reviewer_pillar.md` (B4/B5 checks)
- `/docs/image_generation/best_practices.md` (Background richness)

**Content:** Documentation of mandatory background requirements across all genres

---

### reviewer_enhancement_summary.md

**Status:** Superseded by reviewer pillar doc  
**Replacement:** `/docs/image_generation/03_reviewer_pillar.md`

**Content:** Summary of reviewer enhancement with 16 comprehensive checks (11 cinematography + 5 art
style)

---

### reviewer_testing_checklist.md

**Status:** Merged into troubleshooting  
**Replacement:**

- `/docs/image_generation/troubleshooting.md` (Testing section)
- `/docs/image_generation/best_practices.md` (Testing checklist)

**Content:** Checklist for testing reviewer validation functionality

---

### accent_color_and_background_enforcement.md

**Status:** Most recent iteration, merged into comprehensive docs  
**Replacement:**

- `/docs/image_generation/02_artist_pillar.md` (Both sections)
- `/docs/image_generation/03_reviewer_pillar.md` (B4-B7 checks)
- `/docs/image_generation/README.md` (Overview)

**Content:** Complete documentation of accent color + background enforcement updates (Dec 16, 2025)

---

## Why Archived?

These files were **iterative development documentation** that:

1. **Overlapped significantly** - Multiple files covering same topics
2. **Lacked clear organization** - Mixed analysis, summaries, and guides
3. **Made navigation difficult** - Too many .md files in root docs folder
4. **Contained outdated information** - Some analysis from early development

---

## New Structure Benefits

The new `/docs/image_generation/` structure provides:

✅ **Clear separation by pillar** - One file per component  
✅ **Comprehensive guides** - All info in one place per topic  
✅ **Easy navigation** - Logical folder structure  
✅ **Better maintenance** - Update one file, not five  
✅ **Onboarding friendly** - New developers can follow README → Pillars → Flow

---

## Should You Use These?

**Generally: No.** Use the new organized documentation instead.

**Exception:** If you need to review:

- Historical context of why certain decisions were made
- Evolution of the system over time
- Original analysis that identified flaws
- Specific implementation details from a particular phase

---

## New Documentation Structure

```
docs/image_generation/
├── README.md                   # Overview & quick start
├── 01_director_pillar.md      # Director (extractComposition)
├── 02_artist_pillar.md        # Artist (iconDescription)
├── 03_reviewer_pillar.md      # Reviewer (reviewImagePrompt)
├── system_flow.md             # Complete workflow
├── best_practices.md          # Guidelines & tips
└── troubleshooting.md         # Common issues & solutions
```

**Start here:** `/docs/image_generation/README.md`

---

## Questions?

If you need information that you think was in these archived files but can't find in the new docs,
check:

1. **Specific pillar file** - Most content merged into relevant pillar
2. **System flow** - Workflow and integration details
3. **Best practices** - Tips and guidelines
4. **Troubleshooting** - Issue resolution

If still not found, these archived files are preserved for reference.

---

**Archive Date:** December 16, 2025  
**Archived By:** System reorganization and consolidation

---

## Second Pass: Repo-Wide Cleanup (August 2026)

The first pass above only covered image generation docs. This pass archives one-off implementation
logs and superseded checklists from across the rest of `/docs`, following the same rule: **if the
information already lives in code, in a shipped feature doc, or in a newer canonical file, the log
that describes "how we got there" is historical, not a reference.**

| File | Why Archived | Replacement / current source |
|---|---|---|
| `phase1_fixes_complete.md` | Iteration log for the image gen Director/Artist/Reviewer prompts | `/docs/architecture/image_generation/` |
| `token_optimization_urgent.md`, `token_optimization_status.md` | One-off prompt token-budget fix, already shipped | Current prompt code (`ImagePrompts.kt`, `SagaPrompts.kt`) |
| `ai_message_tone_update.md` | Prompt tone change log from Jan 2026, already shipped | Current prompt code |
| `DJ_FX_INSTRUMENTS.md` | Early draft of genre instrument tables | `/docs/architecture/audio/sound_design.md` (superset, same content per genre) |
| `string_resources_extraction_report_faq.md` | Output log of a single `extract_strings` workflow run | The actual `strings.xml` resources it produced |
| `engineering_tasks_checklist_2025_09.md` (was `tasks.md`) | Generic, unmaintained engineering checklist from Sept 2025 | `/docs/feature_planning/roadmap.md` |
| `ux_feature_wishlist_2025_09.md` (was `feature_ideas.md`) | Generic, unmaintained UX wishlist from Sept 2025 | `/docs/feature_ideas/` (specific, actively used) |
| `faq_initial_data_drafts/` | Two early draft versions of the FAQ content (v1) | `/docs/faq/faq_data_en.json` + `faq_data_pt.json` (v2, live) |
| `marketing_v1/` (README, quick_reference, example_post, content_calendar, marketing_plan) | The fixed Mon/Wed/Fri content calendar + daily agents system, fully retired | `/docs/marketing/marketing_canvas.md`, generated by `marketing_strategy_agent.md` |
| `DOCUMENTATION_REORGANIZATION.md` | Log of the first (image-gen-only) reorg pass — its own "before/after" tree is now stale since `image_generation/` moved under `/docs/architecture/` | This README + `/docs/README.md` |

**Should you use these?** Same answer as the first pass: generally no — use the current docs. Keep
these only for historical context on how a decision or a piece of copy evolved.

