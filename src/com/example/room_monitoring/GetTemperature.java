package com.example.room_monitoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

// Asynctask to retrieve temperature data from my server
// Retreives and updates static varialbes in MainActivity
// Room Sensor 1 and 2, Computer Temperature

class GetTemperature extends AsyncTask<String, Void, String> {
	Context mainActivity;

	public GetTemperature(Context context) {
		mainActivity = context;
	}
	
	// Let user know its updating
	@Override
	protected void onPreExecute() {
		MainActivity.dateTextEnvir.setText("Updating...Please Wait");
	}

	// Retrieve data
	@Override
	protected String doInBackground(String... params) {

		String result = "";
		try {
			// Domain of my server
			Socket s = new Socket();
			s.connect(new InetSocketAddress("www.grantspence.com", 80), 1000);
			OutputStream stream = s.getOutputStream();
			// temp.php will return the latest temp value
			stream.write("GET /temp.php HTTP/1.1\r\n".getBytes());
			stream.write("Host: grantspence.com\r\n".getBytes());
			stream.write("\r\n".getBytes());
			stream.flush();
			String line;

			BufferedReader bin = new BufferedReader(new InputStreamReader(
					s.getInputStream()));

			MainActivity.receiveHeader(bin, 6); // Get Header

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
			return e.toString();
		} catch (IOException e) {
			System.out.println("Error: " + e.toString());
			return e.toString();
		}
		return result;
	}

	// Process data, display
	@Override
	protected void onPostExecute(String result) {
		// Match the result with regex of what we are suppose to get
		if (result.matches("[0-9]+,[0-9.]+,[0-9.]+,[0-9.]+")) {
			// Split values
			String[] temps = result.split(",");
			java.util.Date time = new java.util.Date(
					Long.parseLong(temps[0]) * 1000);

			// Store the values
			MainActivity.curCompTemp = Float.parseFloat(temps[1]);
			MainActivity.curRoomTemp1 = Float.parseFloat(temps[2]);
			MainActivity.curRoomTemp2 = Float.parseFloat(temps[3]);

			// Update the GUI
			MainActivity.compTempView.setText(temps[1] + (char) 0x00B0 + "F");
			MainActivity.roomTemp1View.setText(temps[2] + (char) 0x00B0 + "F");
			MainActivity.roomTemp2View.setText(temps[3] + (char) 0x00B0 + "F");
			MainActivity.dateTextView.setText(time.toGMTString());
		} else if (result.contains("ConnectException")) {
			// No data was received
			new AlertDialog.Builder(mainActivity).setMessage(
					"Cannot connect to www.grantspence.com:80").show();
		} else if (result.contains("SocketTimeoutException")) {
			// No data was received
			new AlertDialog.Builder(mainActivity).setMessage(
					"Socket Timeout Exception: Cannot connect to www.grantspence.com:80").show();
		}

	}
}
