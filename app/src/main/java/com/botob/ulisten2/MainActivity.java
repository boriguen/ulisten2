package com.botob.ulisten2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.botob.ulisten2.fragments.NavigationDrawerFragment;
import com.botob.ulisten2.media.Media;
import com.botob.ulisten2.media.MediaArrayAdapter;
import com.botob.ulisten2.media.impl.FakeMedia;
import com.botob.ulisten2.preferences.SettingsActivity;
import com.botob.ulisten2.preferences.SettingsManager;
import com.botob.ulisten2.services.MediaNotificationListenerService;

import java.util.ArrayList;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    /**
     * The tag to use for logging.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The broadcast media action key.
     */
    public static final String ACTION_BROADCAST_MEDIA = "com.botob.ulisten2.action.media";

    /**
     * The broadcast media extra key.
     */
    public static final String EXTRA_BROADCAST_MEDIA = "com.botob.ulisten2.extra.media";

    /**
     * The code used when requesting settings.
     */
    private static final int REQUEST_SETTINGS = 0;

    /**
     * The code used when requesting notification access.
     */
    private static final int REQUEST_NOTIFICATION_ACCESS = 1;

    /**
     * The broadcast receiver handling media objects.
     */
    private MediaBroadcastReceiver mBroadcastReceiver;

    /**
     * The notification listener service to interact with.
     */
    private MediaNotificationListenerService mNotificationListenerService;

    /**
     * The service connection used to bind to the MediaNotificationListenerService instance.
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected - " + name);
            MediaNotificationListenerService.LocalBinder localBinder =
                    (MediaNotificationListenerService.LocalBinder) service;
            mNotificationListenerService = localBinder.getServiceInstance();
            if (mSettingsManager.getPlayServiceEnabled()) {
                mNotificationListenerService.resume();
            } else {
                mNotificationListenerService.pause();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected - " + name);
        }
    };

    /**
     * The settings manager instance to get and set preferences.
     */
    private SettingsManager mSettingsManager;

    /**
     * The adapter to control the media.
     */
    private MediaArrayAdapter mMediaArrayAdapter;

    /**
     * The service state switch component.
     */
    private Switch mServiceStateSwitch;

    /**
     * The list view showing the played media.
     */
    private ListView mPlayedMediaListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate the settings.
        mSettingsManager = new SettingsManager(this);

        // Set the main view.
        setContentView(R.layout.activity_main);

        // Initialize service state switch component.
        mServiceStateSwitch = findViewById(R.id.switch_service_state);
        mServiceStateSwitch.setChecked(mSettingsManager.getPlayServiceEnabled());
        mServiceStateSwitch.setOnCheckedChangeListener(this);

        // Initialize the media list.
        mMediaArrayAdapter = new MediaArrayAdapter(this, new ArrayList<Media>());
        mPlayedMediaListView = findViewById(R.id.list_played_media);
        mPlayedMediaListView.setAdapter(mMediaArrayAdapter);
        final long timestamp = System.currentTimeMillis();
        final String packageName = getApplicationContext().getPackageName();
        mMediaArrayAdapter.insert(new FakeMedia("Hey yo", "", "Dorris Allan", timestamp, packageName), 0);
        mMediaArrayAdapter.insert(new FakeMedia("What to eat tonight?", "", "Mark Sanders", timestamp, packageName), 0);
        mMediaArrayAdapter.insert(new FakeMedia("Jogo bonito", "", "Nike Soccer", timestamp, packageName), 0);

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        // Setup the broadcast receiver.
        mBroadcastReceiver = new MediaBroadcastReceiver();
        registerMediaBroadcastReceiver();
    }

    /**
     * Registers an action to BroadCastReceiver.
     */
    private void registerMediaBroadcastReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_BROADCAST_MEDIA);
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        tryUnbind();
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isListenerEnabled() && isChecked) {
            startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    REQUEST_NOTIFICATION_ACCESS);
        } else {
            updateServiceState(isChecked);
        }
    }

    private boolean isListenerEnabled() {
        return NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext())
                .contains(getPackageName());
    }

    private void updateServiceState(boolean enabled) {
        Log.i(TAG, "Enabling service: " + enabled);
        mSettingsManager.setPlayServiceEnabled(enabled);
        if (mNotificationListenerService != null) {
            if (enabled) {
                mNotificationListenerService.resume();
            } else {
                mNotificationListenerService.pause();
            }
        } else {
            tryBind();
        }
    }

    private void tryBind() {
        Intent intent = new Intent(MainActivity.this, MediaNotificationListenerService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void tryUnbind() {
        try {
            unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Could not unbind notification listener service: " + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NOTIFICATION_ACCESS) {
            mServiceStateSwitch.setChecked(isListenerEnabled());
            // Set the setting here as state switch listener is not triggered.
            mSettingsManager.setPlayServiceEnabled(isListenerEnabled());
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, MainActivity.PlaceholderFragment.newInstance(position + 1))
                .commit();

        switch (position) {
            case 0:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, REQUEST_SETTINGS);
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                break;
        }
    }

    /**
     * MediaBroadCastReceiver handles the media broadcasts.
     */
    class MediaBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                mMediaArrayAdapter.insert((Media) intent.getParcelableExtra(EXTRA_BROADCAST_MEDIA), 0);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MainActivity.PlaceholderFragment newInstance(int sectionNumber) {
            MainActivity.PlaceholderFragment fragment = new MainActivity.PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);

        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            ((MainActivity) context).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
