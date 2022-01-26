package com.botob.ulisten2.media

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.botob.ulisten2.R
import java.util.*

class MediaAdapter(private val medias: List<Media>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.media_item, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Holder).bind(medias[position])
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.media_title)
        private val album = itemView.findViewById<TextView>(R.id.media_album)
        private val artist = itemView.findViewById<TextView>(R.id.media_artist)
        private val timestamp = itemView.findViewById<TextView>(R.id.media_timestamp)

        fun bind(media: Media) {
            title.text = media.title
            album.text = media.album
            artist.text = media.artist
            timestamp.text = media.broadcastTime.let {
                val calendar = Calendar.getInstance(Locale.ENGLISH)
                calendar.timeInMillis = it
                DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString()
            }
        }
    }
}