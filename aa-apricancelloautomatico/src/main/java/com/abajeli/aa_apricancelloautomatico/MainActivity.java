package com.abajeli.aa_apricancelloautomatico;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Locale;
import java.util.TimerTask;

import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.AudioFormat;

import com.abajeli.aa_apricancelloautomatico.BT.BTMain;

public class MainActivity extends ActionBarActivity
        implements  LocationListener, TextToSpeech.OnInitListener, BeaconConsumer {

        protected static final String TAG = "RangingActivity";
        //private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    private static final int CONNECTION_TIMEOUT=5000;
    private static final int SO_TIMEOUT=7000;

    private static final long POINT_RADIUS = 100; // in Meters
    private static final double gateLatitude=37.541245;
    private static final double gateLongitude=15.106205;
    private static Location gateLocation=null;


    private static final double mCurrLat=0;
    private static final double mCurrLong=0;

    LocationManager locationManager;
    static Context ctx;
    public static Context baseContext;
    static TextToSpeech ttobj;
    static TextView lastUpdate;
    //static BTMain btMain=new BTMain();

    public static Activity activity;

    Button mButtonCommuta;
    public static final int OUT_OF_SERVICE = 0;
    public static final int TEMPORARILY_UNAVAILABLE = 1;
    public static final int AVAILABLE = 2;


    private static LocationListener  locationListener;

    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandlerRepetingTask;





    private BeaconManager beaconManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    long previousOpenedTime=0;
    double minDistToOppen=0.05;
    long minPeriodBeetwenOpen=4000;
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    double distance= beacons.iterator().next().getDistance();
                    Log.i(TAG, "The first beacon I see is about "+distance+" meters away.");

                    if ((distance < minDistToOppen) && ((SystemClock.uptimeMillis()-previousOpenedTime)>minPeriodBeetwenOpen)){
                        Log.i(TAG, "The first beacon I see is about "+distance+" meters away."+(SystemClock.uptimeMillis()-previousOpenedTime));
                        previousOpenedTime=SystemClock.uptimeMillis(); // to avoid too fast opened ratio
                        BTMain btMain=new BTMain();
                        //btMain.startCommunication();
                    }

                }
            }
        });



        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

                Log.i(TAG, "I just saw an beacon for the first time!");
                //try {
                //beaconManager.startRangingBeaconsInRegion(region);
                //} catch (RemoteException e) {    }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
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
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = ttobj.setLanguage(Locale.ITALIAN);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut();
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }


    private void speakOut() {
        System.out.println("Speaking");
        ttobj.speak("Animal", TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseContext=this.getApplication().getBaseContext();
/*
        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.ITALIAN);
                        }
                    }
                });*/

        setContentView(R.layout.activity_monitor);
        //iBeaconManager.bind(this);



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


    protected void onCreateBck(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ctx=this.getApplicationContext();
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


        gateLocation=new Location("FIXGATE");
        gateLocation.setLatitude(gateLatitude);
        gateLocation.setLongitude(gateLongitude);

        activity=this;



        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.ITALIAN);
                        }
                    }
                });


        // new for location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //for debugging...
        // allow notification on the MainActivity_old
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);


        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        boolean enabledGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabledGps) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        boolean enabledNet = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!enabledNet) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        Criteria myCriteria = new Criteria();
        myCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        myCriteria.setPowerRequirement(Criteria.POWER_LOW);
        // let Android select the right location provider for you
        String myProvider = locationManager.getBestProvider(myCriteria, true);
        Toast.makeText(this, "Current provider :: "+myProvider, Toast.LENGTH_SHORT).show();

        //i'm defining and registering the receiver for the notification
        String ACTION_FILTER = "com.bajeli.centralino.";
        registerReceiver(new LocationReceiver(), new IntentFilter(ACTION_FILTER));


        //Setting up My Broadcast Intent
        Intent i = new Intent(ACTION_FILTER);
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), -1, i, 0);


        // setting up locationManager in orther to FIRE the PendingIntent
        locationManager.addProximityAlert(gateLatitude, gateLongitude, POINT_RADIUS, -1, pi);



        // finally require updates at -at least- the desired rate
        long minTimeMillis = 5000; // 600,000 milliseconds make 10 minutes
        locationManager.requestLocationUpdates(myProvider,minTimeMillis,10,this);







        locationListener = new MyLocationListener();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 35000, 10, this.locationListener);

        mHandlerRepetingTask = new Handler();

    }






    private static int ciclicPosition=0;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {

            //TODO mock positions
            switch(ciclicPosition){
                case 0:
                    mSetMockLocation(locationManager, 37.54, 15.10, 20);
                    break;
                case 1:
                    mSetMockLocation(locationManager, 37.6, 15.11, 20);
                    break;
                case 2:
                    mSetMockLocation(locationManager, 37.541245, 15.106205, 20);
                    break;
                case 3:
                    mSetMockLocation(locationManager, 37.54124, 15.10620, 20);
                    break;
                case 4:
                    mSetMockLocation(locationManager, 37.5412, 15.1062, 20);
                    break;
                case 5:
                    mSetMockLocation(locationManager, 37.541, 15.106, 20);
                    break;
                case 6:
                    mSetMockLocation(locationManager, 37.5, 15.1, 20);
                    break;
                default:
                    ciclicPosition=-1;
                    break;
            }
            ciclicPosition++;
            mHandlerRepetingTask.postDelayed(mStatusChecker, mInterval);
        }
    };






