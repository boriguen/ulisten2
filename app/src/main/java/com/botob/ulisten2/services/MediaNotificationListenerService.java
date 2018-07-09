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
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            resume();
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

            // Instantiate the handler.
            mHandler = new Handler();
        }
        // Process active notification.
        processActiveStatusBarNotifications();
    }

    private boolean isInitialized() {
        return mHandler != null;
    }

    public void pause() {
        // Clear runnable.
        cancelPlayMedia();

        if (mTts != null) {
            // Clear TTS.
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }

        // Clear audio.
        clearAudioManager();
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                StatusBarNotification[] activeNotifications = getActiveNotifications();
                if (activeNotifications != null) {
                    for (StatusBarNotification statusBarNotification : getActiveNotifications()) {
                        processStatusBarNotification(statusBarNotification);
                    }
                }
            }
        });
    }

    private void processStatusBarNotification(StatusBarNotification statusBarNotification) {
        if (isPackageRelevant(statusBarNotification)) {
            // Extract text info.
            Extractor extractor = new Extractor();
            NotificationData notificationData = extractor.load(getApplicationContext(), statusBarNotification, new NotificationData());

            // Format notification info.
            String notificationInBrief = String.format(Locale.US, "{id: %d, time: %d, package: %s, Big title: %s, title: %s, " +
                            "summary: %s, info: %s, sub: %s, message: %s, message lines: %s}",
                    statusBarNotification.getId(), statusBarNotification.getPostTime(),
                    statusBarNotification.getPackageName(), notificationData.titleBigText,
                    notificationData.titleText, notificationData.summaryText, notificationData.infoText,
                    notificationData.subText, notificationData.messageText, TextUtils.concat(notificationData.messageTextLines));
            Log.i(TAG, notificationInBrief);

            // Generate related media.
            Media media = MediaFactory.createMedia(notificationData);
            if (media != null && media.isRelevant() && !media.equals(mCurrentMedia)) {
                // Update current media.
                mCurrentMedia = media;

                // Launch the play media thread.
                cancelPlayMedia();
                playMediaAsync();

                // Broadcast media.
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_BROADCAST_MEDIA);
                intent.putExtra(MainActivity.EXTRA_BROADCAST_MEDIA, mCurrentMedia);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }

    private boolean isPackageRelevant(StatusBarNotification statusBarNotification) {
        return Arrays.toString(MediaApp.values()).contains(statusBarNotification.getPackageName());
    }

    private TextToSpeech getTts() {
        return new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTts.setLanguage(Locale.getDefault());
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
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.postDelayed(createPlayRunnable(), mSettingsManager.getPlayMediaDelayInMilliseconds());
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
        if (isInitialized() && mCurrentMedia != null && mCurrentMedia.isRelevant()) {
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