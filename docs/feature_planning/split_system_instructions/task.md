# Spec-Driven Development: Split System Instructions Architecture

## Status: BRAINSTORMING / PLANNING (FINAL SPEC)

**Objective**: Decouple static AI instructions (Role, Directives, Rules) from dynamic task data (
Templates, User Input) using the Gemini API `system_instruction` field. This optimizes instruction
adherence, performance (via potential context caching), and transparency.

---

## 1. Architectural Vision

The "Split & Merge" architecture separates the **AI Persona and Governance** (The Engine/Logic) from
the **AI Task and Context** (The Data/Evidence).

Instructions are treated as **Nested Buckets** that are composed on the fly by the orchestrator.

---

## 2. Component Specifications

### 2.1. PromptBlueprint (Model)

Extend `PromptBlueprint.kt` to support dynamic instruction grouping:

- **`instructions: Map<String, Map<String, String>>`**: A nested map where the first key is the *
  *Category** (e.g., "IDENTITY", "DIALECT") and the second is the **Rule Name** to its **Content**.
- **Legacy Promotion**: Existing `role`, `directives`, and `rules` fields will be automatically "
  promoted" to internal `# IDENTITY`, `# MODULE DIRECTIVES`, and `# RULES` buckets respectively
  during processing.
- **Intent**:
    - **Instructions (Static)**: Role, Directives, Rules, and the new `instructions` map.
    - **Data (Dynamic)**: `template` and `examples`.

### 2.2. PromptService (The Splitter)

**Requirement**: Introduce `buildSplitBlueprint`.
- **Logic**:
    1. Replace placeholders (variables) **only** in the `template` and `examples`.
    2. Collect all static instruction components (`role`, `directives`, `rules`, and `instructions`
       map).
    3. Return a `SplitPrompt` object containing the `instructionBuckets` and the
       `processedTemplate`.

### 2.3. GemmaClient (The Orchestrator)
**Requirement**: Refactor `generate` and `generateStreaming`.

- **Merging Algorithm**:
    - Initialize a master instruction map.
    - Load **Core Blueprint** instructions as the foundational layer.
    - If a `blueprintKey` is provided, merge its instructions into the master map.
    - If optional "Soul/Style" instructions are provided, merge them as the final layer (Recency
      Bias).
- **Assembly**:
    - Render the merged map into a single Markdown-structured string for the `system_instruction`
      API field.
    - Render the processed templates and user `prompt` into the `contents` (User) API field.

### 2.4. AIAuditLog (Visibility & Debugging)

**Requirement**: Update `AIAuditLog.kt` and `AIAuditLogDao`.

- **New Field**: `systemInstruction: String?` - Captures the final merged instruction block.
- **New Field**: `sentVariables: String?` (JSON) - Captures the raw map of variables sent to
  `PromptService` to detect clipping or missing data.
- **Migration**: Increment Room database version and handle schema change.

### 2.5. Audit UI (Debugging)

**Requirement**: Update `AIAuditLogView.kt` and `JsonCodeBlock`.

- Add a new collapsible section in the log detail view to display the `system_instruction`.
- Add a section to display `sentVariables` (using the existing JSON syntax highlighting) to verify
  data integrity.

---

## 3. Implementation Workflow (Agent Instructions)

1. **Database Layer**: Update `AIAuditLog.kt` and perform Room migration.
2. **Domain Layer**: Update `PromptBlueprint.kt` and implement `buildSplitBlueprint` in
   `PromptService.kt`.
3. **Infrastructure Layer**: Refactor `GemmaClient.kt` to support `system_instruction` merging and
   updated logging.
4. **Presentation Layer**: Update `AIAuditLogView.kt` to render the new debug fields.
5. **Documentation Layer**: Update KDoc in `PromptBlueprint.kt` to provide clear guidelines for
   future AI agents/developers on how to structure `systemInstructions` vs `template` in Remote
   Config.

---

## 4. Success Metrics

- **Adherence**: 100% adherence to JSON schemas via prioritized system instructions.
- **Persona Stability**: Zero "personality drift" even with 3+ layers of active blueprints.
- **Transparency**: Developers can see exactly which "Laws" and "Variables" were used in every AI
  turn via the Audit UI.
