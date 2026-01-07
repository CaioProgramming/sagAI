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

| Tag             | Visual Style                                                                                                                                                                                                                                                                                                                                              | Intention                                                 |
|:----------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------|
| `<action>...`   | **Background:** Black/Dark<br>**Text:** Amber/Yellow<br>**Style:** Bold + Italic                                                                                                                                                                                                                                                                          | Physical movements, environmental shifts.                 |
| `<think>...`    | **Text:** Genre Primary Color<br>**Style:** Italic + Light Weight<br>**Effect:** *Optional* sparkle/star animation overlay?                                                                                                                                                                                                                               | Internal monologues, telepathy.                           |
| `<narrator>...` | **Inline narrator box** (distinct from `SenderType.NARRATOR` full bubbles)<br>**Background:** Subtle darker shade than bubble<br>**Border:** Thin border using genre-specific styling<br>**Shape:** Uses `Genre.bubble(isNarrator = true)` for genre-appropriate styling<br>**Text:** Italic + genre body font<br>**Layout:** Inline box within text flow | Omniscient narrator voice embedded in character messages. |

> **Note:** Additional tags like `<shout>` and `<whisper>` can be added in future iterations. For
> now, we're focusing on the core narrative elements: dialogue (plain text), action, thought, and
> narrator.

### Important: Inline Tags vs. Full Bubbles

**Inline `<narrator>` tags** are distinct from **`SenderType.NARRATOR` bubbles**:

- **`SenderType.NARRATOR`**: A full, standalone message bubble for pure narrator content (scene
  transitions, chapter openings, etc.). Already implemented with genre-specific shapes.
- **Inline `<narrator>` tag**: Embedded narrator text *within* a character's message. Example:
  `"She smiled warmly. <narrator>She had no idea what awaited her.</narrator> 'Let's go!'"`

The inline `<narrator>` styling should mirror the genre's narrator bubble aesthetic (using
`Genre.bubble(isNarrator = true)`) but rendered as a small inline box within the text flow.

## Technical Architecture

### 1. Rich Text Parser (`features/saga/chat/utils/RichTextParser.kt`)

A dedicated utility to parse raw strings into Jetpack Compose `AnnotatedString`.

* **Logic:** Use Regex to identify supported tags.
* **Output:** `AnnotatedString` with `SpanStyle` applied to specific ranges.
* **Tag Stripping:** The final string shown to the user must *not* contain the `<tags>`. They are
  consumed to produce the styles.
* **Narrator Box Rendering:** For `<narrator>` tags, the parser will need to work with custom
  `InlineTextContent` or layered `DrawScope` to render bordered boxes inline. Each `<narrator>`
  instance gets its own box, preserving text flow and handling multiple narrators naturally.

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

The AI prompts need significant updates to support the tag-based system and ensure proper usage.

#### A. `replyMessagePrompt` (ChatPrompts.kt)

**Current Issues:**

- Line 119: "NPCs cannot read 'THOUGHT' messages" - needs updating for tag-based thoughts
- No guidance on using tags instead of asterisks for actions
- No rules about when characters can express their own `<think>` tags

**Required Updates:**

1. **Tag Usage Rules** (add after line 115):

```
appendLine("\n# TAG-BASED EXPRESSION SYSTEM")
appendLine("Use inline tags to create rich, flowing narrative:")
appendLine("- <action>...</action> for physical movements (NEVER use asterisks *like this*)")
appendLine("- <think>...</think> for internal thoughts of the speaking character")
appendLine("- <narrator>...</narrator> for omniscient narration or dramatic irony")
appendLine("- Plain text for dialogue")
appendLine("Example: 'I understand. <action>nods slowly</action> <think>Do I really?</think>'")
```

2. **Privacy & Visibility Rules** (replace line 119):

```
appendLine("5. **PRIVACY & VISIBILITY:**")
appendLine("   - NPCs CANNOT read <think> tags from OTHER characters")
appendLine("   - NPCs CAN express their OWN <think> tags to show internal conflict/development")
appendLine("   - NPCs respond only to: dialogue (plain text), <action> tags, and <narrator> context")
appendLine("   - Example: If player sends 'Hello <think>I don't trust him</think>', NPC only sees 'Hello'")
```

3. **Action Formatting** (add to storytelling directives):

