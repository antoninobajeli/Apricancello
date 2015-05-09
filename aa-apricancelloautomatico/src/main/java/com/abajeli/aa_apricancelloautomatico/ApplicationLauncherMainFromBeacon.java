package com.abajeli.aa_apricancelloautomatico;

/**
 * Created by abajeli on 23/04/15.
 */
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.Region;

public class ApplicationLauncherMainFromBeacon extends Application implements BootstrapNotifier {
    private static final String TAG = ".ApplicationLauncherMainFromBeacon";
    private RegionBootstrap regionBootstrap;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App started up");
        // wake up the app when any beacon is seen (you can specify specific id filers in the parameters below)
        //Region region = new Region("com.abajeli.aa_apricancelloautomatico.boostrapRegion", null, null, null);
        //regionBootstrap = new RegionBootstrap(this, region);

        /*Region region = new Region("com.abajeli.aa_apricancelloautomatico.boostrapRegion", null, null, null);*/
        Region region = new Region("com.abajeli.aa_apricancelloautomatico.boostrapRegion",
                Identifier.parse("f8869d30-e2e4-11e4-b571-0800200c9a66"), Identifier.parse("1"), Identifier.parse("1"));
        regionBootstrap = new RegionBootstrap(this, region);




    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
        Log.d(TAG, "didDetermineStateForRegion");
    }

    @Override
    public void didEnterRegion(Region arg0) {
        Log.d(TAG, "Got a didEnterRegion call");
        // This call to disable will make it so the activity below only gets launched the first time a beacon is seen (until the next time the app is launched)
        // if you want the Activity to launch every single time beacons come into view, remove this call.
        regionBootstrap.disable();
        Intent intent = new Intent(this, MainActivity.class);
        // IMPORTANT: in the AndroidManifest.xml definition of this activity, you must set android:launchMode="singleInstance" or you will get two instances
        // created when a user launches the activity manually and it gets launched from here.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @Override
    public void didExitRegion(Region arg0) {
        // Don't care
    }
}