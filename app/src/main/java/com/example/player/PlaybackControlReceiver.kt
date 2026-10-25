package com.example.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaybackControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val player = MediaNotificationManager.playerManagerRef ?: return

        when (action) {
            ACTION_PLAY_PAUSE -> {
                player.togglePlayPause()
            }
            ACTION_REWIND -> {
                player.rewind10Seconds()
            }
            ACTION_FORWARD -> {
                player.forward30Seconds()
            }
            ACTION_STOP -> {
                player.pauseAndDismiss()
            }
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.player.ACTION_PLAY_PAUSE"
        const val ACTION_REWIND = "com.example.player.ACTION_REWIND"
        const val ACTION_FORWARD = "com.example.player.ACTION_FORWARD"
        const val ACTION_STOP = "com.example.player.ACTION_STOP"
    }
}
