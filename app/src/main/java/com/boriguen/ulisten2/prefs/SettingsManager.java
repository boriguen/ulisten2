package com.boriguen.ulisten2.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by guenebau on 10/4/15.
 */
public class SettingsManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PLAY_MEDIA_DELAY = "pref_play_media_delay";
    public static final String PLAY_MEDIA_INTERVAL = "pref_play_media_interval";
    public static final String PLAY_MEDIA_SPEED = "pref_play_media_speed";

    SharedPreferences sharedPreferences = null;
    int delay = -1;
    int interval = -1;
    float speed = -1;

    public SettingsManager(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public int getPlayMediaDelayInMilliseconds() {
        if (delay < 0) {
            delay = Integer.parseInt(sharedPreferences.getString(PLAY_MEDIA_DELAY, "3"));
            delay *= 1000;
        }
        return delay;
    }

    public int getPlayMediaIntervalInMilliseconds() {
        if (interval < 0) {
            interval = Integer.parseInt(sharedPreferences.getString(PLAY_MEDIA_INTERVAL, "30"));
            interval *= 1000;
        }
        return interval;
    }

    public float getPlayMediaSpeed() {
        if (speed < 0) {
            speed = Float.parseFloat(sharedPreferences.getString(PLAY_MEDIA_SPEED, "1"));
        }
        return speed;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PLAY_MEDIA_DELAY)) {
            delay = -1;
        } else if (key.equals(PLAY_MEDIA_INTERVAL)) {
            interval = -1;
        } else if (key.equals(PLAY_MEDIA_SPEED)) {
            speed = -1;
        }
    }
}
