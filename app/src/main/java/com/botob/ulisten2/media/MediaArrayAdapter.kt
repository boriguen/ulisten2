package com.botob.ulisten2.media

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.botob.ulisten2.R
import java.util.*

class MediaArrayAdapter(context: Context, values: List<Media?>?) : ArrayAdapter<Media?>(context, -1, values!!) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = layoutInflater.inflate(R.layout.list_row_media, parent, false)
        val media = getItem(position)
        if (media != null) {
            var view = rowView.findViewById<TextView>(R.id.media_title)
            view.text = media.title
            view = rowView.findViewById(R.id.media_album)
            view.text = media.album
            view = rowView.findViewById(R.id.media_artist)
            view.text = media.artist
            view = rowView.findViewById(R.id.media_timestamp)
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = media.broadcastTime
            view.text = DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString()
        } else {
            Log.w(TAG, "The media object in getView() is null.")
        }
        return rowView
    }

    companion object {
        /**
         * The tag for logging.
         */
        private val TAG = MediaArrayAdapter::class.java.simpleName
    }
}