# Character Generation Documentation 🧙‍♂️

This folder contains the comprehensive documentation for the Character Generation system in SagAI.

## 📚 Documentation Index

### 1. [Philosophy](./philosophy.md) 🧠
**"Bringing a person to life."**
*   The core principles behind character creation.
*   Moving from "filling a database" to "co-authoring a protagonist."
*   The 3-Phase Soulful Pipeline (Director, Artist, Reviewer).

### 2. [Style Guide](./style_guide.md) 🎨
**"Visual Identity & Rendering."**
*   **Genre Guides:** Cyberpunk (Heavy Augmentation), Fantasy (Renaissance Oil), Realistic (Cinematic).
*   **Rendering Essence:** How to describe brushwork, lighting, and texture.
*   **Truthful Extraction:** Avoiding hallucinations in framing.
*   **Theme Colors:** Using `hexColor` as a visual guide.

### 3. [Pose, Orientation & Framing](./pose_and_orientation.md) 📐
**"Composition & Body Language."**
*   **The 3-Component System:** Analyzing Body Axis, Head Direction, and Gaze independently.
*   **Scale & Zoom:** Mandatory extraction of frame fill and perceived distance.
*   **Visibility Rules:** If it's not in frame, don't describe it.

### 4. [Technical Implementation](./technical_implementation.md) 🛠️
**"Under the Hood."**
*   **Architecture:** Unified `NewSagaViewModel` with dual state managers.
*   **State Management:** Reactive flows for Saga and Character creation.
*   **Data Flow:** Single-step conversational prompts with extraction.
*   **UI Components:** Pager transitions and smart navigation.

---

## Quick Start

### For Prompt Engineers
Start with the **[Style Guide](./style_guide.md)** to understand the mandatory visual elements for each genre. Then review **[Pose & Orientation](./pose_and_orientation.md)** to master the 3-component system.

### For Developers
Read **[Technical Implementation](./technical_implementation.md)** to understand the ViewModel/StateManager architecture. Then check **[Philosophy](./philosophy.md)** to understand *why* the prompts are structured the way they are.

### For Content Designers
**[Philosophy](./philosophy.md)** is your bible. It explains how to make the AI feel like a "friendly co-author" rather than a tool.
