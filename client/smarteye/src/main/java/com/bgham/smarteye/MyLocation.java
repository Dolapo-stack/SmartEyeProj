package com.bgham.smarteye;

import android.Manifest;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class MyLocation implements LocationListener {

    Context context;
    public MyLocation(Context c){
        context = c;
    }

    /** getLocation method asks for android permissions from the
     * user to allow this app access their location
     * Only if permitted that we can successfully determine their GPS coordinates
     * @Note: ACCESS_FINE_LOCATION permission must be set in Manifest
     */
    public android.location.Location getLocation(){

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context,"Permission not granted",Toast.LENGTH_SHORT).show();
            return null;
        }
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isGPSEnabled){
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,6000,10,this);
            android.location.Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return l;

        }else{
            Toast.makeText(context,"Please enable GPS for emergency purposes.", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    public void onLocationChanged(android.location.Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



}

