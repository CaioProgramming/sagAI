# Gemma Model Rework Strategy: Multi-Tiered AI Architecture

This document outlines the finalized strategy for load-balancing the AI engine across the Gemma 2/3
model family to solve token bottlenecks and optimize response quality.

## 1. Governance & Configuration

The system moves away from hardcoded model IDs. Instead, it uses a **Requirement-Based Selection**
pattern similar to the `Genre` system.

- **Requirement Enum**: `LOW`, `MEDIUM`, `HIGH`.
- **Dynamic Routing**: Each requirement resolves to a Firebase Remote Config flag.
- **Hot-Swappability**: Updating a flag in Firebase (e.g., `gemma_high_tier`) updates the model
  globally without a code release.

## 2. Tiered Responsibility Mapping

| Tier       | Flag                | Default Model    | Rationale                                                                                                     |
|:-----------|:--------------------|:-----------------|:--------------------------------------------------------------------------------------------------------------|
| **HIGH**   | `gemma_high_tier`   | `gemma-3-27b-it` | **The Architect**: Narrative Replies, Chapter/Act Creation, Image Reviewer, Heartfelt Conclusions.            |
| **MEDIUM** | `gemma_medium_tier` | `gemma-3-12b-it` | **The Analyst**: Scene Summarization, Wiki Extraction, Image Composition Analysis, Clinical Emotional Review. |
| **LOW**    | `gemma_low_tier`    | `gemma-3-1b-it`  | **The Utility**: Sentiment Classification, Typo Correction, Reaction Generation.                              |

## 3. Solving the "Token Summing" Flaw

Chained calls (Tone -> Summary -> Reply) previously hit the 15k limit because they shared a model
ID.

- **Strategic Split**: By calling `1B` for tone and `12B` for summary, we use separate quotas.
- **Context Distillation**: The `12B` Analyst distills the raw history into a technical summary,
  which is the *only* context passed to the `27B` Architect. This preserves the 131k context window
  of the 27B for creativity rather than history tracking.

## 4. Visual & Emotional Core Strategy

- **Visual Core**: Preserve the 3-step pipeline (Director, Artist, Reviewer). Director and Reviewer
  remain on **HIGH (27B)** to ensure aesthetic quality. Composition Extraction moves to **MEDIUM (
  12B)**.
- **Emotional Core**: Clinical note-taking (Reviewer) moves to **MEDIUM (12B)** for objective
  analysis. The "Grand Finale" (Conclusion letter) remains on **HIGH (27B)** for creative warmth.

## 5. Reliability Protocol

- **HIGH Priority Retries**: Critical narrative paths will implement a mandatory retry policy (2
  attempts, 10s delay).
- **Fallback Logic**: If a specialized tier (1B/12B) fails, the system defaults to the 27B or a
  stable Vertex AI model to prevent flow interruption.

---
*Status: Ready for implementation in the next phase (`feat/gemma-model-load-balancing` branch).*
