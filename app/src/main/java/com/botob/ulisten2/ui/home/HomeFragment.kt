package com.botob.ulisten2.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.botob.ulisten2.MainActivity
import com.botob.ulisten2.databinding.FragmentHomeBinding
import com.botob.ulisten2.media.Media
import com.botob.ulisten2.media.MediaAdapter

class HomeFragment : Fragment() {

    companion object {
        /**
         * The tag for logging.
         */
        private val TAG = HomeFragment::class.java.simpleName
    }

    private lateinit var model: HomeViewModel

    private lateinit var binding: FragmentHomeBinding

    private lateinit var broadcastReceiver: HomeFragment.MediaBroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        model = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        model.checked.observe(viewLifecycleOwner, {
            binding.switchServiceState.isChecked = it
        })

        model.medias.observe(viewLifecycleOwner, {
            binding.listPlayedMedia.adapter = MediaAdapter(it)
        })

        // Setup the broadcast receiver.
        broadcastReceiver = MediaBroadcastReceiver()
        registerMediaBroadcastReceiver()

        return binding.root
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)

        super.onDestroy()
    }

    /**
     * Registers an action to BroadCastReceiver.
     */
    private fun registerMediaBroadcastReceiver() {
        try {
            val intentFilter = IntentFilter()
            intentFilter.addAction(MainActivity.ACTION_BROADCAST_MEDIA)
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(broadcastReceiver, intentFilter)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    /**
     * MediaBroadCastReceiver handles the media broadcasts.
     */
    internal inner class MediaBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                (intent.getParcelableExtra<Parcelable>(MainActivity.EXTRA_BROADCAST_MEDIA) as Media?)?.let {
                    model.addMedia(it)
                }
            } catch (exception: Exception) {
                Log.e(TAG, exception.toString())
            }
        }
    }
}