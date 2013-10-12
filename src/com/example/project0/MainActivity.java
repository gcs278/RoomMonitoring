package com.example.project0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	int timesCalled = 1;
	volatile LinkedHashMap<Integer, Float> compTemps = new LinkedHashMap<Integer, Float>();
	volatile LinkedHashMap<Integer, Float> roomTemps = new LinkedHashMap<Integer, Float>();
	volatile LinkedHashMap<Integer, Float> cpuUsage = new LinkedHashMap<Integer, Float>();
	volatile LinkedHashMap<Integer, Float> memoryUsage = new LinkedHashMap<Integer, Float>();
	volatile LinkedHashMap<Integer, Float> diskUsage = new LinkedHashMap<Integer, Float>();
	private ProgressDialog pd;

	static TextView compTemp;
	static TextView roomTemp;
	static TextView dateText;
	static TextView dateTextEnvir;
	static TextView tempOutText;
	static TextView humidityText;
	static float curRoomTemp = 0;
	static float curCompTemp = 0;
	static int tempOut = 0;
	static int humidity = 0;
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// Schedule the thread to run every _ seconds
		ScheduledExecutorService scheduler = Executors
				.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {

				new GetTemperature().execute();
				new GetEnvironmental().execute();
			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	private class GetTemperature extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String result = "";
			try {
				Socket s = new Socket("www.grantspence.com", 80);
				OutputStream stream = s.getOutputStream();
				stream.write("GET /temp.php HTTP/1.1\r\n".getBytes());
				stream.write("Host: grantspence.com\r\n".getBytes());
				stream.write("\r\n".getBytes());
				stream.flush();
				String line;

				BufferedReader bin = new BufferedReader(new InputStreamReader(
						s.getInputStream()));

				receiveHeader(bin, 6); // Get Header

				// Read the first line we get, and make it results
				while ((line = bin.readLine()) != null) {
					if (line.trim().length() > 0) {
						result = line;
						break; // Break from reading
					}
				}
				s.close();
			} catch (RuntimeException e) {
				System.out.println("Error: " + e.toString());
				return "error";
			} catch (IOException e) {
				System.out.println("Error: " + e.toString());
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {

			if (result.matches("[0-9]+,[0-9.]+,[0-9.]+,[0-9.]+")) {
				System.out.println("Result:" + result);
				String[] temps = result.split(",");
				java.util.Date time = new java.util.Date(
						Long.parseLong(temps[0]) * 1000);
				try {
					curCompTemp = Float.parseFloat(temps[1]);
					curRoomTemp = Float.parseFloat(temps[2]);
					compTemp.setText(temps[1] + (char) 0x00B0 + "F");
					roomTemp.setText(temps[2] + (char) 0x00B0 + "F");
					dateText.setText(time.toGMTString());
				} catch (NullPointerException e) {
					new AlertDialog.Builder(MainActivity.this).setMessage(
							"Whoops unable to update").show();
				}
			} else {
				new AlertDialog.Builder(MainActivity.this).setMessage(
						"I received an unknown data format: " + result).show();
			}

		}
	}

	private void receiveHeader(BufferedReader bin, int lines) {
		String status;
		try {
			status = bin.readLine().trim();

			// Check if we received an OK status message
			if (status.contains("HTTP/1.1 200 OK")) {
				for (int i = 0; i < lines; i++)
					bin.readLine();
			} else {
				new AlertDialog.Builder(MainActivity.this).setMessage(
						"Apache returned an error message:\n" + status).show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class GetEnvironmental extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String result = "";
			try {
				Socket s = new Socket("api.worldweatheronline.com", 80);
				OutputStream stream = s.getOutputStream();
				stream.write("GET /free/v1/weather.ashx?q=24060&format=xml&num_of_days=0&show_comments=no&key=mgwhw2gsgvmunzue38farg7v HTTP/1.1\r\n"
						.getBytes());
				stream.write("Host: api.worldweatheronline.com\r\n".getBytes());
				stream.write("\r\n".getBytes());
				stream.flush();
				BufferedReader bin1 = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				receiveHeader(bin1, 13);
				String line;
				while ((line = bin1.readLine()) != null) {
					System.out.println(line);
					if (line.trim().length() > 0) {
						result += line;
					}
					if (line.contains("</data>"))
						break;
				}
				InputSource source = new InputSource(new StringReader(result));
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(source);

				XPathFactory xpathFactory = XPathFactory.newInstance();
				XPath xpath = xpathFactory.newXPath();

				tempOut = Integer.parseInt(xpath.evaluate(
						"/data/current_condition/temp_F", document));
				xpath.reset();
				humidity = Integer.parseInt(xpath.evaluate(
						"/data/current_condition/humidity", document));

				s.close();
			} catch (RuntimeException e) {
				System.out.println("error" + e.toString());
				return "error";
			} catch (IOException e) {
				System.out.println("Error" + e.toString());
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			System.out.println("Updating Out" + Integer.toString(tempOut));
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			Date date = new Date(System.currentTimeMillis()-14400000);
			
			try {
				tempOutText.setText(Integer.toString(tempOut) + (char) 0x00B0
						+ "F");
				humidityText.setText(Integer.toString(humidity) + "%");
				dateTextEnvir.setText(date.toGMTString());
				
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	// Gets
	private class GetChartValues extends AsyncTask<Object, Void, String> {
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(MainActivity.this);
			pd.setTitle("Retrieving...");
			pd.setMessage("Please wait.");
			pd.setCancelable(false);
			pd.setIndeterminate(true);
			pd.show();
		}

		@Override
		protected String doInBackground(Object... params) {
			compTemps.clear();
			roomTemps.clear();
			try {
				System.out.println(params[0]);
				Socket s = new Socket("www.grantspence.com", 80);
				OutputStream stream = s.getOutputStream();
				System.out.println("here");
				String request = "GET /" + (String) params[0] + ".php?start="
						+ Long.toString((Long) params[1] / 1000) + "&end="
						+ Long.toString((Long) params[1] / 1000 + 86400)
						+ " HTTP/1.1\r\n";
				System.out.println(request);
				stream.write(request.getBytes());
				stream.write("Host: grantspence.com\r\n".getBytes());
				stream.write("\r\n".getBytes());
				stream.flush();
				String line;
				BufferedReader bin = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				receiveHeader(bin, 6);

				while ((line = bin.readLine()) != null) {
					System.out.println(line);
					line = line.replace("<br>", "");
					System.out.println("1");
					if (line.trim().length() > 1) {
						if (line.contains(",")) {
							String[] temps = line.split(",");
							compTemps.put(Integer.parseInt(temps[0]),
									Float.parseFloat(temps[1]));
							roomTemps.put(Integer.parseInt(temps[0]),
									Float.parseFloat(temps[2]));
						}
					}
					System.out.println("2");
					s.close();
				}
			} catch (RuntimeException e) {
				System.out.println("runtimeError" + e.toString());
			} catch (IOException e) {
				System.out.println("Error");
			}
			System.out.println("Done");
			return params[0].toString();
		}

		@Override
		protected void onPostExecute(String result) {
			if (pd != null) {
				pd.dismiss();
			}
			if (!compTemps.isEmpty() && !roomTemps.isEmpty()) {
				TemperatureGraph line = new TemperatureGraph();
				Intent lineIntent = line.getIntent(MainActivity.this,
						compTemps, roomTemps);
				startActivity(lineIntent);
			} else {
				new AlertDialog.Builder(MainActivity.this).setMessage(
						"The was no data for the requested date").show();
			}

		}
	}

	private DatePickerDialog.OnDateSetListener tempDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {

			timesCalled += 1;
			if ((timesCalled % 2) == 0) {

				GregorianCalendar time = new java.util.GregorianCalendar(
						selectedYear, selectedMonth, selectedDay, 0, 0);

				GetChartValues task = new GetChartValues();
				task.execute("temp", time.getTimeInMillis());

			}
		}
	};

	// Show the temperature graphs for a day
	public void showTempsGraph(View view) {
		// Build a calender
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create DatePicker which uses a listener
		final DatePickerDialog dpdFromDate = new DatePickerDialog(this,
				tempDateSetListener, year, month, day);

		dpdFromDate.show();
	}

	// Refresh the temperatures, calls network
	public void refreshTemps(View view) {
		GetTemperature task = new GetTemperature();
		task.execute();
	}

	// Refresh the Usage Stats, calls network
	public void refreshUsage(View view) {
		GetEnvironmental task = new GetEnvironmental();
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
				fragment = new ComputerTemp();
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

	public static class EnvironmentalFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.environmental, container,
					false);
			return rootView;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			tempOutText = (TextView) getActivity().findViewById(R.id.tempOut);
			humidityText = (TextView) getActivity().findViewById(R.id.humidity);
			dateTextEnvir = (TextView) getActivity().findViewById(R.id.dateTextEnvir);
			
			if (tempOut == 0)
				tempOutText.setText("--" + (char) 0x00B0 + "F");
			else
				tempOutText.setText(tempOut + (char) 0x00B0 + "F");

			if (humidity == 0)
				humidityText.setText("--" + "%");
			else
				humidityText.setText(humidity + " %");
		}
	}

	public static class ComputerTemp extends Fragment {
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
			compTemp = (TextView) getActivity().findViewById(R.id.compTemp);
			roomTemp = (TextView) getActivity().findViewById(R.id.roomTemp);
			dateText = (TextView) getActivity().findViewById(R.id.dateText);
			if (curCompTemp == 0)
				compTemp.setText("--" + (char) 0x00B0 + "F");
			else
				compTemp.setText(Float.toString(curCompTemp) + (char) 0x00B0
						+ "F");

			if (curRoomTemp == 0)
				roomTemp.setText("--" + (char) 0x00B0 + "F");
			else
				roomTemp.setText(Float.toString(curRoomTemp) + (char) 0x00B0
						+ "F");
		}
	}
}