/*
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an iBeacon for the firt time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing iBeacons: "+state);
            }
        });

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    Log.i(TAG, "The first iBeacon I see is about "+iBeacons.iterator().next().getAccuracy()+" meters away.");

                }
            }
        });

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("f8869d30-e2e4-11e4-b571-0800200c9a66", null, 1, 1));
        } catch (RemoteException e) {   }
    }


*/



    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandlerRepetingTask.removeCallbacks(mStatusChecker);
    }





    public long lastprovidertimestamp=0;
    private Location lastLocation;





    /**
     * try to get the 'best' location selected from all providers
     */

    /*
    private Location getBestLocation() {
        String TAG="getBestLocation";
        Location gpslocation = getLocationByProvider(LocationManager.GPS_PROVIDER);
        Location networkLocation =
                getLocationByProvider(LocationManager.NETWORK_PROVIDER);
        // if we have only one location available, the choice is easy
        if (gpslocation == null) {
            Log.d(TAG, "No GPS Location available.");
            return networkLocation;
        }
        if (networkLocation == null) {
            Log.d(TAG, "No Network Location available");
            return gpslocation;
        }

        // a locationupdate is considered 'old' if its older than the configured
        // update interval. this means, we didn't get a
        // update from this provider since the last check
        long old = System.currentTimeMillis() - getGPSCheckMilliSecsFromPrefs();
        boolean gpsIsOld = (gpslocation.getTime() < old);
        boolean networkIsOld = (networkLocation.getTime() < old);
        // gps is current and available, gps is better than network
        if (!gpsIsOld) {
            Log.d(TAG, "Returning current GPS Location");
            return gpslocation;
        }
        // gps is old, we can't trust it. use network location
        if (!networkIsOld) {
            Log.d(TAG, "GPS is old, Network is current, returning network");
            return networkLocation;
        }
        // both are old return the newer of those two
        if (gpslocation.getTime() > networkLocation.getTime()) {
            Log.d(TAG, "Both are old, returning gps(newer)");
            return gpslocation;
        } else {
            Log.d(TAG, "Both are old, returning network(newer)");
            return networkLocation;
        }


    }*/


