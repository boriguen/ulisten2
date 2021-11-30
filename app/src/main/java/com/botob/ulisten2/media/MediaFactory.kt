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
        when {
            notificationData!!.packageName == MediaApp.ANDROID_MUSIC.toString() -> {
                media = AndroidMusicMedia(notificationData)
            }
            notificationData.packageName == MediaApp.DEEZER.toString() -> {
                media = DeezerMedia(notificationData)
            }
            notificationData.packageName == MediaApp.GOOGLE_PLAY_MUSIC.toString() -> {
                media = GooglePlayMusicMedia(notificationData)
            }
            notificationData.packageName == MediaApp.PANDORA.toString() -> {
                media = PandoraMedia(notificationData)
            }
            notificationData.packageName == MediaApp.SPOTIFY.toString() -> {
                media = SpotifyMedia(notificationData)
            }
        }
        return media
    }
}