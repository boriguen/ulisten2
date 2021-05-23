package com.botob.ulisten2.media

import android.os.Parcelable

/**
 * Created by boris on 12/9/14.
 */
interface Media : Parcelable {
    val title: String?
    val album: String
    val artist: String?
    val broadcastTime: Long
    val packageName: String?
    val isRelevant: Boolean
    val isAd: Boolean
}