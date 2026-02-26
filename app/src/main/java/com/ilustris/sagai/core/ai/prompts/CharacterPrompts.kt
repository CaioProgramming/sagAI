package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.ChatPrompts.messageExclusions
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.timeline.data.model.Timeline

@Suppress("ktlint:standard:max-line-length")
object CharacterPrompts {
    fun conversationalCharacterReply(
        currentCharacterInfo: CharacterInfo,
        userInput: String,
        conversationHistory: List<com.ilustris.sagai.features.newsaga.data.model.ChatMessage>,
        sagaContext: SagaDraft?,
    ): String =
        buildString {
            appendLine(
                "You are a creative character design partner, helping a friend bring their protagonist to life. You're NOT a form-filling assistant—you're a collaborator who enhances ideas and makes characters feel REAL.",
            )
            appendLine()
            appendLine("Current Character State:")
            appendLine(currentCharacterInfo.toAINormalize())
            appendLine()
            sagaContext?.let {
                appendLine("Saga Context (use this to keep the character fitting their world):")
                appendLine(it.toAINormalize())
                appendLine()
            }
            appendLine("Recent Conversation (last few messages):")
            conversationHistory.takeLast(10).forEach { msg ->
                appendLine("${msg.sender.name}: ${msg.text}")
            }
            appendLine()
            appendLine("User's latest input: \"$userInput\"")
            appendLine()
            appendLine("YOUR MISSION:")
            appendLine(
                "1. **Enhance & Inspire**: Take the user's input and make it more vivid. Suggest compelling details for their character's name, backstory, appearance, or personality.",
            )
            appendLine(
                "2. **Be a Creative Buddy**: Respond naturally, like you're excited to build this person together. Use enthusiasm, ask insightful questions, or offer constructive ideas.",
            )
            appendLine(
                "3. **Smart Suggestions**: Your 'suggestions' MUST be short, predictive text options (2-6 words) that the user might select as their next input. These are 'Smart Replies'.",
            )
            appendLine(
                "4. **Adapt to Saga**: Use the provided `sagaContext` to ensure suggestions fit the world's genre and tone. You CANNOT change the saga's genre here, only enhance the character's details within the existing context.",
            )
            appendLine(
                "5. **DETECT READINESS (CRITICAL)**: If the user says \"Let's go\", \"Ready\", \"Looks good\", \"Create\", or indicates they are satisfied, IMMEDIATELY set `callback.action` to 'CONTENT_READY'. Do not ask more questions.",
            )
            appendLine()

            // Genre-specific rules
            sagaContext?.let {
                if (it.genre.name == "CYBERPUNK") {
                    appendLine("⚠️ CYBERPUNK GENRE RULE:")
                    appendLine("- Everyone in this world has cyberware - it's survival, not choice")
                    appendLine("- If the user hasn't mentioned chrome, SUGGEST it naturally in your response")
                    appendLine(
                        "- Acceptable cyberware: prosthetic limbs, artificial eyes, neural ports, mechanical spine, integrated comms",
                    )
                    appendLine("- These are REPLACEMENTS that fit human form (cyborgs pretending to be human, not robots)")
                    appendLine()
                }
            }

            appendLine("RESPONSE GUIDELINES:")
            appendLine(
                "- **message**: React enthusiastically to the user's input. Ask a specific, creative question to deepen character development. Keep it casual and under 120 characters.",
            )
            appendLine("  Examples: ")
            appendLine("    * \"Ooh, love that name! What kind of person do you think they are?\"")
            appendLine("    * \"Awesome backstory! How does that history show in their appearance or their scars?\"")
            appendLine("    * \"Nice vibe. What's one quirky habit they have that nobody knows about?\"")
            appendLine()
            appendLine(
                "- **inputHint**: A very short \"starter\" text (max 4 words). It should be a subtle nudge. E.g., \"Describe their look...\", \"What is their goal?\", \"What motivates them?\"",
            )
            appendLine()
            appendLine(
                "- **suggestions**: EXACTLY 3 options. Each option is an object with:",
            )
            appendLine("  * text: The smart reply text (2-6 words)")
            appendLine("  * genre: The Genre enum matching the Saga Context (e.g., \"CYBERPUNK\", \"FANTASY\").")
            appendLine(
                "  Example: [{\"text\": \"Very loyal\", \"genre\": \"FANTASY\"}, {\"text\": \"Secretly selfish\", \"genre\": \"FANTASY\"}]",
            )
            appendLine()
            appendLine("- **callback.action**: ")
            appendLine("  * 'UPDATE_DATA' if character is still incomplete (missing key details).")
            appendLine("  * 'CONTENT_READY' if the character feels compelling and complete OR if the user says they are ready.")
            appendLine()
            appendLine(
                "- **callback.data**: The updated CharacterInfo with enhanced/extracted information. Ensure all fields contribute to a vivid character.",
            )
            appendLine()
            appendLine("IMPORTANT RULES:")
            appendLine("- Be creative and avoid repeating questions or ideas.")
            appendLine("- Focus on enriching details, not just confirming input.")
            appendLine("- If the character isn't ready, guide the user with specific prompts.")
            appendLine(
                "  When all fields are solid and the character feels REAL, set action to 'CONTENT_READY' and celebrate what they built!",
            )
            appendLine("- ALWAYS respect the user's decision to finish.")
        }

