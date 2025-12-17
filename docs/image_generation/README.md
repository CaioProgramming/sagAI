# Image Generation System Documentation

**Last Updated:** December 16, 2025

## Overview

The SagAI image generation system uses a **Three-Pillar Architecture** to create unique,
cinematographically-precise, genre-authentic artwork for saga icons and character representations.

```
Reference Image → [DIRECTOR] → [ARTIST] → [REVIEWER] → Final Prompt → Generated Image
```

---

## The Three Pillars

### 🎬 [1. Director](./01_director_pillar.md)

**Function:** `extractComposition()`  
**Role:** Senior Cinematographer / Director of Photography  
**Purpose:** Analyzes reference images and extracts precise cinematographic DNA

### 🎨 [2. Artist](./02_artist_pillar.md)

**Function:** `iconDescription()`  
**Role:** World-Class Art Director / Concept Artist  
**Purpose:** Translates technical specs into compelling artistic prompts

### 🔍 [3. Reviewer](./03_reviewer_pillar.md)

**Function:** `reviewImagePrompt()`  
**Role:** Quality Assurance / Art Director Supervisor  
**Purpose:** Validates and corrects prompts before generation

---

## Critical Requirements

### ✅ Mandatory Elements

1. **Cinematography Precision**
    - Angle, framing, lighting must match reference
    - Technical specs translated to visual language
    - No jargon in final prompts

2. **Accent Color Integration**
    - Genre signature color strategically applied
    - Organic integration through lighting/environment
    - Makes genre instantly recognizable

3. **Rich Backgrounds**
    - NO empty/plain/gradient backgrounds
    - 3+ specific environmental objects required
    - Environmental storytelling matches genre

4. **Art Style Adherence**
    - Genre-specific techniques and aesthetics
    - Anatomy follows style rules (not default realism)
    - Banned terms strictly avoided

---

## File Structure

```
docs/image_generation/
├── README.md                          # This file - overview
├── 01_director_pillar.md             # Director role & function
├── 02_artist_pillar.md               # Artist role & function  
├── 03_reviewer_pillar.md             # Reviewer role & function
├── system_flow.md                    # Complete system workflow
├── best_practices.md                 # Tips and guidelines
└── troubleshooting.md                # Common issues & solutions
```

---

## Quick Start

### For Understanding the System

1. Read this README
2. Review [System Flow](./system_flow.md)
3. Deep dive into individual pillar docs

### For Making Changes

1. Identify which pillar needs modification
2. Review that pillar's documentation
3. Check [Best Practices](./best_practices.md)
4. Update code in `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/`

### For Debugging Issues

1. Check [Troubleshooting](./troubleshooting.md)
2. Review pillar-specific validation sections
3. Test with reviewer strictness levels

---

## Key Concepts

### Cinematography Extraction

The Director extracts 15 parameters across 3 tiers:

- **Tier 1 (CRITICAL):** Angle, lens, framing, placement
- **Tier 2 (IMPORTANT):** Lighting, color, environment, mood
- **Tier 3 (REFINEMENT):** DOF, atmosphere, perspective, texture, time, signature

### Translation Layer

Technical cinematography language → Visual descriptive language

- `45° low-angle` → `captured from below at ground level, looking upward`
- `20mm ultra-wide` → `dramatic exaggerated perspective with looming foreground`
- `f/1.4 shallow DOF` → `dreamy soft background blur, subject in sharp focus`

### Three-Part Prompt Structure

1. **Art Style Statement** (1 sentence) - establishes visual language
2. **Cinematography Framework** (2-3 sentences) - camera, lighting, environment
3. **Character Description** (remaining) - expressive, personality-driven, framing-aware

### Validation Scoring

- **Cinematography Score:** 0-100 (validates all 15 parameters)
- **Art Style Score:** 0-100 (validates genre rules, accent color, backgrounds)
- **Overall Readiness:** READY / NEEDS_REVIEW / CRITICAL_ISSUES

---

## Implementation Files

### Core Prompt Files

- `ImagePrompts.kt` - Director & Reviewer functions
- `SagaPrompts.kt` - Artist function (iconDescription)
- `GenrePrompts.kt` - Genre-specific art styles & validation rules

### Use Cases

- `CreateSagaIconUseCase.kt` - Saga icon generation
- `CreateCharacterImageUseCase.kt` - Character portrait generation

---

## Recent Updates

### December 16, 2025

- ✅ Enhanced accent color as "signature final touch"
- ✅ Strengthened background requirements (3+ objects mandatory)
- ✅ Updated reviewer with CRITICAL validation for both

### Previous Milestones

- Cinematography translation layer implementation
- Three-part structured output enforcement
- Framing-aware description filtering
- Personality-driven character expressiveness

---

## Contributing

When updating the system:

1. **Maintain pillar separation** - Each pillar has a distinct role
2. **Update documentation** - Keep pillar docs in sync with code
3. **Test thoroughly** - Use different genres and reference images
4. **Check validation** - Ensure reviewer catches new requirements

---

## Related Documentation

- [Genre Art Styles](../GENRES.md)
- [Token Optimization](../token_optimization_status.md)
- [Feature Ideas](../feature_ideas.md)

---

**Questions?** Review the pillar-specific documentation or check troubleshooting guide.

