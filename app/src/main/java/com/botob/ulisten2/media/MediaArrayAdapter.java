package com.botob.ulisten2.media;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.botob.ulisten2.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MediaArrayAdapter extends ArrayAdapter<Media> {
    /**
     * The tag for logging.
     */
    private static final String TAG = MediaArrayAdapter.class.getSimpleName();

    public MediaArrayAdapter(final @NonNull Context context, final List<Media> values) {
        super(context, -1, values);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = layoutInflater.inflate(R.layout.list_row_media, parent, false);
        final Media media = getItem(position);
        if (media != null) {
            TextView view = rowView.findViewById(R.id.media_title);
            view.setText(media.getTitle());
            view = rowView.findViewById(R.id.media_album);
            view.setText(media.getAlbum());
            view = rowView.findViewById(R.id.media_artist);
            view.setText(media.getArtist());
            view = rowView.findViewById(R.id.media_timestamp);
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setTimeInMillis(media.getBroadcastTime());
            view.setText(DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString());
        } else {
            Log.w(TAG, "The media object in getView() is null.");
        }
        return rowView;
    }
}
