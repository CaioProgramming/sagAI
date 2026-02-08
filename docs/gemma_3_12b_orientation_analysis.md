# Gemma-3-12B-IT Model Capability Analysis for Subject Orientation Extraction

**Date:** January 11, 2026  
**Model:** `gemma-3-12b-it`  
**Current Role:** Image Composition Analysis (MEDIUM tier)

---

## 🔴 PROBLEM STATEMENT

The reference image shows a woman **turning her back to the camera**, but the AI extraction returns:

```
SUBJECT_ORIENTATION: Body front, Head 3/4 left, Gaze averted
```

**Expected Output:**

```
SUBJECT_ORIENTATION: Body back-facing, Head over-shoulder-left, Gaze [direction]
```

OR

```
SUBJECT_ORIENTATION: Body back-facing, Head 3/4 left, Gaze averted
```

This is a **CRITICAL FAILURE** because:

1. The body orientation is completely wrong (front vs back)
2. This affects the visual reproduction in the final image
3. It defeats the entire purpose of the visual director analysis

---

## 📊 MODEL CAPABILITY ASSESSMENT

### Gemma-3-12B-IT Specifications

- **Parameters:** 12 billion
- **Architecture:** Google Gemma-3 family
- **Primary Use Cases:** Analysis, summarization, extraction
- **Vision Capabilities:** Multimodal (text + image)
- **Current Temperature:** 0.0 (deterministic for analysis)

### Known Strengths

✅ Context understanding and summarization  
✅ Structured data extraction  
✅ JSON output formatting  
✅ Following complex instructions  
✅ Technical terminology comprehension

### Known Limitations

⚠️ **Vision Understanding Depth:** While multimodal, the 12B parameter model may have:

- Limited spatial reasoning capabilities
- Difficulty with fine-grained visual distinctions
- Tendency to default to common patterns ("front-facing" is statistically more common in training
  data)
- Less robust 3D orientation understanding from 2D images

---

## 🔍 ROOT CAUSE ANALYSIS

### Why the Model Might Be Failing

#### 1. **Visual Perception Limitation**

The model may struggle with:

- **Shoulder cue interpretation:** In close-up shots, distinguishing front shoulders from back
  shoulders
- **Subtle anatomical markers:** Recognizing back-of-neck vs front-of-neck
- **Depth perception:** Understanding 3D body rotation from 2D images

#### 2. **Statistical Bias**

- Training data likely contains more **front-facing portraits** than back-facing
- Model defaults to statistically common interpretations when uncertain
- "Front-facing" becomes the "safe" answer

#### 3. **Instruction Complexity**

Current prompt has 16+ detailed parameters to analyze:

- Model may "rush" through orientation analysis
- Cognitive load from multiple simultaneous tasks
- Later parameters might receive less attention

#### 4. **Lack of Confidence Weighting**

- No mechanism to express uncertainty
- Model forced to provide definitive answer even when unsure
- Cannot say "Body orientation: uncertain (70% back-facing, 30% front)"

---

## 🧪 DIAGNOSTIC QUESTIONS

### Can Gemma-3-12B-IT Actually See the Difference?

**Test Approach:**
Create isolated test prompt asking ONLY about body orientation with multiple choice format:

```
Analyze ONLY the body orientation of the subject in this image.

Look at these specific visual cues:
1. Can you see the back of the shoulders? (YES/NO)
2. Can you see the front of the chest/torso? (YES/NO)
3. Can you see the back of the neck? (YES/NO)
4. Are both shoulders visible facing forward? (YES/NO)

Based on these cues, the body is:
A) Front-facing (chest/front visible, front of shoulders square)
B) Back-facing (back of shoulders/neck visible, rear torso)
C) 3/4 turn (one shoulder prominent, rotated ~45°)
D) Unable to determine from visible cues

Answer in JSON:
{
  "backOfShoulders": true/false,
  "frontOfChest": true/false,
  "backOfNeck": true/false,
  "bothShouldersFacingForward": true/false,
  "bodyOrientation": "A/B/C/D",
  "confidence": "high/medium/low",
  "reasoning": "brief explanation"
}
```

