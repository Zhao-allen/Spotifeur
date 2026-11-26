package fr.spotifeur.playback

import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import android.os.Bundle

/**
 * Exposes changing lyric metadata to MediaSession without mutating ExoPlayer's playlist.
 * The timeline wrapper changes only the active item's public metadata; the audio timeline,
 * media sources, queue and playback position remain owned by ExoPlayer.
 */
@UnstableApi
internal class LyricSessionPlayer(
    private val exoPlayer: ExoPlayer
) : ForwardingSimpleBasePlayer(exoPlayer) {

    private var lyricMediaId: String? = null
    private var lyricInfo: String? = null
    private var currentLine: String? = null

    fun updateLyrics(mediaId: String, fullLyricInfo: String?, line: String?) {
        if (exoPlayer.currentMediaItem?.mediaId != mediaId) return
        lyricMediaId = mediaId
        lyricInfo = fullLyricInfo
        currentLine = line?.takeIf(String::isNotBlank)
        invalidateState()
    }

    fun clearLyrics(mediaId: String) {
        if (lyricMediaId != mediaId) return
        lyricMediaId = null
        lyricInfo = null
        currentLine = null
        invalidateState()
    }

    override fun getState(): State {
        val base = super.getState()
        if (base.timeline.isEmpty) return base
        val activeIndex = exoPlayer.currentMediaItemIndex
        val activeId = exoPlayer.currentMediaItem?.mediaId
        val transformedTimeline = LyricTimeline(base.timeline, activeIndex) { id, original ->
            transformMetadata(id, original)
        }
        val transformedMetadata = transformMetadata(activeId, base.currentMetadata)
        return base.buildUpon()
            .setPlaylist(transformedTimeline, base.currentTracks, transformedMetadata)
            .build()
    }

    private fun transformMetadata(mediaId: String?, original: MediaMetadata): MediaMetadata {
        if (mediaId == null || mediaId != lyricMediaId) return original
        val extras = Bundle(original.extras ?: Bundle()).apply {
            lyricInfo?.let { putString("lyricInfo", it) } ?: remove("lyricInfo")
        }
        return original.buildUpon()
            .setExtras(extras)
            .setDiscSubtitle("lyricInfo:${lyricInfo?.hashCode() ?: 0}:${currentLine?.hashCode() ?: 0}")
            .setSubtitle(currentLine)
            .setDescription(currentLine)
            .build()
    }
}

@UnstableApi
private class LyricTimeline(
    private val source: Timeline,
    private val activeIndex: Int,
    private val transform: (String?, MediaMetadata) -> MediaMetadata
) : Timeline() {
    override fun getWindowCount(): Int = source.windowCount
    override fun getNextWindowIndex(windowIndex: Int, repeatMode: Int, shuffleModeEnabled: Boolean): Int =
        source.getNextWindowIndex(windowIndex, repeatMode, shuffleModeEnabled)
    override fun getPreviousWindowIndex(windowIndex: Int, repeatMode: Int, shuffleModeEnabled: Boolean): Int =
        source.getPreviousWindowIndex(windowIndex, repeatMode, shuffleModeEnabled)
    override fun getLastWindowIndex(shuffleModeEnabled: Boolean): Int = source.getLastWindowIndex(shuffleModeEnabled)
    override fun getFirstWindowIndex(shuffleModeEnabled: Boolean): Int = source.getFirstWindowIndex(shuffleModeEnabled)
    override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
        val result = source.getWindow(windowIndex, window, defaultPositionProjectionUs)
        if (windowIndex == activeIndex) {
            val item = result.mediaItem
            result.mediaItem = item.buildUpon()
                .setMediaMetadata(transform(item.mediaId, item.mediaMetadata))
                .build()
        }
        return result
    }
    override fun getPeriodCount(): Int = source.periodCount
    override fun getPeriod(periodIndex: Int, period: Period, setIds: Boolean): Period =
        source.getPeriod(periodIndex, period, setIds)
    override fun getIndexOfPeriod(uid: Any): Int = source.getIndexOfPeriod(uid)
    override fun getUidOfPeriod(periodIndex: Int): Any = source.getUidOfPeriod(periodIndex)
}
