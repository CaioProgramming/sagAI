# Realistic Image Extraction Enhancement

## Problem Identified

The AI extraction was **hallucinating details** that weren't visible in reference images:

- Describing "hands in lap" when hands weren't visible in close-up shots
- Describing "standing" postures in extreme close-ups showing only faces
- Guessing body orientation when shoulders weren't visible
- Filling in parameters with assumed information rather than honest analysis

## Example Issue

**Reference Image:** Close-up headshot (CU: Head and Shoulders)  
**Unrealistic Extraction Output:**

```
17. FORM & POSTURE: Sitting upright with shoulders relaxed, slight forward lean, hands resting in lap
```

**Problem:** Hands aren't visible in this framing, but the AI invented their position anyway.

## Solution: Strict Realism Enforcement

### 1. **Absolute Realism Principle (Added to Top of Extraction Prompt)**

```
⚠️⚠️⚠️ ABSOLUTE REALISM PRINCIPLE (NON-NEGOTIABLE) ⚠️⚠️⚠️
YOU MUST BE BRUTALLY HONEST ABOUT WHAT IS ACTUALLY VISIBLE IN THE IMAGE.
DO NOT HALLUCINATE, ASSUME, OR INVENT DETAILS THAT AREN'T CLEARLY PRESENT.
- If you can't see hands → DO NOT describe hand position
- If you can't see legs → DO NOT describe stance or weight distribution
- If shoulders are barely visible → State 'Body axis unclear from framing'
- If body parts are obscured by framing → Acknowledge it explicitly
ACCURACY > COMPLETENESS. Be truthful about framing limitations.
```

### 2. **Enhanced Parameter 16 (Subject Orientation)**

Now includes explicit handling for when body parts aren't visible:

**Before:**

```
BODY AXIS: [Front-facing / 3/4 turn left/right / Side profile / Back-facing]
```

**After:**

```
BODY AXIS: [Front-facing / 3/4 turn left/right / Side profile / Back-facing / OBSCURED]
⚠️ REALISM RULE: If framing is ECU/CU and shoulders are barely/not visible, 
state 'Body axis unclear from framing' for BODY AXIS component.
```

### 3. **Enhanced Parameter 17 (Form & Posture)**

Now explicitly limited by framing level:

**Before:**

```
Complete physical stance description: stance, weight distribution, 
body shape, limb positions...
```

**After:**

```
ONLY DESCRIBE WHAT IS ACTUALLY VISIBLE

⚠️ CRITICAL REALISM RULE:
• ECU/CU (Head-Shoulders): Describe ONLY head position, neck angle, 
  shoulder position. DO NOT mention hands, arms below shoulders, or legs.
• MCU/MS (Head-Chest/Waist): Describe head, neck, shoulders, arms, 
  torso lean. DO NOT mention legs or feet.
• MWS/FS/WS (Knees and beyond): Full stance description including legs.

⚠️ DO NOT HALLUCINATE: If you cannot see a body part, DO NOT describe it.

GOOD: 'Head tilted slightly right, shoulders relaxed and parallel to camera'
BAD: 'Standing upright with hands in lap' (when hands aren't visible)
```

### 4. **Enhanced Visibility Analysis Section**

Added mandatory pre-analysis step:

```
VISIBILITY ANALYSIS (MANDATORY BEFORE EXTRACTING PARAMETERS 16 & 17):
⚠️ CRITICAL: Analyze the image HONESTLY. DO NOT assume or hallucinate.

Classify: 1.HEAD 2.FACE 3.NECK 4.SHOULDERS 5.TORSO 6.ARMS/HANDS 
         7.LEGS/FEET 8.BODY LANGUAGE 9.ENVIRONMENT

⚠️ HONESTY PRINCIPLE:
- If shoulders barely visible → BODY AXIS = 'unclear from framing'
- If hands not visible → DO NOT describe hand position
- If legs not visible → DO NOT describe stance
- BE TRUTHFUL: Better to say 'not visible' than guess incorrectly
```

### 5. **Subject Positioning Analysis Rewrite**

Completely restructured to emphasize framing-first analysis:

