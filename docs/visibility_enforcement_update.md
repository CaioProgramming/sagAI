# Reviewer Update: The Three Pillars

## Overview

The reviewer prompt has been restructured around three core pillars to ensure strict adherence to
cinematography, narrative intent, and artistic style. A critical update has been made to **Pillar 1
** to aggressively target and remove hallucinations of out-of-frame body parts.

## The Three Pillars

### 1. PILLAR 1: CINEMATOGRAPHY & VISIBILITY (The "Camera")

- **Focus**: Framing, Scale, and Visibility.
- **Strictness**: Hallucinations of out-of-frame body parts are critical violations.
- **Enforcement**:
    - **Keyword Scan**: Explicitly bans words like 'pants', 'trousers', 'shoes', 'sneakers', '
      boots', 'legs', 'feet' when framing is CU/MCU/Waist-Up.
    - **Aggressive Execution**: Mandates immediate DELETION of these terms and their associated
      descriptions.
    - **System Failure**: Failure to remove invisible clothing is considered a critical system
      failure.
- **Checks**: Strict comparison against `VISIBILITY_BREAKDOWN`.

### 2. PILLAR 2: NARRATIVE DNA & SUBJECT (The "Actor")

- **Focus**: Action, Expression, Orientation, and Body Language.
- **Anti-Cliché**: Rejects generic "standing" or "posing". Requires specific, narrative-driven
  actions and nuanced emotional beats.
- **Naturalism**: Ensures poses feel motivated by the story and avoids unnatural anatomy.

### 3. PILLAR 3: STYLE & ATMOSPHERE (The "Art")

- **Focus**: Lighting, Composition, and Genre Rendering.
- **Cohesion**: Ensures lighting and composition match the specific genre rules and artistic
  technique.

## Changes in `ImagePrompts.kt`

- Restructured `reviewImagePrompt` to follow the Pillar format.
- Added `CLICHÉ_SUBJECT_VIOLATION` and `UNNATURAL_POSE_VIOLATION`.
- **Enhanced Visibility Logic**: Added explicit keyword scanning and deletion instructions to
  `AUTO-FIX PATTERNS`.