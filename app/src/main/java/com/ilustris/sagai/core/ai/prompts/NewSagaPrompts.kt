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
        appendLine("You are a master storyteller, and you are creating a new saga for the user.")
        appendLine("Your task is to generate a short, engaging message to keep the user excited while they wait.")
        appendLine("The message should be no more than 10 words and should be related to the current process.")
        appendLine("Here is the current process: ${process.name}")
        appendLine("Here is the saga information: $saga")
        appendLine("Here is the character information: $character")
        when (process) {
            SagaProcess.CREATING_SAGA -> {
                appendLine("Generate a message about creating a new world or story.")
            }
            SagaProcess.CREATING_CHARACTER -> {
                appendLine("Generate a message about bringing a character to life.")
            }
            SagaProcess.FINALIZING -> {
                appendLine("Generate a message about finishing the setup and getting ready to start.")
            }

            SagaProcess.SUCCESS -> {
                appendLine("Generate a message that the saga is ready and the player is ready to jump into the story.")
            }
        }
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
            appendLine(
                "Your task is to generate a very short, welcoming, and engaging message to start a conversation about creating a new saga.",
            )
            appendLine(
                "- message: A concise and friendly welcome (max 2 sentences). Greet the user and ask what kind of story they have in mind.",
            )
            appendLine("- inputHint: A short hint for the user's input (e.g., \"A sci-fi epic, a fantasy adventure, ...\").")
            appendLine("- suggestions: Provide 2-3 one-word suggestions to spark ideas (e.g., \"Cyberpunk\", \"Magic\", \"Mystery\").")
            appendLine("- Keep the tone encouraging and imaginative.")
            appendLine()
            appendLine("Important JSON rules:")
            appendLine("- Set `callback` to null.")
            appendLine()
            appendLine("Expected output schema:")
            appendLine(toJsonMap(SagaCreationGen::class.java))
        }
}
