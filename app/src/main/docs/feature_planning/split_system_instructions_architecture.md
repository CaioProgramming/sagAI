# Spec-Driven Development: Split System Instructions Architecture

## Status: BRAINSTORMING / PLANNING

**Objective**: Decouple static AI instructions (Role, Directives, Rules) from dynamic task data (
Templates, User Input) using the Gemini API `system_instruction` field to optimize adherence,
performance, and cacheability.

---

## 1. Architectural Vision

The "Split & Merge" architecture separates the **AI Persona and Governance** (How to behave) from
the **AI Task and Context** (What to process).

### The Layers of Instruction (System Block)

1. **Core Governance (Global)**: JSON schema requirements, UTF-8 encoding, and language constraints.
2. **Feature Workflow (Operational)**: The specific logic for a module (e.g., "You are a Reply
   Generator").
3. **Conversational Soul (Stylistic)**: The persona-specific rules (e.g., "Cowboys Frontier
   Dialect").

---

## 2. Component Specifications

### 2.1. PromptBlueprint (Model)

No changes needed to the data structure, but a change in *intent*:

- `role`, `directives`, `rules`: Mapped to `system_instruction`.
- `template`: Mapped to `contents` (User).
- `examples`: Mapped to `contents` (User/Model turns).

### 2.2. PromptService (The Splitter)

**Requirement**: Introduce `buildSplitPrompt`.

- **Input**: `remoteConfigKey`, `variables`.
- **Output**: `SplitPrompt` object.
- **Logic**:
    - Format `role`, `directives`, and `rules` into a markdown-structured instruction block.
    - Process `template` with `{variable}` replacement as the task block.
    - Replace placeholders in directives/rules *before* moving to system block if necessary.

### 2.3. GemmaClient (The Orchestrator)

**Requirement**: Refactor `generate` and `generateStreaming`.

- **Merging Strategy**:
    - Fetch "Core Blueprint".
    - If `blueprintKey` is provided, call `buildSplitPrompt`.
    - Combine Core + Feature Instructions into one `system_instruction` part.
- **Content Strategy**:
    - Place the `task` (processed template) and the raw `prompt` argument into the `contents` part.

### 2.4. AIAuditLog (Visibility)

**Requirement**: Add `systemInstruction: String?` field.

- **Goal**: Enable distinct debugging of the "Engine" vs. the "Data".

---

## 3. Integration Patterns

### 3.1. Layered "Soul" Injection

For features that require a persona (like Chat), the system must support injecting a secondary
blueprint's instructions into the main workflow's system block.

**Example Flow**:

1. `MessageUseCase` requests a reply.
2. `PromptService` builds the "Reply Workflow" (Split).
3. `PromptService` builds the "Cowboy Style" (Split - extracting only instructions).
4. `GemmaClient` merges: `[Core] + [Reply Workflow Instructions] + [Cowboy Style Instructions]`.

---

## 4. Risks & Considerations

- **Instruction Conflicts**: If a "Soul" directive contradicts a "Workflow" rule. (Resolution:
  System instructions are parsed linearly; later instructions usually take precedence).
- **Token Limits**: Monitor `system_instruction` size. While more efficient, they still count
  towards total input tokens.
- **Migration**: Maintain backward compatibility in `PromptService` to support one-shot prompts
  while migrating modules one by one.

---

## 5. Success Metrics

- **Adherence**: Reduction in "JSON parsing errors" (better following of Core rules).
- **Latency**: Potential reduction via API context caching of the static system block.
- **Coherence**: Persona (e.g., Cowboy) should remain more stable even during long conversations
  with deep history.
