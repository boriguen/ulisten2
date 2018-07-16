package com.botob.ulisten2.media;

import com.botob.ulisten2.media.impl.AndroidMusicMedia;
import com.botob.ulisten2.media.impl.DeezerMedia;
import com.botob.ulisten2.media.impl.FakeMedia;
import com.botob.ulisten2.media.impl.GooglePlayMusicMedia;
import com.botob.ulisten2.media.impl.PandoraMedia;
import com.botob.ulisten2.media.impl.SpotifyMedia;
import com.botob.ulisten2.notification.NotificationData;

/**
 * @author boriguen
 * @date 10/16/16
 */
public class MediaFactory {

    private MediaFactory() {
    }

    public static Media createMedia(NotificationData notificationData) {
        Media media = null;

        if (notificationData.packageName.equals(MediaApp.ANDROID_MUSIC.toString())) {
            media = new AndroidMusicMedia(notificationData);
        } else if (notificationData.packageName.equals(MediaApp.DEEZER.toString())) {
            media = new DeezerMedia(notificationData);
        } else if (notificationData.packageName.equals(MediaApp.GOOGLE_PLAY_MUSIC.toString())) {
            media = new GooglePlayMusicMedia(notificationData);
        } else if (notificationData.packageName.equals(MediaApp.PANDORA.toString())) {
            media = new PandoraMedia(notificationData);
        } else if (notificationData.packageName.equals(MediaApp.SPOTIFY.toString())) {
            media = new SpotifyMedia(notificationData);
        } else if (notificationData.packageName.equals(MediaApp.FAKE.toString())) {
            media = new FakeMedia(notificationData);
        }

        return media;
    }
}
