package com.botob.ulisten2.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference

/**
 * A [android.preference.Preference] displaying a number picker as a dialog.
 * Inspired from https://newbedev.com/android-preferenceactivity-dialog-with-number-picker.
 */
class NumberPickerPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {

    companion object {
        const val INITIAL_VALUE = 0
        const val MIN_VALUE = 0
        const val MAX_VALUE = 120
    }

    private var defaultValue = INITIAL_VALUE

    override fun getSummary(): CharSequence {
        return getPersistedInt(defaultValue).toString()
    }

    override fun onSetInitialValue(restore: Boolean, defaultValue: Any?) {
        defaultValue?.let {
            this.defaultValue = it as Int
            doPersistInt(this.defaultValue)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInteger(index, this.defaultValue)
    }

    fun getPersistedInt() = super.getPersistedInt(INITIAL_VALUE)

    fun doPersistInt(value: Int) {
        super.persistInt(value)
        notifyChanged()
    }
}