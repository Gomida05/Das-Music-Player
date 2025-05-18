package com.das.musicplayer.mediacontrol

import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.PlaybackSpeedState
import com.das.musicplayer.PlayerHolder.exoPlayer

object MusicPlayState {


    @OptIn(UnstableApi::class)
    fun musicPlayState(): PlaybackStateCompat {

        val playState = PlaybackStateCompat.Builder()
            .setState(
                PlaybackStateCompat.STATE_PLAYING,
                exoPlayer?.currentPosition!!,
                PlaybackSpeedState(exoPlayer!!).playbackSpeed
            )
            .setActions(
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
            )
            .build()

        return playState
    }


    @OptIn(UnstableApi::class)
    fun musicPauseState(): PlaybackStateCompat {



        val playState = PlaybackStateCompat.Builder()
            .setState(
                PlaybackStateCompat.STATE_PAUSED,
                exoPlayer?.currentPosition!!,
                PlaybackSpeedState(exoPlayer!!).playbackSpeed
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
            )
            .build()

        return playState
    }

    @OptIn(UnstableApi::class)
    fun musicBufferingState(): PlaybackStateCompat {


        val playState = PlaybackStateCompat.Builder()
            .setState(
                PlaybackStateCompat.STATE_BUFFERING,
                exoPlayer?.currentPosition!!,
                PlaybackSpeedState(exoPlayer!!).playbackSpeed
            )
            .setActions(
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PREPARE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
            )
            .build()

        return playState
    }


}