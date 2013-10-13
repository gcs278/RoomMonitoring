package com.example.room_monitoring;

import com.example.project0.R;

import android.os.Bundle;
import android.app.Activity;
import android.annotation.TargetApi;
import android.os.Build;

// Activity Class for graph view. Once User presses graph button
// and selects a date, this activity will be called
// Intent will be hashtables with temperatures

public class GraphActivity extends Activity {

	// Create Activity
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		setupActionBar();
	}

	// Set up Action bar
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}


}
