package com.ubi.cyclo;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class MockLocationHandler {

    private Context mContext;
    private LocationListener mListener;
    private Handler mHandler;
    private List<Location> mockLocations;
    private int currentMockIndex;

    public MockLocationHandler(Context context, LocationListener listener) {
        mContext = context;
        mListener = listener;
        mHandler = new Handler();
        currentMockIndex = 0;

        //TODO : read mock data from XML
        Location location1 = new Location(LocationManager.GPS_PROVIDER);
        location1.setLatitude(5);
        location1.setAltitude(4);
        location1.setLongitude(10);
        location1.setSpeed(30);
        location1.setTime(1000);
        Location location2 = new Location(LocationManager.GPS_PROVIDER);
        location2.setLatitude(5);
        location2.setAltitude(4);
        location2.setLongitude(10);
        location2.setSpeed(50);
        location2.setTime(5000);
        mockLocations = new ArrayList<Location>();
        mockLocations.add(location1);
        mockLocations.add(location2);
    }

    private Runnable mockLocationRunnable = new Runnable() {
        @Override
        public void run() {
            postMockLocation();
        }
    };

    private void postMockLocation(){
        Location currentLocation = mockLocations.get(currentMockIndex);
        mListener.onLocationChanged(currentLocation);

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
