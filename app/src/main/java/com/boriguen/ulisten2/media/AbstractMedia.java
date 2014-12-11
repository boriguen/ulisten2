package com.boriguen.ulisten2.media;

import com.boriguen.ulisten2.notification.NotificationData;

/**
 * Created by boris on 12/9/14.
 */
public abstract class AbstractMedia implements IMedia {

    protected NotificationData notificationData = null;
    protected String title = null;
    protected String album = null;
    protected String artist = null;

    protected AbstractMedia() {

    }

    public AbstractMedia(NotificationData notificationData) {
        this.notificationData = notificationData;
        this.title = fetchTitle(notificationData);
        this.album = fetchAlbum(notificationData);
        this.artist = fetchArtist(notificationData);
    }

    protected abstract String fetchTitle(NotificationData notificationData);

    protected abstract String fetchAlbum(NotificationData notificationData);

    protected abstract String fetchArtist(NotificationData notificationData);

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public long getBroadcastTime() {
        return notificationData.postTime;
    }

    @Override
    public String getPackageName() {
        return notificationData.packageName;
    }

}
