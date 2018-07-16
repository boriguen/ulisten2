package com.botob.ulisten2.media;

import android.os.Parcelable;

/**
 * Created by boris on 12/9/14.
 */
public interface Media extends Parcelable {

    String getTitle();

    String getAlbum();

    String getArtist();

    long getBroadcastTime();

    String getPackageName();

    boolean isRelevant();

    boolean isAd();
}
