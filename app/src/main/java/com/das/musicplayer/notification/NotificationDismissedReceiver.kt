package com.das.musicplayer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.das.musicplayer.dataAndNames.KeyWords.MUSIC_NOTIFICATION_DISMISSED

class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == MUSIC_NOTIFICATION_DISMISSED) {
            Log.d("NotificationDismissedReceiver", "Notification was dismissed!")
            context.stopService(Intent(context, MusicNotificationForegroundService::class.java))
        }
    }
}