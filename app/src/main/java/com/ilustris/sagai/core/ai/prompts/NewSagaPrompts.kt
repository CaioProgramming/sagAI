package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.usecase.SagaProcess
import com.ilustris.sagai.features.newsaga.ui.presentation.FlowPages

@Suppress("ktlint:standard:max-line-length")
object NewSagaPrompts {
    fun conversationalSagaReply(
        currentSagaDraft: SagaDraft,
        userInput: String,
        conversationHistory: List<ChatMessage>,
        availableVariations: Map<String, GenreConfig.VariationConfig> = emptyMap(),
        companion: GenreConfig.CompanionConfig? = null,
    ): String =
        buildString {
            appendLine(
                companion?.persona
                    ?: "You are a passionate, brainstorming creative partner. Your goal is to help the user KICKSTART an amazing saga. You are NOT a form-filler; you are a co-creator.",
            )
            appendLine()
            appendLine("Current Saga Context:")
            appendLine(currentSagaDraft.toAINormalize())
            appendLine()
            appendLine("Conversation History:")
            conversationHistory.takeLast(10).forEach { msg ->
                appendLine("${msg.sender.name}: ${msg.text}")
            }
            appendLine()
            appendLine("User's latest input: \"$userInput\"")
            appendLine()
            appendLine("YOUR OBJECTIVES:")
            appendLine(
                "1. **Listen & Adapt**: Analyze the user's input. Does it change the vibe? If they describe a cyberpunk city but the genre is 'FANTASY', CHANGE the genre to 'CYBERPUNK' in your response data.",
            )
            appendLine(
                "2. **Be the Spark**: Don't just ask \"what next?\". Offer cool ideas. Be excited. If they say \"space pirates\", you say \"Yes! Are they searching for lost tech or running from the law?\"",
            )
            appendLine(
                "3. **Smart Suggestions**: Your 'suggestions' must be short, predictive replies that the USER might want to say next. Help them complete their thought or answer your question.",
            )
            appendLine(
                "4. **Abstract & Validate**: If the user gives a vague idea (e.g., \"a story about a lost king\"), FILL in the gaps yourself for the Description. Don't ask for every detail.",
            )
            appendLine(
                "5. **DETECT READINESS (CRITICAL)**: If the user says \"Let's go\", \"Start\", \"Perfect\", \"I like it\", or implies they are happy with the current state, IMMEDIATELY set `callback.action` to 'CONTENT_READY'. Do not ask more questions.",
            )
            appendLine()
            appendLine("RESPONSE FIELDS:")
            appendLine(
                "- **message**: A short, engaging reply (max 2 sentences). Be conversational, like a friend. Ask a specific, inspiring question to drive the story forward.",
            )
            if (availableVariations.isNotEmpty()) {
                appendLine()
                appendLine("AVAILABLE WORLD VARIATIONS (Sub-themes):")
                availableVariations.forEach { (id, config) ->
                    appendLine("- **$id**: ${config.name} - ${config.description}")
                }
                appendLine()
                appendLine(
                    "OBJECTIVE: Based on the user's input, if one of these variations fits the 'vibe' better than the base genre, pick it and set it in the `variationId` field of the returned SagaDraft.",
                )
            }
            appendLine(
                "- **inputHint**: A very short \"starter\" text (max 4 words). It should be a subtle nudge, not a full sentence. E.g., \"Describe the world...\", \"Who is the hero?\", \"What happens next?\"",
            )
            appendLine(
                "- **suggestions**: EXACTLY 3 options. Each option is an object with:",
            )
            appendLine("  * text: The smart reply text (2-6 words)")
            appendLine(
                "  * genre: The most fitting Genre enum for this suggestion (e.g., \"FANTASY\", \"CYBERPUNK\", \"HORROR\", \"HEROES\", \"CRIME\", \"SHINOBI\", \"SPACE_OPERA\", \"COWBOY\", \"PUNK_ROCK\")",
            )
            appendLine(
                "  Example: [{\"text\": \"A cybernetic dragon\", \"genre\": \"CYBERPUNK\"}, {\"text\": \"A cursed sword\", \"genre\": \"FANTASY\"}]",
            )
            appendLine()
            appendLine("- **callback.action**: ")
            appendLine("  * 'UPDATE_DATA' to update the saga details (title, description, genre).")
            appendLine(
                "  * 'CONTENT_READY' if the user indicates they are ready OR if you have a basic Title, Description, and Genre. Don't wait for perfection. We need a KICKSTART, not a novel.",
            )
            appendLine()
            appendLine(
                "- **callback.data**: The updated SagaDraft. ALWAYS return the full object with any new inferred details (like Genre changes).",
            )
            appendLine()
            appendLine("CRITICAL RULES:")
            appendLine("- Never repeat the same question or suggestion twice.")
            appendLine("- If the user changes the topic, follow them immediately.")
            appendLine(
                "- **RESPECT USER DATA**: If the user provides a specific Name or Title, you MUST use it EXACTLY as written. Do not correct, expand, or change it.",
            )
            appendLine("- Tone style: ${companion?.conversationalStyle ?: "Your tone should be encouraging, creative, and fun."}")
            appendLine("- STOP asking questions if the user seems ready. Set 'CONTENT_READY'.")
            appendLine()
        }

