package com.botob.ulisten2.media.impl;

import android.os.Parcel;

import com.botob.ulisten2.media.AbstractMedia;
import com.botob.ulisten2.media.MediaApp;
import com.botob.ulisten2.notification.NotificationData;

/**
 * SpotifyMedia is the class extending AbstractMedia responsible for extracting the media information
 * from the Spotify notifications.
 *
 * @author boris
 * @date 10/16/16
 */
public class SpotifyMedia extends AbstractMedia {

    public SpotifyMedia(NotificationData notificationData) {
        super(notificationData);
    }

    public SpotifyMedia(final Parcel parcel) {
        super(parcel);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(MediaApp.SPOTIFY.name());
        super.writeToParcel(out, flags);
    }

    @Override
    protected String fetchTitle(NotificationData notificationData) {
        return notificationData.titleText.toString();
    }

    @Override
    protected String fetchAlbum(NotificationData notificationData) {
        return null;
    }

    @Override
    protected String fetchArtist(NotificationData notificationData) {
        return notificationData.messageTextLines[1].toString();
    }
}