This would tell us if the model CAN see the difference when focused solely on this task.

---

## 🎯 POTENTIAL SOLUTIONS

### Option 1: ⚡ IMMEDIATE - Add Visual Cue Checklist

**Modify the prompt to force step-by-step analysis:**

```kotlin
"16. SUBJECT_ORIENTATION - BODY AXIS: First, answer these questions about VISIBLE BODY CUES:"
"   □ Can you see the BACK of shoulders/neck? → If YES, body is BACK-FACING or side"
"   □ Can you see the FRONT of chest/torso? → If YES, body is FRONT-FACING or 3/4"
"   □ Is ONE shoulder more prominent? → If YES, body is 3/4 TURN"
"   Based on these cues, determine:"
"   • BODY AXIS: [Front-facing / 3/4 turn left/right / Side profile / Back-facing / OBSCURED]"
```

**Pros:** Quick fix, forces methodical analysis  
**Cons:** Adds token count, may not solve perception issue

---

### Option 2: 🔄 MEDIUM - Use Chain-of-Thought Reasoning

**Request explicit reasoning before answer:**

```kotlin
"16. SUBJECT_ORIENTATION:"
"   THINK STEP BY STEP:"
"   1. First describe what parts of the body are visible (shoulders, chest, back, neck)"
"   2. Then identify visual markers (which direction are shoulders facing?)"
"   3. Finally determine body axis based on these observations"
"   
"   • BODY AXIS: [determination] BECAUSE [visible evidence]"
```

**Pros:** Improves accuracy through reasoning  
**Cons:** Significantly increases token usage and latency

---

### Option 3: 🔧 UPGRADE - Use Larger Model for Critical Analysis

**Switch to Gemma-3-27B-IT for visual extraction:**

```kotlin
private suspend fun extractComposition(bitmap: Bitmap?) =
    executeRequest {
        gemmaClient.generate<String>(
            emptyString(),
            temperatureRandomness = 0f,
            references = listOf(
                ImageReference(
                    bitmap!!,
                    ImagePrompts.extractComposition(),
                ),
            ),
            requireTranslation = false,
            requirement = GemmaClient.ModelRequirement.HIGH, // ← Use 27B instead of 12B
        )!!
    }
```

**Pros:** More visual understanding capability, better spatial reasoning  
**Cons:** Higher cost, slower processing, increased token usage

---

### Option 4: 🎨 RADICAL - Use Specialized Vision Model

**Use a dedicated vision model for orientation detection:**

Consider using:

- **Gemini Pro Vision** (better multimodal understanding)
- **GPT-4 Vision** (excellent spatial reasoning)
- **Custom fine-tuned model** for body orientation classification

**Pros:** Specialized capability, highest accuracy  
**Cons:** Significant architectural change, cost implications, added complexity

---

### Option 5: 🧩 HYBRID - Two-Stage Analysis

**Stage 1:** Dedicated orientation classifier (simple task)  
**Stage 2:** Full composition analysis with orientation locked

```kotlin
// Stage 1: Dedicated orientation analysis
val orientation = detectBodyOrientation(bitmap) // Simple, focused task

// Stage 2: Full composition with orientation as input
val composition = extractComposition(bitmap, fixedOrientation = orientation)
```

**Pros:** Focuses model on one task at a time, reduces cognitive load  
**Cons:** Doubles API calls, increased latency and cost

---

## 📋 RECOMMENDED ACTION PLAN

### Phase 1: DIAGNOSIS (Immediate)

1. **Run isolated orientation test** with the checklist prompt
2. **Log model's actual reasoning** if using chain-of-thought
3. **Collect 10 test cases** (5 front-facing, 5 back-facing) and measure accuracy

**Goal:** Determine if this is a perception issue or instruction-following issue

---

### Phase 2: QUICK FIX (If model CAN perceive)

If the model can see the difference when isolated:

