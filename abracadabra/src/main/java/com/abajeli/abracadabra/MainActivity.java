package com.abajeli.abracadabra;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
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


public class MainActivity extends ActionBarActivity implements BeaconConsumer {


    protected static final String TAG = "Main";

    public static Context baseContext;
    private BeaconManager beaconManager;


    long previousOpenedTime=0;
    double minDistToOppen=0.5;
    long minPeriodBeetwenOpen=4000;
    long maxDist=25;
    double correctionFactor=4.0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    RadioButton selectedradiogroup;
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {

                    final double distance= beacons.iterator().next().getDistance() * correctionFactor;


                    Log.i(TAG, " " + String.format( "The first beacon I see is about : %.2f", distance )  + " meters away.");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            distView.setText(String.format( "Distanza : %.2f", distance ) );
                            proximityBar.setProgress((int) (Math.round((maxDist - distance) * 100 / maxDist)))  ;
                            proximityBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        }
                    });



                    // get selected radio button from radioGroup
                    int selectedId = radiogroup.getCheckedRadioButtonId();

                    // find the radiobutton by returned id
                    selectedradiogroup = (RadioButton) findViewById(selectedId);
                    minDistToOppen=  Double.parseDouble( selectedradiogroup.getText().toString());


                    if ((distance < minDistToOppen) && ((SystemClock.uptimeMillis()-previousOpenedTime)>minPeriodBeetwenOpen)){
                        Log.i(TAG, "The first beacon I see is about "+distance+" meters away."+(SystemClock.uptimeMillis()-previousOpenedTime));
                        previousOpenedTime=SystemClock.uptimeMillis(); // to avoid too fast opened ratio
                        BTMain btMain=new BTMain();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                distView.setText(String.format("Distanza : %.2f  Invio comando", distance));
                                proximityBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
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
                distView.setText("Avvistata radioboa");
                Log.i(TAG, "I just saw an beacon for the first time!");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!area.isChecked()){ area.toggle();}

                    }
                });
                //try {
                //beaconManager.startRangingBeaconsInRegion(region);
                //} catch (RemoteException e) {    }
            }

            @Override
            public void didExitRegion(Region region) {
                distView.setText("non vedo piu' la radioboa");

                Log.i(TAG, "I no longer see an beacon");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (area.isChecked()){ area.toggle();}

                    }
                });
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                distView.setText("cambiamento di stato");
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





    public static void parla(String txt){
        //ttobj.speak(txt, TextToSpeech.QUEUE_FLUSH, null);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Ringtone r = RingtoneManager.getRingtone(baseContext, notification);
            //r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }




    }




    ProgressBar proximityBar;
    TextView distView;
    Switch area;
    Button buttonOpen;
    RadioGroup radiogroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        radiogroup= (RadioGroup) findViewById(R.id.radiogroup);

        proximityBar= (ProgressBar) findViewById(R.id.progressBar);
        proximityBar.setProgress(85);



        distView = (TextView) findViewById(R.id.distView);

        area = (Switch)  findViewById(R.id.area);
        area.setEnabled(false);

        buttonOpen = (Button)findViewById(R.id.buttonOpen);
        buttonOpen.setOnClickListener(new View.OnClickListener() {
          @Override
           public void onClick(View v) {
              BTMain btMain=new BTMain();
          }
         });




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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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
}
