# SCALE & ZOOM Parameter Enforcement - Implementation Summary

## Date

January 9, 2026

## Problem

The AI was occasionally forgetting to return the 18th parameter (SCALE & ZOOM) in the visual
extraction phase, causing incomplete cinematography data and breaking framing precision.

## Solution

Implemented multiple layers of enforcement to make the 18th parameter absolutely mandatory and
impossible to skip.

---

## Enforcement Mechanisms Implemented

### 1. **Header Warning - Critical Directive Update**

**Location:** `extractComposition()` header

**Changes:**

- Updated parameter count from "17 parameters" → "18 parameters"
- Changed "Output ONLY the 16 parameters" → "Output ONLY the 18 parameters"
- Added explicit mandate:
  `MANDATORY: ALL 18 PARAMETERS MUST BE PRESENT. Missing even one parameter = CRITICAL FAILURE.`

**Purpose:** Sets expectations from the very beginning that 18 parameters are required.

---

### 2. **Visual Warning Marker Before Parameter 18**

**Location:** Parameter list in `extractComposition()`

**Added:**

```
⚠️ PARAMETER 18 IS MANDATORY - DO NOT SKIP:
18. SCALE & ZOOM: [...]
```

**Purpose:** Creates a visual "stop sign" that the AI must read before reaching parameter 18,
preventing it from stopping at 17.

---

### 3. **Parameter Completeness Checklist**

**Location:** End of `extractComposition()`, before final verification

**Added:**

```
PARAMETER COMPLETENESS CHECK (VERIFY BEFORE OUTPUT):
COUNT YOUR PARAMETERS - YOU MUST HAVE EXACTLY 18:
✓ 1-ANGLE ✓ 2-LENS ✓ 3-FRAMING ✓ 4-PLACEMENT ✓ 5-LIGHTING ✓ 6-COLOR
✓ 7-ENVIRONMENT ✓ 8-MOOD ✓ 9-DOF ✓ 10-ATMOSPHERE ✓ 11-PERSPECTIVE ✓ 12-TEXTURE
✓ 13-TIME ✓ 14-SIGNATURE ✓ 15-DEPTH_LAYERS ✓ 16-SUBJECT_ORIENTATION
✓ 17-FORM_&_POSTURE ✓ 18-SCALE_&_ZOOM
```

**Purpose:** Forces the AI to mentally count all parameters before outputting, ensuring none are
missed.

---

### 4. **Explicit Final Reminder**

**Location:** After the checklist

**Added:**

```
⚠️ CRITICAL: Parameter 18 (SCALE & ZOOM) is MANDATORY - it defines how much frame space the subject occupies.
This parameter is NEW and ESSENTIAL for framing precision. DO NOT SKIP IT.
If you output fewer than 18 parameters, your response is INCOMPLETE and REJECTED.
```

**Purpose:** Reinforces the critical importance of parameter 18 with consequences for omission.

---

### 5. **Updated Final Verification**

**Location:** Last line of `extractComposition()`

**Changed from:**

```
FINAL VERIFICATION: Is the output 100% technical? Is it devoid of pleasantries? If yes, output now.
```

**To:**

```
FINAL VERIFICATION: Is the output 100% technical? Is it devoid of pleasantries? Do you have ALL 18 parameters? If yes, output now.
```

**Purpose:** Adds parameter count verification to the final checklist before output.

---

### 6. **Reviewer Validation Enhancement**

**Location:** `reviewImagePrompt()` validation criteria

**Added:**

```
⚠️ PARAMETER 18 (SCALE & ZOOM) IS MANDATORY: If the visual direction includes SCALE & ZOOM 
but the prompt doesn't describe frame fill/proximity = CRITICAL VIOLATION.
```

**Purpose:** Catches missing SCALE & ZOOM during the review phase as a critical failure.

---

### 7. **New Violation Type: MISSING_SCALE_ZOOM**

**Location:**

- Violation detection list in `reviewImagePrompt()`
- `ViolationType` enum in `ImageReference.kt`

**Added:**

