# Firebase Remote Config Implementation Guide

## 1. New Model Capability Flags

Add these flags to your Firebase Remote Config console to enable the tiered architecture.

| Parameter Key           | Default Value           | Description                                                                                           |
|:------------------------|:------------------------|:------------------------------------------------------------------------------------------------------|
| **`gemma_low_tier`**    | `models/gemma-3-1b-it`  | **The Utility (1B)**: Fast classification (Tone, Typos, Reactions).                                   |
| **`gemma_medium_tier`** | `models/gemma-3-12b-it` | **The Analyst (12B)**: Context, Wiki, Summaries, Clinical Notes, Audio Config.                        |
| **`gemma_high_tier`**   | `models/gemma-3-27b-it` | **The Architect (27B)**: Narrative Generation, Chapter Creation, Image Review, Heartfelt Conclusions. |

## 2. Token Optimization Strategy

With the new architecture, we have implicitly optimized tokens by:

1. **Splitting "Context" from "Creative"**:
    * *Old Way*: One giant prompt doing summary + tone + reply.
    * *New Way*: `12B` generates a concise `SceneSummary`. Only this summary (not the raw chat log)
      is passed to the `27B` for the final reply. This drastically reduces the input token load for
      the most expensive model.
2. **Specialized Context Windows**:
    * 27B has 128k context, allowing for "Whole Book" awareness if needed.
    * 12B and 1B are restricted to shorter, focused tasks, preventing "History Pollution".

## 3. Recommended Prompt Refinements (Next Steps)

Now that specific models handle specific tasks, we can strip "defensive" instructions from prompts:

- **For 1B (Low Tier)**: Remove comprehensive role descriptions. It just needs "You are a typo
  fixer. Fix this:".
- **For 27B (High Tier)**: Remove "Be logical" or "Check consistency" instructions. Trust the
  model's intelligence and the `12B`'s summary. Focus purely on style and voice.
