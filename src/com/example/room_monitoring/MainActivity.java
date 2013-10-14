package com.example.room_monitoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.project0.R;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.inputmethodservice.Keyboard.Key;
import android.net.wifi.WifiConfiguration.Status;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	int timesCalled = 1; // Variable to fix glich in datedialoglistener
	// HashMaps for CPU and room for displaying a graph
	volatile static LinkedHashMap<Integer, Float> compTemps = new LinkedHashMap<Integer, Float>();
	volatile static LinkedHashMap<Integer, Float> roomTemps = new LinkedHashMap<Integer, Float>();
	static ProgressDialog pd;

	// TextViews for computer_temp.xml
	static TextView compTempView, roomTemp1View, roomTemp2View, dateTextView;
	// TextViews for environmental.xml
	static TextView dateTextEnvir, tempOutText, humidityText, weatherDesc,
			tempHighText, tempLowText;
	// Stored temperature values, used for recreation of fragments
	static float curRoomTemp1 = 0;
	static float curRoomTemp2 = 0;
	static float curCompTemp = 0;
	static int curTempOut = 0;
	static int curHumidity = 0;
	static int curTempLow = 0;
	static int curTempHigh = 0;
	static String curConditions = "--";
	GetChartValues getChartValues = new GetChartValues(MainActivity.this);
	// Fragment variables
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the two
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// Schedule the thread to run every 10 seconds
		ScheduledExecutorService scheduler = Executors
				.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// Execute both of the asynctasks
				new GetTemperature(MainActivity.this).execute();
				new GetEnvironmental(MainActivity.this).execute();
			}
		}, 0, 60, TimeUnit.SECONDS);

		// Schedule the thread to run every 10 seconds
		ScheduledExecutorService scheduler2 = Executors
				.newSingleThreadScheduledExecutor();
		scheduler2.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// Execute both of the asynctasks
				new GetEnvironmental(MainActivity.this).execute();
			}
		}, 0, 60, TimeUnit.SECONDS);
	}

	// Removes the header from the incoming HTTP GET data
	// int lines indicates how many to remove
	static void receiveHeader(BufferedReader bin, int lines) {
		String status; // HTTP GET Status
		try {
			status = bin.readLine().trim();

			// Check if we received an OK status message
			if (status.contains("HTTP/1.1 200 OK")) {
				for (int i = 0; i < lines; i++)
					bin.readLine();
			} else {
				// Display error is HTTP Status is bad
				System.out.println("Header Status Error");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Listener for when the user picks a date, calls graph
	private DatePickerDialog.OnDateSetListener graphDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {

			// Glitch where it gets called twice, only way to fix
			timesCalled += 1;
			if ((timesCalled % 2) == 0) {

				// Convert the selected data into time value
				GregorianCalendar time = new java.util.GregorianCalendar(
						selectedYear, selectedMonth, selectedDay, 0, 0);

				getChartValues = new GetChartValues(MainActivity.this);
				// Get values and execute graph
				getChartValues.execute("temp", time.getTimeInMillis());

			}
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getChartValues.getStatus() == AsyncTask.Status.RUNNING)
				getChartValues.cancel(true);
		}
		return true;
	};

	// Show the temperature graphs for a day
	public void showTempsGraph(View view) {
		// Build a calendar
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create DatePicker which uses a listener
		final DatePickerDialog dpdFromDate = new DatePickerDialog(this,
				graphDateSetListener, year, month, day);
		dpdFromDate.show();
	}

	// Refresh the temperatures, calls network
	public void refreshTemps(View view) {
		GetTemperature task = new GetTemperature(MainActivity.this);
		task.execute();
	}

	// Refresh the Usage Stats, calls network
	public void refreshUsage(View view) {
		GetEnvironmental task = new GetEnvironmental(MainActivity.this);
		task.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment;
			switch (position) {
			case 0:
				fragment = new ComputerTempFragment();
				break;
			case 1:
				fragment = new EnvironmentalFragment();
				break;
			default:
				fragment = null;
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		// Returns the titles for the fragments
		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}

	}

	// Enivormental Conditions Fragment
	public static class EnvironmentalFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.environmental, container,
					false);
			return rootView;
		}

		// After view has been created
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			// Instantiate textviews
			tempOutText = (TextView) getActivity().findViewById(R.id.tempOut);
			humidityText = (TextView) getActivity().findViewById(R.id.humidity);
			dateTextEnvir = (TextView) getActivity().findViewById(
					R.id.dateTextEnvir);
			weatherDesc = (TextView) getActivity().findViewById(
					R.id.weatherDesc);
			tempHighText = (TextView) getActivity().findViewById(R.id.tempHigh);
			tempLowText = (TextView) getActivity().findViewById(R.id.tempLow);

			// Set Default views
			setTextViews(tempOutText, curTempOut);
			setTextViews(tempHighText, curTempHigh);
			setTextViews(tempLowText, curTempLow);
			if (curHumidity == 0)
				humidityText.setText("--" + "%");
			else
				humidityText.setText(curHumidity + " %");

			weatherDesc.setText(curConditions);
		}

		// Helper method to set textviews
		private void setTextViews(TextView textview, int value) {
			if (value == 0)
				textview.setText("--" + (char) 0x00B0 + "F");
			else
				textview.setText(value + (char) 0x00B0 + "F");
		}
	}

	// Computer and Room temp Fragments
	public static class ComputerTempFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.computer_temp, container,
					false);
			return rootView;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			// Instantiate textviews
			compTempView = (TextView) getActivity().findViewById(
					R.id.compTempText);
			roomTemp1View = (TextView) getActivity().findViewById(
					R.id.roomTemp1Text);
			roomTemp2View = (TextView) getActivity().findViewById(
					R.id.roomTemp2Text);
			dateTextView = (TextView) getActivity()
					.findViewById(R.id.dateText1);

			// Set the default values
			setTextViews(compTempView, curCompTemp);
			setTextViews(roomTemp1View, curRoomTemp1);
			setTextViews(roomTemp2View, curRoomTemp2);
		}

		// Helper method to set textviews
		private void setTextViews(TextView textview, float value) {
			if (value == 0)
				textview.setText("--" + (char) 0x00B0 + "F");
			else
				textview.setText(value + (char) 0x00B0 + "F");
		}
	}
}
