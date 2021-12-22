package com.botob.ulisten2.preferences

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.preference.PreferenceManager

/**
 * Created by guenebau on 10/4/15.
 */
class SettingsManager(context: Context?) : OnSharedPreferenceChangeListener {
    companion object {
        const val PLAY_MEDIA_DELAY = "pref_play_media_delay"
        const val PLAY_MEDIA_INTERVAL = "pref_play_media_interval"
        const val PLAY_MEDIA_SPEED = "pref_play_media_speed"
        const val PLAY_SERVICE_ENABLED = "pref_play_service_enabled"

        const val PLAY_MEDIA_DELAY_DEFAULT_IN_S = 3
        const val PLAY_MEDIA_INTERVAL_DEFAULT_IN_S = 30
        const val PLAY_MEDIA_SPEED_DEFAULT = "1"
        const val PLAY_SERVICE_ENABLED_DEFAULT = false
    }

    val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private var delay = -1

    private var interval = -1

    private var speed = -1f

    val playMediaDelayInMilliseconds: Int
        get() {
            if (delay < 0) {
                delay = sharedPreferences.getInt(PLAY_MEDIA_DELAY, PLAY_MEDIA_DELAY_DEFAULT_IN_S)
                delay *= 1000
            }
            return delay
        }

    val playMediaIntervalInMilliseconds: Int
        get() {
            if (interval < 0) {
                interval = sharedPreferences.getInt(PLAY_MEDIA_INTERVAL, PLAY_MEDIA_INTERVAL_DEFAULT_IN_S)
                interval *= 1000
            }
            return interval
        }

    val playMediaSpeed: Float
        get() {
            if (speed < 0) {
                speed = sharedPreferences.getString(PLAY_MEDIA_SPEED, PLAY_MEDIA_SPEED_DEFAULT)!!.toFloat()
            }
            return speed
        }

    var playServiceEnabled: Boolean
        get() = sharedPreferences.getBoolean(PLAY_SERVICE_ENABLED, PLAY_SERVICE_ENABLED_DEFAULT)
        set(enabled) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PLAY_SERVICE_ENABLED, enabled)
            editor.apply()
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Reset the cached value so that the newly saved value can fetched from preferences.
        when (key) {
            PLAY_MEDIA_DELAY -> {
                delay = -1
            }
            PLAY_MEDIA_INTERVAL -> {
                interval = -1
            }
            PLAY_MEDIA_SPEED -> {
                speed = -1f
            }
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
}