package com.das.musicplayer.mediacontrol

import android.support.v4.media.session.MediaSessionCompat
import com.das.musicplayer.PlayerHolder.exoPlayer


class MediaActionsListener: MediaSessionCompat.Callback() {



    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onPlay() {
        super.onPlay()
        exoPlayer?.play()
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        exoPlayer?.seekTo(pos)
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        exoPlayer?.seekToNext()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        exoPlayer?.seekToPrevious()
    }


}