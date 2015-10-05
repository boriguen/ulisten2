package com.boriguen.ulisten2.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.boriguen.ulisten2.media.IMedia;
import com.boriguen.ulisten2.media.MediaApp;
import com.boriguen.ulisten2.media.MediaFactory;
import com.boriguen.ulisten2.notification.Extractor;
import com.boriguen.ulisten2.notification.NotificationData;
import com.boriguen.ulisten2.prefs.SettingsManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NLService extends NotificationListenerService implements SharedPreferences.OnSharedPreferenceChangeListener{

    private String TAG = this.getClass().getSimpleName();

    AudioManager am = null;

    TextToSpeech tts = null;

    Timer timer = null;
    TimerTask task = null;

    List<String> speeches = null;

    IMedia currentMedia = null;

    SettingsManager settingsManager = null;

    @Override
    public void onCreate() {
        // Init audio manager.
        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        // Init tts.
        tts = getTts();

        // Init timer.
        timer = new Timer();

        // Init speeches list.
        speeches = new LinkedList<String>();

        // Instantiate the settings manager.
        settingsManager = new SettingsManager(getApplicationContext());

        // Register this service to listen to preference changes.
        settingsManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Clear TTS.
        tts.shutdown();

        // Clear timer.
        cancelPlayMedia();
        timer.cancel();

        // Unregister this service from listening to preference changes.
        settingsManager.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "**********  onNotificationPosted");

        // Process new media info.
        processStatusBarNotification(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "**********  onNotificationRemoved");

        if (isPackageRelevant(sbn)) {
            cancelPlayMedia();
        }
    }

    private void cancelPlayMedia() {
        // Cancel previous task if applicable.
        if (task != null) {
            task.cancel();
            timer.purge();
        }
    }

    private boolean isPackageRelevant(StatusBarNotification sbn) {
        return Arrays.toString(MediaApp.values()).contains(sbn.getPackageName());
    }

    private void processStatusBarNotification(StatusBarNotification sbn) {
        if (isPackageRelevant(sbn)) {
            // Extract text info.
            Extractor extractor = new Extractor();
            NotificationData notificationData = extractor.load(getApplicationContext(), sbn, new NotificationData());

            // Format notification info.
            String notifInBrief = String.format("{id: %d, time: %d, package: %s, Big title: %s, title: %s, " +
                            "summary: %s, info: %s, sub: %s, message: %s, message lines: %s}",
                    sbn.getId(), sbn.getPostTime(), sbn.getPackageName(), notificationData.titleBigText,
                    notificationData.titleText, notificationData.summaryText, notificationData.infoText, notificationData.subText,
                    notificationData.messageText, notificationData.messageTextLines);
            Log.i(TAG, notifInBrief);

            // Generate related media.
            IMedia media = MediaFactory.createMedia(notificationData);
            if (media != null && media.isRelevant() && !media.equals(currentMedia)) {
                // Update current media.
                currentMedia = media;

                // Launch the play media timer.
                playMediaAsync();
            }
        }
    }

    private TextToSpeech getTts() {
        return new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.getDefault());
                tts.setSpeechRate(settingsManager.getPlayMediaSpeed());
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        // Abandon focus.
                        am.abandonAudioFocus(afChangeListener);
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
            }
        });
    }

    private void playMediaAsync() {
        cancelPlayMedia();
        task = createTask();
        timer.scheduleAtFixedRate(task, settingsManager.getPlayMediaDelayInMilliseconds(),
                settingsManager.getPlayMediaIntervalInMilliseconds());
    }

    private TimerTask createTask() {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    playMedia();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        };
    }

    private void playMedia() {
        if (currentMedia != null && currentMedia.isRelevant()) {
            // Prepare speech.
            String newSpeech = String.format("You listen to %s by %s", currentMedia.getTitle(), currentMedia.getArtist());
            speeches.add(newSpeech);

            // Request audio focus for playback
            int result = am.requestAudioFocus(afChangeListener,
                    // Use the notification stream.
                    AudioManager.STREAM_NOTIFICATION,
                    // Request audio focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Speak music notification info.
                Log.i(TAG, "Speech = " + newSpeech);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                tts.speak(newSpeech, TextToSpeech.QUEUE_FLUSH, map);
            }
        }

    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {

            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

            }
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsManager.PLAY_MEDIA_DELAY) || key.equals(SettingsManager.PLAY_MEDIA_INTERVAL)) {
            cancelPlayMedia();
            playMediaAsync();
        } else if (key.equals(SettingsManager.PLAY_MEDIA_SPEED)) {
            tts.setSpeechRate(settingsManager.getPlayMediaSpeed());
        }
    }
}