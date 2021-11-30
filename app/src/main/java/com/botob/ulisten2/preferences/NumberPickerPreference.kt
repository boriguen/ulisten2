package com.botob.ulisten2.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference

/**
 * A [android.preference.Preference] displaying a number picker as a dialog.
 * Inspired from https://newbedev.com/android-preferenceactivity-dialog-with-number-picker.
 */
class NumberPickerPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {

    companion object {
        // allowed range
        const val INITIAL_VALUE = 50
        const val MIN_VALUE = 12
        const val MAX_VALUE = 100
    }

    override fun getSummary(): CharSequence {
        return getPersistedInt(INITIAL_VALUE).toString()
    }

    fun getPersistedInt() = super.getPersistedInt(INITIAL_VALUE)

    fun doPersistInt(value: Int) {
        super.persistInt(value)
        notifyChanged()
    }
}