# Body Orientation Visual Guide

## 🎯 Understanding Body Axis Directions

This guide helps you understand how to identify body orientation in reference images.

---

## 📐 Body Axis Types

### 1. FRONT-FACING

```
     👤
    /||\    ← Both shoulders visible equally
   / || \   ← Chest/front of torso visible
     ||     ← Shoulders parallel to camera
    /  \
```

**Indicators:**

- Both shoulders equally visible
- Chest or front of torso visible
- Body "square" to camera
- Symmetrical shoulder line

**Extract as:** `Body front` or `Body front-facing`

---

### 2. 3/4 TURN (Left or Right)

```
   Left shoulder    Right shoulder
   more visible     less visible
        👤          
       /|\          ← Body rotated ~45°
      / | \         ← One shoulder forward
        |
       / \
```

**Indicators:**

- One shoulder more prominent than the other
- Body rotated approximately 45° from camera
- Partial side of torso visible
- Asymmetrical shoulder line

**Extract as:** `Body 3/4 left` or `Body 3/4 right`

- **Left:** Right shoulder more forward
- **Right:** Left shoulder more forward

---

### 3. SIDE PROFILE (Left or Right)

```
       👤
       |\           ← Only one shoulder visible
       | \          ← Body perpendicular (90°)
       |            ← Pure side view
       |
      / \
```

**Indicators:**

- Only one shoulder visible
- Body perpendicular to camera (90°)
- Pure side silhouette
- Single shoulder line

**Extract as:** `Body side profile left` or `Body side profile right`

---

### 4. BACK-FACING

```
       ⚫ (head)
      /||\          ← Back of shoulders visible
     / || \         ← Back of neck visible
       ||           ← Rear view of torso
      /  \
```

**Indicators:**

- Back of shoulders visible
- Back of neck visible
- Rear view of torso/back
- No chest visible

**Extract as:** `Body back-facing`

---

### 5. BACK-WITH-HEAD-TURN ⭐ (Dynamic Pose)

```
       😊 (face visible)
      /||\          ← Back of shoulders visible
     / || \         ← Head turned over shoulder
       ||           ← Body facing away
      /  \
    
    CREATES VISUAL TENSION!
```

**Indicators:**

- Back of shoulders/torso visible (body turned away)
- BUT head is turned back over shoulder
- Face visible despite back-facing body
- Creates dynamic composition

**Extract as:** `Body back-with-head-turn` or `Body back-facing, Head over-shoulder-left`

**This is CRITICAL for visual diversity!**

---

### 6. OBSCURED (Use sparingly!)

```
       😊
     (ECU - Extreme Close-Up)
     Face fills entire frame
     NO shoulders visible
```

**Indicators:**

- ECU (Extreme Close-Up) framing
- Face fills entire frame
- ZERO shoulder/neck visibility
- Only facial features visible

**Extract as:** `Body axis obscured from framing`

**⚠️ IMPORTANT:** Only use this for true ECU frames!

---

## 🔍 How to Analyze: Step-by-Step

### Step 1: Identify Framing

What parts of the body are visible?

- ECU: Face only
- CU: Head + Shoulders
- MCU: Head + Chest
- MS: Head + Waist
- FS: Full body

### Step 2: Look for Shoulder Cues

Are shoulders visible?

- ✅ YES → Analyze shoulder line direction
- ❌ NO → Check if truly ECU (face-only)

### Step 3: Identify Body Direction

**Front indicators:**

- Both shoulders equal
- Chest visible
- Symmetrical

**3/4 turn indicators:**

- One shoulder forward
- Partial side visible
- ~45° rotation

**Back indicators:**

- Shoulder backs visible
- Neck back visible
- Rear view

**Head turn dynamic:**

- Body away BUT head turned back
- Over-shoulder look

### Step 4: Be Specific!

Don't default to "unclear" unless truly ECU with zero shoulder visibility.

---

## 📋 Quick Decision Tree

```
Can you see shoulders/neck/back?
│
├─ NO → Is it ECU (face-only extreme close-up)?
│      ├─ YES → "Body axis obscured from framing"
│      └─ NO → Look harder! Analyze visible cues
│
└─ YES → What do you see?
       │
       ├─ Both shoulders equal + chest → "Body front-facing"
       │
       ├─ One shoulder forward → "Body 3/4 [direction]"
       │
       ├─ One shoulder only (side) → "Body side profile [direction]"
       │
       ├─ Back of shoulders + back of neck → "Body back-facing"
       │
       └─ Back visible BUT head turned over shoulder → "Body back-with-head-turn"
          (or "Body back-facing, Head over-shoulder-[left/right]")
```

