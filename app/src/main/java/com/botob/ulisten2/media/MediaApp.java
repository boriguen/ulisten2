package com.botob.ulisten2.media;

/**
 * Created by boris on 12/9/14.
 */
public enum MediaApp {

    ANDROID_MUSIC("com.android.music"),
    PANDORA("com.pandora.android");

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
