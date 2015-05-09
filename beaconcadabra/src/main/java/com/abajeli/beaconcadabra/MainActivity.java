package com.abajeli.beaconcadabra;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends Activity implements BeaconConsumer,SensorEventListener {


    public static Context baseContext;
    private TextView mTextView;
    long previousOpenedTime=0;
    double minDistToOppen=0.5;
    long minPeriodBeetwenOpen=15000;
    long maxDist=25;
    double correctionFactor=4.0;

    private BeaconManager beaconManager;


    public static final int NOTIFICATION_ID = 32143;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EXTRA_EVENT_ID = TAG + ".EXTRA_EVENT_ID";
    private static final String EXTRA_EVENT_TEXT = TAG + ".EXTRA_EVENT_TEXT";
    private static final int EVENT_RELAUNCH = 1;
    private static final int EVENT_TALK_BACK = 2;




    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    RadioButton selectedradiogroup;
    static boolean apribile=false;


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }



    private SensorManager sensorMgr ;

    private long timeRight,lastUpdate,elapseRight = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 400;
    private boolean speadRight,letOpen;


    @Override
    public final void onSensorChanged(SensorEvent event) {
        Log.d("sensor", "sensor.getType() " + event.sensor.getType()+"   Sensor.TYPE_ACCELEROMETER"+Sensor.TYPE_ACCELEROMETER);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Log.d("sensor", "sens "+values[SensorManager.AXIS_X]+" "+values[SensorManager.AXIS_Y]+" "+values[SensorManager.AXIS_Z] );
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                x = event.values[SensorManager.AXIS_X];
                y = event.values[SensorManager.AXIS_Y];
                z = event.values[SensorManager.AXIS_Z];

                /*if(Round(x,4)>5.0000){
                    Log.d("sensor", "X Right axis: " + x);
                    //Toast.makeText(this, "Right shake detected", Toast.LENGTH_SHORT).show();
                }
                else if(Round(x,4)<-5.0000){
                    Log.d("sensor", "X Left axis: " + x);
                    // Toast.makeText(this, "Left shake detected", Toast.LENGTH_SHORT).show();
                }*/

                //float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
                float speed = (x - last_x) / diffTime * 10000;


                // Log.d("sensor", "diff: " + diffTime + " - speed: " + speed);
                if (speed > SHAKE_THRESHOLD) {
                    Log.d("sensor", "great speed Right" + speed);
                    speadRight=true;
                    timeRight = System.currentTimeMillis();
                }

                if ( -speed > SHAKE_THRESHOLD) {
                    Log.d("sensor", "great speed Left" + speed);
                    elapseRight = (curTime - timeRight);
                    if ( elapseRight<1000){
                        if ( speadRight==true) {
                            letOpen = true;
                            Log.d("sensor", "let OPEN !!!!" + speed);

                            long[] vibrationPattern = {0, 1000, 50, 50};
                            final int indexInPatternToRepeat = -1; //-1 - don't repeat
                            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
                        }
                    }else{
                        speadRight=false;
                    }
                }


                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    public static float Round(float Rval, int Rpl) {
        float p = (float)Math.pow(10,Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float)tmp/p;
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "onBeaconServiceConnect");
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {

                    final double distance= beacons.iterator().next().getDistance() * correctionFactor;


                    Log.i(TAG, " " + String.format("The first beacon I see is about : %.2f", distance) + " meters away.");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            apribile=true;
                            mTextView.setText(String.format( "(invista)Distanza : %.2f", distance ) );
                            //proximityBar.setProgress((int) (Math.round((maxDist - distance) * 100 / maxDist)))  ;
                            //proximityBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        }
                    });



                    // get selected radio button from radioGroup
                    //int selectedId = radiogroup.getCheckedRadioButtonId();

                    // find the radiobutton by returned id
                    //selectedradiogroup = (RadioButton) findViewById(selectedId);
                    minDistToOppen=  5.0;


                    if ((distance < minDistToOppen) && ((SystemClock.uptimeMillis()-previousOpenedTime)>minPeriodBeetwenOpen)){
                        Log.i(TAG, "The first beacon I see is about "+distance+" meters away."+(SystemClock.uptimeMillis()-previousOpenedTime));
                        previousOpenedTime=SystemClock.uptimeMillis(); // to avoid too fast opened ratio
                        long[] vibrationPattern = {0, 500, 50, 300};
                        //-1 - don't repeat
                        final int indexInPatternToRepeat = -1;
                        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
                        BTMain btMain=new BTMain();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText(String.format("(inarea)Distanza : %.2f  Invio comando", distance));
                                //proximityBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                            }
                        });

                        //btMain.startCommunication();
                    }

                }
            }
        });



        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

                Log.i(TAG, "I just saw an beacon for the first time!");

