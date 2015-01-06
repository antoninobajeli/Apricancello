package com.abajeli.aa_apricancelloautomatico;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

            Toast.makeText(context,
                    "Calling interet Opener.", Toast.LENGTH_SHORT).show();

            Activity activity = (Activity) context;
            ((MainActivity)activity).doOpenGateThroughInternet();

            MainActivity.parla("abra cadaabra ... àpriti cancello!");
            MainActivity.activeArea(true);


        }else {
            Log.i("onLocationChanged.onReceive", "exiting: ");
            MainActivity.activeArea(false);
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
