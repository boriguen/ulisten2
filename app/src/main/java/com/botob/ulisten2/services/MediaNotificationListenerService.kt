package com.botob.ulisten2.services

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.*
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.text.TextUtils
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.botob.ulisten2.MainActivity
import com.botob.ulisten2.media.*
import com.botob.ulisten2.notification.Extractor
import com.botob.ulisten2.notification.NotificationData
import com.botob.ulisten2.preferences.SettingsManager
import java.util.*

class MediaNotificationListenerService : NotificationListenerService(), OnSharedPreferenceChangeListener {
    private var mAudioManager: AudioManager? = null

    private var currentMedia: Media? = null

    private lateinit var mSettingsManager: SettingsManager

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val tts: TextToSpeech by lazy {
        TextToSpeech(applicationContext) {
            if (it != TextToSpeech.SUCCESS) {
                Log.e(TAG, "tts: initialization failed with status $it")
            }
        }.apply {
            language = Locale.getDefault()
            voice = Voice("en-us-x-sfg#male_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW,
                    false, null)
            /*setVoice(new Voice("en-us-x-sfg#female_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW,
                    false, null));*/
            setSpeechRate(mSettingsManager.playMediaSpeed)
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {}
                override fun onDone(utteranceId: String) {
                    // Abandon focus.
                    audioManager!!.abandonAudioFocus(audioFocusChangeListener)
                }

                override fun onError(utteranceId: String) {}
            })
        }
    }

    var audioFocusChangeListener: OnAudioFocusChangeListener? = OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
            }
        }
    }
    private val audioManager: AudioManager?
        get() {
            if (mAudioManager == null) {
                mAudioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
            }
            return mAudioManager
        }

    override fun onCreate() {
        Log.i(TAG, "onCreate")
        super.onCreate()
        // Instantiate the settings manager.
        mSettingsManager = SettingsManager(applicationContext)

        // Register this service to listen to preference changes.
        mSettingsManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        mSettingsManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "onBind")
        resume()
        return if (SERVICE_INTERFACE == intent.action) {
            super.onBind(intent)
        } else {
            LocalBinder()
        }
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
        Log.i(TAG, "onRebind")
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i(TAG, "onUnbind")
        return true
    }

    fun resume() {
        // Process active notification.
        processActiveStatusBarNotifications()
    }

    fun pause() {
        cancelPlayMedia()
        clearTts()
        clearAudioManager()
    }

    private fun clearTts() {
        tts.stop()
        tts.shutdown()
    }

    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        Log.i(TAG, "**********  onNotificationPosted")

        // Process new media info.
        processStatusBarNotification(statusBarNotification)
    }

    override fun onNotificationRemoved(statusBarNotification: StatusBarNotification) {
        Log.i(TAG, "**********  onNotificationRemoved")
        if (isPackageRelevant(statusBarNotification)) {
            cancelPlayMedia()
        }
    }

    private fun processActiveStatusBarNotifications() {
        val activeNotifications = activeNotifications
        if (activeNotifications != null) {
            for (statusBarNotification in getActiveNotifications()) {
                processStatusBarNotification(statusBarNotification)
            }
        }
    }

    private fun processStatusBarNotification(statusBarNotification: StatusBarNotification) {
        if (isPackageRelevant(statusBarNotification)) {
            val extractor = Extractor()
            val notificationData = extractor.load(applicationContext, statusBarNotification, NotificationData())
            logNotificationData(statusBarNotification, notificationData)

            // Generate related media.
            val media = MediaFactory.createMedia(notificationData)
            if (media != null && media != currentMedia) {
                cancelPlayMedia()
                if (media.isRelevant) {
                    currentMedia = media
                    playMediaAsync()
                    currentMedia?.let { broadcastMedia(it) }
                }
            }
        }
    }

    private fun logNotificationData(statusBarNotification: StatusBarNotification, notificationData: NotificationData?) {
        val notificationInBrief = String.format(Locale.US, "{id: %d, time: %d, package: %s, Big title: %s, title: %s, " +
                "summary: %s, info: %s, sub: %s, message: %s, message lines: %s}",
                statusBarNotification.id, statusBarNotification.postTime,
                statusBarNotification.packageName, notificationData!!.titleBigText,
                notificationData.titleText, notificationData.summaryText, notificationData.infoText,
                notificationData.subText, notificationData.messageText, TextUtils.concat(*notificationData.messageTextLines))
        Log.i(TAG, notificationInBrief)
    }

    private fun broadcastMedia(media: Media) {
        val intent = Intent()
        intent.action = MainActivity.Companion.ACTION_BROADCAST_MEDIA
        intent.putExtra(MainActivity.Companion.EXTRA_BROADCAST_MEDIA, media)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun isPackageRelevant(statusBarNotification: StatusBarNotification): Boolean {
        return MediaApp.values().contentToString().contains(statusBarNotification.packageName)
    }// Abandon focus.

    private fun playMediaAsync() {
        handler.postDelayed(createPlayRunnable(), mSettingsManager.playMediaDelayInMilliseconds.toLong())
    }

    private fun cancelPlayMedia() {
        // Cancel previous tasks if applicable.
        handler.removeCallbacksAndMessages(null)
        currentMedia = null
    }

    private fun createPlayRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                playMedia()
                handler.postDelayed(this, mSettingsManager.playMediaIntervalInMilliseconds.toLong())
            }
        }
    }

    private fun playMedia() {
        currentMedia?.run {
            if (isRelevant) {
                // Prepare speech.
                val newSpeech = String.format("You listen to %s by %s", title, artist)

                // Request audio focus for playback
                val result = audioManager!!.requestAudioFocus(audioFocusChangeListener,  // Use the notification stream.
                        AudioManager.STREAM_NOTIFICATION,  // Request audio focus.
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    // Speak music notification info.
                    Log.i(TAG, "Speech = $newSpeech")
                    tts.speak(newSpeech, TextToSpeech.QUEUE_FLUSH, null, "UniqueID")
                }
            }
        }
    }

    private fun clearAudioManager() {
        if (mAudioManager != null) {
            mAudioManager!!.abandonAudioFocus(audioFocusChangeListener)
            mAudioManager = null
            audioFocusChangeListener = null
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SettingsManager.PLAY_MEDIA_DELAY || key == SettingsManager.PLAY_MEDIA_INTERVAL) {
            cancelPlayMedia()
            playMediaAsync()
        } else if (key == SettingsManager.PLAY_MEDIA_SPEED) {
            tts.setSpeechRate(mSettingsManager.playMediaSpeed)
        }
    }

    inner class LocalBinder : Binder() {
        val serviceInstance: MediaNotificationListenerService
            get() = this@MediaNotificationListenerService
    }

    companion object {
        private val TAG = MediaNotificationListenerService::class.java.simpleName
    }
}