package com.botob.ulisten2.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.botob.ulisten2.R;

/**
 * Created by guenebau on 10/4/15.
 */
public class SettingsFragment extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
