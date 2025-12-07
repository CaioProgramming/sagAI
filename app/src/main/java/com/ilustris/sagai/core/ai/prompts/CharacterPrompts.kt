package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.ChatPrompts.messageExclusions
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
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
                )
            appendLine("CURRENT SAGA CAST OVERVIEW:")
            appendLine(characters.normalizetoAIItems(characterExclusions))
        }

    fun characterGeneration(
        saga: SagaContent,
        description: String,
    ) = buildString {
        appendLine("You are an expert character designer for a story.")
        appendLine(
            "Your task is to create a complete and detailed character profile in JSON format based on the provided saga context and a source description.",
        )
        appendLine("The generated character MUST be deeply contextualized within the saga's theme, genre, and narrative.")

        appendLine(SagaPrompts.mainContext(saga))

        appendLine("## 💡 NEW CHARACTER CREATION INSPIRATION 💡")
        appendLine("// The following description is the foundational concept for the new character.")
        appendLine("// Your task is to bring this character to life by expanding on the provided details, filling in the blanks, and adding creative depth to make them a truly unique and complete persona.")
        appendLine("// You should be creative and add details that are not explicitly mentioned, for example, suggesting a last name if one is not provided, or elaborating on their motivations and backstory.")
        appendLine("// The goal is to create a rich, well-rounded character that feels authentic to the saga's world.")
        appendLine(
            GenrePrompts.appearanceGuidelines(saga.data.genre)
        )
        appendLine(description)

        appendLine("// Latest messages for better context")
        appendLine("Conversation History")
        appendLine("Use this for contextualization")
        appendLine("The messages are ordered from newest to oldest")
        appendLine(
            saga.flatMessages().reversed().take(5).normalizetoAIItems(excludingFields = messageExclusions),
        )
        appendLine("## Guidelines for generating the character JSON:")
        appendLine(CharacterGuidelines.creationGuideline)
        appendLine("// **IMPORTANT**: The character must be HIGHLY CONTEXTUALIZED TO THE SAGA'S THEME: ${saga.data.genre.name}.")
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
                    fieldsToExclude =
                        listOf(
                            "details",
                            "id",
                            "image",
                            "hexColor",
                            "sagaId",
                            "joinedAt",
                        ),
                )
            }",
        )
        appendLine("")
        appendLine("// --- MANDATORY OUTPUT STRUCTURE ---")
        appendLine("// The output MUST be a JSON array. The response must NOT include any additional text, explanations, or parentheses.")
        appendLine("// The structure of each object in the array MUST follow this exact format:")
        appendLine("/*")
        appendLine("[")
        appendLine("  {")
        appendLine("    \"characterName\": \"Character Name\",")
        appendLine("    \"description\": \"A concise update (1-2 sentences) about the character's actions or impact in this event.\",")
        appendLine("    \"title\": \"Short, descriptive title for the character's event.\"")
        appendLine("  },")
        appendLine("  {")
        appendLine("    \"characterName\": \"Another Character\",")
        appendLine("    \"description\": \"Update about the second character.\",")
        appendLine("    \"title\": \"Event title for this character.\"")
        appendLine("  }")
        appendLine("]")
        appendLine("*/")
        appendLine("")
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
        appendLine("")
        appendLine("// Characters Context:")
        appendLine(
            characters.toJsonFormatExcludingFields(
                fieldsToExclude =
                    listOf(
                        "details",
                        "id",
                        "image",
                        "hexColor",
                        "sagaId",
                        "joinedAt",
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
        appendLine("1. **Primary Goal: Identify 'earned' or 'spoken' nicknames.** Your main objective is to find nicknames that characters have either been directly called by others or have earned through their actions and role in the story.")
        appendLine()
        appendLine("2. **Analyze for Direct Mentions:**")
        appendLine("   - Scrutinize the 'Recent Messages' for instances where a character is referred to by a name other than their official one.")
        appendLine("   - **Example 1 (Shortened Name):** If a character named 'Daniela' is frequently called 'Dani' by her friends in the conversation, 'Dani' is a valid nickname.")
        appendLine("   - **Example 2 (Title/Hero Name):** If a character is a hero and another character says, 'We need Superwave for this mission!', then 'Superwave' is a valid nickname, provided it's not already in their official profile.")
        appendLine()
        appendLine("3. **Analyze for Earned Nicknames & Contextual Relevance:**")
        appendLine("   - A nickname must be deeply rooted in the events of the story. It is not a random guess, but a name that logically emerges from a character's actions, personality, or a pivotal moment.")
        appendLine("   - Ask yourself: Why was this name used? Does it reflect a new status, a term of endearment, an insult, or a legendary title earned in the narrative? The connection to the story must be strong and clear.")
        appendLine()
        appendLine("4. **Suggesting Creative & Relevant New Nicknames:**")
        appendLine("   - If the story shows significant character development (e.g., a character becomes a legendary warrior) but no one has explicitly used a nickname yet, you can **suggest a creative and fitting nickname** that reflects this new status.")
        appendLine("   - The suggestion must be a logical and creative leap based on the provided context, not a generic label.")
        appendLine()
        appendLine("5. **CRITICAL EXCLUSIONS (What to Avoid):**")
        appendLine("   - **No Generic Roles:** Do NOT extract common nouns or jobs (e.g., \"the girl\", \"ninja\", \"captain\", \"the doctor\"). A nickname is a specific name, not a description.")
        appendLine("   - **No Profile Attributes:** Do NOT use information already present in the character's official profile (like their `occupation`, existing `nicknames`, or base `name`). You are looking for *new*, *emergent*, or *informally used* names from the conversation.")
        appendLine("   - **No Guessing:** The nickname must be directly present in the messages or a very strong, logical inference from the character's recent actions and development in the story.")
        appendLine()
        appendLine("6. **Output Constraints:**")
        appendLine("   - For each character, identify or suggest a **maximum of four** nicknames. Prioritize the most relevant and impactful ones.")
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
                listOf(
                    "id",
                    "image",
                    "hexColor",
                    "details",
                    "sagaId",
                    "joinedAt",
                    "profile",
                ),
            ),
        )
    }.trimIndent()
}
