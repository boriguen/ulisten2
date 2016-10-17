package com.botob.ulisten2.media.impl;

import com.botob.ulisten2.media.AbstractMedia;
import com.botob.ulisten2.notification.NotificationData;

/**
 * DeezerMedia is the class extending AbstractMedia responsible for extracting the media information
 * from the Deezer notifications.
 *
 * @author boris
 * @date 10/16/16
 */
public class DeezerMedia extends AbstractMedia {

    public DeezerMedia(NotificationData notificationData) {
        super(notificationData);
    }

    @Override
    protected CharSequence fetchTitle(NotificationData notificationData) {
        return notificationData.titleText;
    }

    @Override
    protected CharSequence fetchAlbum(NotificationData notificationData) {
        return null;
    }

    @Override
    protected CharSequence fetchArtist(NotificationData notificationData) {
        return notificationData.messageTextLines[0];
    }
}
