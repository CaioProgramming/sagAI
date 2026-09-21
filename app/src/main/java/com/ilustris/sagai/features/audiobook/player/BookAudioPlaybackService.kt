package com.ilustris.sagai.features.audiobook.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ilustris.sagai.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Media session over [BookAudioPlayer]: lock screen controls and background playback for the audiobook. */
@AndroidEntryPoint
class BookAudioPlaybackService : MediaSessionService() {
    @Inject
    lateinit var bookAudioPlayer: BookAudioPlayer

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val openApp =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        session =
            MediaSession
                .Builder(this, bookAudioPlayer.player)
                .setSessionActivity(openApp)
                .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        // The player is an app singleton the reader keeps using; only the session ends with the service.
        session?.release()
        session = null
        super.onDestroy()
    }
}
