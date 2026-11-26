package fr.spotifeur.playback

import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.hchen.superlyricapi.SuperLyricData
import com.hchen.superlyricapi.SuperLyricLine

/** Binder bridge compatible with the public SuperLyric AIDL publisher contract. */
internal class SuperLyricPublisher(private val context: android.content.Context) {
    private var binder: IBinder? = null
    private var manager: com.hchen.superlyricapi.ISuperLyricManager? = null

    fun publish(title: String?, artist: String?, album: String?, text: String?, start: Long, end: Long) {
        try {
            val serviceManager = Class.forName("android.os.ServiceManager")
            val service = serviceManager.getMethod("getService", String::class.java).invoke(null, "super_lyric") as? IBinder
                ?: return
            if (binder !== service) {
                binder = service
                manager = com.hchen.superlyricapi.ISuperLyricManager.Stub.asInterface(service)
                manager?.registerPublisher()
            }
            val data = SuperLyricData().setTitle(title).setArtist(artist).setAlbum(album)
                .setLyric(text?.takeIf { it.isNotBlank() }?.let { SuperLyricLine(it, start, end) })
            if (text.isNullOrBlank()) manager?.sendStop(data) else manager?.sendLyric(data)
        } catch (error: Throwable) {
            Log.d("SpotifeurLyrics", "SuperLyric service unavailable: ${error.message}")
            binder = null
            manager = null
        }
    }

    fun stop() = publish(null, null, null, null, 0L, 0L)
}
