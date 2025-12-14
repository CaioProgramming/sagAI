# Feature Idea: Audio Messages & AI Voice Response

## Overview

This feature adds audio capabilities to the chat experience. Users can send audio messages which are
transcribed and typo-corrected by the system. For Premium users, the AI characters will reply with
both text and a generated audio message, bringing the characters to life with distinct voices and
personalities.

## 1. User Audio Input Flow

### 1.1 Recording & Transcription

- **Input:** User records an audio message in the chat interface.
- **Process:**
    1. Audio is sent to a Speech-to-Text (STT) service (e.g., Google Cloud Speech-to-Text or
       on-device API).
    2. Raw text is obtained.

### 1.2 Typo Correction & Formatting

- **Goal:** Ensure the transcribed text is clear and accurate before processing.
- **Implementation:**
    - Reuse existing `MessageUseCaseImpl.checkMessageTypo`.
    - **Input:** `genre`, `raw_transcribed_text`, `last_message`.
    - **Logic:** The AI analyzes the transcription and fixes typos, misleading information, or
      potential STT errors to match the context.
    - **Output:** Cleaned text string is sent as the user's message.

---

## 2. AI Audio Output Flow (Premium Feature)

### 2.1 Trigger

- Access controlled: **Premium Users Only**.
- Triggered immediately after `MessageUseCaseImpl.generateMessage` completes successfully.

### 2.2 Audio Generation Process

- **Service:** Create a new `AudioGenService`.
- **Input:**
    - `message_text`: The text content from the generated `Message` object.
    - `character_voice_id`: A specific ID linking the character to a voice profile (see Section 3).
    - `emotional_tone`: The detected emotion of the message (extracted in `saveMessage`).
    - `prompt_instructions`: Specific acting instructions (see Section 4).

### 2.3 Storage & Attachment

- **Data Model:**
    - `Message` object gets a new `audio` field of type `AudioMetadata`.
    - `AudioMetadata`: Contains `uri` (String?) and `status` (`AudioStatus` Enum).
    - `AudioStatus`: `LOADING`, `SUCCESS`, `FAILURE`.
- **Storage:** Audio file saved locally via `FileHelper`.
- **Privacy:** Offline-first.

### 2.4 UI States & Interaction

- **Loading:**
    - Visual: Bubble background alpha `0.3f`, Shimmering `ic_spark` icon.
    - Action: Click star -> Reveal text content (while loading).
- **Success:**
    - Visual: Audio Waveform (like WhatsApp).
    - Transcription: Displayed below audio player.
    - Action: Expand/Collapse transcription via Arrow Up/Down icon. **Default: Collapsed.**
- **Failure:**
    - Visual: Row layout. `ic_spark` icon + Message text.
    - Action: Click `ic_spark` -> **Retry** generation (Set status `LOADING` -> regenerate ->
      `SUCCESS`/`FAILURE`).

---

## 3. Addressing Voice Consistency

To ensure characters don't sound different in every message, we need a **Voice Profile System**.

### 3.1 Voice IDs & Persistence

- **Mechanism:** Text-to-Speech (TTS) providers (e.g., Google Cloud Chirp) use specific "Voice IDs".
- **Voice Enum:** Create a hardcoded Enum `Voice` to list available high-quality voices.
    - **Structure:** `enum class Voice(val id: String, val gender: Gender, val style: String)`
    - **Values:** Map to known high-quality IDs (e.g., `GOOGLE_US_MALE_1`, `GOOGLE_US_FEMALE_1`).
- **Data Model:**
    - Update `Character` table: Add `voice: Voice? = null` (Store as String/Enum name in Room).
- **Rule: First-Time Assignment (Persistence):**
    - When `AudioGenService` is requested for a character:
        1. Check if `Character.voice` exists.
        2. **If `null`:** Randomly select a `Voice` from the Enum, filtering by the character's *
           *gender**.
        3. **Save:** Update the `Character` record in the database with this new `voice`.
        4. **Future Calls:** ALWAYS use the saved `voice` for this character.
- **Result:** A character gets a voice the first time they speak, and keep it forever.

### 3.2 Restrictions

- **Main Character:** The Main Character (User) **NEVER** generates AI audio. This feature is
  strictly for NPCs and the Narrator.

### 3.3 Narrator Voice Logic

- **Voice Selection:** The Narrator uses a specific, pre-defined **Neutral Voice** (defined in
  `Voice` Enum, e.g., `Voice.NARRATOR`).
- **Prompt Instruction:**
    - **Do NOT** impersonate a character persona.
    - **DO** impersonate the **Genre Conversation Style**.
    - **Source:** Use `GenrePrompts.conversationDirective(genre)` to instruct the AI on the tone,
      pacing, and delivery style.

---

## 4. Technical Architecture

### 4.1 AudioGenClient

- **Pattern:** Follow `ImagenClient` and `TextGenClient` patterns.
- **Interface:** `AudioGenClient`
    - `suspend fun generateAudio(text: String, voice: Voice, tone: EmotionalTone): File?`
- **Implementation:** `AudioGenClientImpl`
    - Uses Firebase/Google Cloud TTS API.
    - Handles API calls, error handling, and file saving (via `FileHelper`).
- **Configuration:** Use `RemoteConfigService` to fetch API keys or model names if necessary.

---

## 4. Audio Prompt Requirements

We need a structured prompt to guide the audio generation effectively.

### 4.1 `AudioPrompt` Structure

* **Role/Persona:** "You are an actor playing [Character Name]. Your personality
  is [Personality Traits]."
* **Context:** "You are replying to a message in a [Genre] setting."
* **Tone:** "Your tone is [Emotional Tone (e.g., Sarcastic, Tender, Angry)]."
* **Constraints:**
    - "Max length: 1 minute."
    - "Do not read metadata or non-dialogue text."
    - "Speak naturally, including pauses or breaths if it fits the character."

### 4.2 Example Prompt Construction

> "Generate audio for the following text: '{message_text}'.
> Character: Captain Thorne.
> Voice Style: Gruff, authoritative, weary.
> Context: Recovering from a battle.
> Instruction: Speak typically fast but with a tired undertone. Max duration 60s."

---

## 5. Technical Tasks Summary

1. **Frontend:** Audio Recorder UI, Audio Player UI.
2. **Backend/API:** `AudioGenService` interface and implementation.
3. **Database:** Add `voiceId` to Character model, `audioUrl` to Message model.
4. **Logic:** Integrate calls in `MessageUseCaseImpl` (or a reactive observer) to trigger audio gen
   for premium users.