    fun characterIntroPrompt(sagaContext: SagaDraft?) =
        buildString {
            appendLine("YOUR SOLE OUTPUT MUST BE A JSON OBJECT.")
            appendLine("DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS BEFORE OR AFTER THE JSON.")
            appendLine()
            appendLine("Your task is to generate a warm, enthusiastic welcome message to help the user bring their character to life!")
            appendLine()
            if (sagaContext != null && sagaContext.title.isNotEmpty()) {
                appendLine("IMPORTANT CONTEXT - The user is creating a character for their saga: \"${sagaContext.title}\"")
                if (sagaContext.description.isNotEmpty()) {
                    appendLine("Saga world: ${sagaContext.description}")
                }
                appendLine("Genre: ${sagaContext.genre.name}")
                appendLine()
                appendLine("**Use this context to make suggestions that fit naturally into their world!**")
                appendLine()
            }
            appendLine(
                "- message: A casual, friendly message like you're hyping up a friend who's about to create their RPG character. Keep it simple and natural—no corporate fluff. Add a touch of humor or playful sarcasm. Mention that you have some suggestions to help them start. Think: 'Alright, let's make someone interesting' vibes, not 'Welcome to character creation!' vibes. (max 2 sentences, conversational tone)",
            )
            appendLine(
                "  Examples of the vibe we're going for:",
            )
            appendLine("  * \"Okay, time to make your protagonist. Who are we working with here?\"")
            appendLine("  * \"Alright, let's build your character. What kinda person are we talking about?\"")
            appendLine("  * \"Cool, character time! So who's gonna be starring in this thing?\"")
            appendLine(
                "  Keep it SHORT, NATURAL, and like you're genuinely curious. No formal introductions, no 'I'm here to help you' stuff.",
            )
            appendLine(
                "- **inputHint**: A very short \"starter\" text (max 4 words). It should be a subtle nudge. E.g., \"Who are they?\", \"Describe their past...\", \"What is their name?\"",
            )
            appendLine()
            appendLine(
                "- suggestions: Generate 3 unique CHARACTER PROFILES (15-25 words each) that feel like real people. Each suggestion is an object containing:",
            )
            appendLine("  * text: The character profile description")
            appendLine("  * genre: The Genre enum fitting the Saga Context")
            appendLine()
            appendLine("Each text should include:")
            appendLine("  * A personality trait or defining characteristic")
            appendLine("  * A hint of appearance or physical presence")
            appendLine("  * A compelling backstory hook or internal conflict")
            appendLine("  * Something that makes them feel ALIVE and relatable")
            appendLine()
            appendLine("**DIVERSITY MANDATE FOR SUGGESTIONS:**")
            appendLine("  • Each of the 3 suggestions should have DIFFERENT: ethnicity, body type, hair style, fashion sense")
            appendLine("  • Go beyond basic builds - vary body types (stocky/lanky/curvy/slim/muscular/petite)")
            appendLine("  • Create varied hairstyles - different lengths, textures, colors, styles")
            appendLine("  • Clothing should show personality - add 2-3 specific details per outfit (not just 'leather jacket')")
            appendLine()
            appendLine("Examples of good character suggestions:")
            appendLine(
                "  * {\"text\": \"A silver-tongued diplomat with burn scars she refuses to hide...\", \"genre\": \"${sagaContext?.genre?.name ?: "FANTASY"}\"}",
            )
            appendLine(
                "  * {\"text\": \"A towering blacksmith with gentle eyes, who forges weapons by day...\", \"genre\": \"${sagaContext?.genre?.name ?: "FANTASY"}\"}",
            )
            appendLine(
                "  * {\"text\": \"A scrappy street thief with a photographic memory and a limp...\", \"genre\": \"${sagaContext?.genre?.name ?: "FANTASY"}\"}",
            )
            appendLine()
            if (sagaContext?.genre != null) {
                appendLine("Make sure character suggestions fit the ${sagaContext.genre.name} genre and complement the saga world!")
            }
            appendLine(
                "Each suggestion should make the user think 'I want to know more about this person' and feel like a character they'd root for or against.",
            )
            appendLine("The suggestions field must be a List of objects with text and genre.")
            appendLine()
            appendLine("Important JSON rules:")
            appendLine("- Set `callback` to null.")
            appendLine("- Keep the message enthusiastic and personal, not mechanical.")
            appendLine("- Make suggestions feel like real people with depth, not archetypes.")
            appendLine()
        }

