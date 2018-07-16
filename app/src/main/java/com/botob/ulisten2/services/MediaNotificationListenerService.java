package com.botob.ulisten2.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.botob.ulisten2.MainActivity;
import com.botob.ulisten2.media.Media;
import com.botob.ulisten2.media.MediaApp;
import com.botob.ulisten2.media.MediaFactory;
import com.botob.ulisten2.notification.Extractor;
import com.botob.ulisten2.notification.NotificationData;
import com.botob.ulisten2.preferences.SettingsManager;

import java.util.Arrays;
import java.util.Locale;

public class MediaNotificationListenerService extends NotificationListenerService
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MediaNotificationListenerService.class.getSimpleName();

    private AudioManager mAudioManager;

    private TextToSpeech mTts;

    private Handler mHandler;

    private Media mCurrentMedia;

    private SettingsManager mSettingsManager;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        // Instantiate the settings manager.
        mSettingsManager = new SettingsManager(getApplicationContext());

        // Register this service to listen to preference changes.
        mSettingsManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (mSettingsManager != null) {
            // Unregister this service from listening to preference changes.
            mSettingsManager.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        resume();
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return super.onBind(intent);
        } else {
            return new LocalBinder();
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i(TAG, "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return true;
    }

    public void resume() {
        if (!isInitialized()) {
            // Initialize tts.
            mTts = getTts();
        }
        // Process active notification.
        processActiveStatusBarNotifications();
    }

    private boolean isInitialized() {
        return mHandler != null;
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    public void pause() {
        cancelPlayMedia();
        clearTts();
        clearAudioManager();
    }

    private void clearTts() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Log.i(TAG, "**********  onNotificationPosted");

        // Process new media info.
        processStatusBarNotification(statusBarNotification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        Log.i(TAG, "**********  onNotificationRemoved");

        if (isPackageRelevant(statusBarNotification)) {
            cancelPlayMedia();
        }
    }

    private void processActiveStatusBarNotifications() {
        StatusBarNotification[] activeNotifications = getActiveNotifications();
        if (activeNotifications != null) {
            for (StatusBarNotification statusBarNotification : getActiveNotifications()) {
                processStatusBarNotification(statusBarNotification);
            }
        }
    }

    private void processStatusBarNotification(StatusBarNotification statusBarNotification) {
        if (isPackageRelevant(statusBarNotification)) {
            final Extractor extractor = new Extractor();
            NotificationData notificationData = extractor.load(getApplicationContext(), statusBarNotification, new NotificationData());
            logNotificationData(statusBarNotification, notificationData);

            // Generate related media.
            final Media media = MediaFactory.createMedia(notificationData);
            if (media != null && !media.equals(mCurrentMedia)) {
                cancelPlayMedia();
                if (media.isRelevant()) {
                    mCurrentMedia = media;
                    playMediaAsync();
                    broadcastMedia(mCurrentMedia);
                }
            } else if (!isInitialized()) { // After service got stopped and started while same song is on.
                playMediaAsync();
            }
        }
    }

    private void logNotificationData(final StatusBarNotification statusBarNotification, final NotificationData notificationData) {
        String notificationInBrief = String.format(Locale.US, "{id: %d, time: %d, package: %s, Big title: %s, title: %s, " +
                        "summary: %s, info: %s, sub: %s, message: %s, message lines: %s}",
                statusBarNotification.getId(), statusBarNotification.getPostTime(),
                statusBarNotification.getPackageName(), notificationData.titleBigText,
                notificationData.titleText, notificationData.summaryText, notificationData.infoText,
                notificationData.subText, notificationData.messageText, TextUtils.concat(notificationData.messageTextLines));
        Log.i(TAG, notificationInBrief);
    }

    private void broadcastMedia(final Media media) {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_BROADCAST_MEDIA);
        intent.putExtra(MainActivity.EXTRA_BROADCAST_MEDIA, media);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private boolean isPackageRelevant(StatusBarNotification statusBarNotification) {
        return Arrays.toString(MediaApp.values()).contains(statusBarNotification.getPackageName());
    }

    private TextToSpeech getTts() {
        return new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTts.setLanguage(Locale.getDefault());
                mTts.setVoice(new Voice("en-us-x-sfg#male_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW,
                        false, null));
                /*mTts.setVoice(new Voice("en-us-x-sfg#female_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW,
                        false, null));*/
                mTts.setSpeechRate(mSettingsManager.getPlayMediaSpeed());
                mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        // Abandon focus.
                        getAudioManager().abandonAudioFocus(audioFocusChangeListener);
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
            }
        });
    }

    private void playMediaAsync() {
        getHandler().postDelayed(createPlayRunnable(), mSettingsManager.getPlayMediaDelayInMilliseconds());
    }

    private void cancelPlayMedia() {
        // Cancel previous tasks if applicable.
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    private Runnable createPlayRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                playMedia();
                if (mHandler != null) {
                    mHandler.postDelayed(this, mSettingsManager.getPlayMediaIntervalInMilliseconds());
                }
            }
        };
    }

    private void playMedia() {
        if (mCurrentMedia != null && mCurrentMedia.isRelevant()) {
            // Prepare speech.
            String newSpeech = String.format("You listen to %s by %s", mCurrentMedia.getTitle(),
                    mCurrentMedia.getArtist());

            // Request audio focus for playback
            int result = getAudioManager().requestAudioFocus(audioFocusChangeListener,
                    // Use the notification stream.
                    AudioManager.STREAM_NOTIFICATION,
                    // Request audio focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED && mTts != null) {
                // Speak music notification info.
                Log.i(TAG, "Speech = " + newSpeech);
                mTts.speak(newSpeech, TextToSpeech.QUEUE_FLUSH, null, "UniqueID");
            }
        }
    }

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {

            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

            }
        }
    };

    private AudioManager getAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        return mAudioManager;
    }

    private void clearAudioManager() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(audioFocusChangeListener);
            mAudioManager = null;
            audioFocusChangeListener = null;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (isInitialized()) {
            if (key.equals(SettingsManager.PLAY_MEDIA_DELAY) || key.equals(SettingsManager.PLAY_MEDIA_INTERVAL)) {
                cancelPlayMedia();
                playMediaAsync();
            } else if (key.equals(SettingsManager.PLAY_MEDIA_SPEED) && mTts != null) {
                mTts.setSpeechRate(mSettingsManager.getPlayMediaSpeed());
            }
        }
    }

    public class LocalBinder extends Binder {
        public MediaNotificationListenerService getServiceInstance() {
            return MediaNotificationListenerService.this;
        }
    }
}