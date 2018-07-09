package com.botob.ulisten2.media.impl;

import android.os.Parcel;

import com.botob.ulisten2.media.AbstractMedia;
import com.botob.ulisten2.media.MediaApp;
import com.botob.ulisten2.notification.NotificationData;

/**
 * Created by boris on 12/9/14.
 */
public class PandoraMedia extends AbstractMedia {

    public PandoraMedia(NotificationData notificationData) {
        super(notificationData);
    }

    public PandoraMedia(final Parcel parcel) {
        super(parcel);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(MediaApp.PANDORA.name());
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
        return notificationData.messageTextLines[0].toString();
    }
}
