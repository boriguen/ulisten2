package com.botob.ulisten2.preferences

import android.os.Bundle
import android.preference.PreferenceFragment
import com.botob.ulisten2.R

/**
 * Created by guenebau on 10/4/15.
 */
class SettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }
}