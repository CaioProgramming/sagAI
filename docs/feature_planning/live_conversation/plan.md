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
Idle ──hold──▶ Listening ──release──▶ Transcribing ──▶ Thinking ──reply saved──▶ Voicing ──▶ Speaking ──done──▶ Idle
  ▲               │ drag-out / <600ms            │ empty / fail        │ error / guardrail      │ TTS fail/quota      │
  └───────────────┴──────────── Idle + hint ─────┴──── Recovering ─────┴──── Recovering ───────┴─ SpeakingSilently ─┘
```

- **Listening** — `VoiceRecorder` captures PCM, emits RMS for the blob.
- **Transcribing** — WAV → `TranscribeClient`. Result shown briefly as the player's subtitle.
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
  `Listening`. The interrupted message keeps its audio file.

## Architecture

```
features/live/
  data/
    VoiceRecorder.kt            // AudioRecord → 16-bit PCM → WAV (AudioUtils.wrapPcmInWav), RMS flow
    LiveTranscriber.kt          // TranscribeClient first, fallbacks below
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
```

- **Navigation:** new `NavKeys.LiveConversation(sagaId)` entry; opened from the chat input (the
  hidden mic entry point becomes the "live" button).
- **Feature flag:** remote-config boolean (`live_conversation_enabled`) + debug override, so it
  ships dark like the audio generation did.
- **Background guarantees:** text generation already lives in `ChatGenerationService`'s singleton
  scope. The voicing step for a reply must live there too (or a sibling singleton), **not** in
  `viewModelScope` — otherwise leaving the screen mid-turn leaves a message without audio. Leaving
  the screen stops *playback* only.

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

Skip the typo check (`checkMessageTypo`) in live mode — it's another round-trip and the
transcript is already model-cleaned.

### Voicing replies fast (`LiveVoiceUseCase`)

- If the speaker already has a voice (`character.voice` / `saga.narratorVoice`), **skip the Gemma
  `AudioConfig` call**: build the TTS instruction locally from `emotionalTone` + sender type.
  Only speakers without a voice go through the existing config call once (which then persists it).
- Phase 3: split long replies into sentences (the `BookAudioSegmenter` idea) and synthesize chunk
  1 while chunk 2 is generated — first sound much earlier. Chunks are concatenated into the single
  message WAV once all finish.
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
- Phase 2+: slide-up "lock" for long speeches.

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
| Too short / silence / empty transcript | Idle + hint, nothing saved. |
| Phone call / audio focus loss | Stop recording (discard) or pause playback; generation keeps going. |
| Leaving the screen mid-turn | Text + audio finish in background; playback stops. |

Note: `regenerateAudio()`'s failure path currently sets `audible = true` on failure, which makes
the bubble think it should have audio. Worth fixing while we're here.

## Phases

1. **Foundations** — `VoiceRecorder`, `LiveTranscriber` (+ fix `AudioTranscriptionService`),
   `LiveVoiceUseCase` (fast path), feature flag, route. Unit tests for the reducer.
2. **MVP loop** — hold → transcribe → save user msg with audio → generate → voice → play, with a
   simple blob (Canvas fallback) and the silent fallback. Behind the flag.
3. **Magic pass** — AGSL blob, portrait morph, speaker selector carousel, haptics, subtitles,
   reasoning shimmer.
4. **Latency pass** — chunked TTS, pre-warming, measuring per-stage timings in `AIAuditRecorder`.
5. **Hardening** — full error matrix, audio focus, long sessions, storage (AAC).

## Risks

- **Latency is the real risk.** Rough budget per turn: transcribe 1–2 s + reply 3–10 s + TTS
  2–6 s ≈ 6–18 s. The Thinking/Voicing animations have to carry that wait; the fast path and
  chunked TTS are what make it feel "live".
- **Quota:** each turn now costs 1 transcribe + 1–2 text + 1 TTS request against the user's key.
- **Storage:** every turn stores two WAVs.

## Open questions

1. Should the selector include the non-speech modes (Narrator / Action / Thought from
   `SpeechModeSheet`), or only characters?
2. Barge-in: should interrupting a character also cancel that reply's remaining chunks, or just
   stop playback?
3. Show the player's transcript before sending (confirm/edit), or send straight away? Plan assumes
   straight away with a short "undo" window.
4. Should reactions / `resolveReplyFallout` hooks surface in the live screen (e.g. an emoji
   floating up from the blob), or stay chat-only?
