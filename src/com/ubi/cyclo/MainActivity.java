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

public class MainActivity extends Activity implements LocationListener{
    private static final String TAG = "CYCLO";
    private LocationManager mLocationManager;
    private long startTime;
    private TextView mTimerLabel;
    private TextView mSpeedLabel;
    private TextView mDistanceLabel;

    private MockLocationHandler mMockLocationHandler;
    private Handler mHandler;
    private boolean isStarted;

    private Runnable timerUpdater = new Runnable() {
        @Override
        public void run() {
            updateTimer();
        }
    };

    private void updateTimer(){
        long timer = System.currentTimeMillis() - startTime;
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
        mHandler = new Handler();
        isStarted = false;

        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStarted) onBegin();
            }
        });

        onBegin();
    }

    private void onBegin(){
        Log.d(TAG, "onBegin()");
        isStarted=true;
        startTime = System.currentTimeMillis();

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
        mSpeedLabel.setText(location.getSpeed()+"km/h");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}


}
