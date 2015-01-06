package com.abajeli.testsample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ServiceReceiver extends BroadcastReceiver {
    TelephonyManager telephonyMng;


    static final String TAG="ServiceReceiver";
    
    public void onReceive(Context context, Intent intent) {

        PhoneStateListener phoneListener = new PhoneStateListener();
        telephonyMng = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        
        /* telephonyMng.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        Log.d("calling", telephony.getLine1Number());*/
        
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);

        switch (telephonyMng.getCallState()) {

            case TelephonyManager.CALL_STATE_RINGING:
                    String phoneNr= intent.getStringExtra("incoming_number");
                    Log.d(TAG, phoneNr);
                    if (MainActivity.isNumberInList(phoneNr))
                         MainActivity.commuta();
                    
                    break;
        } 
    }


    
    public void onDestroy() {
    	telephonyMng.listen(null, PhoneStateListener.LISTEN_NONE);
    }

}
