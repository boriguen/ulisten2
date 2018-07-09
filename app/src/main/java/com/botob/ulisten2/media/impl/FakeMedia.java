package com.botob.ulisten2.media.impl;

import android.os.Parcel;

import com.botob.ulisten2.media.AbstractMedia;
import com.botob.ulisten2.media.MediaApp;
import com.botob.ulisten2.notification.NotificationData;

/**
 * @author boris on 7/8/18.
 */
public class FakeMedia extends AbstractMedia {

    public FakeMedia(NotificationData notificationData) {
        super(notificationData);
    }

    public FakeMedia(final Parcel parcel) {
        super(parcel);
    }

    public FakeMedia(final String title, final String album, final String artist,
                     final long timestamp, final String packageName) {
        super(title, album, artist, timestamp, packageName);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(MediaApp.FAKE.name());
        super.writeToParcel(out, flags);
    }

    @Override
    protected String fetchTitle(NotificationData notificationData) {
        throw new UnsupportedOperationException("It is just a fake.");
    }

    @Override
    protected String fetchAlbum(NotificationData notificationData) {
        throw new UnsupportedOperationException("It is just a fake.");
    }

    @Override
    protected String fetchArtist(NotificationData notificationData) {
        throw new UnsupportedOperationException("It is just a fake.");
    }
}
