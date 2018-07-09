package com.botob.ulisten2.media;

import android.os.Parcel;

import com.botob.ulisten2.media.impl.AndroidMusicMedia;
import com.botob.ulisten2.media.impl.DeezerMedia;
import com.botob.ulisten2.media.impl.FakeMedia;
import com.botob.ulisten2.media.impl.GooglePlayMusicMedia;
import com.botob.ulisten2.media.impl.PandoraMedia;
import com.botob.ulisten2.media.impl.SpotifyMedia;

import java.lang.reflect.InvocationTargetException;

/**
 * @author boriguen
 * @date 12/9/14
 */
public enum MediaApp {

    ANDROID_MUSIC("com.android.music", AndroidMusicMedia.class),
    DEEZER("deezer.android.app", DeezerMedia.class),
    FAKE("com.botob.ulisten2", FakeMedia.class),
    GOOGLE_PLAY_MUSIC("com.google.android.music", GooglePlayMusicMedia.class),
    PANDORA("com.pandora.android", PandoraMedia.class),
    SPOTIFY("com.spotify.music", SpotifyMedia.class);

    private final String mPackageName;
    private final Class mClass;

    MediaApp(final String packageName, final Class classs) {
        mPackageName = packageName;
        mClass = classs;
    }
    
    @Override
    public String toString() {
        return mPackageName;
    }

    public String getPackageName() {
        return mPackageName;
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
    public Media instantiate(final Parcel parcel) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        return (Media) mClass.getConstructor(Parcel.class).newInstance(parcel);
    }
}
