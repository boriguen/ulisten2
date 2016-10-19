package com.botob.ulisten2.notification;

import android.app.Notification;
import android.app.Notification.Action;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.botob.ulisten2.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Extractor is the class performing different manipulations on status bar notifications to
 * extract relevant information.
 *
 * @author boriguen
 * @date 11/17/14
 */
public class Extractor {

    /**
     * The tag to use for logging.
     */
    private static final String TAG = Extractor.class.getSimpleName();

    /**
     * Removes all kinds of multiple spaces from given string.
     *
     * @param charSequence the char sequence to remove spaces from.
     */
    private static String removeSpaces(CharSequence charSequence) {
        if (charSequence == null) return null;
        String string = charSequence instanceof String
                ? (String) charSequence : charSequence.toString();
        return string
                .replaceAll("(\\s+$|^\\s+)", "")
                .replaceAll("\n+", "\n");
    }

    /**
     * Gets a bundle with additional data from notification.
     *
     * @param statusBarNotification the notification to extract information from.
     */
    private Bundle getExtras(StatusBarNotification statusBarNotification) {
        // Access extras using reflections.
        try {
            Field field = statusBarNotification.getNotification().getClass().getDeclaredField("extras");
            field.setAccessible(true);
            return (Bundle) field.get(statusBarNotification.getNotification());
        } catch (Exception e) {
            Log.w(TAG, "Failed to access extras on Jelly Bean.");
            return null;
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
    private NotificationData loadTexts(Context context, StatusBarNotification statusBarNotification, NotificationData data) {
        final Bundle extras = getExtras(statusBarNotification);

        if (extras != null) {
            loadFromExtras(data, extras);
            Log.d(TAG, "Ended loading from extras.");
        }
        if (TextUtils.isEmpty(data.titleText)
                || TextUtils.isEmpty(data.titleBigText)
                || TextUtils.isEmpty(data.messageText)
                || data.messageTextLines == null) {
            Log.d(TAG, "Starting loading from view.");
            loadFromView(data, context, statusBarNotification);
            Log.d(TAG, "Ended loading from view.");
        }
        if (data.messageTextLines == null) {
            data.messageTextLines = new CharSequence[]{};
        }
        return data;
    }

    public NotificationData load(Context context, StatusBarNotification statusBarNotification, NotificationData data) {
        data.packageName = statusBarNotification.getPackageName();
        data.postTime = statusBarNotification.getPostTime();
        loadTexts(context, statusBarNotification, data);

        return data;
    }

    /**
     * Loads all possible texts from given extras to the given notification data object.
     *
     * @param extras the extras to load from.
     */
    private void loadFromExtras(NotificationData data, Bundle extras) {
        data.titleBigText = extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
        data.titleText = extras.getCharSequence(Notification.EXTRA_TITLE);
        data.infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
        data.subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        data.summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
        data.messageText = extras.getCharSequence(Notification.EXTRA_TEXT);

        CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if (lines != null) {
            // Ignore empty lines.
            List<CharSequence> list = new ArrayList<>();
            for (CharSequence msg : lines) {
                msg = removeSpaces(msg);
                if (!TextUtils.isEmpty(msg)) {
                    list.add(msg);
                }
            }

            // Create new array.
            if (list.size() > 0) {
                lines = list.toArray(new CharSequence[list.size()]);
                data.messageTextLines = lines;
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
    private void loadFromView(NotificationData data,
                              Context context,
                              StatusBarNotification statusBarNotification) {
        ViewGroup view;
        Notification notification = statusBarNotification.getNotification();
        try {
            final RemoteViews remoteViews = notification.bigContentView == null ? notification.contentView :
                    notification.bigContentView;

            // Try to load view from remote views.
            Context contextNotify = context.createPackageContext(statusBarNotification.getPackageName(),
                    Context.CONTEXT_RESTRICTED);
            LayoutInflater inflater = (LayoutInflater) contextNotify.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = (ViewGroup) inflater.inflate(remoteViews.getLayoutId(), null);
            remoteViews.reapply(contextNotify, view);
            Log.d(TAG, "View loaded from remote views.");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return;
        }

        // Remove non relevant text views.
        List<TextView> textViews = new RecursiveFinder<>(TextView.class).expand(view);
        removeClickableViews(textViews);
        removeSubtextViews(context, textViews);
        removeActionViews(notification.actions, textViews);

        // Deal with no views case after filter 1.
        if (textViews.size() == 0)
            return;

        // Process title text.
        TextView title = findTitleTextView(textViews);
        textViews.remove(title); // no need of title view anymore.
        if (title.getText() != null) {
            data.titleText = title.getText();
        }

        // Deal with no views case after filter 2.
        if (textViews.size() == 0)
            return;

        // Process all remaining texts.
        int length = textViews.size();
        CharSequence[] messages = new CharSequence[length];
        for (int i = 0; i < length; i++) {
            messages[i] = textViews.get(i).getText();
        }

        if (messages.length > 0) {
            // Store the message text lines.
            data.messageTextLines = messages;
        }
    }

    private TextView findTitleTextView(List<TextView> textViews) {
        // The idea is that title text is biggest from all
        // views here.
        TextView biggest = null;
        for (TextView textView : textViews) {
            if (biggest == null || textView.getTextSize() > biggest.getTextSize()) {
                biggest = textView;
            }
        }
        return biggest;
    }

    private void removeActionViews(Action[] actions, List<TextView> textViews) {
        if (actions == null) {
            return;
        }

        for (Action action : actions) {
            for (int i = textViews.size() - 1; i >= 0; i--) {
                CharSequence text = textViews.get(i).getText();
                if (text != null && text.equals(action.title)) {
                    textViews.remove(i);
                    break;
                }
            }
        }
    }

    private void removeClickableViews(List<TextView> textViews) {
        for (int i = textViews.size() - 1; i >= 0; i--) {
            TextView child = textViews.get(i);
            if (child.isClickable() || child.getVisibility() != View.VISIBLE) {
                textViews.remove(i);
                break;
            }
        }
    }

    private void removeSubtextViews(Context context, List<TextView> textViews) {
        float subtextSize = context.getResources().getDimension(R.dimen.notification_subtext_size);
        for (int i = textViews.size() - 1; i >= 0; i--) {
            final TextView child = textViews.get(i);
            final String text = child.getText().toString();
            if (child.getTextSize() == subtextSize
                    // Empty textviews.
                    || text.matches("^(\\s*|)$")
                    // Clock textviews.
                    || text.matches("^\\d{1,2}:\\d{1,2}(\\s?\\w{2}|)$")) {
                textViews.remove(i);
            }
        }
    }

    /**
     * RecursiveFinder is the class looking for instances of a given type in a given view group.
     *
     * @param <T> the type of object to look for.
     */
    private static class RecursiveFinder<T extends View> {
        private final List<T> list;
        private final Class<T> clazz;

        public RecursiveFinder(Class<T> clazz) {
            this.list = new ArrayList<>();
            this.clazz = clazz;
        }

        public List<T> expand(ViewGroup viewGroup) {
            int offset = 0;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i + offset);
                if (child != null) {
                    if (clazz.isAssignableFrom(child.getClass())) {
                        // No inspection unchecked.
                        list.add((T) child);
                    } else if (child instanceof ViewGroup) {
                        expand((ViewGroup) child);
                    }
                }
            }
            return list;
        }
    }
}
