package com.ubi.cyclo;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;

import java.util.List;

public class MockLocationHandler {

    public static final String PROVIDER = "CYCLO_MOCK";

    private Context mContext;
    private final LocationManager mLocationManager;
    private Handler mHandler;
    private List<Location> mockLocations;
    private int currentMockIndex;

    public MockLocationHandler(Context context) {
        mContext = context;
        mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        try{
            mLocationManager.removeTestProvider(PROVIDER);
        } catch(IllegalArgumentException e){}
        mLocationManager.addTestProvider(
                PROVIDER, false, false, false, true, true, true, true, 0, 0);
        mLocationManager.setTestProviderEnabled(PROVIDER, true);

        mHandler = new Handler();
        currentMockIndex = 0;

        mockLocations = GPXParser.getPoints(mContext, "gpxfile.xml", true);
    }

    private Runnable mockLocationRunnable = new Runnable() {
        @Override
        public void run() {
            postMockLocation();
        }
    };

    private void postMockLocation(){
        Location currentLocation = mockLocations.get(currentMockIndex);
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
