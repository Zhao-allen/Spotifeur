package fr.spotifeur.playback

import android.os.IBinder
import android.util.Log
import com.hchen.superlyricapi.SuperLyricData
import com.hchen.superlyricapi.SuperLyricLine

/** Binder bridge compatible with the public SuperLyric AIDL publisher contract. */
internal class SuperLyricPublisher(private val context: android.content.Context) {
    private var binder: IBinder? = null
    private var manager: com.hchen.superlyricapi.ISuperLyricManager? = null
    private val deathRecipient = IBinder.DeathRecipient {
        synchronized(this) {
            binder = null
            manager = null
        }
        Log.d("SpotifeurLyrics", "SuperLyric service binder died")
    }

    @Synchronized
    fun publish(title: String?, artist: String?, album: String?, text: String?, start: Long, end: Long) {
        try {
            val serviceManager = Class.forName("android.os.ServiceManager")
            val service = serviceManager.getMethod("getService", String::class.java)
                .invoke(null, "super_lyric") as? IBinder ?: return
            if (!service.isBinderAlive) {
                clearConnection(service)
                return
            }
            if (binder !== service || manager == null) {
                clearConnection()
                val connectedManager = com.hchen.superlyricapi.ISuperLyricManager.Stub.asInterface(service)
                    ?: return
                service.linkToDeath(deathRecipient, 0)
                binder = service
                manager = connectedManager
            }

            val connectedManager = manager ?: return
            // The system service may restart independently of the player process.
            // Check registration before every publish and restore it when necessary.
            if (!connectedManager.isPublisherRegistered()) {
                connectedManager.registerPublisher()
            }

            val data = SuperLyricData().setTitle(title).setArtist(artist).setAlbum(album)
                .setLyric(text?.takeIf { it.isNotBlank() }?.let { SuperLyricLine(it, start, end) })
            if (text.isNullOrBlank()) connectedManager.sendStop(data) else connectedManager.sendLyric(data)
        } catch (error: Throwable) {
            Log.d("SpotifeurLyrics", "SuperLyric service unavailable: ${error.message}")
            clearConnection()
        }
    }

    @Synchronized
    private fun clearConnection(expected: IBinder? = null) {
        val current = binder
        if (expected != null && current !== expected) return
        if (current != null) {
            runCatching { current.unlinkToDeath(deathRecipient, 0) }
        }
        binder = null
        manager = null
    }

    fun stop() = publish(null, null, null, null, 0L, 0L)
}
