# Live Conversation Feature Plan

## Overview

A full-screen, voice-first way to play a saga, in the spirit of Gemini Live / ChatGPT Voice / Siri,
with one twist: the player doesn't talk to "the assistant", they talk to the **story**. Every turn:

1. The player holds a button and speaks as their character.
2. The speech is transcribed, saved as a normal `USER` message **with its audio**.
3. The saga generates its reply exactly as it does in the chat (same pipeline, saved in background).
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
| Speech-to-text | `TranscribeClient.transcribeWords()` (audiobook) | Word-level timestamps, quota rotation via `MediaModelResolver`. Needs WAV input. |
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
- **Latency.** `generateAudio()` runs a Gemma `MEDIUM` call to craft an `AudioConfig` before TTS
  on every message. Acceptable for a background extra, too slow for a live turn.

## Turn lifecycle (state machine)

A pure, unit-testable reducer (`LiveTurnState`) owned by `LiveConversationViewModel`:

```
Idle ──hold──▶ Listening ──release──▶ Transcribing ──▶ Formatting ──▶ Thinking ──reply saved──▶ Voicing ──▶ Speaking ──done──▶ Idle
  ▲               │ drag-out / <600ms            │ empty / fail                   │ error / guardrail      │ TTS fail/quota      │
  └───────────────┴──────────── Idle + hint ─────┴──────── Recovering ────────────┴──── Recovering ───────┴─ SpeakingSilently ─┘

Any state ──leave screen / ON_STOP──▶ Closed        Any state ──milestone ready──▶ (finish turn) ──▶ MilestoneKey
```

- **Listening** — `VoiceRecorder` captures PCM, emits RMS for the blob.
- **Transcribing** — WAV → `TranscribeClient`. Raw text is not shown yet.
- **Formatting** — the pre-request that turns speech into a chat message (see
  [Speech → message formatting](#speech--message-formatting)). The formatted line then appears as
  the player's subtitle. **Sent straight away, no confirm/edit step** — giving the player an edit
  box kills the rhythm; the formatter is what keeps quality up.
- **Thinking** — user `Message` saved (status `LOADING`, `audible = true`, `audioPath` = player
  WAV), then `ChatGenerationService.generate()`. `activeGenerations[sagaId].reasoning` can be shown
  faintly under the blob, like an assistant "thinking".
- **Voicing** — on `ChatGenerationOutcome.Success`, call the live variant of `generateAudio()`
  for `reply.message`. The blob starts morphing toward the speaker's portrait here, so the wait
  already feels like "they're about to speak".
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
    LiveTranscriber.kt          // TranscribeClient first, fallbacks below
    LiveInputFormatter.kt       // pre-request: transcript → chat message with expressive tags
    LiveVoiceUseCase.kt         // fast TTS path for a saved reply
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

- **Navigation:** new `NavKeys.LiveConversation(sagaId)` entry; opened from the chat input (the
  hidden mic entry point becomes the "live" button).
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

### Transcription fallbacks

1. `TranscribeClient` (TRANSCRIBE tier, rotates models on 503/quota).
2. Gemma multimodal with the audio inlined (fixing `AudioTranscriptionService` to actually send it).
3. Give up gracefully: show "não consegui te ouvir" with **Try again** and **Type instead** (a
   small text field inside the live screen). The recording is discarded only if the player cancels.

### Speech → message formatting

The selector only picks **who** the player speaks as; it has no Narrator / Action / Thought
modes. The model has to work out from the speech itself which parts are dialogue, which are
actions and which are narration or thoughts. Example:

> *"eu puxo a espada e digo: ninguém passa daqui. mas por dentro eu tô morrendo de medo"*
>
> → `<action>Puxo a espada.</action> Ninguém passa daqui. <think>Por dentro, estou morrendo de medo.</think>`

`LiveInputFormatter` is a single pre-request between transcription and sending:

- **Input:** the raw transcript, the speaking character (name, personality), the characters in
  the scene and the saga's proper nouns (character/place names — transcription mangles fantasy
  names), the last couple of messages for context, and the `ExpressiveTag` rules the chat already
  uses (`<action>`, `<think>`, `<narrator>`).
- **Output (JSON):** `{ "text": "<formatted message with tags>", "understood": true|false }`.
- **Rules:** keep the player's intent and wording, no new content. Remove filler ("é…", "tipo",
  false starts), fix names against the glossary, split into tags, write in the saga's language.
  `understood = false` when the transcript is noise → handled like an empty transcript.
