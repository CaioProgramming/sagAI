# Audio Recording Module - Technical Documentation

## Overview

This module provides a solid foundation for audio recording functionality in the app. It handles
microphone permissions, audio recording state management, and file caching via the existing
FileCacheService.

## Architecture

### 1. AudioService (`AudioService.kt`)

The core service that manages audio recording operations.

**Key Features:**

- **Enum State Management:** `RecordingState` (IDLE, RECORDING, PAUSED, STOPPED, ERROR)
- **StateFlow Export:** Exposes `recordingState` and `recordingDuration` for reactive UI updates
- **Lifecycle Methods:** `startRecording()`, `pauseRecording()`, `resumeRecording()`,
  `stopRecording()`, `cancelRecording()`
- **FileCacheService Integration:** Auto-creates `audio_recordings` cache folder via
  FileCacheService, generates timestamped filenames
- **Cache Cleanup:** `clearCache()` and `release()` methods for cleanup
- **Reusable:** Can be injected into any screen (Chat, Create Saga, etc)

**Usage:**

```kotlin
// Direct injection - works anywhere (Composable, Activity, Fragment, ViewModel)
@Inject lateinit var audioService: AudioService

// Observe recording state
audioService.recordingState.collect { state ->
  when (state) {
    RecordingState.RECORDING -> // Show recording UI
    RecordingState.STOPPED -> // File ready for use
    else -> {}
  }
}

// Start recording
audioService.startRecording()

// Stop and get file for use in message
val audioFile: File? = audioService.stopRecording()
```

### 2. AudioPermissionManager (`AudioPermissionManager.kt`)

Handles runtime permission checks for audio recording.

**Methods:**

- `getRequiredPermissions()`: Returns list of required permissions (only RECORD_AUDIO - cache only)
- `hasAudioPermissions()`: Check all required permissions
- `hasMicrophonePermission()`: Specifically check RECORD_AUDIO

**API Level Awareness:**

- Android 11+: App cache doesn't require external storage permissions
- All versions: Only RECORD_AUDIO permission needed

### 3. AudioModule (`di/AudioModule.kt`)

Hilt dependency injection module providing singletons.

**Provides:**

- `AudioService` instance (injected with FileCacheService)
- `AudioPermissionManager` instance

## File Structure

```
app/src/main/java/com/ilustris/sagai/
├── core/audio/
│   ├── AudioService.kt
│   ├── AudioPermissionManager.kt
│   ├── RecordingState.kt (enum inside AudioService.kt)
│   ├── AudioUtil.kt
│   └── di/
│       └── AudioModule.kt
```

## Permissions Added

### AndroidManifest.xml

```xml
<!-- Audio Recording Permission (Cache only, no external storage needed) -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

**Note:**

- `RECORD_AUDIO`: Required for microphone access
- No storage permissions needed - files are cached via FileCacheService in app-cache directory

## Audio File Format

- **Format:** MPEG-4 (.m4a)
- **Codec:** AAC
- **Bitrate:** 128 kbps
- **Sample Rate:** 44.1 kHz
- **Cache Location:** `app-cache/file_cache/audio_recordings/` (via FileCacheService)
- **Filename Pattern:** `audio_yyyyMMdd_HHmmss.m4a`

## Recording State Flow

```
IDLE
  ↓
startRecording() → RECORDING
                     ↓
              pauseRecording() → PAUSED
                                   ↓
                           resumeRecording() → RECORDING
                                                  ↓
                                    stopRecording() → STOPPED (returns File)
```

**From any state:** `cancelRecording()` → IDLE (no file returned)

## FileCacheService Integration

AudioService uses the existing FileCacheService for:

- **Cache Directory Management:** Automatically creates and manages `audio_recordings` subdirectory
- **Lifecycle Consistency:** Audio files follow same cache lifecycle as other cached files
- **Unified Cleanup:** Cleanup via FileCacheService.clearCache() removes all cached data including
  audio

## Usage in Different Screens

### In Chat Screen

```kotlin
class ChatViewModel @Inject constructor(
    private val audioService: AudioService,
    private val audioPermissionManager: AudioPermissionManager,
) : ViewModel() {
    
    fun startAudioMessage() {
        if (audioPermissionManager.hasMicrophonePermission()) {
            audioService.startRecording()
        }
    }
    
    fun sendAudioMessage() {
        val audioFile = audioService.stopRecording()
        // Send audio file as message
    }
}
```

### In Create Saga Screen

```kotlin
class CreateSagaViewModel @Inject constructor(
    private val audioService: AudioService,
) : ViewModel() {
    
    fun recordCharacterDescription() {
        audioService.startRecording()
    }
}
```

## Cleanup Strategy

**Cache Directory:** `app-cache/file_cache/audio_recordings/`

**Cleanup Triggers:**

1. **ViewModel.onCleared()**: Service automatically called when ViewModel destroyed
2. **Screen Exit**: Call `audioService.cancelRecording()` when leaving recording UI
3. **App Lifecycle**: FileCacheService.clearCache() removes all cached data
4. **Manual Cleanup**: Call `audioService.clearCache()` as needed

## Next Steps

This module is complete for recording. Future integrations will include:

1. **UI Component:** Recording button + visualizer for any screen
2. **Transcription Module:** Convert audio to text (Speech-to-Text)
3. **Audio Playback:** UI component to play recorded audio
4. **Audio Generation:** TTS for AI character responses
5. **Message Storage:** Integrate with Room database to persist audio metadata

## Testing Considerations

- Test permission flow
- Verify cache cleanup on app exit
- Test pause/resume on API 24+ (not supported on older)
- Verify file validity before using
- Test with various audio durations
- Test reusability across different screens

## Notes

- `MediaRecorder` requires microphone permission; runtime checks are essential
- Pause/Resume only supported on API 24+; fallback to stop/start on older versions
- Cache cleanup via FileCacheService ensures unified cleanup across app
- Service is fully reusable - inject anywhere needed
- No ViewModel wrapper needed - service can be used directly via dependency injection

