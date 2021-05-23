package com.botob.ulisten2.notification

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.botob.ulisten2.R
import java.util.*

/**
 * Extractor is the class performing different manipulations on status bar notifications to
 * extract relevant information.
 *
 * @author boriguen
 * @date 11/17/14
 */
class Extractor {
    /**
     * Gets a bundle with additional data from notification.
     *
     * @param statusBarNotification the notification to extract information from.
     */
    private fun getExtras(statusBarNotification: StatusBarNotification): Bundle? {
        // Access extras using reflections.
        return try {
            val field = statusBarNotification.notification.javaClass.getDeclaredField("extras")
            field.isAccessible = true
            field[statusBarNotification.notification] as Bundle
        } catch (e: Exception) {
            Log.w(TAG, "Failed to access extras on Jelly Bean.")
            null
        }
    }

    /**
     * Loads the notification texts into the notification data instance.
     *
     * @param context               the context to access views from.
     * @param statusBarNotification the notification to extract text from.
     * @param data                  the notification data instance to fill in.
     * @return the filled in notification data instance.
     */
    private fun loadTexts(context: Context, statusBarNotification: StatusBarNotification, data: NotificationData): NotificationData {
        val extras = getExtras(statusBarNotification)
        if (extras != null) {
            loadFromExtras(data, extras)
            Log.d(TAG, "Ended loading from extras.")
        }
        if (TextUtils.isEmpty(data.titleText)
                || TextUtils.isEmpty(data.titleBigText)
                || TextUtils.isEmpty(data.messageText)
                || data.messageTextLines == null) {
            Log.d(TAG, "Starting loading from view.")
            loadFromView(data, context, statusBarNotification)
            Log.d(TAG, "Ended loading from view.")
        }
        if (data.messageTextLines == null) {
            data.messageTextLines = arrayOf()
        }
        return data
    }

    fun load(context: Context, statusBarNotification: StatusBarNotification, data: NotificationData): NotificationData {
        data.packageName = statusBarNotification.packageName
        data.postTime = statusBarNotification.postTime
        loadTexts(context, statusBarNotification, data)
        return data
    }

