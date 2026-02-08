# Character Knowledge & Memory System Implementation Plan

## 1. Objective

To solve the "hive mind" problem where all characters are omniscient. This plan will enable each
character to have a separate "memory" or `knowledge` base, ensuring they only know what they have
personally experienced, witnessed, or been told. This creates more realistic interactions and opens
up new narrative possibilities like secrets, discoveries, and misunderstandings.

## 2. Core Concept: Short-Term vs. Long-Term Memory

The system will operate on a two-tier memory model:

* **Short-Term Memory (Immediate Context):** This is handled by the existing message buffer (the
  last ~15 messages) passed into the AI prompts. A character remembers what was just said because
  it's in the immediate conversation history. No database changes are needed for this.
* **Long-Term Memory (Permanent Knowledge):** This represents significant facts a character has
  learned that must be retained permanently. This is what we will build.

**Why not update knowledge after every single message?**
Updating the database after every message would be inefficient and noisy. We only want to "save"
knowledge when it becomes a significant, permanent part of that character's story. The `Timeline`
update cycle is the perfect trigger for this.

## 3. The Path: Hybrid Approach (Structure & Prompts)

We will use a combination of structural code changes and prompt engineering.

* **Structure (`Character.kt`):** We will add a `knowledge` field to the `Character` data class and
  ensure the database schema is updated.
* **Prompts (`ChatPrompts.kt` & `CharacterPrompts.kt`):** We will update the AI prompts to both
  *populate* this knowledge field during narrative updates using a structured JSON object and *use*
  it during conversation.

## 4. How It Works: The Full Workflow

### Step A: Data Structure & Database (`Character.kt`)

1. **Add `knowledge` field:** A `List<String>` will be added to the `Character` data class.
2. **Generic Converter:** The existing `NicknameTypeConverter` will be renamed to
   `StringListConverter` so it can be reused for both `nicknames` and `knowledge`.
3. **Database Migration:** Since we are modifying the `Character` entity, we must ensure Room
   performs the necessary migration (or clean install) to support the new `knowledge` column.

**Example `Character.kt`:**

```kotlin
@TypeConverters(StringListConverter::class)
data class Character(
    ...
    val nicknames: List<String>? = emptyList(),
    val knowledge: List<String> = emptyList(), // e.g., ["Knows the artifact is a fake", "Witnessed the duel at sunset"]
    ...
)
```

### Step B: Knowledge Acquisition (The "Learning" Phase)

This happens when a narrative event is finalized.

1. **Trigger:** The process starts in `SagaContentManagerImpl` within the `reviewEvent()` function (
   or a similar function that runs after a `Timeline` is generated).
2. **New Use Case:** A new function will be added to `CharacterUseCase`:
   `updateCharacterKnowledge(timeline: Timeline, saga: SagaContent)`.
3. **New AI Prompt (`CharacterPrompts.knowledgeUpdatePrompt`):**
    * **Input:** The `Timeline` event and a list of characters present.
    * **Instruction:** "Analyze this event. Identify NEW facts learned by specific characters.
      Return a JSON object."
    * **Output Structure:** Use a clean, typed JSON structure (mapped to a `KnowledgeUpdateResult`
      class).

   **Expected JSON Format:**
   ```json
   {
     "updates": [
       {
         "characterName": "John",
         "learnedFacts": ["Discovered the killer's weapon", "Knows the door code is 1234"]
       },
       {
         "characterName": "Sarah",
         "learnedFacts": ["Saw John pick up the weapon"]
       }
     ]
   }
   ```

4. **Processing Logic:**
    * The `CharacterUseCase` parses this JSON.
    * It iterates through the `updates` list.
    * For each item, it calls `sagaContent.findCharacter(update.characterName)` to locate the
      correct entity.
    * It appends the `learnedFacts` to the character's existing `knowledge` list (filtering
      duplicates).
    * It calls `repository.updateCharacter()` to save the changes to the database.

### Step C: Knowledge Application (The "Remembering" Phase)

This happens every time the AI needs to generate a response.

1. **Context Injection:** Inside `replyMessagePrompt` in `ChatPrompts.kt`, we'll enhance how
   character context is provided.
2. **Targeted Knowledge:** Instead of showing all knowledge for all characters, we will be specific.
   The prompt will include a special section for the character who is speaking or being addressed.
    * **Example Prompt Snippet:**
      ```
      # ACTIVE CHARACTER CONTEXT
      ## Anya (Speaker)
      - Personality: Cautious, clever
      - Backstory: Former spy
      - Known Facts:
        - "Discovered the secret passage under the library"
        - "Is suspicious of Marcus"

      ## Marcus (In Scene)
      - Personality: Brave, reckless
      - Backstory: City guard
      - Known Facts:
        - "Believes the artifact is genuine"
      ```
3. **AI Instruction:** A new directive will be added: `StorytellingDirective.INDIVIDUAL_KNOWLEDGE`.
    * **Rule:** "A character's response MUST be consistent with their 'Known Facts'. They CANNOT
      know facts listed for other characters unless it is also in their own list. If a character has
      no specific knowledge on a topic, they should react with ignorance, curiosity, or confusion."

## 5. Token Optimization Strategy (Expert Review)

To prevent context bloat and ensure high-quality generation:

1. **Fact Granularity:** The AI will be instructed to generate *high-level* facts ("Knows the layout
   of the castle") rather than granular details ("Knows the kitchen is on the left", "Knows the
   armory is on the right").
2. **List Capping:** In `CharacterUseCase`, we should consider capping the knowledge list (e.g., max
   50 items). If it exceeds this, we might need a future "Memory Consolidation" feature where the AI
   summarizes 50 facts into 10 broader ones. For this v1, a simple First-In-First-Out or soft limit
   is acceptable, but we will monitor it.
3. **Injection Filtering:** When injecting context into `replyMessagePrompt`, we only send the
   `knowledge` list. We rely on the "Short-Term Memory" (message history) for immediate context, so
   `knowledge` should strictly be for *past* events.
4. **Deduplication:** Rigorous check to prevent storing duplicate strings.