package com.boriguen.ulisten2.media;

/**
 * Created by boris on 12/9/14.
 */
public enum MediaApp {

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
