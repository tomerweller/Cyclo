package com.ubi.cyclo;

/**
 * Created by tomerweller on 6/13/13.
 */
public class POI {

    private double mLatitude;
    private double mLongitude;
    private int mImgId;

    public POI(double latitude, double longitude, int imgId) {
        mLatitude = latitude;
        mLongitude = longitude;
        mImgId = imgId;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public int getImgId() {
        return mImgId;
    }

}