# Live Conversation Feature Plan

## Overview

A full-screen, voice-first way to play a saga, in the spirit of Gemini Live / ChatGPT Voice / Siri,
with one twist: the player doesn't talk to "the assistant", they talk to the **story**. Every turn:

1. The player holds a button and speaks as their character.
2. The recording is saved as a normal `USER` message **with its audio**.
3. The saga generates its reply exactly as it does in the chat (same pipeline, saved in background),
   except the reply request **hears the player's audio directly** and also returns the player's
   line as formatted text.
4. The reply is voiced **in the voice of whoever spoke it** (character voice, or the narrator's),
   saved as the message's audio, and played while that character's portrait lives inside a
   cosmic, Siri-like blob.

The live screen is only a different *surface* over the existing chat. The conversation it produces
is the same `messages` table, so leaving live mode drops the player back into a chat that already
has every line, playable through the existing `AudioMessagePlayer` bubbles.

## What we already have (and reuse)

| Need | Existing piece | Notes |
|---|---|---|
| Reply generation, saved in background | `ChatGenerationService.generate()` + `outcomes` | Singleton scope, survives leaving the screen. Live mode subscribes to `ChatGenerationOutcome.Success` to know when to voice the reply. |
| TTS per speaker | `MessageUseCase.generateAudio()` → `AudioGenClient` | Already picks `saga.narratorVoice` / `character.voice`, persists the chosen voice, saves `sagas/{id}/audios/message_{id}_audio.wav`, sets `audible` + `audioPath`. Currently **never called** (`generateExtraContent` has no callers) — this is the hidden feature. |
| Audio into a text request | `GeminiRequestBuilder.buildGeminiContentParts` | Already sends `inline_data` parts (images as `image/jpeg`). An `audio/wav` part is the same mechanism. |
| Speech-to-text (fallback only) | `TranscribeClient.transcribeWords()` (audiobook) | Used only when the reply can't take audio (see [Sending the player's voice](#sending-the-players-voice)). |
| Audio columns on messages | `Message.audible`, `Message.audioPath` | No migration needed for the MVP. |
| Audio in chat history | `ChatBubble` → `AudioMessagePlayer` | Already renders for any message with a valid `audioPath`, user messages included. |
| TTS quota state | `AudioGenClient.quotaStatus()` (`MediaRequirement.AUDIO`) | Same flow the audiobook uses for `ttsQuotaResetAt`. |
| Amplitude-driven visuals | `WaveformExtractor`, `AudiobookAurora` | Precomputed loudness curve from a WAV → drives blob pulse while a character speaks. |
| Cosmic visuals | `StarryTextPlaceholder` (star canvas), `themeBrushColors()`, `Genre.gradient(animated)`, `holographicGradient`, `ShaderGradients.kt`, `rotatingGradientBorder` | There is **no `StarryBackground` component today**; the star field lives in `StarryTextPlaceholder`. Plan: extract a `StarryBackground` alias/wrapper so the name matches intent. |
| Character switching | `ChatUiAction.UpdateCharacter`, `ChatState.selectedCharacter` | Reused by the Instagram-filter-style selector. |
| Permission UX | `PermissionComponent`, `PermissionService` | `RECORD_AUDIO`. |

### Gaps

- **No real audio recorder.** Today's mic flow (`AudioService`) uses `SpeechRecognizer`, which
  never gives us the audio file, so we can't save the player's voice. `AudioTranscriptionService`
  also references a `stopRecording()` that doesn't exist, and its Gemma call never attaches the
  audio (prompt only) — it can't transcribe anything as written.
- **Voicing is per message and lossy.** `generateAudio()` runs a `MEDIUM` call (`audio_config_blueprint`)
  to pick voice + style on every message, then `stripExpressiveTags` deletes every tagged block
  and reads the rest with one voice: narration inside a character message is never heard and
  vocal actions (a sigh) vanish. See [Voicing replies](#voicing-replies-casting--performance-script).

## Turn lifecycle (state machine)

A pure, unit-testable reducer (`LiveTurnState`) owned by `LiveConversationViewModel`:

```
Idle ──hold──▶ Listening ──release + gate ok──▶ Thinking ──reply saved──▶ Voicing ──▶ Speaking ──done──▶ Idle
  ▲               │ drag-out / <600ms / silence          │ error / guardrail      │ TTS fail/quota      │
  └───────────────┴──────────── Idle + hint ─────────────┴──── Recovering ───────┴─ SpeakingSilently ─┘

Any state ──leave screen / ON_STOP──▶ Closed        Any state ──milestone ready──▶ (finish turn) ──▶ MilestoneKey
```

- **Listening** — `VoiceRecorder` captures PCM, emits RMS for the blob.
- **Release** — a local audio-level gate (duration, RMS / voice activity) drops silence and
  accidental taps. Anything that passes is **sent straight away, no confirm/edit step** — an edit
  box kills the rhythm.
- **Thinking** — **the sender stays in focus:** the player's own portrait sits in the blob with a
  rotating gradient ring around it (the "assistant is working" loader) while the reply is on its
  way, so the natural wait reads as "your line was heard", not as an empty loading screen. When
  the reply lands, the portrait crossfades to whoever answered and the speaking animation starts.
  User `Message` saved with the audio and no text yet (status `LOADING`,
  `audible = true`, `audioPath` = player WAV, `inputMode = VOICE`), then
  `ChatGenerationService.generate()` with the audio attached. The reply returns the player's line
  as formatted text (see [Player message correction](#player-message-correction-inside-the-reply)),
  which fills the message and appears as the player's subtitle when the reply lands.
  `activeGenerations[sagaId].reasoning` can be shown faintly under the blob, like an assistant
  "thinking".
- **Voicing** — on `ChatGenerationOutcome.Success`, start the performance script + TTS for
  `reply.message` (~3–7 s). The reply's text is **not** shown yet (it would spoil the line before
  it's heard), but the player's corrected line is already there, so it **takes the stage**: shown
  as the main caption, in the display font, with its actions/thoughts in italic, while the sender
  stays in focus in the blob. The blob starts leaning toward the next speaker's color as
  anticipation. When the audio is ready, the portrait crossfades to whoever answered and Speaking
  starts. In silent mode (no TTS) this beat is kept short (~1 s) so the rhythm doesn't change.
- **Speaking** — play the WAV; blob pulses with the `WaveformExtractor` curve; subtitle shows the
  line.
- **SpeakingSilently** — degraded mode when voice isn't available (see Errors): same portrait,
  subtitle typewriter, no audio, no pulse.
- **Barge-in** — holding the button during `Speaking` stops playback immediately and goes to
  `Listening`. Nothing is cancelled or deleted: the reply keeps its full audio file, and the
  player can hear the rest from the chat bubble. The live screen never resumes an interrupted line.
- **Closed** — see [Leaving live mode](#leaving-live-mode).

## Architecture

```
features/live/
  data/
    VoiceRecorder.kt            // AudioRecord → 16-bit PCM → WAV (AudioUtils.wrapPcmInWav), RMS flow
    LiveTranscriber.kt          // fallback only: TranscribeClient when the reply can't take audio
    LiveVoiceUseCase.kt         // performance script → (multi-speaker) TTS for a saved reply
    VoiceCastingUseCase.kt      // one voice per character, in the background
  presentation/
    LiveConversationViewModel.kt
    LiveTurnState.kt            // sealed states + pure reducer (unit tested)
  ui/
    LiveConversationView.kt     // route, full screen
    CosmicBlob.kt               // blob + portrait morph
    HoldToTalkButton.kt
    SpeakerSelector.kt          // Instagram-filter carousel around the button
    LiveSubtitle.kt
    LiveReactionsOverlay.kt     // TikTok-live style floating reactions
```

- **Navigation — a full screen, never a sheet.** New `LiveConversationKey(sagaId: Int) : NavKey` in
  `NavKeys.kt`, registered in `SagaEntryProvider` like the other screens:

  ```kotlin
  entry<LiveConversationKey> { key ->
      LiveConversationView(
          sagaId = key.sagaId,
          onBack = { navigator.goBack() },
          onNavigate = { navigator.navigate(it) },
          sharedTransitionScope = sharedTransitionScope,
          animatedVisibilityScope = LocalNavAnimatedContentScope.current,
      )
  }
  ```

  Opened from the chat input: the hidden mic entry point (today `RequestAudioTranscript` →
  `AudioRecordingSheet`, a bottom sheet) becomes the "live" button that navigates to this key. The
  sheet isn't reused for live mode. Back is the system back / the close button →
  `navigator.goBack()`, which also ends the session (see [Leaving live mode](#leaving-live-mode)).
- **Feature flag:** remote-config boolean (`live_conversation_enabled`) + debug override, so it
  ships dark like the audio generation did.
- **Background guarantees:** text generation already lives in `ChatGenerationService`'s singleton
  scope, so the conversation is always saved. The voicing step for an in-flight reply also runs
  in a singleton scope (so the chat bubble still gets its audio), but **playback is owned only by
  the live screen** — see [Leaving live mode](#leaving-live-mode).

### Leaving live mode

Going back to the chat (back gesture, close button, or the app going to `ON_STOP`) **ends the
live session completely**:

- Recording is stopped and discarded; playback is stopped and released.
- The `LiveSession` object (the thing that listens to `ChatGenerationService.outcomes` and turns
  a reply into playback) is cancelled with the screen. Nothing outside the live route ever plays
  audio automatically — if a reply lands after the player left, it just shows up in the chat as a
  normal bubble (with audio if the voicing finished), and nobody talks.
- Text generation and voicing already started keep going silently so no message is lost or left
  half-made. Voicing that hasn't *started* when the player leaves is not started (saves TTS quota);
  that message can be voiced later from the chat's *Regenerate audio*.
- Re-entering live mode never replays old lines; it starts at Idle.

### Milestones (message limit)

The chat opens the Milestone screen from one place: `MainActivity` collects
`sagaContentManager.milestoneChainReady` and navigates to `MilestoneKey(sagaId)` **only if
`sagaNavigationTracker.isOnChatForSaga(sagaId)`**, after waiting for the saga's in-flight reply.
On the live route that check is false today, so the limit would silently pass. Plan:

- Add a `LiveConversationKey(sagaId)` and a `SagaNavigationTracker.isInConversation(sagaId)` that
  is true for `ChatKey` **or** `LiveConversationKey`. Use it in the milestone collector, in
  `observeMilestoneChainReadiness`, and in the chat-generation island check (so the "generating"
  island doesn't show on top of the live screen either).
- On the live route the collector also waits for the **turn to finish** (voicing + playback of the
  reply that hit the limit), not just the text, so the milestone doesn't cut a character
  mid-sentence. The live VM exposes a `turnInFlight` flow the collector can wait on (with a
  timeout so a stuck TTS can't hold the milestone forever).
- While a milestone is intrusive (`Loading`, `NewEvent`, `ChapterFinished`, `ActFinished` — the
  same set as `milestoneBlocksInput` in `ChatView`), the hold-to-talk button is disabled and the
  blob shows the "thinking" state.
- The back stack becomes Chat → Live → Milestone; closing the milestone returns to the live
  screen, same as it returns to the chat today.

### Recording the player

- `AudioRecord`, mono, 16 kHz, 16-bit (enough for transcription, ~32 KB/s — a 10 s line ≈ 320 KB).
- Written to cache while recording; after the user `Message` is saved (id known), moved to
  `sagas/{sagaId}/audios/message_{id}_audio.wav` — same naming the TTS side uses, so deletion and
  backups treat both alike.
- Minimum length 600 ms; max 60 s (auto-release with haptic) to stay far below the 20 MB inline cap.
- Later (optional): encode to AAC/Opus to cut storage ~10×.

### Sending the player's voice

**Decision: the audio goes straight into the reply request.** The reply runs on the `HIGH` tier,
which is a Gemini model, and Gemini accepts audio input. The earlier Gemma concern only applied to
a separate `LOW`/`MINIMAL` transcription request, which this design doesn't need.

- **How:** `GeminiRequestBuilder` gets an audio variant of `references` and adds an
  `inline_data` part with `mime_type: audio/wav`, next to the text prompt. The conversation history
  stays text; only the latest player turn is audio.
- **Cost:** Gemini counts audio at roughly 32 tokens per second, so a 10 s line is ~320 input
  tokens — less than the text history already in the prompt. At 16 kHz mono 16-bit, 60 s is about
  2 MB of WAV (≈2.7 MB as base64), far below the 20 MB inline cap.
- **Why it's better than transcribe-then-send:**
  - One request instead of two: saves the transcription round-trip (~1–2 s) and a TRANSCRIBE
    quota request per turn.
  - The model hears *how* the player spoke — whisper, shouting, sarcasm, hesitation — which
    feeds `playerInput.emotionalTone` and the reply itself. A transcript loses all of that.
  - Names get fixed with the full cast and canon in context, in the same step.
- **What changes because there's no text before the reply:**
  - `playerInput.correctedText` is the **only** text of the player's turn, so it's **required**
    for VOICE turns (still optional for TYPED).
  - The user message is saved with an empty `text` and `LOADING` status; the chat bubble shows
    the audio player with a "transcrevendo…" shimmer until the reply fills it in.
  - The noise gate has to be audio-level (duration + RMS / simple voice activity), since there's
    no transcript to check.

**Fallback to transcription**, when the reply can't take audio:

1. **The resolved `HIGH` model doesn't accept audio.** `model_configs` decides which models the
   tier rotates through; if a candidate that doesn't take audio (a Gemma model, for example) is
   picked by rotation, the request can't carry the audio part. `ModelCatalog` gets a
   `supportsAudioInput(model)` check. For such a model: transcribe once with `TranscribeClient`,
   cache the result for this turn, send it as text, and the turn continues as a normal VOICE turn
   (the correction bucket formats the transcript).
2. **The reply came back without `correctedText`** (or it fails the guard): transcribe the saved
   WAV with `TranscribeClient` after the fact and save that as the text, unformatted. The message
   never stays without text.
3. **Transcription also fails:** keep the message as an audio-only bubble with a
   "não consegui transcrever" marker and a retry action in the chat. The reply already happened,
   so the story isn't blocked.
4. **The reply itself fails:** the user message keeps its audio (status `ERROR`); retry resends
   the same WAV.

`AudioTranscriptionService` (the Gemma path that never attached the audio) is not needed anymore
and can be removed.

### Model config (`model_configs`, as of today)

| Tier | Primary | Rotation | Role in live mode |
|---|---|---|---|
| `HIGH` | `gemini-3.5-flash-lite`, thinking `high` | `gemini-3.5-flash-lite`, `gemini-3.6-flash`, `gemini-3.5-flash`, `gemini-3.8-flash` | The reply, **with the player's audio attached**. |
| `MEDIUM` | `gemini-3.5-flash-lite`, thinking `high` | + `gemini-3.1-flash-lite`, `gemini-2.5-flash-lite` | Voice casting, once per character. |
| `LOW` / `MINIMAL` | Gemma 4 | Gemma 4 | Performance script (text → TTS script) and reply fallout (reactions). Text only. |
| `AUDIO` | `gemini-2.5-flash-preview-tts` | only itself | Character/narrator voices. |
| `TRANSCRIBE` | `gemini-3.5-transcribe` | only itself | Fallback only. |

What this means:

- **Audio input is safe on `HIGH` today:** every model in the rotation is a Gemini flash /
  flash-lite, and none is Gemma. Fallback 1 (a non-audio model picked by rotation) can't happen
  with this config; `supportsAudioInput` stays as a guard in case the config changes. Before
  shipping, a debug action should send a short WAV to each `HIGH` candidate once to confirm, since
  the list is remote and can change without an app release.
- **Thinking `high` on every voice turn is the biggest latency cost we control.** The reply
  currently thinks at `high`. For voice turns, add an optional `voiceThinkingLevel` on the `HIGH`
  entry (e.g. `"medium"` or `"low"`), read only when the turn's `inputMode` is `VOICE`, so it's
  tunable from Firebase without touching the typed chat. Today `AIClient.thinkingLevel(requirement,
  model)` only reads the tier config, so this needs a small parameter through `GemmaClient.generate`
  → `GeminiGenerationEngine` (including the fallback path at `RetryWithModel`, which re-resolves the
  level per model). Absent field → same as today.
- **TTS has no rotation.** `AUDIO` has a single preview model, so when its quota runs out the
  session goes straight to *SpeakingSilently*. That's already the planned behavior; if another TTS
  model is available to the key, adding it to `availableModels` gives rotation for free
  (`AudioGenClient` already uses `withRotation`). Of all the tiers, this quota is the one live mode
  spends fastest — one TTS call per reply, each several seconds of audio.
- **`TRANSCRIBE` also has a single model.** Fine now that it's fallback-only; if it's spent, the
  voice message stays audio-only with a retry, and the story isn't blocked.
- **`MEDIUM` rarely runs:** only for voice casting, once per character, mostly in the background.

### Player message correction (inside the reply)

The selector only picks **who** the player speaks as; it has no Narrator / Action / Thought
modes. Something has to work out from the speech which parts are dialogue, actions, thoughts or
narration. Example:

> *"eu puxo a espada e digo: ninguém passa daqui. mas por dentro eu tô morrendo de medo"*
>
> → `<action>Puxo a espada.</action> Ninguém passa daqui. <think>Por dentro, estou morrendo de medo.</think>`

**Decision: no separate pre-request.** The reply generation already reads the player's message,
so it also returns the corrected version. This is **new behavior**: today nothing rewrites the
player's text. The closest precedent is `userTone`, which only sets the user message's
`emotionalTone` after the reply (`MessageUseCaseImpl.generateMessage`, around line 366); the
correction reuses that same "reply returns data about the player's message → update the user
message in the reply transaction" mechanism. This replaces both a separate live formatter and
`checkMessageTypo` (its blueprint was never published), for the typed chat **and** live mode, in
one place.

- **Model change:** group everything that is about *this* player input into one object, instead
  of loose fields on `AIReply`:

  ```kotlin
  data class AIReply(
      val message: Message,
      val sceneSummary: SceneSummary? = null,
      val newCharacter: NewCharacterDiscovery? = null,
      val playerCompass: String? = null,        // stays top-level: saga-wide, built across turns
      val playerInput: PlayerInputFeedback? = null,
  )

  data class PlayerInputFeedback(
      val correctedText: String? = null,         // null or equal = nothing to fix
      val understood: Boolean = true,
      val emotionalTone: EmotionalTone? = null,  // was AIReply.userTone
  )
  ```

  Anything else derived directly from the player's input later (e.g. who they addressed, a
  detected intent) goes in `PlayerInputFeedback` too. `playerCompass` is not about one input,
  so it stays where it is.
- **Where it's applied:** one `updateMessage(message.message.copy(text = …, emotionalTone = …))`
  in the reply transaction, replacing today's `reply.userTone?.let { … }`
  (`MessageUseCaseImpl.kt:366`, its only reader).
- **Propagate it to the fallout:** `ChatGenerationService` calls
  `resolveReplyFallout(userMessage = message.message)` with the **in-memory** message it started
  with, not a fresh read, so updating the DB alone isn't enough — reactions would still see the
  raw text. `StreamingState.Success` must carry the updated user message (e.g. a
  `userMessage: Message` on the success payload next to `reply.copy(message = savedMessage)`), and
  the service passes that to the fallout. Same fix makes the fallout see the `emotionalTone`,
  which it doesn't today.
- **Blueprint sync:** the output shape comes from the class via reflection, but the
  `reply_generation_blueprint` text names the field: `USER_TONE` says "Return the tone in the
  'userTone' field". Update it to `playerInput.emotionalTone` and publish it **with** the app
  version that ships the new class. Old app versions still ask for `userTone` through their
  schema; the stale wording only costs them the tone (nullable), nothing breaks.
- **Prompt:** one reply blueprint for both modes, **not** a duplicated "audio" copy. The
  correction rules live in their own Remote Config key, `player_input_blueprint` (draft in
  [`player_input_blueprint.json`](player_input_blueprint.json)), merged into the reply prompt with
  `mergeInstructions` — the same way `genreConfigService.conversationInstructions` is merged today.
  It has three buckets, and the use case merges the shared one plus **only** the one for the
  message's `inputMode`:
  - `PLAYER INPUT` (always): tag meanings, "classify, never write", names, output shape.
  - `PLAYER INPUT (TYPED)`: typo/name fixes only — short, so a typed turn barely grows.
  - `PLAYER INPUT (VOICE)`: filler, framing, meta talk, third → first person, unintelligible
    speech, and the voice examples.
  Tag meanings reference the existing directives (`ACTION_AS_PHYSICAL_CHANNEL`,
  `NARRATOR_PURPOSE`, `DIALECT_NO_SMOOTHING`) so player and NPC messages mean the same thing; the
  difference is the reply *writes* those channels, the correction only *sorts* what the player said.
  The `USER_TONE` directive stays in the reply blueprint (renamed to `playerInput.emotionalTone`).
- **`inputMode` in the context:** `latestMessage.inputMode = TYPED | VOICE`.
  - TYPED: fix spelling, typos and mangled names only; keep the player's tags, add none.
  - VOICE: full formatting — split into tags, drop filler, framing ("eu digo…") and meta talk
    ("apaga isso"), third person → first person.
  - Stored on `Message` as a new nullable `inputMode` column (Room migration) so the prompt, the
    UI and analytics can tell voice turns apart. Default `TYPED`.
- **Code-side guard** before applying (the model can still overreach): non-blank, every tag
  closed and no unknown tags.
  - TYPED: prose length (tags stripped) ≤ ~1.3× the original, and the original's tags still there.
    Fail → keep the original text.
  - VOICE: there's no original text to compare with, so only the structural checks apply; a
    sanity bound on length vs. recording duration (e.g. ≤ ~4 words per second) catches invented
    content. Fail → fallback 2 in [Sending the player's voice](#sending-the-players-voice).
- **Unintelligible voice:** the reply is already paid for by the time we know, so the bucket tells
  the model to answer **in scene** ("o quê?", a character leaning closer) and set
  `understood = false` instead of advancing the plot. The local audio gate (recording < 600 ms, or
  RMS never above a speech threshold) drops most of those turns first → Idle + hint, nothing sent.
- **Failure:** if the reply fails, the message stays (with `ERROR` status and retry). A TYPED
  retry resends the text; a VOICE retry resends the WAV.
- **Typed chat UX:** the bubble swapping text after the reply could surprise the player. The swap
  animates like the typewriter, and a small "corrigido" marker on the bubble can show the original
  on long-press. Needs keeping the original: nullable `originalText` column, set only when the
  correction changed something. For live mode, the audio file already is the original.
- **Cleanup:** remove `checkMessageTypo`, the `typoFixMessage` UI and the smart-suggestion branch
  in `ChatViewModel.sendInput` once this ships.

**Why it's worth it:**
- **One place:** one prompt, one parse, one update, for both input modes.
- **Better context:** the reply model sees the whole cast, scene and canon. That fixes names
  better than a small LOW model with a glossary.
- **Lower live latency:** the extra output is just the length of the player's line, and with the
  audio going straight in there's no separate transcription or formatting round-trip at all.

**Costs:**
- **Corrected text arrives late:** it only shows up when the reply ends.
- **Bigger reply prompt:** the correction adds instructions to an already large prompt, and they
  compete with the narration directives. Limited by merging only the mode's bucket; watch reply
  quality after publishing.
- **No early noise filter:** a noise turn costs a full reply, unless the local audio gate catches
  it.
- **No text until the reply lands:** the player's own line only appears as text when the reply
  arrives; until then the live screen shows only the blob, and the chat shows an audio bubble.

### Why one reply blueprint, not an audio copy

- **Audio is just a different input, not a different story.** The narration directives don't
  change because the player's turn arrives as audio instead of text. The only extra work (hearing
  the audio, writing the player's line as tagged text) lives in the `PLAYER INPUT (VOICE)` bucket,
  merged only for voice turns.
- **Live and chat share one story.** Both write to the same `messages` history, often in the same
  saga minutes apart. Two narration blueprints would drift (every tweak to a directive done twice,
  or forgotten once), and characters would start sounding different depending on how the player
  sent the message.
- **What actually differs is small and already parameterized:** the correction bucket (by
  `inputMode`) and, if needed, a lower `maxMessageLimit` for voice turns — shorter replies are
  faster to synthesize and easier to follow by ear. It's already a template variable
  (`ChatPrompts`, `MessageUseCaseImpl.kt:263`), no blueprint change needed.
- **Because the reply hears the audio first**, there is no text for the post-reply update to
  compare against: `playerInput.correctedText` is the only text version of the player's turn, so
  it's required for VOICE turns and the noise gate is audio-level (see
  [Sending the player's voice](#sending-the-players-voice)).

### Voicing replies: casting + performance script

Today `generateAudio()` asks `audio_config_blueprint` (MEDIUM) for `{voice, prompt, instruction}`
on **every** message, then `AudioGenClient.stripExpressiveTags` **deletes** every `<action>`,
`<think>` and `<narrator>` block and reads what's left with one voice. So narration inside a
character's message is never heard, and an action like a sigh just disappears instead of
becoming a sound. Two separate problems, solved separately:

#### 1. Voice casting — once per speaker, off the critical path

Choosing a voice is a per-character decision, not a per-message one, so it leaves the turn:

- New `voice_casting_blueprint`: character profile + gender + role + the voice guide
  (`Voice.getVoiceSelectionGuide()`: the 30 Gemini voices already have gender + a short
  description in `Voice.kt`) → `{ voice, voiceDirection }`. The narrator keeps `saga.narratorVoice`.
- **Shared voices are expected.** There are only 30 prebuilt voices, so characters in the same
  saga will sometimes share one. What makes them sound like different people is the **delivery**:
  pace, energy, pitch range, texture (breathy, rough, crisp), accent/register, emotional baseline
  ("fala rápido e atropelado", "voz baixa e melancólica, pausas longas"). Casting prefers a voice
  not yet used in the scene when two fit equally, but never picks a worse fit just to avoid a repeat.
- **`voiceDirection` is persisted** on the character (new nullable column next to `voice`) and fed
  to every performance script for that character, so the delivery stays consistent message after
  message instead of being reinvented per line. It's written from the character's personality,
  age and background — never a generic label.
- **When it runs:**
  - in the background right after a character is created (`CharacterUseCaseImpl` currently
    saves `voice = null`);
  - when the live screen opens: cast every character in the current scene that still has no
    voice (covers old sagas), before the player's first turn;
  - last resort, inside a turn, if a brand-new character speaks in the reply that introduces
    them — the only time casting sits on the critical path.
- Model: `MEDIUM` is fine — it runs once per character.
- Deterministic fallback if casting fails: pick from the voices matching the character's gender,
  seeded by character id (stable across retries), with no `voiceDirection`; casting retries in the
  background next time.

#### Voices move to Remote Config

The 30 voices (id, gender, description) are hardcoded today in the `Voice` enum
(`core/ai/model/Voice.kt`), so a new Gemini voice or a better description needs an app release.
They move to a Remote Config key, `tts_voices` (draft exported from the current enum in
[`tts_voices.json`](tts_voices.json)):

```json
{ "voices": [ { "id": "zephyr", "gender": "FEMALE", "description": "Energetic, bright…" } ] }
```

- `Voice` stops being an enum and becomes a data class `Voice(id, gender, description)`, served
  by a small `VoiceCatalog` that reads `tts_voices` and falls back to the bundled list (today's
  enum values) if the key is missing or malformed.
- Callers to migrate (7 files): `Voice.findByName` → `voiceCatalog.find(id)`,
  `Voice.getVoiceSelectionGuide()` → `voiceCatalog.selectionGuide()`, `AudioConfig.voice`,
  `createAudioGenerationRequest`, `AudioPrompts`, `MessageUseCaseImpl.generateAudio`,
  `BookAudioUseCaseImpl.narratorVoice` (its `book_audio_config.voices` list keeps working, now
  validated against the catalog).
- Stored values don't change: `Character.voice`, `saga.narratorVoice` and the audiobook's
  `narrationVoice` already persist the lowercase id, so no migration.
- Casting only offers voices from the catalog, and any id the model returns is validated against
  it (unknown → deterministic fallback).

#### 2. Performance script — per message, `LOW`/`MINIMAL` pre-request

A message is **text to read**; the audio is a **performance** of it. They won't match 1:1, and
that's intended. `audio_performance_blueprint` (Gemma, `LOW` or `MINIMAL` — text in, text out)
turns the tagged message into a TTS script:

- **Input:** the message already split into typed blocks (dialogue / action / think / narrator —
  the same parsing `RichTextParser` does), speaker name, the speaker's `voiceDirection`,
  `emotionalTone` and the scene brief.
- **Rules per block:**
  - **Dialogue** → spoken by the character, with delivery cues and audio tags where the text
    earns them (whispering, laughing, sighing, a pause).
  - **Narrator** → spoken by the narrator voice.
  - **Action** → never read as prose. Either it becomes a **vocal** sound the character's own
    voice can make (sigh, gasp, laugh, clearing the throat, a sharp breath), or a **delivery cue**
    for the next line ("leans in" → whispered), or it's **dropped** (walking, opening a door,
    anything the voice can't perform).
  - **Think** → never spoken. At most it colors the delivery of the spoken lines around it (a
    shaky voice on a line whose think shows fear), without revealing its content.
- **Output:**

  ```json
  {
    "style": "short direction for the whole clip (pace, mood)",
    "lines": [
      { "speaker": "NARRATOR", "text": "A porta range atrás deles.", "block": 0 },
      { "speaker": "Kael", "text": "[sighs] Ninguém passa daqui.", "block": 2 }
    ]
  }
  ```

  `block` points to the source block, so the live subtitle knows which part of the message is
  being spoken. Blocks without a line (dropped actions, thinks) are shown as italic subtitle
  lines between the spoken ones, never read.
- **TTS request:** Gemini TTS supports two speakers per request (`multiSpeakerVoiceConfig`), and
  one message has at most two: the character + the narrator. `createAudioGenerationRequest`
  gets a multi-speaker variant; a script with a single speaker keeps today's single-voice request.
- **No fixed tag list.** Gemini TTS is steered by natural language: a style direction for the clip
  plus short bracketed cues inline (whispering, laughing, a sigh, a pause). The blueprint teaches
  the **principles** of writing an audio prompt — direction first, cues only where the text earns
  them, never describe what can't be heard, never read a stage direction aloud — and a few
  examples, and the model decides what to include. Nothing to maintain in Firebase per tag.
  - Risk: a cue the TTS doesn't understand can be **read aloud** literally. The blueprint tells the
    model to prefer the clip-level `style` for anything unusual and keep inline cues to the common
    vocal ones. Worth a quick listening test on a handful of scripts before shipping.
- **Deterministic fallback** (Gemma failure / quota / timeout ~3 s): build the script in code —
  dialogue by the character, narrator blocks by the narrator, actions and thinks dropped, no tags.
  Still better than today, because narration is no longer lost. A voice turn never waits on this
  request failing.
- **Replaces** `audio_config_blueprint` for messages: voice now comes from casting and the style
  from the script, so the `MEDIUM` call per message goes away. `stripExpressiveTags` stays only as
  a safety net on the final TTS text (the script shouldn't contain our tags anymore).
- **Same path for the chat:** *Regenerate audio* in the chat uses casting + script too, so a
  message sounds the same whether it was voiced live or later.

#### Text vs. audio mismatch

- The audio is a performance, not a reading: some actions become sounds, others are silent, and
  thinks are never heard. That's by design and applies to the chat bubble player too.
- **Live subtitle:** shows the full message. The block being spoken is highlighted; silent blocks
  (actions, thinks) appear as italic lines at their position. Timing is estimated per line from
  its share of the clip's characters (the audiobook already has a character-estimate fallback);
  no transcription call in live mode.
- Optionally store the script on the message (nullable `audioScript` JSON column) so the chat
  player can highlight the same way and regenerating is deterministic. Not needed for the MVP.

#### Latency and chunking

- Critical path per voiced reply: script (Gemma `MINIMAL`/`LOW`, ~1 s) → TTS. Casting is already
  done by then in almost every turn.
- Phase 4: synthesize the script line by line (or in small groups) and start playing the first
  line while the next ones generate — first sound much earlier. The clips are concatenated into
  the single message WAV once all finish. A barge-in stops **playback only**; the remaining lines
  still finish so the saved file is the complete performance.
- Reuse `generateAudio()`'s save/update path so the chat bubble sees the audio.

### Sender types

| Reply sender | Blob content | Voice |
|---|---|---|
| `CHARACTER` | character portrait (`Character.image`), glow in `hexColor` | `character.voice` |
| `NARRATOR` | saga icon / pure cosmic blob, genre gradient | `saga.narratorVoice` |
| `ACTION` / `THOUGHT` | portrait dimmed / italic subtitle | per the performance script: vocal sounds or silence; thoughts never spoken |

## UI

### Layers (back → front)

1. **StarryBackground** (the `StarryTextPlaceholder` star canvas) over a near-black surface, with
   a slow `Genre.gradient(animated = true)` radial wash so each saga keeps its palette.
2. **CosmicBlob** centered, ~60% of width.
3. Speaker name + **LiveSubtitle** under the blob.
4. **HoldToTalkButton** + **SpeakerSelector** at the bottom; close button and saga title on top.

### CosmicBlob

- AGSL shader on API 33+ (metaball-like noise blob with a gradient from `themeBrushColors()` /
  `holographicGradient`, heavy outer glow); fallback for older APIs: 3–4 blurred, offset circles
  with animated radii — same look, cheaper.
- Driven by two inputs: `level` (0..1) and `mode`.
  - **Idle:** slow breathing, low glow.
  - **Listening:** expands, reacts to mic RMS, colors lean toward the player's character color.
  - **Thinking:** the **sender's portrait** in the blob with a rotating gradient ring (like
    `rotatingGradientBorder`) and the reasoning shimmering below — the "personal assistant"
    loader, but showing who just spoke.
  - **Speaking:** the blob becomes a soft mask around the speaker's portrait (crossfade + scale),
    glow in the speaker's color, pulsing with the waveform curve.
  - **Error:** brief dim + desaturate, then back to Idle.

### HoldToTalkButton (+ selector)

- **Hold to talk** (not always-open mic): press → haptic + Listening; release → send.
  Drag up out of the button → "solte para cancelar" (discard). Quick tap → tooltip "segure para
  falar".
- Button ring = live RMS meter while holding; a timer appears after 5 s.
- **SpeakerSelector:** horizontal carousel of avatars around the button, exactly like Instagram
  Stories filters — the one in the center slot *is* the button. Swiping changes who the player
  speaks as (`UpdateCharacter`), with a haptic tick per snap. Swiping is disabled while holding.
  **Characters only** — no Narrator/Action/Thought modes; those are inferred by the reply's player message correction.
- Disabled (dimmed, no haptic) while a milestone is intrusive.
- Phase 2+: slide-up "lock" for long speeches.

### Live reactions (orbiting the portrait)

Reactions already arrive a beat after each reply: `ChatGenerationService` launches
`resolveReplyFallout()`, which saves `Reaction(messageId, characterId, emoji, thought)` rows for
both the reply and the player's message. The live screen observes reactions for the messages of
the current session and shows each **new** one as it lands, **orbiting whoever is in focus** in
the blob instead of floating over the screen, so they never cover the captions:

- Each reaction is a bead on a ring around the portrait: the emoji with a small avatar of the
  reacting character. It pops in, drifts slowly along the ring and fades after a few seconds.
- Beads only use the **upper arc and the sides** of the ring. The lower arc is kept clear because
  the speaker label and captions sit right below the blob.
- Now and then (not every reaction), the bead carries the reaction's `thought` as a small bubble
  attached to it, kept inside the screen bounds, TikTok-comment style.
- Staggered (~400–500 ms apart), capped on screen, and slot angles spread so beads don't stack.
- Only reactions created during this session animate — entering live mode doesn't replay old ones.
- A reaction to the player's own line can make the blob flicker briefly in the reacting
  character's color.

### Transitions & motion

The live screen has to feel like it grows out of the chat, not like a new page loading on top.

**Shared transitions (chat ⇄ live)**, through the `sharedTransitionScope` +
`LocalNavAnimatedContentScope` pair the other entries already receive:

| Chat (`ChatView` / `ChatInputView`) | Live (`LiveConversationView`) | Modifier |
|---|---|---|
| Selected character's `CharacterAvatar` in the input | Face of the hold-to-talk button | `sharedElement` |
| Input bar container | Bottom dock (selector + button) | `sharedBounds` |
| `SagaTopBar` title + `Volume · Capítulo` subtitle | Live top bar title + subtitle | `sharedElement` (text) |

- Use **dedicated keys** namespaced by saga, e.g. `live_${sagaId}_speaker`, `live_${sagaId}_dock`,
  `live_${sagaId}_title`. Don't reuse `character_${id}_icon`: `ChatBubble` puts that key on every
  bubble of the same character, so several elements share it on screen and the transition has no
  single source.
- The global `NavDisplay` transition is a fade (push) and a vertical slide (pop); shared elements
  fly across it. On pop, the dock shrinks back into the input bar and the avatar lands back in the
  input — the chat is already showing the messages the live session wrote.
- **Entry choreography** (after the shared elements land): stars fade in first, then the blob is
  born from the button — scaling up from the dock toward the center with a spring — then the top
  bar and captions fade in. Exit runs the reverse, shorter.

**Motion inside the screen:**

- **Everything is state-driven, never jumpy:** the blob's `level`, size, orbit speed and palette
  come from `animateFloatAsState` / `animateColorAsState` with springs (medium-low stiffness,
  no bounce on colors), so switching Listening → Thinking → Speaking morphs instead of cutting.
  Mic RMS and playback loudness feed a smoothed target, not the raw value.
- **Portrait ⇄ blob:** an `updateTransition` on the speaker drives the portrait's alpha, scale and
  mask radius together; changing speaker (narrator → character) crossfades through the blob.
- **Captions:** `AnimatedContent` for the speaker label and the player's line settling into its
  corrected version; the karaoke highlight animates color, and the caption window scrolls with an
  animated offset.
- **Hold-to-talk:** press scales the face down and the ring up (spring), haptic on press, release,
  cancel-arm and each selector snap. The selector is a snapping `LazyRow` / pager with
  `animateItem`.
- **Reactions:** each floating emoji is its own `Animatable` (position, scale, rotation, alpha)
  with a staggered start; comment chips enter and leave with `AnimatedVisibility`.
- **Frame budget:** the stars and the blob draw in `Canvas` / `drawWithCache` (AGSL on API 33+),
  and animated values are read in the draw phase (lambda modifiers like `graphicsLayer { }`), so a
  pulse doesn't recompose the screen. Animations pause with `rememberLifecycleAnimationsActive()`
  and slow down when the system's reduce-motion setting is on.

## Errors & limits

| Situation | Behavior |
|---|---|
| TTS daily quota already spent **before** entering | Entry still allowed, but a banner says voices rest until `HH:mm` (from `QuotaStatus.DailyExhausted.until`); session runs in *SpeakingSilently*. |
| TTS quota hits **mid-session** (`QuotaExhaustedException`) | Message is already saved as text. Switch the session to *SpeakingSilently* for the rest of the session, show the reset time once. Message stays `audible = false` (not `true` — see note below), so it can be voiced later with the existing *Regenerate audio*. |
| TTS other failure / timeout | Same silent fallback for that turn only; keep trying next turn. |
| Reply model can't take audio (rotation picked a non-audio model) | Transcribe with `TranscribeClient` first, send as text. |
| Reply missing `correctedText` / fails the guard | Transcribe the saved WAV after the fact; if that fails too, audio-only bubble with retry. |
| Text generation error | `ChatGenerationOutcome.Error` → blob error state + **Try again** (`retryAiResponse`). |
| Guardrail block | User message is deleted by the service; show a gentle notice, return to Idle. |
| Missing API key | `ApiKeyTroubleSheet`. |
| No `RECORD_AUDIO` permission | `PermissionComponent`, then back to Idle. |
| Too short / silence (local audio gate) | Idle + hint, nothing sent. |
| Noise got past the gate (`playerInput.understood = false`) | The reply already reacts in scene ("o quê?"); nothing else to do. |
| Correction missing or fails the code-side guard | Keep the raw text. |
| Phone call / audio focus loss | Stop recording (discard) or stop playback; generation keeps going. |
| Leaving the screen mid-turn (back, close, `ON_STOP`) | Live session ends completely; nothing ever plays outside the live screen. Started work finishes silently, unstarted voicing is skipped. |
| Message limit reached | Turn finishes (reply voiced and played), then the same Milestone screen as the chat opens. |

Note: `regenerateAudio()`'s failure path currently sets `audible = true` on failure, which makes
the bubble think it should have audio. Worth fixing while we're here.

## Phases

1. **Foundations** — `VoiceRecorder` + audio gate, audio parts in `GeminiRequestBuilder`,
   `ModelCatalog.supportsAudioInput`, `voiceThinkingLevel` on the `HIGH` tier, `VoiceCatalog`
   (`tts_voices` on Remote Config, replacing the `Voice` enum),
   `LiveTranscriber` fallback (remove `AudioTranscriptionService`),
   `AIReply.playerInput` (`PlayerInputFeedback`) + `inputMode` / `originalText` columns + `player_input_blueprint` (this
   also ships the typo fix for the typed chat), `LiveVoiceUseCase` (casting + performance script + multi-speaker TTS), feature flag,
   `LiveConversationKey` entry in `SagaEntryProvider` + `isInConversation()` in the navigation
   tracker / milestone collector.
   Unit tests for the reducer and the correction guard.
2. **MVP loop** — hold → audio gate → save user msg with audio → generate (audio in) → voice →
   play, with a simple blob (Canvas fallback), the silent fallback, full stop on leaving, and the
   milestone link. Behind the flag.
3. **Magic pass** — shared transitions chat ⇄ live, entry choreography, AGSL blob, portrait morph,
   speaker selector carousel, live reactions overlay, haptics, subtitles, reasoning shimmer.
4. **Latency pass** — line-by-line TTS, pre-warming, measuring per-stage timings in `AIAuditRecorder`.
5. **Hardening** — full error matrix, audio focus, long sessions, storage (AAC).

## Risks

- **Latency is the real risk.** Rough budget per turn: reply with audio in 3–10 s + script ~1 s +
  TTS 2–6 s ≈ 6–17 s. The Thinking/Voicing animations and the live reactions have to carry that wait; the
  background casting and line-by-line TTS are what make it feel "live".
- **Quota:** each turn costs 1–2 text + 1 TTS request against the user's key (plus the existing
  fallout call); a transcribe request only on fallback.
- **Model rotation:** today's `HIGH` rotation is all Gemini (audio-capable). If a non-audio model is
  ever added there, those turns pay for an extra transcription.
- **TTS quota:** `AUDIO` has one preview model and no rotation; it's the quota live mode burns
  fastest. The silent mode covers it, but a second TTS candidate would stretch sessions.
- **Correction mistakes:** with no edit step, a wrong tag split goes straight into the story. The
  bucket leans on "dialogue when unsure", and the code-side guard falls back to the raw text.
- **Reply prompt weight:** the correction buckets compete with the narration directives; only the
  bucket for the current `inputMode` is merged, and reply quality should be watched after
  publishing.
- **Storage:** every turn stores two WAVs.

## Decisions

1. Selector = characters only. Action / narration / thought are inferred from speech by the
   reply request itself (`AIReply.playerInput`), which also replaces `checkMessageTypo` for the
   typed chat. No separate pre-request.
2. Barge-in stops playback only; the full audio stays on the message, playable from the chat.
3. No confirm/edit step: the recording is sent straight away; the reply hears it and returns the
   player's line as formatted text.
4. Reactions show in real time on the live screen, orbiting the portrait in focus (upper arc and
   sides only) so they never cover the captions.
5. Leaving live mode ends it completely; no audio ever plays outside the live screen.
6. Hitting the message limit opens the same Milestone screen as the chat, after the turn finishes.
7. The audio goes straight into the reply request (`HIGH` is Gemini, which accepts audio);
   `TranscribeClient` is only a fallback.
8. Voice is cast once per character (background, `MEDIUM`) with a persisted `voiceDirection`;
   shared voices are fine because delivery is what tells characters apart. Each message gets a performance
   script (`LOW`/`MINIMAL`) before TTS. Audio is a performance of the text, not a reading of it:
   actions become sounds or silence, thinks are never spoken, narration uses the narrator voice.
9. Live mode is its own screen (`LiveConversationKey` in `SagaEntryProvider`), never a sheet, with
   shared transitions from the chat input and spring-driven motion throughout.
10. While waiting for the reply, the player's portrait stays in focus in the blob; when the reply
    lands it crossfades to whoever answered.
11. The audio is made after the reply text, so during that gap the player's corrected line is the
    main caption (the reply's text stays hidden until its audio plays).
