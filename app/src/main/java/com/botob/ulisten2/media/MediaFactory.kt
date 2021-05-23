package com.botob.ulisten2.media

import com.botob.ulisten2.media.impl.*
import com.botob.ulisten2.notification.NotificationData

/**
 * @author boriguen
 * @date 10/16/16
 */
object MediaFactory {
    fun createMedia(notificationData: NotificationData?): Media? {
        var media: Media? = null
        if (notificationData!!.packageName == MediaApp.ANDROID_MUSIC.toString()) {
            media = AndroidMusicMedia(notificationData)
        } else if (notificationData.packageName == MediaApp.DEEZER.toString()) {
            media = DeezerMedia(notificationData)
        } else if (notificationData.packageName == MediaApp.GOOGLE_PLAY_MUSIC.toString()) {
            media = GooglePlayMusicMedia(notificationData)
        } else if (notificationData.packageName == MediaApp.PANDORA.toString()) {
            media = PandoraMedia(notificationData)
        } else if (notificationData.packageName == MediaApp.SPOTIFY.toString()) {
            media = SpotifyMedia(notificationData)
        } else if (notificationData.packageName == MediaApp.FAKE.toString()) {
            media = FakeMedia(notificationData)
        }
        return media
    }
}