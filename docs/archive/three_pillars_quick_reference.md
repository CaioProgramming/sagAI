# Quick Reference: Three Pillars System

## System Flow

```
Reference Image
    ↓
[DIRECTOR] extractComposition()
    • Analyzes with 15 technical cinematography points
    • Outputs: Camera angle (degrees), lens (mm), lighting (Kelvin), etc.
    ↓
[TRANSLATION LAYER] ← NEW!
    • Artist receives Translation Guide
    • Converts: "45°" → "from below looking up"
    • Converts: "20mm" → "dramatic exaggerated perspective"
    • Converts: "5500K" → "cool blue lighting"
    ↓
[ARTIST] iconDescription()
    • Creates 3-part structured prompt:
      1. Art Style (1 sentence)
      2. Cinematography Framework (2-3 sentences) ← MANDATORY NOW!
      3. Character Description (expressive, filtered by framing)
    ↓
[REVIEWER] reviewImagePrompt()
    • Validates 12 cinematography points (A1-A12)
    • Validates 5 art style points (B1-B5)
    • NEW: A12 checks for technical jargon leakage
    • Scores: cinematographyScore + artStyleScore
    • Outputs: Corrected prompt + violations
    ↓
Final Prompt → Image Generation
```

---

## Key Improvements

### ✅ Translation Layer

**Purpose:** Bridge technical Director and creative Artist  
**Location:** `SagaPrompts.kt` after Visual Direction section  
**Impact:** No more "f/8", "45°", "20mm" in final prompts

### ✅ Mandatory Structure

**Purpose:** Make cinematography prominent, not buried  
**Location:** `SagaPrompts.kt` Final Output Structure  
**Impact:** Every prompt has explicit 2-3 sentence cinematography section

### ✅ Quality Check

**Purpose:** Validate translation, not just presence  
**Location:** `ImagePrompts.kt` A12 validation  
**Impact:** Catches poorly-translated technical jargon

---

## Cheat Sheet: Common Translations

| Technical (Director)               | Visual (Artist Should Use)                         |
|------------------------------------|----------------------------------------------------|
| **Angle**                          |                                                    |
| Low-angle 45°                      | From below, looking up, towers overhead            |
| High-angle 30°                     | From above, looking down, vulnerable               |
| Eye-level                          | Directly facing, at viewer's height                |
| **Lens**                           |                                                    |
| Ultra-wide 20mm                    | Dramatic exaggerated perspective, converging lines |
| Portrait 85mm                      | Natural flattering perspective                     |
| Telephoto 200mm                    | Compressed layers, flattened depth                 |
| **Lighting**                       |                                                    |
| Hard 5500K side                    | Harsh cool lighting from the side, sharp shadows   |
| Soft 3200K front                   | Gentle warm illumination from front, even glow     |
| Backlit rim                        | Glowing edges, darkened subject                    |
| **Depth**                          |                                                    |
| Shallow f/1.4                      | Razor-sharp subject, creamy blurred background     |
| Deep f/11                          | Everything crisp from foreground to horizon        |
| **(Skip for flat/cartoon styles)** |                                                    |
| **Framing**                        |                                                    |
| Close-up                           | Face filling frame, forehead to chin               |
| Medium                             | Head to waist, upper body                          |
| Full body                          | Entire figure head to toe                          |

---

## Validation Checklist

When reviewing a prompt, verify:

### Cinematography (A1-A12)

- [ ] Camera angle described visually (not degrees)
- [ ] Focal length translated to perspective effect (not mm)
- [ ] Framing matches and NO body parts outside view
- [ ] Lighting direction + quality described (not Kelvin)
- [ ] Color mood specified (not temperature numbers)
- [ ] Environment has 3+ named objects
- [ ] NO technical jargon (f/, °, mm, K)

### Art Style (B1-B5)

- [ ] No banned terminology (check style rules)
- [ ] Required elements present (backgrounds, etc.)
- [ ] Anatomy matches style (abstract vs realistic)
- [ ] Background appropriate for framing
- [ ] No style contradictions (bokeh in flat styles)

### Overall

- [ ] 3-part structure visible
- [ ] Cinematography section is 2-3 sentences
- [ ] Character is expressive (not static)
- [ ] Reads like art direction, not camera manual

---

## Common Violations & Fixes

### ❌ TECHNICAL_JARGON_NOT_TRANSLATED

**Bad:** "Shot with 20mm ultra-wide lens at f/8"  
**Fix:** "Dramatic exaggerated perspective with converging lines"

### ❌ CAMERA_ANGLE_MISSING

**Bad:** No mention of camera position  
**Fix:** Add "captured from [below/above/level]"

### ❌ FRAMING_VIOLATION

**Bad:** Close-up prompt mentions legs  
**Fix:** Remove all body parts outside frame

### ❌ LIGHTING_WRONG

**Bad:** "Soft lighting" when harsh specified  
**Fix:** "Harsh lighting casting sharp shadows"

### ❌ ENVIRONMENT_MISSING

**Bad:** "Urban setting"  
**Fix:** "Urban alley with graffiti walls, dumpsters, fire escapes"

---

## Scoring Guide

### Cinematography Score (0-100)

- **90-100:** All specs captured and properly translated
- **75-89:** Most specs present, minor translation issues
- **50-74:** Some specs missing or poorly translated
- **0-49:** Critical specs missing or full of jargon

### Art Style Score (0-100)

- **90-100:** Zero violations
- **75-89:** Minor violations fixed
- **50-74:** Major violations present
- **0-49:** Critical style contradictions

### Overall Readiness

- **READY:** Can generate immediately
- **NEEDS_REVIEW:** Major fixes applied, validate
- **CRITICAL_ISSUES:** Still has severe problems

---

## Files Reference

```
app/src/main/java/com/ilustris/sagai/core/ai/prompts/
├── ImagePrompts.kt
│   ├── extractComposition()     ← Director (15-point technical)
│   └── reviewImagePrompt()      ← Reviewer (12+5 validation)
│
└── SagaPrompts.kt
    └── iconDescription()         ← Artist (with Translation + Structure)

app/src/main/java/com/ilustris/sagai/core/ai/models/
└── ImageReference.kt
    └── ViolationType enum        ← 19 violation types

docs/
├── three_pillars_analysis.md            ← Full flaw analysis (15 issues)
├── phase1_fixes_complete.md             ← Implementation details
├── reviewer_enhancement_summary.md      ← Reviewer deep-dive
├── reviewer_testing_checklist.md        ← Test scenarios
└── extract_composition_improvements.md  ← Director details
```

---

## Quick Test

**Input Reference:** Gorillaz-style low-angle urban shot

**Expected Artist Output Structure:**

```
1. [ART STYLE] "A gritty Gorillaz-style illustration with bold outlines."

2. [CINEMATOGRAPHY] "Captured from ground level looking sharply upward,
   characters tower overhead. Dramatic wide perspective with vertical 
   lines converging. Cool blue night lighting contrasts with warm orange 
   street glow from below. Urban alley with graffiti walls, dumpsters,
   tangled wires."

3. [CHARACTER] "Lead character with confident smirk, chest out, hands
   in pockets, defiant gaze directly at viewer..."
```

**Expected Reviewer Response:**

```json
{
  "cinematographyScore": 95,
  "artStyleScore": 95,
  "overallReadiness": "READY",
  "violations": []
}
```

---

**Status: Phase 1 Complete ✅**  
**Next: Test with real references, monitor metrics**

