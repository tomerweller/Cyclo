package com.ubi.cyclo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jjoe64.graphview.CycloGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

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
    private CycloGraphView mGraph;
    private FrameLayout mGraphContainer;
    private RunningAverageFilter mFilter;

    private void updateTimer(){
        long timer = System.currentTimeMillis() - mStartTime;
        int minutes = (int)timer/(1000*60);
        int seconds = (int)(timer%(1000*60))/1000;

        String minutesStr = minutes<10? "0"+minutes : minutes+"";
        String secondsStr = seconds<10? "0"+seconds : seconds+"";

        mTimerLabel.setText(minutesStr+":"+secondsStr + "(+0:05)");
        mTimerLabel.setTextColor(Color.GREEN);
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
        mHandler = new Handler();
        mIsOn = false;

//        mGraph = (OldGraphView)findViewById(R.id.graph);
//        mGraph.setMeasurementCount(300);
//        mGraph.setLowerBound(0.0f);
//        mGraph.setUpperBound(0.0f);
//        List<Location> realPoints = OldGPXParser.getPoints(this, "gpxfile.xml", false);
//        for(Location l : realPoints){
//            Log.d(TAG, l.toString());
//            mGraph.addValueToPlan((l.getSpeed()));
//        }

        mPastLocations = new ArrayList<Location>();

        mGraphContainer = (FrameLayout)findViewById(R.id.graph);
        mGraph = new CycloGraphView(this,"");
        mGraphContainer.addView(mGraph);
        mGraph.setShowLegend(false);
        mGraph.setDrawBackground(true);
        mGraph.setBackgroundColor(Color.BLACK);


        mFilter = new RunningAverageFilter(25);

        // add real data
        List<Location> realPoints =
                GPXParser.getPoints(this, "nallicruise.gpx", false);
        GraphView.GraphViewData[] realData = new GraphView.GraphViewData[realPoints.size()];
        if (realPoints.size() == 0){Log.e(TAG, "no points were read!");	return; }

        double sumFromStart = 0;
        Location prevPoint = null;

        for(int i = 0; i< realPoints.size(); i++){
            if(prevPoint == null)
                prevPoint = realPoints.get(i);

            double distFromPrevPoint = realPoints.get(i).distanceTo(prevPoint);
            sumFromStart += distFromPrevPoint;

            //double distFromStart = (double) realPoints.get(i).distanceTo( realPoints.get(0) );
            double altitude = realPoints.get(i).getAltitude();
            double filt_altitude = mFilter.filter(altitude);
            //realData[i] = new GraphView.GraphViewData(distFromStart, filt_altitude);
            realData[i] = new GraphView.GraphViewData(sumFromStart, filt_altitude);

            prevPoint = realPoints.get(i);


        }

        GraphViewSeries.GraphViewSeriesStyle realStyle =
                new GraphViewSeries.GraphViewSeriesStyle(Color.DKGRAY, 3, Color.DKGRAY, false);
        mGraph.addSeries(new GraphViewSeries("", realStyle, realData));

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

        initPois();

        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(MockLocationHandler.PROVIDER, 0, 0, this);
        mMockLocationHandler.start();
        updateTimer();
       testMock2(30);
//        mGraph.removeSeries(1);
        testMock1(35);
    }

    @Override
    protected void onDestroy() {
//        mLocationManager.removeUpdates(this);
        super.onDestroy();
    }


    List<Location> mPastLocations;

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location Update:" + location);
        mSpeedLabel.setText((int)location.getSpeed()+"km/h");

        if (mLastLocation!=null){
            mDistance+=location.distanceTo(mLastLocation);
            mDistanceLabel.setText(mDistance+"m");
        }

        if (mPastLocations.size()>1){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGraph.removeSeries(1);
                    testMock1(mPastLocations.size());
                    mGraph.invalidate();
                }
            });

        }

//            mGraph.removeSeries(1);
//
        mPastLocations.add(location);
        mLastLocation = location;