/*
    public void startRecording() {
        String TAG="startRecording";
        gpsTimer.cancel();
        gpsTimer = new Timer();
        long checkInterval = getGPSCheckMilliSecsFromPrefs();
        long minDistance = getMinDistanceFromPrefs();
        // receive updates
        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        for (String s : locationManager.getAllProviders()) {
            locationManager.requestLocationUpdates(s, checkInterval,
                    minDistance, new LocationListener() {

                        @Override
                        public void onStatusChanged(String provider,
                                                    int status, Bundle extras) {}

                        @Override
                        public void onProviderEnabled(String provider) {}

                        @Override
                        public void onProviderDisabled(String provider) {}

                        @Override
                        public void onLocationChanged(Location location) {
                            // if this is a gps location, we can use it
                            if (location.getProvider().equals(
                                    LocationManager.GPS_PROVIDER)) {
                                doLocationUpdate(location, true);
                            }
                        }
                    });
            // //Toast.makeText(this, "GPS Service STARTED",
            // Toast.LENGTH_LONG).show();
            gps_recorder_running = true;
        }
        // start the gps receiver thread
        gpsTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Location location = getBestLocation();
                doLocationUpdate(location, false);
            }
        }, 0, checkInterval);
    }
*/


    public void doLocationUpdate(Location l, boolean force) {
        String TAG="doLocationUpdate";
        long minDistance = 10;
        Log.d(TAG, "update received:" + l);
        if (l == null) {
            Log.d(TAG, "Empty location");
            if (force)
                Toast.makeText(this, "Current location not available",
                        Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastLocation != null) {
            float distance = l.distanceTo(lastLocation);
            Log.d(TAG, "Distance to last: " + distance);
            if (l.distanceTo(lastLocation) < minDistance && !force) {
                Log.d(TAG, "Position didn't change");
                return;
            }
            if (l.getAccuracy() >= lastLocation.getAccuracy()
                    && l.distanceTo(lastLocation) < l.getAccuracy() && !force) {
                Log.d(TAG,
                        "Accuracy got worse and we are still "
                                + "within the accuracy range.. Not updating");
                return;
            }
            if (l.getTime() <= lastprovidertimestamp && !force) {
                Log.d(TAG, "Timestamp not never than last");
                return;
            }
        }
        // upload/store your location here
    }



    /**
     * get the last known location from a specific provider (network/gps)
     */
    private Location getLocationByProvider(String provider) {
        String TAG="getLocationByProvider";
        Location location = null;
        /*if (!isProviderSupported(provider)) {
            return null;
        }*/
        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        try {
            if (locationManager.isProviderEnabled(provider)) {
                location = locationManager.getLastKnownLocation(provider);
            }
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Cannot access Provider " + provider);
        }
        return location;
    }








    public void doOpenGateThroughInternet(){
        new OpenGateThroughInternet().execute("");
    };

    public class OpenGateThroughInternet extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Long startTime,communicationStartTime = null;
            startTime = System.currentTimeMillis();
            String operationResult="Failed";

            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT); // in ms
            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);// in ms

            HttpGet httpget = new HttpGet("http://www.bajeli.com/app/gateopener/setForOpen.php?requester=bajeli");
            // Depends on your web service
            httpget.setHeader("Content-type", "application/json");

            InputStream inputStream = null;
            String msg_res="";
            String result = null;
            try {
                communicationStartTime = System.currentTimeMillis();
                HttpResponse response = httpclient.execute(httpget);
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
                msg_res="CommandSent ";
            }
            catch(SocketTimeoutException se) {
                Long endTime = System.currentTimeMillis();
                System.out.println("SocketTimeoutException :: time elapsed :: " + (endTime-communicationStartTime));
                se.printStackTrace();
                msg_res="SocketTimeout ";
            }
            catch(ConnectTimeoutException cte) {
                Long endTime = System.currentTimeMillis();
                System.out.println("ConnectTimeoutException :: time elapsed :: " + (endTime-communicationStartTime));
                cte.printStackTrace();
                msg_res="ConnectTimeout ";
            } catch (Exception e) {
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


            Long endTime = System.currentTimeMillis();
            System.out.println("RefreshOperation :: time elapsed :: " + (endTime-startTime));
            aggiornna_dati(msg_res + "  " + (endTime - startTime));
            return operationResult;
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }





    public static void openGate(){
        //btMain.startCommunication();
    }





    public static void parla(String txt){
        //ttobj.speak(txt, TextToSpeech.QUEUE_FLUSH, null);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(baseContext, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    public void aggiornna_dati(final String s){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (lastUpdate!=null)
                lastUpdate.setText(s);

            }
        });
    }


    @Override
    public void onResume(){
        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.ITALIAN);
                        }
                    }
                });
        super.onPause();
    }

    @Override
    public void onPause(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //TODO
    public static void activeArea(boolean b){
        if (ctx!=null){
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(ctx, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    double distance=0;
    @Override
    public void onLocationChanged(Location newLocation) {

        //if (lastLocation!=null)
        distance = newLocation.distanceTo(gateLocation);
        lastLocation=newLocation;
        Log.i("onLocationChanged", "Distance: " + distance);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tvLat!=null)
                    tvLat.setText(""+lastLocation.getLatitude());
                if (tvLong!=null)
                    tvLong.setText(""+lastLocation.getLongitude());
                if (tvDist!=null)
                    tvDist.setText(""+distance);

            }
        });
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }



    private void mSetMockLocation(LocationManager lm,double latitude, double longitude, float accuracy) {
        lm.addTestProvider (LocationManager.GPS_PROVIDER,
                "requiresNetwork" == "",
                "requiresSatellite" == "",
                "requiresCell" == "",
                "hasMonetaryCost" == "",
                "supportsAltitude" == "",
                "supportsSpeed" == "",
                "supportsBearing" == "",
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);

        Location newLocation = new Location(LocationManager.GPS_PROVIDER);

        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setAccuracy(accuracy);
        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        newLocation.setTime(SystemClock.elapsedRealtimeNanos());

        lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        lm.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null,System.currentTimeMillis());


        try {
            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    private void sendEmail(){

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, "antonino.bajeli@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Current position");
        if ((tvLat!=null)&&(tvLong!=null)){
            intent.putExtra(Intent.EXTRA_TEXT, "Latitude="+tvLat.getText() +"Longitude="+tvLong.getText());
        }


        startActivity(Intent.createChooser(intent, "Send Email"));
    }





    //private final double freqOfTone = 440; // hz

    private EditText tfTone;

    byte[] genToneold(double freqOfTone,int sampleRate){
        final double duration = 1; // seconds
        //final int sampleRate = 40000;
        final int numSamples =(int)(duration * sampleRate);
        double sample[]= new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];

        for (int i = 0; i < numSamples-1; ++i) {      // Fill the sample array
            sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
        }

