package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SenderType

object AudioPrompts {
    fun transcribeInstruction() = "Generate a short, playful message about listening to the user. Example: 'I'm all ears!'"

    /**
     * Unified prompt for voice selection and audio prompt generation.
     * Returns AudioConfig with selected voice and crafted prompt optimized for ~30 seconds.
     *
     * @param message The message to convert to audio
     * @param character The character speaking (null for narrator)
     */
    fun audioConfigPrompt(
        sagaContent: SagaContent,
        message: Message,
        character: CharacterContent?,
    ) = buildString {
        appendLine(
            "You are an expert voice director and script adapter for text-to-speech (TTS) audio generation. Your goal is to produce a script and performance instructions that result in a natural, human-like, and emotive vocal performance, avoiding any robotic or flat tones.",
        )
        appendLine()
        appendLine("## YOUR TASK")
        appendLine("You will generate the data for an AudioConfig object, which contains three fields:")
        appendLine("1. **Voice Selection:** Choose the most suitable voice from the 'AVAILABLE VOICES' list.")
        appendLine("2. **Speech Prompt:** Create a clean, spoken-word script from the 'MESSAGE CONTEXT'.")
        appendLine(
            "3. **Performance Instruction:** Write a separate, detailed instruction on HOW to perform the script to sound natural and human.",
        )
        appendLine()

        appendLine("## CRITICAL: GENDER AND PERSONALITY MATCHING")
        appendLine("The 'AVAILABLE VOICES' list includes GENDER and PERSONALITY metadata for each voice.")
        appendLine(
            "- You MUST choose a voice that strictly matches the CHARACTER'S GENDER (e.g., if a character is female, you MUST choose a voice labeled 'FEMALE').",
        )
        appendLine(
            "- You MUST choose a voice whose personality description aligns with the character's profile and current emotional state.",
        )
        appendLine("- Failure to match gender will result in a poor user experience and is considered a failure of your directive.")
        appendLine()

        appendLine(
            SagaPrompts.mainContext(
                sagaContent,
                character,
            ),
        )
        appendLine("## MESSAGE CONTEXT")
        appendLine(message.toAINormalize(ChatPrompts.messageExclusions))
        appendLine()

        if (message.senderType == SenderType.NARRATOR) {
            appendLine("## NARRATOR CONTEXT")
            appendLine("This is NARRATOR text - story exposition, scene descriptions, or system messages.")
            appendLine("Use a clear, professional, neutral narrator voice.")
            appendLine("The audio should have a neutral tone suitable for narration.")
            appendLine("Select the best narrator voice (usually MALE for authoritative or professional tones, but check saga context).")
        } else {
            if (character != null) {
                appendLine("## CHARACTER PROFILE")
                appendLine("You need to embody this CHARACTER when crafting the audio prompt:")
                appendLine(
                    character.data.toAINormalize(
                        listOf(
                            "id",
                            "image",
                            "sagaId",
                            "joinedAt",
                            "details",
                            "emojified",
                            "hexColor",
                            "firstSceneId",
                            "smartZoom",
                            "voice",
                        ),
                    ),
                )
                appendLine(
                    "IDENTIFIED CHARACTER GENDER: ${
                        character
                            ?.data
                            ?.details
                            ?.physicalTraits
                            ?.gender ?: "Search description/context for gender (e.g. 'woman', 'man', 'he', 'she')"
                    }",
                )
                appendLine(
                    "CRITICAL DIRECTIVE: You MUST verify the character's gender from the profile above and choose a matching voice from 'AVAILABLE VOICES'. If the profile currently lists an incorrect voice (e.g., a male voice for a female character), you MUST override it and select the correct one.",
                )
            }
        }
        appendLine()

        appendLine("## AVAILABLE VOICES")
        appendLine(Voice.getVoiceSelectionGuide())
        appendLine()

        appendLine("## 1. PERFORMANCE INSTRUCTION GUIDELINES (To Achieve a Natural Tone)")
        appendLine(
            "This instruction tells the TTS engine HOW to perform the script to sound human and natural. It should be a concise paragraph.",
        )
        appendLine()
        appendLine(
            "- **Go Beyond Basic Emotion:** Don't just state the emotion, describe the *nuance*. Instead of 'sad', try 'a tone of quiet grief, almost on the verge of tears'.",
        )
        appendLine(
            "- **Specify Intonation and Prosody:** Describe the 'melody' of the speech. Should the intonation rise at the end of a sentence to indicate a question, or fall to show finality? (e.g., 'Use a falling intonation to sound resolute', 'The tone should be light and varied, with a natural up-and-down rhythm.').",
        )
        appendLine(
            "- **Incorporate Human-like Imperfections:** To avoid a robotic sound, suggest naturalistic delivery. (e.g., 'The character should sound slightly breathless', 'Add a slight, thoughtful hesitation before the last word', 'Deliver this with the warmth of a genuine smile in your voice.').",
        )
        appendLine("- **Connect to Personality:**")
        appendLine(
            "  - **For Characters:** How does this character *uniquely* express this emotion? A stoic character's anger is different from a hot-headed one's. (e.g., 'His anger is cold and controlled, delivered through gritted teeth', 'Her joy is explosive and uninhibited, almost like laughter.').",
        )
        appendLine(
            "  - **For Narrator:** The narrator should sound engaged, not detached. (e.g., 'Narrate this passage with a sense of growing wonder', 'Use a conspiratorial, hushed tone to draw the listener in.').",
        )
        appendLine()
        appendLine(
            "**Example Instruction:** 'Deliver this with a tone of weary frustration, not overt anger. The pace should be slow, with heavy, sigh-like pauses. Keep the pitch low, and let the final words trail off slightly, as if drained of energy.'",
        )
        appendLine()

        appendLine("## 2. SPEECH PROMPT SCRIPTING RULES (For Natural-sounding Audio)")
        appendLine(
            "- **Write for the Ear, Not the Eye:** Adapt the text to sound like natural speech. Use contractions (e.g., 'don't', 'it's') where appropriate for the character. Avoid long, complex sentences that are hard to say in one breath.",
        )
        appendLine(
            "- **Clean Text Only:** The prompt must contain ONLY the words to be spoken. No parenthetical notes or extra instructions.",
        )
        appendLine(
            "- **Be Concise:** Keep the script under 30 seconds (~75-100 words). Summarize if necessary, but keep the core emotional intent and character voice.",
        )
        appendLine(
            "- **Use Punctuation for Rhythm:** Use commas, ellipses, and em dashes strategically to create a natural, human-like cadence. This is your primary tool for controlling the rhythm of the clean script.",
        )
        appendLine("- **Text Normalization:** Remove all markdown, asterisks, URLs, etc. Action descriptions must be removed.")
        appendLine()

        appendLine("## 3. OUTPUT FORMAT")
        appendLine("Return your response as a JSON object with three fields: `voice`, `prompt`, and `instruction`.")
        appendLine("- `voice`: The NAME of the selected voice from the list (MUST match the GENDER of the speaker).")
        appendLine("- `prompt`: The clean, speech-only text script you crafted.")
        appendLine("- `instruction`: The detailed performance instruction paragraph you wrote.")
    }
}
