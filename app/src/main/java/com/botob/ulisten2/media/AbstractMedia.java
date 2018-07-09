package com.botob.ulisten2.media;

import android.os.Parcel;
import android.os.Parcelable;

import com.botob.ulisten2.notification.NotificationData;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Created by boris on 12/9/14.
 */
public abstract class AbstractMedia implements Media {
    /**
     * The parcelable creator.
     */
    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {
        public Media createFromParcel(Parcel source) {
            Media media = null;
            try {
                media = MediaApp.valueOf(source.readString()).instantiate(source);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
            return media;
        }

        public AbstractMedia[] newArray(int size) {
            return new AbstractMedia[size];
        }
    };

    private static final String[] AD_KEYWORDS = new String[]{
            "advertisement"
    };

    private NotificationData mNotificationData;
    private String mTitle;
    private String mAlbum;
    private String mArtist;
    private long mBroadcastTime;
    private String mPackageName;

    public AbstractMedia(NotificationData notificationData) {
        mNotificationData = notificationData;
        mTitle = fetchTitle(notificationData);
        mAlbum = fetchAlbum(notificationData);
        mArtist = fetchArtist(notificationData);
        mBroadcastTime = mNotificationData.postTime;
        mPackageName = mNotificationData.packageName;
    }

    public AbstractMedia(Parcel parcel) {
        mTitle = parcel.readString();
        mAlbum = parcel.readString();
        mArtist = parcel.readString();
        mBroadcastTime = parcel.readLong();
        mPackageName = parcel.readString();
    }

    public AbstractMedia(final String title, final String album, final String artist,
                         final long timestamp, final String packageName) {
        mTitle = title;
        mAlbum = album;
        mArtist = artist;
        mBroadcastTime = timestamp;
        mPackageName = packageName;
    }

    @Override
    public boolean equals(Object object) {
        final boolean result;
        if (object == null || !getClass().equals(object.getClass())) {
            result = false;
        } else {
            Media otherMedia = (Media) object;
            result = getTitle().equals(otherMedia.getTitle())
                    && getAlbum().equals(otherMedia.getAlbum())
                    && getArtist().equals(otherMedia.getArtist());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getAlbum(), getArtist());
    }

    public boolean isRelevant() {
        return getTitle() != null && getArtist() != null && !isAd();
    }

    private boolean isAd() {
        boolean result = false;
        for (int i = 0; i < AD_KEYWORDS.length && !result; i++) {
            result = AD_KEYWORDS[i].equalsIgnoreCase(getTitle());
        }
        return result;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeString(mAlbum);
        out.writeString(mArtist);
    }

    public String getTitle() {
        return mTitle != null ? mTitle : "";
    }

    public String getAlbum() {
        return mAlbum != null ? mAlbum : "";
    }

    public String getArtist() {
        return mArtist != null ? mArtist : "";
    }

    @Override
    public long getBroadcastTime() {
        return mNotificationData != null ? mNotificationData.postTime : System.currentTimeMillis();
    }

    @Override
    public String getPackageName() {
        return mNotificationData.packageName;
    }

    protected abstract String fetchTitle(NotificationData notificationData);

    protected abstract String fetchAlbum(NotificationData notificationData);

    protected abstract String fetchArtist(NotificationData notificationData);
}
