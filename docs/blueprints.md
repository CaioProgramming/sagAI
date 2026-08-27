# Sagas AI Prompt Blueprints

This document tracks all the AI prompt blueprints used in the Sagas application. Each blueprint is stored in Firebase Remote Config with a `_blueprint` suffix and follows a modular structure: **Role**, **System Directives**, **Rules**, and **Template**.

## Design Principles
- **Agnostic Core:** Role, Directives, and Rules should be context-free.
- **Dynamic Templates:** All variable data (Saga context, history, character names) must be injected via the `template` section.
- **Tag-Based Expression:** Use `<action>`, `<think>`, and `<narrator>` tags for richer character prose.
- **Consistent Nomenclature:** Firebase keys must end with `_blueprint`.

## Blueprint Registry

| Blueprint Key | Description | Category | Implemented? |
| :--- | :--- | :--- | :---: |
| `dynamic_saga_creation_blueprint` | Generates CTA cards to inspire new story creation. | Home | ✅ |
| `reply_generation_blueprint` | The "Storyteller"—main narrative generation engine. Also receives `maxMessageLimit` (from the `chat_input_limit` parameter) and must compose within that character ceiling instead of writing long and cutting. | Chat | ✅ |
| `epilogue_chat_intro_blueprint` | Ephemeral post-ending epilogue chat—character's opening line, warm reunion, never advances plot. | Chat | 📝 |
| `epilogue_chat_reply_blueprint` | Ephemeral post-ending epilogue chat—ongoing replies, never advances plot, never persisted. | Chat | 📝 |
| `chat_writing_pal_blueprint` | The "Ghostwriter"—checks messages for character style/typos. | Chat | ✅ |
| `chat_reaction_blueprint` | The "Emote"—quick NPC reactions (emojis and short thoughts). | Chat | ✅ |
| `scene_summarization_blueprint` | The "Analyst"—extracts factual states for continuity. | Chat | ✅ |
| ~~`chat_notification_blueprint`~~ | Deprecated—hook is now generated inline as `sceneSummary.notificationHook` by `reply_generation_blueprint`. | Chat | 🗑️ |
| `emotional_tone_extraction_blueprint` | Identifies dominant emotional tones in user messages. | Emotional | ✅ |
| `emotional_review_blueprint` | Generates clincial behavioral notes for analysis. | Emotional | ✅ |
| `emotional_conclusion_blueprint` | Generates an empathetic final reflection to the player. | Emotional | ✅ |
| `emotional_profile_blueprint` | Provides constructive, friendly feedback on playstyle. | Emotional | ✅ |
| `wiki_generation_blueprint` | Extracts and structures lore entries from story events. | Wiki | ✅ |
| `merge_wiki_blueprint` | Consolidates and merges redundant wiki entries. | Wiki | ✅ |
| `act_conclusion_blueprint` | Provides cinematic closure and insights for an Act. | Act | ✅ |
| `act_introduction_blueprint` | Sets the stage and objectives for a new Act. | Act | ✅ |
| `acts_overview_blueprint` | Summary of all completed acts for context. | Act | 📝 |
| `chapter_introduction_blueprint` | Brief hook to introduce a new chapter. | Chapter | ✅ |
| `chapter_generation_blueprint` | The "Cinematic Narrator"—full chapter prose generation. | Chapter | ✅ |
| `lore_generation_blueprint` | The "Chronicler"—translates chat into permanent lore. | Lore | ✅ |
| `saga_end_credits_blueprint` | Synthesis of the entire saga's emotional and narrative arc. | Saga | ✅ |
| `icon_description_blueprint` | The "Art Director"—generates visual prompts for AI art. | Saga | ✅ |
| `review_generation_blueprint` | Aggregates metrics and feedback for a saga review. | Saga | ✅ |
| `story_briefing_blueprint` | The "Librarian"—summarizes the current state for the player. | Saga | ✅ |
| `saga_resume_blueprint` | Condensed summary for players returning to a saga. | Saga | ✅ |
| `initial_saga_kickoff_blueprint` | Generates the initial story hook when creating a saga. | Saga | ✅ |
| `conversational_character_reply_blueprint` | Handles chat during character discovery and creation. | Character | ✅ |
| `creation_intro_blueprint` | Introduces the multiverse during the new saga flow. | Character | ✅ |
| `character_adaptation_blueprint` | Adapts character drafts to fit different genres. | Character | 📝 |
| `character_generation_blueprint` | Orchestrates the creation of detailed character profiles. | Character | ✅ |
| `character_lore_blueprint` | Extracts character-specific backstory from events. | Character | ✅ |
| `character_nickname_blueprint` | Suggests emergent nicknames based on NPC interaction. | Character | ✅ |
| `character_relation_blueprint` | Maps relationship shifts following significant events. | Character | ✅ |
| `character_resume_blueprint` | Generates a final report for a character's journey. | Character | ✅ |
| `knowledge_update_blueprint` | Updates what a character "knows" based on recent events. | Character | ✅ |
| `refine_character_draft_blueprint` | Polishes raw user input into a refined character draft. | Character | ✅ |
| `refine_saga_draft_blueprint` | Polishes raw user input into a refined saga draft. | Saga | ✅ |
| `creation_flow_assist_blueprint` | Multi-step assistance during saga creation. | Saga | ✅ |
| `saga_process_interlude_blueprint` | Flavor messages during AI generation states. | Saga | ✅ |
| `review_introduction_blueprint` | Polishes initial idea into a cinematic concept. | Review | ✅ |
| `review_playstyle_blueprint` | Analyzes and ribs the player about their habits. | Review | ✅ |
| `review_expressiveness_blueprint` | Reflects on the player's emotional signature. | Review | ✅ |
| `review_connections_blueprint` | Analyzes character bonds and favoritism. | Review | ✅ |
| `review_acts_insight_blueprint` | Reflects on the world's status and legacy. | Review | ✅ |
| `review_conclusion_blueprint` | Final nostalgic farewell in the review flow. | Review | ✅ |
| `saga_input_suggestions_blueprint` | Generates 3 creative action/dialogue choices for players. | Suggestion | ✅ |
| `milestone_congrats_blueprint` | Witty, sass-filled achievement reactions. | Milestone | ✅ |
| `loading_message_blueprint` | Ironic loading screen snippets by genre. | Milestone | ✅ |
| `new_character_milestone_blueprint`| Narrative reaction to a character joining the team. | Milestone | ✅ |
| `intro_milestone_blueprint` | Cinematic "Previously on" state synthesis. | Milestone | ✅ |
| `share_playstyle_blueprint` | Catchy slogans for sharing playstyle cards. | Share | ✅ |
| `share_emotional_blueprint` | Poetic reflection for shareable emotion cards. | Share | ✅ |
| `share_history_blueprint` | Movie teaser style taglines for sharing. | Share | ✅ |
| `share_relations_blueprint` | Taglines about relationship tension for sharing. | Share | ✅ |
| `share_character_blueprint` | Impactful snapshot of a character for sharing. | Share | ✅ |
| `faq_ask_ai_blueprint` | The "Voice of Sagas" for technical and lore support. | FAQ | ✅ |

**Legend:**
- ✅ : Fully refactored to use `PromptService` and Remote Config.
- 📝 : Refactor pending (currently hardcoded).
- 🗑️ : Deprecated—safe to remove the Remote Config parameter.

---
*Last updated: March 17, 2026*