1. **Implement Option 1:** Add visual cue checklist
2. **Reorder prompt sections:** Put SUBJECT_ORIENTATION earlier (before model attention fades)
3. **Add emphasis markers:** Use CRITICAL/MANDATORY flags

---

### Phase 3: ESCALATION (If model CANNOT perceive)

If the model cannot reliably detect orientation:

1. **Upgrade to Gemma-3-27B-IT** (Option 3) for extractComposition
2. **Monitor accuracy improvement**
3. **Evaluate cost vs accuracy tradeoff**

---

### Phase 4: NUCLEAR OPTION (If 27B fails)

If even 27B cannot handle this:

1. **Switch to Gemini Pro Vision** for visual extraction
2. **Use Gemma-3-27B-IT** for artistic prompt generation
3. **Accept higher cost for critical accuracy**

---

## 💰 COST-BENEFIT ANALYSIS

### Current Setup (Gemma-3-12B-IT)

- **Cost per call:** ~$X per 1M tokens
- **Accuracy:** Currently FAILING on orientation
- **Speed:** Fast

### Upgraded Setup (Gemma-3-27B-IT)

- **Cost per call:** ~2-3x current cost
- **Accuracy:** Expected 30-50% improvement
- **Speed:** 1.5-2x slower

### Premium Setup (Gemini Pro Vision)

- **Cost per call:** ~5-10x current cost
- **Accuracy:** Expected 80-90% improvement
- **Speed:** Similar to current

---

## 🎯 MY RECOMMENDATION

**SHORT TERM:**

1. **Immediately test Option 1** (Visual Cue Checklist) - 30 minutes work
2. Run 10 test cases to measure if improvement occurs
3. If success rate < 70%, proceed to next step

**MEDIUM TERM:**

4. **Upgrade to Gemma-3-27B-IT** for extractComposition only
5. Keep 12B for other analysis tasks (cost optimization)
6. Monitor accuracy for 1 week

**LONG TERM:**

7. If 27B accuracy is still < 90%, evaluate Gemini Pro Vision
8. Consider fine-tuning a small classifier for orientation detection
9. Build confidence scoring system

---

## 🔬 TESTING SCRIPT

```kotlin
// Add this to test orientation detection accuracy
suspend fun testOrientationAccuracy(testImages: List<Pair<Bitmap, ExpectedOrientation>>) {
    val results = mutableListOf<TestResult>()
    
    testImages.forEach { (bitmap, expected) ->
        val extracted = extractComposition(bitmap).getSuccess()
        val orientationLine = extracted.lines()
            .find { it.startsWith("SUBJECT_ORIENTATION:") }
        
        val bodyOrientation = orientationLine
            ?.substringAfter("Body ")
            ?.substringBefore(",")
        
        val isCorrect = bodyOrientation?.contains(expected.bodyAxis, ignoreCase = true) == true
        
        results.add(TestResult(
            expected = expected.bodyAxis,
            actual = bodyOrientation ?: "MISSING",
            correct = isCorrect
        ))
    }
    
    val accuracy = results.count { it.correct } / results.size.toFloat()
    Log.i("OrientationTest", "Accuracy: ${accuracy * 100}%")
    Log.i("OrientationTest", "Results: ${results.toJsonFormat()}")
}

data class ExpectedOrientation(
    val bodyAxis: String, // "back-facing", "front-facing", etc.
)

data class TestResult(
    val expected: String,
    val actual: String,
    val correct: Boolean
)
```

---

## 🎓 CONCLUSION

**Can Gemma-3-12B-IT understand our requirements?**

- ✅ **YES** to the complexity of instructions
- ❓ **UNCERTAIN** about visual perception capability for orientation
- ❌ **NO** to current implementation (proven by user's case)

**Should we upgrade?**

- **Try Option 1 first** (checklist) - zero cost
- **If that fails, yes** - upgrade to 27B for critical visual analysis
- **Cost is justified** if it solves the core problem

**The Real Question:**
Is this a **vision problem** or an **attention problem**?

- If vision → Need better model
- If attention → Need better prompt

**Next Step:** Run the diagnostic test to find out which one it is.

