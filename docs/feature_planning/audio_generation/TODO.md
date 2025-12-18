# Audio Generation - TODO List

## ✅ Phase 1: Backend Implementation (COMPLETE)

- [x] Create `Voice` enum with Google Chirp 3 HD voices
- [x] Create `AudioGenClient` interface and implementation
- [x] Add premium billing integration (`runPremiumRequest`)
- [x] Enhance `AudioPrompts` with `buildString` pattern
- [x] Add `saveAudioFile()` and `readAudioFile()` to `FileHelper`
- [x] Update `Message` model with `audioPath: String?`
- [x] Update `Character` model with `voice: String?`
- [x] Update `Saga` model with `narratorVoice: String?`
- [x] Bump database version 7 → 8 with auto-migration
- [x] Integrate `generateAudio()` in `MessageUseCaseImpl`
- [x] Add `AudioGenClient` to Dagger/Hilt `AppModule`

---

## ⏳ Phase 2: Testing & Validation

- [ ] Build project and verify compilation
- [ ] Test database migration 7 → 8
- [ ] Test premium request wrapping
- [ ] Test voice assignment for characters
- [ ] Test voice assignment for narrator
- [ ] Test voice persistence across messages
- [ ] Test audio file creation and storage
- [ ] Validate MP3 file format

---

## ⏳ Phase 3: Firebase TTS Integration

- [ ] Add `audioGenModel` to Remote Config
- [ ] Test Firebase TTS API integration
- [ ] Verify audio byte extraction from response
- [ ] Test different voice IDs
- [ ] Handle API errors gracefully

---

## ⏳ Phase 4: UI Implementation

### ChatBubble Audio Component

- [x] Create audio player composable (`AudioMessagePlayer.kt`)
- [x] Add play/pause button
- [x] Add linear progress bar with progress tracking
- [x] Add duration display (current position / total)
- [ ] Add waveform visualization (WhatsApp-style) - optional enhancement

### Loading State

- [ ] Shimmer animation for audio generation
- [ ] Spark icon (✨) indicator
- [ ] "Generating audio..." text

### Error State

- [ ] Error icon/message display
- [ ] Retry button
- [ ] Fallback to text-only view

### Transcription Display

- [x] Expandable/collapsible text view
- [x] Toggle button with icon
- [x] Default: Collapsed state
- [x] Alpha 0.5f for transcription text

### ChatBubble Integration

- [x] Add `audioPlaybackState` parameter
- [x] Add `onPlayAudio` callback
- [x] Conditional rendering (audio player vs text)
- [x] File existence validation

### ChatViewModel Integration

- [x] Add `audioPlaybackState` StateFlow
- [x] Add `playOrPauseAudio()` method
- [x] Add progress tracking with coroutine job
- [x] Handle audio completion
- [x] Handle multiple audio instances (stop previous)

### ChatView Integration

- [x] Pass `audioPlaybackState` to ChatContent
- [x] Pass `onPlayAudio` callback
- [x] Wire up to ChatBubble

---

## ⏳ Phase 5: Polish & Optimization

- [ ] Audio file cleanup when message is deleted
- [ ] Storage management (auto-cleanup old files?)
- [ ] Analytics tracking for audio generation
- [ ] Performance optimization for large audio files
- [ ] Caching strategy for frequently played audio

---

## 📝 Notes

### Voice IDs (Update when Firebase docs change)

Current voice IDs are placeholders based on Google Chirp 3 HD naming:

- `male-en-US-neural2-A/C/D/E`
- `female-en-US-neural2-A/C/D/E`
- `narrator-en-US-neural2-A`

### Known Issues

- Audio byte extraction uses reflection (may break with SDK updates)
- No batch audio processing yet
- No offline audio caching

### Future Enhancements

- [ ] Voice preview in character settings
- [ ] Manual voice selection override
- [ ] Audio speed/pitch controls
- [ ] Background audio playback
- [ ] Audio export feature

