package com.botob.ulisten2.media

import android.os.Parcel
import com.botob.ulisten2.media.impl.*
import java.lang.reflect.InvocationTargetException

/**
 * @author boriguen
 * @date 12/9/14
 */
enum class MediaApp(private val packageName: String, private val mClass: Class<*>) {
    ANDROID_MUSIC("com.android.music", AndroidMusicMedia::class.java), DEEZER("deezer.android.app", DeezerMedia::class.java), FAKE("com.botob.ulisten2", FakeMedia::class.java), GOOGLE_PLAY_MUSIC("com.google.android.music", GooglePlayMusicMedia::class.java), PANDORA("com.pandora.android", PandoraMedia::class.java), SPOTIFY("com.spotify.music", SpotifyMedia::class.java);

    override fun toString(): String {
        return packageName
    }

    /**
     * Instantiates a Media object from the given parcel.
     *
     * @param parcel the parcel from which to create the object.
     * @return the resulting object.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    @Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    fun instantiate(parcel: Parcel?): Media {
        return mClass.getConstructor(Parcel::class.java).newInstance(parcel) as Media
    }
}