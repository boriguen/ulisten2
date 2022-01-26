package com.botob.ulisten2

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.botob.ulisten2.databinding.ActivityMainBinding
import com.botob.ulisten2.media.MediaAdapter
import com.botob.ulisten2.preferences.SettingsManager
import com.botob.ulisten2.services.MediaNotificationListenerService
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
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

    private lateinit var binding: ActivityMainBinding

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
    private lateinit var mediaArrayAdapter: MediaAdapter

    private val isListenerEnabled: Boolean
        get() = NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
            .contains(packageName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Instantiate the settings.
        settingsManager = SettingsManager(this)

        // Try to bind the service.
        tryBind()
    }

    override fun onDestroy() {
        tryUnbind()
        super.onDestroy()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (!isListenerEnabled && isChecked) {
            startActivityForResult(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                REQUEST_NOTIFICATION_ACCESS
            )
        } else {
            settingsManager.playServiceEnabled = isChecked
        }
    }

    private fun tryBind() {
        val intent = Intent(this@MainActivity, MediaNotificationListenerService::class.java)
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
            // Set the setting here as state switch listener is not triggered.
            settingsManager.playServiceEnabled = isListenerEnabled
        }
    }
}