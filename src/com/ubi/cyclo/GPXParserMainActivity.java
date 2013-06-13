package com.ubi.cyclo;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

import android.location.Location;

public class GPXParserMainActivity extends Activity {

	private GraphView graphView = null;
	
	private static final String TAG = "Cyclo";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
		if (graphView == null) {
        	graphView = new GraphView(this, null);
        	
        	
        	graphView.setMeasurementCount(300);
			graphView.setLowerBound(0.0f);
			graphView.setUpperBound(0.0f);
        	
        	setContentView(graphView);
        }
	}

	public void onResume(){
		super.onResume();
		      
		List<Location> realPoints = GPXParser.getPoints(this, "gpxfile.xml", false);
		for(Location l : realPoints){
			Log.d(TAG, l.toString());			
			graphView.addValueToPlan((float)(l.getSpeed()));
		}
		
		/*List<Location> fakePoints = GPXParser.getPoints(this, "gpxfile.xml", true);
		for(Location l : fakePoints){
			Log.d(TAG, l.toString());			
			graphView.addValueToLive((float)(l.getSpeed()));
		}*/
	}

}