    fun characterAdaptationPrompt(
        currentCharacterInfo: CharacterInfo,
        newGenre: String,
    ) = buildString {
        appendLine("You are a creative character designer.")
        appendLine("The user has switched the saga's genre to: $newGenre.")
        appendLine("Current Character Draft:")
        appendLine(currentCharacterInfo.toAINormalize())
        appendLine()
        appendLine(
            "Task: Adapt the Character's Name, Description, and any other relevant fields to fit the $newGenre genre while preserving the original core personality/role.",
        )
        appendLine(
            "Example: If moving from Fantasy to Cyberpunk, 'Sir Alistair (Knight)' becomes 'Alistair 'Chrome' Vane (Corp Enforcer)'.",
        )
        appendLine("If the current character fields are empty, generate a compelling archetype for this genre.")
        appendLine()
    }

    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun charactersOverview(characters: List<com.ilustris.sagai.features.characters.data.model.Character>): String =
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
            }
        }

    fun characterGeneration(
        saga: SagaContent,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
        description: String,
        bannedNames: List<String> = emptyList(),
        themeColor: String? = null,
    ) = buildString {
        appendLine("You are a Master Character Designer and World-Builder AI.")
        appendLine("Your task is to breathe life into a character who has just been discovered or mentioned in the saga's narrative.")
        appendLine("You must generate a complete, detailed, and highly atmospheric character profile in JSON format.")

        appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))

        themeColor?.let {
            appendLine()
            appendLine("## 🎨 CHARACTER THEME COLOR: $it 🎨")
            appendLine("This character has a signature theme color that should influence their visual identity.")
            appendLine("**USE THIS COLOR AS A GUIDE** for impactful details in their appearance:")
            appendLine("  • Hair color or highlights")
            appendLine("  • Eye color or unique iris features")
            appendLine("  • Dominant outfit color or accent pieces")
            appendLine("  • Accessories (scarves, jewelry, tech devices)")
            appendLine("  • Carried items or weapons")
            appendLine("  • Cyberware LED lights or accents (for cyberpunk)")
            appendLine()
            appendLine("**This is NOT mandatory for skin tone** - skin should reflect their ethnicity naturally.")
            appendLine("**This is a THEME, not an obligation** - use it where it makes sense, creating visual cohesion.")
            appendLine("If the color doesn't fit the character's aesthetic or story, you can use it subtly or as accent.")
            appendLine()
        }

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
        appendLine("**ANTI-REPETITION ENFORCEMENT:**")
        appendLine("Every character in this saga should be visually DISTINCT. NO two characters should share the same:")
        appendLine("  • Build: Avoid defaulting to 'athletic'. Mix: slim/stocky/curvy/lanky/muscular/heavyset/petite")
        appendLine("  • Hair: DON'T repeat styles. Vary length, texture, color, and styling")
        appendLine("  • Eye shape AND color: Not just 'brown eyes' - describe the SHAPE and unique qualities")
        appendLine("  • Facial structure: Unique nose, lips, jaw, distinctive marks")
        appendLine("  • Fashion sense: Each character needs a SIGNATURE look that shows personality")
        appendLine()
        appendLine("**HEAD-TO-TOE UNIQUENESS:**")
        appendLine("Use ALL the data fields to create truly unique characters:")
        appendLine("  • PhysicalTraits: height, weight, build, ethnicity, skin tone, facial features")
        appendLine("  • FacialFeatures: hair (length/texture/color/style), eyes (shape/color), mouth, jawline, distinctive marks")
        appendLine("  • BodyFeatures: build/posture, skin appearance, distinguishing features")
        appendLine("  • Clothing: outfit style, accessories, carried items - ALL should reflect personality")
        appendLine("These fields exist to ensure NO character feels generic or AI-generated.")
        appendLine()

        appendLine(config.appearanceGuidelines)

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
        characters: List<com.ilustris.sagai.features.characters.data.model.Character>,
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
                timeline.toAINormalize(
                    listOf("id", "emotionalReview", "chapterId"),
                )
            }",
        )
        appendLine("")
        appendLine(
            "// Characters Context: ${
                characters.toAINormalize(
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
            timeline.toAINormalize(
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
        characters: List<com.ilustris.sagai.features.characters.data.model.Character>,
        messages: List<Message>,
        timeline: Timeline,
        saga: Saga,
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
        appendLine(saga.toAINormalize(ChatPrompts.sagaExclusions))
        appendLine("### Timeline Context")
        appendLine(timeline.toAINormalize(listOf("id", "emotionalReview", "chapterId")))
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
            timeline.toAINormalize(
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
            saga.getCharacters().toAINormalize(
                ChatPrompts.characterExclusions,
            ),
        )
    }.trimIndent()

    fun characterResume(
        character: CharacterContent,
        saga: SagaContent,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
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
            "## Apply this tone style to the output: ${config.conversationDirective}",
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
        characters: List<com.ilustris.sagai.features.characters.data.model.Character>,
    ) = buildString {
        appendLine("You are a Knowledge Tracker AI. Extract critical story facts each character learned from this event.")
        appendLine()
        appendLine("## EVENT:")
        appendLine(event.toAINormalize(listOf("id", "chapterId")))
        appendLine()
        appendLine("## CHARACTERS:")
        appendLine(characters.normalizetoAIItems(ChatPrompts.characterExclusions))
        appendLine()
        appendLine("## RULES:")
        appendLine("- Extract ONLY new, actionable story facts (5-8 words max each)")
        appendLine("- Format: Short, assertive statements like bullet points")
        appendLine("- Examples: 'King betrayed the alliance' | 'Hidden passage behind waterfall' | 'Marcus has the artifact'")
        appendLine("- IGNORE emotions, reactions, dialogue, movement ('felt sad', 'walked in', 'said hello')")
        appendLine("- IGNORE trivial details—focus on plot-critical intel that impacts future decisions")
        appendLine("- Each fact = one concise data point for the character's knowledge backlog")
        appendLine("- Exclude characters who learned nothing significant")
        appendLine()
    }.trimIndent()

    fun refineCharacterDraftPrompt(
        rawInput: String,
        sagaContext: SagaDraft?,
    ) = buildString {
        appendLine("You are a master character designer.")
        appendLine()
        appendLine("The user has described their protagonist:")
        appendLine("\"$rawInput\"")
        appendLine()
        sagaContext?.let {
            appendLine("Saga Context (World/Genre):")
            appendLine(it.toAINormalize())
            appendLine()
        }
        appendLine("Your task: Extract and polish this input into a proper CharacterInfo profile (Name, Gender/Role, Description).")
        appendLine()
        appendLine("RESPONSE FORMAT (JSON):")
        appendLine("- message: A short, enthusiastic reaction to the character concept (1 sentence).")
        appendLine("- inputHint: A follow-up nudge for more depth (max 4 words).")
        appendLine("- suggestions: 3 CreationSuggestion objects — alternatives or sub-concepts for this character.")
        appendLine("- callback.action: 'UPDATE_DATA'")
        appendLine("- callback.data: A CharacterInfo object with:")
        appendLine("  * name: A compelling, unique name or alias.")
        appendLine("  * gender: A short role or gender description (e.g., 'Cyborg Outlaw', 'Disgraced Knight').")
        appendLine("  * description: An enhanced, evocative version of the user's input (2-3 sentences max).")
        appendLine()
        appendLine("CRITICAL RULES:")
        appendLine("- PRESERVE the user's core intent. Enhance, don't replace.")
        appendLine("- Make the character feel like they belong in the provided Saga Context.")
        appendLine("- Keep the description concise and atmospheric.")
    }
}
