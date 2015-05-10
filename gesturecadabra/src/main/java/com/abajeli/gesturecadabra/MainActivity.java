package com.abajeli.gesturecadabra;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.abajeli.gesturecadabra.fragments.CounterFragment;
import com.abajeli.gesturecadabra.fragments.SettingsFragment;
import com.abajeli.gesturecadabra.fragments.SettingsFragment2;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity
        implements SensorEventListener,BeaconConsumer {

    private static TextView mTextView;
    private static final String TAG = "gcMainActivity";

    private static final long[] VBR_PATT_SENDING_CMD = {0, 200, 0, 0};
    private static final long[] VBR_PATT_OK_CMD = {150, 0, 150, 0};

    /** How long to keep the screen on when no activity is happening **/
    private static final long SCREEN_ON_TIMEOUT_MS = 20000; // in milliseconds

    /** an up-down movement that takes more than this will not be registered as such **/
    private static final long TIME_THRESHOLD_NS = 2000000000; // in nanoseconds (= 2sec)

    /**
     * Earth gravity is around 9.8 m/s^2 but user may not completely direct his/her hand vertical
     * during the exercise so we leave some room. Basically if the x-component of gravity, as
     * measured by the Gravity sensor, changes with a variation (delta) > GRAVITY_THRESHOLD,
     * we consider that a successful count.
     */
    private static final float GRAVITY_THRESHOLD = 7.0f;


    /**
     * Beacon commands settings
     *
     */
    private static final double correctionFactor=4.0;
    private static double minDistToOppen=0.5; // can be chenged by user
    private static long mPreviousOpenedTime=0;
    private static final long minPeriodBeetwenOpen=15000;
    private static boolean mAutoOpen =false;
    private static Activity mCurrAct;


    private boolean mApribile=false;


    private BeaconManager beaconManager;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long mLastTime = 0;
    private boolean mUp = false;
    private int mJumpCounter = 0;
    private ViewPager mPager;
    private CounterFragment mCounterPage;
    private SettingsFragment mSettingPage;
    private SettingsFragment2 mSettingPage2;
    private ImageView mSecondIndicator;
    private ImageView mFirstIndicator;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, " On create ");
        setContentView(R.layout.jj_layout);
        setupViews();
        mHandler = new Handler();
        mJumpCounter = Utils.getCounterFromPreference(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //renewTimer();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);



        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); // setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //beaconManager.setForegroundScanPeriod(30000);
        beaconManager.setBackgroundBetweenScanPeriod(3000);
        beaconManager.setForegroundBetweenScanPeriod(3000);
        beaconManager.setBackgroundScanPeriod(2000);
        beaconManager.setForegroundScanPeriod(2000);


        beaconManager.setDebug(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Successfully registered for the sensor updates");
            }
        }
    }



    /**
     * This will save battery
     */
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Unregistered for sensor events");
        }
    }
    private void setupViews() {
        Log.i(TAG, "setupViews ");
        mPager = (ViewPager) findViewById(R.id.pager);
        mFirstIndicator = (ImageView) findViewById(R.id.indicator_0);
        mSecondIndicator = (ImageView) findViewById(R.id.indicator_1);
        final PagerAdapter adapter = new PagerAdapter(getFragmentManager());
        mCounterPage = new CounterFragment();
        mSettingPage = new SettingsFragment();
        mSettingPage2 = new SettingsFragment2();
        adapter.addFragment(mCounterPage);
        adapter.addFragment(mSettingPage);
        adapter.addFragment(mSettingPage2);
        setIndicator(0);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                setIndicator(i);
                //renewTimer();
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mPager.setAdapter(adapter);
    }





    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "onBeaconServiceConnect");
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {

                    final double distance = beacons.iterator().next().getDistance() * correctionFactor;


                    Log.i(TAG, " " + String.format("The first beacon I see is about : %.2f", distance) + " meters away.");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mApribile = true;
                            //mTextView.setText(String.format("(invista)Distanza : %.2f", distance));
                            mCounterPage.setDistance(String.format("(invista) : %.2f", distance));
                            //proximityBar.setProgress((int) (Math.round((maxDist - distance) * 100 / maxDist)))  ;
                            //proximityBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        }
                    });


                    // get selected radio button from radioGroup
                    //int selectedId = radiogroup.getCheckedRadioButtonId();

                    // find the radiobutton by returned id
                    //selectedradiogroup = (RadioButton) findViewById(selectedId);
                    minDistToOppen = 5.0;

                    if ((distance < minDistToOppen) && ((SystemClock.uptimeMillis() - mPreviousOpenedTime) > minPeriodBeetwenOpen)) {
                        if (mAutoOpen) {
                            Log.i(TAG, "The first beacon I see is about " + distance + " meters away." + (SystemClock.uptimeMillis() - mPreviousOpenedTime));
                            mPreviousOpenedTime = SystemClock.uptimeMillis(); // to avoid too fast opened ratio
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //
                                    //mTextView.setText(String.format("(inarea)Distanza : %.2f  Invio comando", distance));
                                    //proximityBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                                }
                            });

                            sendOpen();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mCounterPage.setSpecialMex("Should open? Automode not set!!");
                                }
                            });
                        }
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
                        //mTextView.setText("Avvistata radioboa");
                        mCounterPage.setSpecialMex("Avvistata radioboa");
                        mApribile=true;
                    }
                });
                //try {
                //beaconManager.startRangingBeaconsInRegion(region);
                //} catch (RemoteException e) {    }
            }

            @Override
            public void didExitRegion(Region region) {
                mApribile=false;
                Log.i(TAG, "I no longer see an beacon");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //if (area.isChecked()){ area.toggle();}
                        //mTextView.setText("Non vedo radioboa");
                        mCounterPage.setDistance("Non vedo radioboa");
                    }
                });
            }

            @Override
            public void didDetermineStateForRegion(final int state, Region region) {
                if (state==0){
                    mApribile=false;
                }else{
                    mApribile=true;
                }
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mTextView.setText("cambio stato in :" + state);
                        mCounterPage.setSpecialMex("cambio stato in :" + state);

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



    public void sendOpen(){
        final int indexInPatternToRepeat = -1;

        runOnUiThread(new Runnable() {
            public void run() {

                mCounterPage.setLayBackgroundColorGreen();
            }
        });

        Utils.vibratePattern(this, VBR_PATT_SENDING_CMD, indexInPatternToRepeat);
        BTMain btMain = new BTMain(this);
        btMain.startCommunication();

    }

    public void enableAutoOpen(boolean status){
        mAutoOpen=status;
    }


        @Override
    public void onSensorChanged(SensorEvent event) {
            detectJump(event.values[0], event.timestamp);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * A simple algorithm to detect a successful up-down movement of hand(s). The algorithm is
     * based on the assumption that when a person is wearing the watch, the x-component of gravity
     * as measured by the Gravity Sensor is +9.8 when the hand is downward and -9.8 when the hand
     * is upward (signs are reversed if the watch is worn on the right hand). Since the upward or
     * downward may not be completely accurate, we leave some room and instead of 9.8, we use
     * GRAVITY_THRESHOLD. We also consider the up <-> down movement successful if it takes less than
     * TIME_THRESHOLD_NS.
     */
    private void detectJump(float xValue, long timestamp) {
        if ((Math.abs(xValue) > GRAVITY_THRESHOLD)) {
            if(timestamp - mLastTime < TIME_THRESHOLD_NS ) {
                // preserve too slow gesture
                if (mUp != (xValue > 0))
                    onOpenDirectiveDetected(!mUp);
            }else{

            }
            mUp = xValue > 0;
            mLastTime = timestamp;
        }
    }



    /**
     * Called on detection of a successful down -> up or up -> down movement of hand.
     */
    private void onOpenDirectiveDetected(boolean up) {
        // we only count a pair of up and down as one successful movement
        if (up) {
            Log.i(TAG,"Excaping onOpenDirectiveDetected");
            return;
        }
        //mJumpCounter++;
        //setCounter(mJumpCounter);
        //renewTimer();

        if ((SystemClock.uptimeMillis() - mPreviousOpenedTime) > minPeriodBeetwenOpen){
            mPreviousOpenedTime=SystemClock.uptimeMillis();
            Log.i(TAG,"onOpenDirectiveDetected");
            mCounterPage.setSpecialMex("onOpenDirectiveDetected !!");
            sendOpen();

        }else{
            mCounterPage.setSpecialMex("...this time I pass !!");
            Log.i(TAG,"last open has been sent too recent time ...this time I pass !!");
        }


    }



    /**
     * Updates the counter on UI, saves it to preferences and vibrates the watch when counter
     * reaches a multiple of 10.
     */
    private void setCounter(int i) {
        mCounterPage.setCounter(i);
        Utils.saveCounterToPreference(this, i);
        if (i > 0 && i % 10 == 0) {
            Utils.vibrate(this, 0);
        }
    }



    public void resetCounter() {
        setCounter(0);
        //renewTimer();
    }

    public void commandProcessed(boolean success){
        if (success==true){
            /**
             * Incremento il contatore solo se l'erito e' stato positivo
             */

            mJumpCounter++;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCounter(mJumpCounter);
                    mCounterPage.setSpecialMex("Job done !!");
                    mCounterPage.setLayBackgroundColorWhite( );
                }
            });

            //parla("Il servo ha eseguito");
            final int indexInPatternToRepeat = -1;
            Utils.vibratePattern(this, VBR_PATT_OK_CMD, indexInPatternToRepeat);
        }

    }





    public void parla(String txt){
        //ttobj.speak(txt, TextToSpeech.QUEUE_FLUSH, null);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            final int indexInPatternToRepeat = -1;
            Utils.vibratePattern(this, VBR_PATT_OK_CMD, indexInPatternToRepeat);

            //Ringtone r = RingtoneManager.getRingtone(baseContext, notification);
            //r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }




/*
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






        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);

// Issue the notification with notification manager.
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        */



    }







    /**
     * Starts a timer to clear the flag FLAG_KEEP_SCREEN_ON.
     */
    /*
    private void renewTimer() {
        if (null != mTimer) {
            mTimer.cancel();
        }
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG,
                            "Removing the FLAG_KEEP_SCREEN_ON flag to allow going to background");
                }
                resetFlag();
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, SCREEN_ON_TIMEOUT_MS);
    }
*/

    /**
     * Resets the FLAG_KEEP_SCREEN_ON flag so activity can go into background.
     */
    /*
    private void resetFlag() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Resetting FLAG_KEEP_SCREEN_ON flag to allow going to background");
                }
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                finish();
            }
        });
    }*/

    /**
     * Sets the page indicator for the ViewPager.
     */
    private void setIndicator(int i) {
        switch (i) {
            case 0:
                mFirstIndicator.setImageResource(R.drawable.full_10);
                mSecondIndicator.setImageResource(R.drawable.empty_10);
                break;
            case 1:
                mFirstIndicator.setImageResource(R.drawable.empty_10);
                mSecondIndicator.setImageResource(R.drawable.full_10);
                break;
            case 2:
                mFirstIndicator.setImageResource(R.drawable.empty_10);
                mSecondIndicator.setImageResource(R.drawable.full_10);
                break;
        }
    }






}
