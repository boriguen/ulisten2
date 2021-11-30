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
import android.widget.CompoundButton
import android.widget.ListView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.botob.ulisten2.databinding.ActivityMain2Binding
import com.botob.ulisten2.fragments.NavigationDrawerFragment
import com.botob.ulisten2.media.Media
import com.botob.ulisten2.media.MediaArrayAdapter
import com.botob.ulisten2.preferences.SettingsActivity
import com.botob.ulisten2.preferences.SettingsManager
import com.botob.ulisten2.services.MediaNotificationListenerService
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.ArrayList

class MainActivity2 : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
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

    private lateinit var binding: ActivityMain2Binding

    /**
     * The broadcast receiver handling media objects.
     */
    private lateinit var broadcastReceiver: MediaBroadcastReceiver

    /**
     * The notification listener service to interact with.
     */
    private lateinit var notificationListenerService: MediaNotificationListenerService

    /**
     * The service connection used to bind to the MediaNotificationListenerService instance.
     */
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(TAG, "onServiceConnected - $name")
            val localBinder = service as MediaNotificationListenerService.LocalBinder
            notificationListenerService = localBinder.serviceInstance
            if (settingsManager.playServiceEnabled) {
                notificationListenerService.resume()
            } else {
                notificationListenerService.pause()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i(TAG, "onServiceDisconnected - $name")
        }
    }

    /**
     * The settings manager instance to get and set preferences.
     */
    private lateinit var settingsManager: SettingsManager

    /**
     * The adapter to control the media.
     */
    private lateinit var mediaArrayAdapter: MediaArrayAdapter

    /**
     * The service state switch component.
     */
    private lateinit var serviceStateSwitch: SwitchMaterial

    /**
     * The list view showing the played media.
     */
    private lateinit var playedMediaListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main2)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Instantiate the settings.
        settingsManager = SettingsManager(this)

        // Try to bind the service.
        tryBind()

        // Initialize service state switch component.
        serviceStateSwitch = findViewById(R.id.switch_service_state)
        serviceStateSwitch.isChecked = settingsManager.playServiceEnabled
        serviceStateSwitch.setOnCheckedChangeListener(this)

        // Initialize the media list.
        mediaArrayAdapter = MediaArrayAdapter(this, ArrayList())
        playedMediaListView = findViewById(R.id.list_played_media)
        playedMediaListView.adapter = mediaArrayAdapter

        // Setup the broadcast receiver.
        broadcastReceiver = MediaBroadcastReceiver()
        registerMediaBroadcastReceiver()
    }

    /**
     * Registers an action to BroadCastReceiver.
     */
    private fun registerMediaBroadcastReceiver() {
        try {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_BROADCAST_MEDIA)
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        tryUnbind()
        super.onDestroy()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (!isListenerEnabled && isChecked) {
            startActivityForResult(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
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
        settingsManager.playServiceEnabled = enabled
        if (enabled) {
            notificationListenerService.resume()
        } else {
            notificationListenerService.pause()
        }
    }

    private fun tryBind() {
        val intent = Intent(this@MainActivity2, MediaNotificationListenerService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun tryUnbind() {
        try {
            unbindService(serviceConnection)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Could not unbind notification listener service: $e")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_NOTIFICATION_ACCESS) {
            serviceStateSwitch.isChecked = isListenerEnabled
            // Set the setting here as state switch listener is not triggered.
            settingsManager.playServiceEnabled = isListenerEnabled
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
                mediaArrayAdapter.insert(intent.getParcelableExtra<Parcelable>(EXTRA_BROADCAST_MEDIA) as Media?, 0)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }
}