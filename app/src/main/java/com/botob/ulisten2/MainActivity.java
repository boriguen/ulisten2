package com.botob.ulisten2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.botob.ulisten2.fragments.NavigationDrawerFragment;
import com.botob.ulisten2.preferences.SettingsActivity;
import com.botob.ulisten2.preferences.SettingsManager;
import com.botob.ulisten2.services.MediaNotificationListenerService;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    /**
     * The tag to use for logging.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The code used when requesting settings.
     */
    private static final int REQUEST_SETTINGS = 0;

    /**
     * The code used when requesting notification access.
     */
    private static final int REQUEST_NOTIFICATION_ACCESS = 1;

    /**
     * The service connection used to bind to the MediaNotificationListenerService instance.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected - " + name);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected - " + name);
        }
    };

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * The service state switch component.
     */
    private Switch serviceStateSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initialize service state switch component.
        serviceStateSwitch = (Switch) findViewById(R.id.switch_service_state);
        serviceStateSwitch.setOnCheckedChangeListener(this);
        serviceStateSwitch.setChecked(new SettingsManager(this).getPlayServiceEnabled());

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private boolean isListenerEnabled() {
        return NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext())
                .contains(getPackageName());
    }

    @Override
    protected void onDestroy() {
        tryUnbind();
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isListenerEnabled() && isChecked) {
            startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    REQUEST_NOTIFICATION_ACCESS);
        } else {
            new SettingsManager(this).setPlayServiceEnabled(isChecked);
            updateServiceState(isChecked);
        }
    }

    private void updateServiceState(boolean enabled) {
        if (enabled) {
            tryBind();
        } else {
            tryUnbind();
        }
    }

    private void tryBind() {
        Intent intent = new Intent(MainActivity.this, MediaNotificationListenerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void tryUnbind() {
        try {
            unbindService(serviceConnection);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Could not unbind notification listener service: " + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NOTIFICATION_ACCESS) {
            serviceStateSwitch.setChecked(isListenerEnabled());
            // Call update state explicitly.
            updateServiceState(isListenerEnabled());
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
                mTitle = getString(R.string.drawer_title_section1);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            ((MainActivity) context).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
