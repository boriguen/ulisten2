package com.botob.ulisten2.media.impl

import android.os.Parcel
import com.botob.ulisten2.media.AbstractMedia
import com.botob.ulisten2.media.MediaApp
import com.botob.ulisten2.notification.NotificationData

/**
 * @author boris on 7/8/18.
 */
class FakeMedia : AbstractMedia {
    constructor(notificationData: NotificationData?) : super(notificationData) {}
    constructor(parcel: Parcel) : super(parcel) {}
    constructor(title: String?, album: String?, artist: String?,
                timestamp: Long, packageName: String?) : super(title, album, artist, timestamp, packageName) {
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(MediaApp.FAKE.name)
        super.writeToParcel(out, flags)
    }

    override fun fetchTitle(notificationData: NotificationData?): String? {
        throw UnsupportedOperationException("It is just a fake.")
    }

    override fun fetchAlbum(notificationData: NotificationData?): String? {
        throw UnsupportedOperationException("It is just a fake.")
    }

    override fun fetchArtist(notificationData: NotificationData?): String? {
        throw UnsupportedOperationException("It is just a fake.")
    }
}