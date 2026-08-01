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

### 23. Saga Wrapped — Renderização Temática 💡 (Signature)

* **Status**: Proposto 💡 — aguardando decisão do time, não priorizado
* **Folder**: `wrapped_themed_render/`
* **Plan**: `wrapped_themed_render/task.md`
* **Descrição**: Versão do Saga Wrapped com renderização temática por gênero (terminal pro
  cyberpunk, livro pro fantasy) para assinantes Signature, mantendo o Wrapped atual 100% free.
* **Origem**: achado de Premium do `product_agent.md`, planejado pelo `feature_planning_agent.md`.
* **Bloqueio**: nenhuma capacidade de vídeo existe hoje no projeto — recomendação é um spike técnico
  (Media3 Transformer, 1 gênero, prova de conceito) antes de qualquer estimativa de prazo real.

### 24. Split System Instructions Architecture

* **Status**: Brainstorming / Planning — não priorizado
* **Folder**: `split_system_instructions/`
* **Plan**: `split_system_instructions/task.md`
* **Description**: Decouples static AI instructions (Role, Directives, Rules) from dynamic task
  data (Templates, User Input) using the Gemini API `system_instruction` field, to improve
  instruction adherence and enable context caching. Relocated here from a stray
  `app/src/main/docs/` copy during the August 2026 docs cleanup.

---
## Usage

To start working on a feature:

1. Open the corresponding `task.md` or `plan.md` in the feature's folder.
2. Follow the agent-specific instructions within.
