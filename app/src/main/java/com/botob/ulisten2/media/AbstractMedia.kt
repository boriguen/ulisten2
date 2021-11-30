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

    private var _notificationData: NotificationData? = null
    private var _title: String? = null
    private var _album: String? = null
    private var _artist: String? = null
    private var _broadcastTime: Long
    private var _packageName: String?

    constructor(notificationData: NotificationData) {
        _notificationData = notificationData
        _broadcastTime = _notificationData!!.postTime
        _packageName = _notificationData!!.packageName
    }

    constructor(parcel: Parcel) {
        _title = parcel.readString()
        _album = parcel.readString()
        _artist = parcel.readString()
        _broadcastTime = parcel.readLong()
        _packageName = parcel.readString()
    }

    constructor(
        title: String?, album: String?, artist: String?,
        timestamp: Long, packageName: String?
    ) {
        _title = title
        _album = album
        _artist = artist
        _broadcastTime = timestamp
        _packageName = packageName
    }

    override fun equals(other: Any?): Boolean {
        val result: Boolean = if (other == null || javaClass != other.javaClass) {
            false
        } else {
            val otherMedia = other as Media
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
        out.writeString(title)
        out.writeString(album)
        out.writeString(artist)
    }

    override val title: String?
        get() = _title ?: fetchTitle(_notificationData)

    override val album: String
        get() = _album ?: fetchAlbum(_notificationData)

    override val artist: String?
        get() = _artist ?: fetchArtist(_notificationData)

    override val broadcastTime: Long
        get() = if (_notificationData != null) _notificationData!!.postTime else System.currentTimeMillis()

    override val packageName: String?
        get() = _notificationData!!.packageName
}