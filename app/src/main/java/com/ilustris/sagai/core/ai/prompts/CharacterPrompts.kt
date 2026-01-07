package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.ChatPrompts.messageExclusions
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationGeneration
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.timeline.data.model.Timeline

object CharacterPrompts {
    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun charactersOverview(characters: List<Character>): String =
        buildString {
            val characterExclusions =
                listOf(
                    "id",
                    "image",
                    "sagaId",
                    "joinedAt",
                    "details",
                    "events",
                    "relationshipEvents",
                    "relationshipsAsFirst",
                    "relationshipsAsSecond",
                    "physicalTraits",
                    "hexColor",
                    "firstSceneId",
                    "emojified",
                    "smartZoom",
                )
            appendLine("CURRENT SAGA CAST OVERVIEW:")
            characters.forEach { character ->
                appendLine(character.name)
                appendLine(character.toAINormalize(characterExclusions))
                if (character.knowledge.isNotEmpty()) {
                    appendLine("  knowledge: ${character.knowledge}")
                }
            }
        }

    fun characterGeneration(
        saga: SagaContent,
        description: String,
        bannedNames: List<String> = emptyList(),
    ) = buildString {
        appendLine("You are a Master Character Designer and World-Builder AI.")
        appendLine("Your task is to breathe life into a character who has just been discovered or mentioned in the saga's narrative.")
        appendLine("You must generate a complete, detailed, and highly atmospheric character profile in JSON format.")

        appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))

        appendLine("## 🧬 THE DISCOVERY SEED (FOUNDATIONAL CONTEXT) 🧬")
        appendLine("// The following description contains the names, roles, or snippets of dialogue that introduced this character.")
        appendLine("// Use this as your ABSOLUTE SOURCE for the character's initial identity (name, if provided).")
        appendLine(description)
        appendLine()

        if (bannedNames.isNotEmpty()) {
            appendLine("## 🚫 BANNED NAMES (CREATIVITY CHALLENGE) 🚫")
            appendLine(
                "To ensure a diverse and expansive universe, the following FULL NAMES are already in use by other characters across different sagas and should be AVOIDED unless explicitly requested in the 'DISCOVERY SEED' above:",
            )
            appendLine(bannedNames.joinToString(", "))
            appendLine("// RULE: If the 'DISCOVERY SEED' contains a name from this list, YOU MUST USE IT (context overrides the ban).")
            appendLine(
                "// RULE: If the 'DISCOVERY SEED' does not provide a specific name, you MUST generate a completely new, unique name that is NOT on this list.",
            )
            appendLine(
                "// RULE: You CAN reuse LAST NAMES from this list ONLY IF the 'DISCOVERY SEED' or the 'LATEST CONVERSATION HISTORY' describes a family relationship (e.g., siblings, marriage, parent) between the new character and an existing one.",
            )
            appendLine()
        }

        appendLine("## 🎭 CONTEXTUAL REASONING: BEYOND THE WORDS 🎭")
        appendLine("To make this character feel authentic, you must analyze the narrative they emerged from:")
        appendLine(
            "1. **The Voice:** If they just spoke in the history below, replicate their specific speech patterns, vocabulary, and tone in their personality and backstory.",
        )
        appendLine(
            "2. **The Relationship:** How did they interact with the protagonist or other NPCs? Are they a threat, an ally, a professional contact, or a mysterious observer?",
        )
        appendLine(
            "3. **The Role:** Based on the saga genre and current location, what is their logical occupation and status? (e.g., a dusty desert settlement suggests a scavenger or a weary trader, not a pristine bureaucrat).",
        )
        appendLine()

        appendLine("## 🎨 CREATIVE DIRECTION: IDENTITY & VIBRANCY 🎨")
        appendLine(
            "The character must feel ALIVE. Do not rely on generic tropes. Captivating characters are built on **specific, unique details**.",
        )
        appendLine(
            "- **Visual Storytelling:** Use clothing and physical traits to hint at backstory. Why is the jacket torn? What does that symbol on their belt mean?",
        )
        appendLine("- **Unique Identifiers:** Give them a signature look. Avoid 'standard' gear unless modified in a unique way.")
        appendLine(
            "- **Sensory Details:** Mention textures, distinct colors, and conditions (dusty, polished, bloodied) to create a vivid mental image.",
        )
        appendLine()

        appendLine("## ✨ RADICAL DIVERSITY & UNIQUE PERSONAS ✨")
        appendLine("Break away from common standards. Your characters should reflect a rich, global range of human and humanoid forms.")
        appendLine(
            "- **Ethnicity & Style**: Actively cycle through underrepresented ethnicities and unique aesthetics (e.g., 'electric lavender tight coils', 'weathered, sun-carved bronze skin').",
        )
        appendLine(
            "- **Silhouette**: Create diverse body shapes and postures that reflect their life journey (e.g., 'a stout, powerful matriarch', 'a lanky, nervous tech-junkie').",
        )
        appendLine()

        appendLine(GenrePrompts.appearanceGuidelines(saga.data.genre))

        appendLine("\n## 📖 LATEST CONVERSATION HISTORY (FOR BEHAVIORAL SEEDING) 📖")
        appendLine("// Use these messages to capture the character's 'voice' and recent narrative impact.")
        appendLine(
            saga
                .flatMessages()
                .sortedByDescending { it.message.timestamp }
                .take(5)
                .map { it.message }
                .normalizetoAIItems(excludingFields = messageExclusions),
        )

        appendLine("\n## 📜 CHARACTER PROFILE GUIDELINES 📜")
        appendLine(CharacterGuidelines.creationGuideline)
        appendLine(
            "\n// **FINAL MANDATE**: The character must be an IRREPLACEABLE piece of the ${saga.data.genre.name} world. They are not a background extra; they are a living soul.",
        )
    }.trimIndent()

    fun characterLoreGeneration(
        timeline: Timeline,
        characters: List<Character>,
    ) = buildString {
        appendLine(
            "You are a narrative AI assistant tasked with tracking individual character progression based on specific timeline events.",
        )
        appendLine("The 'Current Timeline Event' below describes a recent occurrence in the saga.")
        appendLine("The 'List of Characters in Saga' provides context on all characters currently part of the story.")
        appendLine("")
        appendLine("// CORE OBJECTIVE: Extract and summarize individual character events from a narrative.")
        appendLine("")
        appendLine("// --- CONTEXT ---")
        appendLine(
            "// TimelineContext: ${
                timeline.toJsonFormatExcludingFields(
                    listOf("id", "emotionalReview", "chapterId"),
                )
            }",
        )
        appendLine("")
        appendLine(
            "// Characters Context: ${
                characters.toJsonFormatExcludingFields(
                    fieldsToExclude = ChatPrompts.characterExclusions,
                )
            }",
        )
        appendLine("// --- STEP-BY-STEP INSTRUCTIONS ---")
        appendLine("// Follow these steps rigorously to generate the JSON array:")
        appendLine("")
        appendLine(
            "// 1. ANALYSIS: Carefully read the 'TimelineContext' and identify which characters from the 'Characters Context' were **directly involved** or **significantly impacted** by the event.",
        )
        appendLine("// 2. FILTERING: Exclude characters that had no discernible role.")
        appendLine(
            "// 3. GENERATION: For EACH identified character, write a brief 'description' and a relevant 'title', focusing ONLY on the events described in the 'TimelineContext'.",
        )
        appendLine("// 4. ASSEMBLY: Construct the JSON array with the character objects.")
        appendLine("")
        appendLine("// --- CURRENT EVENT ---")
        appendLine("// TimelineContext:")
        appendLine(
            timeline.toJsonFormatExcludingFields(
                listOf(
                    "id",
                    "emotionalReview",
                    "chapterId",
                ),
            ),
        )
    }.trimIndent()

    @Suppress("ktlint:standard:max-line-length")
    fun findNickNames(
        characters: List<Character>,
        messages: List<Message>,
        data: Timeline,
        data1: Saga,
    ) = buildString {
        appendLine(
            "You are a linguistic analyst AI. Your task is to analyze a conversation and identify unique, context-specific nicknames for characters.",
        )
        appendLine(
            "Your goal is to find informal names that have emerged from the characters' interactions and story events. You can identify current nicknames or suggest new ones that could replace older, less relevant ones.",
        )
        appendLine()
        appendLine("## CORE INSTRUCTIONS:")
        appendLine(
            "1. **Primary Goal: Identify 'earned' or 'spoken' nicknames.** Your main objective is to find nicknames that characters have either been directly called by others or have earned through their actions and role in the story.",
        )
        appendLine()
        appendLine("2. **Analyze for Direct Mentions:**")
        appendLine(
            "   - Scrutinize the 'Recent Messages' for instances where a character is referred to by a name other than their official one.",
        )
        appendLine(
            "   - **Example 1 (Shortened Name):** If a character named 'Daniela' is frequently called 'Dani' by her friends in the conversation, 'Dani' is a valid nickname.",
        )
        appendLine(
            "   - **Example 2 (Title/Hero Name):** If a character is a hero and another character says, 'We need Superwave for this mission!', then 'Superwave' is a valid nickname, provided it's not already in their official profile.",
        )
        appendLine()
        appendLine("3. **Analyze for Earned Nicknames & Contextual Relevance:**")
        appendLine(
            "   - A nickname must be deeply rooted in the events of the story. It is not a random guess, but a name that logically emerges from a character's actions, personality, or a pivotal moment.",
        )
        appendLine(
            "   - Ask yourself: Why was this name used? Does it reflect a new status, a term of endearment, an insult, or a legendary title earned in the narrative? The connection to the story must be strong and clear.",
        )
        appendLine()
        appendLine("4. **Suggesting Creative & Relevant New Nicknames:**")
        appendLine(
            "   - If the story shows significant character development (e.g., a character becomes a legendary warrior) but no one has explicitly used a nickname yet, you can **suggest a creative and fitting nickname** that reflects this new status.",
        )
        appendLine("   - The suggestion must be a logical and creative leap based on the provided context, not a generic label.")
        appendLine()
        appendLine("5. **CRITICAL EXCLUSIONS (What to Avoid):**")
        appendLine(
            "   - **No Generic Roles:** Do NOT extract common nouns or jobs (e.g., \"the girl\", \"ninja\", \"captain\", \"the doctor\"). A nickname is a specific name, not a description.",
        )
        appendLine(
            "   - **No Profile Attributes:** Do NOT use information already present in the character's official profile (like their `occupation`, existing `nicknames`, or base `name`). You are looking for *new*, *emergent*, or *informally used* names from the conversation.",
        )
        appendLine(
            "   - **No Guessing:** The nickname must be directly present in the messages or a very strong, logical inference from the character's recent actions and development in the story.",
        )
        appendLine()
        appendLine("6. **Output Constraints:**")
        appendLine(
            "   - For each character, identify or suggest a **maximum of four** nicknames. Prioritize the most relevant and impactful ones.",
        )
        appendLine()
        appendLine("## CONTEXT:")
        appendLine("### Saga Context")
        appendLine(data1.toAINormalize(ChatPrompts.sagaExclusions))
        appendLine("### Timeline Context")
        appendLine(data.toAINormalize(listOf("id", "emotionalReview", "chapterId")))
        appendLine("### Characters List (Official Names):")
        appendLine(characters.normalizetoAIItems(ChatPrompts.characterExclusions))
        appendLine()
        appendLine("### Recent Messages (Conversation to Analyze):")
        appendLine(messages.normalizetoAIItems(messageExclusions))
        appendLine()
        appendLine("## REQUIRED OUTPUT FORMAT:")
        appendLine("Respond ONLY with a valid JSON array. Do not include any other text, explanations, or markdown.")
        appendLine(
            "The JSON array must follow this exact format: `[{\"characterName\": \"Character Full Name\", \"newNicknames\": [\"nickname1\", \"nickname2\"]}]`",
        )
        appendLine(
            "- Only include characters for whom you found or can suggest one or more valid, unique nicknames that meet the criteria above.",
        )
        appendLine("- Each character's `newNicknames` array should contain a maximum of 4 strings.")
        appendLine("- If no new, valid nicknames are found for any character, return an empty array `[]`.")
    }.trimIndent()

    fun generateCharacterRelation(
        timeline: Timeline,
        saga: SagaContent,
    ) = buildString {
        appendLine("You are an expert narrative analyst and relationship extractor AI.")
        appendLine("")
        appendLine(
            "GOAL: Analyze the provided Timeline Event and the list of Characters and determine if there is any relationship established between any two characters based on this specific event (or solidifying an ongoing bond). If there are one or more relationships, output them strictly as a JSON array following the required schema below. If no relationship can be inferred, output an empty JSON array [].",
        )
        appendLine("")
        appendLine("IMPORTANT CONSTRAINTS:")
        appendLine("- Output MUST be ONLY valid JSON. No prose, no markdown, no explanations.")
        appendLine("- Use EXACT character names as they appear in the Characters list (case-sensitive match required for best results).")
        appendLine("- Title: short and assertive (3–6 words max).")
        appendLine("- Description: concise, 1–2 sentences.")
        appendLine(
            "- Emoji (relationEmoji): choose an emoji that reflects the relationship feeling (e.g., 🤝 allies, ❤️ love, 💔 heartbreak, ⚔️ rivalry, 🛡️ protector, 🧪 tension, 💫 admiration, 😠 conflict, 🧠 mentorship, 🕊️ truce, 🌀 complicated, 🌩️ betrayal). One emoji only.",
        )
        appendLine("- Do not fabricate characters not present in the Characters list.")
        appendLine("- Prefer relationships that are explicitly or strongly implied by the Timeline Event.")
        appendLine("")
        appendLine("REQUIRED OUTPUT SCHEMA (JSON array of objects):")
        appendLine("[")
        appendLine("   ${toJsonMap(RelationGeneration::class.java)}")
        appendLine("]")
        appendLine("")
        appendLine("FIELD DEFINITIONS (for precision):")
        appendLine("- firstCharacter: First character's name (string). Must match EXACTLY a name from Characters list.")
        appendLine(
            "- secondCharacter: Second character's name (string). Must match EXACTLY a name from Characters list. Must not be the same as firstCharacter.",
        )
        appendLine("- relationEmoji: Single emoji symbol that best represents the relationship feeling.")
        appendLine("- title: Very short, assertive label (3–6 words max) capturing the essence of the relationship.")
        appendLine(
            "- description: A brief, clear explanation (1–2 sentences) describing how this event establishes or changes their relationship.",
        )
        appendLine("")
        appendLine("STEP-BY-STEP INSTRUCTIONS:")
        appendLine(
            "1) Read the Timeline Event carefully and identify interactions, emotional beats, support/opposition, trust/distrust, alliances, mentorship, romance, betrayal, rivalry, etc.",
        )
        appendLine("2) Cross-check mentions with the Characters list and ensure all names used are exactly as listed.")
        appendLine("3) For each strong relationship signal, create one object following the schema.")
        appendLine("4) Keep the title short and assertive; keep the description 1–2 sentences; pick a fitting emoji.")
        appendLine("5) If multiple distinct relationships are present, include multiple objects in the array. If none, return [].")
        appendLine("")
        appendLine("CONTEXT:")
        appendLine("Timeline Event:")
        appendLine(
            timeline.toJsonFormatExcludingFields(
                listOf(
                    "id",
                    "emotionalReview",
                    "chapterId",
                ),
            ),
        )
        appendLine("")
        appendLine("Characters (names must be used EXACTLY as listed):")
        appendLine(
            saga.getCharacters().toJsonFormatExcludingFields(
                ChatPrompts.characterExclusions,
            ),
        )
    }.trimIndent()

    fun characterResume(
        character: CharacterContent,
        saga: SagaContent,
    ) = buildString {
        val characterData = character.data
        appendLine("You are a master storyteller and narrative biographer.")
        appendLine(
            "Your task is to write a concise, atmospheric, and highly engaging resume of a character's journey in the saga.",
        )
        appendLine("This resume should validate their background, personality, and the major events they've experienced.")
        appendLine()
        appendLine("## SAGA CONTEXT")
        appendLine(SagaPrompts.mainContext(saga, character))
        appendLine()
        appendLine("## CHARACTER IDENTITY")
        appendLine("Name: ${characterData.name} ${characterData.lastName ?: ""}")
        appendLine("Personality: ${characterData.profile.personality}")
        appendLine(
            "Visual Profile: ${characterData.details.physicalTraits.ethnicity} ${characterData.details.physicalTraits.gender}, ${characterData.details.physicalTraits.race}. ${characterData.details.physicalTraits.facialDetails.hair} hair, ${characterData.details.physicalTraits.facialDetails.eyes} eyes. ${characterData.details.physicalTraits.bodyFeatures.buildAndPosture}.",
        )
        appendLine("Style: ${characterData.details.clothing.outfitDescription}")
        appendLine()
        appendLine("## THE JOURNEY SO FAR (KEY EVENTS)")
        if (character.events.isEmpty()) {
            appendLine("The character has just joined the story and hasn't experienced major events yet.")
        } else {
            character.events.sortedByDescending { it.event.createdAt }.take(15).forEach { event ->
                appendLine("- ${event.event.title}: ${event.event.summary}")
            }
        }
        appendLine()
        appendLine("## RELATIONSHIPS")
        if (character.relationships.isEmpty()) {
            appendLine("The character currently has no established significant relationships.")
        } else {
            character.relationships.forEach { relation ->
                val other = relation.getCharacterExcluding(character.data)
                appendLine("- ${relation.data.title} with ${other.name} ${relation.data.emoji}: ${relation.data.description}")
            }
        }
        appendLine()
        appendLine("## INSTRUCTIONS")
        appendLine("1. Write a single, cohesive, and compelling paragraph (max 180 words).")
        appendLine("2. Blend their physical presence and distinctive personality with their narrative arc.")
        appendLine("3. Focus on how the character has changed, matured, or stayed true to their essence through these events.")
        appendLine("4. Use an atmospheric tone that perfectly matches the saga's genre.")
        appendLine(
            "## Apply this tone style to the output: ${
                GenrePrompts.conversationDirective(
                    saga.data.genre,
                )
            }",
        )
        appendLine("5. Mention at least one key relationship if it's pivotal to their development.")
        appendLine("6. Transform the raw list of events into a flowing narrative summary.")
        appendLine(
            "7. The goal is to give the reader a deep understanding of 'who this character is now' in the context of the ongoing story.",
        )
        appendLine("8. Respond ONLY with the resume text. No intro, no outro.")
    }.trimIndent()

    fun knowledgeUpdatePrompt(
        event: Timeline,
        characters: List<Character>,
    ) = buildString {
        appendLine(
            "You are a Character Development AI. Your task is to update the 'knowledge' of specific characters based on a recent event.",
        )
        appendLine("We need to know what NEW significant facts each character has learned.")
        appendLine()
        appendLine("## INPUT DATA")
        appendLine("### The Event (Timeline):")
        appendLine(event.toJsonFormatExcludingFields(listOf("id", "chapterId")))
        appendLine()
        appendLine("### Characters Present:")
        appendLine(characters.normalizetoAIItems(ChatPrompts.characterExclusions))
        appendLine()
        appendLine("## INSTRUCTIONS")
        appendLine("1. Analyze the event description/content.")
        appendLine("2. For each character listed above, determine if they learned any *NEW, PERMANENT, and SIGNIFICANT* facts.")
        appendLine("   - Significant: 'The King is dead', 'Anya has the key', 'The monster is weak to fire'.")
        appendLine("   - Trivial (IGNORE): 'Walked to the door', 'Said hello', 'Is feeling sad'.")
        appendLine("3. If a character wasn't involved or learned nothing new, exclude them.")
        appendLine("4. Return a JSON map where the Key is the Character Name and the Value is a List of Strings (the new facts).")
        appendLine()
        appendLine("## OUTPUT FORMAT")
        appendLine("Strictly JSON format: { \"CharacterName\": [\"Fact 1\", \"Fact 2\"] }")
        appendLine("Example: { \"Anya\": [\"Discovered the secret passage\"], \"Marcus\": [\"Learned that Anya is a spy\"] }")
    }.trimIndent()
}