//        GraphView.GraphViewData[] locationSeries = new GraphView.GraphViewData[mPastLocations.size()];
//
//        for(int i = 0; i<locationSeries.length; i++){
//            double distanceFromStart = (double)mPastLocations.get(i).distanceTo(mPastLocations.get(0));
//            double altitude = mPastLocations.get(i).getAltitude();
//            double filtAltitude = mFilter.filter(altitude);
//            locationSeries[i] = new GraphView.GraphViewData(distanceFromStart, filtAltitude);
//        }
//
//        GraphViewSeries.GraphViewSeriesStyle seriesStyle =
//                new GraphViewSeries.GraphViewSeriesStyle(0xFFFFBB33, 3, 0xFFFFBB33, false);
//
//
//        mGraph.addSeries(new GraphViewSeries("", seriesStyle, locationSeries));
//
//        mGraph.invalidate();

    }

    private void testMock1(int size){

        List<Location> fakePoints = GPXParser.getPoints(this, "nallicruise.gpx", false);

        mFilter.reset();

        GraphView.GraphViewData[] fakeData = new GraphView.GraphViewData[size];
        if (fakePoints.size() == 0){Log.e(TAG, "no points were read!");	return; }

        double sumFromStart = 0;
        Location prevPoint = null;

        for(int i = 0; i<size; i++){
            if(prevPoint == null)
                prevPoint = fakePoints.get(i);

            double distFromPrevPoint = fakePoints.get(i).distanceTo(prevPoint);
            sumFromStart += distFromPrevPoint;

            //double distFromStart = (double) fakePoints.get(i).distanceTo( fakePoints.get(0) );
            double altitude = fakePoints.get(i).getAltitude();
            double filt_altitude = mFilter.filter(altitude);
            //fakeData[i] = new GraphView.GraphViewData(distFromStart, filt_altitude);
            fakeData[i] = new GraphView.GraphViewData(sumFromStart, filt_altitude);

            prevPoint = fakePoints.get(i);
        }
        GraphViewSeries.GraphViewSeriesStyle fakeStyle = new GraphViewSeries.GraphViewSeriesStyle(0xFFFFBB33, 3, 0xFFFFBB33, false);
        mGraph.addSeries(new GraphViewSeries("", fakeStyle, fakeData));
    }

    private void testMock2(int size){
        List<Location> fakePoints = GPXParser.getPoints(this, "nallicruise.gpx", false);

        mFilter.reset();

        GraphView.GraphViewData[] fakeData = new GraphView.GraphViewData[size];
        if (fakePoints.size() == 0){Log.e(TAG, "no points were read!");	return; }

        double sumFromStart = 0;
        Location prevPoint = null;

        for(int i = 0; i<size; i++){
            if(prevPoint == null)
                prevPoint = fakePoints.get(i);

            double distFromPrevPoint = fakePoints.get(i).distanceTo(prevPoint);
            sumFromStart += distFromPrevPoint;

            //double distFromStart = (double) fakePoints.get(i).distanceTo( fakePoints.get(0) );
            double altitude = fakePoints.get(i).getAltitude();
            double filt_altitude = mFilter.filter(altitude);
            //fakeData[i] = new GraphView.GraphViewData(distFromStart, filt_altitude);
            fakeData[i] = new GraphView.GraphViewData(sumFromStart, filt_altitude);

            prevPoint = fakePoints.get(i);
        }
        GraphViewSeries.GraphViewSeriesStyle fakeStyle = new GraphViewSeries.GraphViewSeriesStyle(Color.GRAY, 3, Color.GRAY, false);
        mGraph.addSeries(new GraphViewSeries("", fakeStyle, fakeData));
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
            Intent poiActivityIntent = new Intent(context, DummyActivity.class);
            poiActivityIntent.putExtras(intent);
            startActivity(poiActivityIntent);

            Dummy.class;
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
