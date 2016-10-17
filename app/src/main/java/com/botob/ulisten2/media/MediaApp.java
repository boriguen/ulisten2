package com.botob.ulisten2.media;

/**
 * @author boriguen
 * @date 12/9/14
 */
public enum MediaApp {

    ANDROID_MUSIC("com.android.music"),
    DEEZER("deezer.android.app"),
    GOOGLE_PLAY_MUSIC("com.google.android.music"),
    PANDORA("com.pandora.android"),
    SPOTIFY("com.spotify.music");

    String packageName = null;

    MediaApp(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    @Override
    public String toString() {
        return this.packageName;
    }
}
