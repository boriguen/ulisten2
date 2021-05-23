package com.botob.ulisten2.media

import android.os.Parcel
import android.os.Parcelable
import com.botob.ulisten2.notification.NotificationData
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Created by boris on 12/9/14.
 */
abstract class AbstractMedia : Media {
    private var mNotificationData: NotificationData? = null
    private var mTitle: String?
    private var mAlbum: String?
    private var mArtist: String?
    private var mBroadcastTime: Long
    private var mPackageName: String?

    constructor(notificationData: NotificationData?) {
        mNotificationData = notificationData
        mTitle = fetchTitle(notificationData)
        mAlbum = fetchAlbum(notificationData)
        mArtist = fetchArtist(notificationData)
        mBroadcastTime = mNotificationData!!.postTime
        mPackageName = mNotificationData!!.packageName
    }

    constructor(parcel: Parcel) {
        mTitle = parcel.readString()
        mAlbum = parcel.readString()
        mArtist = parcel.readString()
        mBroadcastTime = parcel.readLong()
        mPackageName = parcel.readString()
    }

    constructor(title: String?, album: String?, artist: String?,
                timestamp: Long, packageName: String?) {
        mTitle = title
        mAlbum = album
        mArtist = artist
        mBroadcastTime = timestamp
        mPackageName = packageName
    }

    override fun equals(`object`: Any?): Boolean {
        val result: Boolean
        result = if (`object` == null || javaClass != `object`.javaClass) {
            false
        } else {
            val otherMedia = `object` as Media
            title == otherMedia.title && album == otherMedia.album && artist == otherMedia.artist
        }
        return result
    }

    override fun hashCode(): Int {
        return Objects.hash(title, album, artist)
    }

    override val isRelevant: Boolean
        get() = title != null && artist != null && !isAd
    override val isAd: Boolean
        get() {
            var result = false
            var i = 0
            while (i < AD_KEYWORDS.size && !result) {
                result = AD_KEYWORDS[i].equals(title, ignoreCase = true)
                i++
            }
            return result
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(mTitle)
        out.writeString(mAlbum)
        out.writeString(mArtist)
    }

    override val title: String?
        get() = if (mTitle != null) mTitle else ""
    override val album: String
        get() = if (mAlbum != null) mAlbum!! else ""
    override val artist: String?
        get() = if (mArtist != null) mArtist else ""
    override val broadcastTime: Long
        get() = if (mNotificationData != null) mNotificationData!!.postTime else System.currentTimeMillis()
    override val packageName: String?
        get() = mNotificationData!!.packageName

    protected abstract fun fetchTitle(notificationData: NotificationData?): String?
    protected abstract fun fetchAlbum(notificationData: NotificationData?): String?
    protected abstract fun fetchArtist(notificationData: NotificationData?): String?

    companion object {
        /**
         * The parcelable creator.
         */
        val CREATOR: Parcelable.Creator<Media> = object : Parcelable.Creator<Media> {
            override fun createFromParcel(source: Parcel): Media? {
                var media: Media? = null
                try {
                    media = MediaApp.valueOf(source.readString()!!).instantiate(source)
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                }
                return media
            }

            override fun newArray(size: Int): Array<AbstractMedia?> {
                return arrayOfNulls(size)
            }
        }
        private val AD_KEYWORDS = arrayOf(
                "advertisement"
        )
    }
}