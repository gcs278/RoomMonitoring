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

public class TemperatureGraph {
	public Intent getIntent(Context context, LinkedHashMap<Integer,Float> cTemps,LinkedHashMap<Integer,Float> rTemps) {
		
		java.util.Date time = null;
		TimeSeries compSeries = new TimeSeries("Computer Temperature");
		TimeSeries roomSeries = new TimeSeries("Room Temperature");
		
        for (Map.Entry<Integer, Float> entry : cTemps.entrySet()) {
        	compSeries.add(entry.getKey(), entry.getValue());
        }
        
        for (Map.Entry<Integer, Float> entry : rTemps.entrySet()) {
        	roomSeries.add(entry.getKey(), entry.getValue());
        }
        
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(compSeries);
		dataset.addSeries(roomSeries);
		
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		XYSeriesRenderer roomRenderer = new XYSeriesRenderer();
		int xRate;
		if(cTemps.size() >= 10) {
			xRate = cTemps.size() / 10;
		}else {
			xRate = 1;
		}
			int i = 0;
			for (Map.Entry<Integer, Float> entry : cTemps.entrySet()) {
				if (i % xRate == 0) {
					time = new java.util.Date(
							Long.valueOf(entry.getKey()) * 1000);
					String hhmm = time.getHours() + ":" + time.getMinutes();
					mRenderer.addTextLabel(entry.getKey(), hhmm);
				}
				i++;
			}
		
		roomRenderer.setColor(Color.RED);
		mRenderer.setZoomEnabled(false);
		mRenderer.setXLabelsAlign(Align.CENTER);
		mRenderer.setXLabels(0);
		mRenderer.setLabelsTextSize(25);
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.addSeriesRenderer(roomRenderer);
		mRenderer.setXTitle("Time(HH:mm)");
		mRenderer.setYTitle("Temperature(F)");
		mRenderer.setLegendTextSize(20);
		mRenderer.setShowCustomTextGrid(true);
		mRenderer.setYAxisMin(60);
		Intent intent = ChartFactory.getLineChartIntent(context, dataset, mRenderer, "Temperatures");
		
		
		return intent;
	}
}
