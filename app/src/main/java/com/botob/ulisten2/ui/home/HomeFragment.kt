package com.botob.ulisten2.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.botob.ulisten2.MainActivity
import com.botob.ulisten2.R
import com.botob.ulisten2.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    companion object {
        /**
         * The tag to use for logging.
         */
        private val TAG = MainActivity::class.java.simpleName

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
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}