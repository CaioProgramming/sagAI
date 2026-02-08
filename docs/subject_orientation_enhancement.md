# Subject Orientation Enhancement

## Problem

The AI was failing to accurately capture how subjects are positioned in reference images,
particularly:

- Not distinguishing between body position and head/face direction
- Missing the complete pose/stance information
- Generating images where the subject looks in a different direction than the reference
- Not capturing the full body positioning (standing, sitting, weight distribution, etc.)

## Solution

Enhanced the subject positioning analysis system with a **three-component breakdown** to capture
complete subject positioning:

### 1. Enhanced SUBJECT_ORIENTATION (Parameter 16)

Now captures THREE separate components:

#### BODY AXIS

- The torso/shoulder line direction relative to camera
- Options: Front-facing / 3/4 turn left/right / Side profile left/right / Back-facing
- Example: "Body 3/4 right" means torso rotated ~45° to the right

#### HEAD DIRECTION

- Where the face/head is pointing (independent from body)
- Options: Front / 3/4 left/right / Profile left/right / Back / Over-shoulder
- Example: "Head front" means face looking directly at camera

#### GAZE

- Where the eyes are looking
- Options: Direct-to-camera / Looking away left/right / Looking up/down / Averted / Closed eyes
- Example: "Gaze direct" means making eye contact with viewer

**Complete Example:**

```
"Body 3/4 right, Head front, Gaze direct"
```

This describes: torso turned to the right, face looking forward, eyes meeting the viewer.

### 2. Enhanced FORM & POSTURE (Parameter 17)

Now requires complete physical stance description:

- **Stance**: standing/sitting/crouching/lying/kneeling/leaning
- **Weight Distribution**: centered/shifted, which leg bears weight
- **Body Shape**: straight/curved/twisted/bent forward or back
- **Limb Positions**:
    - Arms: crossed/at sides/raised/gesturing
    - Legs: together/apart/crossed/one bent

**Complete Example:**

```
"Standing with weight on right leg, left knee slightly bent, arms crossed confidently, shoulders square"
```

### 3. New SUBJECT POSITIONING ANALYSIS Section

Added systematic approach for the AI to analyze reference images:

#### Body Axis Determination

- Analyzes shoulder line and torso alignment
- Front-facing: Both shoulders equally visible
- 3/4 turn: One shoulder more prominent
- Profile: Only one shoulder visible
- Back-facing: Back of torso visible

#### Head Direction Analysis

- Separate analysis from body
- Front: Both eyes, full nose, symmetrical face
- 3/4: Both eyes visible but one more prominent
- Profile: One eye visible, nose silhouette
- Over-shoulder: Head turned back over shoulder

#### Stance & Posture Documentation

- Primary stance identification
- Weight distribution analysis
- Spine position
- Complete limb positioning

### 4. Enhanced Reviewer Validation

The reviewer now validates ALL THREE orientation components separately:

- Body axis must match reference
- Head direction must match reference
- Gaze must match reference

**Any mismatch in ANY component = CRITICAL VIOLATION**

### 5. Enhanced Art Composition Instructions

Updated the artComposition prompt to translate the three-component system into natural language
descriptions for the image generator.

## Benefits

1. **Precision**: Separate tracking of body, head, and gaze ensures accurate positioning
2. **Complete Pose Capture**: Form & Posture now captures full stance, weight, and limb positions
3. **Better Matching**: Generated images will more accurately match the reference's subject
   positioning
4. **Clearer Communication**: Three-component system is easier for AI to understand and execute
5. **Consistent Results**: Systematic analysis approach reduces interpretation errors

## Usage Example

### Before (Single Component)

```
SUBJECT_ORIENTATION: Front-facing
FORM & POSTURE: Leaning
```

❌ Ambiguous - doesn't specify if head is also front, gaze direction, or complete stance

### After (Three-Component System)

```
SUBJECT_ORIENTATION: Body front-facing, Head 3/4 left, Gaze direct
FORM & POSTURE: Standing with weight on right leg, left knee slightly bent, arms crossed at chest, shoulders relaxed and square to camera
```

✅ Complete - precisely describes every aspect of subject positioning

## Implementation Date

January 9, 2026

## Related Parameters

- Parameter 16: SUBJECT_ORIENTATION
- Parameter 17: FORM & POSTURE
- Parameter 18: SCALE & ZOOM (ensures framing matches)

## Next Steps

Test with various reference images to ensure:

1. Body axis is correctly identified
2. Head direction is separate from body
3. Gaze is accurately captured
4. Complete stance is documented
5. Generated images match reference positioning

