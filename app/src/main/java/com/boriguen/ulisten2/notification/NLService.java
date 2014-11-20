package com.boriguen.ulisten2.notification;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();

    AudioManager am = null;

    TextToSpeech tts = null;

    List<String> speeches = null;

    @Override
    public void onCreate() {

        // Init audio manager
        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        // Init text-to-speech
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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
                        // Abandon focus
                        am.abandonAudioFocus(afChangeListener);
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
            }
        });

        // Init speeches list
        speeches = new LinkedList<String>();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "**********  onNotificationPosted");

        // Extract text info
        Extractor extractor = new Extractor();
        NotificationData notificationData = extractor.loadTexts(getApplicationContext(), sbn, new NotificationData());

        // Format notification info
        String notifInBrief = String.format("{id: %d, time: %d, package: %s, Big title: %s, title: %s, " +
                "summary: %s, info: %s, sub: %s, message: %s, message lines: %s}",
                sbn.getId(), sbn.getPostTime(), sbn.getPackageName(), notificationData.titleBigText,
                notificationData.titleText, notificationData.summaryText, notificationData.infoText, notificationData.subText,
                notificationData.messageText, notificationData.messageTextLines);
        Log.i(TAG, notifInBrief);

        // Prepare speech
        String[] songInfoSplit = notificationData.messageText.toString().split("\n"); // [0]: album, [1]: song
        String newSpeech = String.format("You listen to %s by %s", notificationData.titleText, songInfoSplit[1]);
        speeches.add(newSpeech);

        // Request audio focus for playback
        int result = am.requestAudioFocus(afChangeListener,
                // Use the notification stream.
                AudioManager.STREAM_NOTIFICATION,
                // Request audio focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Speak music notification info
            Log.i(TAG, "Speech = " + newSpeech);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            tts.speak(newSpeech, TextToSpeech.QUEUE_FLUSH, map);
        }

        // Broadcast notification info
        Intent i = new Intent("com.boriguen.ulisten2.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event", notifInBrief);
        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            // Pause playback
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Resume playback
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            //am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
            am.abandonAudioFocus(afChangeListener);
            // Stop playback
        }
    }
};
}