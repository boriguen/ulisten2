package com.botob.ulisten2.media.impl

import android.os.Parcel
import com.botob.ulisten2.media.AbstractMedia
import com.botob.ulisten2.media.MediaApp
import com.botob.ulisten2.notification.NotificationData

/**
 * DeezerMedia is the class extending AbstractMedia responsible for extracting the media information
 * from the Deezer notifications.
 *
 * @author boris
 * @date 10/16/16
 */
class DeezerMedia : AbstractMedia {
    constructor(notificationData: NotificationData?) : super(notificationData) {}
    constructor(parcel: Parcel) : super(parcel) {}

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(MediaApp.DEEZER.name)
        super.writeToParcel(out, flags)
    }

    override fun fetchTitle(notificationData: NotificationData?): String? {
        return notificationData!!.titleText.toString()
    }

    override fun fetchAlbum(notificationData: NotificationData?): String? {
        return null
    }

    override fun fetchArtist(notificationData: NotificationData?): String? {
        return notificationData!!.messageTextLines!![0].toString()
    }
}