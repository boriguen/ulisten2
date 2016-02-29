package com.boriguen.ulisten2.media;

import com.boriguen.ulisten2.media.impl.AndroidMusicMedia;
import com.boriguen.ulisten2.media.impl.PandoraMedia;
import com.boriguen.ulisten2.notification.NotificationData;

/**
 * Created by boris on 12/9/14.
 */
public class MediaFactory {

    private MediaFactory() {

    }

    public static IMedia createMedia(NotificationData notificationData) {
        IMedia media = null;

        if (notificationData.packageName.equals(MediaApp.ANDROID_MUSIC.toString())) {
            media = new AndroidMusicMedia(notificationData);
        } else if (notificationData.packageName.equals(MediaApp.PANDORA.toString())) {
            media = new PandoraMedia(notificationData);
        }

        return media;
    }

}
