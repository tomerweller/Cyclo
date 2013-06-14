package com.ubi.cyclo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements LocationListener{
    private static final String TAG = "CYCLO";
    private static final String POI_ACTION = "com.ubi.cyclo.POI";
    public static final String POI_ID_EXTRA = "POI_ID_EXTRA";

    private LocationManager mLocationManager;
    private long mStartTime;
    private TextView mTimerLabel;
    private TextView mSpeedLabel;
    private TextView mDistanceLabel;

    private MockLocationHandler mMockLocationHandler;
    private Handler mHandler;
    private long mDistance;
    private boolean mIsOn;
    private Location mLastLocation;
    private List<POI> mPOIs;

    private Runnable timerUpdater = new Runnable() {
        @Override
        public void run() {
            updateTimer();
        }
    };
    private OldGraphView mGraph;

    private void updateTimer(){
        long timer = System.currentTimeMillis() - mStartTime;
        int minutes = (int)timer/(1000*60);
        int seconds = (int)(timer%(1000*60))/1000;

        String minutesStr = minutes<10? "0"+minutes : minutes+"";
        String secondsStr = seconds<10? "0"+seconds : seconds+"";

        mTimerLabel.setText(minutesStr+":"+secondsStr);
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

        mMockLocationHandler = new MockLocationHandler(this);
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mTimerLabel = (TextView)findViewById(R.id.timerLabel);
        mSpeedLabel = (TextView)findViewById(R.id.speedLabel);
        mDistanceLabel = (TextView)findViewById(R.id.distanceLabel);
        mGraph = (OldGraphView)findViewById(R.id.graph);
        mHandler = new Handler();
        mIsOn = false;

        mGraph.setMeasurementCount(300);
        mGraph.setLowerBound(0.0f);
        mGraph.setUpperBound(0.0f);

        List<Location> realPoints = OldGPXParser.getPoints(this, "gpxfile.xml", false);
        for(Location l : realPoints){
            Log.d(TAG, l.toString());
            mGraph.addValueToPlan((l.getSpeed()));
        }

        onBegin();
    }


    private void initPois(){
        int pendingIntentId = 100;

        mPOIs = new ArrayList<POI>();
        mPOIs.add(new POI(65.02848052978516, 25.42107582092285, R.drawable.eat));

        for (POI poi : mPOIs){
            Intent intent = new Intent(POI_ACTION);
            intent.putExtra(POI_ID_EXTRA, poi.getImgId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, pendingIntentId++,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mLocationManager.addProximityAlert(poi.getLatitude(), poi.getLongitude(), 20, -1,
                    pendingIntent);
        }

    }


    private void onBegin(){
        Log.d(TAG, "onBegin()");
        mIsOn =true;
        mDistance =0;
        mStartTime = System.currentTimeMillis();

        initPois();;

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
        mSpeedLabel.setText((int)location.getSpeed()+"km/h");

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

    BroadcastReceiver mPoiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "POI" + intent.getIntExtra(POI_ID_EXTRA, 0));
            Intent poiActivityIntent = new Intent(context, PoiActivity.class);
            poiActivityIntent.putExtras(intent);
            startActivity(poiActivityIntent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mPoiReceiver, new IntentFilter(POI_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mPoiReceiver);
    }




}