- **Model:** Gemma `ModelRequirement.LOW` with a small, cached prompt — it's on the critical path,
  so it has to be fast. The prompt lives in a new `LivePrompts` next to `AudioPrompts`.
- **Replaces the typo check:** `checkMessageTypo` is skipped in live mode. The formatter already
  does that job, and the message is **sent straight away with no confirm/edit step**.
- **Fallback:** if the formatter fails or times out (~4 s), send the cleaned raw transcript as
  plain dialogue. A formatting failure never blocks the turn.
- **Later optimization:** one multimodal Gemini call that takes the audio and returns the
  formatted message in one step (saves a round-trip). Needs checking whether `GemmaClient` /
  the request model accept inline audio; keep the two-step path as fallback.

The formatted text is what gets saved to `Message.text`, so the chat shows the same tagged bubbles
as if the player had typed them with the tag inserter.

### Voicing replies fast (`LiveVoiceUseCase`)

- If the speaker already has a voice (`character.voice` / `saga.narratorVoice`), **skip the Gemma
  `AudioConfig` call**: build the TTS instruction locally from `emotionalTone` + sender type.
  Only speakers without a voice go through the existing config call once (which then persists it).
- Phase 3: split long replies into sentences (the `BookAudioSegmenter` idea) and synthesize chunk
  1 while chunk 2 is generated — first sound much earlier. Chunks are concatenated into the single
  message WAV once all finish. A barge-in stops **playback only**; the remaining chunks still
  finish so the saved file is the complete line.
- `AudioGenClient.stripExpressiveTags` drops `<action>`, `<think>`, `<narrator>` content from the
  TTS text. In live mode those parts still show as italic subtitle lines around the spoken part,
  so the player doesn't miss what the character *did*.
- Reuse `generateAudio()`'s save/update path so the chat bubble sees the audio.

### Sender types

| Reply sender | Blob content | Voice |
|---|---|---|
| `CHARACTER` | character portrait (`Character.image`), glow in `hexColor` | `character.voice` |
| `NARRATOR` | saga icon / pure cosmic blob, genre gradient | `saga.narratorVoice` |
| `ACTION` / `THOUGHT` | portrait dimmed / italic subtitle | narrator voice, softer instruction |

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
  - **Thinking:** contracts, gradient rotates (like `rotatingGradientBorder`), shimmer — the
    "personal assistant" loader.
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
  **Characters only** — no Narrator/Action/Thought modes; those come from the formatter.
- Disabled (dimmed, no haptic) while a milestone is intrusive.
- Phase 2+: slide-up "lock" for long speeches.

### Live reactions (TikTok-live style)

Reactions already arrive a beat after each reply: `ChatGenerationService` launches
`resolveReplyFallout()`, which saves `Reaction(messageId, characterId, emoji, thought)` rows for
both the reply and the player's message. The live screen observes reactions for the messages of
the current session and shows each **new** one as it lands:

- The emoji floats up from the bottom-right edge with a small avatar of the reacting character,
  drifts and fades (random x-jitter, slight rotation, scale pop), like hearts on a live stream.
