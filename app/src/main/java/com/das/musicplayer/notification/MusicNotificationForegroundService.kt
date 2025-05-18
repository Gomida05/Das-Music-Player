package com.das.musicplayer.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import com.das.musicplayer.PlayerHolder.exoPlayer
import com.das.musicplayer.PlayerHolder.mediaSession
import com.das.musicplayer.R
import com.das.musicplayer.dataAndNames.KeyWords.MUSIC_CHANNEL_ID
import com.das.musicplayer.dataAndNames.KeyWords.MUSIC_NOTIFICATION_DISMISSED
import com.das.musicplayer.mediacontrol.MusicPlayState.musicBufferingState
import com.das.musicplayer.mediacontrol.MusicPlayState.musicPauseState
import com.das.musicplayer.mediacontrol.MusicPlayState.musicPlayState

class MusicNotificationForegroundService: Service() {


    private val notificationID = 908
    private val stopHandler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null


    override fun onCreate() {
        super.onCreate()
        createChannel()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        exoPlayer?.addListener(ExoPlayerListener())
        startForeground(notificationID, startNotification())


        return START_NOT_STICKY


    }


    fun startNotification(): Notification {

        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession?.sessionToken!!)
        mediaSession?.setMetadata(getMediaSession(exoPlayer!!))

        val deleteIntent = Intent(this, NotificationDismissedReceiver::class.java).apply {
            action = MUSIC_NOTIFICATION_DISMISSED
        }
        val pendingDeleteIntent = PendingIntent.getBroadcast(
            this,
            0,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, MUSIC_CHANNEL_ID)
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.music_ico)
            .setDeleteIntent(pendingDeleteIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationID, notification)
        return notification


    }


    private fun createChannel() {

        val channel = NotificationChannel(
            MUSIC_CHANNEL_ID,
            "Music Player",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableVibration(false)
            setSound(null, null)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }


    private fun getMediaSession(player: Player): MediaMetadataCompat {

//            val mediaBitMap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.music_ico)

        return MediaMetadataCompat.Builder()
            .putString(
                MediaMetadataCompat.METADATA_KEY_TITLE,
                player.currentMediaItem?.mediaMetadata?.title.toString()
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                "unknown album"
            )
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.duration)
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, mediaBitMap)
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, mediaBitMap)
            .build()

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    inner class ExoPlayerListener : Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                mediaSession?.setPlaybackState(musicPlayState())
                startNotification()
                cancelStopTimer()
            } else {
                mediaSession?.setPlaybackState(musicPauseState())
                startNotification()
                startStopTimer()

            }
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            if (isLoading) {
                mediaSession?.setPlaybackState(musicBufferingState())
//                startNotification()
            } else {
                mediaSession?.setPlaybackState(musicPlayState())
            }
            startNotification()
        }


    }


    private fun startStopTimer() {
        stopRunnable?.let { stopHandler.removeCallbacks(it) } // Clear old timer if exists

        stopRunnable = Runnable {
            android.util.Log.d("MusicService", "15 minutes passed - stopping service")

            // Stop playback if needed
            exoPlayer?.pause()

            // Release media session
            mediaSession?.release()


            stopSelf()
        }

        // Post delayed runnable for 15 minutes (15 * 60 * 1000 milliseconds)
        stopHandler.postDelayed(stopRunnable!!, 15 * 60 * 1000L)
    }

    private fun cancelStopTimer() {
        stopRunnable?.let { stopHandler.removeCallbacks(it) }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!exoPlayer?.isPlaying!!) {
            exoPlayer?.release()
            mediaSession?.release()
            stopSelf()
        }
    }

}
