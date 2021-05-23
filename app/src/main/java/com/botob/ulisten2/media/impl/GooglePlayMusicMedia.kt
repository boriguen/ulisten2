package com.botob.ulisten2.media.impl

import android.os.Parcel
import com.botob.ulisten2.media.AbstractMedia
import com.botob.ulisten2.media.MediaApp
import com.botob.ulisten2.notification.NotificationData

/**
 * GooglePlayMusicMedia is the class extending AbstractMedia responsible for extracting the media information
 * from the Google Play Music notifications.
 *
 * @author boriguen
 * @date 10/16/16
 */
class GooglePlayMusicMedia : AbstractMedia {
    constructor(notificationData: NotificationData?) : super(notificationData) {}
    constructor(parcel: Parcel) : super(parcel) {}

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(MediaApp.GOOGLE_PLAY_MUSIC.name)
        super.writeToParcel(out, flags)
    }

    override fun fetchTitle(notificationData: NotificationData?): String? {
        return notificationData!!.titleText.toString()
    }

    override fun fetchAlbum(notificationData: NotificationData?): String? {
        return notificationData!!.subText.toString()
    }

    override fun fetchArtist(notificationData: NotificationData?): String? {
        return notificationData!!.messageText.toString()
    }
}