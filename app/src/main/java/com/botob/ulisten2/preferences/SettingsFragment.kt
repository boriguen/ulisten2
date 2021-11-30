package com.botob.ulisten2.preferences

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.botob.ulisten2.R

/**
 * Created by guenebau on 10/4/15.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private const val DIALOG_FRAGMENT_TAG = "NumberPickerDialog"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return
        }
        if (preference is NumberPickerPreference) {
            val dialog = NumberPickerPreferenceDialog.newInstance(preference.key)
            dialog.setTargetFragment(this, 0)
            dialog.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        } else
            super.onDisplayPreferenceDialog(preference)
    }
}