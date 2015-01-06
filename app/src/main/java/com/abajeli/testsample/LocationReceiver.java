package com.abajeli.testsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class LocationReceiver extends BroadcastReceiver {
    TelephonyManager telephonyMng;

    List<String> whiteListNumbers = new ArrayList<String>();
    static final String TAG="ServiceReceiver";

    // Listener location events
    @Override
    public void onReceive(Context context, Intent intent) {

        final String key = LocationManager.KEY_PROXIMITY_ENTERING;
        final Boolean entering = intent.getBooleanExtra(key, false);

        if (entering) {
            Log.i("LocationReceiver.onReceive", "entering: ");
            Toast.makeText(context, "entering", Toast.LENGTH_SHORT).show();

            String uri = "tel:(+49)12345789";

            Intent telIntent = new Intent("com.android.phone.call");

            //Intent telIntent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(uri));


            try {
                context.startActivity(telIntent);
                Log.i("Finished making a call...", "");
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context,
                        "Call faild, please try again later.", Toast.LENGTH_SHORT).show();
            }


        } else {
            Log.i("onLocationChanged.onReceive", "exiting: ");
            Toast.makeText(context, "exiting", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNumberInList(String phoneNr){
    	for(String str: whiteListNumbers) {
            if(str.trim().equals(phoneNr)){
            	return true;
            }
        }
        return false;
    }
    
    public void onDestroy() {

    }

}
