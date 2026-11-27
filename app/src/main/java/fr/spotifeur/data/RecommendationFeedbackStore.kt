package fr.spotifeur.data

import android.content.Context
import androidx.core.content.edit
import java.util.Locale

/** Explicit negative recommendation feedback, persisted separately from the candidate cache. */
class RecommendationFeedbackStore(context: Context) {
    enum class Action { NOT_INTERESTED, LESS_LIKE_ARTIST, BLOCK_ARTIST }
    private val prefs = context.applicationContext.getSharedPreferences("recommendation_feedback", Context.MODE_PRIVATE)

    @Synchronized fun record(item: Music, action: Action) {
        val artist = artistKey(item)
        prefs.edit {
            when (action) {
                Action.NOT_INTERESTED -> putStringSet(KEY_TRACKS, read(KEY_TRACKS) + item.id)
                Action.LESS_LIKE_ARTIST -> if (artist.isNotBlank()) putStringSet(KEY_LESS_ARTISTS, read(KEY_LESS_ARTISTS) + artist)
                Action.BLOCK_ARTIST -> if (artist.isNotBlank()) putStringSet(KEY_BLOCKED_ARTISTS, read(KEY_BLOCKED_ARTISTS) + artist)
            }
        }
    }

    fun isTrackBlocked(item: Music) = item.id in read(KEY_TRACKS)
    fun isArtistBlocked(item: Music) = artistKey(item).let { it.isNotBlank() && it in read(KEY_BLOCKED_ARTISTS) }
    fun isArtistLessLiked(item: Music) = artistKey(item).let { it.isNotBlank() && it in read(KEY_LESS_ARTISTS) }
    private fun read(key: String): Set<String> = prefs.getStringSet(key, emptySet()).orEmpty().toSet()

    companion object {
        private const val KEY_TRACKS = "not_interested_tracks"
        private const val KEY_LESS_ARTISTS = "less_like_artists"
        private const val KEY_BLOCKED_ARTISTS = "blocked_artists"
        fun artistKey(item: Music): String = item.subtitle.substringBefore(" · ").substringBefore(" • ").trim()
            .lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
    }
}

/** Ranks Spotify candidates using saved tracks, Spotify recent plays, and Spotifeur Spotify history. */
object RecommendationRanker {
    fun rank(candidates: List<Music>, saved: List<Music>, recent: List<Music>, spotifeurHistory: List<Music>, feedback: RecommendationFeedbackStore): List<Music> {
        val profiles = listOf(profile(saved), profile(recent), profile(spotifeurHistory))
        val sourceCount = mutableMapOf<String, Int>()
        profiles.forEach { profile -> profile.keys.forEach { key -> sourceCount[key] = (sourceCount[key] ?: 0) + 1 } }
        val ranked = candidates.asSequence()
            .filter { it.kind == MusicKind.Track && it.source == MusicSource.Spotify }
            .filterNot { feedback.isTrackBlocked(it) || feedback.isArtistBlocked(it) }
            .map { item ->
                val keys = features(item)
                val affinity = profiles.map { profile -> keys.mapNotNull(profile::get).maxOrNull() ?: 0.0 }
                val consensus = keys.maxOfOrNull { (sourceCount[it] ?: 0) / 3.0 } ?: 0.0
                val penalty = if (feedback.isArtistLessLiked(item)) 0.55 else 0.0
                val score = affinity[0] * 0.30 + affinity[1] * 0.25 + affinity[2] * 0.25 + consensus * 0.20 - penalty
                item to score
            }.sortedByDescending { it.second }.map { it.first }.toList()
        val remaining = ranked.toMutableList()
        val output = ArrayList<Music>(remaining.size)
        var previous = ""
        var run = 0
        while (remaining.isNotEmpty()) {
            val index = remaining.indexOfFirst { artistKey(it) != previous || run < 2 }.takeIf { it >= 0 } ?: 0
            val next = remaining.removeAt(index)
            val artist = artistKey(next)
            if (artist == previous) run++ else { previous = artist; run = 1 }
            output += next
        }
        return output
    }

    private fun profile(items: List<Music>): Map<String, Double> {
        val counts = mutableMapOf<String, Double>()
        items.filter { it.kind == MusicKind.Track && it.source == MusicSource.Spotify }.forEachIndexed { index, item ->
            val weight = 1.0 / (1.0 + index / 12.0)
            features(item).forEach { key -> counts[key] = (counts[key] ?: 0.0) + weight }
        }
        val max = counts.values.maxOrNull() ?: return emptyMap()
        return counts.mapValues { it.value / max }
    }

    private fun features(item: Music): Set<String> = buildSet {
        artistKey(item).takeIf(String::isNotBlank)?.let { add("artist:" + it) }
        item.genre.split(',', '/', ';').map(String::trim).filter(String::isNotBlank).forEach { add("genre:" + it.lowercase(Locale.ROOT)) }
        item.album.trim().takeIf(String::isNotBlank)?.let { add("album:" + it.lowercase(Locale.ROOT)) }
    }
    private fun artistKey(item: Music) = RecommendationFeedbackStore.artistKey(item)
}