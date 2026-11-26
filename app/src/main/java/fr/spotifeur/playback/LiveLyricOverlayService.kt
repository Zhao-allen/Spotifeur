package fr.spotifeur.playback

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/** Minimal lock-screen-capable floating lyric surface, updated by the active playback controller. */
class LiveLyricOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var lyricView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val line = intent?.getStringExtra(EXTRA_LINE)?.trim().orEmpty()
        if (line.isBlank()) {
            removeOverlay()
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val view = lyricView
        if (view != null) {
            view.text = line
            return START_NOT_STICKY
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
        runCatching { windowManager?.addView(text, params); lyricView = text }
            .onFailure { stopSelf() }
        return START_NOT_STICKY
    }

    private fun removeOverlay() {
        lyricView?.let { view -> runCatching { windowManager?.removeView(view) } }
        lyricView = null
    }

    override fun onDestroy() { removeOverlay(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object { const val EXTRA_LINE = "fr.spotifeur.extra.LIVE_LYRIC_LINE" }
}
