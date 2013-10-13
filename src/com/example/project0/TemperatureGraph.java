package com.example.project0;

import java.util.LinkedHashMap;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

// This is a class that returns intent to create a graph view
// Uses two hash maps for computer and room temperatures

public class TemperatureGraph {
	public Intent getIntent(Context context,
			LinkedHashMap<Integer, Float> cTemps,
			LinkedHashMap<Integer, Float> rTemps) {

		// Create two TimeSeries for each line
		java.util.Date time = null;
		TimeSeries compSeries = new TimeSeries("Computer Temperature");
		TimeSeries roomSeries = new TimeSeries("Room Temperature");
		
		// Add the values from hashtables to timeseries
		for (Map.Entry<Integer, Float> entry : cTemps.entrySet()) 
			compSeries.add(entry.getKey(), entry.getValue());
		for (Map.Entry<Integer, Float> entry : rTemps.entrySet())
			roomSeries.add(entry.getKey(), entry.getValue());

		// Create a dataset, add both of the series
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(compSeries);
		dataset.addSeries(roomSeries);

		// Create a renderer for each of lines
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		XYSeriesRenderer compRenderer = new XYSeriesRenderer();
		XYSeriesRenderer roomRenderer = new XYSeriesRenderer();
		
		// This will keep labels reasonable
		int xRate;
		if (cTemps.size() >= 10)
			xRate = cTemps.size() / 10;
		else 
			xRate = 1;
		
		// Create labels
		int i = 0;
		for (Map.Entry<Integer, Float> entry : cTemps.entrySet()) {
			// Only once every rate
			if (i % xRate == 0) {
				time = new java.util.Date(Long.valueOf(entry.getKey()) * 1000);
				String hhmm = time.getHours() + ":" + time.getMinutes();
				mRenderer.addTextLabel(entry.getKey(), hhmm);
			}
			i++;
		}

		// Set the attributes of the graph
		roomRenderer.setColor(Color.RED);
		mRenderer.setZoomEnabled(false);
		mRenderer.setXLabelsAlign(Align.CENTER);
		mRenderer.setXLabels(0);
		mRenderer.setLabelsTextSize(25);
		mRenderer.addSeriesRenderer(compRenderer);
		mRenderer.addSeriesRenderer(roomRenderer);
		mRenderer.setXTitle("Time(HH:mm)");
		mRenderer.setYTitle("Temperature(F)");
		mRenderer.setLegendTextSize(20);
		mRenderer.setShowCustomTextGrid(true);
		mRenderer.setYAxisMin(60);
		
		// Create the intent and return it
		Intent intent = ChartFactory.getLineChartIntent(context, dataset,
				mRenderer, "Temperatures");

		return intent;
	}
}
