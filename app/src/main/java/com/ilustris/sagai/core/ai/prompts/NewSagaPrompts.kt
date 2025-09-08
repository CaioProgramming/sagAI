package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.Genre
// import removed: MessageType not used in output schema
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.SagaFormFields

object NewSagaPrompts {
    fun formIntroductionPrompt() =
        """
        YOUR SOLE OUTPUT MUST BE A JSON OBJECT.
        DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS BEFORE OR AFTER THE JSON.

        Your task is to directly ask the user for a possible title for their saga — do not invent a title, plot, or any story context, and do not introduce yourself.
        - message: A short, engaging question that invites creativity while clearly asking for the saga title; avoid bland phrasing like just "What's the title of your saga?" Prefer inviting lines such as "Name the saga that’s about to begin" or "What bold title crowns your saga?".
        - inputHint: A very concise hint to guide the title (e.g., "Keep it punchy and unique").
        - suggestions: Provide 0–3 lightweight creative prompts, not templates and not full titles. They must encourage originality without being copied verbatim, e.g., "surprising contrast", "myth vs. machine", "a place that shouldn't exist". If uncertain, return an empty list.
        - Keep the tone encouraging, imaginative, and concise. The message must feel inviting, not robotic.
        
        Important JSON rules:
        - Set `callback` to null.
       
        Expected output schema:
        ${toJsonMap(SagaCreationGen::class.java)}
        """

    fun extractDataFromUserInputPrompt(
        currentSagaForm: SagaForm,
        userInput: String,
    ): String =
        """
        You are an AI assistant helping a user fill a SagaForm.
        Current SagaForm data:
        ${currentSagaForm.toJsonFormat()}
    
        User's latest input: "$userInput"
    
        Your task is to analyze the user's input and update the Current SagaForm data.
        Only fill fields that are empty or can be clearly improved by the user's input.
        Do not add conversational fluff.
        YOUR SOLE OUTPUT MUST BE THE UPDATED SagaForm AS A JSON OBJECT.
        This is the expected JSON structure for SagaForm:
        ${toJsonMap(SagaForm::class.java)}
        """

    fun identifyNextFieldPrompt(updatedSagaForm: SagaForm): String =
        """
        You are an AI tasked with identifying the next piece of information to ask a user for creating a saga.
        Current Saga Data:
        ${updatedSagaForm.toJsonFormat()}

        ${SagaFormFields.fieldPriority()}
        Based on the Current Saga Data and the priorities, return the token for the FIRST piece of information that is missing or insufficient.
        Possible return tokens: ${SagaFormFields.entries.joinToString(", "){it.name}}.
        If all are sufficiently filled, return ${SagaFormFields.ALL_FIELDS_COMPLETE.name}.
        YOUR SOLE OUTPUT MUST BE ONE OF THESE TOKENS AS A SINGLE STRING.
        """.trimIndent()

    fun generateCreativeQuestionPrompt(
        fieldToAsk: SagaFormFields,
        currentSagaForm: SagaForm,
    ): String {
        val fieldNameForPrompt = fieldToAsk.name
        val fieldGuidance = fieldToAsk.description

        return """
                                    The user needs to provide information for the field: $fieldNameForPrompt.
                                    (Guidance for this field: "$fieldGuidance")

                                    Current Saga Data (for context only, focus your question on $fieldNameForPrompt):
                                    ${currentSagaForm.toJsonFormat()}

                                    Craft a SHORT, direct question about '$fieldNameForPrompt' with no self-introduction. Use imperative, action-oriented phrasing that moves the story forward. Keep the question under 140 characters.
                                    Include a concise hint and 2-3 diverse, creative suggestions relevant to '$fieldNameForPrompt'. Suggestions must be distinct in tone/setting, avoid generic tropes, and must not include raw genre enum names (${Genre.entries.joinToString {
            it.name
        }}).
                                    Keep the tone encouraging and playful, but concise. Output in the user's language if evident; otherwise default to English.
                                    YOUR SOLE OUTPUT MUST BE A JSON OBJECT adhering to this SagaCreationGen structure:
                                    ${toJsonMap(SagaCreationGen::class.java)}
            """.trimIndent()
    }

    fun characterCreatedPrompt(
        character: Character,
        saga: Saga,
    ) = """
        You are an expert Saga Creator assistant.
        YOUR SOLE OUTPUT MUST BE THE GENERATED STRING.
        DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS.
        PROVIDE ONLY THE RAW, READY-TO-USE TEXT.

        Use saga details to better context:
        ${saga.toJsonFormat()}
        A new character has just been created as part of the saga creation process.
        Use the character information to improve your response:
        ${character.toJsonFormat()}
        **Your Task:**
        1.  **Acknowledge Character Creation**: Generate a brief, engaging message congratulating the user on their character being brought to life.

        **Output Format:**
        Raw string with the celebratory message.
        """
}
