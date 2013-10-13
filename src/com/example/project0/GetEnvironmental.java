package com.example.project0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Date;

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
import android.content.Context;
import android.os.AsyncTask;
import com.example.project0.MainActivity;

// Asynctask to retrieve the current outdoor data
// Gets temperatures and current weather conditions
// Updates GUI on post

class GetEnvironmental extends AsyncTask<String, Void, String> {
	Context mainActivity;

	public GetEnvironmental(Context context) {
		mainActivity = context;
	}

	// Get Data
	@Override
	protected String doInBackground(String... params) {
		String result = "";
		try {
			// Using WorldWeatherOnline's API socket
			Socket s = new Socket();
			s.connect(new InetSocketAddress("api.worldweatheronline.com", 80),
					1000);

			OutputStream stream = s.getOutputStream();

			// Created query for Blacksburg
			stream.write("GET /free/v1/weather.ashx?q=24060&format=xml&num_of_days=0&show_comments=no&key=mgwhw2gsgvmunzue38farg7v HTTP/1.1\r\n"
					.getBytes());
			stream.write("Host: api.worldweatheronline.com\r\n".getBytes());
			stream.write("\r\n".getBytes());
			stream.flush();
			BufferedReader bin1 = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			MainActivity.receiveHeader(bin1, 13); // Remove Header Data

			// Read in the Weather XML data
			String line;
			while ((line = bin1.readLine()) != null) {
				// Make sure line is valid
				if (line.trim().length() > 0) {
					result += line;
				}
				// Stop if we reach end
				if (line.contains("</data>"))
					break;
			}
			// Build Document to parse the XML data
			InputSource source = new InputSource(new StringReader(result));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(source);
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();

			// Retrieve the current outdoor temperature
			MainActivity.curTempOut = Integer.parseInt(xpath.evaluate(
					"/data/current_condition/temp_F", document));
			xpath.reset(); // Reset XML file
			// Retrieve the humidity
			MainActivity.curHumidity = Integer.parseInt(xpath.evaluate(
					"/data/current_condition/humidity", document));
			xpath.reset();
			MainActivity.curConditions = xpath.evaluate(
					"/data/current_condition/weatherDesc", document);
			xpath.reset();
			MainActivity.curTempLow = Integer.parseInt(xpath.evaluate(
					"/data/weather/tempMinF", document));
			xpath.reset();
			MainActivity.curTempHigh = Integer.parseInt(xpath.evaluate(
					"/data/weather/tempMaxF", document));

			s.close();
			result = "success";
		} catch (RuntimeException e) {
			return e.toString();
		} catch (IOException e) {
			return e.toString();
		} catch (ParserConfigurationException e) {
			return "ParseException";
		} catch (SAXException e) {
			return "ParseException";
		} catch (XPathExpressionException e) {
			return "ParseException";
		}
		return result;
	}

	// Update GUI
	@Override
	protected void onPostExecute(String result) {
		if (result == "success") {
			// Get the current date
			Date date = new Date(System.currentTimeMillis() - 14400000);

			// Update the GUI
			MainActivity.tempOutText.setText(Integer
					.toString(MainActivity.curTempOut) + (char) 0x00B0 + "F");
			MainActivity.tempLowText.setText(Integer
					.toString(MainActivity.curTempLow) + (char) 0x00B0 + "F");
			MainActivity.tempHighText.setText(Integer
					.toString(MainActivity.curTempHigh) + (char) 0x00B0 + "F");
			MainActivity.humidityText.setText(Integer
					.toString(MainActivity.curHumidity) + "%");
			MainActivity.weatherDesc.setText(MainActivity.curConditions);
			MainActivity.dateTextEnvir.setText(date.toGMTString());
		} 

	}
}