package com.ubi.cyclo;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity implements LocationListener{
    private static final String TAG = "CYCLO";
    private LocationManager mLocationManager;
    private long mStartTime;
    private TextView mTimerLabel;
    private TextView mSpeedLabel;
    private TextView mDistanceLabel;

    private MockLocationHandler mMockLocationHandler;
    private Handler mHandler;
    private long mDistance;
    private boolean mIsStarted;
    private Location mLastLocation;

    private Runnable timerUpdater = new Runnable() {
        @Override
        public void run() {
            updateTimer();
        }
    };
    private GraphView mGraph;

    private void updateTimer(){
        long timer = System.currentTimeMillis() - mStartTime;
        mTimerLabel.setText((int)(timer/1000)+"s");
        mHandler.postDelayed(timerUpdater, 1000);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cyclo_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMockLocationHandler = new MockLocationHandler(this, this);
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mTimerLabel = (TextView)findViewById(R.id.timerLabel);
        mSpeedLabel = (TextView)findViewById(R.id.speedLabel);
        mDistanceLabel = (TextView)findViewById(R.id.distanceLabel);
        mGraph = (GraphView)findViewById(R.id.graph);
        mHandler = new Handler();
        mIsStarted = false;

        mGraph.setMeasurementCount(300);
        mGraph.setLowerBound(0.0f);
        mGraph.setUpperBound(0.0f);

        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsStarted) onBegin();
            }
        });

        List<Location> realPoints = GPXParser.getPoints(this, "gpxfile.xml", false);
        for(Location l : realPoints){
            Log.d(TAG, l.toString());
            mGraph.addValueToPlan((l.getSpeed()));
        }

        onBegin();
    }

    private void onBegin(){
        Log.d(TAG, "onBegin()");
        mIsStarted =true;
        mDistance =0;
        mStartTime = System.currentTimeMillis();

        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(MockLocationHandler.PROVIDER, 0, 0, this);
        mMockLocationHandler.start();
        updateTimer();
    }

    @Override
    protected void onDestroy() {
//        mLocationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location Update:" + location);
        mSpeedLabel.setText((int)location.getSpeed()+"m/s");

        if (mLastLocation!=null){
            mDistance+=location.distanceTo(mLastLocation);
            mDistanceLabel.setText(mDistance+"m");
        }

        mLastLocation = location;

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}


}
