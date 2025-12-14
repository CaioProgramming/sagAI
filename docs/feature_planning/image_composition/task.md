# Task Plan: Refine Image Composition & Art Direction Logic

**Status:** ⬜ Pending
**Target Files:**

- `com/ilustris/sagai/core/ai/prompts/ImagePrompts.kt`
- `com/ilustris/sagai/core/ai/prompts/SagaPrompts.kt`

## **Objective**

Upgrade the entire image generation pipeline to follow an "Art Director" workflow.

1. **Extraction (`ImagePrompts`):** Extract *dramatic intent, emotion, and nuance* from reference
   images.
2. **Execution (`SagaPrompts`):** Treat this extraction as a "Creative Brief" that modifies the
   subject's presentation, ensuring the final prompt is a cohesive artwork, not just a list of
   items.

---

## **Phase 1: Prompt Engineering (The "Art Director" Extraction)**

*File: `ImagePrompts.kt`*

- [ ] **Refine `extractComposition` Persona**
    - Define AI as **"Expert Art Director & Cinematographer"**.
    - Prioritize *feeling* over *statistics*.

- [ ] **Expand the 5 Extraction Points**
    - Update labels to be evocative:
        1. `Framing & Narrative Focus`
        2. `Emotional Palette & Atmosphere`
        3. `Visual Language & Perspective`
        4. `Dramatic Lighting & Tonal Contrast`
        5. `Artistic Texture & Medium`

- [ ] **Implement "Visual Adjectives" Rule**
    - Constraint: "Use evocative, sensory adjectives (e.g., 'ethereal,' 'grimy') rather than
      technical terms."

---

## **Phase 2: Integration (The "Concept Artist" Execution)**

*File: `SagaPrompts.kt`*

- [ ] **Refine `iconDescription` Persona**
    - Change persona to **"Lead Concept Artist & Illustrator."**

- [ ] **Implement "Creative Brief" Logic (The Synthesis)**
    - **Input Handling:** Rename `visualDirection` input in the prompt to **"The Art Director's
      Creative Brief."**
    - **Synthesis Rule (CRITICAL):** Add a specific instruction for blending:
      > "Do NOT describe the reference image itself. Instead, paint the **Saga Subject** using the *
      *Texture, Lighting, and Mood** defined in the Brief. If the Brief is dark and gritty, the
      Subject (e.g., a Shield) must look rusted and shadowed."
    - **Background Handling:** Explicitly forbid generic backgrounds.
      > "The background must be abstract or atmospheric, derived *solely* from the 'Emotional
      Palette' of the Brief. Do not copy the specific scenery of the reference, only its *essence* (
      e.g., 'swirling mists', 'neon void')."

- [ ] **Robust Fallback Strategy**
    - Logic: If `visualDirection` is missing/null:
      > "Strictly apply the default `Genre Art Style`. You must invent a dramatic lighting and
      texture profile that fits this Genre perfectly."

- [ ] **Enforce Generation Patterns**
    - Append `ImagePrompts.descriptionRules(genre)` at the very end to ensure technical compliance (
      Full bleed, No Text, etc.) regardless of the art direction.

---

## **Phase 3: Synthesis & Validation**

*File: `ImagePrompts.kt` (Conversion Guidelines)*

- [ ] **Update `conversionGuidelines`**
    - Add Section 0: "The Directorial Vision." Character details must be rendered through this mood
      lens.

- [ ] **Enhance "Style Precedence" Logic**
    - Rule: "If Character Description contradicts Art Direction, **Art Direction** wins (e.g., Happy
      character in Horror -> Sinister smile)."

- [ ] **A/B Test - Genre Check**
    - Verify distinct visual outputs for the same character across different genres/references.