/*
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }*/

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
        return generatedSnd;
    }


    byte[] genTone(){
        final double duration = 1; // seconds
        //final int sampleRate = 40000;
        final int numSamples =(int)(duration * 48000);
        double sample[]= new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];


        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        while(idx<2 * numSamples){
            // scale to maximum amplitude
            short val = (short) (( 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

            val = (short) (( 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
/*
           val = (short) (( 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
   */
            val = (short) (( -32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

            val = (short) (( -32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);


/*

            val = (short) (( 0));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);*/


        }


        return generatedSnd;
    }


    private void sendToneCode(double freqOfTone,int sampleRate){

        byte generatedSnd[] = genTone();

        int minSize =AudioTrack.getMinBufferSize( sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT );
        Log.i("audio","minbuffSize="+minSize);
        Log.i("audio","generatedSnd="+generatedSnd.length);
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }




    TextView tvLat,tvLong,tvDist;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }
        EditText etTone;

        //final Runnable beeper = new Runnable() {
         //   public void run() {
                //((MainActivity) activity).sendToneCode(19000, 38000);

                //((MainActivity) activity).sendToneCode(20000, 40000);

                //((MainActivity) activity).sendToneCode(15000, 30000);
         //   }
         //   };


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            lastUpdate= (TextView) rootView.findViewById(R.id.lastOperation);

            Button buttonCommuta= (Button) rootView.findViewById(R.id.button1);
            buttonCommuta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) activity).doOpenGateThroughInternet();
                }
            });


            Button sendEmail= (Button) rootView.findViewById(R.id.buttonSendEmail);
            sendEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) activity).sendEmail();
                }
            });


            Button sendETone= (Button) rootView.findViewById(R.id.buttonSendToneCode);
            etTone=(EditText) rootView.findViewById(R.id.tfTone);
            sendETone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //((MainActivity) activity).sendToneCode(15000, 20000);// PAZZESCO
                    ((MainActivity) activity).sendToneCode(19000, 48000);
                    //ToneGenerator toneGenerator;
                    //new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                }
            });


            Button sendBTCommand= (Button) rootView.findViewById(R.id.btnBTOpen);
            sendBTCommand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    BTMain btMain=new BTMain();

                }
            });

            ToggleButton buttonStrtStop= (ToggleButton) rootView.findViewById(R.id.buttonStartStopFakePos);
            buttonStrtStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    //toggle.toggle();
                    boolean on = ((ToggleButton) v).isChecked();

                    if (on) {
                        ((MainActivity) activity).startRepeatingTask();
                    } else {
                        ((MainActivity) activity).stopRepeatingTask();
                    }
                }
            });


            ((MainActivity) activity).tvLat = (TextView) rootView.findViewById(R.id.currentPositionLat);
            ((MainActivity) activity).tvLong = (TextView) rootView.findViewById(R.id.currentPositionLong);
            ((MainActivity) activity).tvDist = (TextView) rootView.findViewById(R.id.distance);

            return rootView;
        }
    }
}
