# 🎭 Expressive Messages

**Status:** ALL PHASES COMPLETE ✅
**Date:** January 9, 2026

---

## 📖 Overview

The Expressive Messages feature enhances the immersive quality of the chat experience by moving away from rigid, single-type messages (purely "Action" or purely "Thought" bubbles) towards **Expressive Messages**.

This feature allows a single message bubble to contain multiple narrative intentions—dialogue, internal monologue, and physical actions—blended seamlessly using embedded tags.

Instead of three separate bubbles:
1. *(Action) Draws sword*
2. (Text) "You won't pass."
3. *(Thought) I hope he believes me.*

We have one cohesive stream:
`"<action>Draws sword</action> You won't pass. <think>I hope he believes me.</think>"`

---

## 🎨 Features & Usage

### 1. Supported Tags

| Tag | Syntax | Visual Effect (Last Message) | Intention |
|:---|:---|:---|:---|
| **Action** | `<action>text</action>` | **Levitates + Flickers** (amber, bold italic) | Physical movements, environmental shifts. |
| **Think** | `<think>text</think>` | **✨ Twinkling stars** → Tap to reveal | Internal monologues, telepathy. |
| **Narrator** | `<narrator>text</narrator>` | **Bordered box** (genre border) | Omniscient narrator voice embedded in character messages. |

> **Note:** Only the **LAST** message in the chat has active animations. Older messages show static styled text to preserve performance.

### 2. Input Field UI

*   **Tag Insertion Buttons**: Three buttons above the keyboard (`<action>`, `<think>`, `<narrator>`) insert the corresponding tag pair at the cursor or wrap selected text.
*   **Autocomplete**: Typing `<` triggers a tooltip showing available tags.

### 3. AI Integration

The AI (Gemini) has been trained to use these tags naturally:
*   **Tag-Based Responses**: AI generates messages with mixed tags.
*   **Suggestions**: Suggestions now include tags (e.g., `"I'll help. <think>Against my better judgment.</think>"`).
*   **Privacy Rules**: NPCs cannot "read" `<think>` tags from other characters, but can express their own.

---

## 🏗️ Technical Implementation

### Core Components

1.  **RichTextParser** (`ui/theme/RichTextParser.kt`):
    *   Parses raw message strings into structured segments (`Plain`, `Action`, `Think`, `Narrator`).
    *   Uses Regex for identification.
    *   Cached with `remember(text)` for performance.

2.  **ExpressiveText** (`features/saga/chat/ui/components/ExpressiveText.kt`):
    *   Main orchestrator composable.
    *   Uses `FlowRow` to render segments naturally.
    *   Manages the `shouldAnimate` state.

3.  **Animated Components**:
    *   **LevitatingText**: Handles `<action>` animation (sine wave levitation + alpha flicker).
    *   **ThinkingText**: Handles `<think>` interaction (stars overlay, tap to reveal).
    *   **NarratorBox**: Handles `<narrator>` styling (bordered box).

### Integration

*   **ChatBubble**: Modified to use `ExpressiveText` instead of `TypewriterText` when tags are present.
*   **ChatInputView**: Added tag insertion logic and autocomplete.

### Performance Strategy

**"Smart Animation Strategy"**:
*   Only the **LAST** message in the list has `shouldAnimate = true`.
*   All other messages render static styled text.
*   This ensures zero CPU overhead for scrolled content while maximizing visual impact for the new message.

---

## 🧪 Testing Checklist

### 1. Message Rendering
- [ ] **Action Tag**: Send `Hello <action>waves</action>`. Verify "waves" levitates/flickers (amber, bold italic).
- [ ] **Think Tag**: Send `Hi <think>nervous</think>`. Verify twinkling stars. Tap to reveal text.
- [ ] **Narrator Tag**: Send `Smile <narrator>context</narrator>`. Verify bordered box.
- [ ] **Mixed Tags**: Send `<action>Act</action> Speak <think>Think</think>`. Verify natural flow.
- [ ] **Old Messages**: Scroll up. Verify animations are gone (static text only).

### 2. Input UI
- [ ] **Buttons**: Click `<action>`, `<think>`, `<narrator>` buttons. Verify tag insertion.
- [ ] **Selection**: Select text, click button. Verify text is wrapped.
- [ ] **Autocomplete**: Type `<`. Verify tooltip appears. Select tag. Verify completion.

### 3. AI Behavior
- [ ] **Response**: Chat with AI. Verify it uses tags naturally.
- [ ] **Privacy**: Send a `<think>` tag. Verify AI does not respond to the thought content.
- [ ] **Suggestions**: Check suggestions. Verify they include tags.

---

## 🔮 Future Enhancements

*   **Additional Tags**: `<whisper>`, `<shout>`, `<emote>`.
*   **Custom Colors**: Allow custom tag colors per genre.
*   **Analytics**: Dashboard for tag usage frequency.
*   **Weighted Animations**: More complex animation curves for different emotions.
