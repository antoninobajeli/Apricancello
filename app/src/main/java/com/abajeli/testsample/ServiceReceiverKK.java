package com.abajeli.testsample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ServiceReceiverKK extends BroadcastReceiver {
    TelephonyManager telephonyMng;


    static final String TAG="ServiceReceiver";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";


    public void onReceive(Context context, Intent intent) {


        if (intent.getAction() == SMS_RECEIVED) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }
                if (messages.length > -1) {
                    Log.i(TAG, "Message recieved: " + messages[0].getMessageBody());
                    Log.i(TAG, "Message indirizzo originario: " + messages[0].getOriginatingAddress());
                }
            }
        }






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
