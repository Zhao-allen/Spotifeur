package fr.spotifeur.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.spotifeur.MainActivity
import fr.spotifeur.R
import fr.spotifeur.data.model.Lyrics
import fr.spotifeur.data.model.Song

/**
 * A companion lyrics notification. Android's MediaSession notification is owned by Media3;
 * this notification exposes the currently timed lyric without replacing the media session.
 */
class LyricsNotificationController(context: Context) {
    private val app = context.applicationContext
    private val manager = NotificationManagerCompat.from(app)
    private var lastFingerprint: String? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "同步歌词", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示当前播放歌曲的实时歌词"
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            app.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun update(song: Song?, lyrics: Lyrics?, positionMs: Long, playing: Boolean, offsetMs: Int) {
        if (song == null || lyrics == null || lyrics.synced.isNullOrEmpty()) {
            hide()
            return
        }
        val lines = lyrics.synced
        val position = (positionMs + offsetMs).coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val index = lines.indexOfLast { it.time <= position }
        if (index < 0) {
            hide()
            return
        }
        val current = lines[index].line.trim().ifBlank { song.title }
        val next = lines.getOrNull(index + 1)?.line?.trim().orEmpty()
        val fingerprint = "${song.id}|$index|$current|$playing"
        if (fingerprint == lastFingerprint) return
        lastFingerprint = fingerprint

        val open = PendingIntent.getActivity(
            app, 7401, Intent(app, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = buildString {
            append(if (playing) "▶ " else "Ⅱ ")
            append(current)
            if (next.isNotBlank()) append("\n").append(next)
        }
        val notification = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_spotifeur)
            .setContentTitle(current)
            .setContentText("${song.title} · ${song.artist}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(open)
            .setOnlyAlertOnce(true)
            .setOngoing(playing)
            .setShowWhen(false)
            .build()
        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }

    fun hide() {
        lastFingerprint = null
        runCatching { manager.cancel(NOTIFICATION_ID) }
    }

    companion object {
        private const val CHANNEL_ID = "live_lyrics"
        private const val NOTIFICATION_ID = 7401
    }
}
