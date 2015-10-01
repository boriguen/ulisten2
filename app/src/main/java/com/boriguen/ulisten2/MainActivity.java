package com.boriguen.ulisten2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.boriguen.android.ulisten2.R;
import com.boriguen.ulisten2.service.NLService;

public class MainActivity extends Activity {

	private TextView txtView;
	private NotificationReceiver nReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txtView = (TextView) findViewById(R.id.textView);

        // Start notification listener service
        startService(new Intent(MainActivity.this, NLService.class));

        // Init notification receiver
		nReceiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.boriguen.ulisten2.NOTIFICATION_LISTENER_EXAMPLE");
		registerReceiver(nReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(nReceiver);
		stopService(new Intent(MainActivity.this, NLService.class));
		super.onDestroy();
	}

	public void buttonClicked(View v) {

		if (v.getId() == R.id.btnListNotify) {
			Intent i = new Intent(
					"com.boriguen.ulisten2.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
			i.putExtra("command", "list");
			sendBroadcast(i);
		}

	}

	class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String temp = intent.getStringExtra("notification_event") + "\n"
					+ txtView.getText();
			txtView.setText(temp);
		}
	}

}