- Every so often (not every reaction, so it doesn't turn into a wall of text), a short
  `thought` pops as a small comment chip from that character in the lower-left corner, TikTok
  comment style, fading after a few seconds.
- Staggered queue (~300–500 ms apart) so a burst doesn't pile up; capped on screen.
- Only reactions created during this session animate — entering live mode doesn't replay old ones.
- A reaction to the player's own line can make the blob flicker briefly in the reacting
  character's color.

## Errors & limits

| Situation | Behavior |
|---|---|
| TTS daily quota already spent **before** entering | Entry still allowed, but a banner says voices rest until `HH:mm` (from `QuotaStatus.DailyExhausted.until`); session runs in *SpeakingSilently*. |
| TTS quota hits **mid-session** (`QuotaExhaustedException`) | Message is already saved as text. Switch the session to *SpeakingSilently* for the rest of the session, show the reset time once. Message stays `audible = false` (not `true` — see note below), so it can be voiced later with the existing *Regenerate audio*. |
| TTS other failure / timeout | Same silent fallback for that turn only; keep trying next turn. |
| Transcribe quota / failure | Fallback chain above; never lose the recording silently. |
| Text generation error | `ChatGenerationOutcome.Error` → blob error state + **Try again** (`retryAiResponse`). |
| Guardrail block | User message is deleted by the service; show a gentle notice, return to Idle. |
| Missing API key | `ApiKeyTroubleSheet`. |
| No `RECORD_AUDIO` permission | `PermissionComponent`, then back to Idle. |
| Too short / silence / empty transcript / formatter says `understood = false` | Idle + hint, nothing saved. |
| Formatter fails / times out | Send the cleaned raw transcript as plain dialogue. |
| Phone call / audio focus loss | Stop recording (discard) or stop playback; generation keeps going. |
| Leaving the screen mid-turn (back, close, `ON_STOP`) | Live session ends completely; nothing ever plays outside the live screen. Started work finishes silently, unstarted voicing is skipped. |
| Message limit reached | Turn finishes (reply voiced and played), then the same Milestone screen as the chat opens. |

Note: `regenerateAudio()`'s failure path currently sets `audible = true` on failure, which makes
the bubble think it should have audio. Worth fixing while we're here.

## Phases

1. **Foundations** — `VoiceRecorder`, `LiveTranscriber` (+ fix `AudioTranscriptionService`),
   `LiveInputFormatter` + `LivePrompts`, `LiveVoiceUseCase` (fast path), feature flag,
   `LiveConversationKey` + `isInConversation()` in the navigation tracker / milestone collector.
   Unit tests for the reducer and the formatter's fallback.
2. **MVP loop** — hold → transcribe → format → save user msg with audio → generate → voice →
   play, with a simple blob (Canvas fallback), the silent fallback, full stop on leaving, and the
   milestone link. Behind the flag.
3. **Magic pass** — AGSL blob, portrait morph, speaker selector carousel, live reactions overlay,
   haptics, subtitles, reasoning shimmer.
4. **Latency pass** — chunked TTS, pre-warming, measuring per-stage timings in `AIAuditRecorder`.
5. **Hardening** — full error matrix, audio focus, long sessions, storage (AAC).

## Risks

- **Latency is the real risk.** Rough budget per turn: transcribe 1–2 s + format ~1 s + reply
  3–10 s + TTS 2–6 s ≈ 7–19 s. The Thinking/Voicing animations and the live reactions have to
  carry that wait; the fast voice path, the single-call transcribe+format and chunked TTS are what
  make it feel "live".
- **Quota:** each turn now costs 1 transcribe + 1 format + 1–2 text + 1 TTS request against the
  user's key (plus the existing fallout call).
- **Formatter mistakes:** with no edit step, a wrong tag split goes straight into the story. The
  prompt must lean on "keep it as dialogue when unsure".
- **Storage:** every turn stores two WAVs.

## Decisions

1. Selector = characters only. Action / narration / thought are inferred from speech by the
   formatter pre-request.
2. Barge-in stops playback only; the full audio stays on the message, playable from the chat.
3. No confirm/edit step: the formatted message is sent straight away.
4. Reactions show in real time on the live screen, TikTok-live style.
5. Leaving live mode ends it completely; no audio ever plays outside the live screen.
6. Hitting the message limit opens the same Milestone screen as the chat, after the turn finishes.
