# Subject Orientation Detection - Test Plan

**Date:** January 11, 2026  
**Model Under Test:** Gemma-3-12B-IT  
**Purpose:** Diagnose why back-facing body orientations are being misidentified as front-facing

---

## 🧪 TEST METHODOLOGY

### Test Case Structure

Each test image should be analyzed with the updated prompt and results compared against ground
truth.

### Success Criteria

- **Target Accuracy:** ≥ 90% for body axis detection
- **Critical Failure Rate:** < 5% for back-facing misidentified as front-facing
- **Response Time:** < 10 seconds per analysis

---

## 📋 TEST CASES

### Category 1: BACK-FACING PORTRAITS (Critical Cases)

These are the cases currently failing.

#### Test 1.1: Back-Facing with Head Turn

**Description:** Subject's back to camera, head turned to look over shoulder  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body back-facing, Head over-shoulder-left, Gaze direct
```

**Visual Cues Present:**

- ✓ Back of shoulders visible
- ✓ Back of neck visible
- ✗ Front of chest NOT visible
- ✓ One shoulder prominent (turning shoulder)

**Pass Criteria:** Body axis must be "back-facing" or "back-with-head-turn"

---

#### Test 1.2: Full Back View

**Description:** Subject completely turned away, no head turn  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body back-facing, Head back, Gaze away
```

**Visual Cues Present:**

- ✓ Back of shoulders visible
- ✓ Back of neck visible
- ✗ Front of chest NOT visible
- ✗ Face not visible

**Pass Criteria:** Body axis must be "back-facing"

---

#### Test 1.3: Back-Facing Close-Up (User's Reported Case)

**Description:** CU framing, back of shoulders/neck visible, face turned  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body back-facing, Head 3/4 left, Gaze averted
```

**Visual Cues Present:**

- ✓ Back of shoulders visible (even in CU)
- ✓ Back/side of neck visible
- ✗ Front of chest NOT visible
- ✓ Face partially visible (3/4 view)

**Pass Criteria:** Body axis must be "back-facing", NOT "front"

---

### Category 2: FRONT-FACING PORTRAITS (Control Group)

These should continue working correctly.

#### Test 2.1: Standard Front Portrait

**Description:** Subject facing camera directly  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body front, Head front, Gaze direct
```

**Visual Cues Present:**

- ✗ Back of shoulders NOT visible
- ✓ Front of chest visible
- ✓ Both shoulders square and forward
- ✓ Face front

**Pass Criteria:** Body axis must be "front-facing"

---

#### Test 2.2: Front with Head Turn

**Description:** Body facing camera, head turned to side  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body front, Head 3/4 right, Gaze away
```

**Visual Cues Present:**

- ✗ Back of shoulders NOT visible
- ✓ Front of chest visible
- ✓ Both shoulders visible and forward-facing
- ✓ Face turned to side

**Pass Criteria:** Body axis must be "front-facing"

---

### Category 3: 3/4 TURN PORTRAITS

These test the model's ability to detect rotation.

#### Test 3.1: 3/4 Turn Right

**Description:** Body rotated ~45° to the right  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body 3/4 right, Head front, Gaze direct
```

**Visual Cues Present:**

- ✗ Back not prominently visible
- ✓ Front of chest partially visible
- ✓ One shoulder MORE prominent
- ✓ Body clearly rotated

**Pass Criteria:** Body axis must be "3/4 turn right"

---

#### Test 3.2: 3/4 Turn Left

**Description:** Body rotated ~45° to the left  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body 3/4 left, Head 3/4 left, Gaze direct
```

**Visual Cues Present:**

- ✗ Back not prominently visible
- ✓ Front of chest partially visible
- ✓ One shoulder MORE prominent
- ✓ Body clearly rotated

**Pass Criteria:** Body axis must be "3/4 turn left"

---

### Category 4: SIDE PROFILES

Test lateral views.

#### Test 4.1: Pure Side Profile

**Description:** Subject in perfect side view  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body side profile right, Head profile right, Gaze away
```

**Visual Cues Present:**

- ✗ Back not visible
- ✗ Front not visible
- ✓ Single shoulder visible
- ✓ Side of torso visible

**Pass Criteria:** Body axis must be "side profile"

---

### Category 5: EDGE CASES

Test ambiguous or challenging scenarios.

#### Test 5.1: Extreme Close-Up (Face Only)

**Description:** ECU framing with zero shoulder visibility  
**Expected Output:**

```
SUBJECT_ORIENTATION: Body axis obscured, Head front, Gaze direct
```

**Visual Cues Present:**

- ✗ No shoulders visible
- ✗ No torso visible
- ✓ Face only
- ✓ Framing is ECU

**Pass Criteria:** Body axis must be "OBSCURED" (not a guess)

---

## 🔧 TESTING PROCEDURE

### Step 1: Prepare Test Images

Collect 10 images (3 back-facing, 2 front-facing, 2 3/4 turn, 2 side, 1 ECU)

### Step 2: Run Analysis

For each image:

```kotlin
val result = extractComposition(testBitmap).getSuccess()
Log.d("OrientationTest", result)
```

### Step 3: Extract Body Axis

Parse the SUBJECT_ORIENTATION line:

```kotlin
val orientationLine = result.lines()
    .find { it.contains("SUBJECT_ORIENTATION") }
    
val bodyAxis = orientationLine
    ?.substringAfter("Body ")
    ?.substringBefore(",")
    ?.trim()
```

### Step 4: Compare Against Expected

