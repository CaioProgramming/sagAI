package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.usecase.SagaProcess

@Suppress("ktlint:standard:max-line-length")
object NewSagaPrompts {
    fun conversationalSagaReply(
        currentSagaDraft: SagaDraft,
        userInput: String,
        conversationHistory: List<ChatMessage>,
    ): String =
        buildString {
            appendLine(
                "You are a passionate, brainstorming creative partner. Your goal is to help the user KICKSTART an amazing saga. You are NOT a form-filler; you are a co-creator.",
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
            appendLine("- Your tone should be encouraging, creative, and fun.")
            appendLine("- STOP asking questions if the user seems ready. Set 'CONTENT_READY'.")
            appendLine()
        }

    fun generateProcessPrompt(
        process: SagaProcess,
        saga: String,
        character: String,
    ) = buildString {
        appendLine(
            "You are a slightly sarcastic and humorous AI assistant. Your job is to generate a short, witty, and engaging message to entertain the user while they wait for their saga to be created.",
        )
        appendLine("The message should be under 15 words and related to the current process.")
        appendLine("Feel free to make jokes, be a little ironic, or use hyperbole. The user enjoys a friendly and funny tone.")
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
    ) = buildString {
        appendLine("You are a master storyteller, and you are creating a new saga for the user.")
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
    }

    fun characterSavedPrompt(
        character: Character,
        saga: Saga,
    ) = buildString {
        appendLine("You are a master storyteller, and you have just created a new character for the user.")
        appendLine("Your task is to generate a message to let the user know that the character has been saved.")
        appendLine("Here is the character information: ${character.name} - ${character.backstory}")
        appendLine("Here is the saga information: ${saga.title} - ${saga.description}")
        appendLine("Generate a message to let the user know that the character has been saved.")
    }

    fun introPrompt() =
        buildString {
            appendLine()
            appendLine("Your task is to generate a fun, humorous, and engaging welcome message to start creating an epic saga together!")
            appendLine()
            appendLine(
                "- message: A casual, friendly greeting like you're texting a friend who just said they want to write a story. Be naturally enthusiastic but keep it simple and real—no corporate speak. Add a touch of humor, sarcasm, or playful irony. Mention that you have a few suggestions to start. Think: 'Alright, let's make something cool' vibes, not 'Welcome to our platform!' vibes. (max 2 sentences, conversational tone)",
            )

            appendLine(
                "  Keep it SHORT, NATURAL, and like you're genuinely hyped to help. No formal greetings, no 'I'm here to assist you' stuff.",
            )
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
            appendLine("- Keep the message playful and encouraging, not corporate or robotic.")
            appendLine("- Make suggestions feel cinematic and immediately intriguing.")
        }

    fun genreAdaptationPrompt(currentDraft: SagaDraft) =
        buildString {
            appendLine("You are a creative narrative designer.")
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
            appendLine("RESPONSE FORMAT (JSON ONLY):")
            appendLine(
                "- message: A brief, thematic confirmation of the adaptation (e.g., 'Realigning story matrix to Cyberpunk standards...', 'Summoning the spirits of Fantasy...').",
            )
            appendLine("- inputHint: A new creative starter question fitting the genre (max 4 words).")
            appendLine("- suggestions: 3 new CreationSuggestion objects (text, genre) fitting the new theme.")
            appendLine("- callback.action: 'UPDATE_DATA'")
            appendLine("- callback.data: The updated SagaDraft (ensure genre is ${currentDraft.genre.name}).")
        }
}
