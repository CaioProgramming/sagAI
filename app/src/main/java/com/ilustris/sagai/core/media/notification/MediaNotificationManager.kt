package com.ilustris.sagai.core.media.notification

import android.app.Notification
import android.support.v4.media.session.MediaSessionCompat // <-- Import needed
import com.ilustris.sagai.core.media.model.PlaybackMetadata

interface MediaNotificationManager {
    fun showPlaybackNotification(
        playbackMetadata: PlaybackMetadata,
        isPlaying: Boolean,
        sessionToken: MediaSessionCompat.Token?,
    ): Notification?

    fun cancelPlaybackNotification()
}