```
appendLine("## Action Formatting:")
appendLine("- ALWAYS use <action>...</action> tags for physical movements")
appendLine("- NEVER use asterisks (*waves*, *smiles*) - this is deprecated")
appendLine("- Actions should be concise and integrated into dialogue flow")
```

#### B. `generateSuggestionsPrompt` (SuggestionPrompts.kt)

**Current Issues:**

- Generates separate suggestions per `SenderType` (CHARACTER, THOUGHT, ACTION, NARRATOR)
- Doesn't support mixed-tag suggestions
- Treats each type as mutually exclusive

**Required Refactoring:**

1. **Remove Type-Based Separation** (lines 29-54):
    - Delete the "Allowed `type` values" section
    - Suggestions should now be **full messages** with mixed tags, not type-specific

2. **New Suggestion Format**:

```
appendLine("Task:")
appendLine("Generate exactly 3 creative, contextually relevant suggestions for '${character.name}'.")
appendLine("Each suggestion should be a COMPLETE message that may contain:")
appendLine("  - Dialogue (plain text)")
appendLine("  - <action>...</action> tags for physical movements")
appendLine("  - <think>...</think> tags for internal thoughts")
appendLine("  - <narrator>...</narrator> tags for scene observations")
appendLine("")
appendLine("Examples of good suggestions:")
appendLine("1. \"I'll help you. <action>extends hand</action> <think>Can I trust them?</think>\"")
appendLine("2. \"<action>scans the room</action> Something feels off here.\"")
appendLine("3. \"<narrator>A distant sound echoes.</narrator> Did you hear that?\"")
appendLine("")
appendLine("Each suggestion must:")
appendLine("- Reflect ${character.name}'s personality and current mood")
appendLine("- Be contextually relevant to the scene")
appendLine("- Feel natural and actionable")
appendLine("- Use tags to create rich, flowing narrative")
```

3. **Update Output Schema**:
    - Change `Suggestion` data class to only have `text: String` (remove `type: SenderType`)
    - All suggestions default to `SenderType.USER` when sent
    - Tags are parsed during rendering

#### C. General Tag Guidelines (for all prompts)

Add to system instructions across all AI interactions:

```
## Tag Usage Best Practices:
1. Use tags INLINE within natural dialogue flow
2. Don't overuse tags - plain dialogue is still primary
3. <think> reveals character depth and internal conflict
4. <action> replaces asterisk-based actions entirely
5. <narrator> used sparingly for dramatic effect or scene transitions
6. Multiple tags in one message create richer, more immersive responses
```

#### Key AI Behavior Changes Summary:

| Aspect             | Old Behavior                        | New Behavior                               |
|--------------------|-------------------------------------|--------------------------------------------|
| **Actions**        | Uses asterisks: `*waves*`           | Uses tags: `<action>waves</action>`        |
| **Thoughts**       | Separate THOUGHT message type       | Inline `<think>` tags within messages      |
| **NPC Privacy**    | NPCs can't see THOUGHT messages     | NPCs can't see `<think>` tags from others  |
| **NPC Expression** | NPCs rarely show internal thoughts  | NPCs CAN use `<think>` for character depth |
| **Suggestions**    | One suggestion per type (3-4 types) | 3 rich suggestions with mixed tags         |
| **Message Flow**   | Separate bubbles for each type      | One cohesive message with inline styles    |

### 4. UI Refactoring: Chat Input System

The current `ChatInputView` uses **`SenderType` buttons** to switch between message types (
CHARACTER,
THOUGHT, ACTION, NARRATOR). This needs to be refactored to support the **tag-based system**.

#### Current System (To Be Replaced):

- **Sender Type Buttons** (lines 800-860 in `ChatInputView.kt`): Horizontal row of buttons that
  change the `sendType` state
- **`SenderType.filterUserInputTypes()`**: Returns `[CHARACTER, THOUGHT, ACTION, NARRATOR]`
- **User Flow**: Select a type → Type message → Send (entire message becomes that type)

#### New Tag-Based System:

**Replace sender type buttons with tag insertion helpers:**

1. **Tag Insertion Buttons**: Instead of switching modes, buttons insert tags at cursor position
    - `<action>` button: Inserts `<action>|</action>` (cursor at `|`)
    - `<think>` button: Inserts `<think>|</think>`
    - `<narrator>` button: Inserts `<narrator>|</narrator>`