    /**
     * Loads all possible texts from given extras to the given notification data object.
     *
     * @param extras the extras to load from.
     */
    private fun loadFromExtras(data: NotificationData, extras: Bundle) {
        data.titleBigText = extras.getCharSequence(Notification.EXTRA_TITLE_BIG)
        data.titleText = extras.getCharSequence(Notification.EXTRA_TITLE)
        data.infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)
        data.subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
        data.summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)
        data.messageText = extras.getCharSequence(Notification.EXTRA_TEXT)
        var lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (lines != null) {
            // Ignore empty lines.
            val list: MutableList<CharSequence?> = ArrayList()
            for (message in lines) {
                val finalMessage = removeSpaces(message)
                if (finalMessage.isNullOrBlank().not()) {
                    list.add(finalMessage)
                }
            }

            // Create new array.
            if (list.size > 0) {
                lines = list.toTypedArray()
                data.messageTextLines = lines
            }
        }
    }

    /**
     * Loads all possible texts from given notification to notification data object.
     *
     * @param data                  the notification data to fill in.
     * @param context               the context to get app info from.
     * @param statusBarNotification the notification to extract information from.
     */
    private fun loadFromView(data: NotificationData,
                             context: Context,
                             statusBarNotification: StatusBarNotification) {
        val view: ViewGroup
        val notification = statusBarNotification.notification
        try {
            val remoteViews = if (notification.bigContentView == null) notification.contentView else notification.bigContentView

            // Try to load view from remote views.
            val contextNotify = context.createPackageContext(statusBarNotification.packageName,
                    Context.CONTEXT_RESTRICTED)
            val inflater = contextNotify.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(remoteViews.layoutId, null) as ViewGroup
            remoteViews.reapply(contextNotify, view)
            Log.d(TAG, "View loaded from remote views.")
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            return
        }

        // Remove non relevant text views.
        val textViews = RecursiveFinder(TextView::class.java).expand(view)
        removeClickableViews(textViews)
        removeSubtextViews(context, textViews)
        removeActionViews(notification.actions, textViews)

        // Deal with no views case after filter 1.
        if (textViews.size == 0) return

        // Process title text.
        val title = findTitleTextView(textViews)
        textViews.remove(title) // no need of title view anymore.
        if (title!!.text != null) {
            data.titleText = title.text
        }

        // Deal with no views case after filter 2.
        if (textViews.size == 0) return

        // Process all remaining texts.
        val messages = mutableListOf<CharSequence>()
        textViews.forEach {
            messages.add(it.text)
        }
        if (messages.isNotEmpty()) {
            // Store the message text lines.
            data.messageTextLines = messages.toTypedArray()
        }
    }

    private fun findTitleTextView(textViews: List<TextView?>): TextView? {
        // The idea is that title text is biggest from all
        // views here.
        var biggest: TextView? = null
        for (textView in textViews) {
            if (biggest == null || textView!!.textSize > biggest.textSize) {
                biggest = textView
            }
        }
        return biggest
    }

    private fun removeActionViews(actions: Array<Notification.Action>?, textViews: MutableList<TextView>) {
        if (actions == null) {
            return
        }
        for (action in actions) {
            for (i in textViews.indices.reversed()) {
                val text = textViews[i]!!.text
                if (text != null && text == action.title) {
                    textViews.removeAt(i)
                    break
                }
            }
        }
    }

    private fun removeClickableViews(textViews: MutableList<TextView>) {
        for (i in textViews.indices.reversed()) {
            val child = textViews[i]
            if (child!!.isClickable || child.visibility != View.VISIBLE) {
                textViews.removeAt(i)
                break
            }
        }
    }

    private fun removeSubtextViews(context: Context, textViews: MutableList<TextView>) {
        val subtextSize = context.resources.getDimension(R.dimen.notification_subtext_size)
        for (i in textViews.indices.reversed()) {
            val child = textViews[i]
            val text = child!!.text.toString()
            if (child.textSize == subtextSize // Empty textviews.
                    || text.matches(Regex("^(\\s*|)$")) // Clock textviews.
                    || text.matches(Regex("^\\d{1,2}:\\d{1,2}(\\s?\\w{2}|)$"))) {
                textViews.removeAt(i)
            }
        }
    }

    /**
     * RecursiveFinder is the class looking for instances of a given type in a given view group.
     *
     * @param <T> the type of object to look for.
    </T> */
    private class RecursiveFinder<T : View>(clazz: Class<T>) {
        private val list: MutableList<T>
        private val clazz: Class<T>
        fun expand(viewGroup: ViewGroup): MutableList<T> {
            val offset = 0
            val childCount = viewGroup.childCount
            for (i in 0 until childCount) {
                val child = viewGroup.getChildAt(i + offset)
                if (child != null) {
                    if (clazz.isAssignableFrom(child.javaClass)) {
                        // No inspection unchecked.
                        list.add(child as T)
                    } else if (child is ViewGroup) {
                        expand(child)
                    }
                }
            }
            return list
        }

        init {
            list = ArrayList()
            this.clazz = clazz
        }
    }

    companion object {
        /**
         * The tag to use for logging.
         */
        private val TAG = Extractor::class.java.simpleName

        /**
         * Removes all kinds of multiple spaces from given string.
         *
         * @param charSequence the char sequence to remove spaces from.
         */
        private fun removeSpaces(charSequence: CharSequence?): String? {
            if (charSequence == null) return null
            val string = if (charSequence is String) charSequence else charSequence.toString()
            return string
                    .replace("(\\s+$|^\\s+)".toRegex(), "")
                    .replace("\n+".toRegex(), "\n")
        }
    }
}