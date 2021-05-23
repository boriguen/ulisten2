package com.botob.ulisten2.media.impl

import android.os.Parcel
import com.botob.ulisten2.media.AbstractMedia
import com.botob.ulisten2.media.MediaApp
import com.botob.ulisten2.notification.NotificationData

/**
 * Created by boris on 12/9/14.
 */
class AndroidMusicMedia : AbstractMedia {
    constructor(notificationData: NotificationData?) : super(notificationData) {}
    constructor(parcel: Parcel) : super(parcel) {}

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(MediaApp.ANDROID_MUSIC.name)
        super.writeToParcel(out, flags)
    }

    override fun fetchTitle(notificationData: NotificationData?): String? {
        return notificationData!!.titleText.toString()
    }

    override fun fetchAlbum(notificationData: NotificationData?): String? {
        return notificationData!!.messageTextLines!![0].toString()
    }

    override fun fetchArtist(notificationData: NotificationData?): String? {
        return notificationData!!.messageTextLines!![1].toString()
    }
}