2. **Default Message Type**: All user messages default to `SenderType.USER` (plain text/dialogue)

3. **Character Selection**: Keep existing character avatar selector for NPC messages (
   `SenderType.CHARACTER`)

4. **Full Narrator Mode**: Add a toggle/button for full `SenderType.NARRATOR` bubbles (scene
   transitions, chapter openings)

#### Required Changes:

**`ChatInputView.kt`:**

- Remove `sendType: SenderType` parameter (or keep only for CHARACTER/NARRATOR full bubbles)
- Replace sender type buttons with tag insertion buttons
- Add `insertTagAtCursor(tag: String, inputField: TextFieldValue, onUpdateInput: (TextFieldValue) ->
  Unit)` helper function
- Update button icons/labels to reflect tag insertion (e.g., "Action", "Thought", "Narrator")
- **Refactor Suggestions Card** (lines 372-430):
    - Remove `it.type.icon()` and `it.type.description()` (lines 402-416)
    - Remove `onUpdateSender(it.type)` call (line 389)
    - Display suggestion text with **rich text parsing** (apply tag styles in preview)
    - Simplify to just show the parsed text with genre styling
    - Example: Suggestion `"Hello <action>waves</action>"` shows as styled text in the card

**`ChatViewModel.kt`:**

- Remove or simplify `updateSenderType()` logic
- Default all user messages to `SenderType.USER` unless:
    - Character is selected → `SenderType.CHARACTER`
    - Full narrator mode toggled → `SenderType.NARRATOR`
- The rich text parser will handle inline tags during rendering

**`ChatView.kt`:**

- Update `ChatInputView` call to remove/simplify `sendType` parameter
- Ensure character selection still works for NPC messages

#### Live Preview in Input Field:

The `BasicTextField` in `ChatInputView` already uses `visualTransformation` for character/wiki
highlighting (lines 659-667). **Extend this to also parse and style tags in real-time:**

- **Current**: `transformTextWithContent()` highlights `@character` and `/wiki` mentions
- **New**: Also parse `<action>`, `<think>`, `<narrator>`, etc., and apply their respective styles
- **Benefit**: Users see styled text as they type, not raw tags
- **Implementation**: The `RichTextParser` should be usable in both `visualTransformation` (input
  preview) and final rendering (chat bubble)

**Example:**

- User types: `"Hello <action>waves</action>"`
- Input field shows: `"Hello [waves in yellow/italic]"` (live preview)
- Chat bubble shows: Same styled output

#### Tag Autocomplete Tooltip:

Similar to the existing `@` (character) and `/` (wiki) autocomplete system, add **`<` tag
autocomplete**:

1. **Detection**: When user types `<`, show a tooltip with available tags
2. **Tooltip Content**: List of tags with descriptions:
    - `<action>` - Physical movements
    - `<think>` - Internal thoughts
    - `<narrator>` - Narrator voice
3. **Selection**: Clicking a tag inserts the full `<tag>|</tag>` with cursor at `|`
4. **Dismissal**: Tooltip dismisses when user types `>` or moves cursor away

**Implementation:**

- Extend `detectQueryType()` function to detect `<` symbol
- Add `ItemsType.Tags` to the existing `ItemsType` sealed class (currently has `Characters` and
  `Wikis`)
- Create `TagsTooltip` composable similar to `QueryItemsTooltip`
- Reuse existing tooltip infrastructure (`queryItemsTooltipState`, `queryTooltipPositionProvider`)

**Visual Example:**

```
User types: "Hello <"

┌─────────────────────────────────┐
│ 📝 action   - Physical movements│  ← Tooltip appears
│ 💭 think    - Internal thoughts │
│ 📖 narrator - Narrator voice    │
└─────────────────────────────────┘

User clicks "action" → Input becomes: "Hello <action>|</action>"
                                               ↑ cursor here
```

#### User Experience Example:

**Before (Current System):**

1. User selects "Action" button → Input switches to ACTION mode
2. User types: "draws sword"
3. Sends → Creates separate ACTION bubble: *(draws sword)*

**After (Tag-Based System):**

