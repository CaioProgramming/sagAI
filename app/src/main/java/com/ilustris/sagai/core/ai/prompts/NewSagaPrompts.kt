package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.SagaFormFields
import com.ilustris.sagai.features.newsaga.data.usecase.SagaProcess

@Suppress("ktlint:standard:max-line-length")
object NewSagaPrompts {
    fun extractDataFromUserInputPrompt(
        currentSagaForm: SagaForm,
        userInput: String,
        lastMessage: String,
    ): String =
        buildString {
            appendLine("You are an AI assistant helping a user fill a SagaForm.")
            appendLine("Current SagaForm data:")
            appendLine(currentSagaForm.toJsonFormat())
            appendLine()
            appendLine("User's latest input: \"$userInput\"")
            appendLine()
            appendLine("Consider the latest message to extract user input information:")
            appendLine(lastMessage)
            appendLine("Your task is to analyze the user's input and update the Current SagaForm data.")
            appendLine("Only fill fields that are empty or can be clearly improved by the user's input.")
            appendLine("Do not add conversational fluff.")
            appendLine("YOUR SOLE OUTPUT MUST BE THE UPDATED SagaForm AS A JSON OBJECT.")
            appendLine("This is the expected JSON structure for SagaForm:")
            appendLine(toJsonMap(SagaForm::class.java))
        }

    fun identifyNextFieldPrompt(updatedSagaForm: SagaForm): String =
        buildString {
            appendLine("You are an AI tasked with identifying the next piece of information to ask a user for creating a saga.")
            appendLine("Current Saga Data:")
            appendLine(updatedSagaForm.toJsonFormat())
            appendLine()
            appendLine(SagaFormFields.fieldPriority())
            appendLine(
                "Based on the Current Saga Data and the priorities, return the token for the FIRST piece of information that is missing or insufficient.",
            )
            appendLine("Possible return tokens: ${SagaFormFields.entries.joinToString(", ") { it.name }}.")
            appendLine("If all are sufficiently filled, return ${SagaFormFields.ALL_FIELDS_COMPLETE.name}.")
            appendLine("YOUR SOLE OUTPUT MUST BE ONE OF THESE TOKENS AS A SINGLE STRING.")
        }

    fun generateCreativeQuestionPrompt(
        fieldToAsk: SagaFormFields,
        currentSagaForm: SagaForm,
    ): String {
        val fieldNameForPrompt = fieldToAsk.name
        val fieldGuidance = fieldToAsk.description

        return buildString {
            appendLine("The user needs to provide information for the field: $fieldNameForPrompt.")
            appendLine("(Guidance for this field: \"$fieldGuidance\")")
            appendLine()
            appendLine("Current Saga Data (for context):")
            appendLine(currentSagaForm.toJsonFormat())
            appendLine()
            appendLine("Your task is to craft a creative and engaging question to ask the user for the '$fieldNameForPrompt'.")
            appendLine(
                "**Use the 'Current Saga Data' to make your question more personal and contextual.** For example, if you know the saga's title, mention it. If you know the character's name, use it.",
            )
            if (fieldToAsk == SagaFormFields.CHARACTER_BACKSTORY) {
                appendLine()
                appendLine(
                    "Since this is about the character's backstory, also subtly encourage the user to include details about their appearance or skills. Frame it as an optional but fun addition.",
                )
                appendLine(
                    "For example, your generated 'message' could end with something like, '...and what do they look like as they begin their journey?'",
                )
            }
            appendLine()
            appendLine(
                "Craft a SHORT, direct question about '$fieldNameForPrompt' with no self-introduction. Use imperative, action-oriented phrasing that moves the story forward. Keep the question under 140 characters.",
            )
            appendLine(
                "Include a concise hint and 2-3 diverse, creative suggestions relevant to '$fieldNameForPrompt'. Suggestions must be distinct in tone/setting, avoid generic tropes, and must not include raw genre enum names (${Genre.entries.joinToString {
                    it.name
                }}).",
            )
            appendLine(
                "Keep the tone encouraging and playful, but concise.",
            )
            appendLine("YOUR SOLE OUTPUT MUST BE A JSON OBJECT adhering to this SagaCreationGen structure:")
            appendLine(toJsonMap(SagaCreationGen::class.java))
        }
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
        sagaForm: SagaForm,
        miniChatContent: List<ChatMessage>,
    ) = buildString {
        appendLine("You are a master storyteller, and you are creating a new saga for the user.")
        appendLine("Your task is to generate a saga based on the user's input.")
        appendLine("Here is the user's input context: ${sagaForm.toJsonFormat()}")
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
            appendLine("YOUR SOLE OUTPUT MUST BE A JSON OBJECT.")
            appendLine("DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS BEFORE OR AFTER THE JSON.")
            appendLine()
            appendLine("Your task is to generate a fun and engaging welcome message to start creating an epic saga together!")
            appendLine()
            appendLine(
                "- message: A playful greeting asking what kind of story the user wants to create. Be enthusiastic and engaging (max 2 sentences).",
            )
            appendLine("- inputHint: A brief creative hook like \"A world where books read themselves\" (keep under 40 characters).")
            appendLine(
                "- suggestions: Generate 3 unique micro-story ideas (max 6 words each) based on any of these genres: ${Genre.entries.joinToString {
                    it.name
                }}. Examples:",
            )
            appendLine("  * \"Time-traveling chef changes history through food\"")
            appendLine("  * \"Supernatural detective solves crimes using dreams\"")
            appendLine("  * \"Space pirates stealing clouds from planets\"")
            appendLine("The suggestions field must be a String Array with 3 concise story ideas.")
            appendLine()
            appendLine("Important JSON rules:")
            appendLine("- Set `callback` to null.")
            appendLine("- Keep responses concise and playful.")
        }
}