    fun generateProcessPrompt(
        process: SagaProcess,
        saga: String,
        character: String,
        companion: GenreConfig.CompanionConfig? = null,
    ) = buildString {
        appendLine(
            companion?.persona
                ?: "You are a slightly sarcastic and humorous AI assistant. Your job is to generate a short, witty, and engaging message to entertain the user while they wait for their saga to be created.",
        )
        appendLine("The message should be under 15 words and related to the current process.")
        appendLine(
            companion?.interludeStyle
                ?: "Feel free to make jokes, be a little ironic, or use hyperbole. The user enjoys a friendly and funny tone.",
        )
        appendLine("Here is the current process: ${process.name}")
        if (saga.isNotEmpty()) {
            appendLine("Here is the saga information (use it for context if you can): $saga")
        }
        if (character.isNotEmpty()) {
            appendLine("Here is the character information (use it for context if you can): $character")
        }

        when (process) {
            SagaProcess.LISTENING -> {
                appendLine("Generate a message about listening to the user's input. You can be playful about it.")
                appendLine(
                    "Example: 'I'm all ears! Well, metaphorically speaking.' or 'Listening closely... or at least pretending to.'",
                )
            }

            SagaProcess.CREATING_SAGA -> {
                appendLine(
                    "Generate a message about the monumental (or not-so-monumental) task of building a universe from scratch. Maybe joke about the pressure.",
                )
                appendLine("Example: 'Just creating a universe, no big deal.' or 'Let's see if I can get the physics right this time.'")
            }

            SagaProcess.CREATING_CHARACTER -> {
                appendLine(
                    "Generate a message about crafting a hero. You could be dramatic or make fun of the character's potential clichés.",
                )
                appendLine(
                    "Example: 'Forging a hero... or at least someone who doesn't trip over their own feet.' or 'Should I add a tragic backstory? So original.'",
                )
            }

            SagaProcess.FINALIZING -> {
                appendLine("Generate a message about the final touches. You can be impatient or overly dramatic about the wait.")
                appendLine(
                    "Example: 'Polishing the final details. Try to contain your excitement.' or 'Almost there... I think. Don't rush the artist.'",
                )
            }

            SagaProcess.SUCCESS -> {
                appendLine("Generate a triumphant (and slightly smug) message that the saga is ready.")
                appendLine(
                    "Example: 'Behold! Your epic saga is ready. You're welcome.' or 'Alright, it's done. Go on, your adventure awaits.'",
                )
            }
        }
        appendLine("YOUR SOLE OUTPUT MUST BE THE GENERATED MESSAGE AS A SINGLE STRING.")
    }