1. User types: "You won't pass. "
2. User clicks "Action" button → Inserts `<action>|</action>`
3. User types: "draws sword"
4. User continues: " I hope he believes me."
5. User clicks "Thought" button → Inserts `<think>|</think>`
6. User types: "This is a bad idea"
7. Final text: `"You won't pass. <action>draws sword</action> I hope he believes me. <think>This is a
   bad idea</think>"`
8. Sends → Creates **one cohesive bubble** with mixed styles

#### Suggestions Card Visual Change:

**Before (Type-Based):**

```
┌─────────────────────────────────┐
│ 💬 "Hello there"                │ ← CHARACTER type icon
├─────────────────────────────────┤
│ ⚡ "draws sword"                 │ ← ACTION type icon
├─────────────────────────────────┤
│ 💭 "I'm nervous"                 │ ← THOUGHT type icon
└─────────────────────────────────┘
```

**After (Rich Text):**

```
┌─────────────────────────────────┐
│ "Hello [waves] I'm ready"       │ ← Mixed tags, styled inline
├─────────────────────────────────┤
│ "[scans room] Something's off"  │ ← Action-heavy suggestion
├─────────────────────────────────┤
│ "Yes. [This feels wrong]"       │ ← Dialogue + thought
└─────────────────────────────────┘
```

*Note: [brackets] represent styled tag content in the visual*

## Implementation Steps

1. **Proof of Concept (Parser):**
    * Create `RichTextParser.kt`.
    * Write unit tests with mixed content strings.
   * Ensure parser can be used in both `visualTransformation` (input preview) and final rendering (
     chat bubble).
2. **Component Upgrade:**
    * Refactor `TypewriterText` to accept `AnnotatedString`.
    * Update `ChatBubble` to preprocess the message text through the parser before passing it to the
      UI.
3. **Input Field Live Preview:**
    * Extend `transformTextWithContent()` in `ChatInputView.kt` to parse and style tags in
      real-time.
    * Test that users see styled text (not raw tags) as they type.
4. **Tag Autocomplete System:**
    * Extend `detectQueryType()` to detect `<` symbol.
    * Add `ItemsType.Tags` sealed class variant.
    * Create `TagsTooltip` composable with tag list and descriptions.
    * Implement tag insertion on selection.
5. **UI Refactoring:**
    * Replace sender type buttons with tag insertion buttons in `ChatInputView.kt`.
    * Add `insertTagAtCursor()` helper function.
    * Refactor suggestions card to display rich text instead of type icons.
    * Simplify `ChatViewModel.kt` to default to `SenderType.USER`.
    * Update `ChatView.kt` to use simplified input system.
6. **Visual Polish:**
    * Implement the specific background shapes for `<action>` spans (black background, yellow text).
    * Implement starry effect for `<think>` spans.
    * Implement genre-specific inline boxes for `<narrator>` spans using `Genre.bubble(isNarrator =
      true)`.
7. **Data Model Updates:**
    * Update `Suggestion` data class to remove `type: SenderType` field (only keep `text: String`).
    * Update suggestion handling in `ChatViewModel` to default all suggestions to `SenderType.USER`.
8. **AI Prompt Updates:**
    * **`ChatPrompts.kt`**:
        - Add "TAG-BASED EXPRESSION SYSTEM" section with tag usage rules
        - Update "PRIVACY & VISIBILITY" rules for tag-based thoughts
        - Add "Action Formatting" rules (deprecate asterisks)
    * **`SuggestionPrompts.kt`**:
        - Remove type-based separation (lines 29-54)
        - Replace with mixed-tag suggestion format
        - Update examples to show rich, flowing suggestions
    * **General Guidelines**:
        - Add tag usage best practices to system instructions
9. **AI Integration Testing:**
    * Test AI's ability to generate mixed-tag messages naturally.
    * Verify NPCs ignore `<think>` tags from other characters.
    * Verify NPCs use `<action>` tags instead of asterisks.
    * Verify NPCs can express their own `<think>` tags for character development.
    * Test suggestions generate full messages with mixed tags.

## Risks & Mitigations

* **Complexity:** Stacking `RichTextParser` styles + `Wiki` annotations + `Typewriter` animation is
  complex.
    * *Mitigation:* Order of operations is critical. Parse Tags -> Typewriter Slicing -> Apply
      Wiki/Character Links.
* **Visual Noise:** Too many styles might look chaotic.
    * *Mitigation:* Restrict tags to high-impact moments. Keep the base text style clean.
