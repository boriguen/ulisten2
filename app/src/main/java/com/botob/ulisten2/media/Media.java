package com.botob.ulisten2.media;

/**
 * Created by boris on 12/9/14.
 */
public interface Media {

    String getTitle();

    String getAlbum();

    String getArtist();

    long getBroadcastTime();

    String getPackageName();

    boolean isRelevant();

}