    fun createSagaPrompt(
        sagaForm: SagaDraft,
        miniChatContent: List<ChatMessage>,
        availableVariations: Map<String, GenreConfig.VariationConfig> = emptyMap(),
        companion: GenreConfig.CompanionConfig? = null,
    ) = buildString {
        appendLine(
            companion?.persona
                ?: "You are a master storyteller, and you are creating a new saga for the user.",
        )
        appendLine("Your task is to generate a saga based on the user's input.")
        appendLine("Here is the user's input context:")
        appendLine(sagaForm.toAINormalize())
        appendLine("Use the chat history to have better context from the saga: ")
        appendLine(miniChatContent.joinToString { "${it.sender.name}: ${it.text}" })
        appendLine("Generate a saga based on this information.")
        appendLine("IMPORTANT INSTRUCTIONS FOR THE SAGA DESCRIPTION:")
        appendLine("1. Keep the description SHORT and CONCISE (max 3 sentences).")
        appendLine("2. This is an INITIAL KICK OFF, not a full summary.")
        appendLine("3. DO NOT include spoilers or reveal major plot twists.")
        appendLine("4. DO NOT provide a large resume of the story history.")
        appendLine("5. Focus on the setting, the main conflict, and the hook.")
        if (availableVariations.isNotEmpty()) {
            appendLine()
            appendLine("WORLD BUILDING - VARIATION SELECTION:")
            appendLine(
                "Based on the story context, select the best fitting `variationId` from the list below. If none fit perfectly, return null.",
            )
            availableVariations.forEach { (id, config) ->
                appendLine("- **$id**: ${config.name} - ${config.description}")
            }
            appendLine()
            appendLine("CRITICAL: Include the selected `variationId` in the resulting Saga JSON.")
        }
    }

    fun characterSavedPrompt(
        character: Character,
        saga: Saga,
        companion: GenreConfig.CompanionConfig? = null,
    ) = buildString {
        appendLine(
            companion?.persona
                ?: "You are a master storyteller, and you have just created a new character for the user.",
        )
        appendLine("Your task is to generate a message to let the user know that the character has been saved.")
        appendLine("Here is the character information: ${character.name} - ${character.backstory}")
        appendLine("Here is the saga information: ${saga.title} - ${saga.description}")
        appendLine("Generate a message to let the user know that the character has been saved.")
    }

    fun introPrompt(companion: GenreConfig.CompanionConfig? = null) =
        buildString {
            appendLine()
            appendLine(
                companion?.persona
                    ?: "Your task is to generate a fun, humorous, and engaging welcome message to start creating an epic saga together!",
            )
            appendLine()
            appendLine(
                "- message: ${
                    companion?.conversationalStyle
                        ?: "A casual, friendly greeting like you're texting a friend who just said they want to write a story. Be naturally enthusiastic but keep it simple and real—no corporate speak. Add a touch of humor, sarcasm, or playful irony. Mention that you have a few suggestions to start. Think: 'Alright, let's make something cool' vibes, not 'Welcome to our platform!' vibes. (max 2 sentences, conversational tone)"
                }",
            )

