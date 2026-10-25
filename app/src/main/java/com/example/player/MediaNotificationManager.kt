package com.example.player

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.Episode

object MediaNotificationManager {

    private const val CHANNEL_ID = "iradyo_playback_channel"
    private const val CHANNEL_NAME = "iRadyo Podcast Oynatıcı"
    const val NOTIFICATION_ID = 1001

    @Volatile
    var playerManagerRef: AudioPlayerManager? = null

    private var isChannelCreated = false

    private fun ensureChannel(context: Context) {
        if (isChannelCreated) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Oynatılan podcast kontrolleri ve bildirim çubuğu oynatıcı paneli"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        isChannelCreated = true
    }

    fun showNotification(context: Context, episode: Episode, isPlaying: Boolean) {
        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        ensureChannel(context)

        // Activity Open Intent
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Control Actions Broadcasts
        val rewindIntent = Intent(context, PlaybackControlReceiver::class.java).apply {
            action = PlaybackControlReceiver.ACTION_REWIND
        }
        val rewindPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            rewindIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(context, PlaybackControlReceiver::class.java).apply {
            action = PlaybackControlReceiver.ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val forwardIntent = Intent(context, PlaybackControlReceiver::class.java).apply {
            action = PlaybackControlReceiver.ACTION_FORWARD
        }
        val forwardPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            forwardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, PlaybackControlReceiver::class.java).apply {
            action = PlaybackControlReceiver.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseTitle = if (isPlaying) "Duraklat" else "Oynat"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(episode.title)
            .setContentText(episode.podcastTitle)
            .setSubText("iRadyo Çalıyor")
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_rew, "-10 sn", rewindPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_ff, "+30 sn", forwardPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Kapat", stopPendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            manager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: Exception) {}
    }

    fun cancelNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            manager.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }

    fun showNewEpisodeNotification(context: Context, podcastTitle: String, episodeTitle: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        val channelId = "iradyo_new_episodes_channel"
        val channelName = "Yeni Podcast Yayınları"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Abone olduğunuz podcastlerde yeni bölüm yayınlandığında anlık bildirim"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Yeni Bölüm: $podcastTitle")
            .setContentText(episodeTitle)
            .setSubText("iRadyo Yeni Yayın")
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            manager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        } catch (_: Exception) {}
    }
}
