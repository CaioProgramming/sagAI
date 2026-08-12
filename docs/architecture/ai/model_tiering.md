# Gemma Model Tiering: Multi-Tiered AI Architecture

Strategy for load-balancing the AI engine across the Gemma 2/3 model family to solve token
bottlenecks and optimize response quality.

## 1. Governance & Configuration

The system avoids hardcoded model IDs. Instead, it uses a **Requirement-Based Selection** pattern
similar to the `Genre` system.

- **Requirement Enum**: `LOW`, `MEDIUM`, `HIGH`.
- **Dynamic Routing**: Each requirement resolves to a Firebase Remote Config flag.
- **Hot-Swappability**: Updating a flag in Firebase (e.g., `gemma_high_tier`) updates the model
  globally without a code release.

### Remote Config flags

| Parameter Key | Default Value | Tier |
|:---|:---|:---|
| `gemma_low_tier` | `models/gemma-3-1b-it` | **LOW — The Utility**: Fast classification (Tone, Typos, Reactions). |
| `gemma_medium_tier` | `models/gemma-3-12b-it` | **MEDIUM — The Analyst**: Context, Wiki, Summaries, Clinical Notes, Audio Config. |
| `gemma_high_tier` | `models/gemma-3-27b-it` | **HIGH — The Architect**: Narrative Generation, Chapter Creation, Image Review, Heartfelt Conclusions. |

## 2. Solving the "Token Summing" Flaw

Chained calls (Tone -> Summary -> Reply) used to hit the 15k limit because they shared a model ID.

- **Strategic Split**: Calling `1B` for tone and `12B` for summary uses separate quotas.
- **Context Distillation**: The `12B` Analyst distills the raw history into a technical summary,
  which is the *only* context passed to the `27B` Architect. This preserves the 27B's 128k context
  window for creativity rather than history tracking.
- **Practical effect**: one giant prompt doing summary + tone + reply is replaced by `12B`
  generating a concise `SceneSummary`; only that summary (not the raw chat log) reaches `27B` for
  the final reply, drastically reducing input token load on the most expensive model.

## 3. Visual & Emotional Core Strategy

- **Visual Core**: Preserve the 3-step pipeline (Director, Artist, Reviewer — see
  `docs/architecture/image_generation/`). Director and Reviewer stay on **HIGH (27B)** for
  aesthetic quality. Composition Extraction runs on **MEDIUM (12B)**.
- **Emotional Core**: Clinical note-taking (Reviewer) runs on **MEDIUM (12B)** for objective
  analysis. The "Grand Finale" (Conclusion letter) stays on **HIGH (27B)** for creative warmth.

## 4. Reliability Protocol

- **HIGH Priority Retries**: Critical narrative paths use a mandatory retry policy (2 attempts, 10s
  delay).
- **Fallback Logic**: If a specialized tier (1B/12B) fails, the system defaults to the 27B or a
  stable Vertex AI model to prevent flow interruption.

## 5. Prompt Refinement Guidance

Since specific models now handle specific tasks, prompts can drop redundant "defensive"
instructions:

- **For LOW (1B)**: Skip comprehensive role descriptions — it only needs something like "You are a
  typo fixer. Fix this:".
- **For HIGH (27B)**: Skip "be logical" / "check consistency" instructions. Trust the model's
  intelligence and the MEDIUM tier's summary; focus the prompt purely on style and voice.
