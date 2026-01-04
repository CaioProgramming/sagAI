# Expressive Messages (Rich Text) Implementation Plan

## Objective

Enhance the immersive quality of the chat experience by moving away from rigid, single-type
messages (e.g., purely "Action" or purely "Thought" bubbles) towards **Expressive Messages**.

This feature allows a single message bubble to contain multiple narrative intentions—dialogue,
internal monologue, and physical actions—blended seamlessly using embedded tags.

## Core Concept

Currently, `SenderType` determines the entire style of a bubble. This limits the AI's ability to
interweave narrative elements naturally (e.g., *talking while doing something*).

We will introduce a **Tag-Based Formatting System** similar to HTML/Markdown, which the UI will
parse into rich `AnnotatedString` styles.

### The Vision

Instead of three separate bubbles:

1. (Action) *Draws sword*
2. (Text) "You won't pass."
3. (Thought) *I hope he believes me.*

We have one cohesive stream:
`"<action>Draws sword</action> You won't pass. <think>I hope he believes me.</think>"`

## Supported Tags & Styles

| Tag            | Visual Style                                                                                                                | Intention                                 |
|:---------------|:----------------------------------------------------------------------------------------------------------------------------|:------------------------------------------|
| `<action>...`  | **Background:** Black/Dark<br>**Text:** Amber/Yellow<br>**Style:** Bold + Italic                                            | Physical movements, environmental shifts. |
| `<think>...`   | **Text:** Genre Primary Color<br>**Style:** Italic + Light Weight<br>**Effect:** *Optional* sparkle/star animation overlay? | Internal monologues, telepathy.           |
| `<shout>...`   | **Text:** Uppercase + Larger Size<br>**Style:** Bold/Heavy                                                                  | Screaming, loud noises.                   |
| `<whisper>...` | **Text:** Smaller Size (0.8em)<br>**Opacity:** Reduced (0.7f)                                                               | Whispering, secrets.                      |

## Technical Architecture

### 1. Rich Text Parser (`features/saga/chat/utils/RichTextParser.kt`)

A dedicated utility to parse raw strings into Jetpack Compose `AnnotatedString`.

* **Logic:** Use Regex to identify supported tags.
* **Output:** `AnnotatedString` with `SpanStyle` applied to specific ranges.
* **Tag Stripping:** The final string shown to the user must *not* contain the `<tags>`. They are
  consumed to produce the styles.

### 2. Typewriter Adaptation (`ui/theme/Animations.kt`)

The existing `TypewriterText` component currently operates on raw `String`s. It needs a major
refactor to support `AnnotatedString` while preserving the rich styles during the
character-by-character reveal.

* **Current:** `text.take(n)` -> Returns a String, losing spans.
* **Required:** `annotatedString.subSequence(0, n)` -> Returns `AnnotatedString`, preserving spans.
* **Integration:** Must work in tandem with the existing `CustomVisualTransformation` (
  Wiki/Character highlighting). Ideally, the Rich Text Parser runs *first*, and Wiki/Character
  detection runs on the plain text result, adding their annotations on top.

### 3. AI Prompt Engineering

The `GemmaClient` system instructions need to be updated to teach the AI how to use these tags.

* **Instruction:** "Use `<action>` for physical acts and `<think>` for internal thoughts. Do not
  split these into separate JSON objects unless the speaker changes."
* **Fallbacks:** The parser should handle malformed tags gracefully (e.g., unclosed tags).

## Implementation Steps

1. **Proof of Concept (Parser):**
    * Create `RichTextParser.kt`.
    * Write unit tests with mixed content strings.
2. **Component Upgrade:**
    * Refactor `TypewriterText` to accept `AnnotatedString`.
    * Update `ChatBubble` to preprocess the message text through the parser before passing it to the
      UI.
3. **Visual Polish:**
    * Implement the specific background shapes for `<action>` spans (Compose `DrawStyle` or
      `background` modifier on text layouts is tricky; might need `SpanStyle(background = ...)`).
4. **AI Integration:**
    * Update system prompts to encourage tag usage.

## Risks & Mitigations

* **Complexity:** Stacking `RichTextParser` styles + `Wiki` annotations + `Typewriter` animation is
  complex.
    * *Mitigation:* Order of operations is critical. Parse Tags -> Typewriter Slicing -> Apply
      Wiki/Character Links.
* **Visual Noise:** Too many styles might look chaotic.
    * *Mitigation:* Restrict tags to high-impact moments. Keep the base text style clean.
