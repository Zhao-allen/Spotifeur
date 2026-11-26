package com.hchen.superlyricapi

import android.os.Parcel
import android.os.Parcelable

class SuperLyricWord(
    var word: String = "",
    var startTime: Long = 0L,
    var endTime: Long = 0L
) : Parcelable {
    var delay: Long = (endTime - startTime).coerceAtLeast(0L)
    private constructor(p: Parcel) : this() {
        word = p.readString() ?: ""
        delay = p.readLong()
        startTime = p.readLong()
        endTime = p.readLong()
    }
    override fun writeToParcel(p: Parcel, flags: Int) {
        p.writeString(word); p.writeLong(delay); p.writeLong(startTime); p.writeLong(endTime)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<SuperLyricWord> {
        override fun createFromParcel(p: Parcel) = SuperLyricWord(p)
        override fun newArray(size: Int) = arrayOfNulls<SuperLyricWord>(size)
    }
}