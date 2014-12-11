package com.boriguen.ulisten2.media.impl;

import com.boriguen.ulisten2.media.AbstractMedia;
import com.boriguen.ulisten2.notification.NotificationData;

/**
 * Created by boris on 12/9/14.
 */
public class AndroidMusicMedia extends AbstractMedia {

    @Override
    protected String fetchTitle(NotificationData notificationData) {
        return notificationData.titleText.toString();
    }

    @Override
    protected String fetchAlbum(NotificationData notificationData) {
        String[] splitValues = notificationData.messageText.toString().split("\n");
        return splitValues[0];
    }

    @Override
    protected String fetchArtist(NotificationData notificationData) {
        String[] splitValues = notificationData.messageText.toString().split("\n");
        return splitValues[1];
    }

}
