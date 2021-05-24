package com.botob.ulisten2

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.botob.ulisten2.fragments.NavigationDrawerFragment
import com.botob.ulisten2.fragments.NavigationDrawerFragment.NavigationDrawerCallbacks
import com.botob.ulisten2.media.*
import com.botob.ulisten2.media.impl.FakeMedia
import com.botob.ulisten2.preferences.SettingsActivity
import com.botob.ulisten2.preferences.SettingsManager
import com.botob.ulisten2.services.MediaNotificationListenerService
import com.botob.ulisten2.services.MediaNotificationListenerService.LocalBinder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener, NavigationDrawerCallbacks {
    /**
     * The broadcast receiver handling media objects.
     */
    private var mBroadcastReceiver: MediaBroadcastReceiver? = null

    /**
     * The notification listener service to interact with.
     */
    private lateinit var mNotificationListenerService: MediaNotificationListenerService

    /**
     * The service connection used to bind to the MediaNotificationListenerService instance.
     */
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(TAG, "onServiceConnected - $name")
            val localBinder = service as LocalBinder
            mNotificationListenerService = localBinder.serviceInstance
            if (mSettingsManager.playServiceEnabled) {
                mNotificationListenerService.resume()
            } else {
                mNotificationListenerService.pause()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i(TAG, "onServiceDisconnected - $name")
        }
    }

    /**
     * The settings manager instance to get and set preferences.
     */
    private lateinit var mSettingsManager: SettingsManager

    /**
     * The adapter to control the media.
     */
    private var mMediaArrayAdapter: MediaArrayAdapter? = null

    /**
     * The service state switch component.
     */
    private lateinit var mServiceStateSwitch: SwitchMaterial

    /**
     * The list view showing the played media.
     */
    private lateinit var mPlayedMediaListView: ListView

    /**
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instantiate the settings.
        mSettingsManager = SettingsManager(this)

        // Set the main view.
        setContentView(R.layout.activity_main)

        // Initialize service state switch component.
        mServiceStateSwitch = findViewById(R.id.switch_service_state)
        mServiceStateSwitch.isChecked = mSettingsManager.playServiceEnabled
        mServiceStateSwitch.setOnCheckedChangeListener(this)

        // Initialize the media list.
        mMediaArrayAdapter = MediaArrayAdapter(this, ArrayList())
        mPlayedMediaListView = findViewById(R.id.list_played_media)
        mPlayedMediaListView.adapter = mMediaArrayAdapter
        val timestamp = System.currentTimeMillis()
        val packageName = applicationContext.packageName
        mMediaArrayAdapter!!.insert(FakeMedia("Hey yo", "", "Dorris Allan", timestamp, packageName), 0)
        mMediaArrayAdapter!!.insert(FakeMedia("What to eat tonight?", "", "Mark Sanders", timestamp, packageName), 0)
        mMediaArrayAdapter!!.insert(FakeMedia("Jogo bonito", "", "Nike Soccer", timestamp, packageName), 0)
        val navigationDrawerFragment = supportFragmentManager.findFragmentById(R.id.navigation_drawer) as NavigationDrawerFragment

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer, findViewById<View>(R.id.drawer_layout) as DrawerLayout)

        // Setup the broadcast receiver.
        mBroadcastReceiver = MediaBroadcastReceiver()
        registerMediaBroadcastReceiver()
    }

    /**
     * Registers an action to BroadCastReceiver.
     */
    private fun registerMediaBroadcastReceiver() {
        try {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_BROADCAST_MEDIA)
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver!!, intentFilter)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver!!)
        tryUnbind()
        super.onDestroy()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (!isListenerEnabled && isChecked) {
            startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    REQUEST_NOTIFICATION_ACCESS)
        } else {
            updateServiceState(isChecked)
        }
    }

    private val isListenerEnabled: Boolean
        get() = NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
                .contains(packageName)

    private fun updateServiceState(enabled: Boolean) {
        Log.i(TAG, "Enabling service: $enabled")
        mSettingsManager.playServiceEnabled = enabled
        if (enabled) {
            mNotificationListenerService.resume()
        } else {
            mNotificationListenerService.pause()
        }
    }

    private fun tryBind() {
        val intent = Intent(this@MainActivity, MediaNotificationListenerService::class.java)
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    private fun tryUnbind() {
        try {
            unbindService(mServiceConnection)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Could not unbind notification listener service: $e")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_NOTIFICATION_ACCESS) {
            mServiceStateSwitch.isChecked = isListenerEnabled
            // Set the setting here as state switch listener is not triggered.
            mSettingsManager.playServiceEnabled = isListenerEnabled
        }
    }

    override fun onNavigationDrawerItemSelected(position: Int) {
        // update the main content by replacing fragments
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit()
        when (position) {
            0 -> {
                val i = Intent(this, SettingsActivity::class.java)
                startActivityForResult(i, REQUEST_SETTINGS)
            }
        }
    }

    fun onSectionAttached(number: Int) {
        when (number) {
            1 -> {
            }
        }
    }

    /**
     * MediaBroadCastReceiver handles the media broadcasts.
     */
    internal inner class MediaBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                mMediaArrayAdapter!!.insert(intent.getParcelableExtra<Parcelable>(EXTRA_BROADCAST_MEDIA) as Media?, 0)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_main, container, false)
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            arguments?.let { (requireActivity() as MainActivity).onSectionAttached(it.getInt(ARG_SECTION_NUMBER)) }
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private const val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

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
}