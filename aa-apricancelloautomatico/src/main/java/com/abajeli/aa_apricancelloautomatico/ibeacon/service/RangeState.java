package com.abajeli.aa_apricancelloautomatico.ibeacon.service;

/**
 * Created by abajeli on 14/04/15.
 */
import com.abajeli.aa_apricancelloautomatico.ibeacon.IBeacon;

import java.util.HashSet;
import java.util.Set;

public class RangeState {
    private Callback callback;
    private Set<IBeacon> iBeacons = new HashSet<IBeacon>();

    public RangeState(Callback c) {
        callback = c;
    }

    public Callback getCallback() {
        return callback;
    }
    public void clearIBeacons() {
        iBeacons.clear();
    }
    public Set<IBeacon> getIBeacons() {
        return iBeacons;
    }
    public void addIBeacon(IBeacon iBeacon) {
        iBeacons.add(iBeacon);
    }


}