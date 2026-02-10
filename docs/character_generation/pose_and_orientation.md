# Pose, Orientation & Framing Guide 📐

This document details the systems used to analyze and describe character positioning, camera distance, and composition.

---

## 1. Subject Orientation: The 3-Component System

To capture dynamic poses accurately, we analyze three independent components.

### Component 1: BODY AXIS (The Torso)
**What it measures:** The direction the shoulders/torso are facing relative to the camera.
*   **Front-facing:** Both shoulders equally visible.
*   **3/4 Turn:** One shoulder more prominent (rotated ~45°).
*   **Profile:** Body perpendicular (90°).
*   **Back-facing:** Back of shoulders/neck visible.
*   **Back-with-head-turn (Dynamic):** Back visible + Head turned over shoulder.
*   **Obscured:** Only for ECU where shoulders aren't visible.

### Component 2: HEAD DIRECTION (The Face)
**What it measures:** Where the face is pointing (independent of body).
*   **Front:** Symmetrical face.
*   **3/4:** One side more prominent.
*   **Profile:** Silhouette.
*   **Over-shoulder:** Turned back over the shoulder.

### Component 3: GAZE (The Eyes)
**What it measures:** Where the eyes are looking.
*   **Direct:** Eye contact with viewer/camera.
*   **Averted:** Looking away (left/right/up/down).

### 💡 Common Pitfalls
*   **Confusing Body vs Head:** A character can have a **Back-facing Body** but a **Front-facing Head** (looking back). This is a dynamic "Over-shoulder" pose.
*   **Confusing Head vs Gaze:** A character can face forward but look to the side (side-eye).

---

## 2. Scale & Zoom (Parameter 18)

We explicitly extract **how much of the frame the subject occupies** to ensure the generated image feels like it was taken from the same distance.

### The Two Variables
1.  **Frame Fill %:** Approximate percentage of the image covered by the subject.
2.  **Perceived Distance:** Descriptors like "intimate," "conversational," "distant."

### Alignment Guide
| Shot Type | Frame Fill | Description |
| :--- | :--- | :--- |
| **ECU / CU** | 75-100% | "Intimate proximity," "Face fills frame" |
| **MCU / MS** | 50-75% | "Comfortable distance," "Balanced" |
| **FS / WS** | 30-50% | "Expanded environment," "Full figure" |
| **EWS** | 10-30% | "Distant," "Subject in vast space" |

---

## 3. Truthful Extraction & Visibility

**"If you can't see it, don't describe it."**

The AI must avoid hallucinating body parts that are outside the frame.
*   **CU:** Do NOT describe hands or lap.
*   **MS:** Do NOT describe legs or feet.

### Model Analysis Note (Gemma 3 12B)
Smaller models struggle with 3D orientation in 2D images (e.g., distinguishing a front-facing chest from a back-facing back in a tight shirt).
**Solution:** We use explicit visual cue checklists in prompts:
*   "Can you see the back of the neck?"
*   "Can you see the shoulder blades?"
*   "Is the throat visible?"

This step-by-step reasoning helps the model determine the correct orientation.
