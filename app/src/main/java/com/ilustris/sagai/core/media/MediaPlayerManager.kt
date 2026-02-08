package com.ilustris.sagai.core.media

import android.media.MediaPlayer
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Manages MediaPlayer lifecycle and operations.
 */
interface MediaPlayerManager {
    /**
     * Emits the current playback state (true if playing, false otherwise).
     */
    val isPlaying: StateFlow<Boolean>
    val mediaPlayer: MediaPlayer?

    /**
     * Prepares the MediaPlayer with the given audio file.
     *
     * @param context Android context.
     * @param file The audio file to play.
     * @param looping Whether the media should loop.
     * @param onPrepared Callback invoked when the media player is prepared. Playback can be started here.
     * @param onError Callback invoked if an error occurs during preparation or playback.
     * @param onCompletion Callback invoked when media playback completes (if not looping).
     */
    fun prepareDataSource(
        path: String,
        looping: Boolean = false,
        onPrepared: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null,
        onCompletion: (() -> Unit)? = null,
    )

    /**
     * Starts or resumes playback. Should be called after [prepareDataSource] has successfully completed
     * (usually in its onPrepared callback or if the player was previously paused).
     */
    fun play()

    /**
     * Pauses playback.
     */
    fun pause()

    /**
     * Stops playback and resets the MediaPlayer to its uninitialized state (Idle).
     * Call [prepareDataSource] again to reuse this MediaPlayer instance with a new or the same data source.
     */
    fun stop()

    /**
     * Releases all resources associated with the MediaPlayer.
     * The MediaPlayer object is no longer usable after this call. A new instance must be created.
     */
    fun release()

    fun resume()
}
