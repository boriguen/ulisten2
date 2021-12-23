package com.botob.ulisten2.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
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
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.botob.ulisten2.MainActivity
import com.botob.ulisten2.R
import com.botob.ulisten2.media.*
import com.botob.ulisten2.notification.Extractor
import com.botob.ulisten2.notification.NotificationData
import com.botob.ulisten2.preferences.SettingsManager
import java.util.*


class MediaNotificationListenerService : NotificationListenerService(),
    OnSharedPreferenceChangeListener {
    companion object {
        private val TAG = MediaNotificationListenerService::class.java.simpleName

        private const val NOTIFICATION_ACTION_PLAY = "com.botob.ulisten2.action.play"
        private const val NOTIFICATION_ACTION_PAUSE = "com.botob.ulisten2.action.pause"
    }

    private var currentMedia: Media? = null

    private lateinit var settingsManager: SettingsManager

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private lateinit var tts: TextToSpeech

    var audioFocusChangeListener: OnAudioFocusChangeListener? =
        OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                }
            }
        }
    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        Log.i(TAG, "onCreate")
        super.onCreate()

        // Get the audio manager.
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Instantiate the settings manager.
        settingsManager = SettingsManager(this)

        // Register this service to listen to preference changes.
        settingsManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        tts = TextToSpeech(this) {
            if (it != TextToSpeech.SUCCESS) {
                Log.e(TAG, "tts: initialization failed with status $it")
            }

            tts.language = Locale.getDefault()
            tts.voice = Voice(
                "en-us-x-sfg#male_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW,
                false, null
            )
            /*tts.voice = new Voice("en-us-x-sfg#female_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW,
                    false, null);*/
            tts.setSpeechRate(settingsManager.playMediaSpeed)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {}
                override fun onDone(utteranceId: String) {
                    // Abandon focus.
                    audioManager.abandonAudioFocus(audioFocusChangeListener)
                }

                override fun onError(utteranceId: String) {}
            })
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")

        when (intent.action) {
            NOTIFICATION_ACTION_PLAY -> {
                settingsManager.playServiceEnabled = true
            }
            NOTIFICATION_ACTION_PAUSE -> {
                settingsManager.playServiceEnabled = false
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        settingsManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        clearTts()
        clearAudioManager()

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
        processActiveStatusBarNotifications()
    }

    fun pause() {
        cancelPlayMedia()
        displayForegroundNotification() // Update notification by removing title.

        stopForeground(false)
    }

    private fun clearTts() {
        tts.stop()
        tts.shutdown()
    }

    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        Log.i(TAG, "**********  onNotificationPosted")

        if (settingsManager.playServiceEnabled) {
            // Process new media info.
            processStatusBarNotification(statusBarNotification)
        }
    }

    override fun onNotificationRemoved(statusBarNotification: StatusBarNotification) {
        Log.i(TAG, "**********  onNotificationRemoved")
        if (isPackageRelevant(statusBarNotification)) {
            cancelPlayMedia()
        }
    }

    private fun displayForegroundNotification() {
        val channelId = createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val views = RemoteViews(packageName, R.layout.status_bar)

        if (settingsManager.playServiceEnabled) {
            views.setImageViewResource(R.id.play_media_action, R.drawable.ic_pause_white_24dp)
            views.setOnClickPendingIntent(
                R.id.play_media_action,
                getPendingIntent(NOTIFICATION_ACTION_PAUSE)
            )

            views.setTextViewText(R.id.status_bar_track_name, currentMedia?.title)
            views.setTextViewText(R.id.status_bar_artist_name, currentMedia?.artist)
        } else {
            views.setImageViewResource(R.id.play_media_action, R.drawable.ic_play_white_24dp)
            views.setOnClickPendingIntent(
                R.id.play_media_action,
                getPendingIntent(NOTIFICATION_ACTION_PLAY)
            )

            views.setTextViewText(
                R.id.status_bar_track_name,
                getString(R.string.notification_title_paused)
            )
            views.setTextViewText(R.id.status_bar_artist_name, null)
        }

        val title = if (settingsManager.playServiceEnabled) {
            getString(R.string.notification_title_playing)
        } else {
            getString(R.string.notification_title_paused)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContent(views)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setWhen(0)
            .build()
        startForeground(1001, notification)
    }

    /**
     * Creates a notification channel for a foreground service.
     */
    private fun createNotificationChannel(): String {
        val channelId = "com.botob.ulisten2.channel.playmedia"
        val channelName = "UListen2 Play Service"
        NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE).also {
            it.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(it)
        }
        return channelId
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val serviceIntent = Intent(this, MediaNotificationListenerService::class.java).also {
            it.action = action
        }
        return PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
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
            val notificationData =
                extractor.load(applicationContext, statusBarNotification, NotificationData())
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
        displayForegroundNotification()
    }

    private fun logNotificationData(
        statusBarNotification: StatusBarNotification,
        notificationData: NotificationData?
    ) {
        val notificationInBrief = String.format(
            Locale.US,
            "{id: %d, time: %d, package: %s, Big title: %s, title: %s, " +
                    "summary: %s, info: %s, sub: %s, message: %s, message lines: %s}",
            statusBarNotification.id,
            statusBarNotification.postTime,
            statusBarNotification.packageName,
            notificationData!!.titleBigText,
            notificationData.titleText,
            notificationData.summaryText,
            notificationData.infoText,
            notificationData.subText,
            notificationData.messageText,
            TextUtils.concat(*notificationData.messageTextLines)
        )
        Log.i(TAG, notificationInBrief)
    }

    private fun broadcastMedia(media: Media) {
        val intent = Intent()
        intent.action = MainActivity.ACTION_BROADCAST_MEDIA
        intent.putExtra(MainActivity.EXTRA_BROADCAST_MEDIA, media)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun isPackageRelevant(statusBarNotification: StatusBarNotification): Boolean {
        return MediaApp.values().contentToString().contains(statusBarNotification.packageName)
    }

    private fun playMediaAsync() {
        handler.postDelayed(
            createPlayRunnable(),
            settingsManager.playMediaDelayInMilliseconds.toLong()
        )
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
                handler.postDelayed(this, settingsManager.playMediaIntervalInMilliseconds.toLong())
            }
        }
    }

    private fun playMedia() {
        currentMedia?.run {
            if (isRelevant) {
                // Prepare speech.
                val newSpeech = String.format("You listen to %s by %s", title, artist)

                // Request audio focus for playback
                val result = audioManager.requestAudioFocus(
                    audioFocusChangeListener,  // Use the notification stream.
                    AudioManager.STREAM_NOTIFICATION,  // Request audio focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    // Speak music notification info.
                    Log.i(TAG, "Speech = $newSpeech")
                    tts.speak(newSpeech, TextToSpeech.QUEUE_FLUSH, null, "UniqueID")
                }
            }
        }
    }

    private fun clearAudioManager() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
        audioFocusChangeListener = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SettingsManager.PLAY_MEDIA_DELAY || key == SettingsManager.PLAY_MEDIA_INTERVAL) {
            cancelPlayMedia()
            playMediaAsync()
        } else if (key == SettingsManager.PLAY_MEDIA_SPEED) {
            tts.setSpeechRate(settingsManager.playMediaSpeed)
        } else if (key == SettingsManager.PLAY_SERVICE_ENABLED) {
            if (settingsManager.playServiceEnabled) {
                resume()
            } else {
                pause()
            }
        }
    }

    inner class LocalBinder : Binder() {
        val serviceInstance: MediaNotificationListenerService
            get() = this@MediaNotificationListenerService
    }
}