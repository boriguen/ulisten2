package com.boriguen.ulisten2.service;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NLService extends NotificationListenerService {

    public static final long ASAP = 0;

    public static final long INTERVAL = 60000; // Every 1 minute.

    private String TAG = this.getClass().getSimpleName();

    AudioManager am = null;

    TextToSpeech tts = null;

    Timer timer = null;

    List<String> speeches = null;

    IMedia currentMedia = null;

    @Override
    public void onCreate() {

        // Init audio manager.
        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        // Init tts.
        tts = getTts();

        // Init speeches list.
        speeches = new LinkedList<String>();

        // Start playing media info asynchronous.
        playMediaAsync();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tts.shutdown();
        timer.cancel();
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
    }

    private void processStatusBarNotification(StatusBarNotification sbn) {
        if (Arrays.toString(MediaApp.values()).contains(sbn.getPackageName())) {

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
            if (media != null && media.isRelevant()) {
                // Update current media.
                currentMedia = media;
            }
        }
    }

    private TextToSpeech getTts() {
        return new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            tts.setLanguage(Locale.getDefault());
            tts.setSpeechRate(1f);
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
        final Handler handler = new Handler();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            playMedia();
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
        }, ASAP, INTERVAL);
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
}