```kotlin
val testCase = TestCase(
    name = "Test 1.3: Back-Facing Close-Up",
    expectedBodyAxis = "back-facing",
    actualBodyAxis = bodyAxis ?: "MISSING",
    passed = bodyAxis?.contains("back", ignoreCase = true) == true
)
```

### Step 5: Calculate Metrics

```kotlin
val totalTests = testCases.size
val passed = testCases.count { it.passed }
val accuracy = (passed.toFloat() / totalTests) * 100

val backFacingTests = testCases.filter { it.category == "BACK-FACING" }
val backFacingErrors = backFacingTests.count { 
    it.actualBodyAxis.contains("front", ignoreCase = true) 
}
val criticalErrorRate = (backFacingErrors.toFloat() / backFacingTests.size) * 100
```

---

## 📊 EXPECTED RESULTS

### Before Prompt Update (Baseline)

- Overall Accuracy: ~40-60%
- Critical Error Rate (back→front): ~60-80%
- Back-facing detection: **FAILING**

### After Prompt Update (Target)

- Overall Accuracy: ≥ 90%
- Critical Error Rate (back→front): < 5%
- Back-facing detection: **WORKING**

---

## 🔍 DIAGNOSTIC OUTPUTS

### If Still Failing After Update

Log the following for each failed case:

```kotlin
Log.e("OrientationTest", """
    ❌ FAILED: ${testCase.name}
    Expected: ${testCase.expectedBodyAxis}
    Actual: ${testCase.actualBodyAxis}
    
    Full Extraction:
    ${fullExtractionOutput}
    
    Model: ${modelName}
    Temperature: 0.0
    Tokens Used: ${tokenCount}
""")
```

### Questions to Answer

1. **Does the model output the checklist results?**
    - If NO → Model is ignoring the checklist prompt
    - If YES but still wrong → Model cannot perceive the visual difference

2. **Are back-facing cases marked with back visibility cues?**
    - If NO → Perception problem (needs model upgrade)
    - If YES but still wrong → Logic problem (needs prompt refinement)

3. **Is there a pattern to failures?**
    - Only close-ups? → Framing-dependent perception issue
    - All back-facing? → Statistical bias problem
    - Random? → General instability

---

## 🎯 DECISION TREE

```
Run Test Suite
    │
    ├─→ Accuracy ≥ 90%? → ✅ SUCCESS - Deploy
    │
    └─→ Accuracy < 90%?
        │
        ├─→ Back-facing detection improved by > 50%?
        │   └─→ YES: Prompt is helping but insufficient
        │       └─→ ACTION: Try chain-of-thought reasoning (Option 2)
        │
        └─→ Back-facing detection NOT improved?
            └─→ Model cannot perceive the difference
                └─→ ACTION: Upgrade to Gemma-3-27B-IT (Option 3)
```

---

## 📝 TEST DATA TEMPLATE

```json
{
  "testSuite": "Subject Orientation Detection",
  "date": "2026-01-11",
  "model": "gemma-3-12b-it",
  "promptVersion": "v2_checklist",
  "results": [
    {
      "testId": "1.3",
      "name": "Back-Facing Close-Up",
      "category": "BACK-FACING",
      "expected": "back-facing",
      "actual": "front",
      "passed": false,
      "visualCuesLogged": {
        "backOfShouldersVisible": true,
        "frontOfChestVisible": false,
        "bothShouldersFacingForward": false,
        "oneShoulderProminent": true
      },
      "fullOutput": "SUBJECT_ORIENTATION: Body front, Head 3/4 left, Gaze averted",
      "notes": "Model still defaulting to front despite back shoulder visibility"
    }
  ],
  "summary": {
    "totalTests": 10,
    "passed": 6,
    "failed": 4,
    "accuracy": 60,
    "criticalErrors": 3,
    "criticalErrorRate": 75
  }
}
```

---

## 🚨 CRITICAL SUCCESS METRIC

**The ONE metric that matters most:**

```
Back-facing body orientations must NEVER be extracted as "front-facing"
```

If this single metric fails, the entire system fails because:

- Wrong orientation = wrong final image
- Defeats the purpose of reference-based generation
- Cannot be fixed by downstream reviewer (garbage in = garbage out)

**Target:** 0% back→front misidentification
**Acceptable:** < 5% back→front misidentification  
**Current:** ~80-100% back→front misidentification ❌

---

## 📞 NEXT STEPS BASED ON RESULTS

### If Prompt Update Works (≥ 90% accuracy)

1. ✅ Document the fix
2. ✅ Update the body orientation visual guide
3. ✅ Add regression tests
4. ✅ Monitor production logs for 1 week

### If Prompt Update Partially Works (60-89% accuracy)

1. 🔄 Implement Option 2 (Chain-of-Thought)
2. 🔄 Retest with reasoning outputs
3. 🔄 If still < 90%, proceed to model upgrade

### If Prompt Update Doesn't Work (< 60% accuracy)

1. ⚠️ Model cannot perceive body orientation reliably
2. ⚠️ Upgrade to Gemma-3-27B-IT immediately
3. ⚠️ Retest and compare
4. ⚠️ If 27B also fails, escalate to Gemini Pro Vision

---

## 💡 HYPOTHESIS

**Current Hypothesis:**
Gemma-3-12B-IT has difficulty with:

- Fine-grained shoulder/back differentiation in close-up frames
- Overriding statistical bias toward "front-facing" default
- Maintaining attention on orientation when processing 16+ parameters

**The checklist prompt should:**

- ✓ Force sequential attention to body cues
- ✓ Prevent early conclusion jumping
- ✓ Provide explicit decision rules

**If this doesn't work:**

- Model's visual encoder is insufficient for this task
- Need stronger model or specialized approach

