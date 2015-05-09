package com.abajeli.aa_apricancelloautomatico;

/**
 * Created by abajeli on 14/04/15.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.abajeli.aa_apricancelloautomatico.ibeacon.IBeacon;
import com.abajeli.aa_apricancelloautomatico.ibeacon.IBeaconConsumer;
import com.abajeli.aa_apricancelloautomatico.ibeacon.IBeaconManager;
import com.abajeli.aa_apricancelloautomatico.ibeacon.MonitorNotifier;
import com.abajeli.aa_apricancelloautomatico.ibeacon.RangeNotifier;
import com.abajeli.aa_apricancelloautomatico.ibeacon.Region;

import java.util.Collection;


public class BeaconDetactorService extends Service implements IBeaconConsumer {

    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        iBeaconManager.bind(this);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                stopSelf();
            }
        };
        handler.postDelayed(runnable, 10000);
    }

    @Override
    public void onDestroy() {
        iBeaconManager.unBind(this);
        super.onDestroy();
    }

    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i("RangingActivity", "didEnterRegion");
                generateNotification(BeaconDetactorService.this, region.getUniqueId()
                        + ": just saw this iBeacon for the first time");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i("RangingActivity", "didExitRegion");
                generateNotification(BeaconDetactorService.this, region.getUniqueId() + ": is no longer visible");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i("RangingActivity", "didDetermineStateForRegion:" + state);
            }

        });

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    Log.i("RangingActivity", "The first iBeacon I see is about "+iBeacons.iterator().next().getAccuracy()+" meters away.");
                }
            }
        });
        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("f8869d30-e2e4-11e4-b571-0800200c9a66", null, 1, 1));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {

        Intent launchIntent = new Intent(context, MonitoringActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                0,
                new NotificationCompat.Builder(context).setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_launcher).setTicker(message)
                        .setContentTitle(context.getString(R.string.app_name)).setContentText(message)
                        .setContentIntent(PendingIntent.getActivity(context, 0, launchIntent, 0)).setAutoCancel(true)
                        .build());

    }

}