package com.boriguen.ulisten2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.boriguen.android.ulisten2.R;
import com.boriguen.ulisten2.prefs.SettingsActivity;
import com.boriguen.ulisten2.service.NLService;

public class MainActivity extends Activity {

    private static final int RESULT_SETTINGS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start notification listener service
        startService(new Intent(MainActivity.this, NLService.class));
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this, NLService.class));
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

}
