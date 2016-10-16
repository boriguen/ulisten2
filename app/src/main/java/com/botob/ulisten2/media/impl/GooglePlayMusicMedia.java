package com.botob.ulisten2.media.impl;

import com.botob.ulisten2.media.AbstractMedia;
import com.botob.ulisten2.notification.NotificationData;

/**
 * GooglePlayMusicMedia is the class extending AbstractMedia responsible for extracting the media information
 * from the Google Play Music notifications.
 *
 * @author boriguen
 * @date   10/16/16
 */
public class GooglePlayMusicMedia extends AbstractMedia {

    public GooglePlayMusicMedia(NotificationData notificationData) {
        super(notificationData);
    }

    @Override
    protected CharSequence fetchTitle(NotificationData notificationData) {
        return notificationData.titleText;
    }

    @Override
    protected CharSequence fetchAlbum(NotificationData notificationData) {
        return notificationData.subText;
    }

    @Override
    protected CharSequence fetchArtist(NotificationData notificationData) {
        return notificationData.messageText;
    }

}
