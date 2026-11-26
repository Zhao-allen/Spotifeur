package fr.spotifeur.playback

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/**
 * Optional floating-lyric fallback. This is an application overlay, not a system lock-screen
 * lyrics API; actual visibility over secure lock screens is device/OEM dependent.
 */
class LiveLyricOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var lyricView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val line = if (intent?.hasExtra(EXTRA_LINE) == true) {
            intent.getStringExtra(EXTRA_LINE)?.trim().orEmpty()
        } else {
            // START_STICKY may restart the service with a null intent after process reclaim.
            preferences.getString(KEY_LAST_LINE, null)?.trim().orEmpty()
        }

        if (line.isBlank()) {
            preferences.edit().remove(KEY_LAST_LINE).apply()
            removeOverlay()
            stopSelf(startId)
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            preferences.edit().remove(KEY_LAST_LINE).apply()
            removeOverlay()
            stopSelf(startId)
            return START_NOT_STICKY
        }
        preferences.edit().putString(KEY_LAST_LINE, line).apply()

        val view = lyricView
        if (view != null) {
            view.text = line
            return START_STICKY
        }
        val text = TextView(this).apply {
            this.text = line
            textSize = 17f
            setTextColor(android.graphics.Color.WHITE)
            setShadowLayer(5f, 0f, 2f, android.graphics.Color.BLACK)
            setPadding(24, 14, 24, 14)
            setBackgroundColor(0x66000000)
            gravity = Gravity.CENTER
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = (resources.displayMetrics.density * 110).toInt() }

        return try {
            windowManager?.addView(text, params)
            lyricView = text
            START_STICKY
        } catch (error: Throwable) {
            Log.w(TAG, "Unable to attach live-lyrics overlay", error)
            stopSelf(startId)
            START_NOT_STICKY
        }
    }

    private fun removeOverlay() {
        lyricView?.let { view -> runCatching { windowManager?.removeView(view) } }
        lyricView = null
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_LINE = "fr.spotifeur.extra.LIVE_LYRIC_LINE"
        private const val PREFERENCES = "live_lyric_overlay"
        private const val KEY_LAST_LINE = "last_line"
        private const val TAG = "SpotifeurLyrics"
    }
}