// Build intent for notification content

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //if (!area.isChecked()){ area.toggle();}
                        mTextView.setText("Avvistata radioboa");
                        apribile=true;
                    }
                });
                //try {
                //beaconManager.startRangingBeaconsInRegion(region);
                //} catch (RemoteException e) {    }
            }

            @Override
            public void didExitRegion(Region region) {
                apribile=false;
                Log.i(TAG, "I no longer see an beacon");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //if (area.isChecked()){ area.toggle();}
                        mTextView.setText("Non vedo radioboa");
                    }
                });
            }

            @Override
            public void didDetermineStateForRegion(final int state, Region region) {
                if (state==0){
                    apribile=false;
                }else{
                    apribile=true;
                }
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText("cambio stato in :" + state);
                    }
                });
            }
        });

        try {

            //beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.startRangingBeaconsInRegion(new Region("com.abajeli.aa_apricancelloautomatico.boostrapRegion",
                    Identifier.parse("f8869d30-e2e4-11e4-b571-0800200c9a66"), Identifier.parse("1"), Identifier.parse("1")));
        } catch (RemoteException e) {    }

        /*try {
            //beaconManager.startMonitoringBeaconsInRegion(new Region("f8869d30-e2e4-11e4-b571-0800200c9a66", null, null, null));
            beaconManager.startMonitoringBeaconsInRegion(new Region("com.abajeli.aa_apricancelloautomatico.boostrapRegion",
                    Identifier.parse("f8869d30-e2e4-11e4-b571-0800200c9a66"), Identifier.parse("1"), Identifier.parse("1")));
        } catch (RemoteException e) {    }*/
    }



    @Override
    protected void onResume() {
        super.onResume();
        sensorMgr.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }


    static Context mContext;
    static Vibrator vibrator;
    Sensor mLight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

        /*boolean accelSupported = sensorMgr.registerListener(this,
                sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);*/



        mLight = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);




        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mContext=this;
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        //if (beaconManager==null){
            beaconManager = BeaconManager.getInstanceForApplication(this);

            beaconManager.bind(this);
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); // setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
            //beaconManager.setForegroundScanPeriod(30000);
            beaconManager.setBackgroundBetweenScanPeriod(3000);
            beaconManager.setForegroundBetweenScanPeriod(3000);
            beaconManager.setBackgroundScanPeriod(2000);
            beaconManager.setForegroundScanPeriod(2000);


            beaconManager.setDebug(true);
        //}


    }



    public static void parla(String txt){
        //ttobj.speak(txt, TextToSpeech.QUEUE_FLUSH, null);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            long[] vibrationPattern = {0, 100, 0, 0};
            //-1 - don't repeat
            final int indexInPatternToRepeat = -1;
            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);


            //Ringtone r = RingtoneManager.getRingtone(baseContext, notification);
            //r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }



        Intent viewIntent = new Intent(mContext, NotificationActivity.class);
        viewIntent.putExtra(EXTRA_EVENT_ID, NOTIFICATION_ID);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(mContext, 0, viewIntent, 0);

// Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setHintHideIcon(true);
        //.setBackground(mBitmap);

// Create a NotificationCompat.Builder to build a standard notification
// then extend it with the WearableExtender
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        //.setSmallIcon(R.drawable.ic_event)
                        .setContentTitle("eventTitle")
                        .setContentText("eventLocation")
                        .extend(wearableExtender)
                        .setContentIntent(viewPendingIntent);



                /*

                Notification notif = new NotificationCompat.Builder(mContext)
                        .setContentTitle("New mail from " + sender)
                        .setContentText(subject)
                        .setSmallIcon(R.drawable.new_mail)
                        .extend(wearableExtender)
                        .build();
*/


        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);

// Issue the notification with notification manager.
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());





    }
}
