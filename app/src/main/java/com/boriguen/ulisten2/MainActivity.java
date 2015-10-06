package com.boriguen.ulisten2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.boriguen.android.ulisten2.R;
import com.boriguen.ulisten2.prefs.SettingsActivity;
import com.boriguen.ulisten2.prefs.SettingsManager;
import com.boriguen.ulisten2.service.NLService;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private static final int REQUEST_SETTINGS = 0;
    private static final int REQUEST_NOTIFICATION_ACCESS = 1;

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
        serviceStateSwitch.setChecked(NLService.isNotificationAccessEnabled);
        serviceStateSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onDestroy() {
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
                startActivityForResult(i, REQUEST_SETTINGS);
                break;
        }

        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked && NLService.isNotificationAccessEnabled ||
                isChecked && !NLService.isNotificationAccessEnabled) {
            startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_NOTIFICATION_ACCESS);
        }
    }

    private void updateServiceState(boolean enabled) {
        if (enabled) {
            startService(new Intent(MainActivity.this, NLService.class));
        } else {
            stopService(new Intent(MainActivity.this, NLService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_NOTIFICATION_ACCESS) {
            serviceStateSwitch.setChecked(NLService.isNotificationAccessEnabled);
            //updateServiceState(NLService.isNotificationAccessEnabled);
        }

    }
}
