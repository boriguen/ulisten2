package com.boriguen.ulisten2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.boriguen.android.ulisten2.R;
import com.boriguen.ulisten2.prefs.SettingsActivity;
import com.boriguen.ulisten2.prefs.SettingsManager;
import com.boriguen.ulisten2.service.NLService;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private static final int RESULT_SETTINGS = 0;

    SettingsManager settingsManager = null;

    Switch serviceStateSwitch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize settings manager.
        settingsManager = new SettingsManager(getApplicationContext());

        setContentView(R.layout.activity_main);

        // Initialize service state switch component.
        serviceStateSwitch = (Switch) findViewById(R.id.switch_service_state);
        serviceStateSwitch.setOnCheckedChangeListener(this);
        serviceStateSwitch.setChecked(settingsManager.getPlayServiceEnabled());

        // Check notification access and act accordingly.
        //performNotificationAccessSteps();

        updateServiceState(serviceStateSwitch.isChecked());
    }

    @Override
    protected void onDestroy() {
        updateServiceState(false);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
        }

        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settingsManager.setPlayServiceEnabled(isChecked);
        startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), 0);
        updateServiceState(isChecked);
    }

    private void updateServiceState(boolean enabled) {
        if (enabled) {
            startService(new Intent(MainActivity.this, NLService.class));
        } else {
            stopService(new Intent(MainActivity.this, NLService.class));
        }
    }

    private void performNotificationAccessSteps() {
        Context context = getApplicationContext();
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();

        // Check to see if the enabledNotificationListeners String contains our package name.
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            serviceStateSwitch.setChecked(true);

        }
    }

}
