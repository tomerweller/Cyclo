package cyclo.gpxparser;


import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

import android.location.Location;

public class MainActivity extends Activity {

	private GraphView graphView = null;
	
	private static final String TAG = "Cyclo";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
		if (graphView == null) {
        	graphView = new GraphView(this);
        	
        	
        	graphView.setMeasurementCount(300);
			graphView.setLowerBound(0.0f);
			graphView.setUpperBound(0.0f);
        	
        	setContentView(graphView);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
