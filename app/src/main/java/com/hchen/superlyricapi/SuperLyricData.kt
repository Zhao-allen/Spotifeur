package com.hchen.superlyricapi

import android.os.Parcel
import android.os.Parcelable

class SuperLyricData() : Parcelable {
    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var lyric: SuperLyricLine? = null
    fun setTitle(value: String?) = apply { title = value }
    fun setArtist(value: String?) = apply { artist = value }
    fun setAlbum(value: String?) = apply { album = value }
    fun setLyric(value: SuperLyricLine?) = apply { lyric = value }
    private constructor(p: Parcel) : this() {
        title = p.readString(); artist = p.readString(); album = p.readString()
        lyric = p.readParcelable(SuperLyricLine::class.java.classLoader)
    }
    override fun writeToParcel(p: Parcel, flags: Int) {
        p.writeString(title); p.writeString(artist); p.writeString(album); p.writeParcelable(lyric, flags)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<SuperLyricData> {
        override fun createFromParcel(p: Parcel) = SuperLyricData(p)
        override fun newArray(size: Int) = arrayOfNulls<SuperLyricData>(size)
    }
}