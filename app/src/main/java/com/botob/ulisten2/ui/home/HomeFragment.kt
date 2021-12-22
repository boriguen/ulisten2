package com.botob.ulisten2.ui.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.botob.ulisten2.MainActivity
import com.botob.ulisten2.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    companion object {
        /**
         * The tag to use for logging.
         */
        private val TAG = HomeFragment::class.java.simpleName

        /**
         * The broadcast media action key.
         */
        const val ACTION_BROADCAST_MEDIA = "com.botob.ulisten2.action.media"

        /**
         * The broadcast media extra key.
         */
        const val EXTRA_BROADCAST_MEDIA = "com.botob.ulisten2.extra.media"

        /**
         * The code used when requesting settings.
         */
        private const val REQUEST_SETTINGS = 0

        /**
         * The code used when requesting notification access.
         */
        private const val REQUEST_NOTIFICATION_ACCESS = 1
    }

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel.checked.observe(viewLifecycleOwner, {
            binding.switchServiceState.isChecked = it
        })

        return binding.root
    }
}