---

## 🎨 Real Examples

### Example 1: Your Reference Image

**What's visible:**

- Back of neck and shoulders (back facing)
- Head turned over left shoulder
- Face looking at camera

**Analysis:**

- ✅ Shoulders visible (NOT obscured)
- ✅ Back visible (body turned away)
- ✅ Head turned over shoulder (dynamic)

**Extract:**

```
16. SUBJECT_ORIENTATION: Body back-facing, Head over-shoulder-left, Gaze direct
```

**NOT:** ❌ "Body axis unclear from framing"

---

### Example 2: Standard Portrait

**What's visible:**

- Both shoulders equally
- Chest/front visible
- Face front

**Analysis:**

- ✅ Both shoulders equal
- ✅ Chest visible
- ✅ Symmetrical

**Extract:**

```
16. SUBJECT_ORIENTATION: Body front (shoulders square), Head front, Gaze direct
```

---

### Example 3: Dynamic 3/4

**What's visible:**

- Right shoulder more forward
- Left shoulder back
- Face turned front despite body angle

**Analysis:**

- ✅ Asymmetrical shoulders
- ✅ Body rotated ~45°
- ✅ Head direction differs from body

**Extract:**

```
16. SUBJECT_ORIENTATION: Body 3/4 left, Head front, Gaze direct
```

---

### Example 4: True ECU (Rare)

**What's visible:**

- Eyes, nose, mouth only
- Face fills 100% of frame
- No neck, no shoulders, nothing below

**Analysis:**

- ❌ NO shoulders visible
- ❌ NO neck visible
- ✅ True extreme close-up

**Extract:**

```
16. SUBJECT_ORIENTATION: Body axis obscured from framing, Head front, Gaze direct
```

**This is the ONLY acceptable use of "obscured"!**

---

## ✅ Validation Checklist

Before marking body axis as "obscured":

- [ ] Is the framing truly ECU (extreme close-up)?
- [ ] Can I see ANY part of shoulders? (If yes → NOT obscured)
- [ ] Can I see ANY part of neck? (If yes → analyze neck/back position)
- [ ] Can I see ANY part of torso/back? (If yes → NOT obscured)
- [ ] Is the frame 100% face only? (If no → NOT obscured)

**If ANY of the above are visible → DETERMINE body direction!**

---

## 🚨 Common Mistakes

### ❌ Mistake 1: Defaulting to "unclear"

**Scenario:** CU frame showing back of shoulders and neck

**Wrong:** "Body axis unclear from framing"  
**Right:** "Body back-facing"

**Why:** You CAN see shoulders/neck → body direction is determinable!

---

### ❌ Mistake 2: Missing dynamic poses

**Scenario:** Body turned away, head looking back over shoulder

**Wrong:** "Body front-facing" or "Body axis unclear"  
**Right:** "Body back-facing, Head over-shoulder-left"

**Why:** This is a SPECIFIC dynamic pose that creates visual interest!

---

### ❌ Mistake 3: Generic over-shoulder

**Scenario:** Head turned over right shoulder

**Wrong:** "Head over-shoulder"  
**Right:** "Head over-shoulder-right"

**Why:** Direction matters for accurate reproduction!

---

## 🎯 Key Principles

1. **Be Assertive:** Analyze visible cues, don't default to "unclear"
2. **Observe Shoulders:** They're the key to body direction
3. **Note the Back:** Back visibility = back-facing, not unclear!
4. **Capture Dynamics:** Over-shoulder poses create visual tension
5. **Reserve "Obscured":** Only for true ECU (face-only extreme close-ups)

---

## 📊 Impact on Visual Diversity

### Without accurate body orientation:

```
All portraits look like this:
👤 👤 👤 👤 👤
(all front-facing, repetitive)
```

### With accurate body orientation:

```
Varied, dynamic compositions:
👤 🙃 👤 🤸 👥
(front, back-turn, 3/4, profile, over-shoulder)
```

**Result:** More interesting, diverse artwork! 🎨

---

**Remember:** Body language is as important as facial features for creating compelling, diverse
compositions!

