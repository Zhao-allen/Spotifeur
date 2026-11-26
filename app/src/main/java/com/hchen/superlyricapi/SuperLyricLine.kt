package com.hchen.superlyricapi

import android.os.Parcel
import android.os.Parcelable

class SuperLyricLine(
    var text: String = "",
    var words: Array<SuperLyricWord>? = null,
    var startTime: Long = 0L,
    var endTime: Long = 0L
) : Parcelable {
    var delay: Long = (endTime - startTime).coerceAtLeast(0L)
    constructor(text: String, startTime: Long, endTime: Long) : this(text, null, startTime, endTime)
    private constructor(p: Parcel) : this(
        p.readString() ?: "",
        p.createTypedArray(SuperLyricWord.CREATOR),
        p.readLong(),
        p.readLong()
    ) { delay = p.readLong() }
    override fun writeToParcel(p: Parcel, flags: Int) {
        p.writeString(text); p.writeTypedArray(words, flags)
        p.writeLong(startTime); p.writeLong(endTime); p.writeLong(delay)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<SuperLyricLine> {
        override fun createFromParcel(p: Parcel) = SuperLyricLine(p)
        override fun newArray(size: Int) = arrayOfNulls<SuperLyricLine>(size)
    }
}