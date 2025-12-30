# Audio Transcription in NewSagaUseCase

## Overview

Integration of audio recording and transcription into the saga creation flow via
`NewSagaUseCaseImpl`.

## Architecture

### Components

1. **AudioTranscriptionService** - Transcribes audio files using Gemma
2. **NewSagaUseCaseImpl** - Updated to accept and process audio files
3. **GemmaClient** - Already supports audio via BlobPart

## Data Flow

```
User Records Audio (AudioService)
        ↓
Audio saved in cache: app-cache/file_cache/audio_recordings/audio_xxx.m4a
        ↓
User sends form with audio: replyAiForm(audioFile = file)
        ↓
AudioTranscriptionService.transcribeAudio(audioFile)
        ↓
Gemma receives: "Transcribe this audio and correct pronunciation..."
        ↓
Gemma processes BlobPart with audio
        ↓
Returns transcription: "User's corrected speech text"
        ↓
Transcription used as userInput for form processing
        ↓
GemmaClient.generate<SagaForm>(
    extractDataFromUserInputPrompt(
        userInput = transcription  // ← Gemma-corrected text
    )
)
        ↓
Form field extraction proceeds normally
```

## NewSagaUseCase Changes

### Updated Method Signature

```kotlin
suspend fun replyAiForm(
    currentMessages: List<ChatMessage>,
    latestMessage: String,
    currentFormData: SagaForm,
    audioFile: File?,  // ← NEW: Optional audio file from cache
): RequestResult<SagaCreationGen>
```

### Implementation Flow (NewSagaUseCaseImpl)

```kotlin
override suspend fun replyAiForm(
    currentMessages: List<ChatMessage>,
    latestMessage: String,
    currentFormData: SagaForm,
    audioFile: File?,
): RequestResult<SagaCreationGen> =
    executeRequest {
        val delayDefaultTime = 700L

        // Step 1: Transcribe audio if provided
        val userInput = if (audioFile != null) {
            val transcription = audioTranscriptionService.transcribeAudio(audioFile)
            transcription ?: currentMessages.last().text  // Fallback to last message
        } else {
            currentMessages.last().text
        }

        // Step 2: Extract data from transcribed text (or regular text)
        val extractedDataPrompt = gemmaClient.generate<SagaForm>(
            NewSagaPrompts.extractDataFromUserInputPrompt(
                currentSagaForm = currentFormData,
                userInput = userInput,  // ← Now includes corrected transcription
                lastMessage = latestMessage,
            ),
            requireTranslation = true,
        )!!

        // ... rest of form processing continues normally
    }
```

## AudioTranscriptionService Details

### Transcription Process

```kotlin
suspend fun transcribeAudio(audioFile: File?): String? {
    // 1. Validate file exists and has content
    if (audioFile == null || !audioFile.exists() || audioFile.length() == 0L) {
        return null
    }

    // 2. Create transcription prompt
    val prompt = """
        Transcribe the following audio message accurately.
        Correct any pronunciation errors and maintain coherent text.
        Return ONLY the transcribed text, nothing else.
    """

    // 3. Call Gemma with audio file
    val transcription = gemmaClient.generate<String>(
        prompt = prompt,
        audioFile = audioFile,  // ← File sent as BlobPart
        requireTranslation = false,
        describeOutput = false
    )

    return transcription?.trim()
}
```

### Key Features

- **Null-safe**: Handles null or invalid files gracefully
- **Error Handling**: Returns null on failure, logs errors
- **Fallback**: If transcription fails, uses original text
- **On-Demand**: File only read when transcribing (not before)

## Dependency Injection

### AudioModule

```kotlin
@Singleton
@Provides
fun provideAudioTranscriptionService(
    gemmaClient: GemmaClient,
): AudioTranscriptionService = AudioTranscriptionService(gemmaClient)
```

### NewSagaUseCaseImpl Injection

```kotlin
class NewSagaUseCaseImpl @Inject constructor(
    private val sagaRepository: SagaRepository,
    private val gemmaClient: GemmaClient,
    private val audioTranscriptionService: AudioTranscriptionService,  // ← Injected
) : NewSagaUseCase
```

## Usage Example

### In UI Layer (ViewModel or Composable)

```kotlin
// Assuming user has recorded audio
val audioFile = audioService.stopRecording()  // File in cache

// When user submits form with voice message
launchIO {
    val result = newSagaUseCase.replyAiForm(
        currentMessages = messages,
        latestMessage = lastMessage,
        currentFormData = sagaForm,
        audioFile = audioFile  // ← Pass recorded audio
    )

    // Result contains transcribed text processed into form
    // User's voice input is now structured form data
}
```

## Error Handling

### Transcription Failures

If `audioTranscriptionService.transcribeAudio()` returns null:

1. Fallback to `currentMessages.last().text`
2. Form processing continues normally
3. User is not blocked - they still get a response

```kotlin
val userInput = if (audioFile != null) {
    val transcription = audioTranscriptionService.transcribeAudio(audioFile)
    transcription ?: currentMessages.last().text  // ← Fallback
} else {
    currentMessages.last().text
}
```

### Logging

Both AudioTranscriptionService and GemmaClient log:

- Audio file reading success/failure
- Transcription start/completion
- Byte array size processed
- Any errors encountered

## Performance

### Timeline

1. **Audio transcription**: ~2-4 seconds (Gemma processing)
2. **Form data extraction**: ~2-4 seconds (Gemma processing)
3. **Next field identification**: ~2-4 seconds (Gemma processing)
4. **Creative question generation**: ~2-4 seconds (Gemma processing)

**Total**: ~8-16 seconds for complete form interaction with audio

### Memory

- Audio file: read on-demand (only when transcribing)
- ByteArray: created only for BlobPart (freed after send)
- Peak memory: ~5-10MB total

## Testing Checklist

- [ ] Record audio successfully
- [ ] Pass audio file to replyAiForm
- [ ] Transcription completes successfully
- [ ] Corrected text flows through form extraction
- [ ] Form data is accurate
- [ ] Error handling works (invalid file, read failure)
- [ ] Fallback to text works when transcription fails
- [ ] No memory leaks with repeated calls

## Future Enhancements

1. **Streaming Transcription**: Send audio while recording
2. **Progress Feedback**: Show transcription progress to user
3. **Language Detection**: Auto-detect audio language
4. **Accent Adaptation**: Train on user's voice patterns
5. **Real-time Correction**: Suggest corrections as user speaks
6. **Audio Quality Analysis**: Check audio quality before sending

