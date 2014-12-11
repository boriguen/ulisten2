package com.boriguen.ulisten2.notification;

import android.app.Notification;
import android.app.Notification.Action;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.boriguen.android.ulisten2.R;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by boris on 11/17/14.
 */
public final class Extractor {

    private static final String TAG = "Extractor";

    /**
     * Removes all kinds of multiple spaces from given string.
     */
    static String removeSpaces(CharSequence cs) {
        if (cs == null) return null;
        String string = cs instanceof String
                ? (String) cs : cs.toString();
        return string
                .replaceAll("(\\s+$|^\\s+)", "")
                .replaceAll("\n+", "\n");
    }

    static CharSequence mergeLargeMessage(CharSequence[] messages) {
        if (messages == null) return null;
        int length = messages.length;

        boolean isFirstMessage = true;
        boolean highlight = length > 1; // highlight first letters of messages or no?

        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (CharSequence message : messages) {
            CharSequence line = removeSpaces(message);
            if (TextUtils.isEmpty(line)) {
                Log.w(TAG, "One of text lines was null!");
                continue;
            }

            // Start every new message from new line
            if (!isFirstMessage & !(isFirstMessage = false)) {
                sb.append('\n');
            }

            int start = sb.length();
            sb.append(line);

            if (highlight) {
                sb.setSpan(new ForegroundColorSpan(0xaaFFFFFF),
                        start, start + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new UnderlineSpan(),
                        start, start + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return sb;
    }

    /**
     * Gets a bundle with additional data from notification.
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

    private NotificationData loadTexts(Context context, StatusBarNotification statusBarNotification, NotificationData data) {
        final Bundle extras = getExtras(statusBarNotification);

        if (extras != null) loadFromExtras(data, extras);
        if (TextUtils.isEmpty(data.titleText)
                && TextUtils.isEmpty(data.titleBigText)
                && TextUtils.isEmpty(data.messageText)
                && data.messageTextLines == null) {
            loadFromView(data, context, statusBarNotification);
        }
        return data;
    }

    public NotificationData load(Context context, StatusBarNotification statusBarNotification, NotificationData data) {
        data.packageName = statusBarNotification.getPackageName();
        data.postTime = statusBarNotification.getPostTime();
        loadTexts(context, statusBarNotification, data);

        return data;
    }

    //-- LOADING FROM EXTRAS --------------------------------------------------

    /**
     * Loads all possible texts from given {@link Notification#extras extras} to
     * {@link NotificationData}.
     *
     * @param extras extras to load from
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
            ArrayList<CharSequence> list = new ArrayList<CharSequence>();
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

    //-- LOADING FROM VIES ----------------------------------------------------

    private void loadFromView(NotificationData data,
                              Context context,
                              StatusBarNotification statusBarNotification) {
        ViewGroup view;
        Notification notification = statusBarNotification.getNotification();
        try {
            final RemoteViews rvs = notification.bigContentView == null ? notification.contentView : notification.bigContentView;

            // Try to load view from remote views.
            Context contextNotify = context.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_RESTRICTED);
            LayoutInflater inflater = (LayoutInflater) contextNotify.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = (ViewGroup) inflater.inflate(rvs.getLayoutId(), null);
            rvs.reapply(contextNotify, view);
        } catch (Exception e) {
            return;
        }

        ArrayList<TextView> textViews = new RecursiveFinder<TextView>(TextView.class).expand(view);
        removeClickableViews(textViews);
        removeSubtextViews(context, textViews);
        removeActionViews(notification.actions, textViews);

        // There're no views present.
        if (textViews.size() == 0)
            return;

        TextView title = findTitleTextView(textViews);
        textViews.remove(title); // no need of title view anymore
        data.titleText = title.getText();

        // There're no views present.
        if (textViews.size() == 0)
            return;

        int length = textViews.size();
        CharSequence[] messages = new CharSequence[length];
        for (int i = 0; i < length; i++) {
            messages[i] = textViews.get(i).getText();
        }

        data.messageText = mergeLargeMessage(messages);
    }

    private TextView findTitleTextView(ArrayList<TextView> textViews) {
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

    private void removeActionViews(Action[] actions, ArrayList<TextView> textViews) {
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

    private void removeClickableViews(ArrayList<TextView> textViews) {
        for (int i = textViews.size() - 1; i >= 0; i--) {
            TextView child = textViews.get(i);
            if (child.isClickable() || child.getVisibility() != View.VISIBLE) {
                textViews.remove(i);
                break;
            }
        }
    }

    private void removeSubtextViews(Context context, ArrayList<TextView> textViews) {
        float subtextSize = context.getResources().getDimension(R.dimen.notification_subtext_size);
        for (int i = textViews.size() - 1; i >= 0; i--) {
            final TextView child = textViews.get(i);
            final String text = child.getText().toString();
            if (child.getTextSize() == subtextSize
                    // empty textviews
                    || text.matches("^(\\s*|)$")
                    // clock textviews
                    || text.matches("^\\d{1,2}:\\d{1,2}(\\s?\\w{2}|)$")) {
                textViews.remove(i);
            }
        }
    }

    private static class RecursiveFinder<T extends View> {

        private final ArrayList<T> list;
        private final Class<T> clazz;

        public RecursiveFinder(Class<T> clazz) {
            this.list = new ArrayList<T>();
            this.clazz = clazz;
        }

        public ArrayList<T> expand(ViewGroup viewGroup) {
            int offset = 0;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i + offset);

                if (child == null) {
                    continue;
                }

                if (clazz.isAssignableFrom(child.getClass())) {
                    //noinspection unchecked
                    list.add((T) child);
                } else if (child instanceof ViewGroup) {
                    expand((ViewGroup) child);
                }
            }
            return list;
        }
    }

}
