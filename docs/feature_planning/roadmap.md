# Feature Implementation Roadmap

This document outlines the planned order of execution for upcoming features. Each feature has its
own dedicated folder containing detailed tasks and implementation plans.

### 21. Agentic Tools Architecture 🤖

* **Status**: Cancelled ❌
* **Folder**: `agentic_tools_architecture/`
* **Plan**: `agentic_tools_architecture/task.md`
* **Description**: Architectural shift to support Tool Calling (Function Calling) in `GemmaClient`.
  Introduces the `AITool` interface, Hilt `@IntoMap` registry, `AIToolName` whitelist enum, and
  agentic prompt blueprints. Enables the AI to autonomously fetch lore, characters, and wiki entries
  instead of receiving massive pre-stuffed context blocks. Prerequisite for Books Phase 2.
* **Reason**: Latency was too high and compromising the experience, we rollback to the previous one
  shot prompt.

### 22. Books — Act Chronicles 📖

* **Status**: Parked ⏸️ — Blocked on #21
* **Folder**: `book/` _(to be created)_
* **Plan**:
  `../../../.gemini/antigravity/brain/989dd17f-d917-4768-a544-5e0c2a5950e9/implementation_plan.md`
* **Description**: Transforms a completed Act into a shareable literary mini-book (the "Chronicle").
  The AI re-reads the act's lore sandwich and retells it as third-person prose with woven dialogue.
  Persisted in `Act.book` (Room), generated once, readable from `SagaDetailView`'s Acts section.
  Phase 2 uses agentic tools (`GET_STORY_LOG`, `GET_CHARACTER_PROFILES`, `GET_WIKI_ENTRIES`) so the
  Chronicler agent pulls exactly the context it needs instead of a single stuffed prompt.

---
## Usage

To start working on a feature:

1. Open the corresponding `task.md` or `plan.md` in the feature's folder.
2. Follow the agent-specific instructions within.
