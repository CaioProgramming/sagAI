# Audio Recording + Gemma Transcription Integration

## Overview

This document describes the integration of audio recording with the Gemma AI client for audio
transcription and processing.

## Architecture

### Components

1. **AudioService** - Records audio and manages file caching
2. **GemmaClient** - Updated to accept audio files via BlobPart
3. **AudioTranscriptionViewModel** - Example ViewModel showing usage pattern

## Data Flow

```
User Records Audio
        ↓
AudioService.startRecording() [MediaRecorder active]
        ↓
AudioService.stopRecording() [File saved in cache]
        ↓
File in: app-cache/file_cache/audio_recordings/audio_xxx.m4a
        ↓
User previews (optional) → File.read() from cache
        ↓
User sends → GemmaClient.generate(audioFile = file)
        ↓
GemmaClient reads file as ByteArray (on-demand)
        ↓
BlobPart(mimeType="audio/m4a", blob=audioBytes)
        ↓
Sent to Gemma API with prompt
        ↓
Response returned (transcription, analysis, etc)
```

## GemmaClient Changes

### Updated Method Signature

```kotlin
suspend inline fun <reified T> generate(
    prompt: String,
    references: List<ImageReference?> = emptyList(),
    audioFile: File? = null,  // ← NEW: Audio file from cache
    temperatureRandomness: Float = .5f,
    requireTranslation: Boolean = true,
    describeOutput: Boolean = true,
    filterOutputFields: List<String> = emptyList(),
    useCore: Boolean = false,
): T?
```

### Audio Handling in generate()

```kotlin
// Add audio if provided
audioFile?.let { file ->
    if (file.exists() && file.length() > 0) {
        try {
            val audioBytes = file.readBytes()  // ← Read on-demand
            add(BlobPart(mimeType = "audio/m4a", blob = audioBytes))
            Log.d(javaClass.simpleName, "Audio file added to request: ${file.name}")
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Failed to read audio file: ${e.message}", e)
        }
    }
}
```

### Key Points

- **Optional Parameter**: `audioFile: File? = null` - defaults to null (backward compatible)
- **On-Demand Reading**: File is only read as ByteArray when actually sending to Gemma
- **Error Handling**: Gracefully handles read failures with logging
- **File Format**: Audio must be in `.m4a` (MPEG-4 AAC) format
- **Part Type**: Uses `BlobPart` with mimeType `"audio/m4a"`

## Usage Example

### Simple Transcription

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val audioService: AudioService,
    private val gemmaClient: GemmaClient,
) : ViewModel() {
    
    private var recordedAudio: File? = null
    
    fun recordAndTranscribe() {
        viewModelScope.launch {
            // 1. Record
            audioService.startRecording()
            delay(5000) // Record for 5 seconds
            recordedAudio = audioService.stopRecording()
            
            // 2. File is now in cache
            
            // 3. Send to Gemma for transcription
            val transcription = gemmaClient.generate<String>(
                prompt = "Transcribe this audio",
                audioFile = recordedAudio  // ← Pass File directly
            )
            
            // 4. Use result
            println(transcription)
        }
    }
}
```

### Chat Integration

```kotlin
// In chat ViewModel
fun sendAudioMessage() {
    viewModelScope.launch {
        val audioFile = audioService.stopRecording()
        
        // Option 1: Transcribe first, then send text message
        val transcription = gemmaClient.generate<String>(
            prompt = "Transcribe this voice message",
            audioFile = audioFile
        )
        sendMessage(transcription) // Send as text message
        
        // Option 2: Send as audio message (future)
        // sendAudioMessage(audioFile)
    }
}
```

## File Lifecycle

### Cache Management

**Storage Location:**

```
app-cache/file_cache/audio_recordings/audio_yyyyMMdd_HHmmss.m4a
```

**File Cleanup:**

1. **User Cancels**: Call `audioService.cancelRecording()` → file deleted
2. **Screen Exit**: Call `audioService.release()` in ViewModel.onCleared()
3. **App Cache Clear**: FileCacheService.clearCache() removes all cached files

## Performance Considerations

### Memory Usage

- **While recording**: Minimal (~1MB for reference)
- **While playing preview**: File read into memory on-demand
- **While sending to Gemma**: File read as ByteArray only at send time
- **Total peak**: ~5MB (typical audio file size)

### Latency

- **Recording**: Real-time
- **Preview playback**: ~10ms (cache hit)
- **Send to Gemma**: File read (~50ms) + network (~2-5s)

## Error Handling

### Audio Read Failures

```kotlin
try {
    val audioBytes = file.readBytes()
    add(BlobPart(mimeType = "audio/m4a", blob = audioBytes))
} catch (e: Exception) {
    Log.e("GemmaClient", "Failed to read audio file: ${e.message}")
    // Request continues without audio
}
```

If audio file read fails:

- Error is logged
- Request proceeds without audio
- User gets result (may be incomplete)

## Supported Audio Formats

Currently supported:

- **Format**: MPEG-4 Container (.m4a)
- **Codec**: AAC
- **Bitrate**: 128 kbps
- **Sample Rate**: 44.1 kHz

If other formats are needed, update:

1. AudioService recording format
2. GemmaClient BlobPart mimeType

## Testing Checklist

- [ ] Record audio successfully
- [ ] File saved in cache directory
- [ ] Play audio from cache
- [ ] Cancel recording deletes file
- [ ] Send to Gemma with audio
- [ ] Receive transcription
- [ ] Error handling (invalid file, read failure)
- [ ] Cleanup on screen exit
- [ ] No memory leaks with repeated recording

## Future Enhancements

1. **Audio Playback UI**: MediaPlayer/ExoPlayer integration
2. **Recording Visualizer**: Waveform display during recording
3. **Duration Limit**: Max 60 seconds, etc
4. **Quality Settings**: Choose bitrate/sample rate
5. **Streaming Support**: Send audio while recording
6. **Audio Editing**: Trim, crop before sending
7. **Message Persistence**: Save audio with chat message

