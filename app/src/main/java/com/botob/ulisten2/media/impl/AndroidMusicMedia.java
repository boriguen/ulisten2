package com.botob.ulisten2.media.impl;

import com.botob.ulisten2.media.AbstractMedia;
import com.botob.ulisten2.notification.NotificationData;

/**
 * Created by boris on 12/9/14.
 */
public class AndroidMusicMedia extends AbstractMedia {

    public AndroidMusicMedia(NotificationData notificationData) {
        super(notificationData);
    }

    @Override
    protected CharSequence fetchTitle(NotificationData notificationData) {
        return notificationData.titleText;
    }

    @Override
    protected CharSequence fetchAlbum(NotificationData notificationData) {
        return notificationData.messageTextLines[0];
    }

    @Override
    protected CharSequence fetchArtist(NotificationData notificationData) {
        return notificationData.messageTextLines[1];
    }
}