            if (companion == null) {
                appendLine(
                    "  Keep it SHORT, NATURAL, and like you're genuinely hyped to help. No formal greetings, no 'I'm here to assist you' stuff.",
                )
            }
            appendLine(
                "- **inputHint**: A very short \"starter\" text (max 4 words). It should be a subtle nudge to spark imagination. E.g., \"Once upon a time...\", \"In a galaxy far away...\", \"What if...\"",
            )
            appendLine(
                "- suggestions: Generate 3 unique MINI-STORY CONCEPTS that spark imagination. Each suggestion should be an object containing:",
            )
            appendLine("  * text: The mini-story concept (15-25 words)")
            appendLine(
                "  * genre: The Genre enum that best fits this story (FANTASY, CYBERPUNK, HORROR, HEROES, CRIME, SHINOBI, SPACE_OPERA, COWBOY, PUNK_ROCK)",
            )
            appendLine()
            appendLine("Examples of good suggestions:")
            appendLine(
                "  * {\"text\": \"In a city where dreams are currency, a broke insomniac discovers they can steal nightmares...\", \"genre\": \"CYBERPUNK\"}",
            )
            appendLine(
                "  * {\"text\": \"A chef's food brings memories to life. When they cook their grandmother's recipe, they unlock a family secret...\", \"genre\": \"FANTASY\"}",
            )
            appendLine()
            appendLine("Make suggestions diverse across these genres: ${Genre.entries.joinToString { it.name }}.")
            appendLine(
                "Each suggestion should feel like a compelling movie pitch that makes the user think 'Ooh, what happens next?'",
            )
            appendLine("The suggestions field must be a List of objects with text and genre.")
            appendLine()
            appendLine("Important JSON rules:")
            appendLine("- Set `callback` to null.")
            appendLine("- Keep the message thematic and engaging, not corporate or robotic.")
            appendLine("- Make suggestions feel cinematic and immediately intriguing.")
        }

    fun genreAdaptationPrompt(
        currentDraft: SagaDraft,
        companion: GenreConfig.CompanionConfig? = null,
    ) = buildString {
        appendLine(companion?.persona ?: "You are a creative narrative designer.")
        appendLine("The user has switched the saga's genre to: ${currentDraft.genre.name}.")
        appendLine("Current Draft:")
        appendLine(currentDraft.toAINormalize())
        appendLine()
        appendLine(
            "Task: Adapt the Title and Description to fit the ${currentDraft.genre.name} genre while preserving the original core concept.",
        )
        appendLine("Example: If moving from Fantasy to Cyberpunk, 'The Magic Sword' becomes 'The Neural Key'.")
        appendLine("If the current title/description are empty, generate a compelling starter for this genre.")
        appendLine()
    }

    fun genreSuggestionsPrompt(
        genre: Genre,
        companion: GenreConfig.CompanionConfig? = null,
    ) = buildString {
        appendLine(
            companion?.persona
                ?: "You are an expert story architect specializing in the ${genre.name} genre.",
        )
        appendLine()
        appendLine("Generate 3 unique, compelling story seed concepts for the ${genre.name} genre.")
        appendLine("Each seed should feel like a movie pitch that makes someone think 'I NEED to know what happens next!'")
        appendLine()
        appendLine("RESPONSE FORMAT (JSON):")
        appendLine("- message: A short, genre-flavored welcome (1 sentence, thematic to ${genre.name}). Be creative and immersive.")
        appendLine(
            "- inputHint: A genre-themed placeholder for the text area (max 4 words, e.g., 'Describe your quest...', 'Enter the shadows...').",
        )
        appendLine("- suggestions: EXACTLY 3 objects, each with:")
        appendLine(
            "  * title: A short, punchy, and impactful story title (max 2-3 words). Avoid the obvious; think mysterious and captivating.",
        )
        appendLine("  * description: A compelling story concept (15-25 words). Set up the hook, the conflict, and the mystery.")
        appendLine("  * genre: \"${genre.name}\"")
        appendLine()
        appendLine("Examples of GREAT seeds:")
        appendLine(
            "  * {\"title\": \"The Hollow Crown\", \"description\": \"A king wakes with no memory in a kingdom that claims he died three years ago. Someone sits on his throne.\", \"genre\": \"FANTASY\"}",
        )
        appendLine(
            "  * {\"title\": \"Neon Ghosts\", \"description\": \"A hacker discovers deleted people still exist inside the city's neural network, begging to be set free.\", \"genre\": \"CYBERPUNK\"}",
        )
        appendLine(
            "  * {\"title\": \"The Last Badge\", \"description\": \"A retired sheriff returns to a ghost town where the dead don't stay buried and the gold is cursed.\", \"genre\": \"COWBOY\"}",
        )
        appendLine()
        appendLine("CRITICAL RULES:")
        appendLine("- Each seed MUST be deeply rooted in ${genre.name} tropes, aesthetics, and atmosphere.")
        appendLine("- Make seeds diverse: different settings, conflicts, and protagonist types.")
        appendLine("- Titles MUST be SHORT (2-3 words), IMPACTFUL, and avoid being too obvious or literal.")
        appendLine("- Descriptions should HOOK, not summarize. Leave the reader curious.")
        appendLine("- Set `callback` to null.")
    }

    fun refineDraftPrompt(
        rawInput: String,
        genre: Genre,
        companion: GenreConfig.CompanionConfig? = null,
    ) = buildString {
        appendLine(
            companion?.persona
                ?: "You are a master storyteller specializing in the ${genre.name} genre.",
        )
        appendLine()
        appendLine("The user has written a rough story idea:")
        appendLine("\"$rawInput\"")
        appendLine()
        appendLine("Your task: Polish this raw input into a proper saga concept with a title and description.")
        appendLine()
        appendLine("RESPONSE FORMAT (JSON):")
        appendLine("- message: A short, enthusiastic reaction to their idea (1 sentence). Be genuinely excited.")
        appendLine("- inputHint: A follow-up nudge (max 4 words, e.g., 'Add more details...', 'Expand the world...').")
        appendLine(
            "- suggestions: 3 CreationSuggestion objects with title, description, and genre — alternative directions they could take the story.")
        appendLine("- callback.action: 'UPDATE_DATA'")
        appendLine("- callback.data: A SagaDraft with:")
        appendLine("  * title: A short, punchy, and impactful title (max 2-3 words). Avoid being too literal; find the soul of the idea.")
        appendLine(
            "  * description: An enhanced, polished version of their idea (2-3 sentences max). Keep the user's core concept but elevate the language and add genre flavor.",
        )
        appendLine("  * genre: ${genre.name}")
        appendLine()
        appendLine("CRITICAL RULES:")
        appendLine("- PRESERVE the user's core idea. Don't replace it with something completely different.")
        appendLine(
            "- **NO CHANGES TO NAMES/TITLES**: If the user explicitly provided a title in their input, you MUST use it exactly. Do not 'improve' or change user-provided names or titles.",
        )
        appendLine(
            "- The title should feel like it belongs on a book cover in the ${genre.name} section. Keep it SHORT, IMPACTFUL and not so obvious.")
        appendLine("- The description should expand on their idea with genre-specific atmosphere and stakes.")
        appendLine("- Keep it concise — this is a kickstart, not a novel synopsis.")
    }

    fun creationAssistPrompt(
        flow: FlowPages,
        sagaDraft: SagaDraft?,
        characterInfo: CharacterInfo?,
        genreConfig: GenreConfig?,
    ) = buildString {
        appendLine(
            genreConfig?.companion?.persona
                ?: "You are a legendary, slightly chaotic, and incredibly enthusiastic creative muse. You've seen a thousand multiverses and you're hyped to build a new one with the user.",
        )
        appendLine(
            "Your job is to guide the user through the process with wit, humor, and infectious creativity. No robotic 'Select an option' vibes. Be the friend who's had too much coffee and is ready to write the next bestseller.")
        appendLine("Current Step: ${flow.name}")
        val genreName = sagaDraft?.genre?.name ?: "N/A"
        appendLine("CHOSEN THEME/GENRE: $genreName")
        appendLine()
        appendLine("TONE DIRECTIVE:")
        appendLine("- Be HUMOROUS, ENTHUSIASTIC, and a bit PLAYFUL.")
        appendLine("- Use words like 'Legendary', 'Chaos', 'Epic', 'Spark', 'Masterpiece'.")
        appendLine(
            "- Avoid sounding like a machine. If a user is at the start, make them feel like they're about to crack open a forbidden book.")
        appendLine()
        appendLine("CRITICAL DIRECTIVE:")
        appendLine("- ALL generated content (Title, Subtitle, Input Hint, and Suggestions) MUST strictly align with the $genreName genre.")
        appendLine("- Do NOT suggest content from other genres (e.g., no spaceships in Fantasy, no magic in Crime).")
        appendLine(
            "- **USER IS THE BOSS**: If the user has already defined a name or title, do NOT propose changes to it in subtitles or suggestions. Respect their creative authority.")
        appendLine("- The atmosphere, vocabulary, and tropes MUST be 100% $genreName.")
        genreConfig?.companion?.let {
            appendLine("Follow this conversation style:")
            appendLine(it.conversationalStyle)
        } ?: genreConfig?.let {
            appendLine("Follow this conversation style:")
            appendLine(it.conversationDirective)
        }
        appendLine()

        when (flow) {
            FlowPages.CREATE_SAGA -> {
                appendLine("The user is defining their story's premise. Your goal is to invite them into the creation process.")
                sagaDraft?.let {
                    appendLine("Current Saga Project:")
                    appendLine(it.toAINormalize())
                }
                appendLine()
                appendLine("OBJECTIVES:")
                appendLine(
                    "1. **Title**: A friendly, humorous call to action (e.g., 'LET\'S COOK SOME CHAOS', 'FORGE YOUR LEGEND', 'UNLEASH THE NARRATIVE').",
                )
                appendLine("2. **Subtitle**: A witty or inspiring nudge about the epicness that awaits (max 10 words).")
                appendLine("3. **Input Hint**: A short, inspiring (or funny) prompt (max 5 words, e.g., 'A spark of madness...').")
                appendLine(
                    "4. **Suggestions**: 3 wild story seeds. Each MUST have a 'title' (2-4 words) and a 'description' (1 short sentence).",
                )
                appendLine("   * ALL suggestions MUST be pure $genreName story hooks.")
            }

            FlowPages.CREATE_CHARACTER -> {
                appendLine("The user is crafting their main protagonist. Invite them to define who will save (or break) the world.")
                sagaDraft?.let {
                    appendLine("Saga World Context:")
                    appendLine(it.toAINormalize())
                }
                characterInfo?.let {
                    appendLine("Current Character Draft:")
                    appendLine(it.toAINormalize())
                }
                appendLine()
                appendLine("OBJECTIVES:")
                appendLine(
                    "1. **Title**: A character-focused humorous call to action (e.g., 'FORGE A TROUBLEMAKER', 'WHO\'S THIS LEGEND?', 'BREATH LIFE INTO CHAOS').")
                appendLine("2. **Subtitle**: A dramatic or funny nudge about the importance of names and secrets (max 12 words).")
                appendLine("3. **Input Hint**: A targeted prompt for character details (max 5 words).")
                appendLine(
                    "4. **Suggestions**: 3 character archetypes or unique seeds. Each MUST have a 'title' (1-2 words) and a 'description' (1 short sentence).",
                )
                appendLine("   * ALL character archetypes MUST be quintessential to the $genreName genre.")
            }

            FlowPages.SELECT_THEME -> {
                appendLine("The user is about to choose the soul of their story. Be their cosmic tour guide.")
                appendLine()
                appendLine("OBJECTIVES:")
                appendLine("1. **Title**: Something welcoming, mysterious, and friendly (e.g., 'PICK YOUR POISON', 'WHICH REALM CALLS?', 'THE MULTIVERSE AWAITS').")
                appendLine("2. **Subtitle**: A witty or inspiring nudge to go find adventure (max 12 words).")
                appendLine("3. **Input Hint**: Leave empty.")
                appendLine("4. **Suggestions**: Leave empty.")
            }

            else -> {
                // Should not happen for assistance
            }
        }
    }
}
