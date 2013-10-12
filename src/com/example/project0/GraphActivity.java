package com.example.project0;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

public class GraphActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		// Show the Up button in the action bar.
		setupActionBar();
		System.out.println("here");
		
		@SuppressWarnings("unchecked")
		HashMap<Integer,Float> temperatures = (HashMap<Integer, Float>)getIntent().getSerializableExtra("Temp2");
		java.util.Date time = null;
	   // GraphViewData[] data = new GraphViewData[temperatures.size()];
	    Map<Integer,Float> sorted = new TreeMap<Integer,Float>(temperatures);
        int i = 0;
        for (Map.Entry<Integer, Float> entry : sorted.entrySet()) {
        	if ( i==0 ) {
        		time = new java.util.Date(Long.valueOf(entry.getKey())*1000);
        		System.out.println(time.toGMTString());
        	}
            //data[i] = new GraphViewData(i,entry.getValue());
           i++;
        }
        
        
//    	// init example series data
//        GraphViewSeries tempSeries = new GraphViewSeries("Computer Temp",new GraphViewSeriesStyle(Color.WHITE,3),data);
//        
//        GraphView graphView = new LineGraphView(
//        		this // context
//        		, "Computer Temperature" // heading
//        );
//        graphView.setHorizontalLabels(new String[]{Integer.toString(time.getHours())+":"+Integer.toString(time.getMinutes()),"test","test2","2kljd"});
//        graphView.addSeries(tempSeries); // data
//        System.out.println(Integer.toString(time.getHours())+":"+Integer.toString(time.getMinutes()));
//        System.out.println(time.getMinutes());
//        graphView.setScrollable(true);
//        graphView.setScalable(true);
//        graphView.setManualYAxisBounds(100, 60);
//        graphView.setViewPort(0, i);
//        
//        LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
//        layout.addView(graphView);
       
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.graph, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
