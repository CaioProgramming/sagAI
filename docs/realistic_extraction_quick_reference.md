# Realistic Extraction Quick Reference

## The Problem You Described

**Your Example Image:** Close-up portrait showing face and shoulders
**AI Said:** "hands resting in lap"
**Reality:** ❌ Hands aren't even visible in this close-up!

**Issue:** AI was hallucinating body parts that weren't in frame and making assumptions rather than
being truthful.

## The Solution Applied

### 🎯 Core Principle

**ACCURACY > COMPLETENESS**  
The AI must be **brutally honest** about what it can actually see.

### 📏 Visibility Rules by Framing

| Framing                 | Visible                   | NOT Visible                              |
|-------------------------|---------------------------|------------------------------------------|
| **ECU** (Face)          | Face, head                | Shoulders, neck, everything below        |
| **CU** (Head+Shoulders) | Head, neck, shoulders     | Arms below shoulders, hands, torso, legs |
| **MCU** (Head-Chest)    | Head to chest, upper arms | Hands (unless touching face), legs, feet |
| **MS** (Head-Waist)     | Head to waist, arms       | Legs, feet (below waist)                 |
| **FS+** (Full body)     | Everything                | Fine details at distance                 |

### ✅ Realistic Examples

**Close-Up (CU) - Your Image Type:**

```
✅ CORRECT:
- Subject Orientation: Body front (shoulders square), Head front, Gaze direct
- Form & Posture: Head upright, shoulders relaxed and level, slight forward neck lean

❌ INCORRECT (before fix):
- Form & Posture: Sitting upright with hands resting in lap
  (Can't see hands or lap in a CU!)
```

**Extreme Close-Up (ECU):**

```
✅ CORRECT:
- Subject Orientation: Body axis unclear from framing, Head front, Gaze direct
- Form & Posture: Face fills frame, head upright with neutral tilt

❌ INCORRECT:
- Subject Orientation: Body front, Head front, Gaze direct
  (Can't determine body axis without seeing shoulders!)
```

**Medium Shot (MS):**

```
✅ CORRECT:
- Subject Orientation: Body 3/4 right, Head front, Gaze averted
- Form & Posture: Torso turned right, arms crossed, slight forward lean

❌ INCORRECT:
- Form & Posture: Standing with weight on left leg, feet apart
  (Legs/feet not visible in MS!)
```

### 🔧 What Changed

1. **Added "ABSOLUTE REALISM PRINCIPLE" header** - Makes it crystal clear the AI must be honest

2. **Parameter 16 (Subject Orientation)** - Now allows "Body axis unclear from framing" when
   shoulders aren't visible

3. **Parameter 17 (Form & Posture)** - Now has strict framing-based rules:
    - CU: Only describe head/neck/shoulders
    - MS: Can add torso/arms, NOT legs
    - FS: Full body OK

4. **Mandatory Visibility Analysis** - AI must check what's visible BEFORE describing posture

5. **Reviewer Catches It** - New violation type: UNREALISTIC_POSTURE_EXTRACTION

### 📝 Key Phrases Added to Prompts

- "DO NOT HALLUCINATE"
- "Be brutally honest about what is actually visible"
- "If you can't see hands → DO NOT describe hand position"
- "ACCURACY > COMPLETENESS"
- "Better to say 'not visible' than to guess incorrectly"

### 🎬 Subject Orientation Now Has 3 Components

All three must be analyzed separately:

1. **BODY AXIS**: Shoulder/torso direction
    - Can be "unclear from framing" if shoulders not visible

2. **HEAD DIRECTION**: Where face is pointing
    - Front / 3/4 left/right / Profile / Over-shoulder

3. **GAZE**: Eye contact direction
    - Direct / Away left/right / Up/Down / Averted

**Example for your image (front-facing portrait):**

```
Body front (shoulders visible and parallel to camera),
Head front (face symmetrical),
Gaze direct to camera
```

## Testing It

Next time you use the extraction:

1. Look at your reference image
2. Check the **FRAMING** code the AI returns (ECU/CU/MCU/MS/FS)
3. Check **FORM & POSTURE** - does it only describe visible body parts?
4. If CU but mentions hands/legs → **FAIL** (this is fixed now)

## Files Changed

- `app/src/main/java/com/ilustris/sagai/core/ai/prompts/ImagePrompts.kt`
    - `extractComposition()` - the extraction prompt
    - `reviewImagePrompt()` - the reviewer validation

## Why This Matters

- More accurate art generation (prompts match reality)
- No more invented details confusing the image generator
- Better subject orientation matching
- Truthful about framing limitations = better results

