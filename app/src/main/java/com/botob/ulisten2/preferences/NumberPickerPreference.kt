package com.botob.ulisten2.preferences

import android.content.Context
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker

/**
 * A [android.preference.Preference] displaying a number picker as a dialog.
 * Inspired from https://stackoverflow.com/a/27046784.
 */
class NumberPickerPreference : DialogPreference {
    /**
     * The number picker component.
     */
    private var picker: NumberPicker? = null

    /**
     * The selected value.
     */
    var value = 0
        set(value) {
            field = value
            persistInt(this.value)
        }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onCreateDialogView(): View {
        val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER
        picker = NumberPicker(context)
        picker!!.layoutParams = layoutParams
        picker!!.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        val dialogView = FrameLayout(context)
        dialogView.addView(picker)
        return dialogView
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        picker!!.minValue = MIN_VALUE
        picker!!.maxValue = MAX_VALUE
        picker!!.wrapSelectorWheel = WRAP_SELECTOR_WHEEL
        picker!!.value = value
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            picker!!.clearFocus()
            val newValue = picker!!.value
            if (callChangeListener(newValue)) {
                value = newValue
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, MIN_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any) {
        value = if (restorePersistedValue) getPersistedInt(MIN_VALUE) else (defaultValue as Int)
    }

    companion object {
        /**
         * The minimum value to propose.
         */
        const val MIN_VALUE = 0

        /**
         * The maximum value to propose.
         */
        const val MAX_VALUE = 120

        /**
         * The constant indicating whether to enable circular behavior.
         */
        const val WRAP_SELECTOR_WHEEL = true
    }
}