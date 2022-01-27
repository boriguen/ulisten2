package com.botob.ulisten2.media

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.botob.ulisten2.R
import com.botob.ulisten2.databinding.MediaItemBinding
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
        private val binding = MediaItemBinding.bind(itemView)

        fun bind(media: Media) {
            binding.mediaTitle.text = media.title
            binding.mediaAlbum.text = media.album
            binding.mediaArtist.text = media.artist
            binding.mediaTimestamp.text = media.broadcastTime.let {
                val calendar = Calendar.getInstance(Locale.ENGLISH)
                calendar.timeInMillis = it
                DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString()
            }
        }
    }
}