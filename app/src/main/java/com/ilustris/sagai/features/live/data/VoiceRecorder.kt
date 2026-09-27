package com.ilustris.sagai.features.live.data

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.ilustris.sagai.core.utils.AudioUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * A finished hold-to-talk recording.
 *
 * @property speechMs How long the input was above the speech threshold — the audio-level gate
 * (there's no transcript before the reply) uses it to drop silence and accidental taps.
 */
data class VoiceRecording(
    val file: File,
    val durationMs: Long,
    val speechMs: Long,
) {
    /** Long enough and with enough actual voice to be worth a reply. */
    val passesGate get() = durationMs >= MIN_DURATION_MS && speechMs >= MIN_SPEECH_MS

    companion object {
        const val MIN_DURATION_MS = 600L
        const val MIN_SPEECH_MS = 250L
    }
}

/**
 * Hold-to-talk recorder: 16 kHz mono 16-bit PCM (plenty for speech, ~32 KB/s) written as a WAV
 * the reply model can take inline. Emits a smoothed input [level] (0..1) for the blob while
 * recording. Caps at [MAX_DURATION_MS]; [maxReached] tells the UI to release.
 */
class VoiceRecorder
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var job: Job? = null
        private var record: AudioRecord? = null
        private var pcm = ByteArrayOutputStream()
        private var speechMs = 0L

        private val _level = MutableStateFlow(0f)
        val level: StateFlow<Float> = _level.asStateFlow()

        private val _maxReached = MutableStateFlow(false)
        val maxReached: StateFlow<Boolean> = _maxReached.asStateFlow()

        val isRecording get() = job?.isActive == true

        /** Caller must hold RECORD_AUDIO. Returns false if the mic couldn't be opened. */
        @SuppressLint("MissingPermission")
        fun start(): Boolean {
            if (isRecording) return true
            val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
            if (minBuffer <= 0) return false
            val audioRecord =
                runCatching {
                    AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, CHANNEL, ENCODING, minBuffer * 2)
                }.getOrNull()
            if (audioRecord == null || audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                Timber.w("VoiceRecorder: AudioRecord failed to initialize")
                return false
            }
            record = audioRecord
            pcm = ByteArrayOutputStream()
            speechMs = 0L
            _maxReached.value = false
            audioRecord.startRecording()

            job =
                scope.launch {
                    val buffer = ShortArray(minBuffer / 2)
                    val bytes = ByteArray(buffer.size * 2)
                    var smoothed = 0f
                    while (isActive) {
                        val read = audioRecord.read(buffer, 0, buffer.size)
                        if (read <= 0) continue
                        var sumSquares = 0.0
                        for (i in 0 until read) {
                            val sample = buffer[i].toInt()
                            sumSquares += sample.toDouble() * sample
                            bytes[i * 2] = (sample and 0xFF).toByte()
                            bytes[i * 2 + 1] = (sample shr 8 and 0xFF).toByte()
                        }
                        pcm.write(bytes, 0, read * 2)

                        val rms = sqrt(sumSquares / read).toFloat() / FULL_SCALE
                        if (rms >= SPEECH_THRESHOLD) speechMs += read * 1000L / SAMPLE_RATE
                        // Speech RMS rarely passes ~0.3 of full scale; stretch it so the blob moves.
                        val target = (rms / LOUD_RMS).coerceIn(0f, 1f)
                        smoothed += (target - smoothed) * if (target > smoothed) 0.5f else 0.15f
                        _level.value = smoothed

                        if (pcm.size() >= MAX_BYTES) {
                            _maxReached.value = true
                            break
                        }
                    }
                }
            return true
        }

        /** Stops and writes the WAV. Null if nothing was recorded. */
        suspend fun stop(): VoiceRecording? {
            val running = job ?: return null
            running.cancelAndJoin()
            job = null
            releaseRecord()
            _level.value = 0f
            val data = pcm.toByteArray()
            if (data.isEmpty()) return null
            return withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "live_recording_${System.currentTimeMillis()}.wav")
                file.writeBytes(AudioUtils.wrapPcmInWav(data, SAMPLE_RATE))
                VoiceRecording(file, data.size * 1000L / (SAMPLE_RATE * 2), speechMs)
            }
        }

        /** Stops and throws the audio away (drag-to-cancel, leaving the screen, losing audio focus). */
        fun cancel() {
            job?.cancel()
            job = null
            releaseRecord()
            pcm = ByteArrayOutputStream()
            _level.value = 0f
        }

        private fun releaseRecord() {
            record?.let {
                runCatching { it.stop() }
                it.release()
            }
            record = null
        }

        companion object {
            const val SAMPLE_RATE = 16_000
            private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
            private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
            private const val FULL_SCALE = 32768f
            private const val SPEECH_THRESHOLD = 0.02f
            private const val LOUD_RMS = 0.25f
            const val MAX_DURATION_MS = 60_000L
            private const val MAX_BYTES = (MAX_DURATION_MS / 1000 * SAMPLE_RATE * 2).toInt()
        }
    }
