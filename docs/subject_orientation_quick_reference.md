# Subject Orientation Quick Reference Guide

## Understanding the Three-Component System

### Component 1: BODY AXIS

**What it measures:** The direction the torso/shoulders are facing relative to the camera plane.

#### Visual Guide:

```
Front-facing:          3/4 Turn Right:        Profile Right:        Back-facing:
     📷                     📷                     📷                    📷
     |                      |                      |                     |
    👤                     👤                     👤                    👤
 ┌──┼──┐              ┌──╱                   ──┐                    ──┼──
 │     │              │  ╱                     │                       │
Both shoulders     Right shoulder          One shoulder            Back of
equally visible    more prominent          visible only           shoulders

Shoulders          Shoulders               Shoulders              Shoulders
parallel           ~45° rotated            90° rotated           180° rotated
```

**How to identify:**

- Look at the **shoulder line**
- Check how much of each shoulder is visible
- Determine torso rotation angle from camera

---

### Component 2: HEAD DIRECTION

**What it measures:** Where the face/head is pointing (independent from body).

#### Visual Guide:

```
Front:                 3/4 Left:              Profile Left:         Over-shoulder:
  👀                     👁️                       👁️                     👁️
 👃👃                    👃                        👃                    ╱ 👃
  👄                     👄                        👄                   ╱   👄

Both eyes           Both eyes visible      One eye visible       Head turned
Full nose           One more prominent     Nose silhouette       back over
Symmetrical         Nose blocks far side   One face side         shoulder
```

**How to identify:**

- Count how many eyes are visible
- Check nose position and visibility
- Determine face symmetry

---

### Component 3: GAZE

**What it measures:** Where the eyes are looking.

#### Visual Guide:

```
Direct-to-camera:    Looking Left:         Looking Down:         Averted:
     👀                  👀                    👀                    👀
    ↓↓                 ←←                    ↙↙                   ↗↗
    📷                  🚪                    📱                    🌤️
 
Eye contact        Eyes directed         Eyes looking          Eyes looking
with viewer        to subject's left     downward              away from focus
```

**How to identify:**

- Follow the **pupil direction**
- Check if irises are centered or shifted
- Determine what the subject appears to be looking at

---

## Complete Analysis Examples

### Example 1: Classic Portrait

**Reference Image:** Person standing, body straight forward, face slightly turned, looking at camera

**Analysis:**

```
BODY AXIS: Front-facing
HEAD DIRECTION: 3/4 left
GAZE: Direct-to-camera

FORM & POSTURE: Standing upright with weight centered, arms relaxed at sides, 
shoulders square to camera
```

**Prompt Translation:**
"A character standing with their body facing forward, shoulders parallel to the camera. Their head
is turned slightly to the left, creating a 3/4 view of their face, while their eyes meet the viewer
directly."

---

### Example 2: Dynamic Action Pose

**Reference Image:** Person in motion, torso twisted, looking back over shoulder

**Analysis:**

```
BODY AXIS: 3/4 turn right
HEAD DIRECTION: Over-shoulder left
GAZE: Direct-to-camera

FORM & POSTURE: Standing with weight on left leg, right leg stepping forward, 
torso twisted right, left arm extended, right arm bent at elbow
```

**Prompt Translation:**
"A character in dynamic motion, their torso rotated 3/4 to the right with weight on their left leg.
Their head turns back over their left shoulder, eyes meeting the viewer. Left arm extended forward,
right arm bent, suggesting movement."

---

### Example 3: Contemplative Seated

**Reference Image:** Person sitting, body angled away, looking down

**Analysis:**

```
BODY AXIS: 3/4 turn left
HEAD DIRECTION: Front
GAZE: Looking down

FORM & POSTURE: Sitting with torso leaning forward, elbows on knees, 
hands clasped together, shoulders slouched
```

**Prompt Translation:**
"A character sitting with their body turned 3/4 to the left, torso leaning forward with elbows
resting on their knees. Their face points forward but eyes gaze downward contemplatively, hands
clasped together."

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Confusing Body and Head

```
WRONG: "Body and head facing 3/4 right"
RIGHT: "Body 3/4 right, Head front, Gaze direct"
```

**Why:** Body and head can be positioned independently!

---

### ❌ Mistake 2: Forgetting Gaze

```
WRONG: "Body front, Head 3/4 left"
RIGHT: "Body front, Head 3/4 left, Gaze averted right"
```

**Why:** Where the face points ≠ where the eyes look!

---

### ❌ Mistake 3: Incomplete Posture

```
WRONG: "Standing"
RIGHT: "Standing with weight on right leg, left knee slightly bent, 
        arms crossed at chest, shoulders relaxed"
```

**Why:** Need complete stance description for accurate replication!

---

### ❌ Mistake 4: Vague Descriptions

```
WRONG: "Turned to the side"
RIGHT: "Body profile right (90° rotation), Head over-shoulder, Gaze direct"
```

**Why:** Precision ensures accurate image generation!

---

## Direction Reference (Screen-Relative)

```
        LEFT                    RIGHT
         ←                        →
         
         📷 CAMERA VIEW
         
    ┌────────────────────┐
    │                    │
    │    👤 SUBJECT     │
    │                    │
    └────────────────────┘
```

**"Left" means:** Subject facing toward the left side of the screen
**"Right" means:** Subject facing toward the right side of the screen

---

## Testing Checklist

When analyzing a reference image, verify:

- [ ] Body axis identified (shoulder line angle)
- [ ] Head direction separate from body (face angle)
- [ ] Gaze direction noted (pupil/iris direction)
- [ ] Stance type specified (standing/sitting/etc.)
- [ ] Weight distribution described
- [ ] Spine position noted (straight/curved/twisted)
- [ ] Arm positions detailed
- [ ] Leg positions detailed

**If all checked → Complete analysis achieved! ✅**

---

## Implementation Notes

This three-component system ensures:

1. **Precision**: Each aspect of positioning tracked separately
2. **Flexibility**: Body, head, and gaze can differ
3. **Completeness**: Full pose captured including stance and limbs
4. **Clarity**: Unambiguous instructions for image generation
5. **Consistency**: Systematic approach reduces interpretation errors

