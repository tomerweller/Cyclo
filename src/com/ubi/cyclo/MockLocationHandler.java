package com.ubi.cyclo;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class MockLocationHandler {

    public static final String PROVIDER = "OULU";

    private Context mContext;
    private final LocationManager mLocationManager;
    private LocationListener mListener;
    private Handler mHandler;
    private List<Location> mockLocations;
    private int currentMockIndex;

    public MockLocationHandler(Context context, LocationListener listener) {
        mContext = context;
        mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        try{
            mLocationManager.removeTestProvider(PROVIDER);
        } catch(IllegalArgumentException e){}
        mLocationManager.addTestProvider(PROVIDER,
                false, false, false, true, true, true, true, 0, 0);
        mLocationManager.setTestProviderEnabled(PROVIDER, true);
//        String name, boolean requiresNetwork, .... boolean supportsAltitude, boolean supportsSpeed, boolean supportsBearing, int powerRequirement, int accuracy

        mListener = listener;
        mHandler = new Handler();
        currentMockIndex = 0;

        //TODO : read mock data from XML
        Location location1 = new Location(PROVIDER);
        location1.setLatitude(5);
        location1.setAltitude(4);
        location1.setLongitude(10);
        location1.setSpeed(30);
        location1.setTime(2000);
        Location location2 = new Location(PROVIDER);
        location2.setLatitude(5);
        location2.setAltitude(4);
        location2.setLongitude(10);
        location2.setSpeed(50);
        location2.setTime(10000);

        mockLocations = GPXParser.getPoints(mContext, "gpxfile.xml", true);
//        mockLocations = new ArrayList<Location>();
//        mockLocations.add(location1);
//        mockLocations.add(location2);
    }

    private Runnable mockLocationRunnable = new Runnable() {
        @Override
        public void run() {
            postMockLocation();
        }
    };

    private void postMockLocation(){
        Location currentLocation = mockLocations.get(currentMockIndex);
//        mListener.onLocationChanged(currentLocation);
        mLocationManager.setTestProviderLocation(PROVIDER, currentLocation);

        ++currentMockIndex;
        //if there are more
        if (currentMockIndex<mockLocations.size()){
            Location nextLocation = mockLocations.get(currentMockIndex);
            long timeDelta = nextLocation.getTime()-currentLocation.getTime();
            mHandler.postDelayed(mockLocationRunnable, timeDelta);
        }
    }

    public void start(){
        postMockLocation();
    }


}
