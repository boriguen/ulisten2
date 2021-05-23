package com.botob.ulisten2.notification

/**
 * Created by boris on 11/17/14.
 */
class NotificationData {
    var packageName: String? = null
    var postTime: Long = 0
    var titleBigText: CharSequence? = null
    var titleText: CharSequence? = null
    var messageText: CharSequence? = null
    var messageTextLines: Array<CharSequence> = arrayOf()
    var infoText: CharSequence? = null
    var subText: CharSequence? = null
    var summaryText: CharSequence? = null

    companion object {
        private val TAG = NotificationData::class.java.simpleName
    }
}