# Cinematic Milestones

Cinematic Milestones enhance the narrative progression of Sagas by introducing full-screen,
atmospheric overlays for major story transitions (Acts and Chapters). They also fix race conditions
and provide a smoother user experience during generative steps.

## Features

### 1. Cinematic Introductions

When a new Act or Chapter begins, a cinematic overlay is displayed:

- **Atmospheric Visuals**: A black background with sequential animation reveals.
- **Roman Numerals**: Acts are introduced with large, elegant Roman numerals.
- **AI Rewriting**: The initial (often lengthy) introduction text is dynamically rewritten by AI to
  be short, punchy, and cinematic (max 25 words).
- **Typewriter Animation**: The rewritten text is revealed using a `SimpleTypewriterText` animation.
- **Auto-Dismiss**: The overlay auto-dismisses after the animation completes, ensuring a seamless
  flow.

### 2. Loading State Milestone

A new `Loading` milestone type masks background processing:

- **Visual Mask**: A black screen with a shimmering spark icon.
- **Strategic Use**: Shown ONLY before generative narrative steps (e.g., creating a new Act or
  starting a Chapter) to prevent the user from seeing partial chat updates or "bot-speak" during
  generation.

### 3. Race Condition Fix

The `continueMilestone()` logic was refactored to ensure:

- Saga state is captured *before* dismissal.
- The milestone is dismissed *before* subsequent narrative actions are processed.
- This prevents re-entry issues and ensures the timeline progresses correctly.

## Architecture

### Milestone Data Model

New members added to `SagaMilestone.kt`:

- `SagaMilestone.Introduction`: Contains introduction text, type (ACT/CHAPTER), and sequence number.
- `SagaMilestone.Loading`: A placeholder for loading states.

### AI Processing

- `MilestonePrompts.rewriteIntroduction`: A dedicated prompt that instructs the AI to shorten the
  introduction while maintaining genre tone and emotional impact.
- `MilestoneUseCase.rewriteIntroduction`: Orchestrates the AI call via `GemmaClient`.
- `MilestoneViewModel.rewriteIntroduction`: Exposes the rewriting logic to the UI and manages the
  `rewrittenIntroduction` state.

### UI Components

- `MilestoneOverlay.kt`: The main entry point that delegates to specific overlays.
- `IntroductionOverlay.kt`: Handles the cinematic reveal and typewriter animation.
- `LoadingMilestoneOverlay.kt`: Displays the loading mask.

## Usage

Introductions are triggered automatically within `SagaContentManagerImpl` when a new Act or Chapter
introduction is generated. Loading states are emitted before any long-running generative call.
