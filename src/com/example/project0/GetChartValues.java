package com.example.project0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

// Asynctask to retrieve a range of temperature and date values
// to display in a graph, using AChartEngine

class GetChartValues extends AsyncTask<Object, Void, String> {
	// Context from MainActivity so we can display dialogs
	Context mainActivity;

	// Contructor, get MainActivity context
	public GetChartValues(Context context) {
		mainActivity = context;
	}

	// Display a loading dialog
	@Override
	protected void onPreExecute() {
		// Display a loading widget to keep user happy
		MainActivity.pd = new ProgressDialog(mainActivity);
		MainActivity.pd.setTitle("Retrieving...");
		MainActivity.pd.setMessage("Please wait.");
		MainActivity.pd.setCancelable(false);
		MainActivity.pd.setIndeterminate(true);
		MainActivity.pd.show();
	}

	// Retrieve the data
	@Override
	protected String doInBackground(Object... params) {
		// Clear hashtables of any old daily values
		MainActivity.compTemps.clear();
		MainActivity.roomTemps.clear();
		try {
			// GrantSpence.com Socket
			Socket s = new Socket("www.grantspence.com", 80);
			OutputStream stream = s.getOutputStream();

			// Request using start date and end dates
			String request = "GET /" + (String) params[0] + ".php?start="
					+ Long.toString((Long) params[1] / 1000) + "&end="
					+ Long.toString((Long) params[1] / 1000 + 86400)
					+ " HTTP/1.1\r\n";
			stream.write(request.getBytes());
			System.out.println(request);
			stream.write("Host: grantspence.com\r\n".getBytes());
			stream.write("\r\n".getBytes());
			stream.flush();

			// Read the list of temperature values and store them in
			// hashtables
			String line;
			BufferedReader bin = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			MainActivity.receiveHeader(bin, 6); // Get Header
			while ((line = bin.readLine()) != null) {
				System.out.println(line);
				// Replace any html break
				line = line.replace("<br>", "");

				// Make sure data is valid
				if (line.trim().length() > 1) {
					// Make sure it matches what we want
					if (line.matches("[0-9]+,[0-9.]+,[0-9.]+,[0-9.]+")) {
						String[] temps = line.split(",");
						MainActivity.compTemps.put(Integer.parseInt(temps[0]),
								Float.parseFloat(temps[1]));
						MainActivity.roomTemps.put(Integer.parseInt(temps[0]),
								Float.parseFloat(temps[2]));
					}
				}

			}
			s.close();
		} catch (RuntimeException e) {
			System.out.println("RuntimeError" + e.toString());
		} catch (IOException e) {
			System.out.println("Error" + e.toString());
		}
		return params[0].toString();
	}

	// Open Graph
	@Override
	protected void onPostExecute(String result) {
		// Close Loading Dialog
		if (MainActivity.pd != null) {
			MainActivity.pd.dismiss();
		}
		// Display the graph if we got data sucessfully
		if (!MainActivity.compTemps.isEmpty()
				&& !MainActivity.roomTemps.isEmpty()) {
			TemperatureGraph line = new TemperatureGraph();
			Intent lineIntent = line.getIntent(mainActivity,
					MainActivity.compTemps, MainActivity.roomTemps);
			mainActivity.startActivity(lineIntent);
		} else {
			// No data was received
			new AlertDialog.Builder(mainActivity).setMessage(
					"The was no data for the requested date").show();
		}

	}
}