package com.abajeli.testsample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ServiceReceiver extends BroadcastReceiver {
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
                    final String verificationCode=messages[0].getMessageBody();
                    final String cellNumb=messages[0].getOriginatingAddress();

                    Log.i(TAG, "Message recieved: " + verificationCode);
                    Log.i(TAG, "Message indirizzo originario: " + messages[0].getOriginatingAddress());



                    Thread thread = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {


                                Long startTime,communicationStartTime = null;
                                startTime = System.currentTimeMillis();
                                String operationResult="Failed";

                                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

                                int CONNECTION_TIMEOUT=15000;
                                int SO_TIMEOUT=16000;

                                httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT); // in ms
                                httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);// in ms

                                HttpGet httppost = new HttpGet("http://www.bajeli.com/app/condominial/json-Services.php?serviceName=confirmCellNumber&number="+URLEncoder.encode(cellNumb,"UTF-8")+"&code="+verificationCode);
                                // Depends on your web service
                                httppost.setHeader("Content-type", "application/json");

                                InputStream inputStream = null;
                                String result = null;
                                String msg_res="";
                                try {
                                    communicationStartTime = System.currentTimeMillis();
                                    HttpResponse response = httpclient.execute(httppost);
                                    HttpEntity entity = response.getEntity();

                                    inputStream = entity.getContent();
                                    // json is UTF-8 by default
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                                    StringBuilder sb = new StringBuilder();

                                    String line = null;
                                    while ((line = reader.readLine()) != null)
                                    {
                                        sb.append(line + "\n");
                                    }
                                    result = sb.toString();


                                    Log.i(TAG, "Response " + result.toString());
                                }catch (Exception e) {
                                    // Oops
                                    e.printStackTrace();
                                    msg_res="GenericException ";
                                }
                                finally {
                                    try{
                                        if(inputStream != null)inputStream.close();
                                    }catch(Exception squish){

                                    }
                                }

                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });
                    thread.start();







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