```kotlin
MISSING_SCALE_ZOOM, // In enum

// In prompt:
MISSING_SCALE_ZOOM(CRITICAL): The visual direction provides SCALE & ZOOM parameter
but the prompt doesn 't describe frame fill or proximity at all. This is a CRITICAL 
omission that breaks framing precision.
```

**Purpose:** Explicitly tracks when SCALE & ZOOM is completely absent (different from just being
wrong).

---

### 8. **Auto-Fix Pattern for Missing Parameter**

**Location:** Auto-fix patterns in `reviewImagePrompt()`

**Added:**

```
Missing scale/zoom (CRITICAL) → IMMEDIATELY inject spatial descriptors matching 
the visual direction's SCALE & ZOOM parameter (e.g., 'filling 80% of frame with 
intimate proximity'). This is NON-NEGOTIABLE.
```

**Purpose:** Ensures the reviewer will automatically inject SCALE & ZOOM descriptors if they're
missing from the prompt.

---

## Enforcement Layers Summary

| Layer                     | Type       | Severity       | When It Acts             |
|---------------------------|------------|----------------|--------------------------|
| 1. Header Mandate         | Prevention | ⚠️ Warning     | Before extraction starts |
| 2. Visual Warning Marker  | Prevention | ⚠️ Visual Stop | At parameter 18          |
| 3. Completeness Checklist | Prevention | ✓ Verification | Before output            |
| 4. Final Reminder         | Prevention | 🔴 Critical    | Before output            |
| 5. Verification Question  | Prevention | ❓ Self-check   | Final gate               |
| 6. Reviewer Validation    | Detection  | 🔴 Critical    | Review phase             |
| 7. Missing Violation Type | Detection  | 🔴 Critical    | Review phase             |
| 8. Auto-Fix Pattern       | Correction | 🛠️ Auto-fix   | Review phase             |

---

## Why This Works

### Redundancy Strategy

Multiple enforcement points mean even if the AI misses one warning, it will hit another. This is
defense-in-depth.

### Visual Cues

The ⚠️ emoji and formatting make parameter 18 stand out visually in the prompt text.

### Counting Mechanism

The checklist forces the AI to explicitly count parameters, making it impossible to accidentally
stop at 17.

### Consequence Framing

"INCOMPLETE and REJECTED" language emphasizes that omitting parameters has severe consequences.

### Reviewer Safety Net

Even if the extraction phase fails, the reviewer will catch the missing parameter and auto-fix it.

---

## Expected Outcomes

### Before Enhancement

- AI might output 17 parameters and stop
- No validation for parameter completeness
- Missing SCALE & ZOOM would go undetected until generation

### After Enhancement

- Multiple warnings before parameter 18
- Explicit checklist requires counting to 18
- Final verification asks "Do you have ALL 18 parameters?"
- Reviewer flags missing SCALE & ZOOM as CRITICAL violation
- Auto-fix injects missing parameter if needed

---

## Testing Recommendations

### Validate Enforcement

1. Monitor extraction outputs for parameter count
2. Check analytics for `MISSING_SCALE_ZOOM` violations
3. Compare before/after rates of incomplete extractions
4. Verify reviewer is catching and fixing omissions

### Success Metrics

- **Target:** 0% extractions with fewer than 18 parameters
- **Fallback:** 100% of missing parameters caught by reviewer
- **Quality:** No images generated with incorrect framing scale

---

## Maintenance Notes

### If AI Still Forgets

Additional enforcement options:

1. Add parameter number prefixes in the output format requirements
2. Request explicit confirmation: "I have provided all 18 parameters"
3. Add a post-extraction validation step before passing to art director
4. Implement programmatic validation to count parameter lines

### Future Improvements

- Consider JSON-structured output for extraction (enforces schema)
- Add parameter count to extraction analytics
- Create automated tests that reject incomplete extractions
- Implement retry logic if parameter count < 18

---

## Conclusion

This multi-layered enforcement approach ensures that the SCALE & ZOOM parameter cannot be
accidentally omitted. Through visual warnings, explicit checklists, final verification, and reviewer
safety nets, we've created a robust system that enforces parameter completeness at every stage of
the pipeline.

The key insight: **Don't rely on a single enforcement point.** Layer multiple reminders and checks
so that even if the AI's attention wavers, it will be caught by one of the safety mechanisms.