```
⚠️ FIRST: Determine the FRAMING CODE - dictates what's visible
⚠️ SECOND: Analyze ONLY visible body parts at this framing level

1. BODY AXIS (Skip if body not sufficiently visible)
   - REQUIRES: At least partial shoulder visibility
   - ⚠️ If ECU/tight CU → Use 'Body axis unclear from framing'

3. STANCE & POSTURE (ONLY for visible portions)
   ⚠️ FRAMING GATES:
   • ECU/CU: Head angle, neck, shoulders ONLY
   • MCU/MS: Add torso lean, arm positions if visible
   • MWS+: Add leg stance, weight distribution

   Examples:
   • CU realistic: 'Head upright, shoulders relaxed and level'
   • CU unrealistic: 'Standing with hands in lap' ← WRONG!
```

### 6. **Realistic Example Analysis by Framing Level**

```
- ECU: 'Body axis unclear from framing, Head front, Gaze direct' 
       + 'Face fills frame, head upright'

- CU:  'Body front (shoulders square), Head front, Gaze direct' 
       + 'Head upright, shoulders relaxed and level'

- MS:  'Body 3/4 left, Head front, Gaze averted down' 
       + 'Torso turned left, arms at sides, slight slouch'

- FS:  'Body front, Head 3/4 left, Gaze away right' 
       + 'Standing with weight on right leg, arms crossed'
```

### 7. **Reviewer Enhancement**

Added validation to catch extraction hallucinations:

```
FORM & POSTURE REALISM (CRITICAL):
• ECU/CU: Prompt mentioning hands/legs/stance = VISIBILITY_VIOLATION
• MCU/MS: Prompt mentioning legs/feet = VISIBILITY_VIOLATION
• Critical: If visual direction says 'Lower body not visible' but 
  prompt mentions legs = AUTOMATIC VIOLATION

New violation type: UNREALISTIC_POSTURE_EXTRACTION
- Detects when extraction hallucinated invisible body parts
```

```
REALISM CHECK (CRITICAL): Cross-reference FRAMING with FORM & POSTURE.
If tight framing (CU) but describes 'hands in lap' or 'standing' 
= EXTRACTION HALLUCINATION → must correct
```

## Expected Results

### Before This Update

**Reference:** Close-up portrait (face + shoulders visible)

**Extraction output:**

```
16. SUBJECT_ORIENTATION: Body front, Head front, Gaze direct
17. FORM & POSTURE: Sitting upright with shoulders relaxed, 
    slight forward lean, hands resting in lap
```

❌ Hallucinated "hands in lap" - not visible in frame

### After This Update

**Reference:** Same close-up portrait

**Extraction output:**

```
16. SUBJECT_ORIENTATION: Body front (shoulders square and parallel), 
    Head front, Gaze direct
17. FORM & POSTURE: Head upright with neutral tilt, shoulders relaxed 
    and level, neck in natural vertical alignment
```

✅ Accurate - only describes visible elements

## Key Principles

1. **ACCURACY > COMPLETENESS**: Better to acknowledge limitations than invent details
2. **FRAMING DETERMINES VISIBILITY**: Always check framing code before describing body parts
3. **HONESTY OVER ASSUMPTIONS**: If you can't see it, don't describe it
4. **EXPLICIT ACKNOWLEDGMENT**: Use phrases like "Body axis unclear from framing" when appropriate

## Testing Recommendations

Test with these framing scenarios:

- **ECU (Extreme Close-Up)**: Face only - should NOT describe shoulders/body
- **CU (Close-Up)**: Head + shoulders - should NOT describe hands/arms/legs
- **MCU (Medium Close-Up)**: Head to chest - should NOT describe legs/feet
- **MS (Medium Shot)**: Head to waist - can describe arms, NOT legs
- **FS (Full Shot)**: Full body - all descriptions valid

## Related Files Modified

- `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/ImagePrompts.kt`
    - `extractComposition()` function - extraction prompt
    - `reviewImagePrompt()` function - reviewer validation

## Impact

- ✅ Eliminates hallucination of invisible body parts
- ✅ More accurate subject orientation detection
- ✅ Better framing-appropriate posture descriptions
- ✅ Reviewer catches extraction errors
- ✅ More truthful about framing limitations
- ✅ Improved generated image accuracy by ensuring prompts match actual visual references

