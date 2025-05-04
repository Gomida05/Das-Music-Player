package com.das.musicplayer

import android.app.Activity.AUDIO_SERVICE
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import androidx.media3.exoplayer.ExoPlayer

object PlayerHolder {

    var exoPlayer: ExoPlayer? = null
    var mediaSession: MediaSessionCompat? = null

    val bundles = Bundle()

    fun requestAudioFocusFromMain(context: Context, exoPlayer: ExoPlayer?) {

        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

        val audioFocusRequest = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // Pause playback when losing focus
                    exoPlayer?.playWhenReady = false
                }

                AudioManager.AUDIOFOCUS_GAIN -> {
                    // Resume playback when gaining focus
                    exoPlayer?.playWhenReady = true
                    exoPlayer?.volume = 1.0f
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Pause temporarily (e.g., during a phone call)
                    exoPlayer?.playWhenReady = false
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Can continue playing but with lower volume
                    exoPlayer?.volume = 0.1f  // Reduce volume
                }
            }
        }


        @Suppress("DEPRECATION")
        val result = audioManager.requestAudioFocus(
            audioFocusRequest,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            exoPlayer?.pause()
        }
    }


}
