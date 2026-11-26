package com.hchen.superlyricapi

import android.os.Parcel
import android.os.Parcelable

class SuperLyricLine(
    var text: String = "",
    var startTime: Long = 0L,
    var endTime: Long = 0L
) : Parcelable {
    var delay: Long = (endTime - startTime).coerceAtLeast(0L)
    private constructor(p: Parcel) : this(p.readString() ?: "", p.readLong(), p.readLong()) { delay = p.readLong() }
    override fun writeToParcel(p: Parcel, flags: Int) {
        p.writeString(text); p.writeLong(startTime); p.writeLong(endTime); p.writeLong(delay)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<SuperLyricLine> {
        override fun createFromParcel(p: Parcel) = SuperLyricLine(p).also { }
        override fun newArray(size: Int) = arrayOfNulls<SuperLyricLine>(size)
    }
}