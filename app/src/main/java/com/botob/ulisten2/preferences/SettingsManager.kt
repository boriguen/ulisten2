package com.botob.ulisten2.preferences

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.preference.PreferenceManager

/**
 * Created by guenebau on 10/4/15.
 */
class SettingsManager(context: Context?) : OnSharedPreferenceChangeListener {
    val sharedPreferences: SharedPreferences
    private var delay = -1
    private var interval = -1
    private var speed = -1f
    val playMediaDelayInMilliseconds: Int
        get() {
            if (delay < 0) {
                delay = sharedPreferences.getInt(PLAY_MEDIA_DELAY, 3)
                delay *= 1000
            }
            return delay
        }
    val playMediaIntervalInMilliseconds: Int
        get() {
            if (interval < 0) {
                interval = sharedPreferences.getInt(PLAY_MEDIA_INTERVAL, 30)
                interval *= 1000
            }
            return interval
        }
    val playMediaSpeed: Float
        get() {
            if (speed < 0) {
                speed = sharedPreferences.getString(PLAY_MEDIA_SPEED, "1")!!.toFloat()
            }
            return speed
        }
    var playServiceEnabled: Boolean
        get() = sharedPreferences.getBoolean(PLAY_SERVICE_ENABLED, false)
        set(enabled) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PLAY_SERVICE_ENABLED, enabled)
            editor.apply()
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == PLAY_MEDIA_DELAY) {
            delay = -1
        } else if (key == PLAY_MEDIA_INTERVAL) {
            interval = -1
        } else if (key == PLAY_MEDIA_SPEED) {
            speed = -1f
        }
    }

    companion object {
        const val PLAY_MEDIA_DELAY = "pref_play_media_delay"
        const val PLAY_MEDIA_INTERVAL = "pref_play_media_interval"
        const val PLAY_MEDIA_SPEED = "pref_play_media_speed"
        const val PLAY_SERVICE_ENABLED = "pref_play_service_enabled"
    }

    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
}