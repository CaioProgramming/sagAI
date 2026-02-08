# Cyberpunk Heavy Augmentation Enhancement

**Date:** January 13, 2026 (Updated)  
**Focus:** MANDATORY heavy cyberware enforcement - subtle augmentations are now VIOLATIONS

## Status: ENHANCED v2 - Mandatory Enforcement

## Overview

The cyberpunk genre prompts have been significantly enhanced to feature **heavy, extensive cyberware
** that fundamentally transforms characters into beings that are **MORE MACHINE than flesh**. This
is not about subtle augmentations—it's about the complete dissolution of humanity into technology.

**CRITICAL UPDATE:** The validation/reviewer system now **ENFORCES** heavy cyberware as mandatory.
Subtle augmentations like "silver scars", "enhanced vision", "circuit tattoos", or "data bracelets"
are now flagged as `INSUFFICIENT_CYBERWARE` violations and auto-upgraded.

## Problem Statement (v2)

The v1 implementation encouraged the AI to add cyberware but used weak language ("MAY ADD", "subtle
augmentations") which resulted in characters with:

- Only silver scars where neural ports should be
- "Enhanced vision" instead of visible mechanical eye replacements
- Glowing dermal implants instead of chrome plates
- Circuit tattoos and data bracelets instead of actual limb modifications

## Solution: Mandatory Heavy Cyberware Validation

### New Validation Checklist (Reviewer Must Verify ALL)

- □ At least 3 MAJOR cyberware elements described
- □ Descriptions are MECHANICALLY EXPLICIT (visible chrome, exposed machinery)
- □ Cyberware is HEAVY and VISIBLE, not subtle or hidden
- □ Cyberware shows WEAR (scarring, mismatched parts, grime)
- □ Viewer would IMMEDIATELY recognize character as HEAVILY AUGMENTED

**If ANY checkbox fails → CRITICAL VIOLATION: `INSUFFICIENT_CYBERWARE`**

### Cyberware Intensity Scale (Used for Validation)

| Level                        | Examples                                                                                                                                 | Status       |
|------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| **INSUFFICIENT (VIOLATION)** | silver scars, enhanced vision, circuit tattoos, data bracelets, glowing implants, subtle modifications                                   | ❌ REJECTED   |
| **MINIMUM ACCEPTABLE**       | mechanical eye with LED arrays, chrome jaw plate with servo joints, exposed neural ports with cables                                     | ✅ PASS       |
| **IDEAL HEAVY**              | complete eye replacement with multi-lens array, chrome skull plates, mechanical spine through skin, full arm replacement with hydraulics | ✅✅ EXCELLENT |

### Auto-Fix Upgrade Patterns (New in v2)

| Original (Insufficient)                     | Upgraded (Heavy)                                                                                                                                        |
|---------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| cybernetic eye implants for infrared vision | complete mechanical eye replacement with rotating multi-lens array, visible LED scanner grid, servo-motorized iris, and glowing amber targeting reticle |
| silver scars at nuca                        | chunky chrome neural port at base of skull with thick data cables, exposed junction boxes, and aggressive scarring at flesh-metal interface             |
| dermal implants on temples glowing          | chrome temple plates with exposed circuitry, blinking status LEDs, and power conduits visible through translucent synthetic skin                        |
| circuit tattoo on wrist                     | full chrome forearm replacement from elbow down with visible hydraulic pistons, mechanical finger joints, exposed power cables                          |
| data bracelet                               | integrated wrist computer with exposed components, cracked display, and data ports with jury-rigged cables                                              |

### Banned Subtle Cyberware Terms (New in v2)

These terms are now BANNED as insufficient:

- "faint scars", "silver scars" (as sole augmentation)
- "subtle implants", "hidden augmentations"
- "enhanced vision", "infrared/thermal vision" (without mechanical description)
- "data bracelet" (as sole augmentation)
- "circuit tattoo" (as sole augmentation)
- "glowing implants" (without mechanical description)

## Key Changes (Original v1)

### 1. **Art Style (artStyle function)**

Enhanced to emphasize brutal transformation and inhuman appearance:

#### Cyberware Philosophy

- **FROM:** "Cyberware bleeds into their being"
- **TO:** "Flesh and chrome are FUNDAMENTALLY MERGED. Cyberware is a REQUIREMENT for survival. The
  line between human and machine has been irrevocably ERASED."

#### Heavy Integration Options (Now Mandatory)

Added specific focus on:

- **NEURAL INTERFACES:** PROMINENT data ports with VISIBLE glowing neural pathways, HEAVY
  cyber-jacks with thick cables and junction boxes
- **OCULAR AUGMENTS (CRITICAL):** COMPLETE cybernetic eye replacement (not subtle), with mechanical
  pupils, LED arrays, scanner overlays, mechanical eyelids
- **FACIAL RECONSTRUCTION:** HEAVY synthetic facial panels, chrome plating over bone structure,
  mechanical mandibles, exposed machinery beneath translucent panels
- **LIMB AUGMENTATION:** COMPLETE chrome limb replacement (full arms/legs), NOT partial, with
  visible hydraulic systems and mechanical joints
- **SPINE & SKELETAL MODS:** NEW addition - Exposed mechanical spine, exoskeletal reinforcement,
  shoulder-mounted systems, visible chest cavity mechanics
- **COMBAT/UTILITY AUGMENTATIONS:** NEW addition - Retractable mechanical claws, shoulder-mounted
  weapon systems, integrated tools

#### Integration Principles

- Tech should look **HEAVILY INTEGRATED, BATTLE-WORN, LIVED-IN** for years
- Show **BRUTAL COST:** Aggressive scarring, necrotic tissue, infection marks, jury-rigged repairs
- Mix **HIGH-TECH and LOW-LIFE** brutally: Military-grade paired with street-clinic tech, expensive
  mixed with salvaged scrap
- Technology has **CONSUMED humanity** - Some characters are MORE MACHINE than flesh

#### Visual Intensity

- **Lighting:** Cyberware MUST catch light dramatically differently than organic tissue—gleaming,
  reflecting, glowing
- **Texture Details:** Visible hydraulic fluid, condensation on cold tech, rust on budget
  modifications, grime accumulation
- **Facial Features:** RADICALLY ALTER appearance—embrace the UNCANNY and INHUMAN
- **Aura:** Dangerous, inhuman, tech-infused. MORE MACHINE than flesh. MENACE in their presence.
- **Posing:** Aggressive, mechanical, CYBORG PRECISION. Movement like a machine with human memory.

#### Environment

Enhanced descriptions for a **HOSTILE ENVIRONMENT:**

- Cyberpunk Street: Holographic ads **flickering with decay**, vending machines **with broken
  screens**, **steam vents from underground systems**
- Corporate Interior: **Code scrolling** on walls, **surveillance feeds**, **harsh white fluorescent
  lighting**, **surveillance cameras at every angle**
- Undercity/Slums: Exposed pipes, **jury-rigged systems**, **steam and chemical vents**, **makeshift
  tech and explosives**, **illegal cyber-clinics**
- Tech Lab/Ripperdoc: **Pools of surgical fluid**, **blood mixing with coolant**, **the stench of
  burnt flesh and solder**

#### Mood & Ambience

- **FROM:** "Dystopian, melancholic, and visceral"
- **TO:** "Dystopian, BRUTAL, and visceral. The dark future is UNRELENTING—desperate, beautiful in
  its horror, utterly unforgiving."
- **Ambience:** "OPPRESSIVE TECHNOLOGICAL DOMINANCE...The world is OWNED by corporations and
  technology."

### 2. **Character Appearance (describeCharacter function)**

Transformed from optional to mandatory cyberware presence:

#### Philosophy Shift

- **FROM:** "Cyberware (ENCOURAGED): Even if not explicitly mentioned..."
- **TO:** "Cyberware (HEAVILY ENCOURAGED): Characters MUST feature VISIBLE, EXTENSIVE augmentations.
  This is NOT optional—it is the DEFINING characteristic of cyberpunk existence"

#### Mandatory Augmentation Details

Added specific implementation tiers with visual emphasis:

- ★ NEURAL SYSTEMS (stars used for visual emphasis)
- ★ OCULAR AUGMENTS: Complete, not subtle
- ★ FACIAL RECONSTRUCTION: Heavy replacement
- ★ LIMB AUGMENTATION: Full replacement, not partial
- ★ SKELETAL MODIFICATIONS (new requirement)
- ★ DERMAL TECH: Extensive, covering large sections
- ★ COMBAT AUGMENTATIONS: Integrated weapons
- ★ SENSORY ENHANCEMENTS: Heavy modifications

#### Transformation Messaging

- "INHUMAN TRANSFORMATION, desperation, adaptation, and the **complete dissolution of humanity into
  machine**"
- "Some characters are MORE MACHINE than flesh—and that should be **VISUALLY UNDENIABLE**"

### 3. **Validation Rules (validationRules function) - ENHANCED in v2**

Enhanced CYBERPUNK section to enforce:

#### Cyberware Enforcement (NOW MANDATORY)

- ~~Artist has CREATIVE LICENSE to add cyberware~~ → Artist MUST add heavy cyberware
- ~~This is ENCOURAGED creative license~~ → This is NON-NEGOTIABLE requirement
- Characters without 3+ major visible cyberware elements → CRITICAL VIOLATION

#### New Violation Type: `INSUFFICIENT_CYBERWARE`

- **Severity:** CRITICAL
- **Trigger:** Character has only subtle/invisible augmentations
- **Auto-fix:** Upgrade all subtle descriptions to heavy mechanical versions
- **Check:** Count major cyberware elements; if <3, auto-add mechanical eye, neural port, or chrome
  limb

#### Required Cyberware Element Categories

Characters must have visible augmentations from AT LEAST 3 of these:

1. **OCULAR**: Complete mechanical eye replacement with LED arrays, scanner overlays, mechanical
   iris
2. **NEURAL**: Prominent data ports with visible hardware, cables, junction boxes
3. **DERMAL/FACIAL**: Chrome plates, exposed circuitry, synthetic skin panels
4. **LIMB**: Full chrome replacement with hydraulics, mechanical joints, power cables
5. **SKELETAL/SPINE**: Exposed mechanical spine, exoskeletal reinforcement

## Implementation Files

- `GenrePrompts.kt`: Updated CYBERPUNK validation rules with mandatory cyberware enforcement,
  intensity scale, and upgrade patterns
- `ImagePrompts.kt`: Added `INSUFFICIENT_CYBERWARE` violation type and auto-fix patterns

## Testing (v2)

When testing, verify that:

1. The reviewer flags characters with only subtle cyberware as `INSUFFICIENT_CYBERWARE`
2. The auto-fix upgrades subtle descriptions to heavy mechanical versions
3. The corrected prompt contains at least 3 major visible cyberware elements
4. Silver scars → chrome ports, enhanced eyes → mechanical replacements, tattoos → limb replacements

## Visual Philosophy Summary

### Before v1

Cyberware was an enhancement, an extension of the character. Characters could be predominantly human
with some tech.

### After v1

**Cyberware IS the character.** The visual narrative is about humanity's dissolution into
technology.

### After v2

**Cyberware is MANDATORY and VALIDATED.** The reviewer actively checks for heavy cyberware and
rejects/upgrades subtle augmentations. No character escapes the dark future without visible chrome.

## Compatibility Notes

- The changes maintain the 1980s OVA anime aesthetic
- Cel-shading and hard-edged shadows remain core requirements
- Muted blue/purple color palette enforced
- Deep purple accent requirement unchanged
- All background/environment rules amplified for darker, grittier feel

## Result

Cyberpunk artwork should now feature characters that are **recognizably CYBORG**, with visible heavy
augmentation that cannot be missed. The dark future isn't a subtle aesthetic—it's an oppressive,
inescapable reality where technology has fundamentally consumed humanity.
