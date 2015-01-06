package com.abajeli.aa_apricancelloautomatico;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by abajeli on 03/01/15.
 */

public class MyLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location locFromGps) {
        // called when the listener is notified with a location update from the GPS
    }

    @Override
    public void onProviderDisabled(String provider) {
        // called when the GPS provider is turned off (user turning off the GPS on the phone)
    }

    @Override
    public void onProviderEnabled(String provider) {
        // called when the GPS provider is turned on (user turning on the GPS on the phone)
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // called when the status of the GPS provider changes
    }
}
