# 🎙️ Audio Generation Feature

Premium feature that generates character/narrator voice audio for AI responses using Firebase TTS.

## Overview

When users send audio messages (transcribed), the AI not only replies with text but also generates
audio impersonating the character or a neutral narrator voice.

## Core Concepts

### Voice Persistence

- **Characters**: Get a voice assigned on their first audio generation → persists forever
- **Narrator**: Gets a voice assigned per saga → persists for all narrator messages in that saga
- **Selection**: AI analyzes character traits/personality to pick the most fitting voice

### Voice Options (Google Chirp 3 HD)

| Type     | Options                                        |
|----------|------------------------------------------------|
| Male     | `MALE_1`, `MALE_2`, `MALE_3`, `MALE_4`         |
| Female   | `FEMALE_1`, `FEMALE_2`, `FEMALE_3`, `FEMALE_4` |
| Narrator | `NARRATOR` (neutral)                           |

## Architecture

```
MessageUseCaseImpl.generateAudio()
    │
    ├─ [Premium Check] → billingService.runPremiumRequest()
    │
    ├─ [Voice Logic]
    │   ├─ Character? → Check Character.voice
    │   ├─ Narrator? → Check Saga.narratorVoice
    │   └─ Null? → AI selects from Voice enum
    │
    ├─ AudioGenClient.generateAudio()
    │   ├─ Build prompt (AudioPrompts)
    │   ├─ Gemma selects voice (if needed)
    │   ├─ Firebase TTS generates audio
    │   └─ Save to MP3 file
    │
    └─ [Database Updates]
        ├─ Character.voice / Saga.narratorVoice (if newly assigned)
        └─ Message.audioPath = file path
```

## File Storage

```
/app/files/sagas/{sagaId}/audio/
├── message_1_1702390400000.mp3
├── message_2_1702390500000.mp3
└── ...
```

## Database Changes (Version 7 → 8)

| Table        | New Column      | Type    |
|--------------|-----------------|---------|
| `messages`   | `audioPath`     | `TEXT?` |
| `Characters` | `voice`         | `TEXT?` |
| `sagas`      | `narratorVoice` | `TEXT?` |

## Key Files

| File                                                    | Purpose                           |
|---------------------------------------------------------|-----------------------------------|
| `core/ai/models/Voice.kt`                               | Voice enum with Google TTS IDs    |
| `core/ai/AudioGenClient.kt`                             | Audio generation interface + impl |
| `core/ai/prompts/AudioPrompts.kt`                       | Prompt generation for TTS         |
| `core/file/FileHelper.kt`                               | MP3 file save/read methods        |
| `features/saga/chat/data/usecase/MessageUseCaseImpl.kt` | Integration point                 |

## Premium Integration

Audio generation is wrapped in `billingService.runPremiumRequest()`:

- Premium users → Audio generated
- Non-premium users → `PremiumException` thrown

## Configuration

### Remote Config

```
audioGenModel: "gemini-2.0-flash" (or appropriate TTS model)
```

## Usage Example

```kotlin
// In ChatViewModel or wherever message response is handled
messageUseCase.generateAudio(
    saga = sagaContent,
    savedMessage = aiResponse,
    characterReference = character // null for narrator
)
```

