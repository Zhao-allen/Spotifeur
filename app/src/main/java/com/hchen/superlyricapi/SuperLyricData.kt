package com.hchen.superlyricapi

import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

class SuperLyricData() : Parcelable {
    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var lyric: SuperLyricLine? = null
    var secondary: SuperLyricLine? = null
    var translation: SuperLyricLine? = null
    var mediaMetadata: MediaMetadata? = null
    var playbackState: PlaybackState? = null
    var base64Icon: String? = null
    var extra: Bundle? = null
    fun setTitle(value: String?) = apply { title = value }
    fun setArtist(value: String?) = apply { artist = value }
    fun setAlbum(value: String?) = apply { album = value }
    fun setLyric(value: SuperLyricLine?) = apply { lyric = value }
    private constructor(p: Parcel) : this() {
        val loader = SuperLyricData::class.java.classLoader
        title = p.readString(); artist = p.readString(); album = p.readString()
        lyric = p.readParcelable(loader); secondary = p.readParcelable(loader); translation = p.readParcelable(loader)
        mediaMetadata = p.readParcelable(loader); playbackState = p.readParcelable(loader)
        base64Icon = p.readString(); extra = p.readBundle(loader)
    }
    override fun writeToParcel(p: Parcel, flags: Int) {
        p.writeString(title); p.writeString(artist); p.writeString(album)
        p.writeParcelable(lyric, flags); p.writeParcelable(secondary, flags); p.writeParcelable(translation, flags)
        p.writeParcelable(mediaMetadata, flags); p.writeParcelable(playbackState, flags)
        p.writeString(base64Icon); p.writeBundle(extra)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<SuperLyricData> {
        override fun createFromParcel(p: Parcel) = SuperLyricData(p)
        override fun newArray(size: Int) = arrayOfNulls<SuperLyricData>(size)
    }
}